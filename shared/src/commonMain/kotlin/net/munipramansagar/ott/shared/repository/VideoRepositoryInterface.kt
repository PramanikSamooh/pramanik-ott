package net.munipramansagar.ott.shared.repository

import net.munipramansagar.ott.shared.model.*

interface VideoRepositoryInterface {
    suspend fun getSections(): List<SectionData>
    suspend fun getPlaylistsBySection(sectionId: String, limit: Long = 20): List<PlaylistData>
    suspend fun getPlaylistById(playlistId: String): PlaylistData?
    suspend fun getPlaylistVideos(playlistId: String, limit: Long = 10, lastPublishedAt: String? = null, ascending: Boolean = false): List<VideoData>
    suspend fun searchVideos(query: String, limit: Long = 20): List<VideoData>
    suspend fun getRelatedVideos(video: VideoData, limit: Long = 10): List<VideoData>
}

interface WatchHistoryRepositoryInterface {
    suspend fun saveProgress(videoId: String, title: String, titleHi: String, thumbnailUrl: String, channelName: String, durationFormatted: String, playlistId: String, playlistTitle: String, sectionId: String, positionMs: Long, totalDurationMs: Long, playlistIndex: Int)
    suspend fun getResumePosition(videoId: String): Long
    suspend fun toggleBookmark(videoId: String, title: String, thumbnailUrl: String, channelName: String)
    suspend fun isBookmarked(videoId: String): Boolean
    suspend fun removeFromHistory(videoId: String)
    suspend fun clearHistory()
}

interface LiveRepositoryInterface {
    suspend fun getLiveStatus(): LiveStatusData
    suspend fun getAnnouncements(): List<AnnouncementData>
}

interface PathshalaRepositoryInterface {
    suspend fun getActiveClasses(): List<PathshalaClassData>
    suspend fun getTeachers(): List<TeacherData>
}

interface ShortsRepositoryInterface {
    suspend fun getShorts(limit: Long = 20): List<VideoData>
}

interface DonationRepositoryInterface {
    suspend fun getDonationOrgs(): List<DonationOrgData>
}
