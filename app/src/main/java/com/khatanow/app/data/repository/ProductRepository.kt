package com.khatanow.app.data.repository

import com.khatanow.app.data.local.dao.ProductDao
import com.khatanow.app.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    suspend fun getAllProductsList(): List<ProductEntity> = productDao.getAllProductsList()

    suspend fun getProductById(id: String): ProductEntity? = productDao.getProductById(id)

    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

    suspend fun addProduct(name: String, unit: String? = null): ProductEntity {
        val product = ProductEntity(
            name = name.trim(),
            unit = unit?.trim()?.ifEmpty { null }
        )
        productDao.insertProduct(product)
        return product
    }

    suspend fun updateProduct(product: ProductEntity) {
        val updated = product.copy(updatedAt = System.currentTimeMillis())
        productDao.updateProduct(updated)
    }

    suspend fun deleteProduct(id: String) {
        productDao.softDeleteProduct(id)
    }

    suspend fun getProductCount(): Int = productDao.getProductCount()
}
