package com.sdahymnal.yoruba.data

import android.content.Context
import android.content.SharedPreferences

class Preferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sda_hymnal", Context.MODE_PRIVATE)

    /** Theme mode: "light", "dark", or "system" */
    var themeMode: String
        get() = prefs.getString(KEY_THEME, "system") ?: "system"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    /** Reading font size multiplier: 1.0, 1.2, or 1.45 */
    var readingFontSize: Float
        get() = prefs.getFloat(KEY_READ_FZ, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_READ_FZ, value).apply()

    /** Presentation font size multiplier: 0.4 - 2.5 */
    var presentationFontSize: Float
        get() = prefs.getFloat(KEY_PRES_FZ, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_PRES_FZ, value).apply()

    /** Last viewed hymn number, -1 if none */
    var lastHymn: Int
        get() = prefs.getInt(KEY_LAST_HYMN, -1)
        set(value) = prefs.edit().putInt(KEY_LAST_HYMN, value).apply()

    /** Favorite hymn numbers stored as comma-separated string */
    var favorites: Set<Int>
        get() {
            val raw = prefs.getString(KEY_FAVORITES, "") ?: ""
            if (raw.isBlank()) return emptySet()
            return raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        }
        set(value) = prefs.edit().putString(KEY_FAVORITES, value.joinToString(",")).apply()

    fun toggleFavorite(hymnNumber: Int): Set<Int> {
        val current = favorites.toMutableSet()
        if (current.contains(hymnNumber)) current.remove(hymnNumber) else current.add(hymnNumber)
        favorites = current
        return current
    }

    /** Stored ETag from the hymns.json server response */
    var hymnsEtag: String?
        get() = prefs.getString(KEY_ETAG, null)
        set(value) = prefs.edit().putString(KEY_ETAG, value).apply()

    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_READ_FZ = "readFz"
        private const val KEY_PRES_FZ = "presFz"
        private const val KEY_LAST_HYMN = "lastHymn"
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_ETAG = "hymns_etag"

        val READING_SIZES = floatArrayOf(1.0f, 1.2f, 1.45f)
    }
}
