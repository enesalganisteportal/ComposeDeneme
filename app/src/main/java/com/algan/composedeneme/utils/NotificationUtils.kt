package com.algan.composedeneme.utils

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope

object NotificationUtils {

    private const val CHANNEL_ID = "ForegroundServiceChannel"
    private val notificationId = 1

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    fun completeNotification(contentText: String, service: Service) {
        try {
            // Create an intent that opens the Downloads folder
            // Intent to open the system's Downloads app
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)

            // Create a PendingIntent from the intent
            val pendingIntent = PendingIntent.getActivity(
                service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Build a new notification with a different icon to indicate completion
            val completedNotification = NotificationCompat.Builder(service, CHANNEL_ID)
                .setContentTitle("Conversion Completed")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.stat_sys_download_done) // Icon for completed download
                .setContentIntent(pendingIntent)  // Attach the pending intent to the notification
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                // Add any other properties for the completed notification here
                .build()
            service.stopForeground(false)

            if (ActivityCompat.checkSelfPermission(
                    service,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(service).notify(notificationId, completedNotification)
            } else {
                // Log or handle the lack of permission here
                ToastUtil.showToast(service.applicationContext, "POST_NOTIFICATIONS permission not granted")
            }
        } catch (e: Exception) {
            // Handle any exceptions that occur while attempting to post the notification
            ToastUtil.showToast(service.applicationContext, "Failed to post notification: ${e.localizedMessage}")
            // Consider notifying the user via another mechanism that doesn't require permissions
        }
    }

}