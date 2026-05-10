package com.example.demoapp.domain.repository

import com.example.demoapp.domain.model.AiEnrichment
import com.example.demoapp.domain.model.TouristPointCategory

interface AiRepository {
    suspend fun enrichPoint(
        title: String,
        description: String,
        category: TouristPointCategory,
        imageLabels: List<String>
    ): Result<AiEnrichment>

    suspend fun embedQuery(query: String): Result<List<Double>>
}
