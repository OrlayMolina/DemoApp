package com.example.demoapp.data.repository

import android.util.Log
import com.example.demoapp.data.model.UserDto
import com.example.demoapp.domain.model.Badge
import com.example.demoapp.domain.model.Notification
import com.example.demoapp.domain.model.NotificationType
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.model.UserLevel
import com.example.demoapp.domain.model.UserRole
import com.example.demoapp.domain.repository.NotificationRepository
import com.example.demoapp.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UserRepositoryImpl"
private const val COLLECTION = "users"

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val notificationRepository: NotificationRepository
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
                            // Firestore deja caer el prefijo "is" en propiedades booleanas de
                            // Kotlin (lee "banned" en vez de "isBanned"). Releemos a mano para
                            // mantener el estado de bloqueo confiable.
                            val isBanned = doc.getBoolean("isBanned") ?: doc.getBoolean("banned") ?: false
                            val banReason = doc.getString("banReason") ?: ""
                            val banAppeal = doc.getString("banAppeal") ?: ""
                            dto?.copy(
                                id = doc.id,
                                isBanned = isBanned,
                                banReason = banReason,
                                banAppeal = banAppeal
                            )?.toDomain()
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
        return try {
            val authResult = Tasks.await(firebaseAuth.signInWithEmailAndPassword(email, password))
            val uid = authResult.user?.uid ?: return null

            // Firebase Auth valida la password. Firestore solo guarda el perfil.
            val userInCache = _users.value.firstOrNull { it.id == uid }
            val finalUser = if (userInCache != null) {
                userInCache
            } else {
                val doc = Tasks.await(firestore.collection(COLLECTION).document(uid).get())
                val userByUid = doc.toObject(UserDto::class.java)?.copy(id = uid)?.toDomain()
                userByUid
                    ?: findProfileByEmailAndMoveToAuthUid(email, uid)
                    ?: createProfileForAuthUser(uid, email)
            }

            _currentUser.value = finalUser
            finalUser
        } catch (e: Exception) {
            Log.e(TAG, "Error logging in with Firebase Auth: ${e.message}")
            null
        }
    }

    /**
     * Crea un perfil minimo en Firestore para un usuario que existe en Firebase Auth
     * pero aun no tiene doc en Firestore (ej. lo creaste manualmente en la consola).
     * Detecta el rol por el patron del email: admin@... -> ADMIN, resto -> USER.
     */
    private fun createProfileForAuthUser(uid: String, email: String): User {
        val normalizedEmail = email.trim().lowercase()
        val isAdmin = normalizedEmail.startsWith("admin@")
        val displayName = normalizedEmail
            .substringBefore("@")
            .replaceFirstChar { it.titlecase() }

        val newUser = User(
            id       = uid,
            name     = displayName,
            city     = "",
            address  = "",
            email    = normalizedEmail,
            password = "",
            role     = if (isAdmin) UserRole.ADMIN else UserRole.USER
        )

        Tasks.await(
            firestore.collection(COLLECTION)
                .document(uid)
                .set(UserDto.fromDomain(newUser))
        )
        Log.d(TAG, "Auto-created Firestore profile for Auth user $email ($uid), role=${newUser.role}")
        return newUser
    }

    private fun findProfileByEmailAndMoveToAuthUid(email: String, uid: String): User? {
        val querySnapshot = Tasks.await(
            firestore.collection(COLLECTION)
                .whereEqualTo("email", email.trim().lowercase())
                .limit(1)
                .get()
        )
        val document = querySnapshot.documents.firstOrNull() ?: return null
        val user = document.toObject(UserDto::class.java)
            ?.copy(id = uid)
            ?.toDomain()
            ?: return null

        Tasks.await(firestore.collection(COLLECTION).document(uid).set(UserDto.fromDomain(user)))
        if (document.id != uid) {
            Tasks.await(firestore.collection(COLLECTION).document(document.id).delete())
        }

        return user
    }

    override fun restoreCurrentUser(userId: String): Boolean {
        val user = findById(userId)
        _currentUser.value = user
        return user != null
    }

    override fun logout() {
        firebaseAuth.signOut()
        _currentUser.value = null
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(email: String, newPassword: String): Result<Unit> {
        return Result.failure(
            UnsupportedOperationException(
                "La password se gestiona con Firebase Auth. Usa el enlace de recuperacion enviado por correo."
            )
        )
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
                var finalUserId = user.id
                if (user.password.isNotBlank() && !user.id.startsWith("user_") && !user.id.startsWith("admin_")) {
                    try {
                        val authResult = firebaseAuth.createUserWithEmailAndPassword(user.email, user.password).await()
                        authResult.user?.uid?.let { uid ->
                            finalUserId = uid
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "FirebaseAuth: Error al crear usuario o ya existe: ${e.message}")
                    }
                }
                
                val data = UserDto.fromDomain(user.copy(id = finalUserId))
                firestore.collection(COLLECTION).document(finalUserId).set(data).await()
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
    // Moderacion: bloqueo, desbloqueo y apelaciones
    // -------------------------------------------------------------------------
    override fun banUser(userId: String, reason: String): Result<Unit> {
        val user = findById(userId) ?: return Result.failure(Exception("Usuario no encontrado"))
        val cleanReason = reason.trim()
        if (cleanReason.isBlank()) return Result.failure(IllegalArgumentException("El motivo es obligatorio"))

        val result = update(user.copy(isBanned = true, banReason = cleanReason, banAppeal = ""))
        if (result.isSuccess) {
            val now = System.currentTimeMillis()
            notificationRepository.add(
                Notification(
                    id              = "",
                    type            = NotificationType.BAN,
                    userName        = currentUser.value?.name.orEmpty(),
                    publicationTitle = cleanReason,
                    date            = SimpleDateFormat("dd MMM, HH:mm", Locale("es")).format(Date(now)),
                    createdAt       = now,
                    isRead          = false,
                    relatedEntityId = userId,
                    recipientUserId = userId
                )
            )
        }
        return result
    }

    override fun unbanUser(userId: String): Result<Unit> {
        val user = findById(userId) ?: return Result.failure(Exception("Usuario no encontrado"))
        val result = update(user.copy(isBanned = false, banReason = "", banAppeal = ""))
        if (result.isSuccess) {
            val now = System.currentTimeMillis()
            notificationRepository.add(
                Notification(
                    id              = "",
                    type            = NotificationType.BAN_LIFTED,
                    userName        = currentUser.value?.name.orEmpty(),
                    date            = SimpleDateFormat("dd MMM, HH:mm", Locale("es")).format(Date(now)),
                    createdAt       = now,
                    isRead          = false,
                    relatedEntityId = userId,
                    recipientUserId = userId
                )
            )
        }
        return result
    }

    override fun submitBanAppeal(userId: String, appeal: String): Result<Unit> {
        val user = findById(userId) ?: return Result.failure(Exception("Usuario no encontrado"))
        if (!user.isBanned) return Result.failure(IllegalStateException("El usuario no esta bloqueado"))
        val cleanAppeal = appeal.trim()
        if (cleanAppeal.isBlank()) return Result.failure(IllegalArgumentException("La apelacion es obligatoria"))

        val result = update(user.copy(banAppeal = cleanAppeal))
        if (result.isSuccess) {
            val now = System.currentTimeMillis()
            notificationRepository.add(
                Notification(
                    id              = "",
                    type            = NotificationType.BAN_APPEAL,
                    userName        = user.name,
                    userAvatarUrl   = user.profilePictureUrl.takeIf { it.isNotBlank() },
                    publicationTitle = cleanAppeal,
                    date            = SimpleDateFormat("dd MMM, HH:mm", Locale("es")).format(Date(now)),
                    createdAt       = now,
                    isRead          = false,
                    relatedEntityId = userId,
                    recipientUserId = Notification.RECIPIENT_MODERATORS
                )
            )
        }
        return result
    }

    override fun updateFcmToken(userId: String, token: String): Result<Unit> {
        val user = findById(userId) ?: return Result.failure(Exception("Usuario no encontrado"))
        if (user.fcmToken == token) return Result.success(Unit)
        return update(user.copy(fcmToken = token))
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
