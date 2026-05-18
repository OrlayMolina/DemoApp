package com.example.demoapp.data.repository

import android.util.Log
import com.example.demoapp.R
import com.example.demoapp.core.notifications.LocalNotifier
import com.example.demoapp.core.utils.ResourceProvider
import com.example.demoapp.data.model.AchievementUnlockDto
import com.example.demoapp.domain.model.Achievement
import com.example.demoapp.domain.model.AchievementType
import com.example.demoapp.domain.model.Notification
import com.example.demoapp.domain.model.NotificationType
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.repository.AchievementRepository
import com.example.demoapp.domain.repository.NotificationRepository
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
    private val touristPointRepository: TouristPointRepository,
    private val notificationRepository: NotificationRepository,
    private val resourceProvider: ResourceProvider,
    private val localNotifier: LocalNotifier
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
            // Solo publicaciones aprobadas cuentan para logros (no drafts, no pendientes, no rechazadas)
            val authoredPoints = points.filter {
                (it.authorId == userId || it.authorId == "user_$userId") &&
                    it.isVerified && !it.isRejected && !it.isDraft
            }

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
                    notifyAchievementUnlocked(type, now)
                } else if (!isUnlocked && dates[key] != null) {
                    // Logro ya no cumple criterios: limpia el doc obsoleto en Firestore.
                    // Asi cuando vuelva a cumplirse, dispara el unlock + notificacion de nuevo.
                    unlockDates.value = unlockDates.value - key
                    scope.launch {
                        runCatching {
                            firestore.collection(COLLECTION).document(key).delete().await()
                            Log.d(TAG, "Cleared stale achievement: $key")
                        }.onFailure { Log.e(TAG, "Error clearing stale achievement: ${it.message}", it) }
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
        val authoredPoints = touristPointRepository.touristPoints.value.filter {
            (it.authorId == userId || it.authorId == "user_$userId") &&
                it.isVerified && !it.isRejected && !it.isDraft
        }
        return computeProgress(type, authoredPoints).coerceAtMost(type.goal)
    }

    private fun notifyAchievementUnlocked(type: AchievementType, timestamp: Long) {
        val title = resourceProvider.getString(achievementTitleResId(type))
        val notification = Notification(
            id              = "",
            type            = NotificationType.ACHIEVEMENT,
            userName        = "",
            publicationTitle = title,
            date            = SimpleDateFormat("dd MMM, HH:mm", Locale("es")).format(Date(timestamp)),
            createdAt       = timestamp,
            isRead          = false,
            relatedEntityId = type.id
        )
        notificationRepository.add(notification)

        // System tray (mismo device): el unlock ocurre en el device del usuario afectado,
        // asi que podemos mostrar la notif del sistema sin pasar por FCM
        localNotifier.show(
            title = resourceProvider.getString(R.string.notifications_type_achievement),
            body  = resourceProvider.getString(R.string.notifications_body_achievement, title)
        )
    }

    private fun achievementTitleResId(type: AchievementType): Int = when (type) {
        AchievementType.NOVICE_EXPLORER          -> R.string.achievement_novice_explorer_title
        AchievementType.URBAN_PHOTOGRAPHER       -> R.string.achievement_urban_photographer_title
        AchievementType.LOCAL_INFLUENCER         -> R.string.achievement_local_influencer_title
        AchievementType.MASTER_EXPLORER          -> R.string.achievement_master_explorer_title
        AchievementType.ACTIVE_COMMUNITY_MEMBER  -> R.string.achievement_active_community_member_title
        AchievementType.VERIFIED_USER            -> R.string.achievement_verified_user_title
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
