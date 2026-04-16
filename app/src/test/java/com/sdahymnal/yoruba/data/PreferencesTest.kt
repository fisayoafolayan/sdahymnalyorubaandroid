package com.sdahymnal.yoruba.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferencesTest {

    @Test
    fun `READING_SIZES has three levels`() {
        assertEquals(3, Preferences.READING_SIZES.size)
        assertEquals(1.0f, Preferences.READING_SIZES[0])
        assertEquals(1.2f, Preferences.READING_SIZES[1])
        assertEquals(1.45f, Preferences.READING_SIZES[2])
    }

    @Test
    fun `reading size cycle wraps around`() {
        val sizes = Preferences.READING_SIZES
        // Simulate cycling
        var idx = 0
        idx = (idx + 1) % sizes.size
        assertEquals(1.2f, sizes[idx])
        idx = (idx + 1) % sizes.size
        assertEquals(1.45f, sizes[idx])
        idx = (idx + 1) % sizes.size
        assertEquals(1.0f, sizes[idx]) // wraps back
    }

    @Test
    fun `favorites toggle adds and removes`() {
        val initial = emptySet<Int>()

        // Add
        val afterAdd = initial.toMutableSet()
        afterAdd.add(42)
        assertTrue(42 in afterAdd)

        // Remove
        afterAdd.remove(42)
        assertFalse(42 in afterAdd)
    }

    @Test
    fun `favorites parsing from comma string`() {
        val raw = "1,42,100"
        val parsed = raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        assertEquals(setOf(1, 42, 100), parsed)
    }

    @Test
    fun `favorites parsing handles empty string`() {
        val raw = ""
        val parsed = if (raw.isBlank()) emptySet()
        else raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        assertTrue(parsed.isEmpty())
    }

    @Test
    fun `favorites serialization roundtrip`() {
        val favorites = setOf(5, 10, 200)
        val serialized = favorites.joinToString(",")
        val deserialized = serialized.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        assertEquals(favorites, deserialized)
    }
}
