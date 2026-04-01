package com.ijs.reports.presentation.vehicle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.fleet.core.util.formatCurrency
import com.ijs.reports.domain.entity.VehicleProfitLoss
import com.ijs.reports.presentation.PLStatusFilter
import com.ijs.reports.presentation.ReportChartType
import com.ijs.reports.presentation.ReportViewMode
import com.ijs.reports.presentation.VehiclePLSortOption
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FleetOverviewContent(
    state: VehiclePLContract.State,
    onPeriodChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onViewModeChange: (ReportViewMode) -> Unit,
    onChartTypeChange: (ReportChartType) -> Unit,
    onSortChange: (VehiclePLSortOption) -> Unit,
    onFilterChange: (PLStatusFilter) -> Unit,
    onShowVehicleFilter: () -> Unit,
    onSelectSingleVehicle: () -> Unit,
    onExport: () -> Unit,
    onRetry: () -> Unit
) {
    // Check if we have data to display
    val hasData = state.multiResults.isNotEmpty()
    val hasError = state.error != null && !hasData

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Period Selection - always show
        item {
            PeriodSelectionRow(
                selectedPeriod = state.period,
                useCustomDateRange = state.useCustomDateRange,
                startDate = state.startDate,
                endDate = state.endDate,
                onPeriodChange = onPeriodChange,
                onStartDateChange = onStartDateChange,
                onEndDateChange = onEndDateChange
            )
        }

        // Error state - show inline error card with retry
        if (hasError) {
            item {
                FleetErrorCard(
                    error = state.error!!,
                    vehicleCount = state.vehicles.size,
                    onRetry = onRetry,
                    onSelectSingleVehicle = onSelectSingleVehicle
                )
            }
        } else if (hasData) {
            // Fleet Summary KPI Card
            item {
                FleetSummaryKPICard(
                    totalVehicles = state.multiResults.size,
                    profitableCount = state.totalProfitableCount,
                    lossMakingCount = state.totalLossMakingCount,
                    totalRevenue = state.totalRevenue,
                    totalExpenses = state.totalExpenses,
                    netProfit = state.totalNetProfit,
                    profitMargin = state.fleetProfitMargin,
                    avgProfitPerVehicle = state.averageProfitPerVehicle
                )
            }

            // Top/Worst Performers
            if (state.multiResults.size > 1) {
                item {
                    PerformersCard(
                        topPerformer = state.topPerformer,
                        worstPerformer = state.worstPerformer
                    )
                }
            }

            // View Mode Toggle & Sort/Filter
            item {
                ViewModeAndFilterSection(
                    viewMode = state.viewMode,
                    sortOption = state.sortOption,
                    plStatusFilter = state.plStatusFilter,
                    resultCount = state.sortedFilteredResults.size,
                    totalCount = state.multiResults.size,
                    onViewModeChange = onViewModeChange,
                    onSortChange = onSortChange,
                    onFilterChange = onFilterChange,
                    onShowVehicleFilter = onShowVehicleFilter
                )
            }

            // Content based on view mode
            when (state.viewMode) {
                ReportViewMode.CHART -> {
                    item {
                        ChartViewContent(
                            results = state.sortedFilteredResults.take(10),
                            chartType = state.chartType,
                            onChartTypeChange = onChartTypeChange
                        )
                    }
                }
                ReportViewMode.SUMMARY -> {
                    item {
                        SummaryGridContent(results = state.sortedFilteredResults)
                    }
                }
                ReportViewMode.LIST -> {
                    items(state.sortedFilteredResults, key = { it.vehicleId }) { result ->
                        VehiclePLResultCard(result)
                    }
                }
            }

            // Action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onSelectSingleVehicle,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🚛 Single Vehicle")
                    }
                    Button(
                        onClick = onExport,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("📄 Export Report")
                    }
                }
            }
        } else {
            // Empty state - no P&L data for selected period
            item {
                EmptyPLDataContent(
                    vehicleCount = state.vehicles.size,
                    period = state.currentPeriodLabel,
                    onSelectSingleVehicle = onSelectSingleVehicle
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ============================================================================
// Empty P&L Data Content - When vehicles exist but no P&L data for period
// ============================================================================


@Composable
internal fun EmptyPLDataContent(
    vehicleCount: Int,
    period: String,
    onSelectSingleVehicle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📊", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.vehicle_pl_no_data_available),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (period.isNotBlank())
                    "No trips or costs recorded for $period"
                else
                    "No trips or costs recorded for the selected period",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    Res.string.vehicle_pl_fleet_count,
                    vehicleCount,
                    if (vehicleCount != 1) "s" else ""
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.vehicle_pl_tips_title),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(Res.string.vehicle_pl_tips_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onSelectSingleVehicle,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("🚛 View Single Vehicle")
            }
        }
    }
}

// ============================================================================
// Fleet Error Card - Inline error display with retry option
// ============================================================================


@Composable
internal fun FleetErrorCard(
    error: String,
    vehicleCount: Int,
    onRetry: () -> Unit,
    onSelectSingleVehicle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⚠️", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.vehicle_pl_error_loading),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    Res.string.vehicle_pl_fleet_count,
                    vehicleCount,
                    if (vehicleCount != 1) "s" else ""
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSelectSingleVehicle,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🚛 Single Vehicle")
                }
                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🔄 Retry")
                }
            }
        }
    }
}

// ============================================================================
// Fleet Summary KPI Card
// ============================================================================


@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PeriodSelectionRow(
    selectedPeriod: String,
    useCustomDateRange: Boolean,
    startDate: String,
    endDate: String,
    onPeriodChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit
) {
    Column {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VehiclePLContract.PERIOD_OPTIONS.forEach { (value, label) ->
                FilterChip(
                    selected = selectedPeriod == value,
                    onClick = { onPeriodChange(value) },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    leadingIcon = if (selectedPeriod == value) {
                        { Text("✓", style = MaterialTheme.typography.labelSmall) }
                    } else null
                )
            }
        }

        // Custom date range using ijs-datetime-picker
        AnimatedVisibility(visible = useCustomDateRange) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FleetDateTimePicker(
                    date = startDate,
                    time = "",
                    onDateTimeChange = { newDate, _ -> onStartDateChange(newDate) },
                    label = "From Date",
                    mode = PickerMode.DATE_ONLY,
                    modifier = Modifier.fillMaxWidth()
                )
                FleetDateTimePicker(
                    date = endDate,
                    time = "",
                    onDateTimeChange = { newDate, _ -> onEndDateChange(newDate) },
                    label = "To Date",
                    mode = PickerMode.DATE_ONLY,
                    minDate = startDate.ifBlank { null },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ============================================================================
// View Mode and Filter Section
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)

@Composable
internal fun ViewModeAndFilterSection(
    viewMode: ReportViewMode,
    sortOption: VehiclePLSortOption,
    plStatusFilter: PLStatusFilter,
    resultCount: Int,
    totalCount: Int,
    onViewModeChange: (ReportViewMode) -> Unit,
    onSortChange: (VehiclePLSortOption) -> Unit,
    onFilterChange: (PLStatusFilter) -> Unit,
    onShowVehicleFilter: () -> Unit
) {
    Column {
        // View mode toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportViewMode.entries.forEach { mode ->
                    FilterChip(
                        selected = viewMode == mode,
                        onClick = { onViewModeChange(mode) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(mode.icon)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(mode.label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    )
                }
            }

            // Vehicle filter button
            FilledTonalIconButton(onClick = onShowVehicleFilter) {
                Text("🚛", style = MaterialTheme.typography.titleSmall)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // P&L Status filter & Sort
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PLStatusFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = plStatusFilter == filter,
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            // Result count
            Text(
                text = "$resultCount of $totalCount",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================================
// Chart View Content
// ============================================================================


