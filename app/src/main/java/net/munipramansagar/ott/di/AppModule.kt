package net.munipramansagar.ott.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.munipramansagar.ott.data.local.AppDatabase
import net.munipramansagar.ott.data.local.WatchHistoryDao
import net.munipramansagar.ott.util.LanguageManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "pramanik_ott_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideWatchHistoryDao(db: AppDatabase): WatchHistoryDao = db.watchHistoryDao()

    @Provides
    @Singleton
    fun provideLanguageManager(@ApplicationContext context: Context): LanguageManager =
        LanguageManager(context)
}
