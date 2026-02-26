package com.example.demoapp.features.recovery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.core.utils.RequestResult

@Composable
fun PasswordRecoveryScreen(viewModel: PasswordRecoveryViewModel = viewModel()) {
    Column(
        modifier = Modifier.fillMaxSize().padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, CenterVertically)
    ) {
        Text("Recuperar contraseña", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Ingresa tu email y te enviaremos un código de verificación.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewModel.email.value,
            onValueChange = { viewModel.email.onChange(it) },
            label = { Text("Email") },
            isError = viewModel.email.error != null,
            supportingText = viewModel.email.error?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        when (val result = viewModel.recoveryResult) {
            is RequestResult.Loading -> CircularProgressIndicator()
            is RequestResult.Error   -> Text(result.message, color = MaterialTheme.colorScheme.error)
            is RequestResult.Success -> Text(result.data, color = MaterialTheme.colorScheme.primary)
            null -> {}
        }

        Button(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            onClick = { viewModel.sendRecoveryEmail() },
            enabled = viewModel.email.isValid && viewModel.recoveryResult !is RequestResult.Loading
        ) {
            Text("Enviar código")
        }
    }
}