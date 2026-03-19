package net.munipramansagar.ott.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.munipramansagar.ott.data.model.Category
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.data.repository.VideoRepository
import javax.inject.Inject

data class CategoryUiState(
    val isLoading: Boolean = true,
    val videos: List<Video> = emptyList(),
    val category: Category? = null,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categorySlug: String = savedStateHandle["categorySlug"] ?: ""
    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(category = Category.fromSlug(categorySlug))
        loadVideos()
    }

    private fun loadVideos() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val videos = videoRepository.getVideosByCategory(categorySlug, limit = 24)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    videos = videos,
                    hasMore = videos.size >= 24
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load videos"
                )
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return

        viewModelScope.launch {
            try {
                _uiState.value = state.copy(isLoadingMore = true)
                val lastVideo = state.videos.lastOrNull()
                val moreVideos = videoRepository.getVideosByCategory(
                    categorySlug,
                    limit = 24,
                    lastPublishedAt = lastVideo?.publishedAt
                )
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    videos = state.videos + moreVideos,
                    hasMore = moreVideos.size >= 24
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingMore = false)
            }
        }
    }
}
