package com.sdahymnal.yoruba.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PurpleHeader,
    onPrimary = LightSurface,
    primaryContainer = PurpleLight,
    onPrimaryContainer = PurpleDark,
    secondary = PurplePrimary,
    onSecondary = LightBackground,
    secondaryContainer = LightBadgeBg,
    onSecondaryContainer = LightBadgeText,
    tertiary = PurpleDark,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorder,
    inverseSurface = LightBackground,      // header matches background
    inverseOnSurface = LightTextPrimary,  // header text dark on white
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPurplePrimary,
    onPrimary = DarkBackground,
    primaryContainer = PurpleDark,
    onPrimaryContainer = PurpleLight,
    secondary = DarkPurplePrimary,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = DarkTextSecondary,
    tertiary = PurpleLight,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    inverseSurface = DarkBackground,       // header matches background
    inverseOnSurface = DarkTextPrimary,   // header text
)

/**
 * @param themeMode "light", "dark", or "system"
 */
@Composable
fun SDAHymnalTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
