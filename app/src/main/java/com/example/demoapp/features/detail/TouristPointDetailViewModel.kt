package com.example.demoapp.features.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.core.notifications.FcmTopicManager
import com.example.demoapp.domain.model.Comment
import com.example.demoapp.domain.model.Notification
import com.example.demoapp.domain.model.NotificationType
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.repository.CommentRepository
import com.example.demoapp.domain.repository.FollowRepository
import com.example.demoapp.domain.repository.LikeRepository
import com.example.demoapp.domain.repository.NotificationRepository
import com.example.demoapp.domain.repository.TouristPointRepository
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AuthorUiState(
    val id: String = "",
    val name: String = "Autor",
    val email: String = "",
    val initials: String = "AU",
    val publicationsCount: Int = 0
)

@HiltViewModel
class TouristPointDetailViewModel @Inject constructor(
    private val touristPointRepository: TouristPointRepository,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository,
    private val likeRepository: LikeRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    var point by mutableStateOf<TouristPoint?>(null)
        private set

    var isFollowing by mutableStateOf(false)
        private set

    var isLiked by mutableStateOf(false)
        private set

    // Criterios de revisión (solo moderador)
    var criteriaContent  by mutableStateOf(false)
    var criteriaImages   by mutableStateOf(false)
    var criteriaLocation by mutableStateOf(false)
    var criteriaDesc     by mutableStateOf(false)

    var comments by mutableStateOf<List<Comment>>(emptyList())
        private set

    var authorUiState by mutableStateOf(AuthorUiState())
        private set

    private var commentsJob: Job? = null
    private var followJob: Job? = null
    private var likeJob: Job? = null
    private var pointJob: Job? = null
    private var currentPointId: String? = null

    fun loadPoint(touristPoint: TouristPoint) {
        point = touristPoint
        observeComments(touristPoint.id)
        observePoint(touristPoint.id)
        resolveAuthor(touristPoint)
        observeFollowState(touristPoint)
        observeLikeState(touristPoint)
    }

    private fun observePoint(pointId: String) {
        pointJob?.cancel()
        pointJob = viewModelScope.launch {
            touristPointRepository.touristPoints.collectLatest { list ->
                list.firstOrNull { it.id == pointId }?.let { point = it }
            }
        }
    }

    private fun observeLikeState(touristPoint: TouristPoint) {
        likeJob?.cancel()
        val currentUserId = userRepository.currentUser.value?.id ?: run {
            isLiked = false
            return
        }
        likeJob = viewModelScope.launch {
            likeRepository.observeIsLiked(touristPoint.id, currentUserId).collectLatest { value ->
                isLiked = value
            }
        }
    }

    private fun observeFollowState(touristPoint: TouristPoint) {
        followJob?.cancel()
        val authorId = normalizeUserId(touristPoint.authorId)
        val currentUserId = userRepository.currentUser.value?.id
        if (currentUserId == null || currentUserId == authorId) {
            isFollowing = false
            return
        }
        followJob = viewModelScope.launch {
            followRepository.observeIsFollowing(currentUserId, authorId).collectLatest { value ->
                isFollowing = value
            }
        }
    }

    private fun resolveAuthor(touristPoint: TouristPoint) {
        val author = userRepository.findById(touristPoint.authorId)
            ?: userRepository.findById(touristPoint.authorId.removePrefix("user_"))

        val authorName = author?.name?.takeIf { it.isNotBlank() } ?: "Autor"
        val initials = authorName
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
            .joinToString("")
            .ifBlank { "AU" }

        val publicationsCount = touristPointRepository.touristPoints.value.count { pointItem ->
            pointItem.authorId == touristPoint.authorId ||
                pointItem.authorId.removePrefix("user_") == touristPoint.authorId.removePrefix("user_")
        }

        authorUiState = AuthorUiState(
            id = author?.id ?: normalizeUserId(touristPoint.authorId),
            name = authorName,
            email = author?.email.orEmpty(),
            initials = initials,
            publicationsCount = publicationsCount
        )
    }

    private fun observeComments(pointId: String) {
        if (currentPointId == pointId && commentsJob != null) return
        currentPointId = pointId
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            commentRepository.observeByPoint(pointId).collectLatest { list ->
                comments = list
            }
        }
    }

    fun toggleFollow() {
        val currentPoint = point ?: return
        val currentUser = userRepository.currentUser.value ?: return
        val authorId = normalizeUserId(currentPoint.authorId)
        val currentUserId = currentUser.id

        // Evita autoseguirse
        if (authorId == currentUserId) return

        if (followRepository.isFollowing(currentUserId, authorId)) {
            followRepository.unfollow(currentUserId, authorId)
            FcmTopicManager.unsubscribeFromUserPublications(authorId)
        } else {
            followRepository.follow(currentUserId, authorId)
            FcmTopicManager.subscribeToUserPublications(authorId)
            notifyAuthorOfNewFollower(currentUser)
        }
    }

    private fun notifyAuthorOfNewFollower(follower: User) {
        val notification = Notification(
            id              = "",
            type            = NotificationType.FOLLOWER,
            userName        = follower.name,
            userAvatarUrl   = follower.profilePictureUrl.takeIf { it.isNotBlank() },
            date            = SimpleDateFormat("dd MMM, HH:mm", Locale("es")).format(Date()),
            createdAt       = System.currentTimeMillis(),
            isRead          = false,
            relatedEntityId = follower.id
        )
        notificationRepository.add(notification)
    }

    private fun normalizeUserId(rawId: String): String {
        return rawId.removePrefix("user_")
    }
    fun toggleLike() {
        val currentPoint = point ?: return
        val currentUserId = userRepository.currentUser.value?.id ?: return
        likeRepository.toggle(currentPoint.id, currentUserId)
    }

    fun approvePoint(): Boolean {
        val currentPoint = point ?: return false
        val result = touristPointRepository.approvePoint(currentPoint.id)
        return result.fold(
            onSuccess = {
                point = currentPoint.copy(isVerified = true, isRejected = false, rejectionReason = null)
                true
            },
            onFailure = { false }
        )
    }

    fun rejectPoint(reason: String): Boolean {
        val currentPoint = point ?: return false
        val result = touristPointRepository.rejectPoint(currentPoint.id, reason)
        return result.fold(
            onSuccess = {
                point = currentPoint.copy(isVerified = false, isRejected = true, rejectionReason = reason)
                true
            },
            onFailure = { false }
        )
    }
}