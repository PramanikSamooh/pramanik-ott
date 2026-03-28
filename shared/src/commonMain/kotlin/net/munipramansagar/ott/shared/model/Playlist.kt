package net.munipramansagar.ott.shared.model

data class PlaylistData(
    val id: String = "",
    val title: String = "",
    val titleHi: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val section: String = "",
    val displayOrder: Int = 0,
    val pinned: Boolean = false,
    val visible: Boolean = true,
    val videoCount: Int = 0,
    val publishedAt: String = ""
) {
    fun getTitle(isHindi: Boolean): String =
        if (isHindi && titleHi.isNotBlank()) titleHi else title

    val isMonthly: Boolean
        get() = title.matches(Regex("^\\d{4}-\\d{2}$"))
}
