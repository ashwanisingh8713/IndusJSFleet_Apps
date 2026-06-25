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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetSectionHeader
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.reports.domain.entity.VehicleProfitLoss
import com.ijs.reports.presentation.ReportChartType
import com.ijs.reports.presentation.localizedLabel
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlin.math.abs
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// Bespoke chart layout dimensions — not part of the FleetTokens spacing/icon scale,
// so they live as named constants rather than raw literals at the call sites.
private val ChartPlaceholderHeight: Dp = 200.dp
private val BarChartLabelWidth: Dp = 80.dp
private val BarChartValueWidth: Dp = 70.dp
private val BarChartBarHeight: Dp = 20.dp

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

    FleetSectionCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FleetSectionHeader(
                    title = stringResource(Res.string.reports_fleet_financial_summary),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                Surface(
                    shape = RoundedCornerShape(FleetTokens.Radius.M),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = stringResource(Res.string.reports_vehicles_count_label, totalVehicles),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.XS)
                    )
                }
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

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

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

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
    FleetMetricTile(
        value = value,
        label = label,
        iconRes = icon,
        accent = color,
        valueColor = color,
        // Highlighted KPI (Net Profit) keeps the tinted box for emphasis vs the others.
        showBackground = isHighlighted,
        centered = true
    )
}


@Composable
internal fun SecondaryStatItem(
    label: String,
    value: String,
    color: Color
) {
    FleetMetricTile(
        value = value,
        label = label,
        accent = color,
        valueColor = color,
        showBackground = false,
        centered = true
    )
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
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
    ) {
        topPerformer?.let {
            val vehicleFallback = stringResource(Res.string.reports_vehicle_id_fallback, it.vehicleId)
            FleetSectionCard(
                modifier = Modifier.weight(1f),
                containerColor = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen.copy(alpha = 0.1f),
                border = null,
                contentPadding = FleetTokens.Spacing.M
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_trophy),
                        contentDescription = null,
                        modifier = Modifier.size(FleetTokens.IconSize.M),
                        tint = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
                    )
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                    Text(
                        text = stringResource(Res.string.reports_top_performer),
                        style = MaterialTheme.typography.labelMedium,
                        color = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
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

        worstPerformer?.let {
            val vehicleFallback = stringResource(Res.string.reports_vehicle_id_fallback, it.vehicleId)
            FleetSectionCard(
                modifier = Modifier.weight(1f),
                containerColor = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed.copy(alpha = 0.1f),
                border = null,
                contentPadding = FleetTokens.Spacing.M
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_warning),
                        contentDescription = null,
                        modifier = Modifier.size(FleetTokens.IconSize.M),
                        tint = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
                    )
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                    Text(
                        text = stringResource(Res.string.reports_needs_attention),
                        style = MaterialTheme.typography.labelMedium,
                        color = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
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
    FleetSectionCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FleetSectionHeader(
                    title = stringResource(Res.string.reports_pl_chart_top_10),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                Row(horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)) {
                    ReportChartType.entries.forEach { type ->
                        FilterChip(
                            selected = chartType == type,
                            onClick = { onChartTypeChange(type) },
                            label = { Text(type.localizedLabel(), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            if (results.isNotEmpty()) {
                SimpleBarChart(results = results)
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(ChartPlaceholderHeight),
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

    Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
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
                    modifier = Modifier.width(BarChartLabelWidth),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(BarChartBarHeight)
                        .clip(RoundedCornerShape(FleetTokens.Radius.S))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(barWidth.coerceIn(0.05f, 1f))
                            .background(if (isProfit) profitColor else lossColor)
                    )
                }

                Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))

                Text(
                    text = formatCurrency(result.netProfit),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isProfit) profitColor else lossColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(BarChartValueWidth),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
internal fun SummaryGridContent(results: List<VehicleProfitLoss>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S),
        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
    ) {
        results.forEach { result ->
            VehiclePLSummaryChip(result = result)
        }
    }
}

@Composable
internal fun VehiclePLSummaryChip(result: VehicleProfitLoss) {
    val isProfit = result.netProfit >= 0
    val accent = if (isProfit) com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed

    FleetMetricTile(
        value = formatCurrency(result.netProfit),
        label = result.vehicleNumber ?: "#${result.vehicleId}",
        subLabel = stringResource(Res.string.reports_trips_count, result.totalTrips),
        accent = accent,
        valueColor = accent,
        centered = true
    )
}

// ============================================================================
// Vehicle Filter Sheet Content
// ============================================================================

