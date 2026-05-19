package com.example.demoapp.data.repository

import android.util.Log
import com.example.demoapp.data.model.NotificationDto
import com.example.demoapp.domain.model.Notification
import com.example.demoapp.domain.repository.NotificationRepository
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

private const val TAG = "NotificationRepoImpl"
private const val COLLECTION = "notifications"

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    override val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    override val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        firestore.collection(COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to notifications: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        runCatching {
                            val dto = doc.toObject(NotificationDto::class.java)
                            // Firestore reflexion borra el prefijo "is" en booleans de Kotlin
                            // (la doc puede tener "read" en vez de "isRead"). Releemos a mano.
                            val isRead = doc.getBoolean("isRead") ?: doc.getBoolean("read") ?: false
                            dto?.copy(id = doc.id, isRead = isRead)?.toDomain()
                        }.getOrNull()
                    }.sortedByDescending { it.createdAt }

                    _notifications.value = list
                    _unreadCount.value = list.count { !it.isRead }
                }
            }
    }

    override fun markAsRead(notificationId: String): Result<Unit> {
        val current = _notifications.value.find { it.id == notificationId }
        if (current == null) return Result.failure(NoSuchElementException("Notification $notificationId not found"))
        
        scope.launch {
            runCatching {
                firestore.collection(COLLECTION).document(notificationId)
                    .set(NotificationDto.fromDomain(current.copy(isRead = true))).await()
            }.onFailure { Log.e(TAG, "Error marking notification read: ${it.message}", it) }
        }
        return Result.success(Unit)
    }

    override fun markAllAsRead(): Result<Unit> {
        val unread = _notifications.value.filter { !it.isRead }
        scope.launch {
            runCatching {
                val batch = firestore.batch()
                unread.forEach { n ->
                    val ref = firestore.collection(COLLECTION).document(n.id)
                    batch.set(ref, NotificationDto.fromDomain(n.copy(isRead = true)))
                }
                batch.commit().await()
            }.onFailure { Log.e(TAG, "Error marking all read: ${it.message}", it) }
        }
        return Result.success(Unit)
    }

    override fun delete(notificationId: String): Result<Unit> {
        scope.launch {
            runCatching {
                firestore.collection(COLLECTION).document(notificationId).delete().await()
            }.onFailure { Log.e(TAG, "Error deleting notification: ${it.message}", it) }
        }
        return Result.success(Unit)
    }

    override fun deleteAll(): Result<Unit> {
        val all = _notifications.value
        scope.launch {
            runCatching {
                val batch = firestore.batch()
                all.forEach { n ->
                    val ref = firestore.collection(COLLECTION).document(n.id)
                    batch.delete(ref)
                }
                batch.commit().await()
            }.onFailure { Log.e(TAG, "Error deleting all: ${it.message}", it) }
        }
        return Result.success(Unit)
    }

    override fun add(notification: Notification): Result<Unit> {
        val current = _notifications.value
        if (current.any { it.id == notification.id })
            return Result.failure(IllegalArgumentException("Notification ${notification.id} already exists"))

        scope.launch {
            runCatching {
                val docRef = firestore.collection(COLLECTION).document()
                val newId = docRef.id
                firestore.collection(COLLECTION).document(newId).set(NotificationDto.fromDomain(notification.copy(id = newId))).await()
            }.onFailure { Log.e(TAG, "Error adding notification: ${it.message}", it) }
        }
        return Result.success(Unit)
    }

    override fun getUnread(): List<Notification> =
        _notifications.value.filter { !it.isRead }
}