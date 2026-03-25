package net.munipramansagar.ott.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class LiveStatus(
    @get:PropertyName("isLive") @set:PropertyName("isLive")
    var live: Boolean = false,
    val currentVideoId: String = "",
    val activeStreams: List<LiveStream> = emptyList(),
    val upcomingVideos: List<String> = emptyList()
) {
    // Convenience getter that matches old code
    val isLive: Boolean get() = live
}

@IgnoreExtraProperties
data class LiveStream(
    val videoId: String = "",
    val channelKey: String = "",
    val channelName: String = "",
    val title: String = ""
)
