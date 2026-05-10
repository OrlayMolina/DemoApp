package com.example.demoapp.data.model

import com.example.demoapp.domain.model.FollowRelation

data class FollowRelationDto(
    val followerId: String = "",
    val followingId: String = "",
    val createdAt: Long = 0L
) {
    fun toDomain(): FollowRelation = FollowRelation(
        followerId = followerId,
        followingId = followingId,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: FollowRelation): FollowRelationDto = FollowRelationDto(
            followerId = domain.followerId,
            followingId = domain.followingId,
            createdAt = domain.createdAt
        )
    }
}
