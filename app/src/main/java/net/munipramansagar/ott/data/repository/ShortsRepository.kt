package net.munipramansagar.ott.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import net.munipramansagar.ott.data.model.Video
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShortsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val shortsCollection = firestore.collection("shorts")
    private val videosCollection = firestore.collection("videos")

    /**
     * Fetch curated shorts from /shorts collection.
     * Admin can manually add video docs to /shorts/{videoId}.
     */
    suspend fun getCuratedShorts(limit: Long = 20): List<Video> {
        return try {
            shortsCollection
                .orderBy("publishedAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
                .toObjects(Video::class.java)
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Fetch short videos from /videos where isShort == true.
     * Fallback if no curated shorts exist.
     */
    suspend fun getShortVideos(limit: Long = 20): List<Video> {
        return try {
            videosCollection
                .whereEqualTo("isShort", true)
                .orderBy("publishedAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
                .toObjects(Video::class.java)
        } catch (_: Exception) {
            // Fallback: query by durationSec <= 60
            try {
                videosCollection
                    .whereLessThanOrEqualTo("durationSec", 60)
                    .orderBy("durationSec", Query.Direction.ASCENDING)
                    .limit(limit)
                    .get()
                    .await()
                    .toObjects(Video::class.java)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Get shorts: curated first, then auto-detected shorts as fallback.
     */
    suspend fun getShorts(limit: Long = 20): List<Video> {
        val curated = getCuratedShorts(limit)
        if (curated.isNotEmpty()) return curated

        return getShortVideos(limit)
    }
}
