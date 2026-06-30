package com.ijs.reports.presentation.vehicle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetFilterChip
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetTab
import com.indusjs.uicomponents.components.FleetTabBar
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isExpanded
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.reports.domain.entity.VehicleProfitLoss
import com.ijs.reports.presentation.PLStatusFilter
import com.ijs.reports.presentation.ReportChartType
import com.ijs.reports.presentation.ReportViewMode
import com.ijs.reports.presentation.VehiclePLSortOption
import com.ijs.reports.presentation.localizedLabel
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

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isWide = rememberFleetBreakpoint().isExpanded
        val listModifier = if (isWide) {
            Modifier.fillMaxSize().widthIn(max = FleetTokens.Width.MaxContent)
        } else {
            Modifier.fillMaxSize()
        }

    LazyColumn(
        modifier = listModifier,
        contentPadding = PaddingValues(FleetTokens.Spacing.L),
        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
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
                    error = state.error!!.resolve(),
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
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                ) {
                    FleetButton(
                        text = stringResource(Res.string.reports_single_vehicle),
                        onClick = onSelectSingleVehicle,
                        variant = ButtonVariant.SECONDARY,
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_truck),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(FleetTokens.IconSize.S)
                            )
                        }
                    )
                    FleetButton(
                        text = stringResource(Res.string.reports_export_report_btn),
                        onClick = onExport,
                        variant = ButtonVariant.PRIMARY,
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_download),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(FleetTokens.IconSize.S)
                            )
                        }
                    )
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

        item { Spacer(modifier = Modifier.height(FleetTokens.Spacing.L)) }
    }
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
    FleetSectionCard(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        border = null,
        contentPadding = FleetTokens.Spacing.XXL
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_dashboard),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(FleetTokens.IconSize.XL)
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
            Text(
                text = stringResource(Res.string.vehicle_pl_no_data_available),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
            Text(
                text = if (period.isNotBlank())
                    stringResource(Res.string.reports_fleet_no_data_period, period)
                else
                    stringResource(Res.string.reports_fleet_no_data_range),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
            Text(
                text = stringResource(
                    Res.string.vehicle_pl_fleet_count,
                    vehicleCount,
                    if (vehicleCount != 1) "s" else ""
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))

            Column(
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M),
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

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))

            FleetButton(
                text = stringResource(Res.string.reports_view_single_vehicle),
                onClick = onSelectSingleVehicle,
                variant = ButtonVariant.SECONDARY,
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_truck),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(FleetTokens.IconSize.S)
                    )
                }
            )
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
    FleetSectionCard(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        border = null,
        elevation = FleetTokens.Elevation.None,
        contentPadding = FleetTokens.Spacing.XL
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_warning),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(FleetTokens.IconSize.XL)
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
            Text(
                text = stringResource(Res.string.vehicle_pl_error_loading),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
            Text(
                text = stringResource(
                    Res.string.vehicle_pl_fleet_count,
                    vehicleCount,
                    if (vehicleCount != 1) "s" else ""
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))

            Row(
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
            ) {
                FleetButton(
                    text = stringResource(Res.string.reports_single_vehicle),
                    onClick = onSelectSingleVehicle,
                    variant = ButtonVariant.SECONDARY,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_truck),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(FleetTokens.IconSize.S)
                        )
                    }
                )
                FleetButton(
                    text = stringResource(Res.string.retry),
                    onClick = onRetry,
                    variant = ButtonVariant.PRIMARY,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(FleetTokens.IconSize.S)
                        )
                    }
                )
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
        // Calm Fintech segmented pill (shared FleetTabBar) — replaces the old filled-indigo pills.
        val periodTabs = VehiclePLContract.PERIOD_OPTIONS.map { (value, label) -> FleetTab(value, label) }
        FleetTabBar(
            tabs = periodTabs,
            selectedTabId = selectedPeriod,
            onTabSelected = onPeriodChange,
            modifier = Modifier.fillMaxWidth(),
            scrollable = false,
        )

        // Show selected custom date range label if applicable
        if (useCustomDateRange && startDate.isNotBlank() && endDate.isNotBlank()) {
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
            Surface(
                shape = RoundedCornerShape(FleetTokens.Radius.M),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS),
                    modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.S)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_calendar),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(FleetTokens.IconSize.S)
                    )
                    Text(
                        text = "$startDate — $endDate",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
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
            // Calm Fintech segmented pill (shared FleetTabBar) with leading icons — replaces the chips.
            val viewTabs = listOf(
                FleetTab(ReportViewMode.SUMMARY, ReportViewMode.SUMMARY.localizedLabel(), iconRes = ReportViewMode.SUMMARY.icon),
                FleetTab(ReportViewMode.LIST, ReportViewMode.LIST.localizedLabel(), iconRes = ReportViewMode.LIST.icon),
                FleetTab(ReportViewMode.CHART, ReportViewMode.CHART.localizedLabel(), iconRes = ReportViewMode.CHART.icon),
            )
            FleetTabBar(
                tabs = viewTabs,
                selectedTabId = viewMode,
                onTabSelected = onViewModeChange,
                modifier = Modifier.weight(1f),
                scrollable = false,
            )

            Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))

            // Vehicle filter button
            FilledTonalIconButton(onClick = onShowVehicleFilter) {
                Icon(
                    painter = painterResource(Res.drawable.ic_truck),
                    contentDescription = null,
                    modifier = Modifier.size(FleetTokens.IconSize.M)
                )
            }
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

        // P&L Status filter & Sort
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status filter chips — Calm Fintech neutral chip token.
            Row(horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
                PLStatusFilter.entries.forEach { filter ->
                    FleetFilterChip(
                        selected = plStatusFilter == filter,
                        label = filter.localizedLabel(),
                        onClick = { onFilterChange(filter) }
                    )
                }
            }

            // Result count
            Text(
                text = stringResource(Res.string.reports_count_of_total, resultCount, totalCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================================
// Chart View Content
// ============================================================================


