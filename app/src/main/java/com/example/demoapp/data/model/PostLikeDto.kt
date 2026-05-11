package com.example.demoapp.data.model

import com.example.demoapp.domain.model.PostLike

data class PostLikeDto(
    val postId: String = "",
    val userId: String = "",
    val createdAt: Long = 0L
) {
    fun toDomain(): PostLike = PostLike(
        postId = postId,
        userId = userId,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: PostLike): PostLikeDto = PostLikeDto(
            postId = domain.postId,
            userId = domain.userId,
            createdAt = domain.createdAt
        )
    }
}
