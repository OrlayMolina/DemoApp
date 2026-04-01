package com.example.demoapp.features.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.demoapp.core.utils.RequestResult
import com.example.demoapp.core.utils.ValidatedField
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {

    // ── Usuarios de prueba ────────────────────────────────────────────────────
    private val _users = MutableStateFlow<List<User>>(emptyList())

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

    // ── Resultado del login ───────────────────────────────────────────────────
    private val _loginResult = MutableStateFlow<RequestResult<UserRole>?>(null)
    val loginResult: StateFlow<RequestResult<UserRole>?> = _loginResult

    // ── Campos validados ──────────────────────────────────────────────────────
    val email = ValidatedField<String>(
        initialValue = "",
        validate     = { value ->
            when {
                value.isEmpty() -> "El email es obligatorio"
                !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Ingresa un email válido"
                else -> null
            }
        }
    )

    val password = ValidatedField<String>(
        initialValue = "",
        validate     = { value ->
            when {
                value.isEmpty() -> "La contraseña es obligatoria"
                value.length < 6 -> "Debe tener al menos 6 caracteres"
                else -> null
            }
        }
    )

    val isFormValid: Boolean
        get() = email.isValid && password.isValid

    fun onEmailChange(newEmail: String) {
        email.onChange(newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        password.onChange(newPassword)
    }

    fun login() {
        _loginResult.value = RequestResult.Loading

        val user = _users.value.find {
            it.email == email.value && it.password == password.value
        }

        _loginResult.value = if (user != null) {
            RequestResult.Success(user.role)
        } else {
            RequestResult.Error("Credenciales incorrectas")
        }
    }

    fun resetResult() {
        _loginResult.value = null
    }
}