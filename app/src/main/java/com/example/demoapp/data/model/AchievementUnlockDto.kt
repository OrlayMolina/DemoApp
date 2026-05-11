package com.example.demoapp.data.model

data class AchievementUnlockDto(
    val id: String = "",
    val userId: String = "",
    val achievementId: String = "",
    val unlockedAt: Long = 0L
)
