package com.example.demoapp.domain.repository

import com.example.demoapp.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {

    fun observeByPoint(pointId: String): Flow<List<Comment>>

    fun addComment(
        pointId: String,
        authorId: String,
        authorName: String,
        text: String,
        authorAvatarUrl: String? = null
    ): Result<Comment>
}

