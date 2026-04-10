package com.example.demoapp.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.demoapp.data.model.UserSession
import com.example.demoapp.domain.model.UserRole
import com.example.demoapp.features.dashboard.ModeratorScreen
import com.example.demoapp.features.home.HomeScreen
import com.example.demoapp.features.login.LoginScreen
import com.example.demoapp.features.register.RegisterScreen
import com.example.demoapp.features.recovery.PasswordRecoveryScreen

@Composable
fun AppNavigation(sessionViewModel: SessionViewModel = hiltViewModel()) {
    val sessionState by sessionViewModel.sessionState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        when (sessionState) {
            is SessionState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is SessionState.NotAuthenticated -> AuthNavigation(
                onLoginSuccess = { user ->
                    sessionViewModel.login(user.id, user.role)
                }
            )

            is SessionState.Authenticated -> MainNavigation(
                session = (sessionState as SessionState.Authenticated).session,
                onLogout = sessionViewModel::logout
            )
        }
    }
}

@Composable
private fun AuthNavigation(
    onLoginSuccess: (com.example.demoapp.domain.model.User) -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MainRoutes.Home
    ) {

        composable<MainRoutes.Home> {
            HomeScreen(
                onNavigateToLogin = {
                    navController.navigate(MainRoutes.Login)
                },
                onNavigateToRegister = {
                    navController.navigate(MainRoutes.Register)
                }
            )
        }

        composable<MainRoutes.Login> {
            LoginScreen(
                onNavigateToUsers = {},
                onNavigateToRegister = {
                    navController.navigate(MainRoutes.Register)
                },
                onNavigateToPasswordRecovery = {
                    navController.navigate(MainRoutes.PasswordRecovery)
                },
                onNavigateToModerator = {},
                onLoginSuccess = onLoginSuccess
            )
        }

        composable<MainRoutes.PasswordRecovery> {
            PasswordRecoveryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(MainRoutes.Login) {
                        popUpTo(MainRoutes.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<MainRoutes.Register> {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(MainRoutes.Login) {
                        popUpTo(MainRoutes.Login) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun MainNavigation(
    session: UserSession,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    val startDestination: MainRoutes = when (session.role) {
        UserRole.ADMIN -> MainRoutes.Moderator
        UserRole.USER -> MainRoutes.Main
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<MainRoutes.Main> {
            MainScreen(
                onLogout = onLogout
            )
        }

        composable<MainRoutes.Moderator> {
            ModeratorScreen(
                onLogout = onLogout
            )
        }

    }
}
