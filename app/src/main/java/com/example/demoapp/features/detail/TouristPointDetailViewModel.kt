package com.example.demoapp.features.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.domain.model.Comment
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.repository.CommentRepository
import com.example.demoapp.domain.repository.TouristPointRepository
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthorUiState(
    val name: String = "Autor",
    val email: String = "",
    val initials: String = "AU",
    val publicationsCount: Int = 0
)

@HiltViewModel
class TouristPointDetailViewModel @Inject constructor(
    private val touristPointRepository: TouristPointRepository,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val followedAuthorIds = mutableSetOf<String>()

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
    private var currentPointId: String? = null

    fun loadPoint(touristPoint: TouristPoint) {
        point = touristPoint
        observeComments(touristPoint.id)
        resolveAuthor(touristPoint)
        isFollowing = followedAuthorIds.contains(normalizeUserId(touristPoint.authorId))
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
        val currentUserId = normalizeUserId(currentUser.id)

        // Evita autoseguirse
        if (authorId == currentUserId) return

        val targetUser = userRepository.findById(authorId)
            ?: userRepository.findById(currentPoint.authorId)

        val willFollow = !isFollowing
        val newFollowingCount = if (willFollow) {
            currentUser.following + 1
        } else {
            (currentUser.following - 1).coerceAtLeast(0)
        }

        val currentUpdate = userRepository.update(
            currentUser.copy(following = newFollowingCount)
        )

        if (currentUpdate.isFailure) return

        // Actualiza también seguidores del autor cuando exista en repositorio
        targetUser?.let { author ->
            val newFollowersCount = if (willFollow) {
                author.followers + 1
            } else {
                (author.followers - 1).coerceAtLeast(0)
            }
            userRepository.update(author.copy(followers = newFollowersCount))
        }

        if (willFollow) {
            followedAuthorIds.add(authorId)
        } else {
            followedAuthorIds.remove(authorId)
        }
        isFollowing = willFollow
    }

    private fun normalizeUserId(rawId: String): String {
        return rawId.removePrefix("user_")
    }
    fun toggleLike()   { isLiked    = !isLiked }

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