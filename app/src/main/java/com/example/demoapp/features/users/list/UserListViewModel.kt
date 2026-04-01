package com.example.demoapp.features.users.list

import androidx.lifecycle.ViewModel
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.model.UserRole
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
}