package com.example.demoapp.domain.repository

import com.example.demoapp.domain.model.PostLike
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface LikeRepository {

    val likes: StateFlow<List<PostLike>>

    fun like(postId: String, userId: String): Result<Unit>
    fun unlike(postId: String, userId: String): Result<Unit>
    fun toggle(postId: String, userId: String): Result<Boolean>

    fun isLiked(postId: String, userId: String): Boolean
    fun observeIsLiked(postId: String, userId: String): Flow<Boolean>

    fun observeLikedPostIds(userId: String): Flow<Set<String>>
    fun likeCount(postId: String): Int
}
