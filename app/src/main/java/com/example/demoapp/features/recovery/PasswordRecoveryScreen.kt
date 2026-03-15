package com.example.demoapp.features.recovery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.core.utils.RequestResult

// ─── Paleta (misma que Login y Register) ──────────────────────────────────────

private val GreenPrimary   = Color(0xFF2E7D5E)
private val BackgroundGray = Color(0xFFF0F4F2)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextGray       = Color(0xFF6B6B6B)
private val DividerColor   = Color(0xFFE0E0E0)

// ─── Pantalla ─────────────────────────────────────────────────────────────────

@Composable
fun PasswordRecoveryScreen(
    viewModel: PasswordRecoveryViewModel = viewModel(),
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToLogin: (() -> Unit)? = null
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost   = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = BackgroundGray
    ) { paddingValues ->

        Box(
            modifier          = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment  = Alignment.Center
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
                        .padding(horizontal = 24.dp, vertical = 28.dp),
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
                                contentDescription = "Volver",
                                tint               = Color(0xFF1A1A1A)
                            )
                        }
                        Text(
                            text       = "Recuperar Contraseña",
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFF1A1A1A),
                            modifier   = Modifier.align(Alignment.Center)
                        )
                    }

                    // ── Subtítulo ──────────────────────────────────────────
                    Text(
                        text      = "Ten enviaremos un enlace de recuperación",
                        fontSize  = 13.sp,
                        color     = TextGray,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.offset(y = (-8).dp)
                    )

                    // ── Campo email ────────────────────────────────────────
                    OutlinedTextField(
                        modifier        = Modifier.fillMaxWidth(),
                        value           = viewModel.email.value,
                        onValueChange   = { viewModel.email.onChange(it) },
                        label           = { Text("Email") },
                        placeholder     = { Text("tu@email.com", color = TextGray) },
                        isError         = viewModel.email.error != null,
                        supportingText  = {
                            Text(
                                text  = viewModel.email.error ?: "Ingresa el email asociado a tu cuenta",
                                color = if (viewModel.email.error != null)
                                    MaterialTheme.colorScheme.error
                                else TextGray
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape  = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = GreenPrimary,
                            unfocusedBorderColor = DividerColor
                        )
                    )

                    // ── Feedback resultado ─────────────────────────────────
                    when (val result = viewModel.recoveryResult) {
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

                    // ── Botón ──────────────────────────────────────────────
                    Button(
                        onClick  = { viewModel.sendRecoveryEmail() },
                        enabled  = viewModel.email.isValid &&
                                viewModel.recoveryResult !is RequestResult.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor         = GreenPrimary,
                            disabledContainerColor = GreenPrimary.copy(alpha = 0.4f)
                        )
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Email,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text       = "Enviar Enlace de Recuperación",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // ── ¿Recordaste tu contraseña? ─────────────────────────
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = TextGray, fontSize = 13.sp)) {
                                append("¿Recordaste tu contraseña?, ")
                            }
                            withStyle(
                                SpanStyle(
                                    color      = GreenPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 13.sp
                                )
                            ) {
                                append("Inicia Sesión")
                            }
                        },
                        modifier = Modifier.clickable { onNavigateToLogin?.invoke() }
                    )
                }
            }
        }
    }
}