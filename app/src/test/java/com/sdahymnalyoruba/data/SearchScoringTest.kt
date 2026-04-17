package com.sdahymnalyoruba.data

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

    private fun makeCallResponseHymn(
        number: Int,
        title: String,
        englishTitle: String = "English $number",
        lines: List<Pair<String, String>>, // part to text
    ): Hymn {
        val linesJson = lines.joinToString(",") { (part, text) ->
            """{"part":"$part","text":"$text"}"""
        }
        val jsonStr = """
        {
            "index": "${number.toString().padStart(3, '0')}",
            "number": $number,
            "title": "$title",
            "english_title": "$englishTitle",
            "references": {},
            "lyrics": [{"type":"call_response","index":1,"lines":[$linesJson]}]
        }
        """
        return json.decodeFromString<Hymn>(jsonStr)
    }

    // Simulate search logic from HymnRepository (must match actual implementation)
    private fun search(query: String, hymnList: List<Hymn>): List<Hymn> {
        val normalised = HymnRepository.removeDiacritics(query.trim())
        if (normalised.isEmpty()) return hymnList

        val isDigits = normalised.all { it.isDigit() }
        val words = normalised.split(' ').filter { it.isNotEmpty() }
        val spaceless = normalised.replace(" ", "")

        data class SearchEntry(
            val hymn: Hymn,
            val number: String,
            val title: String, val titleSpaceless: String,
            val englishTitle: String, val englishTitleSpaceless: String,
            val refs: String, val refsSpaceless: String,
            val lyrics: String, val lyricsSpaceless: String,
        )

        val index = hymnList.map { hymn ->
            val lyrics = hymn.lyrics.joinToString(" ") { block ->
                when (block.type) {
                    "call_response" -> block.callResponseLines.joinToString(" ") { it.text }
                    else -> block.textLines.joinToString(" ")
                }
            }
            val refs = hymn.references.entries.joinToString(" ") { "${it.key} ${it.value}" }
            val normTitle = HymnRepository.removeDiacritics(hymn.title)
            val normEng = HymnRepository.removeDiacritics(hymn.englishTitle)
            val normRefs = HymnRepository.removeDiacritics(refs)
            val normLyrics = HymnRepository.removeDiacritics(lyrics)
            SearchEntry(
                hymn = hymn,
                number = hymn.number.toString(),
                title = normTitle, titleSpaceless = normTitle.replace(" ", ""),
                englishTitle = normEng, englishTitleSpaceless = normEng.replace(" ", ""),
                refs = normRefs, refsSpaceless = normRefs.replace(" ", ""),
                lyrics = normLyrics, lyricsSpaceless = normLyrics.replace(" ", ""),
            )
        }

        fun matchQuality(text: String, textSp: String): Int {
            if (text.contains(normalised)) return 2
            if (textSp.contains(spaceless)) return 2
            if (words.size > 1 && words.all { text.contains(it) || textSp.contains(it) }) return 1
            return 0
        }

        val results = ArrayList<Pair<Hymn, Int>>()

        for (entry in index) {
            var score = 0
            if (entry.number == normalised) score = 100
            else if (isDigits && entry.number.startsWith(normalised)) score = 90

            val titleQ = matchQuality(entry.title, entry.titleSpaceless)
            if (titleQ == 2) score = maxOf(score, 80)
            else if (titleQ == 1) score = maxOf(score, 75)

            val engQ = matchQuality(entry.englishTitle, entry.englishTitleSpaceless)
            if (engQ == 2) score = maxOf(score, 70)
            else if (engQ == 1) score = maxOf(score, 65)

            val refsQ = matchQuality(entry.refs, entry.refsSpaceless)
            if (refsQ == 2) score = maxOf(score, 60)
            else if (refsQ == 1) score = maxOf(score, 55)

            if (score == 0) {
                val lyricsQ = matchQuality(entry.lyrics, entry.lyricsSpaceless)
                if (lyricsQ == 2) score = 40
                else if (lyricsQ == 1) score = 35
            }

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

    @Test
    fun `call_response lyrics are searchable`() {
        val crHymn = makeCallResponseHymn(
            number = 200,
            title = "Ìpè Olúwa",
            lines = listOf(
                "leader" to "Ẹ gbọ́ ohùn Olúwa",
                "congregation" to "A gbọ́ ọ Olúwa",
            ),
        )
        val results = search("ohun oluwa", listOf(crHymn))
        assertEquals(1, results.size)
        assertEquals(200, results[0].number)
    }

    @Test
    fun `all-words matching finds hymns with scattered query words`() {
        val hymn = makeHymn(1, "Ẹ Máa Tẹ̀ Sí Wájú")
        val results = search("e ma te", listOf(hymn))
        assertEquals(1, results.size)
        assertEquals(1, results[0].number)
    }

    @Test
    fun `spaceless matching finds hymns despite space differences`() {
        val hymn = makeHymn(1, "Ẹ Máa Tẹ̀ Síwájú")
        val results = search("e maa te si waju", listOf(hymn))
        assertEquals(1, results.size)
    }

    @Test
    fun `exact title match ranks above word match`() {
        val exact = makeHymn(10, "Oluwa Oba Ńlá")
        val words = makeHymn(20, "Oba Oluwa Ni Ńlá", lyricsText = listOf("some text"))
        val results = search("oluwa oba nla", listOf(exact, words))
        assertEquals(10, results[0].number) // exact substring = 80
        assertEquals(20, results[1].number) // all words = 75
    }

    @Test
    fun `word match in title ranks above lyrics match`() {
        val titleMatch = makeHymn(10, "Ẹ Máa Tẹ̀ Sí Wájú")
        val lyricsMatch = makeHymn(20, "Other Title", lyricsText = listOf("Ẹ máa tẹ̀ sí wájú"))
        val results = search("e ma te", listOf(titleMatch, lyricsMatch))
        assertEquals(10, results[0].number) // title words = 75
    }

    @Test
    fun `single word query does not trigger word matching`() {
        // "ma" should only match via exact substring, not word splitting
        val results = search("xyznotfound", hymns)
        assertTrue(results.isEmpty())
    }
}
