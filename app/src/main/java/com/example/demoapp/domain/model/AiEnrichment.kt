package com.example.demoapp.domain.model

data class AiEnrichment(
    val tags: List<String>,
    val improvedDescription: String?,
    val embedding: List<Double>
)
