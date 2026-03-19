package net.munipramansagar.ott.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(
    val slug: String,
    val label: String,
    val labelHi: String,
    val icon: ImageVector,
    val color: Long
) {
    DISCOURSE(
        slug = "discourse",
        label = "Discourses",
        labelHi = "प्रवचन",
        icon = Icons.Default.Book,
        color = 0xFFE8730A
    ),
    BHAWNA_YOG(
        slug = "bhawna-yog",
        label = "Bhawna Yog",
        labelHi = "भावना योग",
        icon = Icons.Default.Favorite,
        color = 0xFFC9932A
    ),
    SWADHYAY(
        slug = "swadhyay",
        label = "Agam Swadhyay",
        labelHi = "आगम स्वाध्याय",
        icon = Icons.Default.MenuBook,
        color = 0xFFB8860B
    ),
    SHANKA_CLIPS(
        slug = "shanka-clips",
        label = "Q&A Highlights",
        labelHi = "शंका समाधान",
        icon = Icons.Default.FlashOn,
        color = 0xFFC9932A
    ),
    SHANKA_FULL(
        slug = "shanka-full",
        label = "Shanka Samadhan (Full)",
        labelHi = "शंका समाधान (पूर्ण)",
        icon = Icons.Default.QuestionAnswer,
        color = 0xFFB8860B
    ),
    LIVE(
        slug = "live",
        label = "Live Events",
        labelHi = "लाइव कार्यक्रम",
        icon = Icons.Default.LiveTv,
        color = 0xFFFF1744
    ),
    KIDS(
        slug = "kids",
        label = "Jain Pathshala",
        labelHi = "जैन पाठशाला",
        icon = Icons.Default.Star,
        color = 0xFF1A4E7A
    );

    companion object {
        fun fromSlug(slug: String): Category? = entries.find { it.slug == slug }
    }

    fun getLabel(isHindi: Boolean): String = if (isHindi) labelHi else label
}
