package net.munipramansagar.ott.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.munipramansagar.ott.data.model.Category
import net.munipramansagar.ott.data.model.LiveStatus
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.data.repository.ChannelRepository
import net.munipramansagar.ott.data.repository.LiveRepository
import net.munipramansagar.ott.data.repository.VideoRepository
import javax.inject.Inject

data class HomeRowData(
    val label: String,
    val labelHi: String,
    val categorySlug: String,
    val videos: List<Video>
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val heroBannerVideos: List<Video> = emptyList(),
    val rows: List<HomeRowData> = emptyList(),
    val liveStatus: LiveStatus = LiveStatus(),
    val liveVideos: List<Video> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val channelRepository: ChannelRepository,
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

                // Load all category rows in parallel
                val categoriesWithLabels = listOf(
                    Triple(Category.DISCOURSE, "Latest Discourses", "नवीनतम प्रवचन"),
                    Triple(Category.BHAWNA_YOG, "Bhawna Yog", "भावना योग"),
                    Triple(Category.SHANKA_CLIPS, "Shanka Samadhan", "शंका समाधान"),
                    Triple(Category.SWADHYAY, "Agam Swadhyay", "आगम स्वाध्याय"),
                    Triple(Category.SHANKA_FULL, "Shanka Samadhan (Full)", "शंका समाधान (पूर्ण)"),
                    Triple(Category.KIDS, "Jain Pathshala", "जैन पाठशाला")
                )

                val deferredRows = categoriesWithLabels.map { (cat, label, labelHi) ->
                    async {
                        val videos = videoRepository.getVideosByCategory(cat.slug, limit = 12)
                        HomeRowData(label, labelHi, cat.slug, videos)
                    }
                }

                val rows = deferredRows.map { it.await() }.filter { it.videos.isNotEmpty() }

                // Hero banner = first 5 discourse videos
                val heroBanner = rows.firstOrNull()?.videos?.take(5) ?: emptyList()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    heroBannerVideos = heroBanner,
                    rows = rows
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
