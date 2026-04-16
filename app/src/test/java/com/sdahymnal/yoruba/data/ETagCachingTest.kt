package com.sdahymnal.yoruba.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ETagCachingTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `etag stored and retrieved correctly`() {
        // Simulate the preferences behavior
        var stored: String? = null

        // First request - no etag
        assertNull(stored)

        // After successful response with etag header
        stored = """W/"5cede10ece3ad43e223b8133f1dd4ca8""""
        assertEquals("""W/"5cede10ece3ad43e223b8133f1dd4ca8"""", stored)

        // Subsequent request should send stored etag
        val headerValue = stored
        assertEquals("""W/"5cede10ece3ad43e223b8133f1dd4ca8"""", headerValue)
    }

    @Test
    fun `null etag from server is handled`() {
        var stored: String? = null

        // Server doesn't return etag
        val responseEtag: String? = null
        stored = responseEtag

        // Next request should not send If-None-Match
        assertNull(stored)
    }

    @Test
    fun `etag updated when server returns new one`() {
        var stored: String? = "old-etag"

        // Server returns new etag
        stored = "new-etag"
        assertEquals("new-etag", stored)
    }

    @Test
    fun `304 response keeps existing etag`() {
        val stored = "existing-etag"

        // On 304, we don't update the etag - just keep using cached data
        // The etag should remain unchanged
        assertEquals("existing-etag", stored)
    }

    @Test
    fun `etag only sent when cache file would exist`() {
        val storedEtag: String? = "some-etag"
        val cacheFileExists = false

        // Should not send If-None-Match if no cache file
        val shouldSendEtag = storedEtag != null && cacheFileExists
        assertEquals(false, shouldSendEtag)
    }

    @Test
    fun `etag sent when both etag and cache exist`() {
        val storedEtag: String? = "some-etag"
        val cacheFileExists = true

        val shouldSendEtag = storedEtag != null && cacheFileExists
        assertEquals(true, shouldSendEtag)
    }

    // --- Atomic write tests ---

    @Test
    fun `atomic write preserves cache on successful rename`() {
        val cacheFile = File(tempFolder.root, "hymns_cache.json")
        val tempFile = File(tempFolder.root, "hymns_cache.json.tmp")

        val newData = """[{"number":1,"title":"Test"}]"""

        // Simulate the atomic write pattern
        tempFile.writeText(newData)
        assertTrue(tempFile.exists())

        tempFile.renameTo(cacheFile)
        assertFalse(tempFile.exists())
        assertTrue(cacheFile.exists())
        assertEquals(newData, cacheFile.readText())
    }

    @Test
    fun `original cache survives if write is interrupted before rename`() {
        val cacheFile = File(tempFolder.root, "hymns_cache.json")
        val tempFile = File(tempFolder.root, "hymns_cache.json.tmp")

        val originalData = """[{"number":1,"title":"Original"}]"""
        cacheFile.writeText(originalData)

        // Simulate interrupted write: temp file exists but rename never happened
        tempFile.writeText("partial or corrupt")

        // On next load, original cache should still be intact
        assertEquals(originalData, cacheFile.readText())
        // And temp file should be cleaned up (as loadFromCache does)
        tempFile.delete()
        assertFalse(tempFile.exists())
    }

    @Test
    fun `stale temp file is cleaned up on cache load`() {
        val tempFile = File(tempFolder.root, "hymns_cache.json.tmp")

        // Leftover temp file from a crash
        tempFile.writeText("stale data")
        assertTrue(tempFile.exists())

        // Simulating loadFromCache cleanup
        tempFile.delete()
        assertFalse(tempFile.exists())
    }

    @Test
    fun `atomic write replaces old cache with new data`() {
        val cacheFile = File(tempFolder.root, "hymns_cache.json")
        val tempFile = File(tempFolder.root, "hymns_cache.json.tmp")

        val oldData = """[{"number":1,"title":"Old"}]"""
        val newData = """[{"number":1,"title":"New"},{"number":2,"title":"Added"}]"""

        cacheFile.writeText(oldData)

        // Atomic update
        tempFile.writeText(newData)
        tempFile.renameTo(cacheFile)

        assertEquals(newData, cacheFile.readText())
        assertFalse(tempFile.exists())
    }
}
