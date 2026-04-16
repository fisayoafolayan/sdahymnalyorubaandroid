# Contributing to SDA Hymnal Yoruba Android

Thank you for your interest in contributing! This guide will help you get started.

## Getting Started

1. **Fork** the repository
2. **Clone** your fork locally
3. Open the project in **Android Studio**
4. Let Gradle sync and resolve dependencies
5. Run the app on an emulator or device to verify everything works
6. Run the unit tests: right-click `app/src/test` > **Run Tests**
7. Run the UI tests: right-click `app/src/androidTest` > **Run Tests** (requires emulator)

## Development Setup

- Android Studio Hedgehog (2023.1) or later
- JDK 17
- Android SDK 35
- A device or emulator running Android 8.0+ (API 26+)

## Branch Naming

- `feature/short-description` - new features
- `fix/short-description` - bug fixes
- `refactor/short-description` - code improvements
- `docs/short-description` - documentation changes

## Making Changes

1. Create a branch from `main`
2. Make your changes in small, focused commits
3. Test on both **light and dark** themes
4. Test in both **portrait and landscape** orientations
5. Run all tests (unit + UI) - all must pass
6. Ensure the app builds without warnings

### Code Style

- Follow standard **Kotlin coding conventions**
- Use **Compose best practices** - stateless composables, state hoisting
- Keep business logic in `MainViewModel`, not in composables
- Use `MaterialTheme` colors and typography - avoid hardcoded values
- All colors must be defined in `Color.kt` - never inline `Color(0x...)`
- All user-facing strings must be in `strings.xml` - never hardcode text in composables
- Match the web app's design language (colors, spacing, fonts)
- Pre-compile regexes as companion object vals, not inline
- No unused imports - clean up before committing
- Serif fonts (Noto Serif/Playfair Display) for content, system sans-serif for UI chrome (bottom nav)

### Analytics

All user interactions should be tracked through the **ViewModel** - never call `Analytics` directly from composables. The ViewModel provides typed methods for common events and a generic `trackEvent()` for others:

```kotlin
// In ViewModel
fun trackShare(hymnNumber: Int) {
    Analytics.trackEvent("share_$hymnNumber")
}

// In NavGraph / composable callback
onShare = { viewModel.trackShare(hymn.number) }
```

Page views are tracked automatically via `viewModel.trackPageView()` in the `LaunchedEffect(currentRoute)` block in NavGraph. Events are prefixed with `android_` automatically.

### Commit Messages

Use clear, descriptive messages:

```
feat: add bookmark functionality
fix: search not matching diacritics correctly
refactor: extract hymn row into separate component
docs: update setup instructions
```

## Architecture Overview

```
MainActivity               - Entry point, edge-to-edge, deep link handling
  |
MainViewModel              - Single source of truth for all app state
  |
  +-- HymnRepository       - Network (shared OkHttpClient + ETag), atomic file cache, search engine
  |     +-- Search index built once at load time (normalized fields)
  |     +-- Number lookup map for O(1) getByNumber()
  |     +-- Scoring: number=100, prefix=90, title=80, english=70, refs=60, lyrics=40
  |
  +-- Preferences           - SharedPreferences (single instance, shared with Repository)
  +-- Analytics             - Umami HTTP client (configurable via BuildConfig, JSON-safe payloads)
  |
NavGraph                   - Routes screens, bottom nav, number pad FAB, page view tracking
  |
  +-- HymnListScreen        - Branded header, debounced search with highlighting, empty state
  +-- HymnDetailScreen      - Lyrics with swipe feedback, top bar favorite, share
  +-- PresentationScreen    - Full-screen projection with staggered line reveal, font controls
  +-- CategoriesScreen      - 2-column searchable grid with count labels
  +-- CategoryDetailScreen  - Hymns within a category with count
  +-- FavoritesScreen       - Saved hymns with undo snackbar on removal
  +-- MoreScreen            - Settings with theme dropdown, font preview, about, share/rate
  +-- LoadingScreen         - Shimmer skeleton
  +-- ErrorScreen           - Offline error with retry
```

**Key patterns:**
- State flows down from `MainViewModel` via `StateFlow` + `collectAsState()`
- Events flow up via callback lambdas (e.g., `onHymnClick`, `onToggleFavorite`)
- No direct `Preferences`, `Repository`, or `Analytics` access from composables
- All analytics routed through ViewModel methods
- Search uses 150ms debounce via `Flow.debounce()` combined with `repository.state`
- Network uses shared `HttpClient.base` with ETag headers
- Atomic cache writes (temp file + rename) protect offline access
- Tab state derived from navigation via `LaunchedEffect(currentRoute)`, not composition side effects
- Deep links handled reactively via `pendingDeepLink` StateFlow
- `BrandHeader` component shared across Hymns, Favorites, Categories, More screens
- `FavoriteHeart` color defined once in `Color.kt`, referenced everywhere

## Testing

### Running Tests

- **Unit tests:** right-click `app/src/test` > **Run Tests**
- **UI tests:** right-click `app/src/androidTest` > **Run Tests** (requires emulator or device)
- **CI:** unit tests and instrumented tests run on every push/PR via GitHub Actions

### Test Coverage

| File | Type | Tests | Coverage |
|---|---|---|---|
| `RemoveDiacriticsTest.kt` | Unit | 6 | Diacritics, punctuation, casing, edge cases |
| `HymnTest.kt` | Unit | 5 | JSON parsing: verses, chorus, call-response |
| `SearchScoringTest.kt` | Unit | 11 | Ranking, priority, diacritics, call-response, sorting |
| `SearchEdgeCasesTest.kt` | Unit | 10 | Single char, spaces, punctuation, long query |
| `HymnEdgeCasesTest.kt` | Unit | 8 | Empty lyrics, mixed blocks, unicode, sorting |
| `ETagCachingTest.kt` | Unit | 10 | Store, retrieve, 304, null handling, atomic writes |
| `PreferencesTest.kt` | Unit | 4 | Font sizes, favorites serialization |
| `ViewModelLogicTest.kt` | Unit | 20 | Flow patterns, deep links, search combine, hymn lookup |
| `HymnListScreenTest.kt` | UI | 5 | Display, count, search, click, header |
| `HymnDetailScreenTest.kt` | UI | 5 | Title, English, number, lyrics, references |
| `FavoritesScreenTest.kt` | UI | 5 | Empty state, list, count, header |
| `CategoriesScreenTest.kt` | UI | 4 | Grid, count, header, empty filtering |
| `MoreScreenTest.kt` | UI | 9 | Sections, theme, fonts, favorites, dialog |

### Writing New Tests

**Unit tests:**
- Place in `app/src/test/java/com/sdahymnal/yoruba/`
- Use JUnit 4 (`@Test`, `assertEquals`)
- Focus on business logic (search, data parsing, preferences)
- For flow-based patterns, use `kotlinx-coroutines-test` (`runTest`, `MutableStateFlow`)

**UI tests:**
- Place in `app/src/androidTest/java/com/sdahymnal/yoruba/`
- Use Compose testing (`createComposeRule`, `onNodeWithText`, `performClick`)
- Use `makeTestHymn()` from `TestHelpers.kt` to create test data
- Each screen should have its own test file

## Areas for Contribution

### Good First Issues

- Add **recently viewed** hymns list
- Add **search history** with recent queries
- Add unit tests for `HymnCategories` data
- Add UI tests for `PresentationScreen` and `NumberPadDialog`

### Feature Ideas

- Hymn of the day / daily reading
- Export favorites as a shareable list
- Adjustable presentation themes (different stage colors)
- Home screen widget
- Wear OS companion

### Hymn Data

Hymn content is managed through the web app at [sdahymnalyoruba.com](https://sdahymnalyoruba.com). To contribute hymn corrections or additions, please open an issue on the [web repository](https://github.com/fisayoafolayan/sdahymnalyorubaweb).

## Pull Requests

1. Push your branch to your fork
2. Open a PR against `main`
3. Describe **what** you changed and **why**
4. Include screenshots for UI changes (both light and dark mode)
5. Confirm all tests pass (unit + UI)
6. Keep PRs focused - one feature or fix per PR

### PR Checklist

- [ ] Builds without warnings
- [ ] All unit tests pass
- [ ] All UI tests pass
- [ ] Tested in light and dark mode
- [ ] Tested in portrait and landscape
- [ ] No unused imports or dead code
- [ ] No hardcoded colors (use Color.kt)
- [ ] No hardcoded strings (use strings.xml)
- [ ] Analytics tracking added for new user actions (through ViewModel)
- [ ] Content descriptions on functional icons
- [ ] Touch targets at least 48dp
- [ ] Follows existing code patterns

## Reporting Bugs

Open an issue with:
- Device model and Android version
- Steps to reproduce
- Expected vs actual behavior
- Screenshots if applicable

## Questions?

Open an issue or reach out at support@sdahymnalyoruba.com.
