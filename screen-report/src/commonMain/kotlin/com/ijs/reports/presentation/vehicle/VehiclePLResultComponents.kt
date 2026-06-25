package com.ijs.reports.presentation.vehicle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetSectionHeader
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.reports.domain.entity.CostBreakdownItem
import com.ijs.reports.domain.entity.VehicleProfitLoss
import com.ijs.reports.presentation.RecentReport
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Fixed width for the horizontally-scrolling recent-report cards. Not part of the
 * [FleetTokens] spacing/icon scale (it is a bespoke card dimension), so it is kept
 * as a single named constant rather than a raw literal at the call site.
 */
private val RecentReportCardWidth: Dp = 140.dp

@Composable
internal fun ResultHeaderCard(
    vehicleNumber: String,
    vehicleMakeModel: String,
    period: String,
    onNewReport: () -> Unit
) {
    FleetSectionCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        border = null,
        elevation = FleetTokens.Elevation.None
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_truck),
                    contentDescription = null,
                    modifier = Modifier.size(FleetTokens.IconSize.Default),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Column {
                    Text(
                        text = vehicleNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$vehicleMakeModel • ${period.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }

            FleetButton(
                text = stringResource(Res.string.reports_new_report),
                onClick = onNewReport,
                variant = ButtonVariant.GHOST,
                size = ButtonSize.SMALL
            )
        }
    }
}

// ============================================================================
// P&L Result KPI Card
// ============================================================================


@Composable
internal fun PLResultKPICard(result: VehicleProfitLoss) {
    val isProfit = result.netProfit >= 0
    val profitColor = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
    val lossColor = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
    val expenseColor = com.indusjs.uicomponents.theme.FleetStatusColors.ExpenseAmber

    Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
        FleetSectionCard(
            containerColor = if (isProfit) profitColor.copy(alpha = 0.1f)
                            else lossColor.copy(alpha = 0.1f),
            border = null,
            contentPadding = FleetTokens.Spacing.XL
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                ) {
                    Text(
                        text = if (isProfit) stringResource(Res.string.reports_net_profit_label) else stringResource(Res.string.reports_net_loss_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(FleetTokens.Radius.S),
                        color = if (isProfit) profitColor.copy(alpha = 0.2f)
                                else lossColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = stringResource(Res.string.reports_trips_count, result.totalTrips),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isProfit) profitColor else lossColor,
                            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XXS)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                Text(
                    text = formatCurrency(kotlin.math.abs(result.netProfit)),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isProfit) profitColor else lossColor
                )
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
                Text(
                    text = stringResource(Res.string.reports_margin_value, formatPercentage(result.profitMargin)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            FleetMetricTile(
                value = formatCurrency(result.totalRevenue),
                label = stringResource(Res.string.reports_revenue),
                iconRes = Res.drawable.ic_trending_up,
                accent = profitColor,
                valueColor = profitColor,
                centered = true,
                modifier = Modifier.weight(1f)
            )

            FleetMetricTile(
                value = formatCurrency(result.totalExpenses),
                label = stringResource(Res.string.reports_expenses),
                iconRes = Res.drawable.ic_trending_down,
                accent = expenseColor,
                valueColor = expenseColor,
                centered = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ============================================================================
// P&L Cost Breakdown Card
// ============================================================================


@Composable
internal fun PLCostBreakdownCard(costs: List<CostBreakdownItem>) {
    var expanded by remember { mutableStateOf(false) }

    FleetSectionCard(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        border = null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            FleetSectionHeader(
                title = stringResource(Res.string.reports_cost_breakdown),
                actionLabel = if (expanded) stringResource(Res.string.reports_hide) else stringResource(Res.string.reports_show),
                onActionClick = { expanded = !expanded }
            )

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = FleetTokens.Spacing.M),
                    verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                ) {
                    costs.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.costType.replace("_", " ")
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
                                if (item.percentage > 0) {
                                    Text(
                                        text = "${item.percentage.toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = formatCurrency(item.amount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// Recent Report Card
// ============================================================================


@Composable
internal fun RecentReportCard(
    report: RecentReport,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(RecentReportCardWidth)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(FleetTokens.Radius.ML),
        color = if (report.isProfit)
            com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen.copy(alpha = 0.1f)
        else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(FleetTokens.Spacing.M),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
        ) {
            Text(
                text = report.vehicleNumber,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = report.vehicleMakeModel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
            Text(
                text = formatCurrency(kotlin.math.abs(report.profitLoss)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (report.isProfit) com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
            )
            Text(
                text = report.period.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================================
// Multi Vehicle Summary Card
// ============================================================================

