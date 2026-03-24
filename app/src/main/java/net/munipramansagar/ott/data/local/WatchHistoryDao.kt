package net.munipramansagar.ott.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WatchHistoryEntry)

    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC LIMIT :limit")
    fun getRecentlyWatched(limit: Int = 20): Flow<List<WatchHistoryEntry>>

    @Query("SELECT * FROM watch_history WHERE videoId = :videoId")
    suspend fun getEntry(videoId: String): WatchHistoryEntry?

    @Query("SELECT resumePositionMs FROM watch_history WHERE videoId = :videoId")
    suspend fun getResumePosition(videoId: String): Long?

    @Query("SELECT * FROM watch_history WHERE completed = 0 AND resumePositionMs > 0 ORDER BY lastWatchedAt DESC LIMIT :limit")
    fun getContinueWatching(limit: Int = 10): Flow<List<WatchHistoryEntry>>

    @Query("SELECT * FROM watch_history WHERE playlistId = :playlistId ORDER BY playlistIndex ASC")
    suspend fun getPlaylistHistory(playlistId: String): List<WatchHistoryEntry>

    @Query("SELECT * FROM watch_history WHERE playlistId = :playlistId AND completed = 0 ORDER BY playlistIndex ASC LIMIT 1")
    suspend fun getNextInPlaylist(playlistId: String): WatchHistoryEntry?

    @Query("DELETE FROM watch_history WHERE videoId = :videoId")
    suspend fun delete(videoId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM watch_history")
    suspend fun count(): Int
}
