package com.khatanow.app.util

import android.content.Context
import android.os.Build
import java.util.UUID

object DeviceUtils {
    private const val PREF_DEVICE_ID = "pref_device_id"

    fun getDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences("khata_now_prefs", Context.MODE_PRIVATE)
        var deviceId = prefs.getString(PREF_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = "DEV-" + UUID.randomUUID().toString().take(8).uppercase()
            prefs.edit().putString(PREF_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.capitalize()
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            model.capitalize()
        } else {
            "$manufacturer $model"
        }
    }
}
