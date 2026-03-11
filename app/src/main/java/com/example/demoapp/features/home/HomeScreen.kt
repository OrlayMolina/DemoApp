package com.example.demoapp.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.demoapp.R
import com.example.demoapp.ui.theme.DemoAppTheme

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit,     // Función para navegar a la pantalla de Login
    onNavigateToRegister: () -> Unit   // Función para navegar a la pantalla de Registro
) {

    Scaffold { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(R.drawable.logo_red_explora),
                contentDescription = "Welcome Image"
            )

            Text(text = "Home Screen")

            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Button(
                    onClick = onNavigateToLogin
                ) {
                    Text(text = "Iniciar sesión")
                }

                Button(
                    onClick = onNavigateToRegister
                ) {
                    Text(text = "Crear una cuenta")
                }
            }
        }
    }
}