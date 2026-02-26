package com.example.demoapp.features.reset

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.core.utils.RequestResult
import com.example.demoapp.core.utils.ValidatedField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PasswordResetViewModel : ViewModel() {

    // 5 campos separados, cada uno valida un solo dígito
    val codeDigits = List(5) { index ->
        ValidatedField(initialValue = "", validate = {
            when {
                it.isEmpty() -> "Requerido"
                it.length > 1 -> "Solo un dígito"
                !it.first().isDigit() -> "Solo números"
                else -> null
            }
        })
    }

    val newPassword = ValidatedField(initialValue = "", validate = {
        when {
            it.isEmpty()   -> "La contraseña es obligatoria"
            it.length < 6  -> "Mínimo 6 caracteres"
            !it.any { c -> c.isUpperCase() } -> "Debe tener al menos una mayúscula"
            !it.any { c -> c.isDigit() }     -> "Debe tener al menos un número"
            else -> null
        }
    })

    val confirmPassword = ValidatedField(initialValue = "", validate = {
        when {
            it.isEmpty()             -> "Confirma tu contraseña"
            it != newPassword.value  -> "Las contraseñas no coinciden"
            else -> null
        }
    })

    val isFormValid: Boolean
        get() = codeDigits.all { it.isValid } && newPassword.isValid && confirmPassword.isValid

    var resetResult by mutableStateOf<RequestResult<String>?>(null)
        private set

    fun resetPassword() {
        val code = codeDigits.joinToString("") { it.value }
        viewModelScope.launch {
            resetResult = RequestResult.Loading
            delay(1500)
            resetResult = if (code == "00000") {
                RequestResult.Error("Código inválido o expirado")
            } else {
                RequestResult.Success("Contraseña restablecida correctamente")
            }
        }
    }
}