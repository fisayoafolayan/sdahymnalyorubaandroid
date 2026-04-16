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
- Match the web app's design language (colors, spacing, fonts)
- Pre-compile regexes as companion object vals, not inline
- No unused imports - clean up before committing
- Serif fonts (Noto Serif/Playfair Display) for content, system sans-serif for UI chrome (bottom nav)

### Analytics

All user interactions should be tracked via `Analytics.trackEvent()`. Events are prefixed with `android_` automatically. Add tracking for any new user action:

```kotlin
Analytics.trackEvent("feature_name")  // becomes "android_feature_name" in Umami
```

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
MainActivity               - Entry point, edge-to-edge, ViewModel creation
  |
MainViewModel              - Single source of truth for all app state
  |
  +-- HymnRepository       - Network (OkHttp + ETag), file cache, search engine
  |     +-- Search index built once at load time (normalized fields)
  |     +-- Scoring: number=100, prefix=90, title=80, english=70, refs=60, lyrics=40
  |
  +-- Preferences           - SharedPreferences (theme, fonts, favorites, ETag)
  +-- Analytics             - Umami HTTP client (fire-and-forget, IO dispatcher)
  |
NavGraph                   - Routes screens, bottom nav, number pad FAB, animated transitions
  |
  +-- HymnListScreen        - Branded header, debounced search with highlighting, animated list
  +-- HymnDetailScreen      - Lyrics with swipe animation, favorites, share
  +-- PresentationScreen    - Full-screen projection, keeps screen on
  +-- CategoriesScreen      - 2-column grid with icons
  +-- CategoryDetailScreen  - Hymns within a category
  +-- FavoritesScreen       - Saved hymns with heart toggle
  +-- MoreScreen            - Settings, about, share/rate
  +-- LoadingScreen         - Shimmer skeleton
  +-- ErrorScreen           - Offline error with retry
```

**Key patterns:**
- State flows down from `MainViewModel` via `StateFlow` + `collectAsState()`
- Events flow up via callback lambdas (e.g., `onHymnClick`, `onToggleFavorite`)
- No direct `Preferences`, `Repository`, or `Analytics` access from composables (except share click in HymnDetailScreen)
- Search uses 150ms debounce via `Flow.debounce()` in ViewModel
- Network uses ETag headers - `If-None-Match` returns 304 when data unchanged
- Configuration changes (rotation) handled without activity recreation via manifest `configChanges`
- `BrandHeader` component shared across Hymns, Favorites, Categories, More screens
- `FavoriteHeart` color defined once in `Color.kt`, referenced everywhere

## Testing

### Running Tests

- **Unit tests:** right-click `app/src/test` > **Run Tests**
- **UI tests:** right-click `app/src/androidTest` > **Run Tests** (requires emulator or device)

### Test Coverage

| File | Type | Tests | Coverage |
|---|---|---|---|
| `RemoveDiacriticsTest.kt` | Unit | 6 | Diacritics, punctuation, casing, edge cases |
| `HymnTest.kt` | Unit | 5 | JSON parsing: verses, chorus, call-response |
| `SearchScoringTest.kt` | Unit | 10 | Ranking, priority, diacritics, sorting |
| `SearchEdgeCasesTest.kt` | Unit | 10 | Single char, spaces, punctuation, long query |
| `HymnEdgeCasesTest.kt` | Unit | 8 | Empty lyrics, mixed blocks, unicode, sorting |
| `ETagCachingTest.kt` | Unit | 6 | Store, retrieve, 304, null handling |
| `PreferencesTest.kt` | Unit | 4 | Font sizes, favorites serialization |
| `ViewModelLogicTest.kt` | Unit | 14 | Theme, fonts, favorites, deep link priority |
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

**UI tests:**
- Place in `app/src/androidTest/java/com/sdahymnal/yoruba/`
- Use Compose testing (`createComposeRule`, `onNodeWithText`, `performClick`)
- Use `makeTestHymn()` from `TestHelpers.kt` to create test data
- Each screen should have its own test file

## Areas for Contribution

### Good First Issues

- Implement the **More** tab About section with app logo and description
- Add **recently viewed** hymns list
- Improve **number pad** with hymn title preview as you type
- Add unit tests for `HymnCategories` data

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
- [ ] Analytics tracking added for new user actions
- [ ] Follows existing code patterns

## Reporting Bugs

Open an issue with:
- Device model and Android version
- Steps to reproduce
- Expected vs actual behavior
- Screenshots if applicable

## Questions?

Open an issue or reach out at support@sdahymnalyoruba.com.
