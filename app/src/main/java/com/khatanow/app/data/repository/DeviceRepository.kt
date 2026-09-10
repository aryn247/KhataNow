package com.khatanow.app.data.repository

import com.khatanow.app.data.local.dao.DeviceDao
import com.khatanow.app.data.local.entities.DeviceEntity
import kotlinx.coroutines.flow.Flow

class DeviceRepository(private val deviceDao: DeviceDao) {
    val authorizedDevices: Flow<List<DeviceEntity>> = deviceDao.getAuthorizedDevices()

    suspend fun getDeviceById(deviceId: String): DeviceEntity? = deviceDao.getDeviceById(deviceId)

    suspend fun authorizeDevice(deviceId: String, deviceName: String, role: String) {
        val device = DeviceEntity(
            deviceId = deviceId,
            deviceName = deviceName,
            role = role,
            pairedAt = System.currentTimeMillis(),
            lastSyncedAt = 0L,
            isAuthorized = true
        )
        deviceDao.insertDevice(device)
    }

    suspend fun updateLastSynced(deviceId: String, timestamp: Long) {
        deviceDao.updateLastSynced(deviceId, timestamp)
    }

    suspend fun removeDevice(deviceId: String) {
        deviceDao.removeDevice(deviceId)
    }
}
