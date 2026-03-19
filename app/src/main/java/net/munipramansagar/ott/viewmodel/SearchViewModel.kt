package net.munipramansagar.ott.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.data.repository.SearchRepository
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<Video> = emptyList(),
    val hasSearched: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)

        searchJob?.cancel()
        if (query.length < 2) {
            _uiState.value = _uiState.value.copy(
                results = emptyList(),
                hasSearched = false
            )
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            try {
                _uiState.value = _uiState.value.copy(isSearching = true, error = null)
                val results = searchRepository.search(query)
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    results = results,
                    hasSearched = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    error = e.message,
                    hasSearched = true
                )
            }
        }
    }
}
