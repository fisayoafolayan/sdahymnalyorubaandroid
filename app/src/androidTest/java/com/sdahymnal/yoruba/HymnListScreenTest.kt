package com.sdahymnal.yoruba

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.sdahymnal.yoruba.data.Hymn
import com.sdahymnal.yoruba.ui.screens.HymnListScreen
import com.sdahymnal.yoruba.ui.theme.SDAHymnalTheme
import org.junit.Rule
import org.junit.Test

class HymnListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testHymns = listOf(
        makeTestHymn(1, "Gbogbo Eyin", "All People On Earth"),
        makeTestHymn(2, "Oluwa Oba", "God Is King"),
        makeTestHymn(3, "Jesu Kristi", "Jesus Christ"),
    )

    @Test
    fun hymn_list_displays_hymns() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                HymnListScreen(
                    displayedHymns = testHymns,
                    selectedHymnNumber = null,
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onHymnClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Gbogbo Eyin").assertIsDisplayed()
        composeTestRule.onNodeWithText("Oluwa Oba").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jesu Kristi").assertIsDisplayed()
    }

    @Test
    fun hymn_list_shows_count() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                HymnListScreen(
                    displayedHymns = testHymns,
                    selectedHymnNumber = null,
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onHymnClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("3 hymns").assertIsDisplayed()
    }

    @Test
    fun hymn_list_shows_brand_header() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                HymnListScreen(
                    displayedHymns = testHymns,
                    selectedHymnNumber = null,
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onHymnClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("SDA Hymnal Yorùbá").assertIsDisplayed()
    }

    @Test
    fun hymn_click_triggers_callback() {
        var clickedHymn: Hymn? = null

        composeTestRule.setContent {
            SDAHymnalTheme {
                HymnListScreen(
                    displayedHymns = testHymns,
                    selectedHymnNumber = null,
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onHymnClick = { clickedHymn = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Oluwa Oba").performClick()
        assert(clickedHymn?.number == 2)
    }

    @Test
    fun search_results_displayed() {
        val filtered = listOf(testHymns[1]) // Only "Oluwa Oba"

        composeTestRule.setContent {
            SDAHymnalTheme {
                HymnListScreen(
                    displayedHymns = filtered,
                    selectedHymnNumber = null,
                    searchQuery = "Oluwa",
                    onSearchQueryChange = {},
                    onHymnClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Oluwa Oba").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 hymn").assertIsDisplayed()
    }
}
