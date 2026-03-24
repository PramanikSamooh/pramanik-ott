package net.munipramansagar.ott.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WatchHistoryEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchHistoryDao(): WatchHistoryDao
}
