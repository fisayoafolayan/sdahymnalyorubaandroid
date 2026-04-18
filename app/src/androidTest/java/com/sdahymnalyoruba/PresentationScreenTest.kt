package com.sdahymnalyoruba

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sdahymnalyoruba.ui.screens.PresentationScreen
import com.sdahymnalyoruba.ui.theme.SDAHymnalTheme
import org.junit.Rule
import org.junit.Test

class PresentationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testHymn = makeTestHymnWithVerses(
        number = 3,
        title = "Ọkàn Mi, Yìn Ọba Ọ̀run",
        englishTitle = "Praise, My Soul, the King of Heaven",
        verses = listOf(
            listOf("First line of verse one", "Second line of verse one"),
            listOf("First line of verse two", "Second line of verse two"),
        ),
    )

    @Test
    fun shows_title_slide_initially() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                PresentationScreen(
                    hymn = testHymn,
                    onExit = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Ọkàn Mi, Yìn Ọba Ọ̀run").assertIsDisplayed()
        composeTestRule.onNodeWithText("Praise, My Soul, the King of Heaven").assertIsDisplayed()
    }

    @Test
    fun shows_hymn_number_on_title_slide() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                PresentationScreen(
                    hymn = testHymn,
                    onExit = {},
                )
            }
        }

        composeTestRule.onNodeWithText("HYMN 3").assertIsDisplayed()
    }

    @Test
    fun next_button_advances_to_verse() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                PresentationScreen(
                    hymn = testHymn,
                    onExit = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Next slide").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("VERSE 1").assertIsDisplayed()
    }

    @Test
    fun shows_end_slide_after_last_verse() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                PresentationScreen(
                    hymn = testHymn,
                    onExit = {},
                )
            }
        }

        // Advance through title + 2 verses to end
        repeat(3) {
            composeTestRule.onNodeWithContentDescription("Next slide").performClick()
            composeTestRule.waitForIdle()
        }

        composeTestRule.onNodeWithText("End of Hymn").assertIsDisplayed()
    }

    @Test
    fun exit_button_triggers_callback() {
        var exited = false

        composeTestRule.setContent {
            SDAHymnalTheme {
                PresentationScreen(
                    hymn = testHymn,
                    onExit = { exited = true },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Exit presentation").performClick()
        assert(exited)
    }

    @Test
    fun font_size_controls_visible() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                PresentationScreen(
                    hymn = testHymn,
                    onExit = {},
                )
            }
        }

        composeTestRule.onNodeWithText("A\u2212").assertIsDisplayed()
        composeTestRule.onNodeWithText("A+").assertIsDisplayed()
    }

    @Test
    fun shows_progress_label() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                PresentationScreen(
                    hymn = testHymn,
                    onExit = {},
                )
            }
        }

        // Title slide is slide 1 of 4 (title + 2 verses + end)
        composeTestRule.onNodeWithText("1 / 4").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Next slide").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("2 / 4").assertIsDisplayed()
    }
}
