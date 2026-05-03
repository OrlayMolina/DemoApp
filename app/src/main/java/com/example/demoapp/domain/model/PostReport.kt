package com.example.demoapp.domain.model

data class PostReport(
    val id: String,
    val postId: String,
    val reporterId: String,
    val reason: ReportReason,
    val details: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
