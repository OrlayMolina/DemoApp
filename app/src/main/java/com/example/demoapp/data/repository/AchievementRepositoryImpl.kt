package com.example.demoapp.data.repository

import com.example.demoapp.domain.model.Achievement
import com.example.demoapp.domain.model.AchievementType
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.repository.AchievementRepository
import com.example.demoapp.domain.repository.TouristPointRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepositoryImpl @Inject constructor(
    private val touristPointRepository: TouristPointRepository
) : AchievementRepository {

    private val unlockDates = MutableStateFlow<Map<String, Long>>(emptyMap())

    override fun observeAchievements(userId: String): Flow<List<Achievement>> {
        return combine(
            touristPointRepository.touristPoints,
            unlockDates
        ) { points, dates ->
            val authoredPoints = points.filter { it.authorId == userId || it.authorId == "user_$userId" }

            AchievementType.values().map { type ->
                val progress = computeProgress(type, authoredPoints).coerceAtMost(type.goal)
                val isUnlocked = progress >= type.goal
                val key = unlockKey(userId, type)

                if (isUnlocked && dates[key] == null) {
                    unlockDates.value = unlockDates.value + (key to System.currentTimeMillis())
                }

                Achievement(
                    id = type.id,
                    title = "",
                    description = "",
                    icon = type.icon,
                    isUnlocked = isUnlocked,
                    unlockedDate = if (isUnlocked) formatDate(unlockDates.value[key]) else null,
                    progress = progress,
                    goal = type.goal
                )
            }
        }
    }

    override fun progressOf(userId: String, type: AchievementType): Int {
        val authoredPoints = touristPointRepository.touristPoints.value
            .filter { it.authorId == userId || it.authorId == "user_$userId" }
        return computeProgress(type, authoredPoints).coerceAtMost(type.goal)
    }

    private fun computeProgress(type: AchievementType, authoredPoints: List<TouristPoint>): Int {
        return when (type) {
            AchievementType.NOVICE_EXPLORER -> authoredPoints.size
            AchievementType.URBAN_PHOTOGRAPHER -> authoredPoints.count { it.photoUrls.isNotEmpty() }
            AchievementType.LOCAL_INFLUENCER -> authoredPoints.sumOf { it.importantVotes }
            AchievementType.MASTER_EXPLORER -> authoredPoints.size
            AchievementType.ACTIVE_COMMUNITY_MEMBER -> authoredPoints.sumOf { it.importantVotes }
            AchievementType.VERIFIED_USER -> authoredPoints.count { it.isVerified && !it.isRejected }
        }
    }

    private fun unlockKey(userId: String, type: AchievementType): String = "${userId}__${type.id}"

    private fun formatDate(timestamp: Long?): String? {
        if (timestamp == null) return null
        val formatter = SimpleDateFormat("d 'of' MMMM 'of' yyyy", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
}
