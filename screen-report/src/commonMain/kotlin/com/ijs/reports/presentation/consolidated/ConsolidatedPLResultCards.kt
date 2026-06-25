 package com.ijs.reports.presentation.consolidated

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.core.util.formatPercentage
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.reports.domain.entity.ConsolidatedPL
import com.ijs.reports.domain.entity.PeriodBreakdown
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Result card composables for Consolidated P&L Screen.
 * Extracted from ConsolidatedPLScreen.kt for 500-line compliance.
 */

@Composable
internal fun ConsolidatedSummaryCard(report: ConsolidatedPL) {
    FleetSectionCard(
        containerColor = if (report.isProfitable)
            FleetStatusColors.ProfitGreen.copy(alpha = 0.1f)
        else
            FleetStatusColors.LossRed.copy(alpha = 0.1f),
        border = null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(Res.string.reports_consolidated_summary_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryColumn(label = stringResource(Res.string.reports_total_revenue), value = formatCurrency(report.totalRevenue))
                SummaryColumn(
                    label = stringResource(Res.string.reports_total_expenses),
                    value = formatCurrency(report.totalExpenses),
                    alignment = Alignment.End
                )
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        Res.string.reports_net_label,
                        if (report.isProfitable) stringResource(Res.string.reports_word_profit_caps)
                        else stringResource(Res.string.reports_word_loss_caps)
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (report.isProfitable) "▲" else "▼"} ${formatCurrency(kotlin.math.abs(report.netProfit))}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (report.isProfitable) FleetStatusColors.ProfitGreen else FleetStatusColors.LossRed
                    )
                    if (report.profitMargin > 0) {
                        Text(
                            text = stringResource(Res.string.reports_label_margin_pct, formatPercentage(report.profitMargin)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(value = "${report.totalVehicles}", label = stringResource(Res.string.reports_stat_vehicles))
                StatColumn(value = "${report.totalTrips}", label = stringResource(Res.string.reports_label_total_trips))
                StatColumn(value = "${report.completedTrips}", label = stringResource(Res.string.reports_label_completed_trips))
            }
        }
    }
}

@Composable
internal fun SummaryColumn(
    label: String,
    value: String,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(horizontalAlignment = alignment) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
internal fun StatColumn(value: String, label: String) {
    FleetMetricTile(
        value = value,
        label = label,
        showBackground = false,
        centered = true
    )
}

@Composable
internal fun PeriodBreakdownCard(period: PeriodBreakdown) {
    FleetSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = period.label ?: period.period,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(Res.string.reports_trips_count, period.tripCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (period.isProfitable) "▲" else "▼"} ${formatCurrency(kotlin.math.abs(period.profit))}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (period.isProfitable) FleetStatusColors.ProfitGreen else FleetStatusColors.LossRed
                )
                Text(
                    text = stringResource(Res.string.reports_label_rev_short, formatCurrency(period.revenue)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun VehicleSummaryCard(
    vehicleNumber: String,
    tripCount: Int,
    revenue: Double,
    expenses: Double,
    profit: Double,
    isProfitable: Boolean
) {
    FleetSectionCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = vehicleNumber,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(Res.string.reports_trips_count, tripCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${if (isProfitable) "▲" else "▼"} ${formatCurrency(kotlin.math.abs(profit))}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isProfitable) FleetStatusColors.ProfitGreen else FleetStatusColors.LossRed
                )
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.reports_label_revenue_amount, formatCurrency(revenue)),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = stringResource(Res.string.reports_label_expenses_amount, formatCurrency(expenses)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

