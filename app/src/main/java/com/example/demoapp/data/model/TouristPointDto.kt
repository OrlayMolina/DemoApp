package com.example.demoapp.data.model

import com.example.demoapp.domain.model.PriceRange
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.TouristPointCategory

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class TouristPointDto(
    var id: String = "",
    var authorId: String = "",
    var title: String = "",
    var categoryName: String = "",
    var description: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var address: String = "",
    var schedule: String = "",
    var priceRangeName: String = "",
    var photoUrls: List<String> = emptyList(),
    @get:PropertyName("isVerified") @set:PropertyName("isVerified") var isVerified: Boolean = false,
    @get:PropertyName("isRejected") @set:PropertyName("isRejected") var isRejected: Boolean = false,
    var rejectionReason: String? = null,
    @get:PropertyName("isResolved") @set:PropertyName("isResolved") var isResolved: Boolean = false,
    @get:PropertyName("isReported") @set:PropertyName("isReported") var isReported: Boolean = false,
    var reportReason: String? = null,
    var importantVotes: Int = 0,
    var visitedByUserIds: List<String> = emptyList(),
    var commentCount: Int = 0,
    var createdAt: Long = 0L,
    var embedding: List<Double> = emptyList(),
    var aiTags: List<String> = emptyList(),
    @get:PropertyName("isSaved") @set:PropertyName("isSaved") var isSaved: Boolean = false,
    @get:PropertyName("isDraft") @set:PropertyName("isDraft") var isDraft: Boolean = false
) {
    fun toDomain(): TouristPoint = TouristPoint(
        id = id,
        authorId = authorId,
        title = title,
        category = try { TouristPointCategory.valueOf(categoryName) } catch (e: Exception) { TouristPointCategory.NATURE },
        description = description,
        latitude = latitude,
        longitude = longitude,
        address = address,
        schedule = schedule,
        priceRange = try { PriceRange.valueOf(priceRangeName) } catch (e: Exception) { PriceRange.FREE },
        photoUrls = photoUrls,
        isVerified = isVerified,
        isRejected = isRejected,
        rejectionReason = rejectionReason,
        isResolved = isResolved,
        isReported = isReported,
        reportReason = reportReason,
        importantVotes = importantVotes,
        visitedByUserIds = visitedByUserIds,
        commentCount = commentCount,
        createdAt = createdAt,
        embedding = embedding,
        aiTags = aiTags,
        isSaved = isSaved,
        isDraft = isDraft
    )

    companion object {
        fun fromDomain(domain: TouristPoint): TouristPointDto = TouristPointDto(
            id = domain.id,
            authorId = domain.authorId,
            title = domain.title,
            categoryName = domain.category.name,
            description = domain.description,
            latitude = domain.latitude,
            longitude = domain.longitude,
            address = domain.address,
            schedule = domain.schedule,
            priceRangeName = domain.priceRange.name,
            photoUrls = domain.photoUrls,
            isVerified = domain.isVerified,
            isRejected = domain.isRejected,
            rejectionReason = domain.rejectionReason,
            isResolved = domain.isResolved,
            isReported = domain.isReported,
            reportReason = domain.reportReason,
            importantVotes = domain.importantVotes,
            visitedByUserIds = domain.visitedByUserIds,
            commentCount = domain.commentCount,
            createdAt = domain.createdAt,
            embedding = domain.embedding,
            aiTags = domain.aiTags,
            isSaved = domain.isSaved,
            isDraft = domain.isDraft
        )
    }
}
