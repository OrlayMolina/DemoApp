package com.example.demoapp.features.login

import android.util.Patterns
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    // Estado para los campos de texto
    var email by mutableStateOf("")
        private set // Solo se puede modificar desde el ViewModel

    var password by mutableStateOf("")
        private set // Solo se puede modificar desde el ViewModel

    // Permite controlar cuándo mostrar los errores
    var showEmailError by mutableStateOf(false)
        private set // Solo se puede modificar desde el ViewModel

    var showPasswordError by mutableStateOf(false)
        private set // Solo se puede modificar desde el ViewModel

    // Mensajes de error. get() para que sean de solo lectura desde el exterior
    val emailError: String?
        get() = if (showEmailError) validateEmail(email) else null

    val passwordError: String?
        get() = if (showPasswordError) validatePassword(password) else null

    val isFormValid: Boolean
        get() = validateEmail(email) == null && validatePassword(password) == null

    fun onEmailChange(newEmail: String) {
        email = newEmail
        showEmailError = true
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        showPasswordError = true
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() -> "El email es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Ingresa un email válido"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "La contraseña es obligatoria"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
    }
}