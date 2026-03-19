package net.munipramansagar.ott.data.model

data class LiveStatus(
    val isLive: Boolean = false,
    val currentVideoId: String = "",
    val upcomingVideos: List<String> = emptyList()
)
