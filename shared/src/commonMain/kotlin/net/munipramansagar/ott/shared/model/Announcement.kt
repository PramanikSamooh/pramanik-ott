package net.munipramansagar.ott.shared.model

data class AnnouncementData(
    val id: String = "",
    val title: String = "",
    val titleHi: String = "",
    val message: String = "",
    val messageHi: String = "",
    val imageUrl: String = "",
    val actionUrl: String = "",
    val active: Boolean = true,
    val priority: Int = 0,
    val createdAt: String = ""
) {
    fun getTitle(isHindi: Boolean): String =
        if (isHindi && titleHi.isNotBlank()) titleHi else title

    fun getMessage(isHindi: Boolean): String =
        if (isHindi && messageHi.isNotBlank()) messageHi else message
}
