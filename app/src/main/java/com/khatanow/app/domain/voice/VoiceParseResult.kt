package com.khatanow.app.domain.voice

import com.khatanow.app.data.local.entities.CustomerEntity

data class MatchedEntity<T>(
    val entity: T,
    val matchedText: String,
    val score: Float
)
