package com.ijs.reports.presentation.vehicle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.util.formatCurrency
import com.ijs.reports.domain.entity.VehicleProfitLoss
import com.ijs.reports.presentation.ReportChartType
import kotlin.math.abs

@Composable
internal fun FleetSummaryKPICard(
    totalVehicles: Int,
    profitableCount: Int,
    lossMakingCount: Int,
    totalRevenue: Double,
    totalExpenses: Double,
    netProfit: Double,
    profitMargin: Double,
    avgProfitPerVehicle: Double
) {
    val isProfit = netProfit >= 0
    val profitColor = com.indusjs.uicomponents.theme.FleetStatusColors.profitLossColor(netProfit)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fleet Financial Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "$totalVehicles Vehicles",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main P&L Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KPIItem(
                    label = "Revenue",
                    value = formatCurrency(totalRevenue),
                    icon = "💰",
                    color = MaterialTheme.colorScheme.primary
                )
                KPIItem(
                    label = "Expenses",
                    value = formatCurrency(totalExpenses),
                    icon = "📉",
                    color = MaterialTheme.colorScheme.error
                )
                KPIItem(
                    label = if (isProfit) "Profit" else "Loss",
                    value = formatCurrency(abs(netProfit)),
                    icon = if (isProfit) "📈" else "📉",
                    color = profitColor,
                    isHighlighted = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SecondaryStatItem(
                    label = "Profitable",
                    value = "$profitableCount",
                    color = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
                )
                SecondaryStatItem(
                    label = "Loss Making",
                    value = "$lossMakingCount",
                    color = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
                )
                SecondaryStatItem(
                    label = "Margin",
                    value = "${formatPercentage(profitMargin)}%",
                    color = profitColor
                )
                SecondaryStatItem(
                    label = "Avg/Vehicle",
                    value = formatCurrency(avgProfitPerVehicle),
                    color = com.indusjs.uicomponents.theme.FleetStatusColors.profitLossColor(avgProfitPerVehicle)
                )
            }
        }
    }
}


@Composable
internal fun KPIItem(
    label: String,
    value: String,
    icon: String,
    color: Color,
    isHighlighted: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = if (isHighlighted) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
internal fun SecondaryStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ============================================================================
// Performers Card - Top and Worst
// ============================================================================


@Composable
internal fun PerformersCard(
    topPerformer: VehicleProfitLoss?,
    worstPerformer: VehicleProfitLoss?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Performer
        topPerformer?.let {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏆", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Top Performer",
                            style = MaterialTheme.typography.labelMedium,
                            color = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it.vehicleNumber ?: "Vehicle #${it.vehicleId}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+${formatCurrency(it.netProfit)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Worst Performer
        worstPerformer?.let {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚠️", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Needs Attention",
                            style = MaterialTheme.typography.labelMedium,
                            color = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it.vehicleNumber ?: "Vehicle #${it.vehicleId}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatCurrency(it.netProfit),
                        style = MaterialTheme.typography.bodyMedium,
                        color = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ============================================================================
// Period Selection Row
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)

@Composable
internal fun ChartViewContent(
    results: List<VehicleProfitLoss>,
    chartType: ReportChartType,
    onChartTypeChange: (ReportChartType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Chart type selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "P&L Chart (Top 10)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ReportChartType.entries.forEach { type ->
                        FilterChip(
                            selected = chartType == type,
                            onClick = { onChartTypeChange(type) },
                            label = { Text(type.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Simple Bar Chart
            if (results.isNotEmpty()) {
                SimpleBarChart(results = results)
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data to display", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

internal fun formatPercentage(value: Double): String {
    val rounded = (value * 10).toLong() / 10.0
    return if (rounded == rounded.toLong().toDouble()) {
        "${rounded.toLong()}.0"
    } else {
        rounded.toString()
    }
}


@Composable
internal fun SimpleBarChart(results: List<VehicleProfitLoss>) {
    val maxProfit = results.maxOfOrNull { abs(it.netProfit) } ?: 1.0
    val profitColor = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
    val lossColor = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        results.forEach { result ->
            val barWidth = (abs(result.netProfit) / maxProfit).toFloat()
            val isProfit = result.netProfit >= 0

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vehicle label
                Text(
                    text = result.vehicleNumber?.take(10) ?: "#${result.vehicleId}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(80.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(barWidth.coerceIn(0.05f, 1f))
                            .background(if (isProfit) profitColor else lossColor)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Value
                Text(
                    text = formatCurrency(result.netProfit),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isProfit) profitColor else lossColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(70.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
internal fun SummaryGridContent(results: List<VehicleProfitLoss>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        results.forEach { result ->
            VehiclePLSummaryChip(result = result)
        }
    }
}

@Composable
internal fun VehiclePLSummaryChip(result: VehicleProfitLoss) {
    val isProfit = result.netProfit >= 0
    val bgColor = if (isProfit) com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen.copy(alpha = 0.1f) else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed.copy(alpha = 0.1f)
    val textColor = if (isProfit) com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = result.vehicleNumber ?: "#${result.vehicleId}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatCurrency(result.netProfit),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${result.totalTrips} trips",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================================
// Vehicle Filter Sheet Content
// ============================================================================


