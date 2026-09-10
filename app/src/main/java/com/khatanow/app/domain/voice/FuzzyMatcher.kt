package com.khatanow.app.domain.voice

import kotlin.math.max
import kotlin.math.min

object FuzzyMatcher {
    
    fun calculateSimilarity(s1: String, s2: String): Float {
        val str1 = s1.lowercase().trim()
        val str2 = s2.lowercase().trim()
        
        if (str1 == str2) return 1.0f
        if (str1.isEmpty() || str2.isEmpty()) return 0.0f
        
        // Exact substring check boost
        if (str1.contains(str2) || str2.contains(str1)) {
            val minLen = min(str1.length, str2.length).toFloat()
            val maxLen = max(str1.length, str2.length).toFloat()
            return 0.85f + (0.15f * (minLen / maxLen))
        }

        // Levenshtein distance
        val levDistance = levenshteinDistance(str1, str2)
        val maxLen = max(str1.length, str2.length)
        val levScore = 1.0f - (levDistance.toFloat() / maxLen.toFloat())
        
        return max(0f, levScore)
    }

    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length
        
        var cost = IntArray(lhsLength + 1) { it }
        var newCost = IntArray(lhsLength + 1)
        
        for (i in 1..rhsLength) {
            newCost[0] = i
            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = min(min(costInsert, costDelete), costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLength]
    }
}
