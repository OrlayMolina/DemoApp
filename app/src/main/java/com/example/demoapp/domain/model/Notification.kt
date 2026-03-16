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
    val relatedEntityId  : String? = null
) {
    companion object {
        val SAMPLE_LIST = listOf(
            Notification(
                id               = "1",
                type             = NotificationType.LIKE,
                userName         = "Juan",
                userAvatarUrl    = "https://picsum.photos/seed/juan/100/100",
                publicationTitle = "Mural Increíble en el Centro",
                publicationImage = "https://picsum.photos/seed/mural/100/100",
                date             = "24 feb, 03:30",
                createdAt        = System.currentTimeMillis() - 86400000L * 2,
                isRead           = false,
                relatedEntityId  = "1"
            ),
            Notification(
                id               = "2",
                type             = NotificationType.COMMENT,
                userName         = "Alma",
                userAvatarUrl    = "https://picsum.photos/seed/alma/100/100",
                publicationTitle = "Parque escondido",
                publicationImage = "https://picsum.photos/seed/parque/100/100",
                date             = "25 feb, 10:42",
                createdAt        = System.currentTimeMillis() - 86400000L,
                isRead           = false,
                relatedEntityId  = "2"
            ),
            Notification(
                id               = "3",
                type             = NotificationType.FOLLOWER,
                userName         = "Carlos",
                userAvatarUrl    = "https://picsum.photos/seed/carlos/100/100",
                date             = "22 feb, 07:20",
                createdAt        = System.currentTimeMillis() - 86400000L * 4,
                isRead           = true,
                relatedEntityId  = "user_carlos"
            ),
            Notification(
                id               = "4",
                type             = NotificationType.VERIFIED,
                userName         = "",
                publicationTitle = "Vista Panorámica",
                publicationImage = "https://picsum.photos/seed/panoramica/100/100",
                date             = "27 feb, 07:20",
                createdAt        = System.currentTimeMillis() - 3600000L,
                isRead           = true,
                relatedEntityId  = "3"
            )
        )
    }
}