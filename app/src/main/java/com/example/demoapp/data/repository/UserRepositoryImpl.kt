package com.example.demoapp.data.repository

import android.util.Log
import com.example.demoapp.data.model.UserDto
import com.example.demoapp.domain.model.Badge
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.model.UserLevel
import com.example.demoapp.domain.model.UserRole
import com.example.demoapp.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UserRepositoryImpl"
private const val COLLECTION = "users"

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _users = MutableStateFlow<List<User>>(emptyList())
    override val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // -------------------------------------------------------------------------
    // Inicialización: escucha en tiempo real la colección "users" de Firestore
    // y hace seed si está vacía.
    // -------------------------------------------------------------------------
    init {
        observeUsers()
    }

    private fun observeUsers() {
        firestore.collection(COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error al escuchar usuarios: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        runCatching {
                            val dto = doc.toObject(UserDto::class.java)
                            dto?.copy(id = doc.id)?.toDomain()
                        }.getOrNull()
                    }
                    _users.value = list

                    // Si la colección está vacía, hacemos seed con los datos iniciales
                    if (list.isEmpty()) {
                        scope.launch { seedInitialData() }
                    }

                    // Sincroniza currentUser si la sesión sigue activa
                    _currentUser.value?.let { current ->
                        _currentUser.value = list.firstOrNull { it.id == current.id }
                    }
                }
            }
    }

    // -------------------------------------------------------------------------
    // Auth
    // -------------------------------------------------------------------------
    override fun login(email: String, password: String): User? {
        val user = _users.value.firstOrNull {
            it.email == email && it.password == password
        }
        _currentUser.value = user
        return user
    }

    override fun restoreCurrentUser(userId: String): Boolean {
        val user = findById(userId)
        _currentUser.value = user
        return user != null
    }

    override fun logout() {
        _currentUser.value = null
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val exists = _users.value.any { it.email == email }
        return if (exists) Result.success(Unit)
        else Result.failure(Exception("No existe una cuenta con ese correo"))
    }

    // -------------------------------------------------------------------------
    // Queries (operan sobre el caché local que Firestore mantiene actualizado)
    // -------------------------------------------------------------------------
    override fun findById(id: String): User? =
        _users.value.firstOrNull { it.id == id }

    override fun getUsersByCity(city: String): List<User> =
        _users.value.filter { it.city.equals(city, ignoreCase = true) }

    // -------------------------------------------------------------------------
    // Commands (escrituras en Firestore)
    // -------------------------------------------------------------------------
    override fun save(user: User) {
        scope.launch {
            runCatching {
                val data = UserDto.fromDomain(user)
                if (user.id.isBlank()) {
                    // Firestore genera el ID automáticamente
                    firestore.collection(COLLECTION).add(data).await()
                } else {
                    firestore.collection(COLLECTION).document(user.id).set(data).await()
                }
            }.onFailure { Log.e(TAG, "Error al guardar usuario: ${it.message}", it) }
        }
    }

    override fun update(user: User): Result<Unit> {
        val exists = _users.value.any { it.id == user.id }
        if (!exists) return Result.failure(Exception("Usuario no encontrado"))

        scope.launch {
            runCatching {
                firestore.collection(COLLECTION)
                    .document(user.id)
                    .set(UserDto.fromDomain(user))
                    .await()
            }.onFailure { Log.e(TAG, "Error al actualizar usuario: ${it.message}", it) }
        }
        return Result.success(Unit)
    }

    override fun delete(id: String): Result<Unit> {
        val exists = _users.value.any { it.id == id }
        if (!exists) return Result.failure(Exception("Usuario no encontrado"))

        scope.launch {
            runCatching {
                firestore.collection(COLLECTION).document(id).delete().await()
                if (_currentUser.value?.id == id) _currentUser.value = null
            }.onFailure { Log.e(TAG, "Error al eliminar usuario: ${it.message}", it) }
        }
        return Result.success(Unit)
    }

    // -------------------------------------------------------------------------
    // Perfil
    // -------------------------------------------------------------------------
    override fun updateProfilePicture(id: String, pictureUrl: String): Result<Unit> {
        val user = findById(id) ?: return Result.failure(Exception("Usuario no encontrado"))
        return update(user.copy(profilePictureUrl = pictureUrl))
    }

    // -------------------------------------------------------------------------
    // Reputación
    // -------------------------------------------------------------------------
    override fun addPoints(userId: String, points: Int): Result<Unit> {
        val user = findById(userId) ?: return Result.failure(Exception("Usuario no encontrado"))
        val newPoints = user.points + points
        return update(user.copy(points = newPoints, level = UserLevel.fromPoints(newPoints)))
    }

    override fun awardBadge(userId: String, badge: Badge): Result<Unit> {
        val user = findById(userId) ?: return Result.failure(Exception("Usuario no encontrado"))
        if (user.badges.any { it.type == badge.type }) return Result.success(Unit)
        return update(user.copy(badges = user.badges + badge))
    }

    override fun getBadges(userId: String): List<Badge> =
        findById(userId)?.badges ?: emptyList()

    override fun getPoints(userId: String): Int =
        findById(userId)?.points ?: 0

    // -------------------------------------------------------------------------
    // Seed: carga datos iniciales cuando Firestore está vacío
    // -------------------------------------------------------------------------
    private suspend fun seedInitialData() {
        Log.d(TAG, "Colección vacía. Cargando datos iniciales en Firestore...")
        val seedUsers = listOf(
            User(
                id = "user_1",
                name = "Juan",
                city = "Armenia",
                address = "Calle 123",
                email = "juan@email.com",
                password = "111111",
                profilePictureUrl = "https://picsum.photos/200?random=1",
                bio = "Amante de la naturaleza y caminatas de fin de semana"
            ),
            User(
                id = "user_2",
                name = "Maria",
                city = "Pereira",
                address = "Calle 456",
                email = "maria@email.com",
                password = "222222",
                profilePictureUrl = "https://picsum.photos/200?random=2",
                bio = "Exploradora urbana apasionada por descubrir lugares únicos"
            ),
            User(
                id = "user_3",
                name = "Carlos",
                city = "Manizales",
                address = "Calle 789",
                email = "carlos@email.com",
                password = "333333",
                profilePictureUrl = "https://picsum.photos/200?random=3",
                bio = "Fan de la fotografía y los espacios culturales",
                role = UserRole.USER
            ),
            User(
                id = "admin_1",
                name = "Carlos Admin",
                city = "Armenia",
                address = "Calle Central",
                email = "admin@redexplora.com",
                password = "admin123456",
                profilePictureUrl = "https://picsum.photos/200?random=99",
                bio = "Moderador de la plataforma Red Explora",
                role = UserRole.ADMIN
            )
        )

        val batch = firestore.batch()
        seedUsers.forEach { user ->
            val ref = firestore.collection(COLLECTION).document(user.id)
            batch.set(ref, UserDto.fromDomain(user))
        }
        runCatching { batch.commit().await() }
            .onSuccess { Log.d(TAG, "Seed completado: ${seedUsers.size} usuarios creados.") }
            .onFailure { Log.e(TAG, "Error en seed: ${it.message}", it) }
    }
}