package com.example.demoapp.features.users.list

import androidx.lifecycle.ViewModel
import com.example.demoapp.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserListViewModel : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    init {
        loadUsers()
    }

    private fun loadUsers() {
        _users.value = listOf(
            User("1", "Juan", "juan@email.com", "", "", "", "")
        )
    }
}