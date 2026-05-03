package com.example.demoapp.data.repository

import com.example.demoapp.domain.model.PostLike
import com.example.demoapp.domain.repository.LikeRepository
import com.example.demoapp.domain.repository.TouristPointRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LikeRepositoryImpl @Inject constructor(
    private val touristPointRepository: TouristPointRepository
) : LikeRepository {

    private val _likes = MutableStateFlow<List<PostLike>>(emptyList())
    override val likes: StateFlow<List<PostLike>> = _likes.asStateFlow()

    override fun like(postId: String, userId: String): Result<Unit> {
        if (_likes.value.any { it.postId == postId && it.userId == userId }) {
            return Result.success(Unit)
        }
        val point = touristPointRepository.findById(postId)
            ?: return Result.failure(IllegalArgumentException("Post not found"))

        _likes.value = _likes.value + PostLike(postId, userId)
        touristPointRepository.update(point.copy(importantVotes = point.importantVotes + 1))
        return Result.success(Unit)
    }

    override fun unlike(postId: String, userId: String): Result<Unit> {
        val existed = _likes.value.any { it.postId == postId && it.userId == userId }
        if (!existed) return Result.success(Unit)

        _likes.value = _likes.value.filterNot { it.postId == postId && it.userId == userId }

        val point = touristPointRepository.findById(postId)
        if (point != null) {
            val newCount = (point.importantVotes - 1).coerceAtLeast(0)
            touristPointRepository.update(point.copy(importantVotes = newCount))
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
