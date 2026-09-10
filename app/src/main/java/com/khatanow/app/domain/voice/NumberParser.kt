package com.khatanow.app.domain.voice

object NumberParser {
    private val numberWords = mapOf(
        // English
        "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
        "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10,
        "eleven" to 11, "twelve" to 12, "dozen" to 12, "twenty" to 20, "fifty" to 50, "hundred" to 100,
        
        // Hindi / Hinglish
        "ek" to 1, "aik" to 1, "do" to 2, "doo" to 2, "teen" to 3, "tin" to 3,
        "chaar" to 4, "char" to 4, "paanch" to 5, "panch" to 5, "chhe" to 6, "che" to 6,
        "saat" to 7, "sat" to 7, "aath" to 8, "ath" to 8, "nau" to 9, "no" to 9,
        "das" to 10, "gyarah" to 11, "baarah" to 12, "darjan" to 12, "beese" to 20
    )

    data class ExtractedQuantity(
        val quantity: Int,
        val tokenIndex: Int,
        val originalToken: String
    )

    fun extractQuantity(tokens: List<String>): ExtractedQuantity? {
        for ((index, token) in tokens.withIndex()) {
            val cleanToken = token.lowercase().trim().replace(Regex("[^a-z0-9]"), "")
            
            // Check direct digits e.g. "5", "12"
            val digit = cleanToken.toIntOrNull()
            if (digit != null && digit in 1..1000) {
                return ExtractedQuantity(digit, index, token)
            }
            
            // Check number words
            val wordNumber = numberWords[cleanToken]
            if (wordNumber != null) {
                return ExtractedQuantity(wordNumber, index, token)
            }
        }
        return null
    }
}
