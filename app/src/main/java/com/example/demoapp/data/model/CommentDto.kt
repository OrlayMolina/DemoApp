package com.example.demoapp.data.model

import com.example.demoapp.domain.model.Comment

data class CommentDto(
    val id: String = "",
    val pointId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String? = null,
    val text: String = "",
    val createdAt: Long = 0L
) {
    fun toDomain(): Comment = Comment(
        id = id,
        pointId = pointId,
        authorId = authorId,
        authorName = authorName,
        authorAvatarUrl = authorAvatarUrl,
        text = text,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: Comment): CommentDto = CommentDto(
            id = domain.id,
            pointId = domain.pointId,
            authorId = domain.authorId,
            authorName = domain.authorName,
            authorAvatarUrl = domain.authorAvatarUrl,
            text = domain.text,
            createdAt = domain.createdAt
        )
    }
}
