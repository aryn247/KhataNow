package com.khatanow.app.data.repository

import com.khatanow.app.data.local.dao.CustomerDao
import com.khatanow.app.data.local.entities.CustomerEntity
import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val customerDao: CustomerDao) {
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    suspend fun getAllCustomersList(): List<CustomerEntity> = customerDao.getAllCustomersList()

    suspend fun getCustomerById(id: String): CustomerEntity? = customerDao.getCustomerById(id)

    fun searchCustomers(query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(query)

    suspend fun addCustomer(name: String, phone: String? = null): CustomerEntity {
        val customer = CustomerEntity(
            name = name.trim(),
            phone = phone?.trim()?.ifEmpty { null }
        )
        customerDao.insertCustomer(customer)
        return customer
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        val updated = customer.copy(updatedAt = System.currentTimeMillis())
        customerDao.updateCustomer(updated)
    }

    suspend fun deleteCustomer(id: String) {
        customerDao.softDeleteCustomer(id)
    }

    suspend fun getCustomerCount(): Int = customerDao.getCustomerCount()
}
