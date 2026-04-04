 package com.ijs.reports.presentation.consolidated

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.core.util.formatPercentage
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.ijs.reports.domain.entity.ConsolidatedPL
import com.ijs.reports.domain.entity.PeriodBreakdown

/**
 * Result card composables for Consolidated P&L Screen.
 * Extracted from ConsolidatedPLScreen.kt for 500-line compliance.
 */

@Composable
internal fun ConsolidatedSummaryCard(report: ConsolidatedPL) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (report.isProfitable)
                FleetStatusColors.ProfitGreen.copy(alpha = 0.1f)
            else
                FleetStatusColors.LossRed.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "CONSOLIDATED SUMMARY",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryColumn(label = "Total Revenue", value = formatCurrency(report.totalRevenue))
                SummaryColumn(
                    label = "Total Expenses",
                    value = formatCurrency(report.totalExpenses),
                    alignment = Alignment.End
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NET ${if (report.isProfitable) "PROFIT" else "LOSS"}",
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
                            text = "Margin: ${formatPercentage(report.profitMargin)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(value = "${report.totalVehicles}", label = "Vehicles")
                StatColumn(value = "${report.totalTrips}", label = "Total Trips")
                StatColumn(value = "${report.completedTrips}", label = "Completed")
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun PeriodBreakdownCard(period: PeriodBreakdown) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                    text = "${period.tripCount} trips",
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
                    text = "Rev: ${formatCurrency(period.revenue)}",
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
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
                        text = "$tripCount trips",
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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Revenue: ${formatCurrency(revenue)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Expenses: ${formatCurrency(expenses)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

