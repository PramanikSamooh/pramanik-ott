package net.munipramansagar.ott.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import net.munipramansagar.ott.data.model.Video
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val videosCollection = firestore.collection("videos")

    suspend fun search(query: String, limit: Long = 30): List<Video> {
        if (query.isBlank()) return emptyList()

        val queryLower = query.lowercase().trim()

        // Firestore doesn't support full-text search natively.
        // We search by title prefix match and tags array-contains.
        // For production, consider Algolia or Typesense integration.
        val titleResults = videosCollection
            .orderBy("title")
            .startAt(queryLower)
            .endAt(queryLower + "\uf8ff")
            .limit(limit)
            .get()
            .await()
            .toObjects(Video::class.java)

        val tagResults = if (titleResults.size < limit) {
            videosCollection
                .whereArrayContains("tags", queryLower)
                .limit(limit)
                .get()
                .await()
                .toObjects(Video::class.java)
        } else {
            emptyList()
        }

        // Merge and deduplicate
        val seen = mutableSetOf<String>()
        return (titleResults + tagResults)
            .filter { seen.add(it.id) }
            .take(limit.toInt())
    }
}
