package net.munipramansagar.ott.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.data.repository.VideoRepository
import javax.inject.Inject

data class PlayerUiState(
    val isLoading: Boolean = true,
    val video: Video? = null,
    val relatedVideos: List<Video> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val videoId: String = savedStateHandle["videoId"] ?: ""
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        loadVideo()
    }

    private fun loadVideo() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val video = videoRepository.getVideoById(videoId)
                if (video != null) {
                    val related = videoRepository.getRelatedVideos(video)
                    _uiState.value = PlayerUiState(
                        isLoading = false,
                        video = video,
                        relatedVideos = related
                    )
                } else {
                    _uiState.value = PlayerUiState(
                        isLoading = false,
                        error = "Video not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = PlayerUiState(
                    isLoading = false,
                    error = e.message ?: "Failed to load video"
                )
            }
        }
    }
}
