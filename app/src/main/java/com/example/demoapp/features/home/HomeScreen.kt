package com.example.demoapp.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.demoapp.R
import com.example.demoapp.core.components.FeatureCard
import com.example.demoapp.data.OnboardingFeature
import com.example.demoapp.ui.theme.DemoAppTheme
import kotlinx.coroutines.launch

private val GreenPrimary  = Color(0xFF2E7D5E)
private val PinkAccent    = Color(0xFFE91E63)
private val OrangeAccent  = Color(0xFFFF5722)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val features = listOf(
        OnboardingFeature(
            icon = Icons.Default.LocationOn,
            iconTint = GreenPrimary,
            title = "Descubre lugares únicos",
            description = "Explora puntos de interés compartidos por la comunidad"
        ),
        OnboardingFeature(
            icon = Icons.Default.Photo,
            iconTint = PinkAccent,
            title = "Comparte tus hallazgos",
            description = "Publica fotos y ubicaciones de lugares especiales"
        ),
        OnboardingFeature(
            icon = Icons.Default.Person,
            iconTint = GreenPrimary,
            title = "Conecta con exploradores",
            description = "Únete a una comunidad de amantes de la exploración"
        ),
        OnboardingFeature(
            icon = Icons.Default.Leaderboard,
            iconTint = OrangeAccent,
            title = "Gana logros",
            description = "Desbloquea badges y sube de nivel mientras exploras"
        )
    )

    val pagerState   = rememberPagerState(pageCount = { features.size })
    val scope        = rememberCoroutineScope()
    val isLastPage   = pagerState.currentPage == features.lastIndex

    Scaffold(containerColor = Color.White) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // ── Botón "Saltar" (esquina superior derecha) ──────────────────
            AnimatedVisibility(
                visible = !isLastPage,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
            ) {
                Button(
                    onClick = onNavigateToLogin,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(text = "Saltar", fontSize = 13.sp)
                }
            }

            // ── Layout principal ───────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(142.dp))

                // Logo
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.logo_red_explora),
                    contentDescription = "Logo RedExplora",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ── Carrusel de features ───────────────────────────────────
                HorizontalPager(
                    state = pagerState,
                    // contentPadding revela el borde de la tarjeta siguiente
                    contentPadding = PaddingValues(horizontal = 40.dp),
                    pageSpacing = 14.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) { page ->
                    FeatureCard(feature = features[page])
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Indicadores de página (dots) ───────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(features.size) { index ->
                        val selected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (selected) 22.dp else 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (selected) GreenPrimary else Color(0xFFD0D0D0)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // ── Botones inferiores ─────────────────────────────────────
                if (isLastPage) {
                    // Última página → mostrar Login + Registro
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToLogin,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GreenPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp, GreenPrimary
                            )
                        ) {
                            Text(text = "Iniciar sesión", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onNavigateToRegister,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary
                            )
                        ) {
                            Text(text = "Crear una cuenta", fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    // Páginas intermedias → botón "Siguiente"
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .padding(bottom = 32.dp)
                            .width(180.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Text(
                            text = "Siguiente",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    DemoAppTheme {
        HomeScreen(
            onNavigateToLogin = {},
            onNavigateToRegister = {}
        )
    }
}