package com.sdahymnalyoruba.navigation

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
import com.sdahymnalyoruba.MainViewModel
import com.sdahymnalyoruba.data.Hymn
import com.sdahymnalyoruba.data.HymnCategoryStore
import com.sdahymnalyoruba.data.HymnLoadState
import com.sdahymnalyoruba.ui.components.BottomNavBar
import com.sdahymnalyoruba.ui.components.BottomTab
import com.sdahymnalyoruba.ui.components.NumberPadDialog
import com.sdahymnalyoruba.ui.screens.CategoriesScreen
import com.sdahymnalyoruba.ui.screens.CategoryDetailScreen
import com.sdahymnalyoruba.ui.screens.ErrorScreen
import com.sdahymnalyoruba.ui.screens.HymnDetailScreen
import com.sdahymnalyoruba.ui.screens.FavoritesScreen
import com.sdahymnalyoruba.ui.screens.HymnListScreen
import com.sdahymnalyoruba.ui.screens.LoadingScreen
import com.sdahymnalyoruba.ui.screens.MoreScreen
import com.sdahymnalyoruba.ui.screens.PresentationScreen

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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Track screen views
    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            Routes.HYMN_LIST -> viewModel.trackPageView("/hymns")
            Routes.CATEGORIES -> viewModel.trackPageView("/categories")
            Routes.CATEGORY_DETAIL -> viewModel.trackPageView("/categories/detail")
            Routes.FAVORITES -> viewModel.trackPageView("/favorites")
            Routes.MORE -> viewModel.trackPageView("/more")
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
        is HymnLoadState.Ready -> ReadyContent(
            navController = navController,
            viewModel = viewModel,
            hymns = state.hymns,
            currentRoute = currentRoute,
        )
    }
}

@Composable
private fun ReadyContent(
    navController: NavHostController,
    viewModel: MainViewModel,
    hymns: List<Hymn>,
    currentRoute: String?,
) {
    val selectedHymnNumber by viewModel.selectedHymnNumber.collectAsState()
    val readingFontSize by viewModel.readingFontSize.collectAsState()
    val presentationFontSize by viewModel.presentationFontSize.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val showNumberPad by viewModel.showNumberPad.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.Hymns) }

    var wasInPresentation by rememberSaveable { mutableStateOf(false) }
    var showBottomBar by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(currentRoute) {
        if (currentRoute == Routes.PRESENTATION) {
            wasInPresentation = true
            showBottomBar = false
        } else if (wasInPresentation) {
            // Delay showing bottom bar until presentation exit animation completes
            kotlinx.coroutines.delay(300)
            wasInPresentation = false
            showBottomBar = true
        } else {
            showBottomBar = true
        }
    }

    // Sync selected tab from navigation
    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            Routes.HYMN_LIST -> selectedTab = BottomTab.Hymns
            Routes.CATEGORIES, Routes.CATEGORY_DETAIL -> selectedTab = BottomTab.Categories
            Routes.FAVORITES -> selectedTab = BottomTab.Favorites
            Routes.MORE -> selectedTab = BottomTab.More
        }
    }

    // Determine initial destination (deep link > last hymn > hymn list)
    val pendingDeepLink by viewModel.pendingDeepLink.collectAsState()
    val initialHymn = rememberSaveable {
        val deepLink = viewModel.pendingDeepLink.value
        if (deepLink > 0 && viewModel.getByNumber(deepLink) != null) {
            viewModel.selectHymn(deepLink)
            viewModel.consumeDeepLink()
            deepLink
        } else {
            val last = viewModel.lastHymn
            if (last > 0 && viewModel.getByNumber(last) != null) {
                viewModel.selectHymn(last)
                last
            } else -1
        }
    }

    // Handle deep links arriving after initial launch (onNewIntent)
    LaunchedEffect(pendingDeepLink) {
        if (pendingDeepLink > 0) {
            if (viewModel.getByNumber(pendingDeepLink) != null) {
                viewModel.selectHymn(pendingDeepLink)
                navController.navigate(Routes.hymnDetail(pendingDeepLink)) {
                    launchSingleTop = true
                }
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
            onDismiss = { viewModel.closeNumberPad() },
            getHymnTitle = { viewModel.getByNumber(it)?.title },
            onGoToHymn = { number ->
                viewModel.closeNumberPad()
                viewModel.trackNumpad(number)
                viewModel.selectHymn(number)
                navController.navigate(Routes.hymnDetail(number))
            },
        )
    }

    AppScaffold(
        showBottomBar = showBottomBar,
        selectedTab = selectedTab,
        onTabSelected = { tab ->
            val route = when (tab) {
                BottomTab.Hymns -> Routes.HYMN_LIST
                BottomTab.Categories -> Routes.CATEGORIES
                BottomTab.Favorites -> Routes.FAVORITES
                BottomTab.More -> Routes.MORE
            }
            if (currentRoute == route) return@AppScaffold
            navController.navigate(route) {
                popUpTo(Routes.HYMN_LIST) { inclusive = false }
                launchSingleTop = true
            }
        },
        onOpenNumberPad = { viewModel.openNumberPad() },
    ) {
        HymnNavHost(
            navController = navController,
            viewModel = viewModel,
            hymns = hymns,
            searchResults = searchResults,
            selectedHymnNumber = selectedHymnNumber,
            searchQuery = searchQuery,
            isSearching = isSearching,
            favorites = favorites,
            themeMode = themeMode,
            readingFontSize = readingFontSize,
            presentationFontSize = presentationFontSize,
            initialHymn = initialHymn,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AppScaffold(
    showBottomBar: Boolean,
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onOpenNumberPad: () -> Unit,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            content()

            if (showBottomBar) {
                BottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                )
            }
        }

        // Floating keypad button
        if (showBottomBar) {
            FloatingActionButton(
                onClick = onOpenNumberPad,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 108.dp)
                    .size(48.dp),
                shape = CircleShape,
                containerColor = com.sdahymnalyoruba.ui.theme.PurpleHeader,
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

@Composable
private fun HymnNavHost(
    navController: NavHostController,
    viewModel: MainViewModel,
    hymns: List<Hymn>,
    searchResults: List<Hymn>,
    selectedHymnNumber: Int?,
    searchQuery: String,
    isSearching: Boolean,
    favorites: Set<Int>,
    themeMode: String,
    readingFontSize: Float,
    presentationFontSize: Float,
    modifier: Modifier = Modifier,
    initialHymn: Int = -1,
) {
    LaunchedEffect(initialHymn) {
        if (initialHymn > 0) {
            navController.navigate(Routes.hymnDetail(initialHymn))
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.HYMN_LIST,
        modifier = modifier,
        enterTransition = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it / 6 } },
        exitTransition = { fadeOut(tween(150)) },
        popEnterTransition = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { -it / 6 } },
        popExitTransition = { fadeOut(tween(150)) + slideOutHorizontally(tween(200)) { it / 6 } },
    ) {
        composable(Routes.HYMN_LIST) {
            HymnListScreen(
                displayedHymns = searchResults,
                selectedHymnNumber = selectedHymnNumber,
                searchQuery = searchQuery,
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
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: return@composable
            val category = HymnCategoryStore.categories
                .firstOrNull { it.id == categoryId } ?: return@composable
            val categoryHymns = HymnCategoryStore.hymnsIn(category, hymns)

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
            arguments = listOf(navArgument("hymnNumber") { type = NavType.IntType }),
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
            arguments = listOf(navArgument("hymnNumber") { type = NavType.IntType }),
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
}
