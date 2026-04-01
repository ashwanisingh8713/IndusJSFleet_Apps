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
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlin.math.abs
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.reports_fleet_financial_summary),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = stringResource(Res.string.reports_vehicles_count_label, totalVehicles),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KPIItem(
                    label = stringResource(Res.string.reports_revenue),
                    value = formatCurrency(totalRevenue),
                    icon = Res.drawable.ic_trending_up,
                    color = MaterialTheme.colorScheme.primary
                )
                KPIItem(
                    label = stringResource(Res.string.reports_expenses),
                    value = formatCurrency(totalExpenses),
                    icon = Res.drawable.ic_trending_down,
                    color = MaterialTheme.colorScheme.error
                )
                KPIItem(
                    label = if (isProfit) stringResource(Res.string.reports_profit) else stringResource(Res.string.reports_loss),
                    value = formatCurrency(abs(netProfit)),
                    icon = if (isProfit) Res.drawable.ic_trending_up else Res.drawable.ic_trending_down,
                    color = profitColor,
                    isHighlighted = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SecondaryStatItem(
                    label = stringResource(Res.string.reports_profitable),
                    value = "$profitableCount",
                    color = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
                )
                SecondaryStatItem(
                    label = stringResource(Res.string.reports_loss_making),
                    value = "$lossMakingCount",
                    color = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
                )
                SecondaryStatItem(
                    label = stringResource(Res.string.reports_margin),
                    value = "${formatPercentage(profitMargin)}%",
                    color = profitColor
                )
                SecondaryStatItem(
                    label = stringResource(Res.string.reports_avg_per_vehicle),
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
    icon: org.jetbrains.compose.resources.DrawableResource,
    color: Color,
    isHighlighted: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = color
        )
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
        topPerformer?.let {
            val vehicleFallback = stringResource(Res.string.reports_vehicle_id_fallback, it.vehicleId)
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_trophy),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(Res.string.reports_top_performer),
                            style = MaterialTheme.typography.labelMedium,
                            color = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it.vehicleNumber ?: vehicleFallback,
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

        worstPerformer?.let {
            val vehicleFallback = stringResource(Res.string.reports_vehicle_id_fallback, it.vehicleId)
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_warning),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(Res.string.reports_needs_attention),
                            style = MaterialTheme.typography.labelMedium,
                            color = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it.vehicleNumber ?: vehicleFallback,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.reports_pl_chart_top_10),
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

            if (results.isNotEmpty()) {
                SimpleBarChart(results = results)
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.reports_no_data),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                Text(
                    text = result.vehicleNumber?.take(10) ?: "#${result.vehicleId}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(80.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

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
                text = stringResource(Res.string.reports_trips_count, result.totalTrips),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================================
// Vehicle Filter Sheet Content
// ============================================================================

