package com.indusjs.uicomponents.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Data class representing a slice of the pie chart.
 */
data class PieChartData(
    val label: String,
    val value: Double,
    val color: Color
)

/**
 * A simple animated pie chart composable.
 *
 * @param data List of PieChartData items to display
 * @param modifier Modifier for the chart
 * @param chartSize Size of the pie chart
 * @param strokeWidth Width of the pie chart stroke (for donut style)
 * @param animationDuration Duration of the drawing animation in milliseconds
 * @param showLegend Whether to show the legend below the chart
 */
@Composable
fun PieChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier,
    chartSize: Dp = 160.dp,
    strokeWidth: Dp = 24.dp,
    animationDuration: Int = 1000,
    showLegend: Boolean = true
) {
    val total = data.sumOf { it.value }
    if (total <= 0) {
        EmptyPieChart(chartSize = chartSize, modifier = modifier)
        return
    }

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = animationDuration)
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(chartSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasSize = size.minDimension
                val radius = (canvasSize - strokeWidth.toPx()) / 2
                val center = Offset(size.width / 2, size.height / 2)
                val arcSize = Size(radius * 2, radius * 2)
                val topLeft = Offset(center.x - radius, center.y - radius)

                var startAngle = -90f // Start from top

                data.forEach { slice ->
                    val sweepAngle = ((slice.value / total) * 360f * animationProgress.value).toFloat()

                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(
                            width = strokeWidth.toPx(),
                            cap = StrokeCap.Butt
                        )
                    )
                    startAngle += sweepAngle
                }
            }

            // Center text showing total
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "₹${formatCompactAmount(total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Legend
        if (showLegend) {
            PieChartLegend(data = data, total = total)
        }
    }
}

/**
 * Legend component for the pie chart.
 */
@Composable
private fun PieChartLegend(
    data: List<PieChartData>,
    total: Double
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.forEach { slice ->
            val percentage = if (total > 0) (slice.value / total * 100) else 0.0

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Color dot
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(slice.color)
                    )
                    // Label
                    Text(
                        text = slice.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Amount
                    Text(
                        text = "₹${formatCompactAmount(slice.value)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Percentage
                    Text(
                        text = "${percentage.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Empty state for pie chart when there's no data.
 */
@Composable
private fun EmptyPieChart(
    chartSize: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(chartSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasSize = size.minDimension
                val radius = (canvasSize - 24.dp.toPx()) / 2
                val center = Offset(size.width / 2, size.height / 2)

                drawCircle(
                    color = Color.Gray.copy(alpha = 0.2f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 24.dp.toPx())
                )
            }

            Text(
                text = "No data",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Format amount to compact form (e.g., 1.5K, 2.3M).
 */
private fun formatCompactAmount(amount: Double): String {
    return when {
        amount >= 10_000_000 -> "${((amount / 10_000_000 * 10).toInt() / 10.0)}Cr"
        amount >= 100_000 -> "${((amount / 100_000 * 10).toInt() / 10.0)}L"
        amount >= 1_000 -> "${((amount / 1_000 * 10).toInt() / 10.0)}K"
        else -> "${amount.toInt()}"
    }
}

/**
 * Predefined colors for expense categories.
 */
object PieChartColors {
    val Fuel = com.indusjs.uicomponents.theme.FleetStatusColors.InfoBlue // Blue
    val Toll = Color(0xFF10B981) // Green
    val Maintenance = com.indusjs.uicomponents.theme.FleetStatusColors.ExpenseAmber // Amber
    val DriverAllowance = Color(0xFF8B5CF6) // Purple
    val Parking = Color(0xFFEC4899) // Pink
    val Loading = Color(0xFF06B6D4) // Cyan
    val Other = Color(0xFF6B7280) // Gray

    fun getCostTypeColor(costType: String): Color {
        return when (costType.lowercase()) {
            "fuel" -> Fuel
            "toll" -> Toll
            "maintenance", "servicing", "tyre", "battery" -> Maintenance
            "driver_allowance" -> DriverAllowance
            "parking" -> Parking
            "loading_charges", "unloading_charges" -> Loading
            else -> Other
        }
    }
}

