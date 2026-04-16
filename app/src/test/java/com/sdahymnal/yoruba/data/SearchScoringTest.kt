package com.sdahymnal.yoruba.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchScoringTest {

    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var hymns: List<Hymn>

    private fun makeHymn(
        number: Int,
        title: String = "Title $number",
        englishTitle: String = "English $number",
        refs: Map<String, Int> = emptyMap(),
        lyricsText: List<String> = emptyList(),
    ): Hymn {
        val lyrics = if (lyricsText.isNotEmpty()) {
            val lyricsJson = lyricsText.joinToString(",") { "\"$it\"" }
            """[{"type":"verse","index":1,"lines":[$lyricsJson]}]"""
        } else "[]"

        val refsJson = if (refs.isNotEmpty()) {
            refs.entries.joinToString(",") { "\"${it.key}\": ${it.value}" }
        } else ""

        val jsonStr = """
        {
            "index": "${number.toString().padStart(3, '0')}",
            "number": $number,
            "title": "$title",
            "english_title": "$englishTitle",
            "references": {$refsJson},
            "lyrics": $lyrics
        }
        """
        return json.decodeFromString<Hymn>(jsonStr)
    }

    // Simulate search logic from HymnRepository
    private fun search(query: String, hymnList: List<Hymn>): List<Hymn> {
        val normalised = HymnRepository.removeDiacritics(query.trim())
        if (normalised.isEmpty()) return hymnList

        val isDigits = normalised.all { it.isDigit() }

        data class SearchEntry(
            val hymn: Hymn,
            val number: String,
            val title: String,
            val englishTitle: String,
            val refs: String,
            val lyrics: String,
        )

        val index = hymnList.map { hymn ->
            val lyrics = hymn.lyrics.joinToString(" ") { block ->
                block.textLines.joinToString(" ")
            }
            val refs = hymn.references.entries.joinToString(" ") { "${it.key} ${it.value}" }
            SearchEntry(
                hymn = hymn,
                number = hymn.number.toString(),
                title = HymnRepository.removeDiacritics(hymn.title),
                englishTitle = HymnRepository.removeDiacritics(hymn.englishTitle),
                refs = HymnRepository.removeDiacritics(refs),
                lyrics = HymnRepository.removeDiacritics(lyrics),
            )
        }

        val results = ArrayList<Pair<Hymn, Int>>()

        for (entry in index) {
            var score = 0
            if (entry.number == normalised) score = 100
            else if (isDigits && entry.number.startsWith(normalised)) score = 90
            if (entry.title.contains(normalised)) score = maxOf(score, 80)
            if (entry.englishTitle.contains(normalised)) score = maxOf(score, 70)
            if (entry.refs.contains(normalised)) score = maxOf(score, 60)
            if (score == 0 && entry.lyrics.contains(normalised)) score = 40
            if (score > 0) results.add(entry.hymn to score)
        }

        results.sortWith(compareByDescending<Pair<Hymn, Int>> { it.second }.thenBy { it.first.number })
        return results.map { it.first }
    }

    @Before
    fun setup() {
        hymns = listOf(
            makeHymn(1, "Gbogbo Ẹ̀yìn", "All People", mapOf("SDAH" to 16), listOf("Oluwa ni olorun")),
            makeHymn(16, "Ọba Ńlá", "Great King", mapOf("SDAH" to 42)),
            makeHymn(42, "Jésù Olúgbàlà", "Jesus Savior"),
            makeHymn(100, "Ẹ Wá Yìn Ọba", "Come Praise the King", emptyMap(), listOf("Gbogbo aye")),
            makeHymn(160, "Ìyìn Fún Ọlọ́rùn", "Praise to God"),
        )
    }

    @Test
    fun `exact number match scores highest`() {
        val results = search("42", hymns)
        assertEquals(42, results.first().number)
    }

    @Test
    fun `number prefix matches`() {
        val results = search("1", hymns)
        // 1 exact match (hymn 1), then prefix matches (16, 100, 160)
        assertEquals(1, results.first().number)
        assertTrue(results.any { it.number == 16 })
        assertTrue(results.any { it.number == 160 })
    }

    @Test
    fun `title match scores higher than lyrics`() {
        // "Gbogbo" appears in hymn 1 title and hymn 100 lyrics
        val results = search("Gbogbo", hymns)
        assertEquals(1, results.first().number) // title match = 80
        assertTrue(results.any { it.number == 100 }) // lyrics match = 40
    }

    @Test
    fun `diacritics insensitive search`() {
        val results = search("eyin", hymns)
        assertTrue(results.any { it.number == 1 }) // matches "Ẹ̀yìn"
    }

    @Test
    fun `english title search`() {
        val results = search("Jesus", hymns)
        assertTrue(results.any { it.number == 42 })
    }

    @Test
    fun `reference search`() {
        val results = search("SDAH", hymns)
        assertTrue(results.any { it.number == 1 })
        assertTrue(results.any { it.number == 16 })
    }

    @Test
    fun `empty query returns all hymns`() {
        val results = search("", hymns)
        assertEquals(hymns.size, results.size)
    }

    @Test
    fun `no match returns empty`() {
        val results = search("xyznonexistent", hymns)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `punctuation does not break search`() {
        val results = search("oluwa owo", listOf(
            makeHymn(1, "Test", "Test", emptyMap(), listOf("Olúwà, ọwọ́ Rẹ̀"))
        ))
        assertEquals(1, results.size)
    }

    @Test
    fun `results sorted by score then number`() {
        // Two hymns with same title match - should be sorted by number
        val testHymns = listOf(
            makeHymn(50, "Oluwa Oba"),
            makeHymn(10, "Oluwa Baba"),
        )
        val results = search("Oluwa", testHymns)
        assertEquals(10, results[0].number) // same score, lower number first
        assertEquals(50, results[1].number)
    }
}
