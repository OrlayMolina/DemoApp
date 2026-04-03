package com.example.demoapp.features.users.detail

import androidx.lifecycle.ViewModel
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    fun loadUserById(userId: String) {
        // Obtenemos el usuario del repo y actualizamos el estado
        _user.value = repository.findById(userId)


        /*
    fun findById(userId: String): User? {
        return repository.findById(userId)
    }*/
    }
}

/*
class UserDetailViewModel : ViewModel() {

    // Temporary local data source.
    // Later you can replace this with repository/database/API.
    private val mockUsers = listOf(
        User(
            id = "1",
            name = "Juan Pérez",
            email = "juan@example.com",
            city = "Armenia",
            address = "Cra 14 # 24-42",
            password = "Qw3erty123!"
        ),
        User(
            id = "2",
            name = "María Gómez",
            email = "maria@example.com",
            city = "Armenia",
            address = "Cra 14 # 24-42",
            password = "Qw3erty123!"
        ),
        User(
            id = "3",
            name = "Carlos Ruiz",
            email = "carlos@example.com",
            city = "Armenia",
            address = "Cra 14 # 24-42",
            password = "Qw3erty123!"
        )
    )

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    fun loadUserById(userId: String) {
        _user.value = mockUsers.find { it.id == userId }
    }
}*/