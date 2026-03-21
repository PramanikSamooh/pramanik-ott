package net.munipramansagar.ott.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class LiveStatus(
    val isLive: Boolean = false,
    val currentVideoId: String = "",
    val activeStreams: List<LiveStream> = emptyList(),
    val upcomingVideos: List<String> = emptyList()
)

@IgnoreExtraProperties
data class LiveStream(
    val videoId: String = "",
    val channelKey: String = "",
    val channelName: String = "",
    val title: String = ""
)
