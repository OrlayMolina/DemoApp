package com.example.demoapp.core.utils

import kotlin.math.sqrt

fun cosineSimilarity(a: List<Double>, b: List<Double>): Double {
    if (a.isEmpty() || b.isEmpty() || a.size != b.size) return 0.0
    var dot = 0.0
    var normA = 0.0
    var normB = 0.0
    for (i in a.indices) {
        val x = a[i]
        val y = b[i]
        dot += x * y
        normA += x * x
        normB += y * y
    }
    val denom = sqrt(normA) * sqrt(normB)
    return if (denom == 0.0) 0.0 else dot / denom
}
