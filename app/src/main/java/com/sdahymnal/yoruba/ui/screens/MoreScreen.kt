package com.sdahymnal.yoruba.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdahymnal.yoruba.R
import com.sdahymnal.yoruba.ui.components.BrandHeader
import com.sdahymnal.yoruba.ui.theme.FavoriteHeart

@Composable
fun MoreScreen(
    themeMode: String,
    hymnCount: Int = 0,
    favoritesCount: Int = 0,
    hymnCacheVersion: String? = null,
    readingFontSize: Float = 1.0f,
    onToggleTheme: () -> Unit,
    onCycleReadingFontSize: () -> Unit = {},
    onClearFavorites: () -> Unit = {},
    onTrackEvent: (String) -> Unit = {},
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }

    val themeLabel = when (themeMode) {
        "light" -> "Light"
        "dark" -> "Dark"
        else -> "System"
    }

    val fontSizeLabel = when (readingFontSize) {
        1.0f -> "Small (1.0x)"
        1.2f -> "Medium (1.2x)"
        1.45f -> "Large (1.45x)"
        else -> "${readingFontSize}x"
    }

    // Clear favorites confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    "Clear Favorites",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    "Remove all $favoritesCount hymns from your favorites?",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    onClearFavorites()
                }) {
                    Text(
                        "Clear",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FavoriteHeart,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(
                        "Cancel",
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
            SectionHeader("Appearance")
            SettingsRow(
                icon = Icons.Outlined.Palette,
                title = "Theme",
                subtitle = themeLabel,
                onClick = onToggleTheme,
            )
            SettingsRow(
                icon = Icons.Outlined.FormatSize,
                title = "Reading Font Size",
                subtitle = fontSizeLabel,
                onClick = onCycleReadingFontSize,
            )

            // HYMN DATA
            SectionHeader("Hymn Data")
            SettingsRow(
                icon = Icons.Outlined.LibraryMusic,
                title = "Hymns",
                subtitle = "$hymnCount hymns cached" + if (hymnCacheVersion != null) " \u00B7 $hymnCacheVersion" else "",
            )
            SettingsRow(
                icon = Icons.Outlined.Favorite,
                title = "Favorites",
                subtitle = if (favoritesCount == 0) "No favorites"
                    else pluralStringResource(R.plurals.hymn_count, favoritesCount, favoritesCount),
            )
            if (favoritesCount > 0) {
                SettingsRow(
                    icon = Icons.Outlined.DeleteSweep,
                    title = "Clear Favorites",
                    subtitle = "Remove all favorites",
                    titleColor = FavoriteHeart,
                    onClick = { showClearDialog = true },
                )
            }

            // SHARE & RATE
            SectionHeader("Spread the Word")
            SettingsRow(
                icon = Icons.Outlined.Share,
                title = "Share App",
                subtitle = "Tell others about SDA Hymnal Yoruba",
                onClick = {
                    onTrackEvent("share_app")
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Check out SDA Hymnal Yoruba - Browse, search, and present Seventh-day Adventist hymns in Yoruba.\n\nhttps://sdahymnalyoruba.com")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share App"))
                },
            )
            SettingsRow(
                icon = Icons.Outlined.StarRate,
                title = "Rate this App",
                subtitle = "Leave a review on the Play Store",
                onClick = {
                    onTrackEvent("rate_app")
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")))
                    } catch (_: Exception) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")))
                    }
                },
            )

            // ABOUT
            SectionHeader("About")
            SettingsRow(
                icon = Icons.Outlined.Language,
                title = "Website",
                subtitle = "sdahymnalyoruba.com",
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://sdahymnalyoruba.com")))
                },
            )
            SettingsRow(
                icon = Icons.Outlined.Email,
                title = "Contact",
                subtitle = "support@sdahymnalyoruba.com",
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@sdahymnalyoruba.com")))
                },
            )
            SettingsRow(
                icon = Icons.Outlined.Code,
                title = "GitHub",
                subtitle = "fisayoafolayan/sdahymnalyorubaandroid",
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/fisayoafolayan/sdahymnalyorubaandroid")))
                },
            )
            SettingsRow(
                icon = Icons.Outlined.Info,
                title = "Version",
                subtitle = context.packageManager
                    .getPackageInfo(context.packageName, 0).versionName ?: "1.0.0",
            )
            SettingsRow(
                icon = Icons.Outlined.Policy,
                title = "Privacy Policy",
                subtitle = "sdahymnalyoruba.com/privacy",
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://sdahymnalyoruba.com/privacy")))
                },
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "\u00A9 ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} SDA Hymnal Yoruba",
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
