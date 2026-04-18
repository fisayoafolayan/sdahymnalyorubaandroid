package com.sdahymnalyoruba

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sdahymnalyoruba.data.Analytics
import com.sdahymnalyoruba.data.Hymn
import com.sdahymnalyoruba.data.HymnLoadState
import com.sdahymnalyoruba.data.HymnRepository
import com.sdahymnalyoruba.data.Preferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = Preferences(application)
    private val repository = HymnRepository(application, preferences)

    // --- Hymn data ---
    val loadState: StateFlow<HymnLoadState> = repository.state
        .stateIn(viewModelScope, SharingStarted.Eagerly, HymnLoadState.Loading)

    val hymns: List<Hymn> get() = repository.hymns
    val hymnCacheVersion: String? get() = preferences.hymnsEtag?.take(12)

    // --- Theme ---
    private val _themeMode = MutableStateFlow(preferences.themeMode)
    val themeMode: StateFlow<String> = _themeMode

    fun setTheme(mode: String) {
        _themeMode.value = mode
        preferences.themeMode = mode
        Analytics.trackEvent("theme_$mode")
    }

    // --- Font sizes ---
    private val _readingFontSize = MutableStateFlow(preferences.readingFontSize)
    val readingFontSize: StateFlow<Float> = _readingFontSize

    fun cycleReadingFontSize() {
        val sizes = Preferences.READING_SIZES
        val idx = sizes.indexOfFirst { it == _readingFontSize.value }
        val next = sizes[(idx + 1) % sizes.size]
        _readingFontSize.value = next
        preferences.readingFontSize = next
        Analytics.trackEvent("fontsize_${next}")
    }

    private val _presentationFontSize = MutableStateFlow(preferences.presentationFontSize)
    val presentationFontSize: StateFlow<Float> = _presentationFontSize

    fun setPresentationFontSize(value: Float) {
        _presentationFontSize.value = value
        preferences.presentationFontSize = value
    }

    // --- Selected hymn ---
    private val _selectedHymnNumber = MutableStateFlow(-1)
    val selectedHymnNumber: StateFlow<Int> = _selectedHymnNumber

    fun selectHymn(number: Int) {
        _selectedHymnNumber.value = number
        preferences.lastHymn = number
        Analytics.trackEvent("hymn_$number")
    }

    // --- Favorites ---
    private val _favorites = MutableStateFlow(preferences.favorites)
    val favorites: StateFlow<Set<Int>> = _favorites

    fun toggleFavorite(hymnNumber: Int) {
        val wasFavorite = hymnNumber in _favorites.value
        _favorites.value = preferences.toggleFavorite(hymnNumber)
        Analytics.trackEvent(if (wasFavorite) "unfavorite_$hymnNumber" else "favorite_$hymnNumber")
    }

    fun clearFavorites() {
        Analytics.trackEvent("clear_favorites")
        preferences.favorites = emptySet()
        _favorites.value = emptySet()
    }

    // --- Search with 150ms debounce (matching web) ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isSearchPending = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearchPending
        .debounce { if (it) 300 else 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val searchResults: StateFlow<List<Hymn>> = combine(
        _searchQuery.debounce(150),
        repository.state,
    ) { query, state ->
        val allHymns = (state as? HymnLoadState.Ready)?.hymns.orEmpty()
        val results = if (query.isBlank()) allHymns else repository.search(query)
        _isSearchPending.value = false
        results
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) _isSearchPending.value = true
    }

    // Track search after 1000ms of inactivity (matching web), race-free via Flow
    init {
        _searchQuery
            .debounce(1000)
            .map { it.trim().take(50) }
            .filter { it.isNotBlank() }
            .onEach { Analytics.trackEvent("search_$it") }
            .launchIn(viewModelScope)
    }

    fun trackPresentation(hymnNumber: Int) {
        Analytics.trackEvent("presented_$hymnNumber")
    }

    fun trackShare(hymnNumber: Int) {
        Analytics.trackEvent("share_$hymnNumber")
    }

    fun trackNumpad(hymnNumber: Int) {
        Analytics.trackEvent("numpad_$hymnNumber")
    }

    fun trackCategory(categoryId: String) {
        Analytics.trackEvent("category_$categoryId")
    }

    fun trackPageView(url: String) {
        Analytics.trackPageView(url)
    }

    fun trackEvent(name: String) {
        Analytics.trackEvent(name)
    }

    fun getByNumber(number: Int): Hymn? = repository.getByNumber(number)

    // --- Number pad ---
    private val _showNumberPad = MutableStateFlow(false)
    val showNumberPad: StateFlow<Boolean> = _showNumberPad

    fun openNumberPad() { _showNumberPad.value = true }
    fun closeNumberPad() { _showNumberPad.value = false }

    // --- Deep links ---
    private val _pendingDeepLink = MutableStateFlow(-1)
    val pendingDeepLink: StateFlow<Int> = _pendingDeepLink

    fun setDeepLink(hymnNumber: Int) {
        if (hymnNumber > 0) _pendingDeepLink.value = hymnNumber
    }

    fun consumeDeepLink() {
        _pendingDeepLink.value = -1
    }

    // --- Data loading ---
    fun load() {
        viewModelScope.launch { repository.load() }
    }

    // --- App readiness (splash screen waits on this) ---
    private val _appReady = MutableStateFlow(false)
    val appReady: StateFlow<Boolean> = _appReady

    // --- Restore state ---
    val lastHymn: Int get() = preferences.lastHymn

    init {
        load()
        Analytics.trackEvent("app_launch")

        // Signal app ready after data loads and search results are populated
        viewModelScope.launch {
            searchResults.first { it.isNotEmpty() }
            // Allow Compose to draw the populated list
            delay(50)
            _appReady.value = true
        }
    }
}
