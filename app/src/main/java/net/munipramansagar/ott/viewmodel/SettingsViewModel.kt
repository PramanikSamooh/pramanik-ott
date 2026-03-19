package net.munipramansagar.ott.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import net.munipramansagar.ott.util.LanguageManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val languageManager: LanguageManager
) : ViewModel() {

    val language: StateFlow<String> = languageManager.language

    fun setLanguage(lang: String) {
        languageManager.setLanguage(lang)
    }

    fun isHindi(): Boolean = languageManager.isHindi()
}
