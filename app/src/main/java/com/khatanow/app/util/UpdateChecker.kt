package com.khatanow.app.util

import android.content.Context
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
                        _updateState.value = UpdateInfo(
                            hasUpdate = true,
                            latestVersionCode = versionCode,
                            latestVersionName = versionName,
                            downloadUrl = downloadUrl,
                            releaseNotes = notes
                        )
                    }
                }
            } catch (ignored: Exception) {
                // Fails silently if offline or network unavailable to avoid consuming battery/data
            }
        }
    }
}
