package com.example.demoapp.features.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.domain.model.ReviewAction
import com.example.demoapp.domain.model.ReviewHistory
import com.example.demoapp.domain.repository.ReviewHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HistoryFilter { ALL, APPROVED, REJECTED }

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val reviewHistoryRepository: ReviewHistoryRepository
) : ViewModel() {

    private var allHistory by mutableStateOf<List<ReviewHistory>>(emptyList())

    init {
        viewModelScope.launch {
            reviewHistoryRepository.history.collectLatest { history ->
                allHistory = history
            }
        }
    }

    var searchQuery by mutableStateOf("")
        private set

    var activeFilter by mutableStateOf(HistoryFilter.ALL)
        private set

    val filteredHistory get() = allHistory
        .filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                item.pointTitle.contains(searchQuery, ignoreCase = true) ||
                item.reviewedBy.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (activeFilter) {
                HistoryFilter.ALL -> true
                HistoryFilter.APPROVED -> item.action == ReviewAction.APPROVED
                HistoryFilter.REJECTED -> item.action == ReviewAction.REJECTED
            }

            matchesSearch && matchesFilter
        }
        .sortedByDescending { it.reviewedAt }

    val totalCount get() = allHistory.size
    val approvedCount get() = allHistory.count { it.action == ReviewAction.APPROVED }
    val rejectedCount get() = allHistory.count { it.action == ReviewAction.REJECTED }

    fun onSearchChange(query: String) {
        searchQuery = query
    }

    fun onFilterChange(filter: HistoryFilter) {
        activeFilter = filter
    }
}