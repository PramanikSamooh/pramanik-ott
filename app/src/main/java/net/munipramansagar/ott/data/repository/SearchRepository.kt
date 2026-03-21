package net.munipramansagar.ott.data.repository

import net.munipramansagar.ott.data.model.Video
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend fun search(query: String, limit: Long = 30): List<Video> {
        return videoRepository.searchVideos(query, limit)
    }
}
