package com.ijs.customer.presentation.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.LoadingContent
import com.ijs.customer.domain.entity.*
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.ijs.customer.presentation.detail.CustomerDetailContract.Intent
import com.ijs.customer.presentation.detail.CustomerDetailContract.ReportType
import com.ijs.customer.presentation.detail.CustomerDetailContract.State
import com.ijs.customer.presentation.localizedDisplayName

/**
 * Financials Tab Content.
 * Shows P&L report with period selection and export capability.
 * Only visible to Owner and General Manager.
 */
@Composable
fun FinancialsTabContent(
    state: State,
    onIntent: (Intent) -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Period Selection
        PeriodSelector(
            selectedPeriod = state.financialsPeriod,
            startDate = state.financialsStartDate,
            endDate = state.financialsEndDate,
            onPeriodSelected = { onIntent(Intent.SetFinancialsPeriod(it)) },
            onCustomDateClick = { onIntent(Intent.ShowDateRangePicker) },
            onExportPdf = onExportPdf,
            isExporting = state.isExporting
        )

        when {
            state.isLoadingFinancials -> {
                LoadingContent()
            }
            state.financialsError != null -> {
                ErrorContent(
                    error = state.financialsError.resolve(),
                    onRetry = { onIntent(Intent.RefreshFinancials) }
                )
            }
            state.financialReport == null -> {
                EmptyContent(
                    title = stringResource(Res.string.customer_financials_empty_title),
                    message = stringResource(Res.string.customer_financials_empty_message_period),
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
    onCustomDateClick: () -> Unit,
    onExportPdf: () -> Unit,
    isExporting: Boolean
) {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(FinancialPeriod.entries.filter { it != FinancialPeriod.CUSTOM }) { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodSelected(period) },
                        label = { Text(period.localizedDisplayName()) },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedPeriod == FinancialPeriod.CUSTOM,
                        onClick = onCustomDateClick,
                        label = { Text(stringResource(Res.string.period_custom)) },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
            IconButton(
                onClick = onExportPdf,
                enabled = !isExporting,
                modifier = Modifier.size(40.dp)
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        painter = painterResource(Res.drawable.ic_download),
                        contentDescription = stringResource(Res.string.cd_export_pdf),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (startDate.isNotBlank() && endDate.isNotBlank()) {
            Text(
                text = stringResource(Res.string.customer_financials_date_range, startDate, endDate),
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
                text = stringResource(Res.string.dashboard_financial_overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FinancialMetric(
                    label = stringResource(Res.string.reports_revenue),
                    value = report.totalRevenueDisplay,
                    color = MaterialTheme.colorScheme.primary
                )
                FinancialMetric(
                    label = stringResource(Res.string.reports_expenses),
                    value = report.totalCostsDisplay,
                    color = MaterialTheme.colorScheme.error
                )
                // Additive: driver costs (net of deductions) are now part of the
                // backend response and are subtracted to reach net profit.
                FinancialMetric(
                    label = stringResource(Res.string.reports_card_driver_costs),
                    value = report.totalDriverCostsDisplay,
                    color = MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FinancialMetric(
                    label = stringResource(Res.string.reports_net_profit_label),
                    value = report.netProfitDisplay,
                    color = if (report.isProfitable) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    isLarge = true
                )
                FinancialMetric(
                    label = stringResource(Res.string.reports_margin),
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
    FleetMetricTile(
        value = value,
        label = label,
        valueColor = color,
        accent = color,
        // Emphasised metrics (Net Profit / Margin) keep the tinted box for visual hierarchy.
        showBackground = isLarge,
        centered = true
    )
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
            Text(stringResource(Res.string.action_exporting))
        } else {
            Text(stringResource(Res.string.action_export_report_pdf), fontWeight = FontWeight.SemiBold)
        }
    }
}

