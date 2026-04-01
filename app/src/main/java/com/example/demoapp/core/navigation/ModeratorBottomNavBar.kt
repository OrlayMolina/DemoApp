package com.example.demoapp.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GreenEmerald = Color(0xFF00897B)
private val TextGray     = Color(0xFF6B6B6B)
private val CardWhite    = Color(0xFFFFFFFF)

enum class ModeratorTab { DASHBOARD, REVIEW, HISTORY, REPORTS, USERS }

@Composable
fun ModeratorBottomNavBar(
    selectedTab  : ModeratorTab,
    onTabSelected: (ModeratorTab) -> Unit
) {
    NavigationBar(
        containerColor = CardWhite,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == ModeratorTab.DASHBOARD,
            onClick  = { onTabSelected(ModeratorTab.DASHBOARD) },
            icon     = {
                Icon(
                    if (selectedTab == ModeratorTab.DASHBOARD) Icons.Filled.Dashboard
                    else Icons.Outlined.Dashboard,
                    "Panel"
                )
            },
            label  = { Text("Panel", fontSize = 10.sp) },
            colors = modNavColors()
        )
        NavigationBarItem(
            selected = selectedTab == ModeratorTab.REVIEW,
            onClick  = { onTabSelected(ModeratorTab.REVIEW) },
            icon     = {
                Icon(
                    if (selectedTab == ModeratorTab.REVIEW) Icons.Filled.RateReview
                    else Icons.Outlined.RateReview,
                    "Revisión"
                )
            },
            label  = { Text("Revisión", fontSize = 10.sp) },
            colors = modNavColors()
        )
        NavigationBarItem(
            selected = selectedTab == ModeratorTab.HISTORY,
            onClick  = { onTabSelected(ModeratorTab.HISTORY) },
            icon     = {
                Icon(
                    if (selectedTab == ModeratorTab.HISTORY) Icons.Filled.History
                    else Icons.Outlined.History,
                    "Historial"
                )
            },
            label  = { Text("Historial", fontSize = 10.sp) },
            colors = modNavColors()
        )
        NavigationBarItem(
            selected = selectedTab == ModeratorTab.REPORTS,
            onClick  = { onTabSelected(ModeratorTab.REPORTS) },
            icon     = {
                Icon(
                    if (selectedTab == ModeratorTab.REPORTS) Icons.Filled.Flag
                    else Icons.Outlined.Flag,
                    "Reportes"
                )
            },
            label  = { Text("Reportes", fontSize = 10.sp) },
            colors = modNavColors()
        )
        NavigationBarItem(
            selected = selectedTab == ModeratorTab.USERS,
            onClick  = { onTabSelected(ModeratorTab.USERS) },
            icon     = {
                Icon(
                    if (selectedTab == ModeratorTab.USERS) Icons.Filled.Group
                    else Icons.Outlined.Group,
                    "Usuarios"
                )
            },
            label  = { Text("Usuarios", fontSize = 10.sp) },
            colors = modNavColors()
        )
    }
}

@Composable
private fun modNavColors() = NavigationBarItemDefaults.colors(
    indicatorColor      = GreenEmerald.copy(alpha = 0.12f),
    selectedIconColor   = GreenEmerald,
    selectedTextColor   = GreenEmerald,
    unselectedIconColor = TextGray,
    unselectedTextColor = TextGray
)