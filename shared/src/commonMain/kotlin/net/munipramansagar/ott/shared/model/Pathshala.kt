package net.munipramansagar.ott.shared.model

data class PathshalaClassData(
    val id: String = "",
    val title: String = "",
    val titleHi: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val language: String = "hindi",
    val dayOfWeek: List<Int> = listOf(0),
    val time: String = "10:00",
    val timezone: String = "IST",
    val youtubeLink: String = "",
    val description: String = "",
    val recurring: Boolean = true,
    val active: Boolean = true
) {
    fun getTitle(isHindi: Boolean): String =
        if (isHindi && titleHi.isNotBlank()) titleHi else title
}

data class TeacherData(
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
