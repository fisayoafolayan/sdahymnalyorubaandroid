package com.sdahymnal.yoruba.data

import android.content.Context
import com.sdahymnal.yoruba.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Request
import java.io.File
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.Normalizer

sealed class HymnLoadState {
    data object Loading : HymnLoadState()
    data class Ready(val hymns: List<Hymn>) : HymnLoadState()
    data class Error(val message: String) : HymnLoadState()
}

class HymnRepository(private val context: Context, private val preferences: Preferences) {

    private val json = Json { ignoreUnknownKeys = true }
    private val cacheFile = File(context.filesDir, "hymns_cache.json")
    private val cacheTempFile = File(context.filesDir, "hymns_cache.json.tmp")

    // Reuses shared connection pool and dispatcher from HttpClient.base
    private val client = HttpClient.base

    private val _state = MutableStateFlow<HymnLoadState>(HymnLoadState.Loading)
    val state: StateFlow<HymnLoadState> = _state

    // Pre-built search index
    private var searchIndex: List<SearchEntry> = emptyList()

    val hymns: List<Hymn>
        get() = (_state.value as? HymnLoadState.Ready)?.hymns.orEmpty()

    suspend fun load() {
        val cached = loadFromCache()
        if (cached != null) {
            _state.value = HymnLoadState.Ready(cached)
            searchIndex = buildIndex(cached)
        } else {
            _state.value = HymnLoadState.Loading
        }

        try {
            val fresh = fetchFromNetwork()
            if (fresh != null) {
                _state.value = HymnLoadState.Ready(fresh)
                searchIndex = buildIndex(fresh)
            }
        } catch (e: Exception) {
            if (cached == null) {
                _state.value = HymnLoadState.Error(errorMessage(e))
            }
        }
    }

    /** Scored search matching the web app's algorithm. */
    fun search(query: String): List<Hymn> {
        if (query.isBlank()) return hymns

        val normalised = removeDiacritics(query.trim())
        if (normalised.isEmpty()) return hymns

        val isDigits = normalised.all { it.isDigit() }

        // Reuse a mutable list to avoid allocations
        val results = ArrayList<Pair<Hymn, Int>>(searchIndex.size / 4)

        for (entry in searchIndex) {
            var score = 0

            if (entry.number == normalised) {
                score = 100
            } else if (isDigits && entry.number.startsWith(normalised)) {
                score = 90
            }
            if (entry.title.contains(normalised)) score = maxOf(score, 80)
            if (entry.englishTitle.contains(normalised)) score = maxOf(score, 70)
            if (entry.refs.contains(normalised)) score = maxOf(score, 60)
            if (score == 0 && entry.lyrics.contains(normalised)) score = 40

            if (score > 0) results.add(entry.hymn to score)
        }

        results.sortWith(compareByDescending<Pair<Hymn, Int>> { it.second }.thenBy { it.first.number })
        return results.map { it.first }
    }

    fun getByNumber(number: Int): Hymn? = hymns.find { it.number == number }

    private fun buildIndex(hymns: List<Hymn>): List<SearchEntry> = hymns.map { hymn ->
        val lyrics = hymn.lyrics.joinToString(" ") { block ->
            when (block.type) {
                "call_response" -> block.callResponseLines.joinToString(" ") { it.text }
                else -> block.textLines.joinToString(" ")
            }
        }
        val refs = hymn.references.entries.joinToString(" ") { "${it.key} ${it.value}" }

        SearchEntry(
            hymn = hymn,
            number = hymn.number.toString(),
            title = removeDiacritics(hymn.title),
            englishTitle = removeDiacritics(hymn.englishTitle),
            refs = removeDiacritics(refs),
            lyrics = removeDiacritics(lyrics),
        )
    }

    private fun loadFromCache(): List<Hymn>? {
        // Clean up stale temp file from a previously interrupted write
        cacheTempFile.delete()
        if (!cacheFile.exists()) return null
        return try {
            val text = cacheFile.readText()
            json.decodeFromString<List<Hymn>>(text).sortedBy { it.number }
        } catch (_: Exception) { null }
    }

    /** Returns new hymns if changed, null if server returned 304 Not Modified. */
    private suspend fun fetchFromNetwork(): List<Hymn>? = withContext(Dispatchers.IO) {
        val requestBuilder = Request.Builder().url(HYMNS_URL)

        // Send stored ETag so server can return 304 if nothing changed
        val storedEtag = preferences.hymnsEtag
        if (storedEtag != null && cacheFile.exists()) {
            requestBuilder.header("If-None-Match", storedEtag)
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            when (response.code) {
                304 -> {
                    // Not modified - cached data is still fresh
                    null
                }
                in 200..299 -> {
                    val body = response.body?.string() ?: throw Exception("Empty response")
                    val hymns = json.decodeFromString<List<Hymn>>(body).sortedBy { it.number }
                    // Atomic write: temp file then rename, so a crash mid-write
                    // can't corrupt the cache and break offline access
                    cacheTempFile.writeText(body)
                    cacheTempFile.renameTo(cacheFile)
                    preferences.hymnsEtag = response.header("ETag")
                    hymns
                }
                else -> throw Exception("HTTP ${response.code}")
            }
        }
    }

    private data class SearchEntry(
        val hymn: Hymn,
        val number: String,
        val title: String,
        val englishTitle: String,
        val refs: String,
        val lyrics: String,
    )

    private fun errorMessage(e: Exception): String = when (e) {
        is UnknownHostException ->
            context.getString(R.string.error_no_internet)
        is SocketTimeoutException ->
            context.getString(R.string.error_timeout)
        is kotlinx.serialization.SerializationException ->
            context.getString(R.string.error_invalid_data)
        else -> {
            val msg = e.message
            if (msg != null && msg.startsWith("HTTP ")) {
                val code = msg.removePrefix("HTTP ").toIntOrNull()
                when (code) {
                    in 500..599 -> context.getString(R.string.error_server)
                    403 -> context.getString(R.string.error_forbidden)
                    404 -> context.getString(R.string.error_not_found)
                    else -> context.getString(R.string.error_http, code ?: 0)
                }
            } else {
                context.getString(R.string.error_generic)
            }
        }
    }

    companion object {
        private const val HYMNS_URL = "https://sdahymnalyoruba.com/hymns.json"

        // Pre-compiled regexes - avoids recompiling on every call
        private val DIACRITICS_REGEX = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        private val NON_ALNUM_REGEX = "[^a-z0-9\\s]".toRegex(RegexOption.IGNORE_CASE)

        fun removeDiacritics(text: String): String {
            return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replace(DIACRITICS_REGEX, "")
                .replace(NON_ALNUM_REGEX, "")
                .lowercase()
        }
    }
}
