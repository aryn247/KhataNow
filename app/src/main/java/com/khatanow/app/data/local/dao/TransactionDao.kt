package com.khatanow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.khatanow.app.data.local.entities.TransactionEntity
import com.khatanow.app.data.local.entities.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("""
        SELECT 
            t.id AS transactionId,
            t.customerId AS customerId,
            c.name AS customerName,
            t.productId AS productId,
            p.name AS productName,
            p.unit AS productUnit,
            t.quantity AS quantity,
            t.timestamp AS timestamp,
            t.deviceId AS deviceId,
            t.syncStatus AS syncStatus,
            t.isDeleted AS isDeleted
        FROM transactions t
        INNER JOIN customers c ON t.customerId = c.id
        INNER JOIN products p ON t.productId = p.id
        WHERE t.isDeleted = 0
        ORDER BY t.timestamp DESC
    """)
    fun getAllTransactionsWithDetails(): Flow<List<TransactionWithDetails>>

    @Query("""
        SELECT 
            t.id AS transactionId,
            t.customerId AS customerId,
            c.name AS customerName,
            t.productId AS productId,
            p.name AS productName,
            p.unit AS productUnit,
            t.quantity AS quantity,
            t.timestamp AS timestamp,
            t.deviceId AS deviceId,
            t.syncStatus AS syncStatus,
            t.isDeleted AS isDeleted
        FROM transactions t
        INNER JOIN customers c ON t.customerId = c.id
        INNER JOIN products p ON t.productId = p.id
        WHERE t.customerId = :customerId AND t.isDeleted = 0
        ORDER BY t.timestamp DESC
    """)
    fun getTransactionsForCustomer(customerId: String): Flow<List<TransactionWithDetails>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteTransaction(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM transactions WHERE isDeleted = 0")
    suspend fun getTransactionCount(): Int

    @Query("SELECT * FROM transactions WHERE updatedAt > :sinceTimestamp")
    suspend fun getTransactionsModifiedSince(sinceTimestamp: Long): List<TransactionEntity>
}
