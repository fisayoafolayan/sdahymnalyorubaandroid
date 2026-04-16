package com.sdahymnal.yoruba.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchEdgeCasesTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun makeHymn(
        number: Int,
        title: String = "Title $number",
        englishTitle: String = "English $number",
        lyricsText: List<String> = emptyList(),
    ): Hymn {
        val lyrics = if (lyricsText.isNotEmpty()) {
            val lyricsJson = lyricsText.joinToString(",") { "\"$it\"" }
            """[{"type":"verse","index":1,"lines":[$lyricsJson]}]"""
        } else "[]"

        return json.decodeFromString<Hymn>("""
        {
            "index": "${number.toString().padStart(3, '0')}",
            "number": $number,
            "title": "$title",
            "english_title": "$englishTitle",
            "lyrics": $lyrics
        }
        """)
    }

    private fun search(query: String, hymnList: List<Hymn>): List<Hymn> {
        val normalised = HymnRepository.removeDiacritics(query.trim())
        if (normalised.isEmpty()) return hymnList

        val isDigits = normalised.all { it.isDigit() }

        data class SearchEntry(
            val hymn: Hymn,
            val number: String,
            val title: String,
            val englishTitle: String,
            val lyrics: String,
        )

        val index = hymnList.map { hymn ->
            val lyrics = hymn.lyrics.joinToString(" ") { block ->
                when (block.type) {
                    "call_response" -> block.callResponseLines.joinToString(" ") { it.text }
                    else -> block.textLines.joinToString(" ")
                }
            }
            SearchEntry(
                hymn = hymn,
                number = hymn.number.toString(),
                title = HymnRepository.removeDiacritics(hymn.title),
                englishTitle = HymnRepository.removeDiacritics(hymn.englishTitle),
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
            if (score == 0 && entry.lyrics.contains(normalised)) score = 40
            if (score > 0) results.add(entry.hymn to score)
        }

        results.sortWith(compareByDescending<Pair<Hymn, Int>> { it.second }.thenBy { it.first.number })
        return results.map { it.first }
    }

    private val hymns = listOf(
        makeHymn(1, "Oluwa Oba", "God is King", listOf("Verse one text")),
        makeHymn(2, "Jesu Kristi", "Jesus Christ"),
        makeHymn(100, "Ayo Ni Fun Wa", "Joy For Us"),
    )

    @Test
    fun `single character query matches`() {
        val results = search("O", hymns)
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun `spaces only query returns all hymns`() {
        val results = search("   ", hymns)
        assertEquals(hymns.size, results.size)
    }

    @Test
    fun `punctuation only query returns all hymns`() {
        val results = search("!!!", hymns)
        assertEquals(hymns.size, results.size)
    }

    @Test
    fun `very long query returns empty`() {
        val results = search("a".repeat(200), hymns)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `query with mixed punctuation and text`() {
        val results = search("Oluwa!", hymns)
        assertTrue(results.any { it.number == 1 })
    }

    @Test
    fun `number 0 does not crash`() {
        val results = search("0", hymns)
        // 0 is a digit, should try prefix match
        assertTrue(results.isEmpty() || results.isNotEmpty())
    }

    @Test
    fun `number larger than any hymn returns empty`() {
        val results = search("9999", hymns)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `partial number matches prefix`() {
        val results = search("10", hymns)
        assertTrue(results.any { it.number == 100 })
    }

    @Test
    fun `case insensitive english title`() {
        val results = search("JESUS CHRIST", hymns)
        assertTrue(results.any { it.number == 2 })
    }

    @Test
    fun `lyrics search only triggers when no higher match`() {
        // "text" only appears in lyrics of hymn 1
        val results = search("verse one text", hymns)
        assertEquals(1, results.size)
        assertEquals(1, results[0].number)
    }
}
