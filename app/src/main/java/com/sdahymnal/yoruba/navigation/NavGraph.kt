package com.sdahymnal.yoruba.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.sdahymnal.yoruba.MainViewModel
import com.sdahymnal.yoruba.data.HymnLoadState
import com.sdahymnal.yoruba.ui.components.BottomNavBar
import com.sdahymnal.yoruba.ui.components.BottomTab
import com.sdahymnal.yoruba.ui.components.NumberPadDialog
import com.sdahymnal.yoruba.ui.screens.CategoriesScreen
import com.sdahymnal.yoruba.ui.screens.CategoryDetailScreen
import com.sdahymnal.yoruba.ui.screens.ErrorScreen
import com.sdahymnal.yoruba.ui.screens.HymnDetailScreen
import com.sdahymnal.yoruba.ui.screens.FavoritesScreen
import com.sdahymnal.yoruba.ui.screens.HymnListScreen
import com.sdahymnal.yoruba.ui.screens.LoadingScreen
import com.sdahymnal.yoruba.ui.screens.MoreScreen
import com.sdahymnal.yoruba.ui.screens.PresentationScreen

object Routes {
    const val HYMN_LIST = "hymn_list"
    const val HYMN_DETAIL = "hymn_detail/{hymnNumber}"
    const val PRESENTATION = "presentation/{hymnNumber}"
    const val CATEGORIES = "categories"
    const val CATEGORY_DETAIL = "category_detail/{categoryId}"
    const val FAVORITES = "favorites"
    const val MORE = "more"

    fun hymnDetail(number: Int) = "hymn_detail/$number"
    fun presentation(number: Int) = "presentation/$number"
    fun categoryDetail(categoryId: String) = "category_detail/$categoryId"
}

@Composable
fun HymnNavGraph(
    navController: NavHostController,
    viewModel: MainViewModel,
) {
    val loadState by viewModel.loadState.collectAsState()
    val selectedHymnNumber by viewModel.selectedHymnNumber.collectAsState()
    val readingFontSize by viewModel.readingFontSize.collectAsState()
    val presentationFontSize by viewModel.presentationFontSize.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.Hymns) }
    var showNumberPad by rememberSaveable { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Routes.PRESENTATION

    // Derive selected tab and track screen views from navigation state
    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            Routes.HYMN_LIST -> {
                selectedTab = BottomTab.Hymns
                viewModel.trackPageView("/hymns")
            }
            Routes.CATEGORIES -> {
                selectedTab = BottomTab.Categories
                viewModel.trackPageView("/categories")
            }
            Routes.CATEGORY_DETAIL -> {
                selectedTab = BottomTab.Categories
                viewModel.trackPageView("/categories/detail")
            }
            Routes.FAVORITES -> {
                selectedTab = BottomTab.Favorites
                viewModel.trackPageView("/favorites")
            }
            Routes.MORE -> {
                selectedTab = BottomTab.More
                viewModel.trackPageView("/more")
            }
            Routes.HYMN_DETAIL -> viewModel.trackPageView("/hymn")
            Routes.PRESENTATION -> viewModel.trackPageView("/presentation")
        }
    }

    when (val state = loadState) {
        is HymnLoadState.Loading -> LoadingScreen()
        is HymnLoadState.Error -> ErrorScreen(
            message = state.message,
            onRetry = { viewModel.load() },
        )
        is HymnLoadState.Ready -> {
            val hymns = state.hymns

            // Restore last hymn on first load (skip if a deep link is pending)
            val pendingDeepLink by viewModel.pendingDeepLink.collectAsState()
            LaunchedEffect(Unit) {
                if (pendingDeepLink > 0) return@LaunchedEffect
                val lastHymn = viewModel.lastHymn
                if (lastHymn > 0 && viewModel.getByNumber(lastHymn) != null) {
                    viewModel.selectHymn(lastHymn)
                    navController.navigate(Routes.hymnDetail(lastHymn))
                }
            }

            // Navigate on deep link (works for both initial launch and onNewIntent)
            LaunchedEffect(pendingDeepLink) {
                if (pendingDeepLink > 0 && viewModel.getByNumber(pendingDeepLink) != null) {
                    viewModel.selectHymn(pendingDeepLink)
                    navController.navigate(Routes.hymnDetail(pendingDeepLink)) {
                        launchSingleTop = true
                    }
                    viewModel.consumeDeepLink()
                }
            }

            // Back from non-Hymns tabs goes to Hymns instead of exiting
            val onNonHymnTab = currentRoute == Routes.CATEGORIES ||
                currentRoute == Routes.FAVORITES ||
                currentRoute == Routes.MORE
            BackHandler(enabled = onNonHymnTab) {
                navController.navigate(Routes.HYMN_LIST) {
                    popUpTo(Routes.HYMN_LIST) { inclusive = false }
                    launchSingleTop = true
                }
            }

            // Number pad dialog
            if (showNumberPad) {
                NumberPadDialog(
                    onDismiss = { showNumberPad = false },
                    hymnExists = { viewModel.getByNumber(it) != null },
                    onGoToHymn = { number ->
                        showNumberPad = false
                        viewModel.trackNumpad(number)
                        viewModel.selectHymn(number)
                        navController.navigate(Routes.hymnDetail(number))
                    },
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
              Column(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.HYMN_LIST,
                    modifier = Modifier.weight(1f),
                    enterTransition = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it / 6 } },
                    exitTransition = { fadeOut(tween(150)) },
                    popEnterTransition = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { -it / 6 } },
                    popExitTransition = { fadeOut(tween(150)) + slideOutHorizontally(tween(200)) { it / 6 } },
                ) {
                    composable(Routes.HYMN_LIST) {
                        HymnListScreen(
                            hymns = hymns,
                            selectedHymnNumber = selectedHymnNumber,
                            searchQuery = searchQuery,
                            searchResults = searchResults,
                            isSearching = isSearching,
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onHymnClick = { hymn ->
                                viewModel.selectHymn(hymn.number)
                                navController.navigate(Routes.hymnDetail(hymn.number))
                            },
                            favorites = favorites,
                            themeMode = themeMode,
                            onSetTheme = { viewModel.setTheme(it) },
                        )
                    }

                    composable(Routes.CATEGORIES) {
                        CategoriesScreen(
                            hymns = hymns,
                            favorites = favorites,
                            onCategoryClick = { category ->
                                viewModel.trackCategory(category.id)
                                navController.navigate(Routes.categoryDetail(category.id))
                            },
                        )
                    }

                    composable(
                        route = Routes.CATEGORY_DETAIL,
                        arguments = listOf(
                            navArgument("categoryId") { type = NavType.StringType }
                        ),
                    ) { backStackEntry ->
                        val categoryId = backStackEntry.arguments?.getString("categoryId") ?: return@composable
                        val category = com.sdahymnal.yoruba.data.HymnCategoryStore.categories
                            .firstOrNull { it.id == categoryId } ?: return@composable
                        val categoryHymns = com.sdahymnal.yoruba.data.HymnCategoryStore.hymnsIn(category, hymns)

                        CategoryDetailScreen(
                            categoryName = category.name,
                            categoryEnglishTitle = category.englishTitle,
                            hymns = categoryHymns,
                            favorites = favorites,
                            onHymnClick = { hymn ->
                                viewModel.selectHymn(hymn.number)
                                navController.navigate(Routes.hymnDetail(hymn.number))
                            },
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(Routes.FAVORITES) {
                        FavoritesScreen(
                            favoriteHymns = hymns.filter { it.number in favorites },
                            selectedHymnNumber = selectedHymnNumber,
                            onHymnClick = { hymn ->
                                viewModel.selectHymn(hymn.number)
                                navController.navigate(Routes.hymnDetail(hymn.number))
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onBrowseHymns = {
                                navController.navigate(Routes.HYMN_LIST) {
                                    popUpTo(Routes.HYMN_LIST) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                        )
                    }


                    composable(Routes.MORE) {
                        MoreScreen(
                            themeMode = themeMode,
                            hymnCount = hymns.size,
                            favoritesCount = favorites.size,
                            hymnCacheVersion = viewModel.hymnCacheVersion,
                            readingFontSize = readingFontSize,
                            onSetTheme = { viewModel.setTheme(it) },
                            onCycleReadingFontSize = { viewModel.cycleReadingFontSize() },
                            onClearFavorites = { viewModel.clearFavorites() },
                            onTrackEvent = { viewModel.trackEvent(it) },
                        )
                    }

                    composable(
                        route = Routes.HYMN_DETAIL,
                        arguments = listOf(
                            navArgument("hymnNumber") { type = NavType.IntType }
                        ),
                    ) { backStackEntry ->
                        val hymnNumber = backStackEntry.arguments?.getInt("hymnNumber") ?: return@composable
                        val hymn = viewModel.getByNumber(hymnNumber) ?: return@composable
                        val currentIndex = hymns.indexOfFirst { it.number == hymnNumber }

                        HymnDetailScreen(
                            hymn = hymn,
                            hasPrevious = currentIndex > 0,
                            hasNext = currentIndex < hymns.size - 1,
                            isFavorite = hymn.number in favorites,
                            onToggleFavorite = { viewModel.toggleFavorite(hymn.number) },
                            onBack = { navController.popBackStack() },
                            onShare = { viewModel.trackShare(hymn.number) },
                            onPresent = {
                                viewModel.trackPresentation(hymn.number)
                                navController.navigate(Routes.presentation(hymn.number))
                            },
                            onPrevious = {
                                if (currentIndex > 0) {
                                    val prev = hymns[currentIndex - 1]
                                    viewModel.selectHymn(prev.number)
                                    navController.navigate(Routes.hymnDetail(prev.number)) {
                                        popUpTo(Routes.HYMN_DETAIL) { inclusive = true }
                                    }
                                }
                            },
                            onNext = {
                                if (currentIndex < hymns.size - 1) {
                                    val next = hymns[currentIndex + 1]
                                    viewModel.selectHymn(next.number)
                                    navController.navigate(Routes.hymnDetail(next.number)) {
                                        popUpTo(Routes.HYMN_DETAIL) { inclusive = true }
                                    }
                                }
                            },
                            readingFontSize = readingFontSize,
                            onCycleReadingFontSize = { viewModel.cycleReadingFontSize() },
                        )
                    }

                    composable(
                        route = Routes.PRESENTATION,
                        arguments = listOf(
                            navArgument("hymnNumber") { type = NavType.IntType }
                        ),
                    ) { backStackEntry ->
                        val hymnNumber = backStackEntry.arguments?.getInt("hymnNumber") ?: return@composable
                        val hymn = viewModel.getByNumber(hymnNumber) ?: return@composable

                        PresentationScreen(
                            hymn = hymn,
                            onExit = { navController.popBackStack() },
                            fontSizeMultiplier = presentationFontSize,
                            onFontSizeChange = { viewModel.setPresentationFontSize(it) },
                        )
                    }
                }

                if (showBottomBar) {
                    BottomNavBar(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            val route = when (tab) {
                                BottomTab.Hymns -> Routes.HYMN_LIST
                                BottomTab.Categories -> Routes.CATEGORIES
                                BottomTab.Favorites -> Routes.FAVORITES
                                BottomTab.More -> Routes.MORE
                            }
                            // Skip if already on this exact route
                            if (currentRoute == route) return@BottomNavBar
                            navController.navigate(route) {
                                popUpTo(Routes.HYMN_LIST) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                    )
                }
              }

              // Floating keypad button
              if (showBottomBar) {
                  FloatingActionButton(
                      onClick = { showNumberPad = true },
                      modifier = Modifier
                          .align(Alignment.BottomEnd)
                          .padding(end = 16.dp, bottom = 108.dp)
                          .size(48.dp),
                      shape = CircleShape,
                      containerColor = com.sdahymnal.yoruba.ui.theme.PurpleHeader,
                      contentColor = androidx.compose.ui.graphics.Color.White,
                  ) {
                      Icon(
                          imageVector = Icons.Default.Dialpad,
                          contentDescription = "Go to hymn number",
                          modifier = Modifier.size(22.dp),
                      )
                  }
              }
            }
        }
    }
}
