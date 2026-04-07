package com.example.demoapp.domain.repository

import com.example.demoapp.domain.model.Notification
import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {

    /** Stream reactivo de todas las notificaciones */
    val notifications: StateFlow<List<Notification>>

    /** Cantidad de no leídas (derivado del flow) */
    val unreadCount: StateFlow<Int>

    /** Marcar una notificación como leída */
    fun markAsRead(notificationId: String): Result<Unit>

    /** Marcar todas como leídas */
    fun markAllAsRead(): Result<Unit>

    /** Eliminar una notificación */
    fun delete(notificationId: String): Result<Unit>

    /** Eliminar todas las notificaciones */
    fun deleteAll(): Result<Unit>

    /** Agregar una notificación (ej. generada por acción de otro usuario) */
    fun add(notification: Notification): Result<Unit>

    /** Obtener notificaciones filtradas por tipo */
    fun getUnread(): List<Notification>
}