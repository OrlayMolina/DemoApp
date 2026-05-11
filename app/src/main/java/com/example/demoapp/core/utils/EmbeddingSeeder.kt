package com.example.demoapp.core.utils

import android.util.Log
import com.example.demoapp.BuildConfig
import com.example.demoapp.domain.repository.AiRepository
import com.example.demoapp.domain.repository.TouristPointRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Genera embeddings para los puntos turisticos de muestra que no los tienen.
 * Se ejecuta una vez por sesion en background al iniciar la app.
 */
@Singleton
class EmbeddingSeeder @Inject constructor(
    private val touristPointRepository: TouristPointRepository,
    private val aiRepository: AiRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var started = false

    fun start() {
        if (started) return
        started = true

        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            Log.d(TAG, "GEMINI_API_KEY vacia, omito seed de embeddings")
            return
        }

        scope.launch {
            val pending = touristPointRepository.touristPoints.value
                .filter { it.embedding.isEmpty() }
            if (pending.isEmpty()) {
                Log.d(TAG, "Sin puntos pendientes")
                return@launch
            }
            Log.d(TAG, "Generando embeddings para ${pending.size} puntos")
            var failures = 0
            for (point in pending) {
                val text = listOfNotNull(
                    point.title.takeIf { it.isNotBlank() },
                    point.description.takeIf { it.isNotBlank() },
                    point.category.name
                ).joinToString(". ")

                aiRepository.embedQuery(text).fold(
                    onSuccess = { vec ->
                        touristPointRepository.update(point.copy(embedding = vec))
                    },
                    onFailure = { e ->
                        failures++
                        Log.w(TAG, "Fallo embed punto ${point.id}: ${e.message}")
                        if (failures >= 3) {
                            Log.w(TAG, "Demasiados fallos, abortando seed")
                            return@launch
                        }
                    }
                )
                delay(150)
            }
            Log.d(TAG, "Seed completado")
        }
    }

    companion object {
        private const val TAG = "EmbeddingSeeder"
    }
}
