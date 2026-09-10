package com.khatanow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.khatanow.app.data.local.entities.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM authorized_devices WHERE isAuthorized = 1 ORDER BY deviceName ASC")
    fun getAuthorizedDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM authorized_devices WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getDeviceById(deviceId: String): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity)

    @Query("UPDATE authorized_devices SET lastSyncedAt = :timestamp WHERE deviceId = :deviceId")
    suspend fun updateLastSynced(deviceId: String, timestamp: Long)

    @Query("DELETE FROM authorized_devices WHERE deviceId = :deviceId")
    suspend fun removeDevice(deviceId: String)
}
