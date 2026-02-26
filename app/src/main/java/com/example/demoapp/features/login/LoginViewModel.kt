package com.example.demoapp.features.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.demoapp.core.utils.ValidatedField

class LoginViewModel : ViewModel() {

    val email = ValidatedField<String>(
        initialValue = "",
        validate = { value ->
            when {
                value.isEmpty() -> "El email es obligatorio"
                !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Ingresa un email válido"
                else -> null
            }
        }
    )

    val password = ValidatedField<String>(
        initialValue = "",
        validate = { value ->
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
}