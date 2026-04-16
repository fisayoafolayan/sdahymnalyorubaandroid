package com.sdahymnal.yoruba

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sdahymnal.yoruba.ui.components.NumberPadDialog
import com.sdahymnal.yoruba.ui.theme.SDAHymnalTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NumberPadDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Tap a number pad button (matches the clickable node, not the display). */
    private fun tapKey(digit: String) {
        composeTestRule.onNode(hasText(digit) and hasClickAction()).performClick()
    }

    @Test
    fun shows_dialog_title() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                NumberPadDialog(
                    onDismiss = {},
                    onGoToHymn = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Go to Hymn").assertIsDisplayed()
    }

    @Test
    fun number_buttons_update_display() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                NumberPadDialog(
                    onDismiss = {},
                    onGoToHymn = {},
                )
            }
        }

        tapKey("4")
        tapKey("2")
        composeTestRule.onNodeWithText("42").assertIsDisplayed()
    }

    @Test
    fun go_button_triggers_callback_for_valid_hymn() {
        var navigatedTo: Int? = null

        composeTestRule.setContent {
            SDAHymnalTheme {
                NumberPadDialog(
                    onDismiss = {},
                    onGoToHymn = { navigatedTo = it },
                    getHymnTitle = { if (it == 42) "Test Hymn" else null },
                )
            }
        }

        tapKey("4")
        tapKey("2")
        tapKey("Go")

        assertEquals(42, navigatedTo)
    }

    @Test
    fun shows_error_for_invalid_hymn() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                NumberPadDialog(
                    onDismiss = {},
                    onGoToHymn = {},
                    getHymnTitle = { null },
                )
            }
        }

        tapKey("9")
        tapKey("9")
        tapKey("9")
        tapKey("Go")

        composeTestRule.onNodeWithText("Hymn not found").assertIsDisplayed()
    }

    @Test
    fun shows_hymn_title_preview() {
        composeTestRule.setContent {
            SDAHymnalTheme {
                NumberPadDialog(
                    onDismiss = {},
                    onGoToHymn = {},
                    getHymnTitle = { if (it == 3) "Ọkàn Mi, Yìn Ọba Ọ̀run" else null },
                )
            }
        }

        tapKey("3")
        composeTestRule.onNodeWithText("Ọkàn Mi, Yìn Ọba Ọ̀run").assertIsDisplayed()
    }

    @Test
    fun cancel_triggers_dismiss() {
        var dismissed = false

        composeTestRule.setContent {
            SDAHymnalTheme {
                NumberPadDialog(
                    onDismiss = { dismissed = true },
                    onGoToHymn = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Cancel").performClick()
        assert(dismissed)
    }
}
