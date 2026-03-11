package com.example.demoapp.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.demoapp.features.home.HomeScreen
import com.example.demoapp.features.login.LoginScreen
import com.example.demoapp.features.register.RegisterScreen
import com.example.demoapp.features.users.detail.UserDetailScreen
import com.example.demoapp.features.users.list.UserListScreen

@Composable
fun AppNavigation() {
    // Estado de la navegación, permite controlar la navegación entre pantallas
    val navController = rememberNavController()

    // Un Surface que ocupa toda la pantalla y se adapta al tema de la aplicación
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController, // Controlador de navegación
            startDestination = MainRoutes.Home // Pantalla de inicio, esta es la primer pantalla que se muestra al iniciar la aplicación
        ) {

            // Definición de las rutas y sus composables asociados (se puede agregar más rutas según sea necesario)

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
                LoginScreen()
            }

            composable<MainRoutes.Register> {
                RegisterScreen()
            }

            composable<MainRoutes.UserDetail> {
                // Se obtienen los argumentos de la ruta
                val args = it.toRoute<MainRoutes.UserDetail>()
                // Se pasa el ID del usuario a la pantalla de detalles del usuario
                UserDetailScreen(
                    userId = args.userId
                )
            }

            composable<MainRoutes.UserList> {
                // Se pasa la función de navegación a la pantalla de la lista de usuarios
                UserListScreen(
                    onNavigateToUserDetail = { userId ->
                        navController.navigate(MainRoutes.UserDetail(userId))
                    }
                )
            }

        }
    }
}