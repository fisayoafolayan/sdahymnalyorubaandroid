package com.sdahymnal.yoruba

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.sdahymnal.yoruba.ui.screens.HymnDetailScreen
import com.sdahymnal.yoruba.ui.theme.SDAHymnalTheme
import org.junit.Rule
import org.junit.Test

class HymnDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displays_hymn_title() {
        val hymn = makeTestHymn(42, "Oluwa Oba Nla", "Great King God")

        composeTestRule.setContent {
            SDAHymnalTheme {
                HymnDetailScreen(
                    hymn = hymn,
                    hasPrevious = false,
                    hasNext = false,
                    onBack = {},
                    onPrevious = {},
                    onNext = {},
                    onPresent = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Oluwa Oba Nla").assertIsDisplayed()
    }

    @Test
    fun displays_english_title() {
        val hymn = makeTestHymn(42, "Oluwa Oba Nla", "Great King God")

        composeTestRule.setContent {
            SDAHymnalTheme {
                HymnDetailScreen(
                    hymn = hymn,
                    hasPrevious = false,
                    hasNext = false,
                    onBack = {},
                    onPrevious = {},
                    onNext = {},
                    onPresent = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Great King God").assertIsDisplayed()
    }

    @Test
    fun displays_hymn_number_in_header() {
        val hymn = makeTestHymn(42, "Test", "Test English")

        composeTestRule.setContent {
            SDAHymnalTheme {
                HymnDetailScreen(
                    hymn = hymn,
                    hasPrevious = false,
                    hasNext = false,
                    onBack = {},
                    onPrevious = {},
                    onNext = {},
                    onPresent = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Hymn 42").assertIsDisplayed()
    }

    @Test
    fun displays_verse_lyrics() {
        val hymn = makeTestHymn(
            1, "Test", "Test",
            lyrics = listOf("First verse line one" to "verse", "First verse line two" to "verse"),
        )

        composeTestRule.setContent {
            SDAHymnalTheme {
                HymnDetailScreen(
                    hymn = hymn,
                    hasPrevious = false,
                    hasNext = false,
                    onBack = {},
                    onPrevious = {},
                    onNext = {},
                    onPresent = {},
                )
            }
        }

        composeTestRule.onNodeWithText("First verse line one").assertIsDisplayed()
        composeTestRule.onNodeWithText("First verse line two").assertIsDisplayed()
    }

    @Test
    fun displays_reference_tags() {
        val hymn = makeTestHymn(1, "Test", "Test", refs = mapOf("SDAH" to 16))

        composeTestRule.setContent {
            SDAHymnalTheme {
                HymnDetailScreen(
                    hymn = hymn,
                    hasPrevious = false,
                    hasNext = false,
                    onBack = {},
                    onPrevious = {},
                    onNext = {},
                    onPresent = {},
                )
            }
        }

        composeTestRule.onNodeWithText("SDAH 16").assertIsDisplayed()
    }
}
