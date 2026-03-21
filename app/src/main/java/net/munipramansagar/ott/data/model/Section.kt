package net.munipramansagar.ott.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Section(
    val id: String = "",
    val label: String = "",
    val labelHi: String = "",
    val icon: String = "",
    val color: String = "",
    val priority: Int = 0,
    val visible: Boolean = true
) {
    fun getLabel(isHindi: Boolean): String = if (isHindi && labelHi.isNotBlank()) labelHi else label
}
