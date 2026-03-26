package net.munipramansagar.ott

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltAndroidApp
class PramanikApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Set up crash logging to file
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logCrash(throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        try {
            FirebaseApp.initializeApp(this)

            // Enable Firestore offline persistence
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings

            // Subscribe to FCM topics for push notifications
            val isTv = net.munipramansagar.ott.util.DeviceUtil.isTv(this)
            com.google.firebase.messaging.FirebaseMessaging.getInstance().apply {
                subscribeToTopic("all")
                subscribeToTopic(if (isTv) "tv" else "mobile")
            }
            Log.d("PramanikApp", "FCM subscribed to topics: all, ${if (isTv) "tv" else "mobile"}")
        } catch (e: Exception) {
            Log.e("PramanikApp", "Firebase init error", e)
        }
    }

    private fun logCrash(throwable: Throwable) {
        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val logText = "=== CRASH $timestamp ===\n$sw\n\n"

            val logFile = File(getExternalFilesDir(null), "crash_log.txt")
            logFile.appendText(logText)
        } catch (_: Exception) {
            // Can't log, ignore
        }
    }
}
