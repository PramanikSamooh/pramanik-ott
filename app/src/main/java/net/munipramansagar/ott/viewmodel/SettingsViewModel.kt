package net.munipramansagar.ott.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.munipramansagar.ott.data.repository.WatchHistoryRepository
import net.munipramansagar.ott.util.LanguageManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val languageManager: LanguageManager,
    private val firebaseAuth: FirebaseAuth,
    private val watchHistoryRepository: WatchHistoryRepository
) : ViewModel() {

    val language: StateFlow<String> = languageManager.language

    private val _userEmail = MutableStateFlow<String?>(firebaseAuth.currentUser?.email)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _isSignedIn = MutableStateFlow(firebaseAuth.currentUser != null)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        _isSignedIn.value = auth.currentUser != null
        _userEmail.value = auth.currentUser?.email
    }

    init {
        firebaseAuth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeAuthStateListener(authListener)
    }

    fun setLanguage(lang: String) {
        languageManager.setLanguage(lang)
    }

    fun isHindi(): Boolean = languageManager.isHindi()

    fun onSignInSuccess() {
        viewModelScope.launch {
            watchHistoryRepository.syncFromCloud()
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            watchHistoryRepository.clearHistory()
        }
    }
}
