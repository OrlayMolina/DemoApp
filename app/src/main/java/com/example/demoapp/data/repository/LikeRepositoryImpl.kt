package com.example.demoapp.data.repository

import android.util.Log
import com.example.demoapp.data.model.PostLikeDto
import com.example.demoapp.domain.model.PostLike
import com.example.demoapp.domain.repository.LikeRepository
import com.example.demoapp.domain.repository.TouristPointRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "LikeRepoImpl"
private const val COLLECTION = "likes"

@Singleton
class LikeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val touristPointRepository: TouristPointRepository
) : LikeRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _likes = MutableStateFlow<List<PostLike>>(emptyList())
    override val likes: StateFlow<List<PostLike>> = _likes.asStateFlow()

    init {
        observeLikes()
    }

    private fun observeLikes() {
        firestore.collection(COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to likes: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        runCatching {
                            doc.toObject(PostLikeDto::class.java)?.toDomain()
                        }.onFailure { Log.e(TAG, "Error mapping doc", it) }.getOrNull()
                    }
                    _likes.value = list
                }
            }
    }

    private fun getDocId(postId: String, userId: String) = "${postId}_${userId}"

    override fun like(postId: String, userId: String): Result<Unit> {
        if (_likes.value.any { it.postId == postId && it.userId == userId }) {
            return Result.success(Unit)
        }
        val point = touristPointRepository.findById(postId)
            ?: return Result.failure(IllegalArgumentException("Post not found"))

        scope.launch {
            runCatching {
                val like = PostLike(postId, userId)
                val docId = getDocId(postId, userId)
                firestore.collection(COLLECTION).document(docId).set(PostLikeDto.fromDomain(like)).await()
                
                touristPointRepository.update(point.copy(importantVotes = point.importantVotes + 1))
            }.onFailure { Log.e(TAG, "Error adding like: ${it.message}", it) }
        }
        return Result.success(Unit)
    }

    override fun unlike(postId: String, userId: String): Result<Unit> {
        val existed = _likes.value.any { it.postId == postId && it.userId == userId }
        if (!existed) return Result.success(Unit)

        val point = touristPointRepository.findById(postId)

        scope.launch {
            runCatching {
                val docId = getDocId(postId, userId)
                firestore.collection(COLLECTION).document(docId).delete().await()

                if (point != null) {
                    val newCount = (point.importantVotes - 1).coerceAtLeast(0)
                    touristPointRepository.update(point.copy(importantVotes = newCount))
                }
            }.onFailure { Log.e(TAG, "Error removing like: ${it.message}", it) }
        }
        return Result.success(Unit)
    }

    override fun toggle(postId: String, userId: String): Result<Boolean> {
        return if (isLiked(postId, userId)) {
            unlike(postId, userId).map { false }
        } else {
            like(postId, userId).map { true }
        }
    }

    override fun isLiked(postId: String, userId: String): Boolean {
        return _likes.value.any { it.postId == postId && it.userId == userId }
    }

    override fun observeIsLiked(postId: String, userId: String): Flow<Boolean> {
        return _likes.map { list ->
            list.any { it.postId == postId && it.userId == userId }
        }
    }

    override fun observeLikedPostIds(userId: String): Flow<Set<String>> {
        return _likes.map { list ->
            list.asSequence().filter { it.userId == userId }.map { it.postId }.toSet()
        }
    }

    override fun likeCount(postId: String): Int {
        return _likes.value.count { it.postId == postId }
    }
}
