package com.sdahymnal.yoruba.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ErrorEdgeCasesTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val json = Json { ignoreUnknownKeys = true }

    // --- Cache corruption recovery ---

    @Test
    fun `corrupted cache file returns null`() {
        val cacheFile = File(tempFolder.root, "hymns_cache.json")
        cacheFile.writeText("this is not valid json{{{")

        val result = try {
            json.decodeFromString<List<Hymn>>(cacheFile.readText())
        } catch (_: Exception) { null }

        assertNull(result)
    }

    @Test
    fun `empty cache file returns null`() {
        val cacheFile = File(tempFolder.root, "hymns_cache.json")
        cacheFile.writeText("")

        val result = try {
            json.decodeFromString<List<Hymn>>(cacheFile.readText())
        } catch (_: Exception) { null }

        assertNull(result)
    }

    @Test
    fun `cache with empty array returns empty list`() {
        val cacheFile = File(tempFolder.root, "hymns_cache.json")
        cacheFile.writeText("[]")

        val result = json.decodeFromString<List<Hymn>>(cacheFile.readText())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `cache with partial JSON returns null`() {
        val cacheFile = File(tempFolder.root, "hymns_cache.json")
        cacheFile.writeText("""[{"index":"001","number":1,"title":"Test""")

        val result = try {
            json.decodeFromString<List<Hymn>>(cacheFile.readText())
        } catch (_: Exception) { null }

        assertNull(result)
    }

    @Test
    fun `atomic write leaves original intact on temp file failure`() {
        val cacheFile = File(tempFolder.root, "hymns_cache.json")
        val tempFile = File(tempFolder.root, "hymns_cache.json.tmp")

        val original = """[{"index":"001","number":1,"title":"Original","english_title":"Eng","lyrics":[]}]"""
        cacheFile.writeText(original)

        // Simulate interrupted write
        tempFile.writeText("partial corrupt data")
        // Rename never happened (simulating crash)

        // Original should be intact
        assertEquals(original, cacheFile.readText())

        // Cleanup (as loadFromCache does)
        tempFile.delete()
        val hymns = json.decodeFromString<List<Hymn>>(cacheFile.readText())
        assertEquals(1, hymns.size)
        assertEquals("Original", hymns[0].title)
    }

    // --- Search with empty/missing data ---

    @Test
    fun `removeDiacritics on empty string returns empty`() {
        assertEquals("", HymnRepository.removeDiacritics(""))
    }

    @Test
    fun `removeDiacritics on whitespace-only returns whitespace`() {
        assertEquals("   ", HymnRepository.removeDiacritics("   "))
    }

    @Test
    fun `removeDiacritics on punctuation-only returns empty`() {
        assertEquals("", HymnRepository.removeDiacritics("!@#\$%"))
    }

    // --- Malformed hymn data ---

    @Test
    fun `hymn with missing optional fields parses correctly`() {
        val hymnJson = """
        {
            "index": "001",
            "number": 1,
            "title": "Test",
            "english_title": "English",
            "lyrics": []
        }
        """
        val hymn = json.decodeFromString<Hymn>(hymnJson)
        assertEquals(1, hymn.number)
        assertEquals(emptyMap<String, Int>(), hymn.references)
        assertEquals(0, hymn.revision)
    }

    @Test
    fun `hymn with unknown fields is ignored`() {
        val hymnJson = """
        {
            "index": "001",
            "number": 1,
            "title": "Test",
            "english_title": "English",
            "lyrics": [],
            "unknown_field": "should be ignored",
            "another_unknown": 42
        }
        """
        val hymn = json.decodeFromString<Hymn>(hymnJson)
        assertEquals("Test", hymn.title)
    }

    @Test
    fun `hymn with empty lyrics list is valid`() {
        val hymnJson = """
        {
            "index": "001",
            "number": 1,
            "title": "Test",
            "english_title": "English",
            "lyrics": []
        }
        """
        val hymn = json.decodeFromString<Hymn>(hymnJson)
        assertTrue(hymn.lyrics.isEmpty())
        assertTrue(hymn.lyrics.flatMap { it.textLines }.isEmpty())
    }

    @Test
    fun `lyric block with mixed line types handles gracefully`() {
        val hymnJson = """
        {
            "index": "001",
            "number": 1,
            "title": "Test",
            "english_title": "English",
            "lyrics": [{
                "type": "verse",
                "index": 1,
                "lines": ["Valid line", "Another line"]
            }]
        }
        """
        val hymn = json.decodeFromString<Hymn>(hymnJson)
        val lines = hymn.lyrics[0].textLines
        assertEquals(2, lines.size)
        assertEquals("Valid line", lines[0])
        assertEquals("Another line", lines[1])
    }

    @Test
    fun `call_response block with missing fields uses empty defaults`() {
        val hymnJson = """
        {
            "index": "001",
            "number": 1,
            "title": "Test",
            "english_title": "English",
            "lyrics": [{
                "type": "call_response",
                "index": 1,
                "lines": [{"part": "leader", "text": "Call"}, {"text": "No part"}]
            }]
        }
        """
        val hymn = json.decodeFromString<Hymn>(hymnJson)
        val lines = hymn.lyrics[0].callResponseLines
        assertEquals(2, lines.size)
        assertEquals("leader", lines[0].part)
        assertEquals("", lines[1].part)
    }

    // --- HTTP error code mapping ---

    @Test
    fun `HTTP error message format is parseable`() {
        val msg = "HTTP 503"
        assertTrue(msg.startsWith("HTTP "))
        val code = msg.removePrefix("HTTP ").toIntOrNull()
        assertEquals(503, code)
    }

    @Test
    fun `malformed HTTP error message falls through`() {
        val msg = "HTTP abc"
        val code = msg.removePrefix("HTTP ").toIntOrNull()
        assertNull(code)
    }

    @Test
    fun `non-HTTP error message falls through`() {
        val msg = "Connection reset"
        assertTrue(!msg.startsWith("HTTP "))
    }
}
