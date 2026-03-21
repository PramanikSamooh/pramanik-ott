package net.munipramansagar.ott.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.munipramansagar.ott.data.model.Playlist
import net.munipramansagar.ott.data.model.Section
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.data.repository.VideoRepository
import javax.inject.Inject

data class CategoryUiState(
    val isLoading: Boolean = true,
    val section: Section? = null,
    val playlists: List<PlaylistWithVideos> = emptyList(),
    // For playlist detail view
    val selectedPlaylist: Playlist? = null,
    val playlistVideos: List<Video> = emptyList(),
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sectionId: String = savedStateHandle["sectionId"] ?: savedStateHandle["categorySlug"] ?: ""
    private val playlistId: String? = savedStateHandle["playlistId"]

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        if (playlistId != null) {
            loadPlaylistVideos(playlistId)
        } else {
            loadSectionPlaylists()
        }
    }

    private fun loadSectionPlaylists() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                // Fetch playlists for this section
                val playlists = videoRepository.getPlaylistsBySection(sectionId)

                // For each playlist, fetch preview videos (first 6)
                val playlistsWithVideos = playlists.map { playlist ->
                    async {
                        val videos = videoRepository.getPlaylistVideos(playlist.id, limit = 6)
                        PlaylistWithVideos(playlist, videos)
                    }
                }.awaitAll()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    playlists = playlistsWithVideos
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load playlists"
                )
            }
        }
    }

    private fun loadPlaylistVideos(pId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val playlist = videoRepository.getPlaylistById(pId)
                val videos = videoRepository.getPlaylistVideos(pId, limit = 24)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedPlaylist = playlist,
                    playlistVideos = videos,
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
        val pId = playlistId ?: return

        viewModelScope.launch {
            try {
                _uiState.value = state.copy(isLoadingMore = true)
                val lastVideo = state.playlistVideos.lastOrNull()
                val moreVideos = videoRepository.getPlaylistVideos(
                    pId,
                    limit = 24,
                    lastPublishedAt = lastVideo?.publishedAt
                )
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    playlistVideos = state.playlistVideos + moreVideos,
                    hasMore = moreVideos.size >= 24
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingMore = false)
            }
        }
    }
}
