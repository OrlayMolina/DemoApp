package com.example.demoapp.features.reset

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.demoapp.R
import com.example.demoapp.core.utils.RequestResult
import kotlinx.coroutines.launch

@Composable
fun PasswordResetScreen(
    viewModel: PasswordResetViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resetLoadingMessage = stringResource(R.string.reset_snackbar_loading)

    val focusRequesters = remember { List(5) { FocusRequester() } }

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

            // 🔹 Logo
            Image(
                painter = painterResource(id = R.drawable.logo_red_explora),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(120.dp)
            )

            Text(
                stringResource(R.string.reset_title),
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                stringResource(R.string.reset_subtitle),
                style = MaterialTheme.typography.bodyMedium
            )

            // 🔹 Código de 5 dígitos
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
                            val digit = input.filter { it.isDigit() }.takeLast(1)
                            field.onChange(digit)

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

            // 🔹 Nueva contraseña
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = viewModel.newPassword.value,
                onValueChange = { viewModel.newPassword.onChange(it) },
                label = { Text(stringResource(R.string.reset_new_password_label)) },
                visualTransformation = PasswordVisualTransformation(),
                isError = viewModel.newPassword.error != null,
                supportingText = {
                    viewModel.newPassword.error?.let { Text(it) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            // 🔹 Confirmar contraseña
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = viewModel.confirmPassword.value,
                onValueChange = { viewModel.confirmPassword.onChange(it) },
                label = { Text(stringResource(R.string.register_confirm_password_label)) },
                visualTransformation = PasswordVisualTransformation(),
                isError = viewModel.confirmPassword.error != null,
                supportingText = {
                    viewModel.confirmPassword.error?.let { Text(it) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            when (val result = viewModel.resetResult) {
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
                    viewModel.resetPassword()

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = resetLoadingMessage,
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                enabled = viewModel.isFormValid &&
                        viewModel.resetResult !is RequestResult.Loading
            ) {
                Text(stringResource(R.string.reset_button))
            }
        }
    }
}