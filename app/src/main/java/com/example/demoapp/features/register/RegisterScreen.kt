package com.example.demoapp.features.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.core.utils.RequestResult

@Composable
fun RegisterScreen(viewModel: RegisterViewModel = viewModel()) {

    // Reaccionamos al resultado del registro
    LaunchedEffect(viewModel.registerResult) {
        if (viewModel.registerResult is RequestResult.Success) {
            // Aquí navegarías a otra pantalla
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
            .verticalScroll(rememberScrollState()), // scroll para pantallas pequeñas
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)

        // Nombre
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewModel.name.value,
            onValueChange = { viewModel.name.onChange(it) },
            label = { Text("Nombre") },
            isError = viewModel.name.error != null,
            supportingText = viewModel.name.error?.let { { Text(it) } }
        )

        // Ciudad
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewModel.city.value,
            onValueChange = { viewModel.city.onChange(it) },
            label = { Text("Ciudad") },
            isError = viewModel.city.error != null,
            supportingText = viewModel.city.error?.let { { Text(it) } }
        )

        // Dirección
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewModel.address.value,
            onValueChange = { viewModel.address.onChange(it) },
            label = { Text("Dirección") },
            isError = viewModel.address.error != null,
            supportingText = viewModel.address.error?.let { { Text(it) } }
        )

        // Email
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewModel.email.value,
            onValueChange = { viewModel.email.onChange(it) },
            label = { Text("Email") },
            isError = viewModel.email.error != null,
            supportingText = viewModel.email.error?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        // Contraseña
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewModel.password.value,
            onValueChange = { viewModel.password.onChange(it) },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = viewModel.password.error != null,
            supportingText = viewModel.password.error?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Confirmar contraseña
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewModel.confirmPassword.value,
            onValueChange = { viewModel.confirmPassword.onChange(it) },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = viewModel.confirmPassword.error != null,
            supportingText = viewModel.confirmPassword.error?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Feedback del resultado
        when (val result = viewModel.registerResult) {
            is RequestResult.Loading -> CircularProgressIndicator()
            is RequestResult.Error   -> Text(result.message, color = MaterialTheme.colorScheme.error)
            is RequestResult.Success -> Text(result.data, color = MaterialTheme.colorScheme.primary)
            null -> {}
        }

        Button(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            onClick = { viewModel.register() },
            enabled = viewModel.isFormValid && viewModel.registerResult !is RequestResult.Loading
        ) {
            Text("Registrarse")
        }
    }
}