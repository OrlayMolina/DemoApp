package com.example.demoapp.features.users.list

import androidx.lifecycle.ViewModel
import com.example.demoapp.domain.model.FollowRelation
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.repository.FollowRepository
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class UserListViewModel @Inject constructor(
    private val repository: UserRepository,
    private val followRepository: FollowRepository
) : ViewModel() {

    val users: StateFlow<List<User>> = repository.users
    val relations: StateFlow<List<FollowRelation>> = followRepository.relations

    fun followersCount(userId: String): Int =
        relations.value.count { it.followingId == userId }

    fun followingCount(userId: String): Int =
        relations.value.count { it.followerId == userId }

    fun banUser(userId: String, reason: String) {
        repository.banUser(userId, reason)
    }

    fun unbanUser(userId: String) {
        repository.unbanUser(userId)
    }
}


/*class UserListViewModel : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init {
        loadUsers()
    }

    private fun loadUsers() {
        _users.value = listOf(
            User(
                id       = "1",
                name     = "Juan García",
                email    = "juan@email.com",
                password = "123456",
                city     = "Armenia",
                address  = "",
                role     = UserRole.USER
            ),
            User(
                id       = "2",
                name     = "Carlos Admin",
                email    = "admin@demo.com",
                password = "admin123",
                city     = "Armenia",
                address  = "",
                role     = UserRole.ADMIN
            )
        )
    }
}*/