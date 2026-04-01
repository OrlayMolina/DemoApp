package com.example.demoapp.features.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.TouristPointCategory

data class Comment(
    val id        : String,
    val authorName: String,
    val avatarUrl : String? = null,
    val text      : String,
    val date      : String
)

class TouristPointDetailViewModel : ViewModel() {

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
        Comment("1", "María García",  null, "¡Increíble lugar! Definitivamente tengo que visitarlo.", "21/2/2026"),
        Comment("2", "Carlos Admin",  null, "Excelente fotografía. Gracias por compartir.",           "21/2/2026"),
        Comment("3", "Ana Pérez",     null, "Lo visité el fin de semana pasado, muy recomendado.",    "22/2/2026"),
        Comment("4", "Luis Martínez", null, "¿Saben si está abierto los domingos?",                  "22/2/2026"),
    )

    fun loadPoint(touristPoint: TouristPoint) {
        point = touristPoint
    }

    fun toggleFollow() { isFollowing = !isFollowing }
    fun toggleLike()   { isLiked    = !isLiked }

    fun approvePoint(): Boolean {
        // TODO: llamar a Firestore
        return true
    }

    fun rejectPoint(reason: String): Boolean {
        // TODO: llamar a Firestore
        return reason.isNotBlank()
    }
}