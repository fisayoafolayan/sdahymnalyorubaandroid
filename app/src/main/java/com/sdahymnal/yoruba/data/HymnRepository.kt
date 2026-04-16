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

    // Pre-built search index and number lookup, swapped atomically
    private data class HymnIndex(
        val searchEntries: List<SearchEntry> = emptyList(),
        val byNumber: Map<Int, Hymn> = emptyMap(),
    )

    @Volatile
    private var index: HymnIndex = HymnIndex()

    val hymns: List<Hymn>
        get() = (_state.value as? HymnLoadState.Ready)?.hymns.orEmpty()

    suspend fun load() {
        val cached = loadFromCache()
        if (cached != null) {
            _state.value = HymnLoadState.Ready(cached)
            index = HymnIndex(buildIndex(cached), cached.associateBy { it.number })
        } else {
            _state.value = HymnLoadState.Loading
        }

        try {
            val fresh = fetchFromNetwork()
            if (fresh != null) {
                _state.value = HymnLoadState.Ready(fresh)
                index = HymnIndex(buildIndex(fresh), fresh.associateBy { it.number })
            }
        } catch (e: Exception) {
            if (cached == null) {
                _state.value = HymnLoadState.Error(errorMessage(e))
            }
        }
    }

    /** Scored search matching the web app's algorithm.
     *  Tries exact substring first, then falls back to all-words matching. */
    fun search(query: String): List<Hymn> {
        if (query.isBlank()) return hymns

        val normalised = removeDiacritics(query.trim())
        if (normalised.isEmpty()) return hymns

        val isDigits = normalised.all { it.isDigit() }
        val words = normalised.split(' ').filter { it.isNotEmpty() }
        val spaceless = normalised.replace(" ", "")

        // Snapshot the index once so a concurrent swap can't affect this search
        val entries = index.searchEntries
        val results = ArrayList<Pair<Hymn, Int>>(entries.size / 4)

        for (entry in entries) {
            var score = 0

            if (entry.number == normalised) {
                score = 100
            } else if (isDigits && entry.number.startsWith(normalised)) {
                score = 90
            }
            val titleQ = matchQuality(entry.title, entry.titleSpaceless, normalised, spaceless, words)
            if (titleQ == 2) score = maxOf(score, 80)
            else if (titleQ == 1) score = maxOf(score, 75)

            val engQ = matchQuality(entry.englishTitle, entry.englishTitleSpaceless, normalised, spaceless, words)
            if (engQ == 2) score = maxOf(score, 70)
            else if (engQ == 1) score = maxOf(score, 65)

            val refsQ = matchQuality(entry.refs, entry.refsSpaceless, normalised, spaceless, words)
            if (refsQ == 2) score = maxOf(score, 60)
            else if (refsQ == 1) score = maxOf(score, 55)

            if (score == 0) {
                val lyricsQ = matchQuality(entry.lyrics, entry.lyricsSpaceless, normalised, spaceless, words)
                if (lyricsQ == 2) score = 40
                else if (lyricsQ == 1) score = 35
            }

            if (score > 0) results.add(entry.hymn to score)
        }

        results.sortWith(compareByDescending<Pair<Hymn, Int>> { it.second }.thenBy { it.first.number })
        return results.map { it.first }
    }

    /** Match quality: 2 = exact/spaceless substring, 1 = all words present, 0 = no match. */
    private fun matchQuality(
        text: String, textSpaceless: String,
        query: String, querySpaceless: String,
        words: List<String>,
    ): Int {
        if (text.contains(query)) return 2
        if (textSpaceless.contains(querySpaceless)) return 2
        if (words.size > 1 && words.all { text.contains(it) || textSpaceless.contains(it) }) return 1
        return 0
    }

    fun getByNumber(number: Int): Hymn? = index.byNumber[number]

    private fun buildIndex(hymns: List<Hymn>): List<SearchEntry> = hymns.map { hymn ->
        val lyrics = hymn.lyrics.joinToString(" ") { block ->
            when (block.type) {
                "call_response" -> block.callResponseLines.joinToString(" ") { it.text }
                else -> block.textLines.joinToString(" ")
            }
        }
        val refs = hymn.references.entries.joinToString(" ") { "${it.key} ${it.value}" }
        val normTitle = removeDiacritics(hymn.title)
        val normEng = removeDiacritics(hymn.englishTitle)
        val normRefs = removeDiacritics(refs)
        val normLyrics = removeDiacritics(lyrics)

        SearchEntry(
            hymn = hymn,
            number = hymn.number.toString(),
            title = normTitle,
            titleSpaceless = normTitle.replace(" ", ""),
            englishTitle = normEng,
            englishTitleSpaceless = normEng.replace(" ", ""),
            refs = normRefs,
            refsSpaceless = normRefs.replace(" ", ""),
            lyrics = normLyrics,
            lyricsSpaceless = normLyrics.replace(" ", ""),
        )
    }

    private fun loadFromCache(): List<Hymn>? {
        // Clean up stale temp file from a previously interrupted write
        cacheTempFile.delete()
        if (!cacheFile.exists()) return null
        return try {
            val text = cacheFile.readText()
            json.decodeFromString<List<Hymn>>(text).sortedBy { it.number }
        } catch (e: Exception) {
            io.sentry.Sentry.captureException(e)
            null
        }
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
                    val body = response.body.string()
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
        val titleSpaceless: String,
        val englishTitle: String,
        val englishTitleSpaceless: String,
        val refs: String,
        val refsSpaceless: String,
        val lyrics: String,
        val lyricsSpaceless: String,
    )

    private fun errorMessage(e: Exception): String = when (e) {
        is UnknownHostException ->
            context.getString(R.string.error_no_internet)
        is SocketTimeoutException ->
            context.getString(R.string.error_timeout)
        is kotlinx.serialization.SerializationException ->
            context.getString(R.string.error_invalid_data)
        else -> when (val msg = e.message) {
            null -> context.getString(R.string.error_generic)
            else -> if (msg.startsWith("HTTP ")) {
                when (val code = msg.removePrefix("HTTP ").toIntOrNull()) {
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
