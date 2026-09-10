package com.khatanow.app.domain.sync

import com.khatanow.app.data.local.AppDatabase
import com.khatanow.app.data.local.entities.CustomerEntity
import com.khatanow.app.data.local.entities.ProductEntity
import com.khatanow.app.data.local.entities.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseMerger(private val db: AppDatabase) {

    suspend fun mergePayload(
        remoteCustomers: List<CustomerEntity>,
        remoteProducts: List<ProductEntity>,
        remoteTransactions: List<TransactionEntity>
    ): MergeResult = withContext(Dispatchers.IO) {
        var addedCustomers = 0
        var addedProducts = 0
        var addedTransactions = 0

        db.runInTransaction {
            val customerDao = db.customerDao()
            val productDao = db.productDao()
            val transactionDao = db.transactionDao()

            // 1. Merge Customers
            for (remoteCust in remoteCustomers) {
                val existing = kotlinx.coroutines.runBlocking { customerDao.getCustomerById(remoteCust.id) }
                if (existing == null) {
                    kotlinx.coroutines.runBlocking { customerDao.insertCustomer(remoteCust) }
                    addedCustomers++
                } else if (remoteCust.updatedAt > existing.updatedAt) {
                    kotlinx.coroutines.runBlocking { customerDao.insertCustomer(remoteCust) }
                }
            }

            // 2. Merge Products
            for (remoteProd in remoteProducts) {
                val existing = kotlinx.coroutines.runBlocking { productDao.getProductById(remoteProd.id) }
                if (existing == null) {
                    kotlinx.coroutines.runBlocking { productDao.insertProduct(remoteProd) }
                    addedProducts++
                } else if (remoteProd.updatedAt > existing.updatedAt) {
                    kotlinx.coroutines.runBlocking { productDao.insertProduct(remoteProd) }
                }
            }

            // 3. Merge Transactions (Append-Only UUID Duplicate Prevention)
            for (remoteTx in remoteTransactions) {
                val existing = kotlinx.coroutines.runBlocking { transactionDao.getTransactionById(remoteTx.id) }
                if (existing == null) {
                    val syncedTx = remoteTx.copy(syncStatus = TransactionEntity.SYNC_STATUS_SYNCHRONIZED)
                    kotlinx.coroutines.runBlocking { transactionDao.insertTransaction(syncedTx) }
                    addedTransactions++
                } else if (remoteTx.updatedAt > existing.updatedAt) {
                    val syncedTx = remoteTx.copy(syncStatus = TransactionEntity.SYNC_STATUS_SYNCHRONIZED)
                    kotlinx.coroutines.runBlocking { transactionDao.insertTransaction(syncedTx) }
                }
            }
        }

        MergeResult(addedCustomers, addedProducts, addedTransactions)
    }
}

data class MergeResult(
    val newCustomersCount: Int,
    val newProductsCount: Int,
    val newTransactionsCount: Int
)
