package com.example.demoapp.features.recovery

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordRecoveryViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    val email = ValidatedField(initialValue = "", validate = {
        when {
            it.isEmpty() -> resourceProvider.getString(R.string.error_email_empty)
            !Patterns.EMAIL_ADDRESS.matcher(it).matches() -> resourceProvider.getString(R.string.error_email_invalid)
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
                RequestResult.Error(resourceProvider.getString(R.string.recovery_email_not_found))
            } else {
                RequestResult.Success(
                    resourceProvider.getString(R.string.recovery_success_code_sent, email.value)
                )
            }
        }
    }
}