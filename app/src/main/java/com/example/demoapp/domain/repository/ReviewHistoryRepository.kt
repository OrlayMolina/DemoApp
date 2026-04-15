package com.example.demoapp.domain.repository

import com.example.demoapp.domain.model.ReviewHistory
import com.example.demoapp.domain.model.TouristPoint
import kotlinx.coroutines.flow.StateFlow

interface ReviewHistoryRepository {

    val history: StateFlow<List<ReviewHistory>>

    fun seedFromPoints(points: List<TouristPoint>)

    fun recordApproval(
        point: TouristPoint,
        reviewedBy: String,
        reviewedAt: Long = System.currentTimeMillis()
    )

    fun recordRejection(
        point: TouristPoint,
        reviewedBy: String,
        reason: String,
        reviewedAt: Long = System.currentTimeMillis()
    )
}
