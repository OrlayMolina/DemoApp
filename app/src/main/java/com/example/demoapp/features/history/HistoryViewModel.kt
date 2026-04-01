package com.example.demoapp.features.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.demoapp.domain.model.ReviewAction
import com.example.demoapp.domain.model.ReviewHistory
import com.example.demoapp.domain.model.TouristPointCategory

enum class HistoryFilter { ALL, APPROVED, REJECTED }

class HistoryViewModel : ViewModel() {

    // ── Datos de muestra — reemplazar por llamada a Firestore ─────────────────
    private val _allHistory = listOf(
        ReviewHistory(
            id           = "1",
            pointTitle   = "Café artesanal en Roma Norte",
            category     = TouristPointCategory.GASTRONOMY,
            reviewedBy   = "Carlos Admin",
            reviewedAt   = 1708725000000L,
            action       = ReviewAction.APPROVED
        ),
        ReviewHistory(
            id           = "2",
            pointTitle   = "Grafiti ofensivo en pared",
            category     = TouristPointCategory.ENTERTAINMENT,
            reviewedBy   = "Carlos Admin",
            reviewedAt   = 1708638600000L,
            action       = ReviewAction.REJECTED,
            rejectionReason = "Contenido inapropiado"
        ),
        ReviewHistory(
            id           = "3",
            pointTitle   = "Mirador en Chapultepec",
            category     = TouristPointCategory.NATURE,
            reviewedBy   = "Carlos Admin",
            reviewedAt   = 1708552800000L,
            action       = ReviewAction.APPROVED
        ),
        ReviewHistory(
            id           = "4",
            pointTitle   = "Publicación spam repetida",
            category     = TouristPointCategory.ENTERTAINMENT,
            reviewedBy   = "Carlos Admin",
            reviewedAt   = 1708466400000L,
            action       = ReviewAction.REJECTED,
            rejectionReason = "Spam"
        ),
        ReviewHistory(
            id           = "5",
            pointTitle   = "Parque ecológico nuevo",
            category     = TouristPointCategory.NATURE,
            reviewedBy   = "Carlos Admin",
            reviewedAt   = 1708380000000L,
            action       = ReviewAction.APPROVED
        )
    )

    // ── Estado ────────────────────────────────────────────────────────────────
    var searchQuery  by mutableStateOf("")
        private set

    var activeFilter by mutableStateOf(HistoryFilter.ALL)
        private set

    // ── Datos filtrados ───────────────────────────────────────────────────────
    val filteredHistory get() = _allHistory
        .filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.pointTitle.contains(searchQuery, ignoreCase = true) ||
                    item.reviewedBy.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (activeFilter) {
                HistoryFilter.ALL      -> true
                HistoryFilter.APPROVED -> item.action == ReviewAction.APPROVED
                HistoryFilter.REJECTED -> item.action == ReviewAction.REJECTED
            }

            matchesSearch && matchesFilter
        }
        .sortedByDescending { it.reviewedAt }

    // ── Resumen ───────────────────────────────────────────────────────────────
    val totalCount    get() = _allHistory.size
    val approvedCount get() = _allHistory.count { it.action == ReviewAction.APPROVED }
    val rejectedCount get() = _allHistory.count { it.action == ReviewAction.REJECTED }

    // ── Acciones ──────────────────────────────────────────────────────────────
    fun onSearchChange(query: String) {
        searchQuery = query
    }

    fun onFilterChange(filter: HistoryFilter) {
        activeFilter = filter
    }

    // ── Futuro: cargar desde Firestore ────────────────────────────────────────
    // fun loadHistory() {
    //     viewModelScope.launch {
    //         _allHistory = repository.getReviewHistory()
    //     }
    // }
}