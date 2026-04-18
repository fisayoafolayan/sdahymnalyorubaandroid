package com.sdahymnalyoruba.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdahymnalyoruba.R
import com.sdahymnalyoruba.data.Hymn
import com.sdahymnalyoruba.data.LyricBlock
import com.sdahymnalyoruba.ui.theme.ChorusBgDark
import com.sdahymnalyoruba.ui.theme.ChorusBgLight
import com.sdahymnalyoruba.ui.theme.SWIPE_THRESHOLD

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HymnDetailScreen(
    hymn: Hymn,
    hasPrevious: Boolean,
    hasNext: Boolean,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onBack: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPresent: () -> Unit,
    onShare: () -> Unit = {},
    readingFontSize: Float = 1.0f,
    onCycleReadingFontSize: () -> Unit = {},
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val baseFontSize = 17.sp
    val scaledFontSize = baseFontSize * readingFontSize

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.hymn_title_format, hymn.number),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp),
                    ) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (isFavorite) {
                                    com.sdahymnalyoruba.ui.theme.FavoriteHeart
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                },
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(onClick = onCycleReadingFontSize),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = "A",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = "A",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(onClick = onPresent),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DesktopWindows,
                                    contentDescription = "Present",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                },
                windowInsets = WindowInsets.statusBars,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { padding ->
        var dragOffset by remember { mutableFloatStateOf(0f) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(hasPrevious, hasNext) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { dragOffset = 0f; totalDrag = 0f },
                        onDragEnd = {
                            when {
                                totalDrag < -SWIPE_THRESHOLD && hasNext -> onNext()
                                totalDrag > SWIPE_THRESHOLD && hasPrevious -> onPrevious()
                            }
                            dragOffset = 0f
                            totalDrag = 0f
                        },
                        onDragCancel = { dragOffset = 0f; totalDrag = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            totalDrag += dragAmount
                            // Rubber-band at boundaries, full follow otherwise
                            dragOffset += when {
                                dragAmount < 0 && !hasNext -> dragAmount * 0.3f
                                dragAmount > 0 && !hasPrevious -> dragAmount * 0.3f
                                else -> dragAmount
                            }
                        },
                    )
                }
                .graphicsLayer { translationX = dragOffset },
        ) {
            HymnContent(
                hymn = hymn,
                isDark = isDark,
                scaledFontSize = scaledFontSize,
                onShare = onShare,
                context = context,
            )

            // Swipe affordance chevrons
            if (hasPrevious) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(28.dp),
                )
            }
            if (hasNext) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(28.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HymnContent(
    hymn: Hymn,
    isDark: Boolean,
    scaledFontSize: androidx.compose.ui.unit.TextUnit,
    onShare: () -> Unit,
    context: android.content.Context,
) {
    val scrollState = rememberScrollState()
    val containerSize = LocalWindowInfo.current.containerSize
    val isLandscape = containerSize.width > containerSize.height

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .then(if (isLandscape) Modifier.widthIn(max = 600.dp) else Modifier.fillMaxWidth())
                .verticalScroll(scrollState)
                .padding(horizontal = 32.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = hymn.title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = hymn.englishTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        if (hymn.references.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                hymn.references.forEach { (key, value) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(4.dp),
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "$key $value",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        hymn.lyrics.forEach { block ->
            LyricSection(block = block, isDark = isDark, fontSize = scaledFontSize)
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Share at the end of the hymn
        val shareText = stringResource(R.string.share_hymn_format, hymn.number, hymn.title)
        val shareChooser = stringResource(R.string.share_hymn_chooser, hymn.number)
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .size(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable {
                    onShare()
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, shareChooser))
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LyricSection(
    block: LyricBlock,
    isDark: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit,
) {
    val isChorus = block.type == "chorus"
    val isCallResponse = block.type == "call_response"

    val containerModifier = if (isChorus) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDark) ChorusBgDark else ChorusBgLight)
            .padding(16.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    }

    Column(modifier = containerModifier) {
        val label = when (block.type) {
            "verse" -> "VERSE ${block.index}"
            "chorus" -> "CHORUS"
            "call_response" -> "CALL & RESPONSE ${block.index}"
            else -> ""
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isChorus) FontWeight.SemiBold else FontWeight.Normal,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primary.copy(alpha = if (isChorus) 0.15f else 0.08f),
                thickness = 1.dp,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isCallResponse) {
            block.callResponseLines.forEach { line ->
                val partLabel = when (line.part) {
                    "leader" -> "Leader / L\u00edl\u00e9"
                    "congregation" -> "All / \u1eb8gb\u1eb9\u0301"
                    else -> line.part
                }
                Text(
                    text = partLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                )
                Text(
                    text = line.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = fontSize,
                        lineHeight = fontSize * 1.65f,
                        fontStyle = if (line.part == "congregation") FontStyle.Italic else FontStyle.Normal,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        } else {
            block.textLines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = fontSize,
                        lineHeight = fontSize * 1.65f,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}
