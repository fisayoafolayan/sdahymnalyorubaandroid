package com.sdahymnalyoruba

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.sdahymnalyoruba.ui.screens.MoreScreen
import com.sdahymnalyoruba.ui.theme.SDAHymnalTheme
import org.junit.Rule
import org.junit.Test

class MoreScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shows_all_sections() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                MoreScreen(
                    themeMode = "system",
                    hymnCount = 621,
                    favoritesCount = 5,
                    onSetTheme = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hymn Data").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Spread the Word").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("About").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun shows_theme_label() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                MoreScreen(
                    themeMode = "dark",
                    onSetTheme = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
    }

    @Test
    fun shows_hymn_count() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                MoreScreen(
                    themeMode = "system",
                    hymnCount = 621,
                    onSetTheme = {},
                )
            }
        }

        composeTestRule.onNodeWithText("621 hymns cached").assertExists()
    }

    @Test
    fun shows_favorites_count() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                MoreScreen(
                    themeMode = "system",
                    favoritesCount = 12,
                    onSetTheme = {},
                )
            }
        }

        composeTestRule.onNodeWithText("12 hymns").assertIsDisplayed()
    }

    @Test
    fun shows_clear_favorites_when_has_favorites() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                MoreScreen(
                    themeMode = "system",
                    favoritesCount = 5,
                    onSetTheme = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Clear Favorites").assertIsDisplayed()
    }

    @Test
    fun hides_clear_favorites_when_empty() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                MoreScreen(
                    themeMode = "system",
                    favoritesCount = 0,
                    onSetTheme = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Clear Favorites").assertDoesNotExist()
    }

    @Test
    fun shows_font_size_label() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                MoreScreen(
                    themeMode = "system",
                    readingFontSize = 1.2f,
                    onSetTheme = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Medium (1.2x)").assertIsDisplayed()
    }

    @Test
    fun clear_favorites_shows_confirmation_dialog() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                MoreScreen(
                    themeMode = "system",
                    favoritesCount = 3,
                    onSetTheme = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Clear Favorites").performClick()
        composeTestRule.onNodeWithText("This will remove all 3 hymns from your favorites.").assertIsDisplayed()
    }

    @Test
    fun shows_brand_header() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                MoreScreen(
                    themeMode = "system",
                    onSetTheme = {},
                )
            }
        }

        composeTestRule.onNodeWithText("SDA Hymnal Yorùbá").assertIsDisplayed()
    }
}
