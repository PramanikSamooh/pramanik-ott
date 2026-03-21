package net.munipramansagar.ott.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Announcement(
    val id: String = "",
    val title: String = "",
    val titleHi: String = "",
    val type: String = "",        // event, quote, location, notification
    val imageUrl: String = "",
    val actionUrl: String = "",
    val priority: Int = 0,
    val active: Boolean = true,
    val startDate: String = "",
    val endDate: String = ""
) {
    fun getTitle(isHindi: Boolean): String = if (isHindi && titleHi.isNotBlank()) titleHi else title
}
