package net.munipramansagar.ott.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class PathshalaClass(
    val id: String = "",
    val title: String = "",
    val titleHi: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val language: String = "hindi",
    val dayOfWeek: List<Int> = listOf(0), // 0=Sunday, 6=Saturday
    val time: String = "10:00", // HH:mm format
    val timezone: String = "IST",
    val youtubeLink: String = "",
    val description: String = "",
    val recurring: Boolean = true,
    val active: Boolean = true
) {
    fun getTitle(isHindi: Boolean): String =
        if (isHindi && titleHi.isNotBlank()) titleHi else title
}

@IgnoreExtraProperties
data class Teacher(
    val id: String = "",
    val name: String = "",
    val nameHi: String = "",
    val photoUrl: String = "",
    val language: String = "",
    val bio: String = "",
    val active: Boolean = true
) {
    fun getName(isHindi: Boolean): String =
        if (isHindi && nameHi.isNotBlank()) nameHi else name
}
