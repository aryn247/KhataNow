package com.khatanow.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "authorized_devices")
data class DeviceEntity(
    @PrimaryKey
    val deviceId: String,
    val deviceName: String,
    val role: String, // "BOSS" or "EMPLOYEE"
    val pairedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long = 0L,
    val isAuthorized: Boolean = true
) {
    companion object {
        const val ROLE_BOSS = "BOSS"
        const val ROLE_EMPLOYEE = "EMPLOYEE"
    }
}
