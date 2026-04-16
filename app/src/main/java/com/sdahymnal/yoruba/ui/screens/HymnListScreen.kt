package com.sdahymnal.yoruba.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sdahymnal.yoruba.R
import com.sdahymnal.yoruba.data.Hymn
import com.sdahymnal.yoruba.ui.components.BrandHeader
import com.sdahymnal.yoruba.ui.components.HymnRow
import com.sdahymnal.yoruba.ui.components.HymnSearchBar
import com.sdahymnal.yoruba.ui.components.ThemeMenuItems

@Composable
fun HymnListScreen(
    displayedHymns: List<Hymn>,
    selectedHymnNumber: Int?,
    searchQuery: String,
    isSearching: Boolean = false,
    onSearchQueryChange: (String) -> Unit,
    onHymnClick: (Hymn) -> Unit,
    favorites: Set<Int> = emptySet(),
    themeMode: String = "system",
    onSetTheme: (String) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Scroll to top when search query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            listState.scrollToItem(0)
        }
    }

    // Scroll to selected hymn when returning from detail
    LaunchedEffect(selectedHymnNumber) {
        if (selectedHymnNumber != null && selectedHymnNumber > 0) {
            val index = displayedHymns.indexOfFirst { it.number == selectedHymnNumber }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    val themeIcon = when (themeMode) {
        "light" -> Icons.Default.LightMode
        "dark" -> Icons.Default.DarkMode
        else -> Icons.Default.BrightnessAuto
    }

    var showThemeMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            BrandHeader(
                trailingIcon = {
                    Box {
                        IconButton(onClick = { showThemeMenu = true }) {
                            Icon(
                                imageVector = themeIcon,
                                contentDescription = "Theme",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false },
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
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HymnSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                )

                if (isSearching) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    )
                }

                if (displayedHymns.isEmpty() && searchQuery.isNotBlank() && !isSearching) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SearchOff,
                            contentDescription = "No results",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_hymns_found),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.try_different_search),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        )
                    }
                } else {
                    Text(
                        text = pluralStringResource(R.plurals.hymn_count, displayedHymns.size, displayedHymns.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )

                    val highlightedNumber = if (searchQuery.isNotBlank())
                        displayedHymns.firstOrNull()?.number else selectedHymnNumber

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            items = displayedHymns,
                            key = { it.number },
                        ) { hymn ->
                            HymnRow(
                                hymn = hymn,
                                isSelected = hymn.number == highlightedNumber,
                                isFavorite = hymn.number in favorites,
                                searchQuery = searchQuery,
                                onClick = {
                                    focusManager.clearFocus()
                                    onHymnClick(hymn)
                                },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }
    }
}
