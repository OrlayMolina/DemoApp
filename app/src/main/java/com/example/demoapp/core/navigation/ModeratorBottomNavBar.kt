package com.example.demoapp.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demoapp.R

private val GreenEmerald = Color(0xFF00897B)

enum class ModeratorTab { DASHBOARD, REVIEW, HISTORY, REPORTS, USERS }

@Composable
fun ModeratorBottomNavBar(
    selectedTab  : ModeratorTab,
    onTabSelected: (ModeratorTab) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    NavigationBar(
        containerColor = colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == ModeratorTab.DASHBOARD,
            onClick  = { onTabSelected(ModeratorTab.DASHBOARD) },
            icon     = {
                Icon(
                    if (selectedTab == ModeratorTab.DASHBOARD) Icons.Filled.Dashboard
                    else Icons.Outlined.Dashboard,
                    stringResource(R.string.moderator_nav_dashboard)
                )
            },
            label  = { Text(stringResource(R.string.moderator_nav_dashboard), fontSize = 10.sp) },
            colors = modNavColors()
        )
        NavigationBarItem(
            selected = selectedTab == ModeratorTab.REVIEW,
            onClick  = { onTabSelected(ModeratorTab.REVIEW) },
            icon     = {
                Icon(
                    if (selectedTab == ModeratorTab.REVIEW) Icons.Filled.RateReview
                    else Icons.Outlined.RateReview,
                    stringResource(R.string.moderator_nav_review)
                )
            },
            label  = { Text(stringResource(R.string.moderator_nav_review), fontSize = 10.sp) },
            colors = modNavColors()
        )
        NavigationBarItem(
            selected = selectedTab == ModeratorTab.HISTORY,
            onClick  = { onTabSelected(ModeratorTab.HISTORY) },
            icon     = {
                Icon(
                    if (selectedTab == ModeratorTab.HISTORY) Icons.Filled.History
                    else Icons.Outlined.History,
                    stringResource(R.string.moderator_nav_history)
                )
            },
            label  = { Text(stringResource(R.string.moderator_nav_history), fontSize = 10.sp) },
            colors = modNavColors()
        )
        NavigationBarItem(
            selected = selectedTab == ModeratorTab.REPORTS,
            onClick  = { onTabSelected(ModeratorTab.REPORTS) },
            icon     = {
                Icon(
                    if (selectedTab == ModeratorTab.REPORTS) Icons.Filled.Flag
                    else Icons.Outlined.Flag,
                    stringResource(R.string.moderator_nav_reports)
                )
            },
            label  = { Text(stringResource(R.string.moderator_nav_reports), fontSize = 10.sp) },
            colors = modNavColors()
        )
        NavigationBarItem(
            selected = selectedTab == ModeratorTab.USERS,
            onClick  = { onTabSelected(ModeratorTab.USERS) },
            icon     = {
                Icon(
                    if (selectedTab == ModeratorTab.USERS) Icons.Filled.Group
                    else Icons.Outlined.Group,
                    stringResource(R.string.moderator_nav_users)
                )
            },
            label  = { Text(stringResource(R.string.moderator_nav_users), fontSize = 10.sp) },
            colors = modNavColors()
        )
    }
}

@Composable
private fun modNavColors() = NavigationBarItemDefaults.colors(
    indicatorColor      = GreenEmerald.copy(alpha = 0.12f),
    selectedIconColor   = GreenEmerald,
    selectedTextColor   = GreenEmerald,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
)