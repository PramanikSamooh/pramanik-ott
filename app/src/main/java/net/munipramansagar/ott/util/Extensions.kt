package net.munipramansagar.ott.util

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun String.toRelativeTime(): String {
    val instant = try {
        Instant.parse(this)
    } catch (_: Exception) {
        return this
    }

    val now = Instant.now()
    val minutes = ChronoUnit.MINUTES.between(instant, now)
    val hours = ChronoUnit.HOURS.between(instant, now)
    val days = ChronoUnit.DAYS.between(instant, now)

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        days < 30 -> "${days / 7}w ago"
        days < 365 -> "${days / 30}mo ago"
        else -> "${days / 365}y ago"
    }
}

fun String.toThumbnailUrl(quality: String = "hqdefault"): String {
    return "${Constants.YOUTUBE_THUMBNAIL_BASE}$this/$quality.jpg"
}
