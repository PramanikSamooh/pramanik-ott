package net.munipramansagar.ott.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Announcement(
    val id: String = "",
    val title: String = "",
    val titleHi: String = "",
    val body: String = "",
    val bodyHi: String = "",
    val type: String = "",        // event, quote, location, notification, whatsapp, gallery
    val imageUrl: String = "",
    val actionUrl: String = "",
    val actionLabel: String = "",
    val actionLabelHi: String = "",
    val priority: Int = 0,
    val active: Boolean = true,
    val showOnMobile: Boolean = true,
    val showOnTv: Boolean = true,
    val startDate: String = "",
    val endDate: String = ""
) {
    fun getTitle(isHindi: Boolean): String = if (isHindi && titleHi.isNotBlank()) titleHi else title
    fun getBody(isHindi: Boolean): String = if (isHindi && bodyHi.isNotBlank()) bodyHi else body
    fun getActionLabel(isHindi: Boolean): String = if (isHindi && actionLabelHi.isNotBlank()) actionLabelHi else actionLabel
}
