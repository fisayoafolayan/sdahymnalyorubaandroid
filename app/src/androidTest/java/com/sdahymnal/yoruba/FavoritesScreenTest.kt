package com.sdahymnal.yoruba

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.sdahymnal.yoruba.ui.screens.FavoritesScreen
import com.sdahymnal.yoruba.ui.theme.SDAHymnalTheme
import org.junit.Rule
import org.junit.Test

class FavoritesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shows_empty_state_when_no_favorites() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                FavoritesScreen(
                    favoriteHymns = emptyList(),
                    selectedHymnNumber = null,
                    onHymnClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("No favorites yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Browse Hymns").assertIsDisplayed()
    }

    @Test
    fun shows_favorite_hymns() {
        val favorites = listOf(
            makeTestHymn(1, "Gbogbo Eyin", "All People"),
            makeTestHymn(42, "Oluwa Oba", "God Is King"),
        )

        composeTestRule.setContent {
            SDAHymnalTheme {
                FavoritesScreen(
                    favoriteHymns = favorites,
                    selectedHymnNumber = null,
                    onHymnClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Gbogbo Eyin").assertIsDisplayed()
        composeTestRule.onNodeWithText("Oluwa Oba").assertIsDisplayed()
    }

    @Test
    fun shows_count_when_more_than_seven() {
        val favorites = (1..10).map { makeTestHymn(it, "Hymn $it", "English $it") }

        composeTestRule.setContent {
            SDAHymnalTheme {
                FavoritesScreen(
                    favoriteHymns = favorites,
                    selectedHymnNumber = null,
                    onHymnClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("10 hymns").assertIsDisplayed()
    }

    @Test
    fun shows_count_for_any_number_of_favorites() {
        val favorites = (1..5).map { makeTestHymn(it, "Hymn $it", "English $it") }

        composeTestRule.setContent {
            SDAHymnalTheme {
                FavoritesScreen(
                    favoriteHymns = favorites,
                    selectedHymnNumber = null,
                    onHymnClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("5 hymns").assertIsDisplayed()
    }

    @Test
    fun shows_brand_header() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                FavoritesScreen(
                    favoriteHymns = emptyList(),
                    selectedHymnNumber = null,
                    onHymnClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("SDA Hymnal Yorùbá").assertIsDisplayed()
    }
}
