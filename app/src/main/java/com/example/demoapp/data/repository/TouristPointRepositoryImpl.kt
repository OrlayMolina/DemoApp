package com.example.demoapp.data.repository

import android.util.Log
import com.example.demoapp.data.model.TouristPointDto
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.repository.ReviewHistoryRepository
import com.example.demoapp.domain.repository.TouristPointRepository
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

private const val TAG = "TouristPointRepoImpl"
private const val COLLECTION = "tourist_points"

@Singleton
class TouristPointRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    private val userRepository: UserRepository
) : TouristPointRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _touristPoints = MutableStateFlow<List<TouristPoint>>(emptyList())
    override val touristPoints: StateFlow<List<TouristPoint>> = _touristPoints.asStateFlow()

    init {
        observeTouristPoints()
    }

    private fun observeTouristPoints() {
        firestore.collection(COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to tourist points: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        runCatching {
                            val id = doc.id
                            val authorId = doc.getString("authorId") ?: ""
                            val title = doc.getString("title") ?: ""
                            val categoryName = doc.getString("categoryName") ?: "NATURE"
                            val description = doc.getString("description") ?: ""
                            val latitude = doc.getDouble("latitude") ?: 0.0
                            val longitude = doc.getDouble("longitude") ?: 0.0
                            val address = doc.getString("address") ?: ""
                            val schedule = doc.getString("schedule") ?: ""
                            val priceRangeName = doc.getString("priceRangeName") ?: "FREE"
                            val photoUrls = doc.get("photoUrls") as? List<*>
                            val parsedPhotoUrls = photoUrls?.filterIsInstance<String>() ?: emptyList()
                            val isVerified = doc.getBoolean("isVerified") ?: doc.getBoolean("verified") ?: false
                            val isRejected = doc.getBoolean("isRejected") ?: doc.getBoolean("rejected") ?: false
                            val rejectionReason = doc.getString("rejectionReason")
                            val isResolved = doc.getBoolean("isResolved") ?: doc.getBoolean("resolved") ?: false
                            val isReported = doc.getBoolean("isReported") ?: doc.getBoolean("reported") ?: false
                            val reportReason = doc.getString("reportReason")
                            val importantVotes = doc.getLong("importantVotes")?.toInt() ?: 0
                            val visitedRaw = doc.get("visitedByUserIds") as? List<*>
                            val visitedByUserIds = visitedRaw?.filterIsInstance<String>() ?: emptyList()
                            val commentCount = doc.getLong("commentCount")?.toInt() ?: 0
                            val createdAt = doc.getLong("createdAt") ?: 0L
                            val embeddingRaw = doc.get("embedding") as? List<*>
                            val embedding = embeddingRaw?.mapNotNull { (it as? Number)?.toDouble() } ?: emptyList()
                            val aiTagsRaw = doc.get("aiTags") as? List<*>
                            val aiTags = aiTagsRaw?.filterIsInstance<String>() ?: emptyList()
                            val isSaved = doc.getBoolean("isSaved") ?: doc.getBoolean("saved") ?: false

                            TouristPoint(
                                id = id,
                                authorId = authorId,
                                title = title,
                                category = try { com.example.demoapp.domain.model.TouristPointCategory.valueOf(categoryName) } catch(e: Exception) { com.example.demoapp.domain.model.TouristPointCategory.NATURE },
                                description = description,
                                latitude = latitude,
                                longitude = longitude,
                                address = address,
                                schedule = schedule,
                                priceRange = try { com.example.demoapp.domain.model.PriceRange.valueOf(priceRangeName) } catch(e: Exception) { com.example.demoapp.domain.model.PriceRange.FREE },
                                photoUrls = parsedPhotoUrls,
                                isVerified = isVerified,
                                isRejected = isRejected,
                                rejectionReason = rejectionReason,
                                isResolved = isResolved,
                                isReported = isReported,
                                reportReason = reportReason,
                                importantVotes = importantVotes,
                                visitedByUserIds = visitedByUserIds,
                                commentCount = commentCount,
                                createdAt = createdAt,
                                embedding = embedding,
                                aiTags = aiTags,
                                isSaved = isSaved
                            )
                        }.onFailure { Log.e(TAG, "Error mapping doc", it) }.getOrNull()
                    }.sortedByDescending { it.createdAt }
                    
                    _touristPoints.value = list

                    // Forzar la migración de los datos de prueba (incluyendo los verificados con imágenes aleatorias)
                    if (list.none { it.id == "point_1" }) {
                        scope.launch { seedInitialData() }
                    } else {
                        reviewHistoryRepository.seedFromPoints(list)
                    }
                }
            }
    }

    override suspend fun save(point: TouristPoint): Result<Unit> {
        return runCatching {
            // Aseguramos que el createdAt sea el actual al momento de guardar
            val pointWithDate = point.copy(createdAt = System.currentTimeMillis())
            val data = TouristPointDto.fromDomain(pointWithDate)
            
            if (point.id.isBlank() || point.id.toLongOrNull() != null || point.id.length < 5) {
                // Dejamos que Firestore genere el ID automáticamente si el ID es blanco, 
                // puramente numérico (como los antiguos) o muy corto.
                val docRef = firestore.collection(COLLECTION).document()
                val finalData = data.copy(id = docRef.id)
                firestore.collection(COLLECTION).document(docRef.id).set(finalData).await()
                Log.d(TAG, "Point '${point.title}' saved to Firebase with generated ID: ${docRef.id}")
            } else {
                firestore.collection(COLLECTION).document(point.id).set(data).await()
                Log.d(TAG, "Point '${point.title}' updated in Firebase with existing ID: ${point.id}")
            }
            Unit
        }.onFailure { Log.e(TAG, "Error saving point to Firebase: ${it.message}", it) }
    }

    override fun findById(id: String): TouristPoint? {
        return _touristPoints.value.find { it.id == id }
    }

    override fun update(point: TouristPoint): Result<Unit> {
        val exists = _touristPoints.value.any { it.id == point.id }
        if (!exists) return Result.failure(NoSuchElementException("Punto no encontrado: ${point.id}"))

        scope.launch {
            runCatching {
                firestore.collection(COLLECTION)
                    .document(point.id)
                    .set(TouristPointDto.fromDomain(point))
                    .await()
                Log.d(TAG, "Point '${point.title}' updated successfully.")
            }.onFailure { Log.e(TAG, "Error updating point: ${it.message}", it) }
        }
        return Result.success(Unit)
    }

    override fun delete(id: String) {
        scope.launch {
            runCatching {
                firestore.collection(COLLECTION).document(id).delete().await()
                Log.d(TAG, "Point with ID $id deleted.")
            }.onFailure { Log.e(TAG, "Error deleting point: ${it.message}", it) }
        }
    }

    override fun approvePoint(id: String): Result<Unit> {
        val current = findById(id) ?: return Result.failure(NoSuchElementException("Punto no encontrado: $id"))
        
        val updated = current.copy(
            isVerified = true,
            isRejected = false,
            rejectionReason = null
        )
        val result = update(updated)
        if (result.isSuccess) {
            reviewHistoryRepository.recordApproval(updated, currentReviewerName())
            Log.d(TAG, "Punto '$id' aprobado por moderacion.")
        }
        return result
    }

    override fun rejectPoint(id: String, reason: String): Result<Unit> {
        val current = findById(id) ?: return Result.failure(NoSuchElementException("Punto no encontrado: $id"))
        if (reason.isBlank()) return Result.failure(IllegalArgumentException("El motivo de rechazo es obligatorio"))

        val updated = current.copy(
            isVerified = false,
            isRejected = true,
            rejectionReason = reason.trim()
        )
        val result = update(updated)
        if (result.isSuccess) {
            reviewHistoryRepository.recordRejection(updated, currentReviewerName(), reason)
            Log.d(TAG, "Punto '$id' rechazado por moderacion.")
        }
        return result
    }

    private fun currentReviewerName(): String {
        return userRepository.currentUser.value?.name?.takeIf { it.isNotBlank() } ?: "Moderador"
    }

    private suspend fun seedInitialData() {
        Log.d(TAG, "Collection empty. Seeding initial tourist points...")
        val seedPoints = TouristPoint.SAMPLE_LIST
        val batch = firestore.batch()
        seedPoints.forEach { point ->
            val idToUse = if (point.id.toLongOrNull() != null) "point_${point.id}" else point.id
            val ref = firestore.collection(COLLECTION).document(idToUse)
            val updatedPoint = point.copy(id = idToUse)
            batch.set(ref, TouristPointDto.fromDomain(updatedPoint))
        }
        runCatching { batch.commit().await() }
            .onSuccess { Log.d(TAG, "Seed completed: ${seedPoints.size} points created.") }
            .onFailure { Log.e(TAG, "Error seeding points: ${it.message}", it) }
    }
}