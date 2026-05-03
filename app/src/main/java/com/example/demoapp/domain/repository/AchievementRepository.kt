package com.example.demoapp.domain.repository

import com.example.demoapp.domain.model.Achievement
import com.example.demoapp.domain.model.AchievementType
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {

    fun observeAchievements(userId: String): Flow<List<Achievement>>

    fun progressOf(userId: String, type: AchievementType): Int
}
