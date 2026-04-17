package com.sdahymnalyoruba.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.ui.unit.DpOffset
import androidx.core.net.toUri
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdahymnalyoruba.R
import com.sdahymnalyoruba.ui.components.BrandHeader
import com.sdahymnalyoruba.ui.components.ThemeMenuItems
import com.sdahymnalyoruba.ui.theme.FavoriteHeart

@Composable
fun MoreScreen(
    themeMode: String,
    hymnCount: Int = 0,
    favoritesCount: Int = 0,
    hymnCacheVersion: String? = null,
    readingFontSize: Float = 1.0f,
    onSetTheme: (String) -> Unit,
    onCycleReadingFontSize: () -> Unit = {},
    onClearFavorites: () -> Unit = {},
    onTrackEvent: (String) -> Unit = {},
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }

    val themeLabel = when (themeMode) {
        "light" -> stringResource(R.string.theme_light)
        "dark" -> stringResource(R.string.theme_dark)
        else -> stringResource(R.string.theme_system)
    }

    val fontSizeLabel = when (readingFontSize) {
        1.0f -> stringResource(R.string.font_size_small)
        1.2f -> stringResource(R.string.font_size_medium)
        1.45f -> stringResource(R.string.font_size_large)
        else -> "${readingFontSize}x"
    }

    // Clear favorites confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    stringResource(R.string.setting_clear_favorites),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    stringResource(R.string.clear_favorites_confirm, favoritesCount),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    onClearFavorites()
                }) {
                    Text(
                        stringResource(R.string.action_clear),
                        style = MaterialTheme.typography.bodyMedium,
                        color = FavoriteHeart,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(
                        stringResource(R.string.action_cancel),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { BrandHeader() },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // APPEARANCE
            SectionHeader(stringResource(R.string.section_appearance))
            Box {
                SettingsRow(
                    icon = Icons.Outlined.Palette,
                    title = stringResource(R.string.setting_theme),
                    subtitle = themeLabel,
                    onClick = { showThemeMenu = true },
                )
                DropdownMenu(
                    expanded = showThemeMenu,
                    onDismissRequest = { showThemeMenu = false },
                    offset = DpOffset(x = 56.dp, y = (-48).dp),
                ) {
                    ThemeMenuItems(
                        currentMode = themeMode,
                        onSelect = {
                            showThemeMenu = false
                            onSetTheme(it)
                        },
                    )
                }
            }
            SettingsRow(
                icon = Icons.Outlined.FormatSize,
                title = stringResource(R.string.setting_font_size),
                subtitle = fontSizeLabel,
                onClick = onCycleReadingFontSize,
            )
            Text(
                text = stringResource(R.string.font_size_preview),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp * readingFontSize,
                    lineHeight = 17.sp * readingFontSize * 1.65f,
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 12.dp),
            )

            // HYMN DATA
            SectionHeader(stringResource(R.string.section_hymn_data))
            SettingsRow(
                icon = Icons.Outlined.LibraryMusic,
                title = stringResource(R.string.setting_hymns),
                subtitle = if (hymnCacheVersion != null) stringResource(R.string.hymns_cached_version, hymnCount, hymnCacheVersion)
                    else stringResource(R.string.hymns_cached, hymnCount),
            )
            SettingsRow(
                icon = Icons.Outlined.Favorite,
                title = stringResource(R.string.setting_favorites),
                subtitle = if (favoritesCount == 0) stringResource(R.string.no_favorites)
                    else pluralStringResource(R.plurals.hymn_count, favoritesCount, favoritesCount),
            )
            if (favoritesCount > 0) {
                SettingsRow(
                    icon = Icons.Outlined.DeleteSweep,
                    title = stringResource(R.string.setting_clear_favorites),
                    subtitle = stringResource(R.string.clear_favorites_subtitle),
                    titleColor = FavoriteHeart,
                    onClick = { showClearDialog = true },
                )
            }

            // SHARE & RATE
            SectionHeader(stringResource(R.string.section_spread))
            val shareAppText = stringResource(R.string.share_app_text)
            val shareAppChooser = stringResource(R.string.share_app_chooser)
            SettingsRow(
                icon = Icons.Outlined.Share,
                title = stringResource(R.string.setting_share_app),
                subtitle = stringResource(R.string.share_app_subtitle),
                onClick = {
                    onTrackEvent("share_app")
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareAppText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, shareAppChooser))
                },
            )
            SettingsRow(
                icon = Icons.Outlined.StarRate,
                title = stringResource(R.string.setting_rate_app),
                subtitle = stringResource(R.string.rate_app_subtitle),
                onClick = {
                    onTrackEvent("rate_app")
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=${context.packageName}".toUri()))
                    } catch (_: Exception) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()))
                    }
                },
            )

            // ABOUT
            SectionHeader(stringResource(R.string.section_about))
            SettingsRow(
                icon = Icons.Outlined.Language,
                title = stringResource(R.string.setting_website),
                subtitle = "sdahymnalyoruba.com",
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, "https://sdahymnalyoruba.com".toUri()))
                },
            )
            SettingsRow(
                icon = Icons.Outlined.Email,
                title = stringResource(R.string.setting_contact),
                subtitle = "support@sdahymnalyoruba.com",
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_SENDTO, "mailto:support@sdahymnalyoruba.com".toUri()))
                },
            )
            SettingsRow(
                icon = Icons.Outlined.Code,
                title = stringResource(R.string.setting_github),
                subtitle = "fisayoafolayan/sdahymnalyorubaandroid",
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, "https://github.com/fisayoafolayan/sdahymnalyorubaandroid".toUri()))
                },
            )
            SettingsRow(
                icon = Icons.Outlined.Info,
                title = stringResource(R.string.setting_version),
                subtitle = context.packageManager
                    .getPackageInfo(context.packageName, 0).versionName ?: "1.0.0",
            )
            SettingsRow(
                icon = Icons.Outlined.Policy,
                title = stringResource(R.string.setting_privacy),
                subtitle = "sdahymnalyoruba.com/privacy",
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, "https://sdahymnalyoruba.com/privacy".toUri()))
                },
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.copyright_format, java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color? = null,
    onClick: (() -> Unit)? = null,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = titleColor?.copy(alpha = 0.7f)
                    ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = titleColor ?: MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.06f),
            thickness = 0.5.dp,
            modifier = Modifier.padding(start = 56.dp),
        )
    }
}
