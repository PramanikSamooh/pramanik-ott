package net.munipramansagar.ott.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import net.munipramansagar.ott.data.model.Announcement
import net.munipramansagar.ott.data.model.Playlist
import net.munipramansagar.ott.data.model.Section
import net.munipramansagar.ott.data.model.Video
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val videosCollection = firestore.collection("videos")
    private val playlistsCollection = firestore.collection("playlists")
    private val sectionsCollection = firestore.collection("sections")
    private val announcementsCollection = firestore.collection("announcements")

    // ── Sections ──

    suspend fun getSections(): List<Section> {
        return sectionsCollection
            .get()
            .await()
            .toObjects(Section::class.java)
            .filter { it.visible }
            .sortedBy { it.priority }
    }

    // ── Playlists ──

    suspend fun getPlaylistsBySection(
        sectionId: String,
        limit: Long = 20
    ): List<Playlist> {
        // Simple query: just filter by section, sort client-side
        // Avoids complex composite index requirements
        val playlists = playlistsCollection
            .whereEqualTo("section", sectionId)
            .limit(100)
            .get()
            .await()
            .toObjects(Playlist::class.java)

        return playlists
            .filter { it.visible }
            .sortedWith(compareByDescending<Playlist> { it.pinned }.thenBy { it.displayOrder })
            .take(limit.toInt())
    }

    suspend fun getPlaylistById(playlistId: String): Playlist? {
        val doc = playlistsCollection.document(playlistId).get().await()
        return doc.toObject(Playlist::class.java)
    }

    // ── Playlist Videos (subcollection) ──

    suspend fun getPlaylistVideos(
        playlistId: String,
        limit: Long = 10,
        lastPublishedAt: String? = null
    ): List<Video> {
        var query = playlistsCollection
            .document(playlistId)
            .collection("videos")
            .orderBy("publishedAt", Query.Direction.DESCENDING)
            .limit(limit)

        if (lastPublishedAt != null) {
            query = query.startAfter(lastPublishedAt)
        }

        return query.get().await().toObjects(Video::class.java)
    }

    // ── Flat /videos collection (backward compat) ──

    suspend fun getVideoById(videoId: String): Video? {
        val doc = videosCollection.document(videoId).get().await()
        return doc.toObject(Video::class.java)
    }

    suspend fun getLatestVideos(limit: Long = 10): List<Video> {
        return videosCollection
            .orderBy("publishedAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
            .toObjects(Video::class.java)
    }

    // Cache all videos for client-side search (works better for Hindi text)
    private var allVideosCache: List<Video>? = null

    suspend fun searchVideos(query: String, limit: Long = 30): List<Video> {
        if (query.isBlank()) return emptyList()
        val queryLower = query.lowercase().trim()

        // Load all videos once and cache them
        if (allVideosCache == null) {
            allVideosCache = videosCollection
                .get()
                .await()
                .toObjects(Video::class.java)
        }

        val videos = allVideosCache ?: return emptyList()

        // Client-side contains search — works for Hindi, partial matches, etc.
        return videos
            .filter { video ->
                video.title.lowercase().contains(queryLower) ||
                video.description.lowercase().contains(queryLower) ||
                video.playlistTitle.lowercase().contains(queryLower) ||
                video.tags.any { tag -> tag.lowercase().contains(queryLower) }
            }
            .sortedByDescending { video ->
                // Prioritize title matches
                when {
                    video.title.lowercase().startsWith(queryLower) -> 3
                    video.title.lowercase().contains(queryLower) -> 2
                    else -> 1
                }
            }
            .take(limit.toInt())
    }

    suspend fun getRelatedVideos(video: Video, limit: Long = 10): List<Video> {
        // Try to get related videos from same playlist first
        val source = if (video.playlistId.isNotBlank()) {
            getPlaylistVideos(video.playlistId, limit = limit + 1)
        } else {
            // Fallback to flat collection by categorySlug
            videosCollection
                .whereEqualTo("categorySlug", video.categorySlug)
                .orderBy("publishedAt", Query.Direction.DESCENDING)
                .limit(limit + 1)
                .get()
                .await()
                .toObjects(Video::class.java)
        }
        return source.filter { it.id != video.id }.take(limit.toInt())
    }

    // ── Announcements ──

    suspend fun getActiveAnnouncements(): List<Announcement> {
        return announcementsCollection
            .get()
            .await()
            .toObjects(Announcement::class.java)
            .filter { it.active }
            .sortedBy { it.priority }
    }

    // ── Donations ──

    suspend fun getDonationOrgs(): List<net.munipramansagar.ott.data.model.DonationOrg> {
        return firestore.collection("donations")
            .get()
            .await()
            .toObjects(net.munipramansagar.ott.data.model.DonationOrg::class.java)
            .filter { it.active }
            .sortedBy { it.priority }
    }

    // ── Legacy methods (backward compat for search/other consumers) ──

    @Deprecated("Use getPlaylistsBySection + getPlaylistVideos instead")
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
}
