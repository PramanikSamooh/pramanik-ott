package net.munipramansagar.ott.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Video(
    val id: String = "",
    val title: String = "",
    val titleHi: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val thumbnailUrlHQ: String = "",
    val channelKey: String = "",
    val channelName: String = "",
    val categorySlug: String = "",
    val playlistId: String = "",
    val playlistTitle: String = "",
    val publishedAt: String = "",
    val duration: String = "",
    val durationFormatted: String = "",
    val viewCount: Long = 0,
    val viewCountFormatted: String = "",
    val isLive: Boolean = false,
    val isUpcoming: Boolean = false,
    val isShort: Boolean = false,
    val durationSec: Long = 0,
    val tags: List<String> = emptyList(),
    val youtubeUrl: String = ""
)
