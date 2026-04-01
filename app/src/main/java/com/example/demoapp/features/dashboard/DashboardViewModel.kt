package com.example.demoapp.features.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.demoapp.domain.model.TouristPoint

data class RecentActivity(
    val title    : String,
    val subtitle : String,
    val type     : ActivityType,
    val time     : String
)

enum class ActivityType { APPROVED, REJECTED, REPORTED }

class DashboardViewModel : ViewModel() {

    val allPoints = TouristPoint.SAMPLE_LIST

    val pendingCount   get() = allPoints.count { !it.isVerified && !it.isRejected }
    val approvedToday  get() = allPoints.count { it.isVerified }
    val rejectedToday  get() = allPoints.count { it.isRejected }
    val activeUsers    = 1234   // quemado

    val recentActivity = listOf(
        RecentActivity(
            title    = "Publicación aprobada",
            subtitle = "María García · hace 5 min",
            type     = ActivityType.APPROVED,
            time     = "hace 5 min"
        ),
        RecentActivity(
            title    = "Publicación rechazada",
            subtitle = "Juan Pérez · hace 12 min",
            type     = ActivityType.REJECTED,
            time     = "hace 12 min"
        ),
        RecentActivity(
            title    = "Reporte recibido",
            subtitle = "Ana Pérez · hace 30 min",
            type     = ActivityType.REPORTED,
            time     = "hace 30 min"
        ),
        RecentActivity(
            title    = "Publicación aprobada",
            subtitle = "Carlos López · hace 1 hora",
            type     = ActivityType.APPROVED,
            time     = "hace 1 hora"
        )
    )

    val reviewsToday  = 27
    val precision     = 89
    val minPerReview  = 3.2f
}