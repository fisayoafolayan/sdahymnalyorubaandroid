package com.sdahymnalyoruba.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HymnTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parses hymn with verses`() {
        val jsonStr = """
        {
            "index": "001",
            "number": 1,
            "title": "Gbogbo Ẹ̀yin Tí Ń Gbé Ayé",
            "english_title": "All People On Earth Do Dwell",
            "references": {"SDAH": 16, "NAH": 2},
            "lyrics": [
                {
                    "type": "verse",
                    "index": 1,
                    "lines": ["Line one", "Line two"]
                }
            ],
            "revision": 3
        }
        """
        val hymn = json.decodeFromString<Hymn>(jsonStr)
        assertEquals(1, hymn.number)
        assertEquals("Gbogbo Ẹ̀yin Tí Ń Gbé Ayé", hymn.title)
        assertEquals("All People On Earth Do Dwell", hymn.englishTitle)
        assertEquals(16, hymn.references["SDAH"])
        assertEquals(2, hymn.references["NAH"])
        assertEquals(1, hymn.lyrics.size)
        assertEquals("verse", hymn.lyrics[0].type)
        assertEquals(listOf("Line one", "Line two"), hymn.lyrics[0].textLines)
    }

    @Test
    fun `parses hymn with chorus`() {
        val jsonStr = """
        {
            "index": "002",
            "number": 2,
            "title": "Test",
            "english_title": "Test English",
            "lyrics": [
                {
                    "type": "chorus",
                    "index": 1,
                    "lines": ["Chorus line"]
                }
            ]
        }
        """
        val hymn = json.decodeFromString<Hymn>(jsonStr)
        assertEquals("chorus", hymn.lyrics[0].type)
        assertEquals(listOf("Chorus line"), hymn.lyrics[0].textLines)
    }

    @Test
    fun `parses call and response`() {
        val jsonStr = """
        {
            "index": "003",
            "number": 3,
            "title": "Test",
            "english_title": "Test",
            "lyrics": [
                {
                    "type": "call_response",
                    "index": 1,
                    "lines": [
                        {"part": "leader", "text": "Leader sings"},
                        {"part": "congregation", "text": "All respond"}
                    ]
                }
            ]
        }
        """
        val hymn = json.decodeFromString<Hymn>(jsonStr)
        val block = hymn.lyrics[0]
        assertEquals("call_response", block.type)
        assertEquals(2, block.callResponseLines.size)
        assertEquals("leader", block.callResponseLines[0].part)
        assertEquals("Leader sings", block.callResponseLines[0].text)
        assertEquals("congregation", block.callResponseLines[1].part)
        assertEquals("All respond", block.callResponseLines[1].text)
    }

    @Test
    fun `empty references default to empty map`() {
        val jsonStr = """
        {
            "index": "004",
            "number": 4,
            "title": "Test",
            "english_title": "Test",
            "lyrics": []
        }
        """
        val hymn = json.decodeFromString<Hymn>(jsonStr)
        assertTrue(hymn.references.isEmpty())
    }

    @Test
    fun `parses list of hymns`() {
        val jsonStr = """
        [
            {"index": "001", "number": 1, "title": "A", "english_title": "A", "lyrics": []},
            {"index": "002", "number": 2, "title": "B", "english_title": "B", "lyrics": []}
        ]
        """
        val hymns = json.decodeFromString<List<Hymn>>(jsonStr)
        assertEquals(2, hymns.size)
        assertEquals(1, hymns[0].number)
        assertEquals(2, hymns[1].number)
    }
}
