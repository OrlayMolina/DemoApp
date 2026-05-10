package com.example.demoapp.data.repository

import android.util.Log
import com.example.demoapp.data.model.FollowRelationDto
import com.example.demoapp.domain.model.FollowRelation
import com.example.demoapp.domain.repository.FollowRepository
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

private const val TAG = "FollowRepoImpl"
private const val COLLECTION = "follows"

@Singleton
class FollowRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FollowRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _relations = MutableStateFlow<List<FollowRelation>>(emptyList())
    override val relations: StateFlow<List<FollowRelation>> = _relations.asStateFlow()

    init {
        observeRelations()
    }

    private fun observeRelations() {
        firestore.collection(COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to follows: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        runCatching {
                            doc.toObject(FollowRelationDto::class.java)?.toDomain()
                        }.getOrNull()
                    }
                    _relations.value = list
                }
            }
    }

    private fun getDocId(followerId: String, followingId: String) = "${followerId}_${followingId}"

    override fun follow(followerId: String, followingId: String): Result<Unit> {
        if (followerId == followingId) return Result.failure(IllegalArgumentException("A user cannot follow themselves"))

        val current = _relations.value
        if (current.any { it.followerId == followerId && it.followingId == followingId }) {
            return Result.success(Unit)
        }

        scope.launch {
            runCatching {
                val relation = FollowRelation(followerId, followingId)
                val docId = getDocId(followerId, followingId)
                firestore.collection(COLLECTION).document(docId).set(FollowRelationDto.fromDomain(relation)).await()
            }.onFailure { Log.e(TAG, "Error adding follow: ${it.message}", it) }
        }
        return Result.success(Unit)
    }

    override fun unfollow(followerId: String, followingId: String): Result<Unit> {
        scope.launch {
            runCatching {
                val docId = getDocId(followerId, followingId)
                firestore.collection(COLLECTION).document(docId).delete().await()
            }.onFailure { Log.e(TAG, "Error removing follow: ${it.message}", it) }
        }
        return Result.success(Unit)
    }

    override fun isFollowing(followerId: String, followingId: String): Boolean {
        return _relations.value.any { it.followerId == followerId && it.followingId == followingId }
    }

    override fun observeIsFollowing(followerId: String, followingId: String): Flow<Boolean> {
        return _relations.map { list -> list.any { it.followerId == followerId && it.followingId == followingId } }
    }

    override fun followersOf(userId: String): List<String> {
        return _relations.value.filter { it.followingId == userId }.map { it.followerId }
    }

    override fun followingOf(userId: String): List<String> {
        return _relations.value.filter { it.followerId == userId }.map { it.followingId }
    }

    override fun observeFollowersCount(userId: String): Flow<Int> {
        return _relations.map { list -> list.count { it.followingId == userId } }
    }

    override fun observeFollowingCount(userId: String): Flow<Int> {
        return _relations.map { list -> list.count { it.followerId == userId } }
    }
}
