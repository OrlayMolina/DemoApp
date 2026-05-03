package com.example.demoapp.data.repository

import com.example.demoapp.domain.model.FollowRelation
import com.example.demoapp.domain.repository.FollowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowRepositoryImpl @Inject constructor() : FollowRepository {

    private val _relations = MutableStateFlow<List<FollowRelation>>(emptyList())
    override val relations: StateFlow<List<FollowRelation>> = _relations.asStateFlow()

    override fun follow(followerId: String, followingId: String): Result<Unit> {
        if (followerId == followingId) {
            return Result.failure(IllegalArgumentException("A user cannot follow themselves"))
        }
        val current = _relations.value
        if (current.any { it.followerId == followerId && it.followingId == followingId }) {
            return Result.success(Unit)
        }
        _relations.value = current + FollowRelation(followerId, followingId)
        return Result.success(Unit)
    }

    override fun unfollow(followerId: String, followingId: String): Result<Unit> {
        _relations.value = _relations.value.filterNot {
            it.followerId == followerId && it.followingId == followingId
        }
        return Result.success(Unit)
    }

    override fun isFollowing(followerId: String, followingId: String): Boolean {
        return _relations.value.any {
            it.followerId == followerId && it.followingId == followingId
        }
    }

    override fun observeIsFollowing(followerId: String, followingId: String): Flow<Boolean> {
        return _relations.map { list ->
            list.any { it.followerId == followerId && it.followingId == followingId }
        }
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
