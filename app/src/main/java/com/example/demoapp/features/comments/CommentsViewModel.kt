package com.example.demoapp.features.comments

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.domain.model.Comment
import com.example.demoapp.domain.repository.CommentRepository
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class CommentsUiState(
    val isLoading: Boolean = true,
    val comments: List<Comment> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    var commentInput by mutableStateOf("")
        private set

    private var currentPointId: String? = null
    private var observeJob: Job? = null

    fun loadComments(pointId: String) {
        if (currentPointId == pointId && observeJob != null) return
        currentPointId = pointId
        observeJob?.cancel()

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        observeJob = viewModelScope.launch {
            commentRepository.observeByPoint(pointId).collectLatest { comments ->
                _uiState.value = CommentsUiState(
                    isLoading = false,
                    comments = comments,
                    errorMessage = null
                )
            }
        }
    }

    fun onCommentInputChange(value: String) {
        commentInput = value
    }

    fun publishComment() {
        val pointId = currentPointId ?: return
        val currentUser = userRepository.currentUser.value
        val result = commentRepository.addComment(
            pointId = pointId,
            authorId = currentUser?.id ?: "anon",
            authorName = currentUser?.name ?: "Tu",
            text = commentInput,
            authorAvatarUrl = currentUser?.profilePictureUrl
        )

        result.onSuccess {
            commentInput = ""
            _uiState.value = _uiState.value.copy(errorMessage = null)
        }.onFailure { error ->
            _uiState.value = _uiState.value.copy(
                errorMessage = error.message ?: "No se pudo publicar el comentario"
            )
        }
    }
}

