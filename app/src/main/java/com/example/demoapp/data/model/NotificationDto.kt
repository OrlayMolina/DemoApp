package com.example.demoapp.data.model

import com.example.demoapp.domain.model.Notification
import com.example.demoapp.domain.model.NotificationType

data class NotificationDto(
    val id: String = "",
    val typeName: String = "",
    val userName: String = "",
    val userAvatarUrl: String? = null,
    val publicationTitle: String? = null,
    val publicationImage: String? = null,
    val date: String = "",
    val createdAt: Long = 0L,
    val isRead: Boolean = false,
    val relatedEntityId: String? = null,
    val recipientUserId: String? = null
) {
    fun toDomain(): Notification = Notification(
        id = id,
        type = try { NotificationType.valueOf(typeName) } catch (e: Exception) { NotificationType.VERIFIED },
        userName = userName,
        userAvatarUrl = userAvatarUrl,
        publicationTitle = publicationTitle,
        publicationImage = publicationImage,
        date = date,
        createdAt = createdAt,
        isRead = isRead,
        relatedEntityId = relatedEntityId,
        recipientUserId = recipientUserId
    )

    companion object {
        fun fromDomain(domain: Notification): NotificationDto = NotificationDto(
            id = domain.id,
            typeName = domain.type.name,
            userName = domain.userName,
            userAvatarUrl = domain.userAvatarUrl,
            publicationTitle = domain.publicationTitle,
            publicationImage = domain.publicationImage,
            date = domain.date,
            createdAt = domain.createdAt,
            isRead = domain.isRead,
            relatedEntityId = domain.relatedEntityId,
            recipientUserId = domain.recipientUserId
        )
    }
}
