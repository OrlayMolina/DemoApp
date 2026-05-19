// com/example/demoapp/features/notifications/NotificationsViewModel.kt
package com.example.demoapp.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.domain.model.Notification
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.model.UserRole
import com.example.demoapp.domain.repository.NotificationRepository
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = userRepository.currentUser

    val notifications: StateFlow<List<Notification>> = combine(
        notificationRepository.notifications,
        userRepository.currentUser
    ) { list, user ->
        if (user == null) emptyList()
        else list.filter { n -> isVisibleTo(n, user) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val unreadCount: StateFlow<Int> = combine(
        notifications,
        userRepository.currentUser
    ) { list, _ -> list.count { !it.isRead } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    private fun isVisibleTo(notification: Notification, user: User): Boolean {
        val recipient = notification.recipientUserId ?: return false
        if (recipient == user.id) return true
        if (recipient == Notification.RECIPIENT_MODERATORS && user.role == UserRole.ADMIN) return true
        return false
    }

    fun markAsRead(id: String)    = notificationRepository.markAsRead(id)
    fun markAllAsRead()           = notificationRepository.markAllAsRead()
    fun delete(id: String)        = notificationRepository.delete(id)
    fun deleteAll()               = notificationRepository.deleteAll()

    fun submitBanAppeal(appeal: String): Result<Unit> {
        val userId = userRepository.currentUser.value?.id
            ?: return Result.failure(IllegalStateException("Usuario no encontrado"))
        return userRepository.submitBanAppeal(userId, appeal)
    }
}