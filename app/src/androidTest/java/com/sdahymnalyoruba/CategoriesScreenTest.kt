package com.sdahymnalyoruba

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.sdahymnalyoruba.ui.screens.CategoriesScreen
import com.sdahymnalyoruba.ui.theme.SDAHymnalTheme
import org.junit.Rule
import org.junit.Test

class CategoriesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testHymns = (1..40).map { makeTestHymn(it, "Hymn $it", "English $it") }

    @Test
    fun shows_categories_with_hymns() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                CategoriesScreen(
                    hymns = testHymns,
                    favorites = emptySet(),
                    onCategoryClick = {},
                )
            }
        }

        // First category "Adoration And Praise" has range 1..22, all covered by test data
        composeTestRule.onNodeWithText("Ìbà Àti Ìyìn").assertIsDisplayed()
    }

    @Test
    fun shows_hymn_count_on_card() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                CategoriesScreen(
                    hymns = testHymns,
                    favorites = emptySet(),
                    onCategoryClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("22 hymns").assertIsDisplayed()
    }

    @Test
    fun shows_brand_header() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                CategoriesScreen(
                    hymns = testHymns,
                    favorites = emptySet(),
                    onCategoryClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("SDA Hymnal Yorùbá").assertIsDisplayed()
    }

    @Test
    fun hides_empty_categories() {
        // Only hymns 1-5, so most categories should be hidden
        val fewHymns = (1..5).map { makeTestHymn(it, "Hymn $it", "English $it") }

        composeTestRule.setContent {
            SDAHymnalTheme {
                CategoriesScreen(
                    hymns = fewHymns,
                    favorites = emptySet(),
                    onCategoryClick = {},
                )
            }
        }

        // "Morning Worship" needs hymns 23-33 - none in test data, should not be shown
        composeTestRule.onNodeWithText("Ìsìn Òwúrọ̀").assertDoesNotExist()
    }
}
