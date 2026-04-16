package com.sdahymnal.yoruba

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sdahymnal.yoruba.data.Analytics
import com.sdahymnal.yoruba.data.Hymn
import com.sdahymnal.yoruba.data.HymnLoadState
import com.sdahymnal.yoruba.data.HymnRepository
import com.sdahymnal.yoruba.data.Preferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
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

    fun toggleTheme() {
        val next = when (_themeMode.value) {
            "light" -> "dark"
            "dark" -> "system"
            else -> "light"
        }
        _themeMode.value = next
        preferences.themeMode = next
        Analytics.trackEvent("theme_$next")
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

    private var searchTrackTimer: kotlinx.coroutines.Job? = null

    val searchResults: StateFlow<List<Hymn>> = combine(
        _searchQuery.debounce(150),
        repository.state,
    ) { query, state ->
        val allHymns = (state as? HymnLoadState.Ready)?.hymns.orEmpty()
        if (query.isBlank()) allHymns else repository.search(query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // Track search after 1000ms of inactivity (matching web)
        searchTrackTimer?.cancel()
        if (query.isNotBlank()) {
            searchTrackTimer = viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                Analytics.trackEvent("search_${query.take(50)}")
            }
        }
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

    fun trackEvent(name: String) {
        Analytics.trackEvent(name)
    }

    fun getByNumber(number: Int): Hymn? = repository.getByNumber(number)

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

    // --- Restore state ---
    val lastHymn: Int get() = preferences.lastHymn

    init {
        load()
        Analytics.trackEvent("app_launch")
    }
}
