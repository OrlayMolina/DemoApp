package com.example.demoapp.data.repository

import android.util.Log
import com.example.demoapp.data.model.ReviewHistoryDto
import com.example.demoapp.domain.model.ReviewAction
import com.example.demoapp.domain.model.ReviewHistory
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.repository.ReviewHistoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ReviewRepoImpl"
private const val COLLECTION = "reviews"

@Singleton
class ReviewHistoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReviewHistoryRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _history = MutableStateFlow<List<ReviewHistory>>(emptyList())
    override val history: StateFlow<List<ReviewHistory>> = _history.asStateFlow()

    init {
        observeReviews()
    }

    private fun observeReviews() {
        firestore.collection(COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to reviews: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        runCatching {
                            val dto = doc.toObject(ReviewHistoryDto::class.java)
                            dto?.copy(id = doc.id)?.toDomain()
                        }.onFailure { Log.e(TAG, "Error mapping doc", it) }.getOrNull()
                    }.sortedByDescending { it.reviewedAt }
                    _history.value = list
                }
            }
    }

    override fun seedFromPoints(points: List<TouristPoint>) {
        if (_history.value.isNotEmpty()) return
        val newReviews = points
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
        
        scope.launch {
            val batch = firestore.batch()
            newReviews.forEach { review ->
                val idToUse = if (review.id.toLongOrNull() != null) "review_${review.id}" else review.id
                val ref = firestore.collection(COLLECTION).document(idToUse)
                batch.set(ref, ReviewHistoryDto.fromDomain(review.copy(id = idToUse)))
            }
            runCatching { batch.commit().await() }
                .onFailure { Log.e(TAG, "Error seeding reviews: ${it.message}", it) }
        }
    }

    override fun recordApproval(point: TouristPoint, reviewedBy: String, reviewedAt: Long) {
        upsert(ReviewHistory(point.id, point.title, point.category, reviewedBy, reviewedAt, ReviewAction.APPROVED))
    }

    override fun recordRejection(point: TouristPoint, reviewedBy: String, reason: String, reviewedAt: Long) {
        upsert(ReviewHistory(point.id, point.title, point.category, reviewedBy, reviewedAt, ReviewAction.REJECTED, reason.trim()))
    }

    private fun upsert(item: ReviewHistory) {
        scope.launch {
            runCatching {
                val data = ReviewHistoryDto.fromDomain(item)
                val idToUse = if (item.id.toLongOrNull() != null) "review_${item.id}" else item.id
                firestore.collection(COLLECTION).document(idToUse).set(data.copy(id = idToUse)).await()
            }.onFailure { Log.e(TAG, "Error upserting review: ${it.message}", it) }
        }
    }
}
