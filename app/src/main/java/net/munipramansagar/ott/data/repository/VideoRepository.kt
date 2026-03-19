package net.munipramansagar.ott.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import net.munipramansagar.ott.data.model.Video
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val videosCollection = firestore.collection("videos")

    suspend fun getVideosByCategory(
        categorySlug: String,
        limit: Long = 24,
        lastPublishedAt: String? = null
    ): List<Video> {
        var query = videosCollection
            .whereEqualTo("categorySlug", categorySlug)
            .orderBy("publishedAt", Query.Direction.DESCENDING)
            .limit(limit)

        if (lastPublishedAt != null) {
            query = query.startAfter(lastPublishedAt)
        }

        return query.get().await().toObjects(Video::class.java)
    }

    suspend fun getVideosByChannel(
        channelKey: String,
        limit: Long = 24,
        lastPublishedAt: String? = null
    ): List<Video> {
        var query = videosCollection
            .whereEqualTo("channelKey", channelKey)
            .orderBy("publishedAt", Query.Direction.DESCENDING)
            .limit(limit)

        if (lastPublishedAt != null) {
            query = query.startAfter(lastPublishedAt)
        }

        return query.get().await().toObjects(Video::class.java)
    }

    suspend fun getLatestVideos(limit: Long = 10): List<Video> {
        return videosCollection
            .orderBy("publishedAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
            .toObjects(Video::class.java)
    }

    suspend fun getVideoById(videoId: String): Video? {
        val doc = videosCollection.document(videoId).get().await()
        return doc.toObject(Video::class.java)
    }

    suspend fun getRelatedVideos(video: Video, limit: Long = 10): List<Video> {
        return videosCollection
            .whereEqualTo("categorySlug", video.categorySlug)
            .orderBy("publishedAt", Query.Direction.DESCENDING)
            .limit(limit + 1)
            .get()
            .await()
            .toObjects(Video::class.java)
            .filter { it.id != video.id }
            .take(limit.toInt())
    }
}
