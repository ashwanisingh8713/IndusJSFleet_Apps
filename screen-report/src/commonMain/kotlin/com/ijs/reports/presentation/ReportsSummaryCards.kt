package com.ijs.reports.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.indusjs.fleet.core.util.formatCurrencyFull
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.reports.domain.entity.PLSummary
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.roundToInt

// ═══════════════════════════════════════════════════════════════════
// Financial Hero — compact profit/loss + revenue/expenses
// ═══════════════════════════════════════════════════════════════════

/**
 * Maps a [ProfitStatus] to a semantic tint from the shared status palette
 * ([ReportsColors] → [com.indusjs.uicomponents.theme.FleetStatusColors]),
 * which is verified light/dark safe — instead of the enum's fixed Material hex.
 */
private fun ProfitStatus.statusColor(): Color = when (this) {
    ProfitStatus.HIGHLY_PROFITABLE, ProfitStatus.PROFITABLE -> ReportsColors.ProfitGreen
    ProfitStatus.BREAK_EVEN -> ReportsColors.WarningAmber
    ProfitStatus.LOSS, ProfitStatus.SEVERE_LOSS -> ReportsColors.LossRed
}

@Composable
internal fun FinancialHeroCard(summary: PLSummary) {
    val isProfit = summary.netProfit >= 0
    val profitStatus = ProfitStatus.fromMargin(summary.profitMarginPercentage)
    // Semantic status tint from the shared FleetStatusColors palette (light/dark safe)
    // rather than the fixed enum hex, so the hero tile follows the design-system colors.
    val statusColor = profitStatus.statusColor()
    // Expense ratio = expenses as a fraction of REVENUE (the conventional reading), not of
    // total cash flow (revenue+expenses). Capped at 1.0 so the bar/label stay ≤100% in a loss.
    val expenseRatio = if (summary.totalRevenue > 0)
        (summary.totalExpenses / summary.totalRevenue).toFloat().coerceIn(0f, 1f)
    else 0f

    FleetSectionCard {
        Column {
            // Row 1: Revenue vs Expenses side by side
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
                // Revenue
                Box(
                    Modifier.weight(1f)
                        .clip(RoundedCornerShape(FleetTokens.Radius.L))
                        .background(ReportsColors.ProfitGreen.copy(alpha = 0.08f))
                        .padding(FleetTokens.Spacing.M)
                ) {
                    Column {
                        Text(
                            stringResource(Res.string.reports_revenue),
                            style = MaterialTheme.typography.labelSmall,
                            color = ReportsColors.ProfitGreen
                        )
                        Text(
                            formatCurrencyFull(summary.totalRevenue),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ReportsColors.ProfitGreen
                        )
                    }
                }
                // Expenses
                Box(
                    Modifier.weight(1f)
                        .clip(RoundedCornerShape(FleetTokens.Radius.L))
                        .background(ReportsColors.LossRed.copy(alpha = 0.08f))
                        .padding(FleetTokens.Spacing.M)
                ) {
                    Column {
                        Text(
                            stringResource(Res.string.reports_expenses),
                            style = MaterialTheme.typography.labelSmall,
                            color = ReportsColors.LossRed
                        )
                        Text(
                            formatCurrencyFull(summary.totalExpenses),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ReportsColors.LossRed
                        )
                    }
                }
            }

            Spacer(Modifier.height(FleetTokens.Spacing.M))

            // Row 2: Expense ratio bar
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(Res.string.reports_expense_ratio),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${(expenseRatio * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (expenseRatio > 0.85f) ReportsColors.LossRed
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(FleetTokens.Spacing.XS))
                Box(
                    Modifier.fillMaxWidth().height(FleetTokens.Height.ProgressBar)
                        .clip(RoundedCornerShape(FleetTokens.Radius.M))
                        .background(ReportsColors.ProfitGreen.copy(alpha = 0.15f))
                ) {
                    Box(
                        Modifier.fillMaxWidth(expenseRatio.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(FleetTokens.Radius.M))
                            .background(
                                if (expenseRatio > 0.85f) ReportsColors.LossRed.copy(alpha = 0.7f)
                                else if (expenseRatio > 0.7f) ReportsColors.WarningAmber.copy(alpha = 0.7f)
                                else ReportsColors.ProfitGreen.copy(alpha = 0.5f)
                            )
                    )
                }
            }

            Spacer(Modifier.height(FleetTokens.Spacing.M))

            // Row 3: Net profit/loss result
            Box(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(FleetTokens.Radius.L))
                    .background(statusColor.copy(alpha = 0.08f))
                    .padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.M)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                    ) {
                        Icon(
                            painter = painterResource(profitStatus.icon),
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(FleetTokens.IconSize.M)
                        )
                        Column {
                            Text(
                                if (isProfit) {
                                    stringResource(Res.string.reports_net_profit_label)
                                } else {
                                    stringResource(Res.string.reports_net_loss_label)
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor
                            )
                            Text(
                                profitStatus.localizedLabel(),
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${if (isProfit) "+" else "-"}${formatCurrencyFull(abs(summary.netProfit))}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Text(
                            stringResource(Res.string.reports_margin_percent, "${summary.profitMarginPercentage.roundToInt()}%"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Fleet Snapshot — merged Key Metrics + Performance in one card
// ═══════════════════════════════════════════════════════════════════

@Composable
internal fun FleetSnapshotCard(summary: PLSummary) {
    FleetTitledSectionCard(title = stringResource(Res.string.reports_fleet_snapshot)) {
        Column {
            // Key metrics row — 3 compact items
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
            ) {
                SnapshotMetric(
                    Modifier.weight(1f), "${summary.completedTrips}",
                    stringResource(Res.string.org_stats_trips), ReportsColors.InfoBlue
                )
                SnapshotMetric(
                    Modifier.weight(1f), "${summary.activeVehicles}",
                    stringResource(Res.string.org_stats_vehicles), ReportsColors.Purple
                )
                SnapshotMetric(
                    Modifier.weight(1f),
                    "${summary.profitMarginPercentage.roundToInt()}%",
                    stringResource(Res.string.reports_margin),
                    if (summary.isProfitable) ReportsColors.ProfitGreen else ReportsColors.LossRed
                )
            }

            Spacer(Modifier.height(FleetTokens.Spacing.M))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(FleetTokens.Spacing.M))

            // Fleet performance row
            PerformanceRow(
                iconRes = Res.drawable.ic_truck,
                title = stringResource(Res.string.reports_perf_row_fleet),
                total = summary.totalVehicles,
                profitable = summary.profitableVehicles,
                loss = summary.lossMakingVehicles
            )

            Spacer(Modifier.height(FleetTokens.Spacing.S))

            // Trip performance row
            PerformanceRow(
                iconRes = Res.drawable.ic_trip,
                title = stringResource(Res.string.reports_perf_row_trips),
                total = summary.totalTrips,
                profitable = summary.profitableTrips,
                loss = summary.lossMakingTrips
            )
        }
    }
}

@Composable
private fun SnapshotMetric(modifier: Modifier, value: String, label: String, color: Color) {
    FleetMetricTile(
        value = value,
        label = label,
        modifier = modifier,
        accent = color,
        valueColor = color,
        centered = true
    )
}

@Composable
private fun PerformanceRow(
    iconRes: DrawableResource,
    title: String,
    total: Int,
    profitable: Int,
    loss: Int
) {
    // Denominator must reflect the counts actually shown: if `total` is stale/0 (e.g. a
    // Trips total that didn't populate) fall back to profitable+loss, so the bar can't
    // read 0% while the label says "1 profit, 0 loss". Use the larger of the two so a real
    // total (which may exceed profitable+loss when some entities are break-even) still wins.
    val denom = maxOf(total, profitable + loss)
    val healthPct = if (denom > 0) (profitable * 100 / denom) else 0
    val healthColor = when {
        healthPct >= 70 -> ReportsColors.ProfitGreen
        healthPct >= 40 -> ReportsColors.WarningAmber
        else -> ReportsColors.LossRed
    }
    val barFraction = if (denom > 0) profitable.toFloat() / denom else 0f

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(FleetTokens.IconSize.S)
        )
        Column(Modifier.weight(1f)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(Res.string.reports_perf_profit_loss_counts, profitable, loss),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(FleetTokens.Spacing.XS))
            Box(
                Modifier.fillMaxWidth().height(FleetTokens.Height.ProgressBar)
                    .clip(RoundedCornerShape(FleetTokens.Radius.M))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Box(
                    Modifier.fillMaxWidth(barFraction.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(FleetTokens.Radius.M))
                        .background(healthColor)
                )
            }
        }
        // Health badge
        Surface(
            shape = RoundedCornerShape(FleetTokens.Radius.M),
            color = healthColor.copy(alpha = 0.12f)
        ) {
            Text(
                "${healthPct}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = healthColor,
                modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XXS)
            )
        }
    }
}

