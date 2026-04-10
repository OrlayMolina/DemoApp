package com.example.demoapp.features.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.demoapp.domain.model.Comment
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.repository.TouristPointRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TouristPointDetailViewModel @Inject constructor(
    private val touristPointRepository: TouristPointRepository
) : ViewModel() {

    var point by mutableStateOf<TouristPoint?>(null)
        private set

    var isFollowing by mutableStateOf(false)
        private set

    var isLiked by mutableStateOf(false)
        private set

    // Criterios de revisión (solo moderador)
    var criteriaContent  by mutableStateOf(false)
    var criteriaImages   by mutableStateOf(false)
    var criteriaLocation by mutableStateOf(false)
    var criteriaDesc     by mutableStateOf(false)

    // Comentarios quemados
    val comments = listOf(
        Comment("1", "1", "u1", "Maria Garcia", null, "Increible lugar! Definitivamente tengo que visitarlo."),
        Comment("2", "1", "u2", "Carlos Admin", null, "Excelente fotografia. Gracias por compartir."),
        Comment("3", "1", "u3", "Ana Perez", null, "Lo visite el fin de semana pasado, muy recomendado."),
        Comment("4", "1", "u4", "Luis Martinez", null, "Saben si esta abierto los domingos?"),
    )

    fun loadPoint(touristPoint: TouristPoint) {
        point = touristPoint
    }

    fun toggleFollow() { isFollowing = !isFollowing }
    fun toggleLike()   { isLiked    = !isLiked }

    fun approvePoint(): Boolean {
        val currentPoint = point ?: return false
        val result = touristPointRepository.approvePoint(currentPoint.id)
        return result.fold(
            onSuccess = {
                point = currentPoint.copy(isVerified = true, isRejected = false, rejectionReason = null)
                true
            },
            onFailure = { false }
        )
    }

    fun rejectPoint(reason: String): Boolean {
        val currentPoint = point ?: return false
        val result = touristPointRepository.rejectPoint(currentPoint.id, reason)
        return result.fold(
            onSuccess = {
                point = currentPoint.copy(isVerified = false, isRejected = true, rejectionReason = reason)
                true
            },
            onFailure = { false }
        )
    }
}