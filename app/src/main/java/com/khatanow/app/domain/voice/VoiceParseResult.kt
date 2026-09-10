package com.khatanow.app.domain.voice

import com.khatanow.app.data.local.entities.CustomerEntity
import com.khatanow.app.data.local.entities.ProductEntity

data class MatchedEntity<T>(
    val entity: T,
    val matchedText: String,
    val score: Float
)

data class VoiceParseResult(
    val rawText: String,
    val matchedCustomer: CustomerEntity? = null,
    val customerConfidence: Float = 0f,
    val customerAlternatives: List<CustomerEntity> = emptyList(),
    val matchedProduct: ProductEntity? = null,
    val productConfidence: Float = 0f,
    val productAlternatives: List<ProductEntity> = emptyList(),
    val quantity: Int = 1,
    val isFullyConfident: Boolean = false
)
