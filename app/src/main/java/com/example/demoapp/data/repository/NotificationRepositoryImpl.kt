// com/example/demoapp/data/repository/NotificationRepositoryImpl.kt
package com.example.demoapp.data.repository

import com.example.demoapp.domain.model.Notification
import com.example.demoapp.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {

    private val _notifications = MutableStateFlow<List<Notification>>(
        Notification.SAMPLE_LIST
    )
    override val notifications: StateFlow<List<Notification>> =
        _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(
        Notification.SAMPLE_LIST.count { !it.isRead }
    )
    override val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    // ── Helpers privados ────────────────────────────────────────────────────

    private fun refresh(updated: List<Notification>) {
        _notifications.value = updated
        _unreadCount.value   = updated.count { !it.isRead }
    }

    // ── Implementación ──────────────────────────────────────────────────────

    override fun markAsRead(notificationId: String): Result<Unit> {
        val current = _notifications.value
        val exists  = current.any { it.id == notificationId }
        if (!exists) return Result.failure(NoSuchElementException("Notification $notificationId not found"))

        refresh(current.map { if (it.id == notificationId) it.copy(isRead = true) else it })
        return Result.success(Unit)
    }

    override fun markAllAsRead(): Result<Unit> {
        refresh(_notifications.value.map { it.copy(isRead = true) })
        return Result.success(Unit)
    }

    override fun delete(notificationId: String): Result<Unit> {
        val current = _notifications.value
        val exists  = current.any { it.id == notificationId }
        if (!exists) return Result.failure(NoSuchElementException("Notification $notificationId not found"))

        refresh(current.filter { it.id != notificationId })
        return Result.success(Unit)
    }

    override fun deleteAll(): Result<Unit> {
        refresh(emptyList())
        return Result.success(Unit)
    }

    override fun add(notification: Notification): Result<Unit> {
        val current = _notifications.value
        if (current.any { it.id == notification.id })
            return Result.failure(IllegalArgumentException("Notification ${notification.id} already exists"))

        refresh(listOf(notification) + current)   // más reciente al inicio
        return Result.success(Unit)
    }

    override fun getUnread(): List<Notification> =
        _notifications.value.filter { !it.isRead }
}