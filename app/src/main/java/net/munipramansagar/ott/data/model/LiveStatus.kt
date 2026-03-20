package net.munipramansagar.ott.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class LiveStatus(
    val isLive: Boolean = false,
    val currentVideoId: String = "",
    val upcomingVideos: List<String> = emptyList()
)
