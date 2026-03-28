package net.munipramansagar.ott.shared.domain

object WatchProgressCalculator {
    fun isCompleted(positionMs: Long, totalDurationMs: Long): Boolean =
        totalDurationMs > 0 && positionMs > (totalDurationMs * 0.9)

    fun progressFraction(positionMs: Long, totalDurationMs: Long): Float =
        if (totalDurationMs > 0) (positionMs.toFloat() / totalDurationMs).coerceIn(0f, 1f) else 0f

    fun shouldResume(positionMs: Long): Boolean = positionMs > 5000L
}
