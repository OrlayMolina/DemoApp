package com.example.demoapp.domain.repository

import com.example.demoapp.domain.model.PostReport
import com.example.demoapp.domain.model.ReportReason
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PostReportRepository {

    val reports: StateFlow<List<PostReport>>

    fun reportPost(
        postId: String,
        reporterId: String,
        reason: ReportReason,
        details: String? = null
    ): Result<PostReport>

    fun reportsForPost(postId: String): List<PostReport>
    fun observeReportsForPost(postId: String): Flow<List<PostReport>>
    fun hasUserReported(postId: String, reporterId: String): Boolean
}
