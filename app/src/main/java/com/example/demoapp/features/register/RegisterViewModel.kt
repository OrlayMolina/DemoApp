// features/register/RegisterViewModel.kt
package com.example.demoapp.features.register

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.core.utils.RequestResult
import com.example.demoapp.core.utils.ValidatedField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    val name = ValidatedField(initialValue = "", validate = {
        if (it.isEmpty()) "El nombre es obligatorio" else null
    })

    val city = ValidatedField(initialValue = "", validate = {
        if (it.isEmpty()) "La ciudad es obligatoria" else null
    })

    val address = ValidatedField(initialValue = "", validate = {
        if (it.isEmpty()) "La dirección es obligatoria" else null
    })

    val email = ValidatedField(initialValue = "", validate = {
        when {
            it.isEmpty() -> "El email es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(it).matches() -> "Ingresa un email válido"
            else -> null
        }
    })

    val password = ValidatedField(initialValue = "", validate = {
        when {
            it.isEmpty() -> "La contraseña es obligatoria"
            it.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }
    })

    val confirmPassword = ValidatedField(initialValue = "", validate = {
        when {
            it.isEmpty() -> "Confirma tu contraseña"
            it != password.value -> "Las contraseñas no coinciden"
            else -> null
        }
    })

    val isFormValid: Boolean
        get() = name.isValid && city.isValid && address.isValid &&
                email.isValid && password.isValid && confirmPassword.isValid

    var registerResult by mutableStateOf<RequestResult<String>?>(null)
        private set

    fun register() {
        viewModelScope.launch {
            registerResult = RequestResult.Loading
            delay(1500) // Simula llamada a API
            registerResult = if (email.value == "error@test.com") {
                RequestResult.Error("Este email ya está registrado")
            } else {
                RequestResult.Success("Registro exitoso. Bienvenido ${name.value}")
            }
        }
    }
}