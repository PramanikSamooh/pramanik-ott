package net.munipramansagar.ott.shared.domain

data class TimezoneInfo(val label: String, val offsetMin: Int)

object TimezoneConverter {
    val TIMEZONES = listOf(
        TimezoneInfo("IST", 330),
        TimezoneInfo("AEST", 600),
        TimezoneInfo("SGT", 480),
        TimezoneInfo("CET", 60),
        TimezoneInfo("EST", -300),
        TimezoneInfo("CST", -360),
        TimezoneInfo("PST", -480),
    )

    fun convertISTToTimezones(istTime: String): String {
        val parts = istTime.split(":")
        if (parts.size != 2) return istTime
        val istHour = parts[0].toIntOrNull() ?: return istTime
        val istMin = parts[1].toIntOrNull() ?: return istTime
        val istTotalMin = istHour * 60 + istMin

        return TIMEZONES.joinToString("  •  ") { tz ->
            val diff = tz.offsetMin - 330
            var totalMin = istTotalMin + diff
            if (totalMin < 0) totalMin += 1440
            if (totalMin >= 1440) totalMin -= 1440
            val h = totalMin / 60
            val m = totalMin % 60
            val amPm = if (h < 12) "AM" else "PM"
            val dh = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
            "${tz.label} ${dh}:${m.toString().padStart(2, '0')} $amPm"
        }
    }

    fun formatTime12Hour(time: String): String {
        val parts = time.split(":")
        if (parts.size != 2) return time
        val hour = parts[0].toIntOrNull() ?: return time
        val minute = parts[1]
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return "$displayHour:$minute $amPm"
    }
}
