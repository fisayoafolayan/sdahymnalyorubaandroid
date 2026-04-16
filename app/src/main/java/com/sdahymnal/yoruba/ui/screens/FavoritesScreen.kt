package com.sdahymnal.yoruba.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.sdahymnal.yoruba.R
import com.sdahymnal.yoruba.data.Hymn
import com.sdahymnal.yoruba.ui.components.BrandHeader
import com.sdahymnal.yoruba.ui.components.HymnRow

@Composable
fun FavoritesScreen(
    favoriteHymns: List<Hymn>,
    selectedHymnNumber: Int?,
    onHymnClick: (Hymn) -> Unit,
    onToggleFavorite: (Int) -> Unit = {},
    onBrowseHymns: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { BrandHeader() },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 64.dp),
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    actionColor = MaterialTheme.colorScheme.primary,
                )
            }
        },
    ) { padding ->
        if (favoriteHymns.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "No favorites",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No favorites yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap the heart on any hymn to add it here",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                )
                Spacer(modifier = Modifier.height(20.dp))
                TextButton(onClick = onBrowseHymns) {
                    Text(
                        text = "Browse Hymns",
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                if (favoriteHymns.size > 7) {
                    Text(
                        text = pluralStringResource(R.plurals.hymn_count, favoriteHymns.size, favoriteHymns.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = favoriteHymns,
                        key = { it.number },
                    ) { hymn ->
                        HymnRow(
                            hymn = hymn,
                            isSelected = hymn.number == selectedHymnNumber,
                            isFavorite = true,
                            onFavoriteClick = {
                                onToggleFavorite(hymn.number)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Removed \u201C${hymn.title}\u201D",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short,
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        onToggleFavorite(hymn.number)
                                    }
                                }
                            },
                            onClick = { onHymnClick(hymn) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}
