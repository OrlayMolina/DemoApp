package com.example.demoapp.data.model

import com.example.demoapp.domain.model.ReviewAction
import com.example.demoapp.domain.model.ReviewHistory
import com.example.demoapp.domain.model.TouristPointCategory

data class ReviewHistoryDto(
    val id: String = "",
    val pointTitle: String = "",
    val categoryName: String = "",
    val reviewedBy: String = "",
    val reviewedAt: Long = 0L,
    val actionName: String = "",
    val rejectionReason: String? = null
) {
    fun toDomain(): ReviewHistory = ReviewHistory(
        id = id,
        pointTitle = pointTitle,
        category = try { TouristPointCategory.valueOf(categoryName) } catch (e: Exception) { TouristPointCategory.NATURE },
        reviewedBy = reviewedBy,
        reviewedAt = reviewedAt,
        action = try { ReviewAction.valueOf(actionName) } catch (e: Exception) { ReviewAction.APPROVED },
        rejectionReason = rejectionReason
    )

    companion object {
        fun fromDomain(domain: ReviewHistory): ReviewHistoryDto = ReviewHistoryDto(
            id = domain.id,
            pointTitle = domain.pointTitle,
            categoryName = domain.category.name,
            reviewedBy = domain.reviewedBy,
            reviewedAt = domain.reviewedAt,
            actionName = domain.action.name,
            rejectionReason = domain.rejectionReason
        )
    }
}
