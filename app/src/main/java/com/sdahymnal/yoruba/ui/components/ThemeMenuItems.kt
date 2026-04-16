package com.sdahymnal.yoruba.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sdahymnal.yoruba.R

@Composable
fun ThemeMenuItems(
    currentMode: String,
    onSelect: (String) -> Unit,
) {
    listOf(
        Triple("light", stringResource(R.string.theme_light), Icons.Default.LightMode),
        Triple("dark", stringResource(R.string.theme_dark), Icons.Default.DarkMode),
        Triple("system", stringResource(R.string.theme_system), Icons.Default.BrightnessAuto),
    ).forEach { (mode, label, icon) ->
        DropdownMenuItem(
            text = { Text(label) },
            onClick = { onSelect(mode) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = {
                if (currentMode == mode) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            },
        )
    }
}
