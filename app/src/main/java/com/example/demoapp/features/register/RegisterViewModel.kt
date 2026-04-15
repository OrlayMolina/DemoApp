// features/register/RegisterViewModel.kt
package com.example.demoapp.features.register

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.R
import com.example.demoapp.core.utils.RequestResult
import com.example.demoapp.core.utils.ResourceProvider
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
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val resourceProvider: ResourceProvider
)
    : ViewModel() {

    private fun normalizeEmail(value: String): String = value.trim().lowercase()

    val name = ValidatedField(initialValue = "", validate = {
        if (it.isEmpty()) resourceProvider.getString(R.string.error_name_required) else null
    })

    /*val city = ValidatedField(initialValue = "", validate = {
        if (it.isEmpty()) "La ciudad es obligatoria" else null
    })

    val address = ValidatedField(initialValue = "", validate = {
        if (it.isEmpty()) "La dirección es obligatoria" else null
    })*/

    val email = ValidatedField(initialValue = "", validate = {
        when {
            it.isEmpty() -> resourceProvider.getString(R.string.error_email_empty)
            !Patterns.EMAIL_ADDRESS.matcher(it).matches() -> resourceProvider.getString(R.string.error_email_invalid)
            else -> null
        }
    })

    val password = ValidatedField(initialValue = "", validate = {
        when {
            it.isEmpty() -> resourceProvider.getString(R.string.error_password_empty)
            it.length < 6 -> resourceProvider.getString(R.string.error_password_short)
            else -> null
        }
    })

    val confirmPassword = ValidatedField(initialValue = "", validate = {
        when {
            it.isEmpty() -> resourceProvider.getString(R.string.error_confirm_password_required)
            it != password.value -> resourceProvider.getString(R.string.error_passwords_do_not_match)
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
            val normalizedEmail = normalizeEmail(email.value)
            val existingUser = userRepository.users.value.firstOrNull {
                it.email.trim().lowercase() == normalizedEmail
            }

            if (existingUser != null) {
                registerResult = RequestResult.Error(resourceProvider.getString(R.string.error_email_already_registered))
                return@launch
            }

            val newUser = User(
                id = UUID.randomUUID().toString(),
                name = name.value.trim(),
                city = "No especificada",
                address = "No especificada",
                email = normalizedEmail,
                password = password.value.trim(),
                role = UserRole.USER,
                level = UserLevel.NOVATO
            )

            userRepository.save(newUser)
            registerResult = RequestResult.Success(
                resourceProvider.getString(R.string.register_success_welcome, name.value)
            )
        }
    }
}