package com.example.demoapp.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.repository.FollowRepository
import com.example.demoapp.domain.repository.TouristPointRepository
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VisitedProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository,
    private val touristPointRepository: TouristPointRepository
) : ViewModel() {

    private val _userId = MutableStateFlow<String?>(null)

    val user: StateFlow<User?> = _userId
        .map { id -> id?.let { userRepository.findById(it) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isOwnProfile: StateFlow<Boolean> = combine(
        _userId,
        userRepository.currentUser
    ) { viewedId, current -> viewedId != null && current != null && viewedId == current.id }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isFollowing: StateFlow<Boolean> = combine(
        _userId,
        userRepository.currentUser
    ) { viewedId, current -> viewedId to current?.id }
        .flatMapLatest { (viewedId, currentId) ->
            if (viewedId == null || currentId == null || viewedId == currentId) {
                flowOf(false)
            } else {
                followRepository.observeIsFollowing(currentId, viewedId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val followersCount: StateFlow<Int> = _userId
        .flatMapLatest { id ->
            if (id == null) flowOf(0) else followRepository.observeFollowersCount(id)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val followingCount: StateFlow<Int> = _userId
        .flatMapLatest { id ->
            if (id == null) flowOf(0) else followRepository.observeFollowingCount(id)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val publications: StateFlow<List<TouristPoint>> = combine(
        _userId,
        touristPointRepository.touristPoints
    ) { id, points ->
        if (id == null) emptyList()
        else points.filter { point ->
            (point.authorId == id || point.authorId.removePrefix("user_") == id) &&
                point.isVerified && !point.isRejected
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun load(userId: String) {
        _userId.value = userId
    }

    fun toggleFollow() {
        val viewedId = _userId.value ?: return
        val currentId = userRepository.currentUser.value?.id ?: return
        if (viewedId == currentId) return
        if (followRepository.isFollowing(currentId, viewedId)) {
            followRepository.unfollow(currentId, viewedId)
        } else {
            followRepository.follow(currentId, viewedId)
        }
    }
}
