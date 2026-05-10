package com.example.demoapp.data.model

import com.example.demoapp.domain.model.PostReport
import com.example.demoapp.domain.model.ReportReason

data class PostReportDto(
    val id: String = "",
    val postId: String = "",
    val reporterId: String = "",
    val reasonName: String = "",
    val details: String? = null,
    val createdAt: Long = 0L
) {
    fun toDomain(): PostReport = PostReport(
        id = id,
        postId = postId,
        reporterId = reporterId,
        reason = try { ReportReason.valueOf(reasonName) } catch (e: Exception) { ReportReason.OTHER },
        details = details,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: PostReport): PostReportDto = PostReportDto(
            id = domain.id,
            postId = domain.postId,
            reporterId = domain.reporterId,
            reasonName = domain.reason.name,
            details = domain.details,
            createdAt = domain.createdAt
        )
    }
}
