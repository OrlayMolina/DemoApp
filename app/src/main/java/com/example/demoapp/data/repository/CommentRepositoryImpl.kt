package com.example.demoapp.data.repository

import android.util.Log
import com.example.demoapp.data.model.CommentDto
import com.example.demoapp.domain.model.Comment
import com.example.demoapp.domain.repository.CommentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CommentRepoImpl"
private const val COLLECTION = "comments"

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CommentRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())

    init {
        observeComments()
    }

    private fun observeComments() {
        firestore.collection(COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to comments: ${error.message}", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        runCatching {
                            val dto = doc.toObject(CommentDto::class.java)
                            dto?.copy(id = doc.id)?.toDomain()
                        }.onFailure { Log.e(TAG, "Error mapping doc", it) }.getOrNull()
                    }
                    _comments.value = list

                    if (list.isEmpty()) {
                        scope.launch { seedInitialData() }
                    }
                }
            }
    }

    override fun observeByPoint(pointId: String): Flow<List<Comment>> {
        return _comments.map { list ->
            list.filter { it.pointId == pointId }.sortedByDescending { it.createdAt }
        }
    }

    override fun observeCommentCounts(): Flow<Map<String, Int>> {
        return _comments.map { list ->
            list.groupingBy { it.pointId }.eachCount()
        }
    }

    override fun addComment(
        pointId: String,
        authorId: String,
        authorName: String,
        text: String,
        authorAvatarUrl: String?
    ): Result<Comment> {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return Result.failure(IllegalArgumentException("El comentario no puede estar vacio"))

        val newComment = Comment(
            id = "c_${System.currentTimeMillis()}",
            pointId = pointId,
            authorId = authorId,
            authorName = authorName,
            authorAvatarUrl = authorAvatarUrl,
            text = cleanText,
            createdAt = System.currentTimeMillis()
        )

        scope.launch {
            runCatching {
                val data = CommentDto.fromDomain(newComment)
                firestore.collection(COLLECTION).document(newComment.id).set(data).await()
            }.onFailure { Log.e(TAG, "Error adding comment: ${it.message}", it) }
        }

        return Result.success(newComment)
    }

    private suspend fun seedInitialData() {
        Log.d(TAG, "Collection empty. Seeding initial comments...")
        val allSeeds = Comment.SAMPLE_BY_POINT.values.flatten()
        val batch = firestore.batch()
        allSeeds.forEach { comment ->
            val ref = firestore.collection(COLLECTION).document(comment.id)
            batch.set(ref, CommentDto.fromDomain(comment))
        }
        runCatching { batch.commit().await() }
            .onFailure { Log.e(TAG, "Error seeding comments: ${it.message}", it) }
    }
}
