package com.sdahymnal.yoruba

import com.sdahymnal.yoruba.data.Hymn
import com.sdahymnal.yoruba.data.HymnLoadState
import com.sdahymnal.yoruba.data.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelLogicTest {

    // --- Theme cycling ---

    private fun nextTheme(current: String): String = when (current) {
        "light" -> "dark"
        "dark" -> "system"
        else -> "light"
    }

    @Test
    fun `theme cycles light to dark to system to light`() {
        var theme = "light"
        theme = nextTheme(theme)
        assertEquals("dark", theme)
        theme = nextTheme(theme)
        assertEquals("system", theme)
        theme = nextTheme(theme)
        assertEquals("light", theme)
    }

    @Test
    fun `theme unknown value defaults to light`() {
        assertEquals("light", nextTheme("invalid"))
    }

    // --- Font size cycling ---

    @Test
    fun `reading font size cycles through all sizes and wraps`() {
        val sizes = Preferences.READING_SIZES
        var idx = 0
        val visited = mutableListOf(sizes[idx])

        repeat(sizes.size) {
            idx = (idx + 1) % sizes.size
            visited.add(sizes[idx])
        }

        assertEquals(listOf(1.0f, 1.2f, 1.45f, 1.0f), visited)
    }

    @Test
    fun `presentation font size clamps to range`() {
        assertEquals(0.4f, (0.3f).coerceIn(0.4f, 2.5f), 0.001f)
        assertEquals(2.5f, (2.7f).coerceIn(0.4f, 2.5f), 0.001f)
        assertEquals(1.0f, (1.0f).coerceIn(0.4f, 2.5f), 0.001f)
    }

    // --- Favorites toggle logic ---

    @Test
    fun `favorites toggle adds and removes`() {
        val favorites = mutableSetOf<Int>()

        favorites.add(42)
        assertTrue(42 in favorites)

        favorites.remove(42)
        assertFalse(42 in favorites)
    }

    @Test
    fun `favorites filter returns only matching hymns`() {
        val allNumbers = listOf(1, 2, 3, 4, 5)
        val favorites = setOf(2, 4)
        assertEquals(listOf(2, 4), allNumbers.filter { it in favorites })
    }

    @Test
    fun `favorites filter with empty set returns empty`() {
        val allNumbers = listOf(1, 2, 3)
        assertTrue(allNumbers.filter { it in emptySet<Int>() }.isEmpty())
    }

    // --- Deep link flow ---

    @Test
    fun `deep link set updates flow value`() = runTest {
        val pendingDeepLink = MutableStateFlow(-1)

        assertEquals(-1, pendingDeepLink.value)

        // Simulate setDeepLink
        val hymnNumber = 42
        if (hymnNumber > 0) pendingDeepLink.value = hymnNumber
        assertEquals(42, pendingDeepLink.value)
    }

    @Test
    fun `deep link consume resets to negative one`() = runTest {
        val pendingDeepLink = MutableStateFlow(42)

        // Simulate consumeDeepLink
        pendingDeepLink.value = -1
        assertEquals(-1, pendingDeepLink.value)
    }

    @Test
    fun `deep link ignores invalid numbers`() = runTest {
        val pendingDeepLink = MutableStateFlow(-1)

        // Simulate setDeepLink with invalid values
        val invalid = -1
        if (invalid > 0) pendingDeepLink.value = invalid
        assertEquals(-1, pendingDeepLink.value)

        val zero = 0
        if (zero > 0) pendingDeepLink.value = zero
        assertEquals(-1, pendingDeepLink.value)
    }

    // --- Search + load state combine ---

    private val testHymns = listOf(
        Hymn(
            index = "0",
            number = 1,
            title = "Jẹ́ Ká Jọ Yìn Ọlọ́run",
            englishTitle = "Praise God Together",
            lyrics = emptyList(),
        ),
        Hymn(
            index = "1",
            number = 2,
            title = "Ẹ Wá Bá Mi Yọ̀",
            englishTitle = "Come Rejoice With Me",
            lyrics = emptyList(),
        ),
    )

    @Test
    fun `search results empty when state is Loading`() = runTest {
        val query = MutableStateFlow("")
        val state = MutableStateFlow<HymnLoadState>(HymnLoadState.Loading)

        val result = combine(query, state) { q, s ->
            val allHymns = (s as? HymnLoadState.Ready)?.hymns.orEmpty()
            if (q.isBlank()) allHymns else allHymns.filter { it.title.contains(q) }
        }.first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `search results returns all hymns when query is blank and state is Ready`() = runTest {
        val query = MutableStateFlow("")
        val state = MutableStateFlow<HymnLoadState>(HymnLoadState.Ready(testHymns))

        val result = combine(query, state) { q, s ->
            val allHymns = (s as? HymnLoadState.Ready)?.hymns.orEmpty()
            if (q.isBlank()) allHymns else allHymns.filter { it.title.contains(q) }
        }.first()

        assertEquals(2, result.size)
    }

    @Test
    fun `search results update when state transitions from Loading to Ready`() = runTest {
        val query = MutableStateFlow("")
        val state = MutableStateFlow<HymnLoadState>(HymnLoadState.Loading)

        val results = mutableListOf<List<Hymn>>()
        val job = launch {
            combine(query, state) { q, s ->
                val allHymns = (s as? HymnLoadState.Ready)?.hymns.orEmpty()
                if (q.isBlank()) allHymns else allHymns.filter { it.title.contains(q) }
            }.toList(results)
        }

        advanceUntilIdle()

        // Transition to Ready
        state.value = HymnLoadState.Ready(testHymns)
        advanceUntilIdle()

        job.cancel()

        assertEquals(2, results.size)
        assertTrue(results[0].isEmpty())       // Loading -> empty
        assertEquals(2, results[1].size)        // Ready -> all hymns
    }

    @Test
    fun `search results update when query changes`() = runTest {
        val query = MutableStateFlow("")
        val state = MutableStateFlow<HymnLoadState>(HymnLoadState.Ready(testHymns))

        val results = mutableListOf<List<Hymn>>()
        val job = launch {
            combine(query, state) { q, s ->
                val allHymns = (s as? HymnLoadState.Ready)?.hymns.orEmpty()
                if (q.isBlank()) allHymns else allHymns.filter {
                    it.englishTitle.contains(q, ignoreCase = true)
                }
            }.toList(results)
        }

        advanceUntilIdle()

        query.value = "Praise"
        advanceUntilIdle()

        query.value = ""
        advanceUntilIdle()

        job.cancel()

        assertEquals(3, results.size)
        assertEquals(2, results[0].size)    // blank -> all
        assertEquals(1, results[1].size)    // "Praise" -> 1 match
        assertEquals(2, results[2].size)    // blank again -> all
    }

    @Test
    fun `search results returns empty for no matches`() = runTest {
        val query = MutableStateFlow("nonexistent")
        val state = MutableStateFlow<HymnLoadState>(HymnLoadState.Ready(testHymns))

        val result = combine(query, state) { q, s ->
            val allHymns = (s as? HymnLoadState.Ready)?.hymns.orEmpty()
            if (q.isBlank()) allHymns else allHymns.filter {
                it.title.contains(q, ignoreCase = true)
            }
        }.first()

        assertTrue(result.isEmpty())
    }

    // --- Hymn lookup by number ---

    @Test
    fun `hymn lookup by number returns correct hymn`() {
        val map = testHymns.associateBy { it.number }
        assertEquals("Jẹ́ Ká Jọ Yìn Ọlọ́run", map[1]?.title)
        assertEquals("Ẹ Wá Bá Mi Yọ̀", map[2]?.title)
    }

    @Test
    fun `hymn lookup by number returns null for missing`() {
        val map = testHymns.associateBy { it.number }
        assertEquals(null, map[999])
    }

    // --- Deep link vs restore priority ---

    @Test
    fun `restore skipped when deep link is pending`() {
        val pendingDeepLink = 100
        val lastHymn = 42

        val shouldRestore = pendingDeepLink <= 0 && lastHymn > 0
        assertFalse(shouldRestore)
    }

    @Test
    fun `restore proceeds when no deep link`() {
        val pendingDeepLink = -1
        val lastHymn = 42

        val shouldRestore = pendingDeepLink <= 0 && lastHymn > 0
        assertTrue(shouldRestore)
    }

    @Test
    fun `neither restore nor deep link when both empty`() {
        val pendingDeepLink = -1
        val lastHymn = -1

        val shouldRestore = pendingDeepLink <= 0 && lastHymn > 0
        assertFalse(shouldRestore)
    }
}
