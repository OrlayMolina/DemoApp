package com.example.demoapp.domain.model

data class ReviewHistory(
    val id          : String,
    val pointTitle  : String,
    val category    : TouristPointCategory,
    val reviewedBy  : String,
    val reviewedAt  : Long,
    val action      : ReviewAction,
    val rejectionReason: String? = null
)

enum class ReviewAction { APPROVED, REJECTED }