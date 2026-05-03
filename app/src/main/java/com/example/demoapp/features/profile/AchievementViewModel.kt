package com.example.demoapp.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.domain.model.Achievement
import com.example.demoapp.domain.repository.AchievementRepository
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val achievements: StateFlow<List<Achievement>> = userRepository.currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else achievementRepository.observeAchievements(user.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
