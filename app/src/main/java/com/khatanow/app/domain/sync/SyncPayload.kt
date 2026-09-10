package com.khatanow.app.domain.sync

import com.khatanow.app.data.local.entities.CustomerEntity
import com.khatanow.app.data.local.entities.ProductEntity
import com.khatanow.app.data.local.entities.TransactionEntity
import org.json.JSONArray
import org.json.JSONObject

data class SyncPayload(
    val messageType: String, // "REQUEST", "ACCEPT", "DECLINE", "DATA", "ACK"
    val senderDeviceId: String,
    val senderDeviceName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val customers: List<CustomerEntity> = emptyList(),
    val products: List<ProductEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList()
) {
    fun toJsonString(): String {
        val root = JSONObject()
        root.put("messageType", messageType)
        root.put("senderDeviceId", senderDeviceId)
        root.put("senderDeviceName", senderDeviceName)
        root.put("timestamp", timestamp)

        val custArray = JSONArray()
        customers.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("phone", c.phone ?: "")
            obj.put("createdAt", c.createdAt)
            obj.put("updatedAt", c.updatedAt)
            obj.put("isDeleted", c.isDeleted)
            custArray.put(obj)
        }
        root.put("customers", custArray)

        val prodArray = JSONArray()
        products.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("unit", p.unit ?: "")
            obj.put("createdAt", p.createdAt)
            obj.put("updatedAt", p.updatedAt)
            obj.put("isDeleted", p.isDeleted)
            prodArray.put(obj)
        }
        root.put("products", prodArray)

        val txArray = JSONArray()
        transactions.forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("customerId", t.customerId)
            obj.put("productId", t.productId)
            obj.put("quantity", t.quantity)
            obj.put("timestamp", t.timestamp)
            obj.put("deviceId", t.deviceId)
            obj.put("syncStatus", t.syncStatus)
            obj.put("updatedAt", t.updatedAt)
            obj.put("isDeleted", t.isDeleted)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        return root.toString()
    }

    companion object {
        const val TYPE_REQUEST = "REQUEST"
        const val TYPE_ACCEPT = "ACCEPT"
        const val TYPE_DECLINE = "DECLINE"
        const val TYPE_DATA = "DATA"
        const val TYPE_ACK = "ACK"

        fun fromJsonString(jsonStr: String): SyncPayload {
            val root = JSONObject(jsonStr)
            val messageType = root.getString("messageType")
            val senderDeviceId = root.getString("senderDeviceId")
            val senderDeviceName = root.getString("senderDeviceName")
            val timestamp = root.getLong("timestamp")

            val customersList = mutableListOf<CustomerEntity>()
            if (root.has("customers")) {
                val custArray = root.getJSONArray("customers")
                for (i in 0 until custArray.length()) {
                    val obj = custArray.getJSONObject(i)
                    customersList.add(
                        CustomerEntity(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            phone = obj.optString("phone").ifEmpty { null },
                            createdAt = obj.getLong("createdAt"),
                            updatedAt = obj.getLong("updatedAt"),
                            isDeleted = obj.getBoolean("isDeleted")
                        )
                    )
                }
            }

            val productsList = mutableListOf<ProductEntity>()
            if (root.has("products")) {
                val prodArray = root.getJSONArray("products")
                for (i in 0 until prodArray.length()) {
                    val obj = prodArray.getJSONObject(i)
                    productsList.add(
                        ProductEntity(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            unit = obj.optString("unit").ifEmpty { null },
                            createdAt = obj.getLong("createdAt"),
                            updatedAt = obj.getLong("updatedAt"),
                            isDeleted = obj.getBoolean("isDeleted")
                        )
                    )
                }
            }

            val transactionsList = mutableListOf<TransactionEntity>()
            if (root.has("transactions")) {
                val txArray = root.getJSONArray("transactions")
                for (i in 0 until txArray.length()) {
                    val obj = txArray.getJSONObject(i)
                    transactionsList.add(
                        TransactionEntity(
                            id = obj.getString("id"),
                            customerId = obj.getString("customerId"),
                            productId = obj.getString("productId"),
                            quantity = obj.getInt("quantity"),
                            timestamp = obj.getLong("timestamp"),
                            deviceId = obj.getString("deviceId"),
                            syncStatus = TransactionEntity.SYNC_STATUS_SYNCHRONIZED,
                            updatedAt = obj.getLong("updatedAt"),
                            isDeleted = obj.getBoolean("isDeleted")
                        )
                    )
                }
            }

            return SyncPayload(
                messageType = messageType,
                senderDeviceId = senderDeviceId,
                senderDeviceName = senderDeviceName,
                timestamp = timestamp,
                customers = customersList,
                products = productsList,
                transactions = transactionsList
            )
        }
    }
}
