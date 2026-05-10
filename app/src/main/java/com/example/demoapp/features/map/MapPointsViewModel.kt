package com.example.demoapp.features.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.core.utils.cosineSimilarity
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.TouristPointCategory
import com.example.demoapp.domain.repository.AiRepository
import com.example.demoapp.domain.repository.TouristPointRepository
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class MapPointsViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val touristPointRepository: TouristPointRepository
) : ViewModel() {

    var allPoints by mutableStateOf<List<TouristPoint>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            touristPointRepository.touristPoints.collect { points ->
                allPoints = points
            }
        }
    }

    var selectedCategory by mutableStateOf<TouristPointCategory?>(null)
        private set

    var showingMap by mutableStateOf(false)
        private set

    // ── Busqueda IA ──────────────────────────────────────────────────────────
    var aiSearchActive by mutableStateOf(false)
        private set

    private val _aiQuery = MutableStateFlow("")
    val aiQuery: StateFlow<String> = _aiQuery.asStateFlow()

    sealed interface AiSearchState {
        data object Idle : AiSearchState
        data object Loading : AiSearchState
        data class Success(val points: List<TouristPoint>) : AiSearchState
        data class Error(val message: String) : AiSearchState
    }

    val aiResults: StateFlow<AiSearchState> = _aiQuery
        .debounce(400)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            flow {
                val q = query.trim()
                if (q.length < 3) {
                    emit(AiSearchState.Idle)
                    return@flow
                }
                emit(AiSearchState.Loading)
                aiRepository.embedQuery(q).fold(
                    onSuccess = { qVec ->
                        val ranked = allPoints
                            .filter { it.embedding.isNotEmpty() && it.isVerified && !it.isRejected && !it.isSaved }
                            .map { it to cosineSimilarity(qVec, it.embedding) }
                            .filter { it.second > 0.55 }
                            .sortedByDescending { it.second }
                            .take(20)
                            .map { it.first }
                        emit(AiSearchState.Success(ranked))
                    },
                    onFailure = { e ->
                        emit(AiSearchState.Error(e.message ?: "Error de IA"))
                    }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, AiSearchState.Idle)

    val filteredPoints: List<TouristPoint>
        get() = if (aiSearchActive) {
            (aiResults.value as? AiSearchState.Success)?.points ?: emptyList()
        } else {
            selectedCategory
                ?.let { cat -> allPoints.filter { it.category == cat && !it.isSaved } }
                ?: allPoints.filter { !it.isSaved }
        }

    val countByCategory: Map<TouristPointCategory, Int>
        get() = TouristPointCategory.entries.associateWith { cat ->
            allPoints.count { it.category == cat }
        }.filter { it.value > 0 }

    val topCategories
        get() = countByCategory.entries.sortedByDescending { it.value }.take(4)

    fun selectCategory(category: TouristPointCategory?) {
        selectedCategory = category
        aiSearchActive = false
        showingMap = true
    }

    fun goBackToSelection() {
        showingMap = false
        selectedCategory = null
        aiSearchActive = false
        _aiQuery.value = ""
    }

    fun setPoints(points: List<TouristPoint>) {
        allPoints = points
    }

    fun toggleAiSearch(active: Boolean) {
        aiSearchActive = active
        if (!active) _aiQuery.value = ""
    }

    fun onAiQueryChange(text: String) {
        _aiQuery.value = text
    }

    fun openAiResultsOnMap() {
        if (aiResults.value is AiSearchState.Success) {
            aiSearchActive = true
            showingMap = true
        }
    }
}
