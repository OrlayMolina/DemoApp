package com.example.demoapp.features.create

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.core.utils.ImageLabeler
import com.example.demoapp.core.utils.RequestResult
import com.example.demoapp.core.utils.ValidatedField
import com.example.demoapp.domain.model.AiEnrichment
import com.example.demoapp.domain.model.PriceRange
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.TouristPointCategory
import com.example.demoapp.domain.repository.AiRepository
import com.example.demoapp.domain.repository.TouristPointRepository
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel compartido. Ahora inyecta el repositorio mediante Hilt
 * para que los datos persistan y se reflejen en el Feed.
 */
@HiltViewModel
class CreatePointViewModel @Inject constructor(
    private val repository: TouristPointRepository, // <── INYECCIÓN DEL REPO
    private val userRepository: UserRepository,
    private val aiRepository: AiRepository,
    private val imageLabeler: ImageLabeler
) : ViewModel() {

    // Repositorio simple para subir imágenes a ImgBB (se puede inyectar con Hilt si se desea)
    private val imageRepository = com.example.demoapp.data.ImageRepository()

    // Estados de subida de imagen
    var isUploadingImage by mutableStateOf(false)
        private set

    var imageUploadError by mutableStateOf<String?>(null)
        private set

    private var editingPoint: TouristPoint? = null

    // Fuente única para que Home/Mapa/Perfil reflejen nuevas publicaciones.
    val touristPoints: StateFlow<List<TouristPoint>> = repository.touristPoints

    // ── Paso 1: Información básica ────────────────────────────────────────────
    val title = ValidatedField(initialValue = "", validate = {
        when {
            it.isBlank() -> "El título es obligatorio"
            it.length > 80 -> "Máximo 80 caracteres"
            else -> null
        }
    })

    val description = ValidatedField(initialValue = "", validate = {
        when {
            it.length > 500 -> "Máximo 500 caracteres"
            else -> null
        }
    })

    val schedule = ValidatedField(initialValue = "", validate = {
        when {
            it.isNotBlank() && it.length < 3 -> "Horario inválido"
            else -> null
        }
    })

    var selectedCategory by mutableStateOf<TouristPointCategory?>(null)
        private set

    var categoryError by mutableStateOf<String?>(null)
        private set

    fun onCategoryChange(displayName: String) {
        selectedCategory = TouristPointCategory.fromDisplayName(displayName)
        categoryError = null
    }

    fun onCategoryChange(category: TouristPointCategory) {
        selectedCategory = category
        categoryError = null
    }

    var selectedPriceRange by mutableStateOf<PriceRange?>(null)
        private set

    var priceRangeError by mutableStateOf<String?>(null)
        private set

    fun onPriceRangeChange(displayName: String) {
        selectedPriceRange = PriceRange.fromDisplayName(displayName)
        priceRangeError = null
    }

    val isStep1Valid: Boolean
        get() = title.isValid && description.isValid && selectedCategory != null

    fun validateStep1(): Boolean {
        title.onChange(title.value)
        description.onChange(description.value)
        if (selectedCategory == null) categoryError = "Selecciona una categoría"
        return isStep1Valid
    }

    // ── Galería de fotos ──────────────────────────────────────────────────────
    val selectedPhotoUrls = mutableStateListOf<String>()
    var photoError by mutableStateOf<String?>(null)
        private set

    fun addPhoto(url: String) {
        if (selectedPhotoUrls.size >= 5) {
            photoError = "Máximo 5 fotos permitidas"
            return
        }
        if (!selectedPhotoUrls.contains(url)) {
            selectedPhotoUrls.add(url)
            photoError = null
        }
    }

    fun removePhoto(url: String) {
        selectedPhotoUrls.remove(url)
    }

    val isGalleryValid: Boolean get() = selectedPhotoUrls.isNotEmpty()

    fun validateGallery(): Boolean {
        if (selectedPhotoUrls.isEmpty()) photoError = "Agrega al menos una foto"
        return isGalleryValid
    }

    // ── Paso 2: Ubicación ─────────────────────────────────────────────────────
    var latitude by mutableStateOf<Double?>(null)
        private set
    var longitude by mutableStateOf<Double?>(null)
        private set
    var latitudeInput by mutableStateOf("")
        private set
    var longitudeInput by mutableStateOf("")
        private set
    var address by mutableStateOf("")
        private set
    var locationError by mutableStateOf<String?>(null)
        private set

    fun onLocationSelected(lat: Double, lng: Double, resolvedAddress: String = "") {
        latitude = lat
        longitude = lng
        latitudeInput = String.format(Locale.US, "%.6f", lat)
        longitudeInput = String.format(Locale.US, "%.6f", lng)
        address = resolvedAddress
        locationError = null
    }

    // Funciones setters para edición manual de coordenadas en la UI
    fun onLatitudeChange(value: String) {
        val normalizedValue = normalizeCoordinateInput(value)
        latitudeInput = normalizedValue
        latitude = normalizedValue.toDoubleOrNull()
    }

    fun onLongitudeChange(value: String) {
        val normalizedValue = normalizeCoordinateInput(value)
        longitudeInput = normalizedValue
        longitude = normalizedValue.toDoubleOrNull()
    }

    fun onAddressChange(value: String) { address = value }

    private fun normalizeCoordinateInput(value: String): String {
        return value.replace(',', '.')
    }

    val isStep2Valid: Boolean get() = latitude != null && longitude != null

    fun validateStep2(): Boolean {
        if (latitude == null || longitude == null) locationError = "Selecciona una ubicación"
        return isStep2Valid
    }

    // ── Asistente IA (auto-tagging + descripcion) ────────────────────────────
    sealed interface AiSuggestionState {
        data object Idle : AiSuggestionState
        data object Loading : AiSuggestionState
        data class Ready(val enrichment: AiEnrichment) : AiSuggestionState
        data class Error(val message: String) : AiSuggestionState
    }

    var aiSuggestion by mutableStateOf<AiSuggestionState>(AiSuggestionState.Idle)
        private set

    val acceptedAiTags = mutableStateListOf<String>()

    private var lastEmbedding: List<Double> = emptyList()

    fun runAiAssist() {
        val cat = selectedCategory ?: run {
            aiSuggestion = AiSuggestionState.Error("Selecciona una categoria primero")
            return
        }
        if (title.value.isBlank()) {
            aiSuggestion = AiSuggestionState.Error("Escribe un titulo primero")
            return
        }
        aiSuggestion = AiSuggestionState.Loading
        viewModelScope.launch {
            val labels = selectedPhotoUrls.firstOrNull()
                ?.let { runCatching { imageLabeler.label(Uri.parse(it)) }.getOrDefault(emptyList()) }
                ?: emptyList()

            aiRepository.enrichPoint(
                title = title.value,
                description = description.value,
                category = cat,
                imageLabels = labels
            ).fold(
                onSuccess = { enrichment ->
                    lastEmbedding = enrichment.embedding
                    aiSuggestion = AiSuggestionState.Ready(enrichment)
                },
                onFailure = { e ->
                    aiSuggestion = AiSuggestionState.Error(e.message ?: "Error de IA")
                }
            )
        }
    }

    fun toggleAiTag(tag: String) {
        if (acceptedAiTags.contains(tag)) acceptedAiTags.remove(tag)
        else acceptedAiTags.add(tag)
    }

    fun applyAiDescription() {
        val current = aiSuggestion
        if (current is AiSuggestionState.Ready) {
            current.enrichment.improvedDescription?.let { description.onChange(it) }
        }
    }

    fun dismissAiSuggestion() {
        aiSuggestion = AiSuggestionState.Idle
        acceptedAiTags.clear()
    }

    // ── Envío final ───────────────────────────────────────────────────────────
    var createResult by mutableStateOf<RequestResult<TouristPoint>?>(null)
        private set

    fun submitPoint(): Boolean = persist(asDraft = false)

    fun submitAsDraft(): Boolean = persist(asDraft = true)

    private fun persist(asDraft: Boolean): Boolean {
        if (!isStep1Valid || !isGalleryValid || !isStep2Valid) {
            createResult = RequestResult.Error("Completa los datos requeridos antes de publicar")
            return false
        }

        createResult = RequestResult.Loading

        val basePoint = editingPoint
        val finalTags = acceptedAiTags.toList()
        val finalEmbedding = lastEmbedding
        val pointToPersist = if (basePoint != null) {
            basePoint.copy(
                title = title.value,
                category = selectedCategory!!,
                description = description.value.ifBlank { "Sin descripción" },
                latitude = latitude!!,
                longitude = longitude!!,
                address = address,
                schedule = schedule.value.ifBlank { "No especificado" },
                priceRange = selectedPriceRange ?: PriceRange.FREE,
                photoUrls = selectedPhotoUrls.toList(),
                aiTags = if (finalTags.isNotEmpty()) finalTags else basePoint.aiTags,
                embedding = if (finalEmbedding.isNotEmpty()) finalEmbedding else basePoint.embedding,
                isDraft = asDraft
            )
        } else {
            val authorId = userRepository.currentUser.value?.id
            if (authorId.isNullOrBlank()) {
                createResult = RequestResult.Error("Debes iniciar sesion para publicar")
                return false
            }
            TouristPoint(
                id = "", // ID vacío para que el repo genere uno nuevo en Firestore
                authorId = authorId,
                title = title.value,
                category = selectedCategory!!,
                description = description.value.ifBlank { "Sin descripción" },
                latitude = latitude!!,
                longitude = longitude!!,
                address = address,
                schedule = schedule.value.ifBlank { "No especificado" },
                priceRange = selectedPriceRange ?: PriceRange.FREE,
                photoUrls = selectedPhotoUrls.toList(),
                isVerified = false,
                isDraft = asDraft,
                aiTags = finalTags,
                embedding = finalEmbedding
            )
        }

        viewModelScope.launch {
            val persistResult = if (basePoint != null) {
                repository.update(pointToPersist)
            } else {
                repository.save(pointToPersist)
            }

            persistResult.fold(
                onSuccess = {
                    Log.d("CreatePoint", "Punto persistido (draft=$asDraft): ${pointToPersist.title}")
                    createResult = RequestResult.Success(pointToPersist)
                },
                onFailure = { error ->
                    createResult = RequestResult.Error(error.message ?: "No se pudo guardar la publicacion")
                }
            )
        }
        return true
    }

    fun startEditing(point: TouristPoint) {
        editingPoint = point
        title.onChange(point.title)
        description.onChange(point.description)
        schedule.onChange(point.schedule)
        selectedCategory = point.category
        categoryError = null
        selectedPriceRange = point.priceRange
        selectedPhotoUrls.clear()
        selectedPhotoUrls.addAll(point.photoUrls)
        photoError = null
        latitude = point.latitude
        longitude = point.longitude
        latitudeInput = point.latitude.toString()
        longitudeInput = point.longitude.toString()
        address = point.address
        locationError = null
        createResult = null
        aiSuggestion = AiSuggestionState.Idle
        acceptedAiTags.clear()
        acceptedAiTags.addAll(point.aiTags)
        lastEmbedding = point.embedding
    }

    fun reset() {
        editingPoint = null
        title.reset()
        description.reset()
        schedule.reset()
        selectedCategory = null
        categoryError = null
        selectedPriceRange = null
        priceRangeError = null
        selectedPhotoUrls.clear()
        photoError = null
        latitude = null
        longitude = null
        latitudeInput = ""
        longitudeInput = ""
        address = ""
        locationError = null
        createResult = null
        aiSuggestion = AiSuggestionState.Idle
        acceptedAiTags.clear()
        lastEmbedding = emptyList()
    }

    // Subir imagen desde un Uri (usado por la UI Compose al seleccionar/capturar una foto)
    fun uploadImageFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            isUploadingImage = true
            imageUploadError = null
            val localPreviewUrl = uri.toString()

            // Añadir preview local inmediatamente si no está presente
            if (!selectedPhotoUrls.contains(localPreviewUrl)) {
                addPhoto(localPreviewUrl)
            }

            // Intentar subir; si falla, mantener la preview local para que no desaparezca
            val remoteUrl = imageRepository.uploadImageFromUri(context, uri)
            if (remoteUrl != null) {
                // Reemplazar la preview local por la URL remota en la misma posición
                val idx = selectedPhotoUrls.indexOf(localPreviewUrl)
                if (idx >= 0) {
                    selectedPhotoUrls[idx] = remoteUrl
                } else {
                    // si por alguna razón no existe, añadir al final
                    addPhoto(remoteUrl)
                }
            } else {
                // Mantener el preview local y mostrar un error para poder reintentar
                imageUploadError = "No se pudo subir la imagen"
                Log.w("CreatePointViewModel", "Falló la subida de la imagen para: $localPreviewUrl")
            }

            isUploadingImage = false
        }
    }
}