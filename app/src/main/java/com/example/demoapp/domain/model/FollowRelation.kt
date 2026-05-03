package com.example.demoapp.domain.model

data class FollowRelation(
    val followerId: String,
    val followingId: String,
    val createdAt: Long = System.currentTimeMillis()
)
