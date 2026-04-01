package com.example.demoapp.features.review

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.demoapp.domain.model.TouristPoint

enum class DateFilter(val label: String) {
    ALL("Todos"),
    DAY_1("Hace 1 día"),
    DAY_2("Hace 2 días"),
    DAY_5_PLUS("Más de 5 días")
}

class ReviewQueueViewModel : ViewModel() {

    private val allPoints = TouristPoint.SAMPLE_LIST

    var selectedDateFilter by mutableStateOf(DateFilter.ALL)
        private set

    val pendingPoints get() = allPoints
        .filter { !it.isVerified && !it.isRejected }
        .applyDateFilter(selectedDateFilter)

    val reportedPoints get() = allPoints
        .filter { it.isVerified && it.isReported }
        .applyDateFilter(selectedDateFilter)

    fun setDateFilter(filter: DateFilter) {
        selectedDateFilter = filter
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