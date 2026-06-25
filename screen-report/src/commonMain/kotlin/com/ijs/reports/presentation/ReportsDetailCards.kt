package com.ijs.reports.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.PieChart
import com.indusjs.uicomponents.components.PieChartColors
import com.indusjs.uicomponents.components.PieChartData
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.reports.domain.entity.ExpenseBreakdownItem
import com.ijs.reports.domain.entity.VehiclePerformer
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

// ═══════════════════════════════════════════════════════════════════
// Expense Breakdown
// ═══════════════════════════════════════════════════════════════════

@Composable
internal fun ExpenseBreakdownSection(breakdown: List<ExpenseBreakdownItem>) {
    var expanded by remember { mutableStateOf(false) }
    val totalExpenses = breakdown.sumOf { it.amount }

    FleetSectionCard {
        Column {
            Text(
                stringResource(Res.string.reports_total_amount, formatCurrency(totalExpenses)),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(FleetTokens.Spacing.M))
            // Stable colour per category: keep semantic colours for recognised types,
            // else assign a distinct palette colour by index so slices + legend dots are
            // distinguishable (not all grey) when the backend returns opaque cost codes.
            val colorByType: Map<String, Color> = breakdown.mapIndexed { i, b ->
                val named = PieChartColors.getCostTypeColor(b.type)
                b.type to (if (named != PieChartColors.Other) named else PieChartColors.colorAt(i))
            }.toMap()
            PieChart(
                data = breakdown.map {
                    PieChartData(
                        formatCostType(it.type), it.amount,
                        colorByType[it.type] ?: PieChartColors.getCostTypeColor(it.type)
                    )
                },
                chartSize = 130.dp, strokeWidth = 18.dp, showLegend = false
            )
            Spacer(Modifier.height(FleetTokens.Spacing.M))
            (if (expanded) breakdown else breakdown.take(4))
                .sortedByDescending { it.amount }.forEach { item ->
                    ExpenseItemRow(
                        formatCostType(item.type), item.amount,
                        item.percentage, colorByType[item.type] ?: PieChartColors.getCostTypeColor(item.type),
                        totalExpenses
                    )
                    Spacer(Modifier.height(FleetTokens.Spacing.S))
                }
            if (breakdown.size > 4) {
                TextButton(
                    onClick = { expanded = !expanded },
                    Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        if (expanded) stringResource(Res.string.reports_show_less_caps)
                        else stringResource(Res.string.reports_show_all_caps, breakdown.size)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseItemRow(
    type: String, amount: Double, percentage: Double,
    color: Color, total: Double
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
        ) {
            // Legend dot: bespoke 10dp glyph, not on the spacing scale.
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Text(
                type,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
        ) {
            // Inline mini bar + fixed numeric columns: bespoke widths, no matching token.
            Box(
                Modifier.width(40.dp).height(FleetTokens.Spacing.XS)
                    .clip(RoundedCornerShape(FleetTokens.Radius.XS))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    Modifier.fillMaxWidth((amount / total).toFloat().coerceIn(0f, 1f))
                        .fillMaxHeight().background(color)
                )
            }
            Text(
                "${percentage.roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(FleetTokens.Height.StepCircle)
            )
            Text(
                formatCurrency(amount),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Vehicle Insights
// ═══════════════════════════════════════════════════════════════════

@Composable
internal fun VehicleInsightsSection(
    topPerformer: VehiclePerformer?,
    lossMakingVehicles: List<VehiclePerformer>,
    onViewDetails: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
        topPerformer?.let {
            Box(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(FleetTokens.Radius.L))
                    .background(ReportsColors.ProfitGreen.copy(alpha = 0.08f))
                    .padding(FleetTokens.Spacing.M)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_trophy),
                            contentDescription = null,
                            tint = ReportsColors.ProfitGreen,
                            modifier = Modifier.size(FleetTokens.IconSize.M)
                        )
                        Column {
                            Text(
                                stringResource(Res.string.reports_top_performer),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                it.registrationNumber ?: "N/A",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        "+${formatCurrency(it.profit)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ReportsColors.ProfitGreen
                    )
                }
            }
        }
        if (lossMakingVehicles.isNotEmpty()) {
            Box(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(FleetTokens.Radius.L))
                    .background(ReportsColors.LossRed.copy(alpha = 0.06f))
                    .padding(FleetTokens.Spacing.M)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_warning),
                            contentDescription = null,
                            tint = ReportsColors.LossRed,
                            modifier = Modifier.size(FleetTokens.IconSize.S)
                        )
                        Text(
                            stringResource(Res.string.reports_vehicles_need_attention, lossMakingVehicles.size),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ReportsColors.LossRed
                        )
                    }
                    Spacer(Modifier.height(FleetTokens.Spacing.S))
                    lossMakingVehicles.take(3).forEach { vehicle ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = FleetTokens.Spacing.XS),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                vehicle.registrationNumber ?: stringResource(Res.string.reports_unknown),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "-${formatCurrency(vehicle.loss)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ReportsColors.LossRed
                            )
                        }
                    }
                    if (lossMakingVehicles.size > 3) {
                        Spacer(Modifier.height(FleetTokens.Spacing.XS))
                        TextButton(
                            onClick = onViewDetails,
                            Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(stringResource(Res.string.reports_view_all_vehicles, lossMakingVehicles.size))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DocumentCostsNotice() {
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(FleetTokens.Radius.M))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                FleetTokens.Height.Divider,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(FleetTokens.Radius.M)
            )
            .padding(FleetTokens.Spacing.M)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_info),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(FleetTokens.IconSize.S)
            )
            Text(
                stringResource(Res.string.reports_pl_note_documents),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Detailed Reports Grid
// ═══════════════════════════════════════════════════════════════════

@Composable
internal fun DetailedReportsSection(
    onVehiclePLClick: () -> Unit,
    onTripPLClick: () -> Unit,
    onConsolidatedClick: () -> Unit,
    onCustomerPLClick: () -> Unit,
    onCombinedReportClick: () -> Unit,
    onMaintenanceCostClick: () -> Unit,
    onTripCostClick: () -> Unit,
    onDriverCostClick: () -> Unit,
    onCostAnalysisClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
        Text(
            stringResource(Res.string.reports_section_pl_reports),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
            ReportCard(Modifier.weight(1f), Res.drawable.ic_truck, stringResource(Res.string.reports_card_vehicle_pl), stringResource(Res.string.reports_card_subtitle_by_vehicle), ReportsColors.InfoBlue, onVehiclePLClick)
            ReportCard(Modifier.weight(1f), Res.drawable.ic_trip, stringResource(Res.string.reports_card_trip_pl), stringResource(Res.string.reports_card_subtitle_by_trip), ReportsColors.ProfitGreen, onTripPLClick)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
            ReportCard(Modifier.weight(1f), Res.drawable.ic_dashboard, stringResource(Res.string.reports_card_fleet_pl), stringResource(Res.string.reports_card_subtitle_consolidated), ReportsColors.Purple, onConsolidatedClick)
            ReportCard(Modifier.weight(1f), Res.drawable.ic_refresh, stringResource(Res.string.reports_card_combined), stringResource(Res.string.reports_card_subtitle_combined), ReportsColors.Cyan, onCombinedReportClick)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
            ReportCard(
                Modifier.weight(1f),
                Res.drawable.ic_team,
                stringResource(Res.string.customer_pl_card_title),
                stringResource(Res.string.customer_pl_card_desc),
                ReportsColors.Teal,
                onCustomerPLClick
            )
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(FleetTokens.Spacing.XS))
        Text(
            stringResource(Res.string.reports_cost_analysis),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
            ReportCard(Modifier.weight(1f), Res.drawable.ic_settings, stringResource(Res.string.reports_card_maintenance), stringResource(Res.string.reports_card_maintenance_sub), ReportsColors.WarningAmber, onMaintenanceCostClick)
            ReportCard(Modifier.weight(1f), Res.drawable.ic_fuel, stringResource(Res.string.reports_card_trip_costs), stringResource(Res.string.reports_card_trip_costs_sub), ReportsColors.Pink, onTripCostClick)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
            ReportCard(Modifier.weight(1f), Res.drawable.ic_profile, stringResource(Res.string.reports_card_driver_costs), stringResource(Res.string.reports_card_driver_costs_sub), ReportsColors.Teal, onDriverCostClick)
            ReportCard(Modifier.weight(1f), Res.drawable.ic_cost, stringResource(Res.string.reports_card_all_costs), stringResource(Res.string.reports_card_all_costs_sub), ReportsColors.WarningAmber, onCostAnalysisClick)
        }
    }
}

@Composable
private fun ReportCard(
    modifier: Modifier, iconRes: DrawableResource, title: String,
    description: String, color: Color, onClick: () -> Unit
) {
    FleetSectionCard(
        modifier = modifier,
        onClick = onClick,
        containerColor = color.copy(alpha = 0.06f),
        border = null,
        elevation = FleetTokens.Elevation.None,
        contentPadding = FleetTokens.Spacing.M
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            // Circular icon badge: bespoke 36dp chip, no dedicated chip-size token.
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(FleetTokens.IconSize.S)
                )
            }
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Utility functions
// ═══════════════════════════════════════════════════════════════════

internal fun formatCostType(costType: String) =
    costType.replace("_", " ").split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

