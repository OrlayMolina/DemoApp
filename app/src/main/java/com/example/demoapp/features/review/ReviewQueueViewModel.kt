package com.example.demoapp.features.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.repository.TouristPointRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class DateFilter(val label: String) {
    ALL("Todos"),
    DAY_1("Hace 1 día"),
    DAY_2("Hace 2 días"),
    DAY_5_PLUS("Más de 5 días")
}

@HiltViewModel
class ReviewQueueViewModel @Inject constructor(
    private val touristPointRepository: TouristPointRepository
) : ViewModel() {

    private val _selectedDateFilter = MutableStateFlow(DateFilter.ALL)
    val selectedDateFilter: StateFlow<DateFilter> = _selectedDateFilter.asStateFlow()

    val pendingPoints: StateFlow<List<TouristPoint>> = combine(
        touristPointRepository.touristPoints,
        selectedDateFilter
    ) { allPoints, filter ->
            allPoints
                .filter { !it.isVerified && !it.isRejected }
                .applyDateFilter(filter)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val reportedPoints: StateFlow<List<TouristPoint>> = combine(
        touristPointRepository.touristPoints,
        selectedDateFilter
    ) { allPoints, filter ->
            allPoints
                .filter { it.isVerified && it.isReported }
                .applyDateFilter(filter)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun setDateFilter(filter: DateFilter) {
        _selectedDateFilter.value = filter
    }

    private fun List<TouristPoint>.applyDateFilter(
        filter: DateFilter
    ): List<TouristPoint> {
        val now = System.currentTimeMillis()
        val day = 86_400_000L
        return when (filter) {
            DateFilter.ALL      -> this
            DateFilter.DAY_1    -> filter { now - it.createdAt <= day }
            DateFilter.DAY_2    -> filter { now - it.createdAt <= day * 2 }
            DateFilter.DAY_5_PLUS -> filter { now - it.createdAt > day * 5 }
        }
    }
}