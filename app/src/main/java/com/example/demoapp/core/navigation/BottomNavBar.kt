package com.example.demoapp.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GreenPrimary = Color(0xFF2E7D5E)
private val TextGray     = Color(0xFF6B6B6B)
private val CardWhite    = Color(0xFFFFFFFF)

enum class BottomNavTab { HOME, PUBLISH, NOTIFICATIONS, PROFILE }

@Composable
fun BottomNavBar(
    selectedTab : BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit
) {
    NavigationBar(
        containerColor = CardWhite,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == BottomNavTab.HOME,
            onClick  = { onTabSelected(BottomNavTab.HOME) },
            icon     = {
                Icon(
                    if (selectedTab == BottomNavTab.HOME) Icons.Filled.Home
                    else Icons.Outlined.Home,
                    contentDescription = "Inicio"
                )
            },
            label  = { Text("Inicio", fontSize = 11.sp) },
            colors = navBarColors()
        )
        NavigationBarItem(
            selected = selectedTab == BottomNavTab.PUBLISH,
            onClick  = { onTabSelected(BottomNavTab.PUBLISH) },
            icon     = {
                Icon(
                    if (selectedTab == BottomNavTab.PUBLISH) Icons.Filled.AddCircle
                    else Icons.Outlined.AddCircle,
                    contentDescription = "Publicar"
                )
            },
            label  = { Text("Publicar", fontSize = 11.sp) },
            colors = navBarColors()
        )
        NavigationBarItem(
            selected = selectedTab == BottomNavTab.NOTIFICATIONS,
            onClick  = { onTabSelected(BottomNavTab.NOTIFICATIONS) },
            icon     = {
                Icon(
                    if (selectedTab == BottomNavTab.NOTIFICATIONS) Icons.Filled.Notifications
                    else Icons.Outlined.Notifications,
                    contentDescription = "Notificaciones"
                )
            },
            label  = { Text("Notificaciones", fontSize = 11.sp) },
            colors = navBarColors()
        )
        NavigationBarItem(
            selected = selectedTab == BottomNavTab.PROFILE,
            onClick  = { onTabSelected(BottomNavTab.PROFILE) },
            icon     = {
                Icon(
                    if (selectedTab == BottomNavTab.PROFILE) Icons.Filled.Person
                    else Icons.Outlined.Person,
                    contentDescription = "Perfil"
                )
            },
            label  = { Text("Perfil", fontSize = 11.sp) },
            colors = navBarColors()
        )
    }
}

@Composable
private fun navBarColors() = NavigationBarItemDefaults.colors(
    indicatorColor      = GreenPrimary.copy(alpha = 0.12f),
    selectedIconColor   = GreenPrimary,
    selectedTextColor   = GreenPrimary,
    unselectedIconColor = TextGray,
    unselectedTextColor = TextGray
)