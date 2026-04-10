package com.example.demoapp.features.login

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demoapp.ui.theme.DemoAppTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.demoapp.core.utils.RequestResult
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.model.UserRole

private val GreenPrimary   = Color(0xFF2E7D5E)
private val BackgroundGray = Color(0xFFF0F4F2)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextGray       = Color(0xFF6B6B6B)
private val DividerColor   = Color(0xFFE0E0E0)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToUsers: () -> Unit,
    onNavigateToRegister: (() -> Unit)? = null,   // opcional, para el link "Regístrate"
    onNavigateToPasswordRecovery: (() -> Unit)? = null,
    onNavigateToModerator: (() -> Unit)? = null,
    onLoginSuccess: (User) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()
    var selectedRole      by remember { mutableStateOf(UserRole.USER) }
    val loginResult by viewModel.loginResult.collectAsState()

    LaunchedEffect(loginResult) {
        when (val result = loginResult) {
            is RequestResult.Success -> {
                val user = result.data
                onLoginSuccess(user)
                if (user.role == UserRole.ADMIN) {
                    onNavigateToModerator?.invoke()
                } else {
                    onNavigateToUsers()
                }
            }
            is RequestResult.Error -> {
                snackbarHostState.showSnackbar(
                    message  = result.message,
                    duration = SnackbarDuration.Short
                )
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = BackgroundGray
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // ── Tarjeta central ────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    // ── Encabezado ─────────────────────────────────────────
                    Text(
                        text = "Bienvenido",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "Inicia sesión para continuar",
                        fontSize = 14.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(y = (-12).dp)   // acerca al título
                    )

                    // ── Selector de rol (segmented) ────────────────────────
                    // Solo mostrar si es acceso de usuario
                    if (selectedRole == UserRole.USER) {
                        RoleSelector(
                            selectedIndex = selectedRole,
                            onRoleSelected = { selectedRole = it }
                        )
                    } else {
                        Text(
                            text       = "Acceso de Moderador",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color(0xFF1A1A1A),
                            modifier   = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    // ── Campos ─────────────────────────────────────────────
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = viewModel.email.value,
                        onValueChange = { viewModel.onEmailChange(it) },
                        label = { Text("Email") },
                        placeholder = { Text("tu@email.com", color = TextGray) },
                        isError = viewModel.email.error != null,
                        supportingText = {
                            viewModel.email.error?.let { Text(it) }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = GreenPrimary,
                            unfocusedBorderColor = DividerColor
                        )
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = viewModel.password.value,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text("Contraseña") },
                        isError = viewModel.password.error != null,
                        supportingText = {
                            viewModel.password.error?.let { Text(it) }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = GreenPrimary,
                            unfocusedBorderColor = DividerColor
                        )
                    )

                    // ── ¿Olvidaste tu contraseña? ──────────────────────────
                    if (selectedRole == UserRole.USER) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color                = GreenPrimary,
                                        fontSize             = 13.sp,
                                        textDecoration       = TextDecoration.Underline,
                                        fontWeight           = FontWeight.SemiBold
                                    )
                                ) {
                                    append("¿Olvidaste tu contraseña?")
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.Start)
                                .offset(y = (-8).dp)
                                .clickable { onNavigateToPasswordRecovery?.invoke() }
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message  = "Iniciando sesión...",
                                    duration = SnackbarDuration.Short
                                )
                            }
                            Log.d(
                                "Login",
                                "Email: ${viewModel.email.value}, " +
                                        "Password: ${viewModel.password.value}, " +
                                        "Rol selector: ${if (selectedRole == UserRole.USER) "Usuario" else "Moderador"}"
                            )
                            viewModel.login()   // ← solo llama login, la navegación la maneja LaunchedEffect
                        },
                        enabled  = viewModel.isFormValid,
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
                            text       = if (selectedRole == UserRole.USER) "Iniciar sesión" else "Acceso Moderador",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // ── ¿No tienes cuenta? ─────────────────────────────────
                    if (selectedRole == UserRole.USER) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = TextGray, fontSize = 13.sp)) {
                                    append("¿No tienes cuenta?, ")
                                }
                                withStyle(
                                    SpanStyle(
                                        color      = GreenPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize   = 13.sp
                                    )
                                ) {
                                    append("Regístrate")
                                }
                            },
                            modifier = Modifier.clickable {
                                onNavigateToRegister?.invoke()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleSelector(
    selectedIndex  : UserRole,
    onRoleSelected : (UserRole) -> Unit
) {
    val roles = listOf(UserRole.USER to "Usuario", UserRole.ADMIN to "Moderador")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(50))
            .background(BackgroundGray)
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            roles.forEach { (role, label) ->
                val isSelected = selectedIndex == role
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) GreenPrimary else Color.Transparent)
                        .clickable { onRoleSelected(role) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = label,
                        fontSize   = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (isSelected) Color.White else TextGray
                    )
                }
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