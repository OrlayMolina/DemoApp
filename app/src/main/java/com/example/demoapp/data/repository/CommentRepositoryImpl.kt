package com.example.demoapp.data.repository

import com.example.demoapp.domain.model.Comment
import com.example.demoapp.domain.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor() : CommentRepository {

    private val commentsByPoint = MutableStateFlow(Comment.SAMPLE_BY_POINT)

    override fun observeByPoint(pointId: String): Flow<List<Comment>> {
        return commentsByPoint.map { byPoint ->
            byPoint[pointId].orEmpty().sortedByDescending { it.createdAt }
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
        if (cleanText.isBlank()) {
            return Result.failure(IllegalArgumentException("El comentario no puede estar vacio"))
        }

        val newComment = Comment(
            id = "c_${System.currentTimeMillis()}",
            pointId = pointId,
            authorId = authorId,
            authorName = authorName,
            authorAvatarUrl = authorAvatarUrl,
            text = cleanText,
            createdAt = System.currentTimeMillis()
        )

        val updatedMap = commentsByPoint.value.toMutableMap()
        val currentList = updatedMap[pointId].orEmpty()
        updatedMap[pointId] = listOf(newComment) + currentList
        commentsByPoint.value = updatedMap
        return Result.success(newComment)
    }
}

