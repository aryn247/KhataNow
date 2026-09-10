package com.khatanow.app.domain.voice

object NumberParser {
    private val numberWords = mapOf(
        // English
        "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
        "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10,
        "eleven" to 11, "twelve" to 12, "dozen" to 12, "twenty" to 20, "fifty" to 50, "hundred" to 100,
        
        // Hinglish
        "ek" to 1, "aik" to 1, "do" to 2, "doo" to 2, "teen" to 3, "tin" to 3,
        "chaar" to 4, "char" to 4, "paanch" to 5, "panch" to 5, "chhe" to 6, "che" to 6,
        "saat" to 7, "sat" to 7, "aath" to 8, "ath" to 8, "nau" to 9, "no" to 9,
        "das" to 10, "gyarah" to 11, "baarah" to 12, "darjan" to 12, "beese" to 20,

        // Devanagari Hindi Script
        "एक" to 1, "दो" to 2, "तीन" to 3, "चार" to 4, "पांच" to 5, "पाँच" to 5,
        "छह" to 6, "छः" to 6, "सात" to 7, "आठ" to 8, "नौ" to 9, "दस" to 10,
        "ग्यारह" to 11, "बारह" to 12, "दर्जन" to 12, "बीस" to 20, "पचास" to 50, "सौ" to 100,
        "१" to 1, "२" to 2, "३" to 3, "४" to 4, "५" to 5, "६" to 6, "७" to 7, "८" to 8, "९" to 9, "१०" to 10
    )

    data class ExtractedQuantity(
        val quantity: Int,
        val tokenIndex: Int,
        val originalToken: String
    )

    fun extractQuantity(tokens: List<String>): ExtractedQuantity? {
        for ((index, token) in tokens.withIndex()) {
            val cleanToken = token.lowercase().trim().replace(Regex("[^a-z0-9अ-ह१-९]"), "")
            
            // Check direct digits e.g. "5", "12"
            val digit = cleanToken.toIntOrNull()
            if (digit != null && digit in 1..1000) {
                return ExtractedQuantity(digit, index, token)
            }
            
            // Check number words & Devanagari numbers
            val wordNumber = numberWords[cleanToken] ?: numberWords[token.trim()]
            if (wordNumber != null) {
                return ExtractedQuantity(wordNumber, index, token)
            }
        }
        return null
    }
}
