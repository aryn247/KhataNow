package com.khatanow.app.domain.voice

import com.khatanow.app.data.local.entities.CustomerEntity
import com.khatanow.app.data.local.entities.ProductEntity

class LocalVoiceParser {

    private val fillerWords = setOf(
        "ko", "ne", "ka", "ki", "ke", "gave", "took", "take", "give", "wanted",
        "wants", "piece", "pieces", "pkt", "packet", "packets", "kg", "bottle", "bottles"
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

        // 2. Remove quantity token and filler words from remaining phrase
        val remainingTokens = originalTokens.filterIndexed { index, token ->
            val clean = token.lowercase().replace(Regex("[^a-z0-9]"), "")
            index != extractedQty?.tokenIndex && !fillerWords.contains(clean)
        }

        val remainingPhrase = remainingTokens.joinToString(" ")

        // 3. Generate candidate n-grams for entity matching
        val nGrams = generateNGrams(remainingTokens)

        // 4. Find Best Customer Match
        val customerMatches = mutableListOf<MatchedEntity<CustomerEntity>>()
        for (customer in customers) {
            var maxScore = 0f
            var bestMatchedText = ""
            
            // Match against whole remaining phrase first
            val fullScore = FuzzyMatcher.calculateSimilarity(remainingPhrase, customer.name)
            if (fullScore > maxScore) {
                maxScore = fullScore
                bestMatchedText = remainingPhrase
            }

            // Match against n-grams
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

        // 5. Find Best Product Match (Exclude tokens matched to Customer if score is high)
        val productTokens = if (bestCustomerMatch != null && bestCustomerMatch.score > 0.7f) {
            remainingTokens.filter { !bestCustomerMatch.matchedText.lowercase().contains(it.lowercase()) }
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

        val isFullyConfident = (bestCustomerMatch?.score ?: 0f) >= 0.75f &&
                                (bestProductMatch?.score ?: 0f) >= 0.75f

        return VoiceParseResult(
            rawText = rawTrimmed,
            matchedCustomer = bestCustomerMatch?.entity,
            customerConfidence = bestCustomerMatch?.score ?: 0f,
            customerAlternatives = customerAlternatives,
            matchedProduct = bestProductMatch?.entity,
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
}
