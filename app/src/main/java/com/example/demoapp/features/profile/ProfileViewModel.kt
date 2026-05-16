@file:Suppress("unused")

package com.example.demoapp.features.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.R
import com.example.demoapp.core.utils.RequestResult
import com.example.demoapp.core.utils.ResourceProvider
import com.example.demoapp.core.utils.ValidatedField
import com.example.demoapp.data.datastore.SessionDataStore
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.repository.ProfileRepository
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val sessionDataStore: SessionDataStore,
    private val resources: ResourceProvider
) : ViewModel() {

    private val _updateResult = MutableStateFlow<RequestResult<String>?>(null)
    val updateResult: StateFlow<RequestResult<String>?> = _updateResult.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    val name = ValidatedField(initialValue = "", validate = { value ->
        when {
            value.isBlank() -> resources.getString(R.string.error_name_required)
            value.length > 30 -> resources.getString(R.string.error_name_length)
            else -> null
        }
    })

    val city = ValidatedField(initialValue = "", validate = { value ->
        if (value.isBlank()) resources.getString(R.string.error_city_required) else null
    })

    val address = ValidatedField(initialValue = "", validate = { value ->
        if (value.isBlank()) resources.getString(R.string.error_address_required) else null
    })

    val phone = ValidatedField(initialValue = "", validate = { value ->
        when {
            value.isBlank() -> resources.getString(R.string.error_phone_required)
            !value.matches(Regex("^[0-9]{10}$")) -> resources.getString(R.string.error_phone_invalid)
            else -> null
        }
    })

    val isFormValid: Boolean
        get() = name.isValid && city.isValid && address.isValid && phone.isValid

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val session = sessionDataStore.sessionFlow.first()
            if (session != null) {
                loadUser(session.userId)
            } else {
                clearLoadedUser()
            }
        }
    }

    private fun clearLoadedUser() {
        _user.value = null
        name.reset()
        city.reset()
        address.reset()
        phone.reset()
        _photoUri.value = null
    }

    private fun syncFormFromUser(currentUser: User) {
        name.initialize(currentUser.name)
        city.initialize(currentUser.city)
        address.initialize(currentUser.address)
        phone.initialize(currentUser.phoneNumber)
        _photoUri.value = currentUser.profilePictureUrl.takeIf { it.isNotBlank() }?.let(Uri::parse)
    }

    fun loadUser(userId: String) {
        val foundUser = repository.findById(userId)
        _user.value = foundUser
        if (foundUser != null) {
            syncFormFromUser(foundUser)
        } else {
            clearLoadedUser()
        }
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
        if (_isEditMode.value) {
            _user.value?.let { syncFormFromUser(it) }
        }
    }

    fun onPhotoSelected(uri: Uri?) {
        _photoUri.value = uri
    }

    fun saveChanges(): Boolean {
        if (!isFormValid) {
            _updateResult.value = RequestResult.Error(resources.getString(R.string.profile_update_invalid_form))
            return false
        }

        val currentUser = _user.value
            ?: run {
                _updateResult.value = RequestResult.Error(resources.getString(R.string.profile_update_no_user))
                return false
            }

        val updatedUser = currentUser.copy(
            name = name.value.trim(),
            city = city.value.trim(),
            address = address.value.trim(),
            phoneNumber = phone.value.trim(),
            profilePictureUrl = _photoUri.value?.toString() ?: currentUser.profilePictureUrl
        )

        return repository.update(updatedUser).fold(
            onSuccess = {
                _user.value = updatedUser
                _updateResult.value = RequestResult.Success(resources.getString(R.string.profile_updated_successfully))
                _isEditMode.value = false
                true
            },
            onFailure = { error ->
                _updateResult.value = RequestResult.Error(error.message ?: resources.getString(R.string.profile_update_failed))
                false
            }
        )
    }

    fun cancelEdit() {
        _user.value?.let { syncFormFromUser(it) }
        _isEditMode.value = false
    }

    fun clearResult() {
        _updateResult.value = null
    }

    val myPublications: StateFlow<List<TouristPoint>> = profileRepository.observeMyPublications().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val followers: StateFlow<Int> = profileRepository.observeFollowers().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0
    )

    val following: StateFlow<Int> = profileRepository.observeFollowing().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0
    )
}

