package com.example.demoapp.features.reset

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.R
import com.example.demoapp.core.utils.RequestResult
import com.example.demoapp.core.utils.ResourceProvider
import com.example.demoapp.core.utils.ValidatedField
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordResetViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    // 5 campos separados, cada uno valida un solo dígito
    val codeDigits = List(5) { index ->
        ValidatedField(initialValue = "", validate = {
            when {
                it.isEmpty() -> resourceProvider.getString(R.string.error_required)
                it.length > 1 -> resourceProvider.getString(R.string.error_single_digit)
                !it.first().isDigit() -> resourceProvider.getString(R.string.error_numbers_only)
                else -> null
            }
        })
    }

    val newPassword = ValidatedField(initialValue = "", validate = {
        when {
            it.isEmpty()   -> resourceProvider.getString(R.string.error_password_empty)
            it.length < 6  -> resourceProvider.getString(R.string.error_password_short)
            !it.any { c -> c.isUpperCase() } -> resourceProvider.getString(R.string.error_password_uppercase)
            !it.any { c -> c.isDigit() }     -> resourceProvider.getString(R.string.error_password_number)
            else -> null
        }
    })

    val confirmPassword = ValidatedField(initialValue = "", validate = {
        when {
            it.isEmpty()             -> resourceProvider.getString(R.string.error_confirm_password_required)
            it != newPassword.value  -> resourceProvider.getString(R.string.error_passwords_do_not_match)
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
                RequestResult.Error(resourceProvider.getString(R.string.reset_error_code_invalid))
            } else {
                RequestResult.Success(resourceProvider.getString(R.string.reset_success))
            }
        }
    }
}