package com.example.demoapp.domain.model

data class User (
    val id: String,
    val name: String,
    val city: String,
    val address: String,
    val email: String,
    val password: String,
    val phoneNumber: String = "",
    val profilePictureUrl: String = "",
    val bio: String = "",
    val role: UserRole = UserRole.USER,

    val points: Int = 0,
    val badges: List<Badge> = emptyList(),
    var level: UserLevel = UserLevel.NOVATO,

    val followers: Int = 0,
    val following: Int = 0,
    val savedPublications: List<String> = emptyList()
)