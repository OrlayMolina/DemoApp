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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demoapp.R
import com.example.demoapp.domain.model.TouristPoint
import com.example.demoapp.domain.model.TouristPointCategory
import androidx.compose.ui.draw.clip
import java.util.Calendar

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
    // Solo publicaciones aprobadas por el moderador cuentan para las estadisticas.
    val approved = points.filter { it.isVerified && !it.isRejected && !it.isDraft }
    val totalLikes    = approved.sumOf { it.importantVotes }
    val totalComments = approved.sumOf { it.commentCount }
    val uniquePlaces  = approved.size
    val totalViews    = approved.sumOf { it.visitedByUserIds.size }

    val bestPublication = approved.maxByOrNull { (it.importantVotes * 2) + (it.commentCount * 3) }

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

private val monthLabels = listOf(
    "Ene", "Feb", "Mar", "Abr", "May", "Jun",
    "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
)

private fun buildMonthlyPublicationSeries(points: List<TouristPoint>): List<Pair<String, Int>> {
    if (points.isEmpty()) return monthLabels.take(7).map { it to 0 }

    val counts = IntArray(12)
    points.forEach { point ->
        counts[point.createdAt.monthIndex()]++
    }

    return monthLabels.mapIndexed { index, label -> label to counts[index] }
        .dropWhile { it.second == 0 }
        .ifEmpty { monthLabels.take(7).map { it to 0 } }
}

private fun buildMonthlyLikesSeries(points: List<TouristPoint>): List<Pair<String, Float>> {
    if (points.isEmpty()) return monthLabels.take(7).map { it to 0f }

    val likes = FloatArray(12)
    points.forEach { point ->
        likes[point.createdAt.monthIndex()] += point.importantVotes.toFloat()
    }

    return monthLabels.mapIndexed { index, label -> label to likes[index] }
        .dropWhile { it.second == 0f }
        .ifEmpty { monthLabels.take(7).map { it to 0f } }
}

private fun Long.monthIndex(): Int = Calendar.getInstance().apply {
    timeInMillis = this@monthIndex
}.get(Calendar.MONTH)

// ─── Pantalla ─────────────────────────────────────────────────────────────────

@Composable
fun StatisticsScreen(
    publications  : List<TouristPoint> = TouristPoint.SAMPLE_LIST,
    onNavigateBack: () -> Unit         = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val approvedPublications = remember(publications) {
        publications.filter { it.isVerified && !it.isRejected && !it.isDraft }
    }
    val stats = remember(approvedPublications) { computeStats(approvedPublications) }
    val publicationsByMonth = remember(approvedPublications) { buildMonthlyPublicationSeries(approvedPublications) }
    val likesByMonth = remember(approvedPublications) { buildMonthlyLikesSeries(approvedPublications) }

    val categoryCount = remember(approvedPublications) {
        TouristPointCategory.entries.map { cat ->
            cat to approvedPublications.count { it.category == cat }
        }.filter { it.second > 0 }
            .sortedByDescending { it.second }
    }

    Scaffold(containerColor = colorScheme.background) { padding ->
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
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.common_back), tint = colorScheme.onBackground)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text       = stringResource(R.string.profile_button_statistics),
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = colorScheme.onBackground
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
                        label    = stringResource(R.string.stats_total_likes),
                        value    = formatNumber(stats.totalLikes)
                    )
                    StatCard(
                        modifier  = Modifier.weight(1f),
                        icon      = Icons.Default.Visibility,
                        iconColor = BlueAccent,
                        label     = stringResource(R.string.stats_total_views),
                        value     = formatNumber(stats.totalViews)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        modifier  = Modifier.weight(1f),
                        icon      = Icons.Default.ModeComment,
                        iconColor = GreenPrimary,
                        label     = stringResource(R.string.stats_total_comments),
                        value     = formatNumber(stats.totalComments)
                    )
                    StatCard(
                        modifier  = Modifier.weight(1f),
                        icon      = Icons.Default.LocationOn,
                        iconColor = PurplePrimary,
                        label     = stringResource(R.string.stats_unique_places),
                        value     = formatNumber(stats.uniquePlaces)
                    )
                }
            }

            // ── Gráfico de barras: Publicaciones por mes ───────────────────
            ChartCard(title = stringResource(R.string.stats_chart_publications_by_month)) {
                BarChart(
                    data      = publicationsByMonth,
                    barColor  = BlueAccent,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }

            // ── Gráfico de línea: Crecimiento de Likes ─────────────────────
            ChartCard(title = stringResource(R.string.stats_chart_likes_growth)) {
                LineChart(
                    data      = likesByMonth,
                    lineColor = PinkAccent,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }

            // ── Barras horizontales: Categorías más publicadas ─────────────
            ChartCard(title = stringResource(R.string.stats_chart_top_categories)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val maxVal = categoryCount.maxOfOrNull { it.second } ?: 1
                    categoryCount.forEach { (cat, count) ->
                        val categoryText = when (cat) {
                            TouristPointCategory.NATURE -> stringResource(R.string.stats_category_nature)
                            TouristPointCategory.GASTRONOMY -> stringResource(R.string.stats_category_gastronomy)
                            TouristPointCategory.CULTURE -> stringResource(R.string.stats_category_culture)
                            TouristPointCategory.ENTERTAINMENT -> stringResource(R.string.stats_category_urban_art)
                            else -> stringResource(R.string.stats_category_other)
                        }
                        CategoryBar(
                            label    = categoryText,
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
                                text       = stringResource(R.string.stats_best_publication),
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
                            )
                        }

                        Card(
                            shape  = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorScheme.surface.copy(alpha = 0.15f)
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
                                        stringResource(R.string.profile_publication_likes, best.importantVotes),
                                        fontSize = 12.sp,
                                        color    = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        stringResource(R.string.profile_publication_comments, best.commentCount),
                                        fontSize = 12.sp,
                                        color    = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        stringResource(R.string.stats_views_count, formatNumber(best.visitedByUserIds.size)),
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
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = colorScheme.surface),
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
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = colorScheme.surface),
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
    val colorScheme = MaterialTheme.colorScheme
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.stats_no_data), fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
        }
        return
    }

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
                Text(label, fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
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
    val colorScheme = MaterialTheme.colorScheme
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.stats_no_data), fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxVal = data.maxOfOrNull { it.second } ?: 1f
    val minVal = data.minOfOrNull { it.second } ?: 0f
    val range   = (maxVal - minVal).takeIf { it != 0f } ?: 1f

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val chartH  = size.height - 8.dp.toPx()
            val stepX   = if (data.size == 1) size.width / 2f else size.width / (data.size - 1).toFloat()

            val points = data.mapIndexed { i, (_, v) ->
                val x = i * stepX
                val y = chartH - ((v - minVal) / range) * chartH
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
                Text(label, fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
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
    val colorScheme = MaterialTheme.colorScheme
    val safeMax = maxCount.coerceAtLeast(1)

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = colorScheme.onSurface)
            Text(stringResource(R.string.stats_points_count, count), fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress   = { count.toFloat() / safeMax },
            modifier   = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
            color      = color,
            trackColor = colorScheme.surfaceVariant
        )
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun formatNumber(n: Int): String = when {
    n >= 1000 -> "${"%.1f".format(n / 1000f)}k"
    else      -> n.toString()
}

private fun List<Pair<String, Int>>.indexOfMax(): Int =
    if (isEmpty()) -1 else indexOfFirst { it.second == maxOfOrNull { p -> p.second } }


private fun categoryColor(cat: TouristPointCategory) = when (cat) {
    TouristPointCategory.NATURE -> Color(0xFF388E3C)
    TouristPointCategory.GASTRONOMY -> Color(0xFFE64A19)
    TouristPointCategory.CULTURE -> Color(0xFF7B1FA2)
    TouristPointCategory.ENTERTAINMENT -> Color(0xFF1976D2)
    else -> Color(0xFF455A64)
}