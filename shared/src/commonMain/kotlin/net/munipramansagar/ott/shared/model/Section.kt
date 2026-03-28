package net.munipramansagar.ott.shared.model

data class SectionData(
    val id: String = "",
    val label: String = "",
    val labelHi: String = "",
    val slug: String = "",
    val displayOrder: Int = 0,
    val visible: Boolean = true,
    val icon: String = ""
) {
    fun getLabel(isHindi: Boolean): String =
        if (isHindi && labelHi.isNotBlank()) labelHi else label
}
