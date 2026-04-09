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
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.model.UserLevel
import com.example.demoapp.domain.model.UserRole
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val userRepository: UserRepository)
    : ViewModel() {

    val name = ValidatedField(initialValue = "", validate = {
        if (it.isEmpty()) "El nombre es obligatorio" else null
    })

    /*val city = ValidatedField(initialValue = "", validate = {
        if (it.isEmpty()) "La ciudad es obligatoria" else null
    })

    val address = ValidatedField(initialValue = "", validate = {
        if (it.isEmpty()) "La dirección es obligatoria" else null
    })*/

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
        get() = name.isValid &&
                email.isValid && password.isValid && confirmPassword.isValid

    var registerResult by mutableStateOf<RequestResult<String>?>(null)
        private set

    fun register() {
        viewModelScope.launch {
            registerResult = RequestResult.Loading

            // Simulación de delay para que se vea el cargando
            delay(1000)

            // Verifica si el email ya existe
            val existingUser = userRepository.findById(
                userRepository.users.value.find { it.email == email.value }?.id ?: ""
            )

            if (existingUser != null) {
                registerResult = RequestResult.Error("Este email ya está registrado")
                return@launch
            }

            val newUser = User(
                id = UUID.randomUUID().toString(),
                name = name.value,
                city = "No especificada",
                address = "No especificada",
                email = email.value,
                password = password.value,
                role = UserRole.USER,
                level = UserLevel.NOVATO
            )

            userRepository.save(newUser)
            registerResult = RequestResult.Success("Registro exitoso. Bienvenido ${name.value}")
        }
    }
}