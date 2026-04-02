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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indusjs.fleet.core.util.formatCurrency
import com.ijs.reports.domain.entity.PLSummary
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.roundToInt

// ═══════════════════════════════════════════════════════════════════
// Financial Hero — compact profit/loss + revenue/expenses
// ═══════════════════════════════════════════════════════════════════

@Composable
internal fun FinancialHeroCard(summary: PLSummary) {
    val isProfit = summary.isProfitable
    val profitStatus = ProfitStatus.fromMargin(summary.profitMarginPercentage)
    val statusColor = Color(profitStatus.colorHex)
    val total = summary.totalRevenue + summary.totalExpenses
    val expenseRatio = if (total > 0) (summary.totalExpenses / total).toFloat() else 0.5f

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            // Row 1: Revenue vs Expenses side by side
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Revenue
                Box(
                    Modifier.weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ReportsColors.ProfitGreen.copy(alpha = 0.08f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            "Revenue",
                            style = MaterialTheme.typography.labelSmall,
                            color = ReportsColors.ProfitGreen
                        )
                        Text(
                            formatCurrency(summary.totalRevenue),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ReportsColors.ProfitGreen
                        )
                    }
                }
                // Expenses
                Box(
                    Modifier.weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ReportsColors.LossRed.copy(alpha = 0.08f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            stringResource(Res.string.reports_expenses),
                            style = MaterialTheme.typography.labelSmall,
                            color = ReportsColors.LossRed
                        )
                        Text(
                            formatCurrency(summary.totalExpenses),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ReportsColors.LossRed
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

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
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier.fillMaxWidth().height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(ReportsColors.ProfitGreen.copy(alpha = 0.15f))
                ) {
                    Box(
                        Modifier.fillMaxWidth(expenseRatio.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (expenseRatio > 0.85f) ReportsColors.LossRed.copy(alpha = 0.7f)
                                else if (expenseRatio > 0.7f) ReportsColors.WarningAmber.copy(alpha = 0.7f)
                                else ReportsColors.ProfitGreen.copy(alpha = 0.5f)
                            )
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Row 3: Net profit/loss result
            Box(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(statusColor.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(profitStatus.icon, fontSize = 20.sp)
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
                            "${if (isProfit) "+" else "-"}${formatCurrency(abs(summary.grossProfit))}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Text(
                            stringResource(Res.string.reports_margin_percent, summary.profitMarginPercentage.roundToInt()),
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
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            // Header
            Text(
                stringResource(Res.string.reports_fleet_snapshot),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))

            // Key metrics row — 3 compact items
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(10.dp))

            // Fleet performance row
            PerformanceRow(
                icon = "🚛",
                title = "Fleet",
                total = summary.totalVehicles,
                profitable = summary.profitableVehicles,
                loss = summary.lossMakingVehicles
            )

            Spacer(Modifier.height(8.dp))

            // Trip performance row
            PerformanceRow(
                icon = "🛣️",
                title = "Trips",
                total = summary.totalTrips,
                profitable = summary.profitableTrips,
                loss = summary.lossMakingTrips
            )
        }
    }
}

@Composable
private fun SnapshotMetric(modifier: Modifier, value: String, label: String, color: Color) {
    Box(
        modifier.clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PerformanceRow(
    icon: String,
    title: String,
    total: Int,
    profitable: Int,
    loss: Int
) {
    val healthPct = if (total > 0) (profitable * 100 / total) else 0
    val healthColor = when {
        healthPct >= 70 -> ReportsColors.ProfitGreen
        healthPct >= 40 -> ReportsColors.WarningAmber
        else -> ReportsColors.LossRed
    }
    val barFraction = if (total > 0) profitable.toFloat() / total else 0f

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(icon, fontSize = 16.sp)
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
                    "$profitable profit · $loss loss",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Box(
                Modifier.fillMaxWidth().height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Box(
                    Modifier.fillMaxWidth(barFraction.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(healthColor)
                )
            }
        }
        // Health badge
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = healthColor.copy(alpha = 0.12f)
        ) {
            Text(
                "${healthPct}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = healthColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

