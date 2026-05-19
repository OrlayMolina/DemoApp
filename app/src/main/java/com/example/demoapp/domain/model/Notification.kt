package com.example.demoapp.domain.model

data class Notification(
    val id               : String,
    val type             : NotificationType,
    val userName         : String,
    val userAvatarUrl    : String? = null,
    val publicationTitle : String? = null,
    val publicationImage : String? = null,
    val date             : String,
    val createdAt        : Long = System.currentTimeMillis(),
    val isRead           : Boolean = false,
    val relatedEntityId  : String? = null,
    val recipientUserId  : String? = null
) {
    companion object {
        /** Recipient especial: cualquier usuario con rol ADMIN/moderador. */
        const val RECIPIENT_MODERATORS = "_moderators_"
    }
}