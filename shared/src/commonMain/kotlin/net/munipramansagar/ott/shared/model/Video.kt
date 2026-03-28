package net.munipramansagar.ott.shared.model

data class VideoData(
    val id: String = "",
    val title: String = "",
    val titleHi: String = "",
    val thumbnailUrl: String = "",
    val thumbnailUrlHQ: String = "",
    val channelName: String = "",
    val publishedAt: String = "",
    val durationFormatted: String = "",
    val durationSec: Int = 0,
    val viewCountFormatted: String = "",
    val playlistId: String = "",
    val playlistTitle: String = "",
    val sectionId: String = "",
    val isShort: Boolean = false,
    val isLive: Boolean = false
) {
    fun getTitle(isHindi: Boolean): String =
        if (isHindi && titleHi.isNotBlank()) titleHi else title
}
