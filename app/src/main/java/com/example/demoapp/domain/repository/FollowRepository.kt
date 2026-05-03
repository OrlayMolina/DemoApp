package com.example.demoapp.domain.repository

import com.example.demoapp.domain.model.FollowRelation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface FollowRepository {

    val relations: StateFlow<List<FollowRelation>>

    fun follow(followerId: String, followingId: String): Result<Unit>
    fun unfollow(followerId: String, followingId: String): Result<Unit>

    fun isFollowing(followerId: String, followingId: String): Boolean
    fun observeIsFollowing(followerId: String, followingId: String): Flow<Boolean>

    fun followersOf(userId: String): List<String>
    fun followingOf(userId: String): List<String>

    fun observeFollowersCount(userId: String): Flow<Int>
    fun observeFollowingCount(userId: String): Flow<Int>
}
