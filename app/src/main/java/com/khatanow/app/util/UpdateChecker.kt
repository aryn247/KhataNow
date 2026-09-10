package com.khatanow.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersionCode: Int,
    val latestVersionName: String,
    val downloadUrl: String,
    val releaseNotes: String
)

object UpdateChecker {
    private const val VERSION_URL = "https://aryn247.github.io/Self-Portfolio/version.json"
    private const val CHANNEL_ID = "khata_now_updates"
    private val _updateState = MutableStateFlow<UpdateInfo?>(null)
    val updateState: StateFlow<UpdateInfo?> = _updateState

    suspend fun checkForUpdates(context: Context, currentVersionCode: Int = 1) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(VERSION_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 3000
                connection.readTimeout = 3000
                connection.requestMethod = "GET"
                
                if (connection.responseCode == 200) {
                    val stream = connection.inputStream
                    val jsonText = stream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonText)
                    
                    val versionCode = json.optInt("versionCode", 1)
                    val versionName = json.optString("versionName", "1.0.0")
                    val downloadUrl = json.optString("downloadUrl", "https://aryn247.github.io/Self-Portfolio/KhataNow.apk")
                    val notes = json.optString("releaseNotes", "New performance and feature updates available!")

                    if (versionCode > currentVersionCode) {
                        val info = UpdateInfo(
                            hasUpdate = true,
                            latestVersionCode = versionCode,
                            latestVersionName = versionName,
                            downloadUrl = downloadUrl,
                            releaseNotes = notes
                        )
                        _updateState.value = info

                        // Send System Notification to Phone Status Bar
                        showSystemNotification(context, info)
                    }
                }
            } catch (ignored: Exception) {
                // Fails silently if offline or network unavailable to save data
            }
        }
    }

    private fun showSystemNotification(context: Context, updateInfo: UpdateInfo) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "App Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for KhataNow app updates"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl))
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("✨ KhataNow Update Available (v${updateInfo.latestVersionName})")
                .setContentText("Tap to download the new version of KhataNow.")
                .setStyle(NotificationCompat.BigTextStyle().bigText("${updateInfo.releaseNotes}\nTap to download."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(1001, notification)
        } catch (ignored: Exception) {}
    }
}
