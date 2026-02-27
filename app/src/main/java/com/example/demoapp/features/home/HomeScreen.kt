package com.example.demoapp.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.demoapp.R
import com.example.demoapp.ui.theme.DemoAppTheme

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
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
                onClick = {
                    // Acción al hacer clic en el botón de inicio de sesión
                }
            ) {
                Text(text = "Iniciar sesión")
            }
            Button(
                onClick = {
                    // Acción al hacer clic en el botón de registro
                }
            ) {
                Text(text = "Crear una cuenta")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Home Screen")
@Composable
fun HomeScreenPreview() {
    DemoAppTheme {
        HomeScreen()
    }
}