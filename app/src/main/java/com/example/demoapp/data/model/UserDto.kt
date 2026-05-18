package com.example.demoapp.data.model

import com.example.demoapp.domain.model.Badge
import com.example.demoapp.domain.model.BadgeType
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.model.UserLevel
import com.example.demoapp.domain.model.UserRole

/**
 * Data Transfer Object que mapea 1:1 con un documento de Firestore en la colección "users".
 * Todos los campos tienen valores por defecto para que Firestore pueda deserializarlos
 * automáticamente con toObject<UserDto>().
 */
data class UserDto(
    val id: String = "",
    val name: String = "",
    val city: String = "",
    val address: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String = "",
    val bio: String = "",
    val role: String = UserRole.USER.name,
    val points: Int = 0,
    val badges: List<Map<String, Any>> = emptyList(),
    val level: String = UserLevel.NOVATO.name,
    val followers: Int = 0,
    val following: Int = 0,
    val savedPublications: List<String> = emptyList(),
    val fcmToken: String = ""
) {
    /** Convierte el DTO al modelo de dominio. */
    fun toDomain(): User = User(
        id = id,
        name = name,
        city = city,
        address = address,
        email = email,
        password = password,
        phoneNumber = phoneNumber,
        profilePictureUrl = profilePictureUrl,
        bio = bio,
        role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.USER),
        points = points,
        badges = badges.mapNotNull { map ->
            runCatching {
                Badge(
                    id = map["id"] as? String ?: "",
                    type = BadgeType.valueOf(map["type"] as? String ?: ""),
                    name = map["name"] as? String ?: "",
                    description = map["description"] as? String ?: "",
                    iconUrl = map["iconUrl"] as? String ?: "",
                    earnedAt = (map["earnedAt"] as? Long) ?: System.currentTimeMillis()
                )
            }.getOrNull()
        },
        level = runCatching { UserLevel.valueOf(level) }.getOrDefault(UserLevel.NOVATO),
        followers = followers,
        following = following,
        savedPublications = savedPublications,
        fcmToken = fcmToken
    )

    companion object {
        /** Convierte el modelo de dominio a un Map listo para guardar en Firestore. */
        fun fromDomain(user: User): Map<String, Any> = mapOf(
            "id" to user.id,
            "name" to user.name,
            "city" to user.city,
            "address" to user.address,
            "email" to user.email,
            "password" to user.password,
            "phoneNumber" to user.phoneNumber,
            "profilePictureUrl" to user.profilePictureUrl,
            "bio" to user.bio,
            "role" to user.role.name,
            "points" to user.points,
            "badges" to user.badges.map { badge ->
                mapOf(
                    "id" to badge.id,
                    "type" to badge.type.name,
                    "name" to badge.name,
                    "description" to badge.description,
                    "iconUrl" to badge.iconUrl,
                    "earnedAt" to badge.earnedAt
                )
            },
            "level" to user.level.name,
            "followers" to user.followers,
            "following" to user.following,
            "savedPublications" to user.savedPublications,
            "fcmToken" to user.fcmToken
        )
    }
}
