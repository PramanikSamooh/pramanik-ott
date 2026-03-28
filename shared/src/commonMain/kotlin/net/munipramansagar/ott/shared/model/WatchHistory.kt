package net.munipramansagar.ott.shared.model

data class WatchHistoryData(
    val videoId: String,
    val title: String = "",
    val titleHi: String = "",
    val thumbnailUrl: String = "",
    val channelName: String = "",
    val durationFormatted: String = "",
    val playlistId: String = "",
    val playlistTitle: String = "",
    val sectionId: String = "",
    val resumePositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val completed: Boolean = false,
    val lastWatchedAt: Long = 0L,
    val playlistIndex: Int = -1,
    val bookmarked: Boolean = false,
    val bookmarkedAt: Long = 0L
)
