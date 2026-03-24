package net.munipramansagar.ott.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.munipramansagar.ott.data.local.WatchHistoryEntry
import net.munipramansagar.ott.data.repository.WatchHistoryRepository
import javax.inject.Inject

@HiltViewModel
class WatchHistoryViewModel @Inject constructor(
    private val watchHistoryRepository: WatchHistoryRepository
) : ViewModel() {

    val watchHistory = watchHistoryRepository.getRecentlyWatched(limit = 500)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearHistory() {
        viewModelScope.launch {
            watchHistoryRepository.clearHistory()
        }
    }
}
