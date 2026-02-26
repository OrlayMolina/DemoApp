package com.example.demoapp.features.recovery

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

class PasswordRecoveryViewModel : ViewModel() {

    val email = ValidatedField(initialValue = "", validate = {
        when {
            it.isEmpty() -> "El email es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(it).matches() -> "Ingresa un email válido"
            else -> null
        }
    })

    var recoveryResult by mutableStateOf<RequestResult<String>?>(null)
        private set

    fun sendRecoveryEmail() {
        viewModelScope.launch {
            recoveryResult = RequestResult.Loading
            delay(1500)
            recoveryResult = if (email.value == "noexiste@test.com") {
                RequestResult.Error("No existe una cuenta con este email")
            } else {
                RequestResult.Success("Se envió un código a ${email.value}")
            }
        }
    }
}