package com.khatanow.app.data.repository

import com.khatanow.app.data.local.dao.TransactionDao
import com.khatanow.app.data.local.entities.TransactionEntity
import com.khatanow.app.data.local.entities.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val deviceId: String
) {
    val allTransactions: Flow<List<TransactionWithDetails>> = transactionDao.getAllTransactionsWithDetails()

    fun getTransactionsForCustomer(customerId: String): Flow<List<TransactionWithDetails>> {
        return transactionDao.getTransactionsForCustomer(customerId)
    }

    suspend fun addTransaction(customerId: String, productId: String, quantity: Int): TransactionEntity {
        val transaction = TransactionEntity(
            customerId = customerId,
            productId = productId,
            quantity = quantity,
            timestamp = System.currentTimeMillis(),
            deviceId = deviceId,
            syncStatus = TransactionEntity.SYNC_STATUS_LOCAL_ONLY,
            updatedAt = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(transaction)
        return transaction
    }

    suspend fun updateTransactionQuantity(transactionId: String, newQuantity: Int) {
        val existing = transactionDao.getTransactionById(transactionId) ?: return
        val updated = existing.copy(
            quantity = newQuantity,
            updatedAt = System.currentTimeMillis(),
            syncStatus = TransactionEntity.SYNC_STATUS_LOCAL_ONLY
        )
        transactionDao.updateTransaction(updated)
    }

    suspend fun deleteTransaction(transactionId: String) {
        transactionDao.softDeleteTransaction(transactionId)
    }

    suspend fun getTransactionCount(): Int = transactionDao.getTransactionCount()
}
