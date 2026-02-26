package com.example.demoapp

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.features.login.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp, alignment = CenterVertically)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewModel.email.value, // Estado del campo de email desde el ViewModel
            onValueChange = { viewModel.email.onChange(it) }, // Actualiza el estado en el ViewModel
            label = {
                Text(text = "Email")
            },
            isError = viewModel.email.error != null, // Se muestra el borde rojo si hay error
            supportingText = viewModel.email.error?.let { error ->
                { Text(text = error) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewModel.password.value, // Estado del campo de password desde el ViewModel
            onValueChange = { viewModel.password.onChange(it) }, // Actualiza el estado en el ViewModel
            visualTransformation = PasswordVisualTransformation(),
            label = {
                Text(text = "Password")
            },
            isError = viewModel.password.error != null, // Se muestra el borde rojo si hay error
            supportingText = viewModel.password.error?.let { error ->
                { Text(text = error) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Button(
            onClick = {
                // Se imprime el email y password en el logcat por ahora
                Log.d("Login", "Email: ${viewModel.email.value}, Password: ${viewModel.password.value}")
            },
            enabled = viewModel.isFormValid,
            content = {
                Text(text = "Iniciar Sesión")
            }
        )

    }
}