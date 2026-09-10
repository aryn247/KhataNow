package com.khatanow.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["productId"]),
        Index(value = ["timestamp"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val productId: String,
    val quantity: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceId: String,
    val syncStatus: String = SYNC_STATUS_LOCAL_ONLY,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
) {
    companion object {
        const val SYNC_STATUS_LOCAL_ONLY = "LOCAL_ONLY"
        const val SYNC_STATUS_SYNCHRONIZED = "SYNCHRONIZED"
        const val SYNC_STATUS_CONFLICT = "CONFLICT"
    }
}

data class TransactionWithDetails(
    val transactionId: String,
    val customerId: String,
    val customerName: String,
    val productId: String,
    val productName: String,
    val productUnit: String?,
    val quantity: Int,
    val timestamp: Long,
    val deviceId: String,
    val syncStatus: String,
    val isDeleted: Boolean
)
