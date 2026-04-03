package com.example.demoapp.features.users.list

import androidx.lifecycle.ViewModel
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.model.UserRole
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel // Anotamos el ViewModel con @HiltViewModel para que Hilt pueda inyectarlo
class UserListViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    // Exponemos la lista de usuarios desde el repositorio como un StateFlow para que la UI pueda observar los cambios
    val users: StateFlow<List<User>> = repository.users

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