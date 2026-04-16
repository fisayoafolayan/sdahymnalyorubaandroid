# SDA Hymnal Yoruba - Android

The official Android companion app for [sdahymnalyoruba.com](https://sdahymnalyoruba.com). Browse, search, and present Seventh-day Adventist hymns in Yoruba.

## Features

- **620+ hymns** with Yoruba lyrics, English titles, and cross-references (SDAH, NAH, CH)
- **Live data** - fetches hymns from the web with ETag caching (no app update needed for hymn changes)
- **Full-text search** - diacritics-insensitive, scored ranking (number > title > English > references > lyrics), 150ms debounce, search highlighting
- **Presentation mode** - full-screen projection with slide-by-slide navigation, swipe/tap controls, adjustable font size, keeps screen on
- **Categories** - 60+ hymn categories in a 2-column grid, tap to browse hymns by topic
- **Favorites** - mark hymns with a heart, browse in Favorites tab, tap heart to remove
- **Number pad** - jump directly to any hymn number via floating keypad with validation
- **Settings** - theme toggle, font size, hymn data info, favorites management, share/rate app, contact, privacy policy
- **Theme support** - light, dark, and system modes with manual toggle
- **Font size controls** - 3 reading sizes (1.0x, 1.2x, 1.45x), adjustable from hymn view or settings
- **Swipe navigation** - swipe left/right between hymns with slide animation
- **Deep linking** - handles `https://sdahymnalyoruba.com/?hymn=N` URLs
- **Share** - share hymn URLs via Android share sheet
- **Offline support** - cached hymns available without internet
- **Haptic feedback** - subtle vibration on favorite toggle
- **Landscape support** - responsive layout for hymn detail
- **Last hymn recall** - reopens to the last viewed hymn
- **Scroll to selected** - hymn list scrolls to your last viewed hymn on return
- **Keyboard dismiss** - keyboard hides when tapping a hymn from search
- **Analytics** - Umami event tracking (search, views, shares, favorites, categories, presentation)

## Tech Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **MVVM** architecture with `AndroidViewModel` + `StateFlow`
- **OkHttp** for network requests with ETag-based caching
- **kotlinx.serialization** for JSON parsing
- **Navigation Compose** for screen routing with animated transitions
- **Playfair Display + Noto Serif** fonts (bundled TTFs)
- **Umami** analytics (same dashboard as web app)
- **JUnit 4** for unit tests, **Compose UI Testing** for instrumentation tests

## Requirements

- Android Studio Hedgehog (2023.1) or later
- JDK 17
- Android SDK 35
- Min SDK 26 (Android 8.0)
- Kotlin 2.1+

## Setup

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle
4. Run on device or emulator

No API keys or additional configuration needed. The app fetches hymn data from `https://sdahymnalyoruba.com/hymns.json`.

## Running on a Device

**Emulator:** Tools > Device Manager > Create Device > Run

**USB debug (app stays installed after unplug):**
1. Enable Developer Options on your phone (Settings > About > tap Build Number 7 times)
2. Enable USB Debugging in Developer Options
3. Connect via USB, approve the prompt
4. Select your device in Android Studio and click Run

## Project Structure

```
app/src/
  main/java/com/sdahymnal/yoruba/
    MainActivity.kt              # Entry point, edge-to-edge setup
    MainViewModel.kt             # App state (theme, search, favorites, font sizes, analytics)
    data/
      Hymn.kt                    # Data models (Hymn, LyricBlock, CallResponseLine)
      HymnRepository.kt          # Network + ETag cache + search engine
      HymnCategories.kt          # Category definitions with hymn number ranges
      Preferences.kt             # SharedPreferences wrapper (theme, fonts, favorites, ETag)
      Analytics.kt               # Umami event tracking client
    navigation/
      NavGraph.kt                # Screen routing, bottom nav, number pad FAB
    ui/
      theme/
        Color.kt                 # Color palette (light + dark, presentation, favorites)
        Type.kt                  # Typography (Playfair Display + Noto Serif)
        Theme.kt                 # Material 3 color schemes
      screens/
        HymnListScreen.kt        # Branded header, search with highlighting, hymn list
        HymnDetailScreen.kt      # Hymn lyrics with swipe animation, favorites, share
        PresentationScreen.kt    # Full-screen projection with font controls
        CategoriesScreen.kt      # 2-column category grid with icons
        CategoryDetailScreen.kt  # Hymns within a category
        FavoritesScreen.kt       # Favorite hymns with heart toggle
        MoreScreen.kt            # Settings, about, share/rate app
        LoadingScreen.kt         # Branded shimmer skeleton
        ErrorScreen.kt           # Offline error with retry
      components/
        BrandHeader.kt           # Reusable branded header (book icon + title + subtitle)
        SearchBar.kt             # Search input with clear button
        HymnRow.kt               # Hymn list item (number, title, subtitle, search highlight, favorite heart)
        BottomNavBar.kt          # Bottom navigation (Hymns, Categories, Favorites, More)
        NumberPadDialog.kt       # Go-to-hymn keypad with validation
  main/res/
    font/                        # Bundled Playfair Display + Noto Serif TTFs
    drawable/                    # Brand book icon (ic_book_brand)
    mipmap-*/                    # Launcher icons at all densities + monochrome
    values/                      # Colors, strings, themes, splash screen (v31)
  test/java/com/sdahymnal/yoruba/
    data/
      RemoveDiacriticsTest.kt    # Diacritics stripping, punctuation, edge cases
      HymnTest.kt                # JSON parsing: verses, chorus, call-response
      SearchScoringTest.kt       # Search ranking, priority, diacritics tolerance
      SearchEdgeCasesTest.kt     # Edge cases: single char, punctuation, long query
      HymnEdgeCasesTest.kt       # Edge cases: empty lyrics, mixed blocks, unicode
      ETagCachingTest.kt         # ETag logic: store, retrieve, 304 handling
      PreferencesTest.kt         # Font sizes, favorites serialization
    ViewModelLogicTest.kt        # Theme cycling, font size, favorites, deep link priority
  androidTest/java/com/sdahymnal/yoruba/
    TestHelpers.kt               # Shared test hymn factory
    HymnListScreenTest.kt        # List display, count, search, click, brand header
    HymnDetailScreenTest.kt      # Title, English title, number, lyrics, references
    FavoritesScreenTest.kt       # Empty state, list, count visibility, brand header
    CategoriesScreenTest.kt      # Grid display, count, empty filtering, brand header
    MoreScreenTest.kt            # All sections, theme, fonts, favorites, dialog
```

## Architecture

```
MainActivity
  |
  +-- MainViewModel (single source of truth)
  |     |
  |     +-- HymnRepository (network + cache + search)
  |     |     +-- OkHttp with ETag headers
  |     |     +-- Pre-built search index (normalized at load time)
  |     |     +-- File-based JSON cache
  |     |
  |     +-- Preferences (SharedPreferences)
  |     |     +-- Theme, font sizes, favorites, last hymn, ETag
  |     |
  |     +-- Analytics (Umami HTTP client)
  |           +-- Fire-and-forget on IO dispatcher
  |           +-- android_ prefix to distinguish from web events
  |
  +-- NavGraph (Compose Navigation)
        +-- Bottom nav (Hymns, Categories, Favorites, More)
        +-- Number pad FAB
        +-- Animated transitions (fade + slide)
        +-- Screen composables (stateless, receive state via params)
```

## Data Flow

```
App launch
  |
  +-- Load cached hymns from file (instant)
  +-- Build search index (once)
  +-- Send GET with If-None-Match: <stored-etag>
  |
  +-- Server returns 304? --> keep cached data (no download)
  +-- Server returns 200? --> save new JSON + ETag, rebuild index
  +-- Network error + cache exists? --> silently use cache
  +-- Network error + no cache? --> show error screen with retry
```

## Analytics Events

All events are prefixed with `android_` to distinguish from web traffic in the shared Umami dashboard.

| Event | When |
|---|---|
| `android_app_launch` | App opens |
| `android_hymn_42` | Hymn viewed |
| `android_search_oluwa` | Search (1s debounce, max 50 chars) |
| `android_presented_42` | Presentation mode entered |
| `android_share_42` | Hymn shared |
| `android_theme_dark` | Theme toggled |
| `android_category_adoration` | Category tapped |
| `android_favorite_42` | Hymn favorited |
| `android_unfavorite_42` | Hymn unfavorited |
| `android_clear_favorites` | All favorites cleared |
| `android_numpad_42` | Number pad used |
| `android_fontsize_1.2` | Font size changed |
| `android_share_app` | App shared from settings |
| `android_rate_app` | Rate app tapped |

## Testing

**Unit tests** (63 tests): `app/src/test` - run via right-click > Run Tests

**UI tests** (28 tests): `app/src/androidTest` - requires emulator or device

| Category | Unit | UI | Total |
|---|---|---|---|
| Search & normalization | 16 | - | 16 |
| Data parsing | 13 | - | 13 |
| Preferences & ViewModel | 18 | - | 18 |
| ETag caching | 6 | - | 6 |
| Hymn List Screen | - | 5 | 5 |
| Hymn Detail Screen | - | 5 | 5 |
| Favorites Screen | - | 5 | 5 |
| Categories Screen | - | 4 | 4 |
| More Screen | - | 9 | 9 |
| **Total** | **53** | **28** | **81** |

## Related

- **Web app**: [github.com/fisayoafolayan/sdahymnalyorubaweb](https://github.com/fisayoafolayan/sdahymnalyorubaweb)
- **Live site**: [sdahymnalyoruba.com](https://sdahymnalyoruba.com)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
