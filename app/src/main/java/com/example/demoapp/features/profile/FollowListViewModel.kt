package com.example.demoapp.features.profile

import androidx.lifecycle.ViewModel
import com.example.demoapp.domain.model.FollowRelation
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.repository.FollowRepository
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class FollowListViewModel @Inject constructor(
    userRepository: UserRepository,
    followRepository: FollowRepository
) : ViewModel() {

    val users: StateFlow<List<User>> = userRepository.users
    val relations: StateFlow<List<FollowRelation>> = followRepository.relations
}
