package com.example.demoapp.features.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    var name by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var bio by mutableStateOf("")
        private set
    var profilePictureUrl by mutableStateOf("")
        private set

    var saveMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            profileRepository.observeCurrentUser().collectLatest { user ->
                if (user != null && name.isBlank() && email.isBlank()) {
                    name = user.name
                    email = user.email
                    bio = user.bio
                    profilePictureUrl = user.profilePictureUrl
                }
            }
        }
    }

    fun onNameChange(value: String) {
        name = value
    }

    fun onEmailChange(value: String) {
        email = value
    }

    fun onBioChange(value: String) {
        bio = value
    }

    fun onProfilePictureChange(value: String) {
        profilePictureUrl = value
    }

    fun saveProfile(): Boolean {
        val result = profileRepository.updateProfile(
            name = name,
            email = email,
            bio = bio,
            profilePictureUrl = profilePictureUrl
        )
        return result.fold(
            onSuccess = {
                saveMessage = "Perfil actualizado"
                true
            },
            onFailure = {
                saveMessage = it.message ?: "No se pudo guardar"
                false
            }
        )
    }

    fun deleteCurrentAccount(): Boolean {
        val result = profileRepository.deleteCurrentAccount()
        return result.isSuccess
    }
}

