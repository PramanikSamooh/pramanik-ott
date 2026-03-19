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
        const val HINDI = "hi"
        const val ENGLISH = "en"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, HINDI) ?: HINDI)
    val language: StateFlow<String> = _language.asStateFlow()

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _language.value = lang
    }

    fun isHindi(): Boolean = _language.value == HINDI
}
