package net.munipramansagar.ott.data.model

import com.google.firebase.firestore.DocumentId

data class HomeRow(
    @DocumentId val id: String = "",
    val label: String = "",
    val labelHi: String = "",
    val type: String = "",       // "category", "channel", "live"
    val filter: String = "",     // categorySlug or channelKey
    val priority: Int = 0,
    val maxItems: Int = 12
) {
    fun getLabel(isHindi: Boolean): String = if (isHindi) labelHi else label
}
