package com.example.demoapp.domain.repository

import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.model.Badge
import com.example.demoapp.domain.model.UserRole
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {

    // --- State ---
    val users: StateFlow<List<User>>

    // --- Auth ---
    fun login(email: String, password: String): User?
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    // --- Queries ---
    fun findById(id: String): User?
    fun getUsersByCity(city: String): List<User>

    // --- Commands ---
    fun save(user: User)
    fun update(user: User): Result<Unit>
    fun delete(id: String): Result<Unit>

    // --- Perfil ---
    fun updateProfilePicture(id: String, pictureUrl: String): Result<Unit>

    // --- Reputación (puntos e insignias) ---
    fun addPoints(userId: String, points: Int): Result<Unit>
    fun awardBadge(userId: String, badge: Badge): Result<Unit>
    fun getBadges(userId: String): List<Badge>
    fun getPoints(userId: String): Int
}