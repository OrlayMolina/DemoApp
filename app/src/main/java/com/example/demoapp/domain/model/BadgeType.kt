package com.example.demoapp.domain.model

enum class BadgeType {
    FIRST_PUBLICATION,
    TEN_VERIFIED,
    TOP_OF_MONTH,
    FIRST_COMMENT,
    MOST_VOTED
}

data class Badge(
    val id: String,
    val type: BadgeType,
    val name: String,
    val description: String,
    val iconUrl: String = "",
    val earnedAt: Long = System.currentTimeMillis()
)