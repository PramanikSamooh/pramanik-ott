package net.munipramansagar.ott.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import net.munipramansagar.ott.data.local.WatchHistoryDao
import net.munipramansagar.ott.data.local.WatchHistoryEntry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchHistoryRepository @Inject constructor(
    private val dao: WatchHistoryDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // --- Local operations (always available) ---

    fun getRecentlyWatched(limit: Int = 20): Flow<List<WatchHistoryEntry>> =
        dao.getRecentlyWatched(limit)

    fun getContinueWatching(limit: Int = 10): Flow<List<WatchHistoryEntry>> =
        dao.getContinueWatching(limit)

    suspend fun getResumePosition(videoId: String): Long =
        dao.getResumePosition(videoId) ?: 0L

    suspend fun getNextInPlaylist(playlistId: String): WatchHistoryEntry? =
        dao.getNextInPlaylist(playlistId)

    suspend fun saveProgress(
        videoId: String,
        title: String = "",
        titleHi: String = "",
        thumbnailUrl: String = "",
        channelName: String = "",
        durationFormatted: String = "",
        playlistId: String = "",
        playlistTitle: String = "",
        sectionId: String = "",
        positionMs: Long,
        totalDurationMs: Long = 0L,
        playlistIndex: Int = -1
    ) {
        val completed = totalDurationMs > 0 && positionMs > (totalDurationMs * 0.9)
        // Preserve bookmark state from existing entry
        val existing = dao.getEntry(videoId)
        val entry = WatchHistoryEntry(
            videoId = videoId,
            title = title,
            titleHi = titleHi,
            thumbnailUrl = thumbnailUrl,
            channelName = channelName,
            durationFormatted = durationFormatted,
            playlistId = playlistId,
            playlistTitle = playlistTitle,
            sectionId = sectionId,
            resumePositionMs = if (completed) 0L else positionMs,
            totalDurationMs = totalDurationMs,
            completed = completed,
            lastWatchedAt = System.currentTimeMillis(),
            playlistIndex = playlistIndex,
            bookmarked = existing?.bookmarked ?: false,
            bookmarkedAt = existing?.bookmarkedAt ?: 0L
        )
        dao.upsert(entry)

        // Sync to Firestore if signed in
        syncToCloud(entry)
    }

    // --- Bookmark operations ---

    fun getBookmarked(limit: Int = 50): Flow<List<WatchHistoryEntry>> =
        dao.getBookmarked(limit)

    suspend fun toggleBookmark(videoId: String, title: String = "", thumbnailUrl: String = "", channelName: String = "") {
        val existing = dao.getEntry(videoId)
        if (existing != null) {
            val newState = !(existing.bookmarked)
            dao.setBookmark(videoId, newState, if (newState) System.currentTimeMillis() else 0L)
        } else {
            // Create a new entry just for bookmarking (not watched yet)
            dao.upsert(WatchHistoryEntry(
                videoId = videoId,
                title = title,
                thumbnailUrl = thumbnailUrl,
                channelName = channelName,
                bookmarked = true,
                bookmarkedAt = System.currentTimeMillis()
            ))
        }
    }

    suspend fun isBookmarked(videoId: String): Boolean =
        dao.isBookmarked(videoId) ?: false

    suspend fun removeFromHistory(videoId: String) {
        dao.delete(videoId)
    }

    suspend fun clearHistory() {
        dao.clearAll()
        // Also clear cloud
        val uid = auth.currentUser?.uid ?: return
        try {
            val docs = firestore.collection("users").document(uid)
                .collection("watch_history")
                .get().await()
            for (doc in docs) {
                doc.reference.delete()
            }
        } catch (_: Exception) {}
    }

    // --- Cloud sync (only when signed in) ---

    private suspend fun syncToCloud(entry: WatchHistoryEntry) {
        val uid = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(uid)
                .collection("watch_history").document(entry.videoId)
                .set(mapOf(
                    "videoId" to entry.videoId,
                    "title" to entry.title,
                    "titleHi" to entry.titleHi,
                    "thumbnailUrl" to entry.thumbnailUrl,
                    "channelName" to entry.channelName,
                    "durationFormatted" to entry.durationFormatted,
                    "playlistId" to entry.playlistId,
                    "playlistTitle" to entry.playlistTitle,
                    "sectionId" to entry.sectionId,
                    "resumePositionMs" to entry.resumePositionMs,
                    "totalDurationMs" to entry.totalDurationMs,
                    "completed" to entry.completed,
                    "lastWatchedAt" to entry.lastWatchedAt,
                    "playlistIndex" to entry.playlistIndex
                )).await()
        } catch (_: Exception) {
            // Silently fail — local data is the source of truth
        }
    }

    suspend fun syncFromCloud() {
        val uid = auth.currentUser?.uid ?: return
        try {
            val docs = firestore.collection("users").document(uid)
                .collection("watch_history")
                .get().await()
            for (doc in docs) {
                val data = doc.data
                val entry = WatchHistoryEntry(
                    videoId = data["videoId"] as? String ?: continue,
                    title = data["title"] as? String ?: "",
                    titleHi = data["titleHi"] as? String ?: "",
                    thumbnailUrl = data["thumbnailUrl"] as? String ?: "",
                    channelName = data["channelName"] as? String ?: "",
                    durationFormatted = data["durationFormatted"] as? String ?: "",
                    playlistId = data["playlistId"] as? String ?: "",
                    playlistTitle = data["playlistTitle"] as? String ?: "",
                    sectionId = data["sectionId"] as? String ?: "",
                    resumePositionMs = data["resumePositionMs"] as? Long ?: 0L,
                    totalDurationMs = data["totalDurationMs"] as? Long ?: 0L,
                    completed = data["completed"] as? Boolean ?: false,
                    lastWatchedAt = data["lastWatchedAt"] as? Long ?: 0L,
                    playlistIndex = (data["playlistIndex"] as? Long)?.toInt() ?: -1
                )
                // Only update local if cloud entry is newer
                val local = dao.getEntry(entry.videoId)
                if (local == null || entry.lastWatchedAt > local.lastWatchedAt) {
                    dao.upsert(entry)
                }
            }
        } catch (_: Exception) {}
    }
}
