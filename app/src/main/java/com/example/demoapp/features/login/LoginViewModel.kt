package com.example.demoapp.features.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.demoapp.R
import com.example.demoapp.core.notifications.FcmTopicManager
import com.example.demoapp.core.utils.RequestResult
import com.example.demoapp.core.utils.ResourceProvider
import com.example.demoapp.core.utils.ValidatedField
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.model.UserRole
import com.example.demoapp.domain.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel(){

    private val _loginResult = MutableStateFlow<RequestResult<User>?>(null)
    val loginResult: StateFlow<RequestResult<User>?> = _loginResult

    // ── Campos validados ──────────────────────────────────────────────────────
    val email = ValidatedField<String>(
        initialValue = "",
        validate     = { value ->
            when {
                value.isEmpty() -> resourceProvider.getString(R.string.error_email_empty)
                !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> resourceProvider.getString(R.string.error_email_invalid)
                else -> null
            }
        }
    )

    val password = ValidatedField<String>(
        initialValue = "",
        validate     = { value ->
            when {
                value.isEmpty() -> resourceProvider.getString(R.string.error_password_empty)
                value.length < 6 -> resourceProvider.getString(R.string.error_password_short)
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

        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.login(email.value.trim(), password.value.trim())
            _loginResult.value = if (user != null) {
                registerForPushNotifications(user)
                RequestResult.Success(user)
            } else {
                RequestResult.Error(resourceProvider.getString(R.string.login_failure))
            }
        }
    }

    private fun registerForPushNotifications(user: User) {
        FcmTopicManager.subscribeToUserInbox(user.id)
        if (user.role == UserRole.ADMIN) {
            FcmTopicManager.subscribeToModerators()
        }
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            repository.updateFcmToken(user.id, token)
        }
    }

    fun resetResult() {
        _loginResult.value = null
    }
}



