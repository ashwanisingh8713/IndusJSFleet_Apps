package com.indusjs.fleet.presentation.customers.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.ui.EmptyContent
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.domain.entity.customer.*
import com.indusjs.fleet.presentation.customers.detail.CustomerDetailContract.Intent
import com.indusjs.fleet.presentation.customers.detail.CustomerDetailContract.ReportType
import com.indusjs.fleet.presentation.customers.detail.CustomerDetailContract.State

/**
 * Financials Tab Content.
 * Shows P&L report with period selection and export capability.
 * Only visible to Owner and General Manager.
 */
@Composable
fun FinancialsTabContent(
    state: State,
    onIntent: (Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Period Selection
        PeriodSelector(
            selectedPeriod = state.financialsPeriod,
            startDate = state.financialsStartDate,
            endDate = state.financialsEndDate,
            onPeriodSelected = { onIntent(Intent.SetFinancialsPeriod(it)) },
            onCustomDateClick = { onIntent(Intent.ShowDateRangePicker) }
        )

        when {
            state.isLoadingFinancials -> {
                LoadingContent()
            }
            state.financialsError != null -> {
                ErrorContent(
                    error = state.financialsError,
                    onRetry = { onIntent(Intent.RefreshFinancials) }
                )
            }
            state.financialReport == null -> {
                EmptyContent(
                    title = "No financial data",
                    message = "No financial data available for this period",
                    icon = "📊"
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Main P&L Card
                    item {
                        ProfitLossCard(state.financialReport!!)
                    }

                    // Trip Summary
                    state.financialReport!!.tripSummary?.let { summary ->
                        item {
                            TripSummaryCard(summary)
                        }
                    }

                    // Payment Status Card
                    item {
                        PaymentStatusCard(
                            received = state.financialReport!!.paymentReceived,
                            pending = state.financialReport!!.paymentPending
                        )
                    }

                    // Period Breakdown
                    if (state.financialReport!!.periodBreakdown.isNotEmpty()) {
                        item {
                            PeriodBreakdownCard(state.financialReport!!.periodBreakdown)
                        }
                    }

                    // Top Vehicles
                    if (state.financialReport!!.topVehicles.isNotEmpty()) {
                        item {
                            TopVehiclesCard(state.financialReport!!.topVehicles)
                        }
                    }

                    // Export Button
                    item {
                        ExportButton(
                            isExporting = state.isExportingPdf,
                            onClick = { onIntent(Intent.ExportPdf(ReportType.FINANCIALS)) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: FinancialPeriod,
    startDate: String,
    endDate: String,
    onPeriodSelected: (FinancialPeriod) -> Unit,
    onCustomDateClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FinancialPeriod.entries.filter { it != FinancialPeriod.CUSTOM }) { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { onPeriodSelected(period) },
                    label = { Text(period.displayName) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
            item {
                FilterChip(
                    selected = selectedPeriod == FinancialPeriod.CUSTOM,
                    onClick = onCustomDateClick,
                    label = { Text("Custom") },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        if (startDate.isNotBlank() && endDate.isNotBlank()) {
            Text(
                text = "📅 $startDate to $endDate",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfitLossCard(report: CustomerFinancialReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (report.isProfitable) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📊 Financial Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FinancialMetric(
                    label = "Revenue",
                    value = report.totalRevenueDisplay,
                    color = MaterialTheme.colorScheme.primary
                )
                FinancialMetric(
                    label = "Expenses",
                    value = report.totalCostsDisplay,
                    color = MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FinancialMetric(
                    label = "Net Profit",
                    value = report.netProfitDisplay,
                    color = if (report.isProfitable) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    isLarge = true
                )
                FinancialMetric(
                    label = "Margin",
                    value = report.profitMarginDisplay,
                    color = MaterialTheme.colorScheme.onSurface,
                    isLarge = true
                )
            }
        }
    }
}

@Composable
private fun FinancialMetric(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    isLarge: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = if (isLarge) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TripSummaryCard(summary: FinancialTripSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🚛 Trip Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = summary.totalTrips.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = summary.completedTrips.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = summary.averageTripValueDisplay,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Avg Value",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentStatusCard(received: Double, pending: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "💰 Payment Status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "₹${formatAmount(received)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Received",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "₹${formatAmount(pending)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (pending > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pending",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodBreakdownCard(breakdown: List<PeriodBreakdown>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📈 Period Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            breakdown.forEach { period ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = period.period,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = period.revenueDisplay,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = period.profitDisplay,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (period.isProfitable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (period != breakdown.last()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun TopVehiclesCard(vehicles: List<TopVehicle>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🚛 Top Performing Vehicles",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            vehicles.forEachIndexed { index, vehicle ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (index) {
                                        0 -> MaterialTheme.colorScheme.primaryContainer
                                        1 -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = vehicle.vehicleRegistration,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${vehicle.trips} trips",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = vehicle.revenueDisplay,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (vehicle != vehicles.last()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun ExportButton(
    isExporting: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = !isExporting,
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isExporting) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Exporting...")
        } else {
            Text("📥 Export Report as PDF", fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun formatAmount(amount: Double): String {
    val absAmount = kotlin.math.abs(amount)
    val prefix = if (amount < 0) "-" else ""
    return prefix + when {
        absAmount >= 10000000 -> "${(absAmount / 10000000).toString().take(4)}Cr"
        absAmount >= 100000 -> "${(absAmount / 100000).toString().take(4)}L"
        absAmount >= 1000 -> "${(absAmount / 1000).toString().take(4)}K"
        else -> absAmount.toInt().toString()
    }
}
