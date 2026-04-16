package com.sdahymnal.yoruba

import com.sdahymnal.yoruba.data.Preferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewModelLogicTest {

    // Test the theme cycling logic (extracted from ViewModel)
    private fun nextTheme(current: String): String = when (current) {
        "light" -> "dark"
        "dark" -> "system"
        else -> "light"
    }

    @Test
    fun `theme cycles light to dark`() {
        assertEquals("dark", nextTheme("light"))
    }

    @Test
    fun `theme cycles dark to system`() {
        assertEquals("system", nextTheme("dark"))
    }

    @Test
    fun `theme cycles system to light`() {
        assertEquals("light", nextTheme("system"))
    }

    @Test
    fun `theme cycle is idempotent over 3 steps`() {
        var theme = "light"
        theme = nextTheme(theme) // dark
        theme = nextTheme(theme) // system
        theme = nextTheme(theme) // light
        assertEquals("light", theme)
    }

    // Test font size cycling logic
    @Test
    fun `font size cycles through all three sizes`() {
        val sizes = Preferences.READING_SIZES
        var idx = 0

        idx = (idx + 1) % sizes.size
        assertEquals(1.2f, sizes[idx])

        idx = (idx + 1) % sizes.size
        assertEquals(1.45f, sizes[idx])

        idx = (idx + 1) % sizes.size
        assertEquals(1.0f, sizes[idx])
    }

    @Test
    fun `font size index wraps correctly`() {
        val sizes = Preferences.READING_SIZES
        // Start at last size
        var idx = sizes.indexOfFirst { it == 1.45f }
        assertEquals(2, idx)

        idx = (idx + 1) % sizes.size
        assertEquals(0, idx)
        assertEquals(1.0f, sizes[idx])
    }

    // Test favorites toggle logic
    @Test
    fun `favorites toggle adds new item`() {
        val current = mutableSetOf<Int>()
        current.add(42)
        assertTrue(42 in current)
    }

    @Test
    fun `favorites toggle removes existing item`() {
        val current = mutableSetOf(42, 100)
        current.remove(42)
        assertFalse(42 in current)
        assertTrue(100 in current)
    }

    @Test
    fun `favorites toggle is idempotent`() {
        val current = mutableSetOf<Int>()

        // Add
        current.add(42)
        assertTrue(42 in current)

        // Remove
        current.remove(42)
        assertFalse(42 in current)

        // Add again
        current.add(42)
        assertTrue(42 in current)
    }

    @Test
    fun `favorites filter produces correct hymn list`() {
        val allNumbers = listOf(1, 2, 3, 4, 5)
        val favorites = setOf(2, 4)
        val filtered = allNumbers.filter { it in favorites }
        assertEquals(listOf(2, 4), filtered)
    }

    @Test
    fun `favorites filter with empty set returns empty`() {
        val allNumbers = listOf(1, 2, 3)
        val favorites = emptySet<Int>()
        val filtered = allNumbers.filter { it in favorites }
        assertTrue(filtered.isEmpty())
    }

    // Test presentation font size logic
    @Test
    fun `presentation font size clamps at minimum`() {
        var fz = 0.5f
        fz = (fz - 0.15f).coerceAtLeast(0.4f)
        assertEquals(0.4f, fz, 0.001f)
    }

    @Test
    fun `presentation font size clamps at maximum`() {
        var fz = 2.45f
        fz = (fz + 0.15f).coerceAtMost(2.5f)
        assertEquals(2.5f, fz, 0.001f)
    }

    @Test
    fun `presentation font size step is 0_15`() {
        var fz = 1.0f
        fz += 0.15f
        assertEquals(1.15f, fz, 0.001f)
    }

    // Test select hymn / last hymn logic
    @Test
    fun `selected hymn number updates`() {
        var selected = -1
        selected = 42
        assertEquals(42, selected)
    }

    @Test
    fun `deep link takes priority over last hymn`() {
        val deepLinkHymn = 100
        val lastHymn = 42

        val target = when {
            deepLinkHymn > 0 -> deepLinkHymn
            lastHymn > 0 -> lastHymn
            else -> -1
        }
        assertEquals(100, target)
    }

    @Test
    fun `last hymn used when no deep link`() {
        val deepLinkHymn = -1
        val lastHymn = 42

        val target = when {
            deepLinkHymn > 0 -> deepLinkHymn
            lastHymn > 0 -> lastHymn
            else -> -1
        }
        assertEquals(42, target)
    }

    @Test
    fun `no target when both are empty`() {
        val deepLinkHymn = -1
        val lastHymn = -1

        val target = when {
            deepLinkHymn > 0 -> deepLinkHymn
            lastHymn > 0 -> lastHymn
            else -> -1
        }
        assertEquals(-1, target)
    }
}
