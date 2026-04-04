package com.example.demoapp.features.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.demoapp.core.utils.RequestResult
import com.example.demoapp.core.utils.ValidatedField
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.model.UserRole
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel(){

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

        val user = repository.login(email.value,password.value)
        _loginResult.value = if (user != null){
            RequestResult.Success(user.role)
        } else {
            RequestResult.Error("Credenciales incorrectas")
        }
    }

    fun resetResult() {
        _loginResult.value = null
    }
}



