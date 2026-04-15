package com.example.demoapp.data.repository
import com.example.demoapp.domain.model.ReviewAction
import com.example.demoapp.domain.model.ReviewHistory
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.repository.ReviewHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class ReviewHistoryRepositoryImpl @Inject constructor() : ReviewHistoryRepository {
    private val _history = MutableStateFlow<List<ReviewHistory>>(emptyList())
    override val history: StateFlow<List<ReviewHistory>> = _history.asStateFlow()
    override fun seedFromPoints(points: List<TouristPoint>) {
        if (_history.value.isNotEmpty()) return
        _history.value = points
            .filter { it.isVerified || it.isRejected }
            .map { point ->
                ReviewHistory(
                    id = point.id,
                    pointTitle = point.title,
                    category = point.category,
                    reviewedBy = "Sistema",
                    reviewedAt = point.createdAt,
                    action = if (point.isRejected) ReviewAction.REJECTED else ReviewAction.APPROVED,
                    rejectionReason = point.rejectionReason
                )
            }
            .sortedByDescending { it.reviewedAt }
    }
    override fun recordApproval(point: TouristPoint, reviewedBy: String, reviewedAt: Long) {
        upsert(ReviewHistory(point.id, point.title, point.category, reviewedBy, reviewedAt, ReviewAction.APPROVED))
    }
    override fun recordRejection(point: TouristPoint, reviewedBy: String, reason: String, reviewedAt: Long) {
        upsert(ReviewHistory(point.id, point.title, point.category, reviewedBy, reviewedAt, ReviewAction.REJECTED, reason.trim()))
    }
    private fun upsert(item: ReviewHistory) {
        val updated = _history.value.toMutableList()
        val index = updated.indexOfFirst { it.id == item.id }
        if (index >= 0) updated[index] = item else updated.add(item)
        _history.value = updated.sortedByDescending { it.reviewedAt }
    }
}
