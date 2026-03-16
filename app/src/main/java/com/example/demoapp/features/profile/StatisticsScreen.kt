package com.example.demoapp.features.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.TouristPointCategory
import androidx.compose.ui.draw.clip

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val PurplePrimary  = Color(0xFF7C4DFF)
private val BackgroundGray = Color(0xFFF5F5F5)
private val CardWhite      = Color(0xFFFFFFFF)
private val TextGray       = Color(0xFF6B6B6B)
private val TextDark       = Color(0xFF1A1A1A)
private val PinkAccent     = Color(0xFFE91E63)
private val BlueAccent     = Color(0xFF1E88E5)
private val GreenPrimary   = Color(0xFF2E7D5E)

// ─── Datos calculados desde SAMPLE_LIST ───────────────────────────────────────

private fun computeStats(points: List<TouristPoint>): StatsSummary {
    val totalLikes    = points.sumOf { it.importantVotes }
    val totalComments = points.sumOf { it.commentCount }
    val uniquePlaces  = points.size

    // Simulamos visualizaciones como votos * factor
    val totalViews = totalLikes * 6 + uniquePlaces * 100

    val bestPublication = points.maxByOrNull { it.importantVotes + it.commentCount }

    return StatsSummary(
        totalLikes    = totalLikes,
        totalViews    = totalViews,
        totalComments = totalComments,
        uniquePlaces  = uniquePlaces,
        bestPublication = bestPublication
    )
}

data class StatsSummary(
    val totalLikes      : Int,
    val totalViews      : Int,
    val totalComments   : Int,
    val uniquePlaces    : Int,
    val bestPublication : TouristPoint?
)

// Publicaciones por mes (datos quemados de ejemplo)
private val publicationsByMonth = listOf(
    "Ene" to 1, "Feb" to 2, "Mar" to 3,
    "Abr" to 1, "May" to 2, "Jun" to 5, "Jul" to 3
)

// Crecimiento de likes por mes
private val likesByMonth = listOf(
    "Ene" to 20f, "Feb" to 45f, "Mar" to 80f,
    "Abr" to 60f, "May" to 120f, "Jun" to 200f, "Jul" to 250f
)

// ─── Pantalla ─────────────────────────────────────────────────────────────────

@Composable
fun StatisticsScreen(
    publications  : List<TouristPoint> = TouristPoint.SAMPLE_LIST,
    onNavigateBack: () -> Unit         = {}
) {
    val stats = computeStats(publications)

    val categoryCount = TouristPointCategory.entries.map { cat ->
        cat to publications.count { it.category == cat }
    }.filter { it.second > 0 }
        .sortedByDescending { it.second }

    Scaffold(containerColor = BackgroundGray) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Header ─────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = TextDark)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text       = "Estadísticas",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextDark
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(48.dp))
            }

            // ── Cards resumen ──────────────────────────────────────────────
            Column(
                modifier            = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon     = Icons.Default.Favorite,
                        iconColor = PinkAccent,
                        label    = "Total de Likes",
                        value    = formatNumber(stats.totalLikes)
                    )
                    StatCard(
                        modifier  = Modifier.weight(1f),
                        icon      = Icons.Default.Visibility,
                        iconColor = BlueAccent,
                        label     = "Visualizaciones",
                        value     = formatNumber(stats.totalViews)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        modifier  = Modifier.weight(1f),
                        icon      = Icons.Default.ModeComment,
                        iconColor = GreenPrimary,
                        label     = "Comentarios",
                        value     = formatNumber(stats.totalComments)
                    )
                    StatCard(
                        modifier  = Modifier.weight(1f),
                        icon      = Icons.Default.LocationOn,
                        iconColor = PurplePrimary,
                        label     = "Lugares Únicos",
                        value     = formatNumber(stats.uniquePlaces)
                    )
                }
            }

            // ── Gráfico de barras: Publicaciones por mes ───────────────────
            ChartCard(title = "Publicaciones por mes") {
                BarChart(
                    data      = publicationsByMonth,
                    barColor  = BlueAccent,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }

            // ── Gráfico de línea: Crecimiento de Likes ─────────────────────
            ChartCard(title = "Crecimiento de Likes") {
                LineChart(
                    data      = likesByMonth,
                    lineColor = PinkAccent,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }

            // ── Barras horizontales: Categorías más publicadas ─────────────
            ChartCard(title = "Categorías Más Publicadas") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val maxVal = categoryCount.maxOfOrNull { it.second } ?: 1
                    categoryCount.forEach { (cat, count) ->
                        CategoryBar(
                            label    = categoryLabel(cat),
                            count    = count,
                            maxCount = maxVal,
                            color    = categoryColor(cat)
                        )
                    }
                }
            }

            // ── Mejor publicación ──────────────────────────────────────────
            stats.bestPublication?.let { best ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color(0xFF7C4DFF), Color(0xFF448AFF))
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text("🏆", fontSize = 18.sp)
                            Text(
                                text       = "Tu Mejor Publicación",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
                            )
                        }

                        Card(
                            shape  = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.15f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text       = best.title,
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = Color.White
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        "${best.importantVotes} likes",
                                        fontSize = 12.sp,
                                        color    = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        "${best.commentCount} comentarios",
                                        fontSize = 12.sp,
                                        color    = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        "${formatNumber(best.importantVotes * 6 + 100)} vistas",
                                        fontSize = 12.sp,
                                        color    = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Composables auxiliares ───────────────────────────────────────────────────

@Composable
private fun StatCard(
    modifier  : Modifier,
    icon      : ImageVector,
    iconColor : Color,
    label     : String,
    value     : String
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
                Text(label, fontSize = 11.sp, color = TextGray)
            }
            Text(
                text       = value,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = TextDark
            )
        }
    }
}

@Composable
private fun ChartCard(
    title   : String,
    content : @Composable () -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text       = title,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextDark
            )
            content()
        }
    }
}

@Composable
private fun BarChart(
    data     : List<Pair<String, Int>>,
    barColor : Color,
    modifier : Modifier = Modifier
) {
    val maxVal = data.maxOfOrNull { it.second }?.toFloat() ?: 1f

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val barWidth   = size.width / (data.size * 2f)
            val chartH     = size.height - 20.dp.toPx()
            val highlightI = data.indexOfMax()

            data.forEachIndexed { i, (_, value) ->
                val barH  = (value / maxVal) * chartH
                val x     = i * (size.width / data.size) + barWidth / 2f
                val color = if (i == highlightI) barColor else barColor.copy(alpha = 0.5f)

                drawRoundRect(
                    color       = color,
                    topLeft     = Offset(x, chartH - barH),
                    size        = Size(barWidth, barH),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }

        // Labels eje X
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data.forEach { (label, _) ->
                Text(label, fontSize = 10.sp, color = TextGray)
            }
        }
    }
}

@Composable
private fun LineChart(
    data      : List<Pair<String, Float>>,
    lineColor : Color,
    modifier  : Modifier = Modifier
) {
    val maxVal = data.maxOfOrNull { it.second } ?: 1f
    val minVal = data.minOfOrNull { it.second } ?: 0f

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val chartH  = size.height - 8.dp.toPx()
            val stepX   = size.width / (data.size - 1).toFloat()

            val points = data.mapIndexed { i, (_, v) ->
                val x = i * stepX
                val y = chartH - ((v - minVal) / (maxVal - minVal)) * chartH
                Offset(x, y)
            }

            // Área rellena bajo la línea
            val fillPath = Path().apply {
                moveTo(points.first().x, chartH)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, chartH)
                close()
            }
            drawPath(fillPath, color = lineColor.copy(alpha = 0.12f))

            // Línea
            val linePath = Path().apply {
                points.forEachIndexed { i, p ->
                    if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                }
            }
            drawPath(
                linePath,
                color     = lineColor,
                style     = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Puntos
            points.forEach { p ->
                drawCircle(color = lineColor, radius = 4.dp.toPx(), center = p)
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = p)
            }
        }

        // Labels eje X
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { (label, _) ->
                Text(label, fontSize = 10.sp, color = TextGray)
            }
        }
    }
}

@Composable
private fun CategoryBar(
    label    : String,
    count    : Int,
    maxCount : Int,
    color    : Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = TextDark)
            Text("$count punto${if (count != 1) "s" else ""}", fontSize = 11.sp, color = TextGray)
        }
        LinearProgressIndicator(
            progress   = { count.toFloat() / maxCount },
            modifier   = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
            color      = color,
            trackColor = Color(0xFFF0F0F0)
        )
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun formatNumber(n: Int): String = when {
    n >= 1000 -> "${"%.1f".format(n / 1000f)}k"
    else      -> n.toString()
}

private fun List<Pair<String, Int>>.indexOfMax(): Int =
    indexOfFirst { it.second == maxOfOrNull { p -> p.second } }

private fun categoryLabel(cat: TouristPointCategory) = when (cat) {
    TouristPointCategory.NATURE        -> "Naturaleza"
    TouristPointCategory.GASTRONOMY    -> "Gastronomía"
    TouristPointCategory.CULTURE       -> "Cultura"
    TouristPointCategory.ENTERTAINMENT -> "Arte Urbano"
    else                               -> "Otro"
}

private fun categoryColor(cat: TouristPointCategory) = when (cat) {
    TouristPointCategory.NATURE -> Color(0xFF388E3C)
    TouristPointCategory.GASTRONOMY -> Color(0xFFE64A19)
    TouristPointCategory.CULTURE -> Color(0xFF7B1FA2)
    TouristPointCategory.ENTERTAINMENT -> Color(0xFF1976D2)
    else -> Color(0xFF455A64)
}