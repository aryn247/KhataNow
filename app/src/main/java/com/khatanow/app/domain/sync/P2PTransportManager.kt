package com.khatanow.app.domain.sync

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.khatanow.app.data.local.AppDatabase
import com.khatanow.app.util.DeviceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

sealed class SyncState {
    object Idle : SyncState()
    object Discovering : SyncState()
    object Advertising : SyncState()
    data class DeviceDiscovered(val endpointId: String, val deviceName: String) : SyncState()
    data class SyncRequested(val endpointId: String, val requesterName: String, val payload: SyncPayload) : SyncState()
    object Transferring : SyncState()
    data class Completed(val message: String) : SyncState()
    data class Error(val message: String) : SyncState()
}

class P2PTransportManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val merger = DatabaseMerger(db)
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val localDeviceId = DeviceUtils.getDeviceId(context)
    private val localDeviceName = DeviceUtils.getDeviceName()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var activeEndpointId: String? = null
    private val SERVICE_ID = "com.khatanow.app.SYNC_SERVICE"

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val bytes = payload.asBytes() ?: return
            val jsonStr = String(bytes, StandardCharsets.UTF_8)
            try {
                val syncPayload = SyncPayload.fromJsonString(jsonStr)
                handleIncomingSyncPayload(endpointId, syncPayload)
            } catch (e: Exception) {
                _syncState.value = SyncState.Error("Invalid sync data format: ${e.localizedMessage}")
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            activeEndpointId = endpointId
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                activeEndpointId = endpointId
            } else {
                _syncState.value = SyncState.Error("P2P Connection rejected or failed.")
            }
        }

        override fun onDisconnected(endpointId: String) {
            activeEndpointId = null
            if (_syncState.value !is SyncState.Completed) {
                _syncState.value = SyncState.Idle
            }
        }
    }

    fun startAdvertising() {
        _syncState.value = SyncState.Advertising
        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startAdvertising(localDeviceName, SERVICE_ID, connectionLifecycleCallback, options)
            .addOnFailureListener { e ->
                _syncState.value = SyncState.Error("Failed to start advertising: ${e.localizedMessage}")
            }
    }

    fun startDiscovery() {
        _syncState.value = SyncState.Discovering
        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.startDiscovery(SERVICE_ID, object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                _syncState.value = SyncState.DeviceDiscovered(endpointId, info.endpointName)
            }

            override fun onEndpointLost(endpointId: String) {}
        }, options).addOnFailureListener { e ->
            _syncState.value = SyncState.Error("Failed to start discovery: ${e.localizedMessage}")
        }
    }

    fun connectToDevice(endpointId: String) {
        connectionsClient.requestConnection(localDeviceName, endpointId, connectionLifecycleCallback)
    }

    fun initiateSyncRequest(endpointId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val customers = db.customerDao().getAllCustomersList()
            val products = db.productDao().getAllProductsList()
            val transactions = db.transactionDao().getTransactionsModifiedSince(0L)

            val payload = SyncPayload(
                messageType = SyncPayload.TYPE_REQUEST,
                senderDeviceId = localDeviceId,
                senderDeviceName = localDeviceName,
                customers = customers,
                products = products,
                transactions = transactions
            )

            sendPayload(endpointId, payload)
        }
    }

    fun acceptSyncRequest(endpointId: String) {
        _syncState.value = SyncState.Transferring
        CoroutineScope(Dispatchers.IO).launch {
            val customers = db.customerDao().getAllCustomersList()
            val products = db.productDao().getAllProductsList()
            val transactions = db.transactionDao().getTransactionsModifiedSince(0L)

            val acceptPayload = SyncPayload(
                messageType = SyncPayload.TYPE_ACCEPT,
                senderDeviceId = localDeviceId,
                senderDeviceName = localDeviceName,
                customers = customers,
                products = products,
                transactions = transactions
            )

            sendPayload(endpointId, acceptPayload)
        }
    }

    fun declineSyncRequest(endpointId: String) {
        val declinePayload = SyncPayload(
            messageType = SyncPayload.TYPE_DECLINE,
            senderDeviceId = localDeviceId,
            senderDeviceName = localDeviceName
        )
        sendPayload(endpointId, declinePayload)
        _syncState.value = SyncState.Idle
    }

    private fun handleIncomingSyncPayload(endpointId: String, payload: SyncPayload) {
        when (payload.messageType) {
            SyncPayload.TYPE_REQUEST -> {
                _syncState.value = SyncState.SyncRequested(endpointId, payload.senderDeviceName, payload)
            }
            SyncPayload.TYPE_ACCEPT -> {
                _syncState.value = SyncState.Transferring
                CoroutineScope(Dispatchers.IO).launch {
                    val result = merger.mergePayload(payload.customers, payload.products, payload.transactions)
                    _syncState.value = SyncState.Completed(
                        "Sync successful! Added ${result.newTransactionsCount} new transactions, ${result.newCustomersCount} customers, ${result.newProductsCount} products."
                    )
                }
            }
            SyncPayload.TYPE_DECLINE -> {
                _syncState.value = SyncState.Error("Sync request declined by paired phone.")
            }
        }
    }

    private fun sendPayload(endpointId: String, payload: SyncPayload) {
        val bytes = payload.toJsonString().toByteArray(StandardCharsets.UTF_8)
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(bytes))
    }

    fun stopAll() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        _syncState.value = SyncState.Idle
    }
}
