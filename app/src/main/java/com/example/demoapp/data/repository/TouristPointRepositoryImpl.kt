package com.example.demoapp.data.repository

import android.util.Log
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.repository.TouristPointRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio siguiendo el patrón de diseño del profesor.
 * El uso de @Singleton asegura que los puntos publicados no se borren al navegar.
 */
@Singleton
class TouristPointRepositoryImpl @Inject constructor() : TouristPointRepository {

    // 1. Lista interna reactiva que inicia con tus datos quemados (SAMPLE_LIST)
    private val _touristPoints = MutableStateFlow<List<TouristPoint>>(TouristPoint.SAMPLE_LIST)

    // 2. Propiedad pública que el Feed (Inicio) observará para actualizarse solo
    override val touristPoints: StateFlow<List<TouristPoint>> = _touristPoints.asStateFlow()

    /**
     * Guarda un nuevo punto turístico.
     * Al usar +=, notificamos el cambio a toda la app de forma inmediata.
     */
    override fun save(point: TouristPoint) {
        try {
            // Agregamos el punto a la lista (se añade al final o al principio según prefieras)
            // Para que aparezca de primero en el Feed:
            _touristPoints.value = listOf(point) + _touristPoints.value

            Log.d("Repository", "Punto '${point.title}' guardado con éxito. Total: ${_touristPoints.value.size}")
        } catch (e: Exception) {
            Log.e("Repository", "Error al guardar el punto: ${e.message}")
        }
    }

    /**
     * Busca un punto por su ID único.
     * Útil para cargar los datos en la pantalla de edición que vimos antes.
     */
    override fun findById(id: String): TouristPoint? {
        return _touristPoints.value.find { it.id == id }
    }

    /**
     * Elimina un punto de la lista (opcional, para completar el CRUD)
     */
    override fun delete(id: String) {
        _touristPoints.value = _touristPoints.value.filterNot { it.id == id }
        Log.d("Repository", "Punto con ID $id eliminado.")
    }
}