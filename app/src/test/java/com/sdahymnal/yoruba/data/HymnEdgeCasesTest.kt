package com.sdahymnal.yoruba.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HymnEdgeCasesTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `hymn with no lyrics`() {
        val hymn = json.decodeFromString<Hymn>("""
        {
            "index": "001",
            "number": 1,
            "title": "Test",
            "english_title": "Test",
            "lyrics": []
        }
        """)
        assertTrue(hymn.lyrics.isEmpty())
    }

    @Test
    fun `hymn with only call response blocks`() {
        val hymn = json.decodeFromString<Hymn>("""
        {
            "index": "001",
            "number": 1,
            "title": "Test",
            "english_title": "Test",
            "lyrics": [
                {
                    "type": "call_response",
                    "index": 1,
                    "lines": [
                        {"part": "leader", "text": "Leader part"},
                        {"part": "congregation", "text": "All part"}
                    ]
                }
            ]
        }
        """)
        assertEquals(1, hymn.lyrics.size)
        assertEquals("call_response", hymn.lyrics[0].type)
        assertTrue(hymn.lyrics[0].textLines.isEmpty())
        assertEquals(2, hymn.lyrics[0].callResponseLines.size)
    }

    @Test
    fun `hymn with empty references map`() {
        val hymn = json.decodeFromString<Hymn>("""
        {
            "index": "001",
            "number": 1,
            "title": "Test",
            "english_title": "Test",
            "references": {},
            "lyrics": []
        }
        """)
        assertTrue(hymn.references.isEmpty())
    }

    @Test
    fun `hymn with many references`() {
        val hymn = json.decodeFromString<Hymn>("""
        {
            "index": "001",
            "number": 1,
            "title": "Test",
            "english_title": "Test",
            "references": {"SDAH": 16, "NAH": 2, "CH": 14, "AH": 100},
            "lyrics": []
        }
        """)
        assertEquals(4, hymn.references.size)
        assertEquals(16, hymn.references["SDAH"])
        assertEquals(100, hymn.references["AH"])
    }

    @Test
    fun `hymn with mixed verse and chorus`() {
        val hymn = json.decodeFromString<Hymn>("""
        {
            "index": "001",
            "number": 1,
            "title": "Test",
            "english_title": "Test",
            "lyrics": [
                {"type": "verse", "index": 1, "lines": ["V1 line"]},
                {"type": "chorus", "index": 1, "lines": ["Chorus line"]},
                {"type": "verse", "index": 2, "lines": ["V2 line"]}
            ]
        }
        """)
        assertEquals(3, hymn.lyrics.size)
        assertEquals("verse", hymn.lyrics[0].type)
        assertEquals("chorus", hymn.lyrics[1].type)
        assertEquals("verse", hymn.lyrics[2].type)
    }

    @Test
    fun `hymn with unicode diacritics in title`() {
        val hymn = json.decodeFromString<Hymn>("""
        {
            "index": "001",
            "number": 1,
            "title": "Gbogbo \u1eb8\u0300y\u00ecn T\u00ed \u0143 Gb\u00e9 Ay\u00e9",
            "english_title": "All People On Earth Do Dwell",
            "lyrics": []
        }
        """)
        assertTrue(hymn.title.isNotEmpty())
        assertEquals("All People On Earth Do Dwell", hymn.englishTitle)
    }

    @Test
    fun `hymn with zero revision defaults`() {
        val hymn = json.decodeFromString<Hymn>("""
        {
            "index": "001",
            "number": 1,
            "title": "Test",
            "english_title": "Test",
            "lyrics": []
        }
        """)
        assertEquals(0, hymn.revision)
    }

    @Test
    fun `hymn list sorted by number`() {
        val hymns = json.decodeFromString<List<Hymn>>("""
        [
            {"index": "003", "number": 3, "title": "C", "english_title": "C", "lyrics": []},
            {"index": "001", "number": 1, "title": "A", "english_title": "A", "lyrics": []},
            {"index": "002", "number": 2, "title": "B", "english_title": "B", "lyrics": []}
        ]
        """).sortedBy { it.number }

        assertEquals(1, hymns[0].number)
        assertEquals(2, hymns[1].number)
        assertEquals(3, hymns[2].number)
    }
}
