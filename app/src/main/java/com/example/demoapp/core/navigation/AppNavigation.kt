package com.example.demoapp.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.demoapp.features.home.HomeScreen
import com.example.demoapp.features.login.LoginScreen
import com.example.demoapp.features.register.RegisterScreen
import com.example.demoapp.features.users.list.UserListScreen
import androidx.navigation.toRoute
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.features.explore.ExploreScreen
import com.example.demoapp.features.profile.ProfileScreen
import com.example.demoapp.features.recovery.PasswordRecoveryScreen
import com.example.demoapp.features.users.detail.UserDetailScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Surface(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController    = navController,
            startDestination = MainRoutes.Home
        ) {

            composable<MainRoutes.Home> {
                HomeScreen(
                    onNavigateToLogin    = { navController.navigate(MainRoutes.Login) },
                    onNavigateToRegister = { navController.navigate(MainRoutes.Register) }
                )
            }

            composable<MainRoutes.Login> {
                LoginScreen(
                    onNavigateToUsers = {
                        navController.navigate(MainRoutes.Main) {
                            popUpTo(MainRoutes.Home) { inclusive = false }
                        }
                    },
                    onNavigateToRegister         = { navController.navigate(MainRoutes.Register) },
                    onNavigateToPasswordRecovery = { navController.navigate(MainRoutes.PasswordRecovery) }
                )
            }

            composable<MainRoutes.Register> {
                RegisterScreen(
                    onNavigateBack    = { navController.popBackStack() },
                    onNavigateToLogin = {
                        navController.navigate(MainRoutes.Login) {
                            popUpTo(MainRoutes.Register) { inclusive = true }
                        }
                    }
                )
            }

            composable<MainRoutes.PasswordRecovery> {
                PasswordRecoveryScreen(
                    onNavigateBack    = { navController.popBackStack() },
                    onNavigateToLogin = {
                        navController.navigate(MainRoutes.Login) {
                            popUpTo(MainRoutes.PasswordRecovery) { inclusive = true }
                        }
                    }
                )
            }

            composable<MainRoutes.Main> {
                MainScreen()
            }

            composable<MainRoutes.UserDetail> { backStackEntry ->
                val args = backStackEntry.toRoute<MainRoutes.UserDetail>()
                UserDetailScreen(
                    userId         = args.userId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}