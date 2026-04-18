package com.sdahymnalyoruba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.sdahymnalyoruba.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sdahymnalyoruba.ui.theme.NotoSerif
import com.sdahymnalyoruba.ui.theme.PlayfairDisplay

@Composable
fun NumberPadDialog(
    onDismiss: () -> Unit,
    onGoToHymn: (Int) -> Unit,
    getHymnTitle: (Int) -> String? = { null },
) {
    var input by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val previewTitle = input.toIntOrNull()?.let { getHymnTitle(it) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.go_to_hymn),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PlayfairDisplay,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Display with delete button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Balance spacer
                    Spacer(modifier = Modifier.size(48.dp))
                    Text(
                        text = input.ifEmpty { "\u2014" },
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = PlayfairDisplay,
                            fontSize = 36.sp,
                            letterSpacing = 4.sp,
                        ),
                        color = if (input.isEmpty()) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .then(
                                if (input.isNotEmpty()) Modifier.clickable {
                                    input = input.dropLast(1)
                                    showError = false
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (input.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                if (previewTitle != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = previewTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else if (showError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.hymn_not_found),
                        style = MaterialTheme.typography.bodySmall,
                        color = com.sdahymnalyoruba.ui.theme.FavoriteHeart,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                val tryGo = {
                    val number = input.toIntOrNull()
                    if (number != null && number > 0) {
                        if (getHymnTitle(number) != null) {
                            onGoToHymn(number)
                        } else {
                            showError = true
                        }
                    }
                }

                // Number pad grid
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "go"),
                )

                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        row.forEach { key ->
                            when (key) {
                                "go" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .then(
                                                if (input.isNotEmpty()) Modifier.clickable { tryGo() }
                                                else Modifier
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = stringResource(R.string.action_go),
                                            fontFamily = NotoSerif,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (input.isNotEmpty()) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                            },
                                        )
                                    }
                                }
                                "" -> Spacer(modifier = Modifier.size(64.dp))
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                if (input.length < 4) {
                                                    input += key
                                                    showError = false
                                                }
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = key,
                                            fontFamily = NotoSerif,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        fontFamily = NotoSerif,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
