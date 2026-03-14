package com.example.demoapp.features.login

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.R
import com.example.demoapp.ui.theme.DemoAppTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToUsers: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo_red_explora),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = viewModel.email.value,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                isError = viewModel.email.error != null,
                supportingText = {
                    viewModel.email.error?.let { error ->
                        Text(text = error)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = viewModel.password.value,
                onValueChange = { viewModel.onPasswordChange(it) },
                visualTransformation = PasswordVisualTransformation(),
                label = { Text("Password") },
                isError = viewModel.password.error != null,
                supportingText = {
                    viewModel.password.error?.let { error ->
                        Text(text = error)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Button(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Iniciando sesión...",
                            duration = SnackbarDuration.Short
                        )
                    }

                    Log.d(
                        "Login",
                        "Email: ${viewModel.email.value}, Password: ${viewModel.password.value}"
                    )

                    onNavigateToUsers()
                },
                enabled = viewModel.isFormValid
            ) {
                Text("Iniciar Sesión")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Login Screen")
@Composable
fun LoginScreenPreview() {
    DemoAppTheme {
        LoginScreen(
            onNavigateToUsers = {}
        )
    }
}