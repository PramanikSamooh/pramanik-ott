package net.munipramansagar.ott.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LanguageManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "pramanik_prefs"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_THEME = "theme_mode"
        const val HINDI = "hi"
        const val ENGLISH = "en"
        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"
        const val THEME_SYSTEM = "system"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, HINDI) ?: HINDI)
    val language: StateFlow<String> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME, THEME_DARK) ?: THEME_DARK)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _language.value = lang
    }

    fun setTheme(mode: String) {
        prefs.edit().putString(KEY_THEME, mode).apply()
        _themeMode.value = mode
    }

    fun isHindi(): Boolean = _language.value == HINDI
}
