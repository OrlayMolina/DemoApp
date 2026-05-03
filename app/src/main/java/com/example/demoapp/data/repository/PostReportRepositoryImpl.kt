package com.example.demoapp.data.repository

import com.example.demoapp.domain.model.PostReport
import com.example.demoapp.domain.model.ReportReason
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.repository.PostReportRepository
import com.example.demoapp.domain.repository.TouristPointRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostReportRepositoryImpl @Inject constructor(
    private val touristPointRepository: TouristPointRepository
) : PostReportRepository {

    private val _reports = MutableStateFlow<List<PostReport>>(emptyList())
    override val reports: StateFlow<List<PostReport>> = _reports.asStateFlow()

    override fun reportPost(
        postId: String,
        reporterId: String,
        reason: ReportReason,
        details: String?
    ): Result<PostReport> {
        val point: TouristPoint = touristPointRepository.findById(postId)
            ?: return Result.failure(IllegalArgumentException("Post not found"))

        if (hasUserReported(postId, reporterId)) {
            return Result.failure(IllegalStateException("User has already reported this post"))
        }

        val report = PostReport(
            id = UUID.randomUUID().toString(),
            postId = postId,
            reporterId = reporterId,
            reason = reason,
            details = details?.takeIf { it.isNotBlank() }
        )
        _reports.value = _reports.value + report

        touristPointRepository.update(
            point.copy(
                isReported = true,
                reportReason = reportReasonText(reason, details)
            )
        )

        return Result.success(report)
    }

    override fun reportsForPost(postId: String): List<PostReport> {
        return _reports.value.filter { it.postId == postId }
    }

    override fun observeReportsForPost(postId: String): Flow<List<PostReport>> {
        return _reports.map { list -> list.filter { it.postId == postId } }
    }

    override fun hasUserReported(postId: String, reporterId: String): Boolean {
        return _reports.value.any { it.postId == postId && it.reporterId == reporterId }
    }

    private fun reportReasonText(reason: ReportReason, details: String?): String {
        val base = when (reason) {
            ReportReason.INAPPROPRIATE_CONTENT -> "Inappropriate content"
            ReportReason.FALSE_OR_MISLEADING_INFORMATION -> "False or misleading information"
            ReportReason.SPAM -> "Spam"
            ReportReason.HARASSMENT -> "Harassment"
            ReportReason.OTHER -> "Other"
        }
        return if (details.isNullOrBlank()) base else "$base — $details"
    }
}
