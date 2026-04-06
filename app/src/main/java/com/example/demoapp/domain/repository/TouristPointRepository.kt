package com.example.demoapp.domain.repository

import com.example.demoapp.domain.model.TouristPoint
import kotlinx.coroutines.flow.StateFlow

interface TouristPointRepository {
    // Para que el Inicio (Feed) se actualice solo
    val touristPoints: StateFlow<List<TouristPoint>>

    // El método que llamarás al dar clic en "Publicar"
    fun save(point: TouristPoint)

    // Para cuando quieras implementar la edición (findById)
    fun findById(id: String): TouristPoint?

    // Borra un punto de la lista
    fun delete(id: String)

}