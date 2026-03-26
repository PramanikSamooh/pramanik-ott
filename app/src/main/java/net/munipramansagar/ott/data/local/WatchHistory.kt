package net.munipramansagar.ott.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntry(
    @PrimaryKey
    val videoId: String,
    val title: String = "",
    val titleHi: String = "",
    val thumbnailUrl: String = "",
    val channelName: String = "",
    val durationFormatted: String = "",
    val playlistId: String = "",
    val playlistTitle: String = "",
    val sectionId: String = "",
    // Resume position in milliseconds
    val resumePositionMs: Long = 0L,
    // Total duration in milliseconds (0 if unknown)
    val totalDurationMs: Long = 0L,
    // Whether the video was completed (watched > 90%)
    val completed: Boolean = false,
    // Timestamp of last watch
    val lastWatchedAt: Long = System.currentTimeMillis(),
    // For auto-next: index within playlist
    val playlistIndex: Int = -1,
    // User bookmarked/saved this video
    val bookmarked: Boolean = false,
    val bookmarkedAt: Long = 0L
)
