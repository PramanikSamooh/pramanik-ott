package net.munipramansagar.ott

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PramanikMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "PramanikFCM"
        private const val CHANNEL_ID = "pramanik_notifications"
        private const val CHANNEL_NAME = "Pramanik Notifications"
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM token: $token")
        // Token is auto-managed by Firebase — no need to store manually
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "FCM message from: ${message.from}")

        val title = message.notification?.title ?: message.data["title"] ?: "प्रमाणिक"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val videoId = message.data["videoId"]
        val type = message.data["type"] ?: "general" // live, video, announcement

        showNotification(title, body, videoId, type)
    }

    private fun showNotification(title: String, body: String, videoId: String?, type: String) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            videoId?.let { putExtra("videoId", it) }
            putExtra("notificationType", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val iconColor = 0xFFE8730A.toInt() // Saffron

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setColor(iconColor)
            .setContentIntent(pendingIntent)
            .setPriority(
                when (type) {
                    "live" -> NotificationCompat.PRIORITY_HIGH
                    else -> NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications from Pramanik OTT"
                enableLights(true)
                enableVibration(true)
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
