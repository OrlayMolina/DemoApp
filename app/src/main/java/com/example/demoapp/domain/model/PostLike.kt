package com.example.demoapp.domain.model

data class PostLike(
    val postId: String,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis()
)
