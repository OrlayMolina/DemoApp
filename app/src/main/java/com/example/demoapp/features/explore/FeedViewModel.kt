package com.example.demoapp.features.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.repository.CommentRepository
import com.example.demoapp.domain.repository.LikeRepository
import com.example.demoapp.domain.repository.TouristPointRepository
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val touristPointRepository: TouristPointRepository,
    private val userRepository: UserRepository,
    private val likeRepository: LikeRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    val feed: StateFlow<List<TouristPoint>> = combine(
        touristPointRepository.touristPoints,
        commentRepository.observeCommentCounts()
    ) { points, counts ->
        points
            .filter { it.isVerified && !it.isRejected && !it.isSaved }
            .map { point -> point.copy(commentCount = counts[point.id] ?: point.commentCount) }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val likedIds: StateFlow<Set<String>> = userRepository.currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptySet()) else likeRepository.observeLikedPostIds(user.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet()
        )

    fun toggleLike(postId: String) {
        val userId = userRepository.currentUser.value?.id ?: return
        likeRepository.toggle(postId, userId)
    }
}
