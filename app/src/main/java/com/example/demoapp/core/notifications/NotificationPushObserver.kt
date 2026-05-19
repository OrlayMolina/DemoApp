package com.example.demoapp.core.notifications

import com.example.demoapp.R
import com.example.demoapp.core.utils.ResourceProvider
import com.example.demoapp.domain.model.Notification
import com.example.demoapp.domain.model.NotificationType
import com.example.demoapp.domain.model.UserRole
import com.example.demoapp.domain.repository.NotificationRepository
import com.example.demoapp.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Escucha el flujo de notificaciones en Firestore y dispara una notificacion
 * del sistema (LocalNotifier) cuando llega una nueva dirigida al usuario actual.
 * Esto cubre el caso de "alguien me sigue mientras estoy logueado" sin requerir FCM.
 */
@Singleton
class NotificationPushObserver @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val localNotifier: LocalNotifier,
    private val resourceProvider: ResourceProvider
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val handledIds = Collections.synchronizedSet(mutableSetOf<String>())
    private val observerStartTime = System.currentTimeMillis()

    init {
        scope.launch {
            notificationRepository.notifications.collect { list ->
                val currentUser = userRepository.currentUser.value ?: return@collect
                list.forEach { n ->
                    if (n.id.isBlank()) return@forEach
                    if (n.id in handledIds) return@forEach
                    if (n.createdAt <= observerStartTime) {
                        handledIds.add(n.id)
                        return@forEach
                    }
                    val recipient = n.recipientUserId ?: return@forEach
                    val isForMe = recipient == currentUser.id ||
                        (recipient == Notification.RECIPIENT_MODERATORS && currentUser.role == UserRole.ADMIN)
                    if (isForMe) {
                        handledIds.add(n.id)
                        localNotifier.show(formatTitle(n), formatBody(n))
                    }
                }
            }
        }
    }

    private fun formatTitle(n: Notification): String = when (n.type) {
        NotificationType.LIKE             -> resourceProvider.getString(R.string.notifications_type_like)
        NotificationType.COMMENT          -> resourceProvider.getString(R.string.notifications_type_comment)
        NotificationType.FOLLOWER         -> resourceProvider.getString(R.string.notifications_type_follower)
        NotificationType.VERIFIED         -> resourceProvider.getString(R.string.notifications_type_verified)
        NotificationType.NEW_PUBLICATION  -> resourceProvider.getString(R.string.notifications_type_new_publication)
        NotificationType.REJECTED         -> resourceProvider.getString(R.string.notifications_type_rejected)
        NotificationType.REVIEW_REMINDER  -> resourceProvider.getString(R.string.notifications_type_review_reminder)
        NotificationType.ACHIEVEMENT      -> resourceProvider.getString(R.string.notifications_type_achievement)
        NotificationType.BAN              -> resourceProvider.getString(R.string.notifications_type_ban)
        NotificationType.BAN_APPEAL       -> resourceProvider.getString(R.string.notifications_type_ban_appeal)
        NotificationType.BAN_LIFTED       -> resourceProvider.getString(R.string.notifications_type_ban_lifted)
    }

    private fun formatBody(n: Notification): String = when (n.type) {
        NotificationType.LIKE ->
            resourceProvider.getString(R.string.notifications_body_like, n.userName, n.publicationTitle.orEmpty())
        NotificationType.COMMENT ->
            resourceProvider.getString(R.string.notifications_body_comment, n.userName, n.publicationTitle.orEmpty())
        NotificationType.FOLLOWER ->
            resourceProvider.getString(R.string.notifications_body_follower, n.userName)
        NotificationType.VERIFIED ->
            resourceProvider.getString(R.string.notifications_body_verified, n.publicationTitle.orEmpty())
        NotificationType.NEW_PUBLICATION ->
            resourceProvider.getString(R.string.notifications_body_new_publication, n.userName, n.publicationTitle.orEmpty())
        NotificationType.REJECTED ->
            resourceProvider.getString(R.string.notifications_body_rejected, n.publicationTitle.orEmpty())
        NotificationType.REVIEW_REMINDER ->
            resourceProvider.getString(R.string.notifications_body_review_reminder)
        NotificationType.ACHIEVEMENT ->
            resourceProvider.getString(R.string.notifications_body_achievement, n.publicationTitle.orEmpty())
        NotificationType.BAN ->
            resourceProvider.getString(R.string.notifications_body_ban, n.publicationTitle.orEmpty())
        NotificationType.BAN_APPEAL ->
            resourceProvider.getString(R.string.notifications_body_ban_appeal, n.userName)
        NotificationType.BAN_LIFTED ->
            resourceProvider.getString(R.string.notifications_body_ban_lifted)
    }
}
