package com.example.demoapp.features.reset

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.core.utils.RequestResult

@Composable
fun PasswordResetScreen(viewModel: PasswordResetViewModel = viewModel()) {

    // FocusRequester para saltar al siguiente campo automáticamente
    val focusRequesters = remember { List(5) { FocusRequester() } }

    Column(
        modifier = Modifier.fillMaxSize().padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text("Restablecer contraseña", style = MaterialTheme.typography.headlineMedium)
        Text("Ingresa el código de 5 dígitos que recibiste.", style = MaterialTheme.typography.bodyMedium)

        // 5 campos de un dígito en fila
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.codeDigits.forEachIndexed { index, field ->
                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequesters[index]),
                    value = field.value,
                    onValueChange = { input ->
                        // Solo acepta el último carácter ingresado (un dígito)
                        val digit = input.filter { it.isDigit() }.takeLast(1)
                        field.onChange(digit)
                        // Salta al siguiente campo si hay un dígito
                        if (digit.isNotEmpty() && index < 4) {
                            focusRequesters[index + 1].requestFocus()
                        }
                    },
                    isError = field.error != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    singleLine = true
                )
            }
        }

        // Nueva contraseña
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewModel.newPassword.value,
            onValueChange = { viewModel.newPassword.onChange(it) },
            label = { Text("Nueva contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = viewModel.newPassword.error != null,
            supportingText = viewModel.newPassword.error?.let { { Text(it) } },
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

        when (val result = viewModel.resetResult) {
            is RequestResult.Loading -> CircularProgressIndicator()
            is RequestResult.Error   -> Text(result.message, color = MaterialTheme.colorScheme.error)
            is RequestResult.Success -> Text(result.data, color = MaterialTheme.colorScheme.primary)
            null -> {}
        }

        Button(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            onClick = { viewModel.resetPassword() },
            enabled = viewModel.isFormValid && viewModel.resetResult !is RequestResult.Loading
        ) {
            Text("Restablecer contraseña")
        }
    }
}