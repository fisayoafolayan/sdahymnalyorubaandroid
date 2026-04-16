package com.sdahymnal.yoruba.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sdahymnal.yoruba.data.Hymn
import com.sdahymnal.yoruba.ui.theme.findActivity
import com.sdahymnal.yoruba.ui.theme.NotoSerif
import com.sdahymnal.yoruba.ui.theme.PlayfairDisplay
import com.sdahymnal.yoruba.ui.theme.PurpleLight
import com.sdahymnal.yoruba.ui.theme.StageBgChorus
import com.sdahymnal.yoruba.ui.theme.StageBgVerse
import com.sdahymnal.yoruba.ui.theme.StageChorusText
import com.sdahymnal.yoruba.ui.theme.StageFooterBg
import com.sdahymnal.yoruba.ui.theme.StageLabelBg
import com.sdahymnal.yoruba.ui.theme.StageLabelBorder
import com.sdahymnal.yoruba.ui.theme.StageText

private sealed class Slide {
    data class Title(val hymn: Hymn) : Slide()
    data class Lyrics(val block: com.sdahymnal.yoruba.data.LyricBlock) : Slide()
    data object End : Slide()
}

@Composable
fun PresentationScreen(
    hymn: Hymn,
    onExit: () -> Unit,
    fontSizeMultiplier: Float = 1.0f,
    onFontSizeChange: (Float) -> Unit = {},
) {
    val view = LocalView.current
    val screenWidth = LocalConfiguration.current.screenWidthDp

    val activity = view.context.findActivity()
    DisposableEffect(Unit) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        view.keepScreenOn = true
        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
            view.keepScreenOn = false
        }
    }

    BackHandler(onBack = onExit)

    val slides = remember(hymn) {
        buildList {
            add(Slide.Title(hymn))
            hymn.lyrics.forEach { add(Slide.Lyrics(it)) }
            add(Slide.End)
        }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var presFz by remember { mutableFloatStateOf(fontSizeMultiplier) }
    val animatedFz by animateFloatAsState(
        targetValue = presFz,
        animationSpec = tween(durationMillis = 150),
        label = "fontSize",
    )
    val slide = slides[currentIndex]

    // Responsive base size: 3.8% of screen width, clamped 18-52
    val basePx = (screenWidth * 0.038f).coerceIn(18f, 52f)
    val scaledSize = (basePx * animatedFz).sp

    val isChorus = slide is Slide.Lyrics && slide.block.type == "chorus"
    val bgColor = if (isChorus) StageBgChorus else StageBgVerse

    // Progress label
    val progressLabel = when (slide) {
        is Slide.Title -> "Title"
        is Slide.End -> "End"
        is Slide.Lyrics -> {
            val contentIndex = currentIndex  // 1-based since title is 0
            val totalContent = slides.size - 2  // minus title and end
            "$contentIndex / $totalContent"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (totalDrag < -60 && currentIndex < slides.size - 1) currentIndex++
                        else if (totalDrag > 60 && currentIndex > 0) currentIndex--
                        totalDrag = 0f
                    },
                    onDragCancel = { totalDrag = 0f },
                    onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                if (currentIndex < slides.size - 1) currentIndex++
            },
    ) {
        // Corner decorations
        CornerDecoration(modifier = Modifier.align(Alignment.TopStart).offset(16.dp, 16.dp), topLeft = true)
        CornerDecoration(modifier = Modifier.align(Alignment.TopEnd).offset((-16).dp, 16.dp), topRight = true)
        CornerDecoration(modifier = Modifier.align(Alignment.BottomStart).offset(16.dp, (-70).dp), bottomLeft = true)
        CornerDecoration(modifier = Modifier.align(Alignment.BottomEnd).offset((-16).dp, (-70).dp), bottomRight = true)

        // Hint text (only on title slide)
        if (currentIndex == 0) {
            Text(
                text = "swipe or tap to advance",
                style = TextStyle(fontFamily = NotoSerif, fontSize = 11.sp),
                color = StageText.copy(alpha = 0.25f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 72.dp),
            )
        }

        // Slide content
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                (fadeIn() + slideInHorizontally { it / 4 })
                    .togetherWith(fadeOut() + slideOutHorizontally { -it / 4 })
            },
            label = "slide",
            modifier = Modifier.fillMaxSize(),
        ) { index ->
            when (val s = slides[index]) {
                is Slide.Title -> TitleSlide(s.hymn, scaledSize * 1.5f)
                is Slide.Lyrics -> LyricsSlide(s.block, scaledSize)
                is Slide.End -> EndSlide()
            }
        }

        // Footer bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(StageFooterBg)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            // Hymn info (left)
            Text(
                text = "${hymn.number}. ${hymn.title}",
                style = TextStyle(fontFamily = NotoSerif, fontSize = 12.sp),
                color = StageText.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.35f),
                maxLines = 1,
            )

            // Font size controls (center)
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = {
                    val newFz = (presFz - 0.15f).coerceAtLeast(0.4f)
                    presFz = newFz
                    onFontSizeChange(newFz)
                }) {
                    Text("A\u2212", color = StageText, fontSize = 14.sp)
                }
                Text(
                    text = progressLabel,
                    style = TextStyle(fontFamily = NotoSerif, fontSize = 12.sp),
                    color = StageText.copy(alpha = 0.5f),
                )
                TextButton(onClick = {
                    val newFz = (presFz + 0.15f).coerceAtMost(2.5f)
                    presFz = newFz
                    onFontSizeChange(newFz)
                }) {
                    Text("A+", color = StageText, fontSize = 14.sp)
                }
            }

            // Navigation (right)
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { if (currentIndex > 0) currentIndex-- },
                    enabled = currentIndex > 0,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                        contentDescription = "Previous slide",
                        tint = if (currentIndex > 0) StageText else StageText.copy(alpha = 0.3f),
                    )
                }
                IconButton(
                    onClick = { if (currentIndex < slides.size - 1) currentIndex++ },
                    enabled = currentIndex < slides.size - 1,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                        contentDescription = "Next slide",
                        tint = if (currentIndex < slides.size - 1) StageText else StageText.copy(alpha = 0.3f),
                    )
                }
                IconButton(
                    onClick = onExit,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit presentation",
                        tint = StageText,
                    )
                }
            }
        }
    }
}

@Composable
private fun CornerDecoration(
    modifier: Modifier,
    topLeft: Boolean = false,
    topRight: Boolean = false,
    bottomLeft: Boolean = false,
    bottomRight: Boolean = false,
) {
    val borderColor = StageText.copy(alpha = 0.06f)
    Box(
        modifier = modifier
            .size(24.dp)
            .drawBehind {
                val stroke = 1.dp.toPx()
                val w = size.width
                val h = size.height
                when {
                    topLeft -> {
                        drawLine(borderColor, Offset(0f, 0f), Offset(w, 0f), stroke)
                        drawLine(borderColor, Offset(0f, 0f), Offset(0f, h), stroke)
                    }
                    topRight -> {
                        drawLine(borderColor, Offset(0f, 0f), Offset(w, 0f), stroke)
                        drawLine(borderColor, Offset(w, 0f), Offset(w, h), stroke)
                    }
                    bottomLeft -> {
                        drawLine(borderColor, Offset(0f, h), Offset(w, h), stroke)
                        drawLine(borderColor, Offset(0f, 0f), Offset(0f, h), stroke)
                    }
                    bottomRight -> {
                        drawLine(borderColor, Offset(0f, h), Offset(w, h), stroke)
                        drawLine(borderColor, Offset(w, 0f), Offset(w, h), stroke)
                    }
                }
            }
    )
}

@Composable
private fun TitleSlide(hymn: Hymn, titleSize: androidx.compose.ui.unit.TextUnit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(StageLabelBg)
                    .border(1.dp, StageLabelBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "HYMN ${hymn.number}",
                    style = TextStyle(
                        fontFamily = NotoSerif,
                        fontSize = 14.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = PurpleLight,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = hymn.title,
                style = TextStyle(
                    fontFamily = PlayfairDisplay,
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    lineHeight = titleSize * 1.28f,
                ),
                color = StageText,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = hymn.englishTitle,
                style = TextStyle(
                    fontFamily = NotoSerif,
                    fontSize = titleSize * 0.5f,
                    fontStyle = FontStyle.Italic,
                    lineHeight = titleSize * 0.72f,
                ),
                color = StageText.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LyricsSlide(
    block: com.sdahymnal.yoruba.data.LyricBlock,
    fontSize: androidx.compose.ui.unit.TextUnit,
) {
    val isChorus = block.type == "chorus"
    val isCallResponse = block.type == "call_response"

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp, vertical = 80.dp),
        ) {
            val label = when (block.type) {
                "verse" -> "VERSE ${block.index}"
                "chorus" -> "CHORUS"
                "call_response" -> "CALL & RESPONSE ${block.index}"
                else -> ""
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(StageLabelBg)
                    .border(1.dp, StageLabelBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    text = label,
                    style = TextStyle(
                        fontFamily = NotoSerif,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = if (isChorus) StageChorusText else PurpleLight,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            val textColor = if (isChorus) StageChorusText else StageText

            if (isCallResponse) {
                block.callResponseLines.forEach { line ->
                    val partLabel = when (line.part) {
                        "leader" -> "Leader / L\u00edl\u00e9"
                        "congregation" -> "All / \u1eb8gb\u1eb9\u0301"
                        else -> line.part
                    }
                    Text(
                        text = partLabel,
                        style = TextStyle(
                            fontFamily = NotoSerif,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = PurpleLight.copy(alpha = 0.7f),
                    )
                    Text(
                        text = line.text,
                        style = TextStyle(
                            fontFamily = NotoSerif,
                            fontSize = fontSize,
                            lineHeight = fontSize * 1.58f,
                            fontStyle = if (line.part == "congregation") FontStyle.Italic else FontStyle.Normal,
                        ),
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }
            } else {
                block.textLines.forEach { line ->
                    Text(
                        text = line,
                        style = TextStyle(
                            fontFamily = NotoSerif,
                            fontSize = fontSize,
                            lineHeight = fontSize * 1.58f,
                        ),
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EndSlide() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(2.dp)
                    .background(PurpleLight.copy(alpha = 0.4f)),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "End of Hymn",
                style = TextStyle(
                    fontFamily = PlayfairDisplay,
                    fontSize = 22.sp,
                    fontStyle = FontStyle.Italic,
                ),
                color = StageText.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(2.dp)
                    .background(PurpleLight.copy(alpha = 0.4f)),
            )
        }
    }
}
