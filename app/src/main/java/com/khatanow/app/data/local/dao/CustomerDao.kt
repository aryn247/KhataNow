package com.khatanow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.khatanow.app.data.local.entities.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE isDeleted = 0")
    suspend fun getAllCustomersList(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%' AND isDeleted = 0")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteCustomer(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM customers WHERE isDeleted = 0")
    suspend fun getCustomerCount(): Int

    @Query("SELECT * FROM customers WHERE updatedAt > :sinceTimestamp")
    suspend fun getCustomersModifiedSince(sinceTimestamp: Long): List<CustomerEntity>
}
