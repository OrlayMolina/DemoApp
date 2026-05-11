package com.example.demoapp.data.repository

import android.util.Log
import com.example.demoapp.data.model.AchievementUnlockDto
import com.example.demoapp.domain.model.Achievement
import com.example.demoapp.domain.model.AchievementType
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.repository.AchievementRepository
import com.example.demoapp.domain.repository.TouristPointRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AchievementRepoImpl"
private const val COLLECTION = "achievements"

@Singleton
class AchievementRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val touristPointRepository: TouristPointRepository
) : AchievementRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val unlockDates = MutableStateFlow<Map<String, Long>>(emptyMap())

    init {
        observeAchievements()
    }

    private fun observeAchievements() {
        firestore.collection(COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to achievements: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val map = snapshot.documents.mapNotNull { doc ->
                        runCatching {
                            doc.toObject(AchievementUnlockDto::class.java)
                        }.getOrNull()
                    }.associate { "${it.userId}__${it.achievementId}" to it.unlockedAt }
                    unlockDates.value = map
                }
            }
    }

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
                    val now = System.currentTimeMillis()
                    unlockDates.value = unlockDates.value + (key to now)
                    scope.launch {
                        runCatching {
                            val dto = AchievementUnlockDto(
                                id = key,
                                userId = userId,
                                achievementId = type.id,
                                unlockedAt = now
                            )
                            firestore.collection(COLLECTION).document(key).set(dto).await()
                        }.onFailure { Log.e(TAG, "Error saving achievement: ${it.message}", it) }
                    }
                }

                val currentDates = unlockDates.value
                Achievement(
                    id = type.id,
                    title = "", // Replaced by UI resources
                    description = "",
                    icon = type.icon,
                    isUnlocked = isUnlocked,
                    unlockedDate = if (isUnlocked) formatDate(currentDates[key]) else null,
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
