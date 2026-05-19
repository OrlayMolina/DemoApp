package com.example.demoapp.features.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.data.ImageRepository
import com.example.demoapp.data.datastore.UiPreferencesDataStore
import com.example.demoapp.domain.repository.ProfileRepository
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val uiPreferencesDataStore: UiPreferencesDataStore
) : ViewModel() {

    private val imageRepository = ImageRepository()

    var name by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var bio by mutableStateOf("")
        private set
    var profilePictureUrl by mutableStateOf("")
        private set

    var darkModeEnabled by mutableStateOf(false)
        private set

    var saveMessage by mutableStateOf<String?>(null)
        private set

    var isUploadingPhoto by mutableStateOf(false)
        private set

    var photoUploadError by mutableStateOf<String?>(null)
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

        viewModelScope.launch {
            uiPreferencesDataStore.darkModeEnabledFlow.collectLatest { enabled ->
                darkModeEnabled = enabled
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

    fun uploadProfilePicture(context: Context, uri: Uri) {
        val localPreview = uri.toString()
        profilePictureUrl = localPreview
        viewModelScope.launch {
            isUploadingPhoto = true
            photoUploadError = null
            val remoteUrl = imageRepository.uploadImageFromUri(context, uri)
            if (remoteUrl != null) {
                profilePictureUrl = remoteUrl
                val currentUserId = userRepository.currentUser.value?.id
                if (currentUserId != null) {
                    userRepository.updateProfilePicture(currentUserId, remoteUrl)
                        .onFailure { Log.w("EditProfileViewModel", "No se pudo persistir la foto de perfil: ${it.message}") }
                }
            } else {
                photoUploadError = "No se pudo subir la imagen"
                Log.w("EditProfileViewModel", "Fallo la subida de la foto de perfil: $localPreview")
            }
            isUploadingPhoto = false
        }
    }

    fun onDarkModeEnabledChange(enabled: Boolean) {
        darkModeEnabled = enabled
        viewModelScope.launch {
            uiPreferencesDataStore.setDarkModeEnabled(enabled)
        }
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

