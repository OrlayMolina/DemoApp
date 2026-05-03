package com.example.demoapp.features.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.domain.model.ReportReason
import com.example.demoapp.domain.repository.PostReportRepository
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportPostUiState(
    val selectedReason: ReportReason? = null,
    val details: String = "",
    val isSubmitting: Boolean = false,
    val submitted: Boolean = false,
    val errorMessageRes: Int? = null
)

@HiltViewModel
class ReportPostViewModel @Inject constructor(
    private val postReportRepository: PostReportRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReportPostUiState())
    val state: StateFlow<ReportPostUiState> = _state.asStateFlow()

    fun selectReason(reason: ReportReason) {
        _state.update { it.copy(selectedReason = reason, errorMessageRes = null) }
    }

    fun updateDetails(value: String) {
        _state.update { it.copy(details = value) }
    }

    fun submit(postId: String, onDone: () -> Unit) {
        val current = _state.value
        val reason = current.selectedReason
        if (reason == null) {
            _state.update { it.copy(errorMessageRes = com.example.demoapp.R.string.report_error_no_reason) }
            return
        }

        val reporterId = userRepository.currentUser.value?.id
        if (reporterId == null) {
            _state.update { it.copy(errorMessageRes = com.example.demoapp.R.string.report_error_not_logged_in) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            val result = postReportRepository.reportPost(
                postId = postId,
                reporterId = reporterId,
                reason = reason,
                details = current.details
            )
            if (result.isSuccess) {
                _state.update { it.copy(isSubmitting = false, submitted = true) }
                onDone()
            } else {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessageRes = com.example.demoapp.R.string.report_error_submit_failed
                    )
                }
            }
        }
    }

    fun reset() {
        _state.value = ReportPostUiState()
    }
}
