// com/example/demoapp/features/notifications/NotificationsViewModel.kt
package com.example.demoapp.features.notifications

import androidx.lifecycle.ViewModel
import com.example.demoapp.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val notifications = notificationRepository.notifications
    val unreadCount   = notificationRepository.unreadCount

    fun markAsRead(id: String)    = notificationRepository.markAsRead(id)
    fun markAllAsRead()           = notificationRepository.markAllAsRead()
    fun delete(id: String)        = notificationRepository.delete(id)
    fun deleteAll()               = notificationRepository.deleteAll()
}