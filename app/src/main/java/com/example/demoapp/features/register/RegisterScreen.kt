package com.example.demoapp.features.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.example.demoapp.R
import com.example.demoapp.core.utils.RequestResult
import com.example.demoapp.ui.theme.DemoAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Paleta (misma que Login) ─────────────────────────────────────────────────

private val GreenPrimary   = Color(0xFF2E7D5E)
private val BackgroundGray = Color(0xFFF0F4F2)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextGray       = Color(0xFF6B6B6B)
private val DividerColor   = Color(0xFFE0E0E0)

// ─── Pantalla ─────────────────────────────────────────────────────────────────

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToLogin: (() -> Unit)? = null
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()
    var termsAccepted by remember { mutableStateOf(false) }
    val registerLoadingMessage = stringResource(R.string.register_snackbar_loading)

    LaunchedEffect(viewModel.registerResult) {
        val result = viewModel.registerResult
        if (result is RequestResult.Success) {
            scope.launch {
                snackbarHostState.showSnackbar(result.data)
                delay(500) // Pequeña pausa para que el usuario vea el mensaje
                onNavigateToLogin?.invoke()
            }
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = BackgroundGray
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape     = RoundedCornerShape(24.dp),
                colors    = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ── Encabezado con flecha ──────────────────────────────
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick  = { onNavigateBack?.invoke() },
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.common_back),
                                tint               = Color(0xFF1A1A1A)
                            )
                        }
                        Text(
                            text       = stringResource(R.string.register_title),
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFF1A1A1A),
                            modifier   = Modifier.align(Alignment.Center)
                        )
                    }

                    Text(
                        text      = stringResource(R.string.register_subtitle),
                        fontSize  = 14.sp,
                        color     = TextGray,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.offset(y = (-8).dp)
                    )

                    // ── Campos ─────────────────────────────────────────────
                    OutlinedTextField(
                        modifier      = Modifier.fillMaxWidth(),
                        value         = viewModel.name.value,
                        onValueChange = { viewModel.name.onChange(it) },
                        label         = { Text(stringResource(R.string.register_name_label)) },
                        placeholder   = { Text(stringResource(R.string.register_name_placeholder), color = TextGray) },
                        isError       = viewModel.name.error != null,
                        supportingText = { viewModel.name.error?.let { Text(it) } },
                        shape  = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = GreenPrimary,
                            unfocusedBorderColor = DividerColor
                        )
                    )

                    OutlinedTextField(
                        modifier      = Modifier.fillMaxWidth(),
                        value         = viewModel.email.value,
                        onValueChange = { viewModel.email.onChange(it) },
                        label         = { Text(stringResource(R.string.login_email_label)) },
                        placeholder   = { Text(stringResource(R.string.login_email_placeholder), color = TextGray) },
                        isError       = viewModel.email.error != null,
                        supportingText = { viewModel.email.error?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape  = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = GreenPrimary,
                            unfocusedBorderColor = DividerColor
                        )
                    )

                    OutlinedTextField(
                        modifier              = Modifier.fillMaxWidth(),
                        value                 = viewModel.password.value,
                        onValueChange         = { viewModel.password.onChange(it) },
                        label                 = { Text(stringResource(R.string.login_password_label)) },
                        placeholder           = { Text(stringResource(R.string.register_password_placeholder), color = TextGray) },
                        visualTransformation  = PasswordVisualTransformation(),
                        isError               = viewModel.password.error != null,
                        supportingText        = { viewModel.password.error?.let { Text(it) } },
                        keyboardOptions       = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape  = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = GreenPrimary,
                            unfocusedBorderColor = DividerColor
                        )
                    )

                    OutlinedTextField(
                        modifier              = Modifier.fillMaxWidth(),
                        value                 = viewModel.confirmPassword.value,
                        onValueChange         = { viewModel.confirmPassword.onChange(it) },
                        label                 = { Text(stringResource(R.string.register_confirm_password_label)) },
                        placeholder           = { Text(stringResource(R.string.register_confirm_password_placeholder), color = TextGray) },
                        visualTransformation  = PasswordVisualTransformation(),
                        isError               = viewModel.confirmPassword.error != null,
                        supportingText        = { viewModel.confirmPassword.error?.let { Text(it) } },
                        keyboardOptions       = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape  = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = GreenPrimary,
                            unfocusedBorderColor = DividerColor
                        )
                    )

                    // ── Checkbox términos ──────────────────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked         = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            colors          = CheckboxDefaults.colors(
                                checkedColor = GreenPrimary
                            )
                        )
                        Text(
                            text     = stringResource(R.string.register_terms_text),
                            fontSize = 13.sp,
                            color    = TextGray,
                            modifier = Modifier.clickable { termsAccepted = !termsAccepted }
                        )
                    }

                    // ── Feedback del resultado ─────────────────────────────
                    when (val result = viewModel.registerResult) {
                        is RequestResult.Loading -> CircularProgressIndicator(color = GreenPrimary)
                        is RequestResult.Error   -> Text(
                            result.message,
                            color    = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                        is RequestResult.Success -> Text(
                            result.data,
                            color    = GreenPrimary,
                            fontSize = 13.sp
                        )
                        null -> {}
                    }

                    // ── Botón Registrarse ──────────────────────────────────
                    Button(
                        onClick = {
                            viewModel.register()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message  = registerLoadingMessage,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        enabled  = viewModel.isFormValid
                                && termsAccepted
                                && viewModel.registerResult !is RequestResult.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor         = GreenPrimary,
                            disabledContainerColor = GreenPrimary.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text       = stringResource(R.string.register_button),
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // ── ¿Ya tienes cuenta? ─────────────────────────────────
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = TextGray, fontSize = 13.sp)) {
                                append(stringResource(R.string.register_have_account))
                            }
                            withStyle(
                                SpanStyle(
                                    color      = GreenPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 13.sp
                                )
                            ) {
                                append(stringResource(R.string.register_login_action))
                            }
                        },
                        modifier = Modifier.clickable { onNavigateToLogin?.invoke() }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    DemoAppTheme {
        RegisterScreen()
    }
}