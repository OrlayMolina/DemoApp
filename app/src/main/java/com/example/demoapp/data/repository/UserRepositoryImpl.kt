package com.example.demoapp.data.repository

import com.example.demoapp.domain.model.Badge
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.model.UserLevel
import com.example.demoapp.domain.model.UserRole
import com.example.demoapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor() : UserRepository {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    override val users: StateFlow<List<User>> = _users.asStateFlow()

    init {
        _users.value = fetchUsers()
    }

    // --- Auth ---
    override fun login(email: String, password: String): User? {
        return _users.value.firstOrNull { it.email == email && it.password == password }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val exists = _users.value.any { it.email == email }
        return if (exists) Result.success(Unit)
        else Result.failure(Exception("No existe una cuenta con ese correo"))
    }

    // --- Queries ---
    override fun findById(id: String): User? {
        return _users.value.firstOrNull { it.id == id }
    }

    override fun getUsersByCity(city: String): List<User> {
        return _users.value.filter { it.city.equals(city, ignoreCase = true) }
    }

    // --- Commands ---
    override fun save(user: User) {
        _users.value += user
    }

    override fun update(user: User): Result<Unit> {
        val index = _users.value.indexOfFirst { it.id == user.id }
        return if (index != -1) {
            val updated = _users.value.toMutableList()
            updated[index] = user
            _users.value = updated
            Result.success(Unit)
        } else {
            Result.failure(Exception("Usuario no encontrado"))
        }
    }

    override fun delete(id: String): Result<Unit> {
        val exists = _users.value.any { it.id == id }
        return if (exists) {
            _users.value = _users.value.filter { it.id != id }
            Result.success(Unit)
        } else {
            Result.failure(Exception("Usuario no encontrado"))
        }
    }

    // --- Perfil ---
    override fun updateProfilePicture(id: String, pictureUrl: String): Result<Unit> {
        val user = findById(id) ?: return Result.failure(Exception("Usuario no encontrado"))
        return update(user.copy(profilePictureUrl = pictureUrl))
    }

    // --- Reputación ---
    override fun addPoints(userId: String, points: Int): Result<Unit> {
        val user = findById(userId) ?: return Result.failure(Exception("Usuario no encontrado"))
        val newPoints = user.points + points
        return update(user.copy(
            points = newPoints,
            level = UserLevel.fromPoints(newPoints)
        ))
    }

    override fun awardBadge(userId: String, badge: Badge): Result<Unit> {
        val user = findById(userId) ?: return Result.failure(Exception("Usuario no encontrado"))
        if (user.badges.any { it.type == badge.type }) return Result.success(Unit) // ya tiene la insignia
        return update(user.copy(badges = user.badges + badge))
    }

    override fun getBadges(userId: String): List<Badge> {
        return findById(userId)?.badges ?: emptyList()
    }

    override fun getPoints(userId: String): Int {
        return findById(userId)?.points ?: 0
    }

    // --- Datos precargados ---
    private fun fetchUsers(): List<User> {
        return listOf(
            User(
                id = "1",
                name = "Juan",
                city = "Armenia",
                address = "Calle 123",
                email = "juan@email.com",
                password = "111111",
                profilePictureUrl = "https://picsum.photos/200?random=1"
            ),
            User(
                id = "2",
                name = "Maria",
                city = "Pereira",
                address = "Calle 456",
                email = "maria@email.com",
                password = "222222",
                profilePictureUrl = "https://picsum.photos/200?random=2"
            ),
            User(
                id = "3",
                name = "Carlos",
                city = "Manizales",
                address = "Calle 789",
                email = "carlos@email.com",
                password = "333333",
                profilePictureUrl = "https://picsum.photos/200?random=3",
                role = UserRole.USER
            )
        )
    }
}