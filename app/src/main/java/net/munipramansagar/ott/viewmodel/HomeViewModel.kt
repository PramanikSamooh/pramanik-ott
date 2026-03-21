package net.munipramansagar.ott.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.munipramansagar.ott.data.model.Announcement
import net.munipramansagar.ott.data.model.LiveStatus
import net.munipramansagar.ott.data.model.Playlist
import net.munipramansagar.ott.data.model.Section
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.data.repository.LiveRepository
import net.munipramansagar.ott.data.repository.VideoRepository
import javax.inject.Inject

data class PlaylistWithVideos(
    val playlist: Playlist,
    val videos: List<Video>
)

data class HomeSectionData(
    val section: Section,
    val playlists: List<PlaylistWithVideos>
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val sections: List<HomeSectionData> = emptyList(),
    val heroBannerVideos: List<Video> = emptyList(),
    val announcements: List<Announcement> = emptyList(),
    val liveStatus: LiveStatus = LiveStatus(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val liveRepository: LiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
        observeLiveStatus()
    }

    private fun loadHome() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                // Fetch sections and announcements in parallel
                val sectionsDeferred = async { videoRepository.getSections() }
                val announcementsDeferred = async {
                    try { videoRepository.getActiveAnnouncements() } catch (_: Exception) { emptyList() }
                }

                val sections = sectionsDeferred.await()
                val announcements = announcementsDeferred.await()

                // For each section, fetch playlists (limit 5), then for each playlist fetch 10 videos
                val sectionDataList = sections.map { section ->
                    async {
                        try {
                            val playlists = videoRepository.getPlaylistsBySection(section.id, limit = 5)
                            val playlistsWithVideos = playlists.map { playlist ->
                                async {
                                    try {
                                        val videos = videoRepository.getPlaylistVideos(playlist.id, limit = 10)
                                        PlaylistWithVideos(playlist, videos)
                                    } catch (_: Exception) {
                                        PlaylistWithVideos(playlist, emptyList())
                                    }
                                }
                            }.awaitAll()
                            HomeSectionData(section, playlistsWithVideos.filter { it.videos.isNotEmpty() })
                        } catch (_: Exception) {
                            HomeSectionData(section, emptyList())
                        }
                    }
                }.awaitAll().filter { it.playlists.isNotEmpty() }

                // Hero banner = first 5 videos from the first section's first playlist
                val heroBanner = sectionDataList
                    .firstOrNull()
                    ?.playlists
                    ?.firstOrNull()
                    ?.videos
                    ?.take(5)
                    ?: emptyList()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    sections = sectionDataList,
                    heroBannerVideos = heroBanner,
                    announcements = announcements
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load content"
                )
            }
        }
    }

    private fun observeLiveStatus() {
        viewModelScope.launch {
            liveRepository.observeLiveStatus().collect { status ->
                _uiState.value = _uiState.value.copy(liveStatus = status)
            }
        }
    }

    fun refresh() {
        loadHome()
    }
}
