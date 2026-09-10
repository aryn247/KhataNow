package com.khatanow.app.domain.voice

import com.khatanow.app.data.local.entities.CustomerEntity
import com.khatanow.app.data.local.entities.ProductEntity

data class VoiceParseResult(
    val rawText: String,
    val matchedCustomer: CustomerEntity? = null,
    val candidateCustomerName: String = "",
    val customerConfidence: Float = 0f,
    val customerAlternatives: List<CustomerEntity> = emptyList(),
    val matchedProduct: ProductEntity? = null,
    val candidateProductName: String = "",
    val productConfidence: Float = 0f,
    val productAlternatives: List<ProductEntity> = emptyList(),
    val quantity: Int = 1,
    val isFullyConfident: Boolean = false
)

class LocalVoiceParser {

    private val fillerWords = setOf(
        "ko", "ne", "ka", "ki", "ke", "gave", "took", "take", "give", "wanted",
        "wants", "piece", "pieces", "pkt", "packet", "packets", "kg", "bottle", "bottles",
        "को", "ने", "का", "की", "के", "ने लिया", "लिया", "दिया"
    )

    fun parse(
        spokenText: String,
        customers: List<CustomerEntity>,
        products: List<ProductEntity>
    ): VoiceParseResult {
        val rawTrimmed = spokenText.trim()
        if (rawTrimmed.isEmpty()) {
            return VoiceParseResult(rawText = "")
        }

        val originalTokens = rawTrimmed.split(Regex("\\s+"))
        
        // 1. Extract Quantity
        val extractedQty = NumberParser.extractQuantity(originalTokens)
        val quantity = extractedQty?.quantity ?: 1

        // 2. Filter quantity token and filler words
        val remainingTokens = originalTokens.filterIndexed { index, token ->
            val clean = token.lowercase().replace(Regex("[^a-z0-9अ-ह१-९]"), "")
            index != extractedQty?.tokenIndex && !fillerWords.contains(clean) && !fillerWords.contains(token)
        }

        val remainingPhrase = remainingTokens.joinToString(" ")

        // Generate candidate n-grams
        val nGrams = generateNGrams(remainingTokens)

        // 3. Match Customer
        val customerMatches = mutableListOf<MatchedEntity<CustomerEntity>>()
        for (customer in customers) {
            var maxScore = 0f
            var bestMatchedText = ""
            
            val fullScore = FuzzyMatcher.calculateSimilarity(remainingPhrase, customer.name)
            if (fullScore > maxScore) {
                maxScore = fullScore
                bestMatchedText = remainingPhrase
            }

            for (ngram in nGrams) {
                val score = FuzzyMatcher.calculateSimilarity(ngram, customer.name)
                if (score > maxScore) {
                    maxScore = score
                    bestMatchedText = ngram
                }
            }

            if (maxScore > 0.4f) {
                customerMatches.add(MatchedEntity(customer, bestMatchedText, maxScore))
            }
        }

        customerMatches.sortByDescending { it.score }
        val bestCustomerMatch = customerMatches.firstOrNull()
        val customerAlternatives = customerMatches.drop(1).take(3).map { it.entity }

        // Candidate customer text if missing
        val candidateCustomerName = if (bestCustomerMatch != null && bestCustomerMatch.score > 0.6f) {
            bestCustomerMatch.entity.name
        } else {
            remainingTokens.firstOrNull() ?: "New Customer"
        }

        // 4. Match Product
        val productTokens = if (bestCustomerMatch != null && bestCustomerMatch.score > 0.7f) {
            remainingTokens.filter { !bestCustomerMatch.matchedText.lowercase().contains(it.lowercase()) }
        } else if (remainingTokens.size > 1) {
            remainingTokens.drop(1)
        } else {
            remainingTokens
        }
        
        val productPhrase = productTokens.joinToString(" ")
        val productNGrams = generateNGrams(productTokens)

        val productMatches = mutableListOf<MatchedEntity<ProductEntity>>()
        for (product in products) {
            var maxScore = 0f
            var bestMatchedText = ""

            val fullScore = FuzzyMatcher.calculateSimilarity(productPhrase, product.name)
            if (fullScore > maxScore) {
                maxScore = fullScore
                bestMatchedText = productPhrase
            }

            for (ngram in productNGrams) {
                val score = FuzzyMatcher.calculateSimilarity(ngram, product.name)
                if (score > maxScore) {
                    maxScore = score
                    bestMatchedText = ngram
                }
            }

            if (maxScore > 0.4f) {
                productMatches.add(MatchedEntity(product, bestMatchedText, maxScore))
            }
        }

        productMatches.sortByDescending { it.score }
        val bestProductMatch = productMatches.firstOrNull()
        val productAlternatives = productMatches.drop(1).take(3).map { it.entity }

        // Candidate product text if missing
        val candidateProductName = if (bestProductMatch != null && bestProductMatch.score > 0.6f) {
            bestProductMatch.entity.name
        } else {
            productTokens.joinToString(" ").ifEmpty { "New Product" }
        }

        val isFullyConfident = (bestCustomerMatch?.score ?: 0f) >= 0.75f &&
                                (bestProductMatch?.score ?: 0f) >= 0.75f

        return VoiceParseResult(
            rawText = rawTrimmed,
            matchedCustomer = bestCustomerMatch?.entity,
            candidateCustomerName = candidateCustomerName.capitalizeWords(),
            customerConfidence = bestCustomerMatch?.score ?: 0f,
            customerAlternatives = customerAlternatives,
            matchedProduct = bestProductMatch?.entity,
            candidateProductName = candidateProductName.capitalizeWords(),
            productConfidence = bestProductMatch?.score ?: 0f,
            productAlternatives = productAlternatives,
            quantity = quantity,
            isFullyConfident = isFullyConfident
        )
    }

    private fun generateNGrams(tokens: List<String>): List<String> {
        val ngrams = mutableListOf<String>()
        val n = tokens.size
        for (len in 1..n) {
            for (i in 0..n - len) {
                ngrams.add(tokens.subList(i, i + len).joinToString(" "))
            }
        }
        return ngrams
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } }
    }
}
