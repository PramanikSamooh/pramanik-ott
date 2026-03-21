package net.munipramansagar.ott.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.munipramansagar.ott.data.model.Video
import net.munipramansagar.ott.data.repository.ShortsRepository
import net.munipramansagar.ott.player.DownloaderImpl
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo
import javax.inject.Inject

data class ShortsUiState(
    val isLoading: Boolean = true,
    val shorts: List<Video> = emptyList(),
    val currentIndex: Int = 0,
    val error: String? = null
)

@HiltViewModel
class ShortsViewModel @Inject constructor(
    private val shortsRepository: ShortsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShortsUiState())
    val uiState: StateFlow<ShortsUiState> = _uiState.asStateFlow()

    // Cache of extracted stream URLs: videoId -> streamUrl
    private val streamUrlCache = mutableMapOf<String, String>()

    private val _streamUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val streamUrls: StateFlow<Map<String, String>> = _streamUrls.asStateFlow()

    init {
        loadShorts()
    }

    private fun loadShorts() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val shorts = shortsRepository.getShorts(limit = 30)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    shorts = shorts
                )
                // Pre-extract first two videos
                if (shorts.isNotEmpty()) {
                    extractStreamUrl(shorts[0].id)
                    if (shorts.size > 1) {
                        extractStreamUrl(shorts[1].id)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load shorts"
                )
            }
        }
    }

    fun onPageChanged(index: Int) {
        _uiState.value = _uiState.value.copy(currentIndex = index)
        val shorts = _uiState.value.shorts

        // Preload next video stream URL
        val nextIndex = index + 1
        if (nextIndex < shorts.size) {
            extractStreamUrl(shorts[nextIndex].id)
        }
    }

    fun extractStreamUrl(videoId: String) {
        if (streamUrlCache.containsKey(videoId)) return

        viewModelScope.launch {
            try {
                val url = withContext(Dispatchers.IO) {
                    try {
                        NewPipe.init(DownloaderImpl.getInstance())
                    } catch (_: Exception) { }

                    val videoUrl = "https://www.youtube.com/watch?v=$videoId"
                    val info = StreamInfo.getInfo(ServiceList.YouTube, videoUrl)

                    // For shorts, prefer video streams with audio
                    val videoStreams = info.videoStreams
                        .filter { !it.isVideoOnly }
                        .sortedByDescending {
                            it.resolution?.replace("p", "")?.toIntOrNull() ?: 0
                        }

                    val bestStream = videoStreams.firstOrNull {
                        val res = it.resolution?.replace("p", "")?.toIntOrNull() ?: 0
                        res in 360..720
                    } ?: videoStreams.firstOrNull()

                    bestStream?.content ?: info.hlsUrl ?: ""
                }

                if (url.isNotEmpty()) {
                    streamUrlCache[videoId] = url
                    _streamUrls.value = streamUrlCache.toMap()
                }
            } catch (e: Exception) {
                // Stream extraction failed - will retry when page is visited
                android.util.Log.e("ShortsViewModel", "Failed to extract stream for $videoId", e)
            }
        }
    }

    fun refresh() {
        streamUrlCache.clear()
        _streamUrls.value = emptyMap()
        loadShorts()
    }
}
