package com.khatanow.app.domain.voice

import com.khatanow.app.data.local.entities.CustomerEntity
import com.khatanow.app.data.local.entities.ProductEntity

data class ParsedVoiceItem(
    val matchedProduct: ProductEntity? = null,
    val candidateProductName: String,
    val quantity: Int = 1
)

data class VoiceParseResult(
    val rawText: String,
    val matchedCustomer: CustomerEntity? = null,
    val candidateCustomerName: String = "",
    val items: List<ParsedVoiceItem> = emptyList()
)

class LocalVoiceParser {

    private val fillerWords = setOf(
        "ko", "ne", "ka", "ki", "ke", "gave", "took", "take", "give", "wanted",
        "wants", "piece", "pieces", "pkt", "packet", "packets", "kg", "bottle", "bottles",
        "को", "ने", "का", "की", "के", "ने लिया", "लिया", "दिया"
    )

    private val conjunctions = setOf("and", "aur", "और", "+", ",")

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

        // 1. Find Best Customer Match across all n-grams
        val customerNGrams = generateNGrams(originalTokens)
        val customerMatches = mutableListOf<MatchedEntity<CustomerEntity>>()
        for (customer in customers) {
            var maxScore = 0f
            var bestMatchedText = ""
            for (ngram in customerNGrams) {
                val score = FuzzyMatcher.calculateSimilarity(ngram, customer.name)
                if (score > maxScore) {
                    maxScore = score
                    bestMatchedText = ngram
                }
            }
            if (maxScore > 0.45f) {
                customerMatches.add(MatchedEntity(customer, bestMatchedText, maxScore))
            }
        }
        customerMatches.sortByDescending { it.score }
        val bestCustomerMatch = customerMatches.firstOrNull()

        // 2. Filter out Customer tokens from remaining token stream
        val remainingTokens = if (bestCustomerMatch != null && bestCustomerMatch.score > 0.65f) {
            originalTokens.filter { !bestCustomerMatch.matchedText.lowercase().contains(it.lowercase()) }
        } else {
            // First word assumed candidate customer if not matched
            originalTokens.drop(1)
        }

        val candidateCustomerName = if (bestCustomerMatch != null && bestCustomerMatch.score > 0.65f) {
            bestCustomerMatch.entity.name
        } else {
            originalTokens.firstOrNull() ?: "Customer"
        }

        // 3. Segment remaining tokens into multiple item chunks (e.g. ["3", "Parle-G", "and", "2", "Sugar"])
        val itemSegments = segmentItemTokens(remainingTokens)
        val parsedItems = mutableListOf<ParsedVoiceItem>()

        for (segment in itemSegments) {
            if (segment.isEmpty()) continue

            // Extract Quantity from segment
            val extractedQty = NumberParser.extractQuantity(segment)
            val quantity = extractedQty?.quantity ?: 1

            // Clean product tokens
            val productTokens = segment.filterIndexed { idx, t ->
                val clean = t.lowercase().replace(Regex("[^a-z0-9]"), "")
                idx != extractedQty?.tokenIndex && !fillerWords.contains(clean) && !conjunctions.contains(clean)
            }

            if (productTokens.isEmpty()) continue
            val productPhrase = productTokens.joinToString(" ")

            // Match against Product DB
            val productNGrams = generateNGrams(productTokens)
            var bestProductMatch: ProductEntity? = null
            var maxProductScore = 0f

            for (prod in products) {
                val fullScore = FuzzyMatcher.calculateSimilarity(productPhrase, prod.name)
                if (fullScore > maxProductScore) {
                    maxProductScore = fullScore
                    bestProductMatch = prod
                }
                for (ngram in productNGrams) {
                    val score = FuzzyMatcher.calculateSimilarity(ngram, prod.name)
                    if (score > maxProductScore) {
                        maxProductScore = score
                        bestProductMatch = prod
                    }
                }
            }

            val finalMatchedProduct = if (maxProductScore >= 0.55f) bestProductMatch else null
            val candidateProdName = finalMatchedProduct?.name ?: productPhrase.capitalizeWords()

            parsedItems.add(
                ParsedVoiceItem(
                    matchedProduct = finalMatchedProduct,
                    candidateProductName = candidateProdName,
                    quantity = quantity
                )
            )
        }

        // Fallback default single item if segmenter found 0
        if (parsedItems.isEmpty()) {
            parsedItems.add(
                ParsedVoiceItem(
                    matchedProduct = null,
                    candidateProductName = "Product",
                    quantity = 1
                )
            )
        }

        return VoiceParseResult(
            rawText = rawTrimmed,
            matchedCustomer = bestCustomerMatch?.entity,
            candidateCustomerName = candidateCustomerName.capitalizeWords(),
            items = parsedItems
        )
    }

    private fun segmentItemTokens(tokens: List<String>): List<List<String>> {
        val segments = mutableListOf<MutableList<String>>()
        var currentSegment = mutableListOf<String>()

        for (token in tokens) {
            val clean = token.lowercase().replace(Regex("[^a-z0-9]"), "")
            
            // Check if token is conjunction ("and", "aur") or a new number starting a new item
            val isConjunction = conjunctions.contains(clean)
            val isNumber = NumberParser.extractQuantity(listOf(token)) != null

            if (isConjunction) {
                if (currentSegment.isNotEmpty()) {
                    segments.add(currentSegment)
                    currentSegment = mutableListOf()
                }
            } else if (isNumber && currentSegment.any { NumberParser.extractQuantity(listOf(it)) == null }) {
                // New quantity token starting next item e.g. "3 Parle-G 2 Sugar"
                if (currentSegment.isNotEmpty()) {
                    segments.add(currentSegment)
                    currentSegment = mutableListOf()
                }
                currentSegment.add(token)
            } else {
                currentSegment.add(token)
            }
        }

        if (currentSegment.isNotEmpty()) {
            segments.add(currentSegment)
        }

        return segments
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
