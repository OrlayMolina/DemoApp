package com.example.demoapp.features.recovery

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.R
import com.example.demoapp.core.utils.RequestResult
import kotlinx.coroutines.launch

@Composable
fun PasswordRecoveryScreen(
    viewModel: PasswordRecoveryViewModel = viewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, CenterVertically)
        ) {

            // 🔹 Logo
            Image(
                painter = painterResource(id = R.drawable.logo_red_explora),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )

            Text(
                "Recuperar contraseña",
                style = MaterialTheme.typography.headlineMedium
            )

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
                supportingText = {
                    viewModel.email.error?.let { Text(it) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            when (val result = viewModel.recoveryResult) {
                is RequestResult.Loading -> CircularProgressIndicator()
                is RequestResult.Error ->
                    Text(result.message, color = MaterialTheme.colorScheme.error)

                is RequestResult.Success ->
                    Text(result.data, color = MaterialTheme.colorScheme.primary)

                null -> {}
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onClick = {
                    viewModel.sendRecoveryEmail()

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Enviando código de recuperación...",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                enabled = viewModel.email.isValid &&
                        viewModel.recoveryResult !is RequestResult.Loading
            ) {
                Text("Enviar código")
            }
        }
    }
}