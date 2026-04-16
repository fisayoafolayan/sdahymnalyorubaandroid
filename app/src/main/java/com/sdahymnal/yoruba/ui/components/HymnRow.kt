package com.sdahymnal.yoruba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.sdahymnal.yoruba.data.Hymn
import com.sdahymnal.yoruba.data.HymnRepository
import com.sdahymnal.yoruba.ui.theme.SelectedRowBg
import com.sdahymnal.yoruba.ui.theme.SelectedRowNumber
import com.sdahymnal.yoruba.ui.theme.SelectedRowText

@Composable
fun HymnRow(
    hymn: Hymn,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    searchQuery: String = "",
    onFavoriteClick: (() -> Unit)? = null,
) {
    val titleColor = if (isSelected) SelectedRowText else MaterialTheme.colorScheme.onBackground
    val subtitleColor = if (isSelected) SelectedRowText.copy(alpha = 0.7f)
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    val highlightColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isSelected) SelectedRowBg else MaterialTheme.colorScheme.background)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = hymn.number.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) SelectedRowNumber else MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(36.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                val activeQuery = if (isSelected) "" else searchQuery
                Text(
                    text = highlightText(hymn.title, activeQuery, titleColor, highlightColor),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = highlightText(hymn.englishTitle, activeQuery, subtitleColor, highlightColor),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (isFavorite) {
                if (onFavoriteClick != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable(onClick = onFavoriteClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Remove from favorites",
                            tint = com.sdahymnal.yoruba.ui.theme.FavoriteHeart.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = com.sdahymnal.yoruba.ui.theme.FavoriteHeart.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
        if (!isSelected) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                thickness = 0.5.dp,
            )
        }
    }
}

private fun highlightText(
    text: String,
    query: String,
    baseColor: Color,
    highlightColor: Color,
) = buildAnnotatedString {
    if (query.isBlank()) {
        withStyle(SpanStyle(color = baseColor)) { append(text) }
        return@buildAnnotatedString
    }

    val normalizedQuery = HymnRepository.removeDiacritics(query.trim())
    if (normalizedQuery.isEmpty()) {
        withStyle(SpanStyle(color = baseColor)) { append(text) }
        return@buildAnnotatedString
    }

    // Build mapping from normalized positions back to original positions.
    // Uses NFD decomposition directly instead of per-character removeDiacritics()
    // to avoid repeated regex + normalization calls in the render path.
    val nfd = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
    val normalized = StringBuilder()
    val toOriginal = IntArray(nfd.length) // upper bound, trimmed by normalizedLen
    var normalizedLen = 0
    var origIdx = 0
    var nfdIdx = 0

    for (i in text.indices) {
        val charNfd = java.text.Normalizer.normalize(text[i].toString(), java.text.Normalizer.Form.NFD)
        for (j in charNfd.indices) {
            val ch = charNfd[j]
            // Keep only alphanumeric and whitespace (same as removeDiacritics)
            if (ch.isLetterOrDigit() || ch.isWhitespace()) {
                if (!isCombiningMark(ch)) {
                    toOriginal[normalizedLen] = i
                    normalized.append(ch.lowercaseChar())
                    normalizedLen++
                }
            }
            nfdIdx++
        }
        origIdx++
    }

    val normalizedStr = normalized.toString()
    val matchIndex = normalizedStr.indexOf(normalizedQuery)
    if (matchIndex < 0) {
        withStyle(SpanStyle(color = baseColor)) { append(text) }
        return@buildAnnotatedString
    }

    val matchEndNorm = matchIndex + normalizedQuery.length
    val originalStart = toOriginal[matchIndex]
    val originalEnd = if (matchEndNorm < normalizedLen) {
        toOriginal[matchEndNorm]
    } else {
        text.length
    }

    withStyle(SpanStyle(color = baseColor)) { append(text.substring(0, originalStart)) }
    withStyle(SpanStyle(color = highlightColor, fontWeight = FontWeight.Bold)) {
        append(text.substring(originalStart, originalEnd))
    }
    withStyle(SpanStyle(color = baseColor)) { append(text.substring(originalEnd)) }
}

private fun isCombiningMark(ch: Char): Boolean {
    val type = Character.getType(ch)
    return type == Character.NON_SPACING_MARK.toInt() ||
        type == Character.COMBINING_SPACING_MARK.toInt() ||
        type == Character.ENCLOSING_MARK.toInt()
}
