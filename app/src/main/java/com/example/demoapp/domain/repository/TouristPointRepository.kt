package com.example.demoapp.domain.repository

import com.example.demoapp.domain.model.TouristPoint
import kotlinx.coroutines.flow.StateFlow

interface TouristPointRepository {
    // Para que el Inicio (Feed) se actualice solo
    val touristPoints: StateFlow<List<TouristPoint>>

    // El método que llamarás al dar clic en "Publicar"
    suspend fun save(point: TouristPoint): Result<Unit>

    // Para cuando quieras implementar la edición (findById)
    fun findById(id: String): TouristPoint?

    // Actualiza un punto existente
    fun update(point: TouristPoint): Result<Unit>

    // Borra un punto de la lista
    fun delete(id: String)

    // Moderación
    fun approvePoint(id: String): Result<Unit>
    fun rejectPoint(id: String, reason: String): Result<Unit>

    // Borradores: pasa un draft a "pendiente de revisión" (visible para moderadores)
    fun publishDraft(id: String): Result<Unit>

    // Visualizaciones: registra que un usuario vio la publicacion (unico por usuario)
    fun markVisit(pointId: String, userId: String): Result<Unit>
}