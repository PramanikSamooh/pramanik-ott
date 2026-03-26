package net.munipramansagar.ott.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WatchHistoryEntry::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchHistoryDao(): WatchHistoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE watch_history ADD COLUMN bookmarked INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE watch_history ADD COLUMN bookmarkedAt INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
