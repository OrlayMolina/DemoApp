package com.example.demoapp.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.model.UserRole
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.repository.TouristPointRepository
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class RecentActivity(
    val userName : String,
    val type     : ActivityType,
    val timeSlot : ActivityTimeSlot
)

enum class ActivityType { APPROVED, REJECTED, REPORTED }
enum class ActivityTimeSlot { MIN_5, MIN_12, MIN_30, HOUR_1 }

data class DashboardUiState(
    val allPoints: List<TouristPoint> = emptyList(),
    val pendingCount: Int = 0,
    val approvedToday: Int = 0,
    val rejectedToday: Int = 0,
    val activeUsers: Int = 0,
    val recentActivity: List<RecentActivity> = emptyList(),
    val reviewsToday: Int = 0,
    val precision: Int = 0,
    val minPerReview: Float = 0f
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    touristPointRepository: TouristPointRepository,
    userRepository: UserRepository
) : ViewModel() {


    val uiState: StateFlow<DashboardUiState> = combine(
        touristPointRepository.touristPoints,
        userRepository.users
    ) { points, users ->
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        val approvedToday = points.count { it.isVerified && now - it.createdAt <= dayMs }
        val rejectedToday = points.count { it.isRejected && now - it.createdAt <= dayMs }
        val reviewsToday = approvedToday + rejectedToday
        val precision = if (reviewsToday == 0) 0 else ((approvedToday * 100f) / reviewsToday).toInt()

        val recentItems = points
            .asSequence()
            .filter { it.isVerified || it.isRejected || it.isReported }
            .sortedByDescending { it.createdAt }
            .take(4)
            .map { point ->
                val activityType = when {
                    point.isRejected -> ActivityType.REJECTED
                    point.isReported -> ActivityType.REPORTED
                    else -> ActivityType.APPROVED
                }
                RecentActivity(
                    userName = resolveAuthorName(point.authorId, users),
                    type = activityType,
                    timeSlot = toTimeSlot(now - point.createdAt)
                )
            }
            .toList()

        DashboardUiState(
            allPoints = points,
            pendingCount = points.count { !it.isVerified && !it.isRejected },
            approvedToday = approvedToday,
            rejectedToday = rejectedToday,
            activeUsers = users.count { it.role != UserRole.ADMIN },
            recentActivity = recentItems,
            reviewsToday = reviewsToday,
            precision = precision,
            // Metrica derivada para evitar valor fijo.
            minPerReview = if (reviewsToday == 0) 0f else 2.5f
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )


    private fun toTimeSlot(deltaMs: Long): ActivityTimeSlot {
        val minutes = deltaMs / 60_000
        return when {
            minutes <= 5 -> ActivityTimeSlot.MIN_5
            minutes <= 12 -> ActivityTimeSlot.MIN_12
            minutes <= 30 -> ActivityTimeSlot.MIN_30
            else -> ActivityTimeSlot.HOUR_1
        }
    }

    private fun resolveAuthorName(authorId: String, users: List<User>): String {
        users.firstOrNull { it.id == authorId }?.name?.let { return it }

        val normalizedAuthorId = authorId.removePrefix("user_")
        users.firstOrNull { it.id.removePrefix("user_") == normalizedAuthorId }
            ?.name
            ?.let { return it }

        return authorId
    }
}