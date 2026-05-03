package com.example.demoapp.features.users.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.repository.FollowRepository
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
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    val isOwnProfile: StateFlow<Boolean> = combine(
        _user,
        userRepository.currentUser
    ) { viewed, current -> viewed != null && current != null && viewed.id == current.id }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isFollowing: StateFlow<Boolean> = combine(
        _user,
        userRepository.currentUser
    ) { viewed, current -> viewed?.id to current?.id }
        .flatMapLatest { (viewedId, currentId) ->
            if (viewedId == null || currentId == null || viewedId == currentId) {
                flowOf(false)
            } else {
                followRepository.observeIsFollowing(currentId, viewedId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val followersCount: StateFlow<Int> = _user
        .flatMapLatest { viewed ->
            if (viewed == null) flowOf(0) else followRepository.observeFollowersCount(viewed.id)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val followingCount: StateFlow<Int> = _user
        .flatMapLatest { viewed ->
            if (viewed == null) flowOf(0) else followRepository.observeFollowingCount(viewed.id)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun loadUserById(userId: String) {
        _user.value = userRepository.findById(userId)
    }

    fun toggleFollow() {
        val viewed = _user.value ?: return
        val current = userRepository.currentUser.value ?: return
        if (viewed.id == current.id) return

        if (followRepository.isFollowing(current.id, viewed.id)) {
            followRepository.unfollow(current.id, viewed.id)
        } else {
            followRepository.follow(current.id, viewed.id)
        }
    }
}
