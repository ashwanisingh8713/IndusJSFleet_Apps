package com.indusjs.fleet.presentation.reports.vehicle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.FleetDateFieldCompact
import com.indusjs.fleet.core.ui.FleetSearchField
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.core.util.formatPercentage
import com.indusjs.fleet.domain.entity.reports.CostBreakdownItem
import com.indusjs.fleet.domain.entity.reports.VehicleProfitLoss
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.entity.vehicle.VehicleStatus
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLContract.ChartType
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLContract.Effect
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLContract.ExportFormat
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLContract.Intent
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLContract.PLStatusFilter
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLContract.RecentReport
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLContract.SortOption
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLContract.State
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLContract.ViewMode
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.abs

/**
 * Vehicle P&L Screen - Enhanced with Fleet Overview mode showing all vehicles by default
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleProfitLossScreen(
    viewModel: VehiclePLViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val vehicleSelectorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
                is Effect.ExportGenerated -> {
                    scope.launch { snackbarHostState.showSnackbar("Report exported: ${effect.fileName}") }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (state.isFleetOverviewMode) "Fleet P&L Overview" else "Vehicle P&L",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.currentPeriodLabel.isNotBlank()) {
                            Text(
                                text = state.currentPeriodLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Profit & Loss Analysis",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    // Filter button with badge
                    if (state.isFleetOverviewMode && state.hasResult) {
                        BadgedBox(
                            badge = {
                                if (state.activeFilterCount > 0) {
                                    Badge { Text("${state.activeFilterCount}") }
                                }
                            }
                        ) {
                            IconButton(onClick = { viewModel.sendIntent(Intent.ShowVehicleFilterSheet) }) {
                                Text("🔍", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                    // Export button
                    if (state.hasResult) {
                        IconButton(onClick = { viewModel.sendIntent(Intent.ShowExportOptions) }) {
                            Text("📄", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    // Refresh button
                    IconButton(onClick = { viewModel.sendIntent(Intent.Refresh) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = "Refresh",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when {
                // Loading vehicles or initial data (only show full-screen loading before initial load)
                (state.isLoading || state.isLoadingVehicles) && !state.initialLoadComplete -> {
                    LoadingContent(
                        message = if (state.isLoadingVehicles) "Loading vehicles..." else "Loading fleet data..."
                    )
                }
                // No vehicles available - show helpful empty state
                state.vehicles.isEmpty() && state.initialLoadComplete && !state.isLoadingVehicles -> {
                    NoVehiclesContent(
                        onRefresh = { viewModel.sendIntent(Intent.Refresh) }
                    )
                }
                // Fleet Overview Mode - Show all vehicles data (handles errors internally)
                state.isFleetOverviewMode -> {
                    FleetOverviewContent(
                        state = state,
                        onPeriodChange = { viewModel.sendIntent(Intent.UpdatePeriod(it)) },
                        onStartDateChange = { viewModel.sendIntent(Intent.UpdateStartDate(it)) },
                        onEndDateChange = { viewModel.sendIntent(Intent.UpdateEndDate(it)) },
                        onViewModeChange = { viewModel.sendIntent(Intent.UpdateViewMode(it)) },
                        onChartTypeChange = { viewModel.sendIntent(Intent.UpdateChartType(it)) },
                        onSortChange = { viewModel.sendIntent(Intent.UpdateSortOption(it)) },
                        onFilterChange = { viewModel.sendIntent(Intent.UpdatePLStatusFilter(it)) },
                        onShowVehicleFilter = { viewModel.sendIntent(Intent.ShowVehicleFilterSheet) },
                        onSelectSingleVehicle = { viewModel.sendIntent(Intent.ShowVehicleSelector) },
                        onExport = { viewModel.sendIntent(Intent.ShowExportOptions) },
                        onRetry = { viewModel.sendIntent(Intent.Refresh) }
                    )
                }
                // Single Vehicle Mode - error state
                state.error != null && state.result == null -> {
                    ErrorContent(
                        error = state.error!!,
                        onRetry = { viewModel.sendIntent(Intent.Refresh) }
                    )
                }
                // Single Vehicle Mode (existing flow)
                else -> {
                    VehiclePLContent(
                        state = state,
                        onShowVehicleSelector = { viewModel.sendIntent(Intent.ShowVehicleSelector) },
                        onPeriodChange = { viewModel.sendIntent(Intent.UpdatePeriod(it)) },
                        onStartDateChange = { viewModel.sendIntent(Intent.UpdateStartDate(it)) },
                        onEndDateChange = { viewModel.sendIntent(Intent.UpdateEndDate(it)) },
                        onGenerateReport = { viewModel.sendIntent(Intent.GenerateReport) },
                        onQuickReport = { viewModel.sendIntent(Intent.QuickReportFromRecent(it)) },
                        onSortChange = { viewModel.sendIntent(Intent.UpdateSortOption(it)) },
                        onFilterChange = { viewModel.sendIntent(Intent.UpdatePLStatusFilter(it)) }
                    )
                }
            }

            // Loading overlay
            AnimatedVisibility(
                visible = state.isLoading && state.initialLoadComplete,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Updating...", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Export generating overlay
            AnimatedVisibility(
                visible = state.isGeneratingExport,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Generating Report...", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }

    // Vehicle Filter Bottom Sheet
    if (state.showVehicleFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.sendIntent(Intent.DismissVehicleFilterSheet) },
            sheetState = filterSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            VehicleFilterSheetContent(
                vehicles = state.filteredVehicles,
                selectedIds = state.tempSelectedVehicleIds,
                searchQuery = state.vehicleSearchQuery,
                onSearchChange = { viewModel.sendIntent(Intent.UpdateVehicleSearch(it)) },
                onToggleVehicle = { viewModel.sendIntent(Intent.ToggleVehicleInFilter(it)) },
                onSelectAll = { viewModel.sendIntent(Intent.SelectAllVehiclesInFilter) },
                onClearAll = { viewModel.sendIntent(Intent.ClearVehicleFilter) },
                onApply = { viewModel.sendIntent(Intent.ApplyVehicleFilter) },
                onDismiss = { viewModel.sendIntent(Intent.DismissVehicleFilterSheet) }
            )
        }
    }

    // Single Vehicle Selector Bottom Sheet
    if (state.showVehicleSelector) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.sendIntent(Intent.DismissVehicleSelector) },
            sheetState = vehicleSelectorSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            VehicleSelectorContent(
                vehicles = state.filteredVehicles,
                recentVehicles = state.recentVehicles,
                searchQuery = state.vehicleSearchQuery,
                isLoading = state.isLoadingVehicles,
                selectedVehicleId = state.selectedVehicleId,
                onSearchChange = { viewModel.sendIntent(Intent.UpdateVehicleSearch(it)) },
                onClearSearch = { viewModel.sendIntent(Intent.ClearVehicleSearch) },
                onVehicleSelected = { viewModel.sendIntent(Intent.SelectVehicle(it)) }
            )
        }
    }

    // Export Options Dialog
    if (state.showExportOptions) {
        ExportOptionsDialog(
            onDismiss = { viewModel.sendIntent(Intent.DismissExportOptions) },
            onExport = { viewModel.sendIntent(Intent.ExportReport(it)) }
        )
    }
}

// ============================================================================
// Fleet Overview Content - Shows all vehicles P&L by default
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FleetOverviewContent(
    state: State,
    onPeriodChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onViewModeChange: (ViewMode) -> Unit,
    onChartTypeChange: (ChartType) -> Unit,
    onSortChange: (SortOption) -> Unit,
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
                ViewMode.CHART -> {
                    item {
                        ChartViewContent(
                            results = state.sortedFilteredResults.take(10),
                            chartType = state.chartType,
                            onChartTypeChange = onChartTypeChange
                        )
                    }
                }
                ViewMode.SUMMARY -> {
                    item {
                        SummaryGridContent(results = state.sortedFilteredResults)
                    }
                }
                ViewMode.LIST -> {
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
private fun EmptyPLDataContent(
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
                text = "No P&L Data Available",
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
                text = "$vehicleCount vehicle${if (vehicleCount != 1) "s" else ""} in your fleet",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💡 Tips to get started:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "• Create trips for your vehicles\n• Record trip costs (fuel, toll, etc.)\n• Add maintenance costs",
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
private fun FleetErrorCard(
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
                text = "Unable to Load P&L Data",
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
                text = "$vehicleCount vehicle${if (vehicleCount != 1) "s" else ""} in your fleet",
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

@Composable
private fun FleetSummaryKPICard(
    totalVehicles: Int,
    profitableCount: Int,
    lossMakingCount: Int,
    totalRevenue: Double,
    totalExpenses: Double,
    netProfit: Double,
    profitMargin: Double,
    avgProfitPerVehicle: Double
) {
    val isProfit = netProfit >= 0
    val profitColor = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 Fleet Financial Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "$totalVehicles Vehicles",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main P&L Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KPIItem(
                    label = "Revenue",
                    value = formatCurrency(totalRevenue),
                    icon = "💰",
                    color = MaterialTheme.colorScheme.primary
                )
                KPIItem(
                    label = "Expenses",
                    value = formatCurrency(totalExpenses),
                    icon = "📉",
                    color = MaterialTheme.colorScheme.error
                )
                KPIItem(
                    label = if (isProfit) "Profit" else "Loss",
                    value = formatCurrency(abs(netProfit)),
                    icon = if (isProfit) "📈" else "📉",
                    color = profitColor,
                    isHighlighted = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SecondaryStatItem(
                    label = "Profitable",
                    value = "$profitableCount",
                    color = Color(0xFF10B981)
                )
                SecondaryStatItem(
                    label = "Loss Making",
                    value = "$lossMakingCount",
                    color = Color(0xFFEF4444)
                )
                SecondaryStatItem(
                    label = "Margin",
                    value = "${formatPercentage(profitMargin)}%",
                    color = profitColor
                )
                SecondaryStatItem(
                    label = "Avg/Vehicle",
                    value = formatCurrency(avgProfitPerVehicle),
                    color = if (avgProfitPerVehicle >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
private fun KPIItem(
    label: String,
    value: String,
    icon: String,
    color: Color,
    isHighlighted: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = if (isHighlighted) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SecondaryStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ============================================================================
// Performers Card - Top and Worst
// ============================================================================

@Composable
private fun PerformersCard(
    topPerformer: VehicleProfitLoss?,
    worstPerformer: VehicleProfitLoss?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Performer
        topPerformer?.let {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏆", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Top Performer",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it.vehicleNumber ?: "Vehicle #${it.vehicleId}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+${formatCurrency(it.netProfit)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Worst Performer
        worstPerformer?.let {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚠️", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Needs Attention",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it.vehicleNumber ?: "Vehicle #${it.vehicleId}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatCurrency(it.netProfit),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ============================================================================
// Period Selection Row
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PeriodSelectionRow(
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

        // Custom date range inputs
        AnimatedVisibility(visible = useCustomDateRange) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FleetDateFieldCompact(
                    rawValue = startDate,
                    onRawValueChange = onStartDateChange,
                    label = "From",
                    modifier = Modifier.weight(1f)
                )
                FleetDateFieldCompact(
                    rawValue = endDate,
                    onRawValueChange = onEndDateChange,
                    label = "To",
                    modifier = Modifier.weight(1f)
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
private fun ViewModeAndFilterSection(
    viewMode: ViewMode,
    sortOption: SortOption,
    plStatusFilter: PLStatusFilter,
    resultCount: Int,
    totalCount: Int,
    onViewModeChange: (ViewMode) -> Unit,
    onSortChange: (SortOption) -> Unit,
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
                ViewMode.entries.forEach { mode ->
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

@Composable
private fun ChartViewContent(
    results: List<VehicleProfitLoss>,
    chartType: ChartType,
    onChartTypeChange: (ChartType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Chart type selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📈 P&L Chart (Top 10)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ChartType.entries.forEach { type ->
                        FilterChip(
                            selected = chartType == type,
                            onClick = { onChartTypeChange(type) },
                            label = { Text(type.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Simple Bar Chart
            if (results.isNotEmpty()) {
                SimpleBarChart(results = results)
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data to display", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SimpleBarChart(results: List<VehicleProfitLoss>) {
    val maxProfit = results.maxOfOrNull { abs(it.netProfit) } ?: 1.0
    val profitColor = Color(0xFF10B981)
    val lossColor = Color(0xFFEF4444)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        results.forEach { result ->
            val barWidth = (abs(result.netProfit) / maxProfit).toFloat()
            val isProfit = result.netProfit >= 0

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vehicle label
                Text(
                    text = result.vehicleNumber?.take(10) ?: "#${result.vehicleId}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(80.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(barWidth.coerceIn(0.05f, 1f))
                            .background(if (isProfit) profitColor else lossColor)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Value
                Text(
                    text = formatCurrency(result.netProfit),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isProfit) profitColor else lossColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(70.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

// ============================================================================
// Summary Grid Content
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryGridContent(results: List<VehicleProfitLoss>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        results.forEach { result ->
            VehiclePLSummaryChip(result = result)
        }
    }
}

@Composable
private fun VehiclePLSummaryChip(result: VehicleProfitLoss) {
    val isProfit = result.netProfit >= 0
    val bgColor = if (isProfit) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f)
    val textColor = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = result.vehicleNumber ?: "#${result.vehicleId}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatCurrency(result.netProfit),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${result.totalTrips} trips",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================================
// Vehicle Filter Sheet Content
// ============================================================================

@Composable
private fun VehicleFilterSheetContent(
    vehicles: List<Vehicle>,
    selectedIds: Set<String>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleVehicle: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🚛 Filter Vehicles",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${selectedIds.size} selected",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search
        FleetSearchField(
            query = searchQuery,
            onQueryChange = onSearchChange,
            placeholder = "Search vehicles...",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Select All / Clear All
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onSelectAll) {
                Text("Select All (${vehicles.size})")
            }
            TextButton(onClick = onClearAll) {
                Text("Clear All")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Vehicle list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(vehicles, key = { it.id }) { vehicle ->
                val isSelected = selectedIds.contains(vehicle.id)
                Surface(
                    onClick = { onToggleVehicle(vehicle.id) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleVehicle(vehicle.id) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = vehicle.registrationNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${vehicle.make ?: ""} ${vehicle.model ?: ""}".trim().ifEmpty { "Vehicle" },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                enabled = selectedIds.isNotEmpty()
            ) {
                Text("Apply Filter")
            }
        }
    }
}

// ============================================================================
// Export Options Dialog
// ============================================================================

@Composable
private fun ExportOptionsDialog(
    onDismiss: () -> Unit,
    onExport: (ExportFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("📄", style = MaterialTheme.typography.displaySmall) },
        title = { Text("Export Report", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Choose export format:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExportFormat.entries.forEach { format ->
                    Surface(
                        onClick = { onExport(format) },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(format.icon, style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = format.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = ".${format.extension}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ============================================================================
// Loading, Error & Empty States
// ============================================================================

@Composable
private fun LoadingContent(
    message: String = "Loading fleet data..."
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoVehiclesContent(
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("🚛", style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Vehicles Found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add vehicles to your fleet to see\nprofit & loss analysis",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = onRefresh,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("🔄 Refresh")
            }
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("⚠️", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("🔄 Retry")
            }
        }
    }
}

// ============================================================================
// Vehicle PL Content - Existing single vehicle report content
// ============================================================================

@Composable
private fun VehiclePLContent(
    state: State,
    onShowVehicleSelector: () -> Unit,
    onPeriodChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onGenerateReport: () -> Unit,
    onQuickReport: (RecentReport) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onFilterChange: (PLStatusFilter) -> Unit
) {
    // Determine current step
    val currentStep = when {
        state.result != null -> 3
        state.selectedVehicleId != null -> 2
        else -> 1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Step Indicator - only show when not viewing results
        if (state.result == null) {
            WizardStepIndicator(
                currentStep = currentStep,
                steps = listOf("Select Vehicle", "Select Period", "View Report")
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        when {
            // ========== STEP 3: Results ==========
            state.result != null -> {
                // Result Header with vehicle info and New Report button
                ResultHeaderCard(
                    vehicleNumber = state.result!!.vehicleNumber ?: "Vehicle",
                    vehicleMakeModel = "${state.selectedVehicle?.make ?: ""} ${state.selectedVehicle?.model ?: ""}".trim(),
                    period = state.result!!.period ?: state.period,
                    onNewReport = onShowVehicleSelector
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { PLResultKPICard(result = state.result!!) }

                    if (state.result!!.costBreakdown.isNotEmpty()) {
                        item { PLCostBreakdownCard(costs = state.result!!.costBreakdown) }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onShowVehicleSelector,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("🔄 New Report")
                            }
                            Button(
                                onClick = { /* TODO: Export */ },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("📄 Export PDF")
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }

            // ========== STEP 2: Period Selection ==========
            state.selectedVehicleId != null -> {
                // Selected Vehicle Card with Change button
                SelectedVehicleDisplayCard(
                    vehicle = state.selectedVehicle!!,
                    onChangeVehicle = onShowVehicleSelector
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Period selection title
                Text(
                    text = "📅 Select Report Period",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Period selection card
                PeriodSelectionChipsCard(
                    selectedPeriod = state.period,
                    useCustomDateRange = state.useCustomDateRange,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    onPeriodChange = onPeriodChange,
                    onStartDateChange = onStartDateChange,
                    onEndDateChange = onEndDateChange
                )

                Spacer(modifier = Modifier.weight(1f))

                // Error message
                if (state.error != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = state.error ?: "Something went wrong",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Generate Report Button
                Button(
                    onClick = onGenerateReport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = state.canGenerateReport,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "📊 Generate Report",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ========== STEP 1: Vehicle Selection ==========
            else -> {
                // Welcome Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📊", style = MaterialTheme.typography.displaySmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Vehicle Profit & Loss",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Analyze financial performance of your vehicles",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onShowVehicleSelector,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🚛")
                                Text(
                                    text = "Select Vehicle",
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (state.vehicles.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${state.vehicles.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Reports
                if (state.recentReports.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "⏱ Recent Reports",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(state.recentReports) { report ->
                            RecentReportCard(report = report, onClick = { onQuickReport(report) })
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f)
                )
            }
        }

        // Multi-Vehicle Results
        if (state.multiResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    MultiVehicleSummaryCard(
                        totalVehicles = state.multiResults.size,
                        profitableCount = state.totalProfitableCount,
                        lossMakingCount = state.totalLossMakingCount,
                        totalRevenue = state.totalRevenue,
                        totalExpenses = state.totalExpenses,
                        totalNetProfit = state.totalNetProfit
                    )
                }
                item {
                    SortingFilterSection(
                        sortOption = state.sortOption,
                        plStatusFilter = state.plStatusFilter,
                        resultCount = state.sortedFilteredResults.size,
                        onSortChange = onSortChange,
                        onFilterChange = onFilterChange
                    )
                }
                items(state.sortedFilteredResults, key = { it.vehicleId }) { result ->
                    VehiclePLResultCard(result)
                }
            }
        }
    }
}

// ============================================================================
// Wizard Step Indicator
// ============================================================================

@Composable
private fun WizardStepIndicator(
    currentStep: Int,
    steps: List<String>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val stepNumber = index + 1
            val isCompleted = stepNumber < currentStep
            val isCurrent = stepNumber == currentStep

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Step circle
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = when {
                        isCompleted -> Color(0xFF10B981)
                        isCurrent -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "$stepNumber",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) MaterialTheme.colorScheme.onPrimary
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Step label (only show for current on small screens)
                if (isCurrent) {
                    Text(
                        text = step,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                // Connector line (except for last step)
                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .padding(horizontal = 4.dp)
                            .background(
                                if (isCompleted) Color(0xFF10B981)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
        }
    }
}

// ============================================================================
// Selected Vehicle Display Card - Clear with Change button
// ============================================================================

@Composable
private fun SelectedVehicleDisplayCard(
    vehicle: Vehicle,
    onChangeVehicle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkmark
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color(0xFF10B981)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Vehicle Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.registrationNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${vehicle.make ?: ""} ${vehicle.model ?: ""}".trim().ifEmpty { "Vehicle" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Change Vehicle Button
            OutlinedButton(
                onClick = onChangeVehicle,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Change Vehicle",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ============================================================================
// Period Selection Card with Chips
// ============================================================================

@Composable
private fun PeriodSelectionChipsCard(
    selectedPeriod: String,
    useCustomDateRange: Boolean,
    startDate: String,
    endDate: String,
    onPeriodChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Period chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "weekly" to "Week",
                    "monthly" to "Month",
                    "yearly" to "Year",
                    "custom" to "Custom"
                ).forEach { (key, label) ->
                    val isSelected = selectedPeriod == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPeriodChange(key) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Custom date range
            AnimatedVisibility(visible = useCustomDateRange) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FleetDateFieldCompact(
                        rawValue = startDate,
                        onRawValueChange = onStartDateChange,
                        label = "From",
                        modifier = Modifier.weight(1f)
                    )
                    FleetDateFieldCompact(
                        rawValue = endDate,
                        onRawValueChange = onEndDateChange,
                        label = "To",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ============================================================================
// Result Header Card - Shows vehicle info and New Report action
// ============================================================================

@Composable
private fun ResultHeaderCard(
    vehicleNumber: String,
    vehicleMakeModel: String,
    period: String,
    onNewReport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🚛", style = MaterialTheme.typography.headlineSmall)
                Column {
                    Text(
                        text = vehicleNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$vehicleMakeModel • ${period.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TextButton(onClick = onNewReport) {
                Text(
                    text = "New Report",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ============================================================================
// P&L Result KPI Card
// ============================================================================

@Composable
private fun PLResultKPICard(result: VehicleProfitLoss) {
    val isProfit = result.netProfit >= 0
    val profitColor = Color(0xFF10B981)
    val lossColor = Color(0xFFEF4444)
    val expenseColor = Color(0xFFF59E0B)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Main KPI - Net Profit/Loss
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isProfit) profitColor.copy(alpha = 0.1f)
                                else lossColor.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isProfit) "Net Profit" else "Net Loss",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isProfit) profitColor.copy(alpha = 0.2f)
                                else lossColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${result.totalTrips} trips",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isProfit) profitColor else lossColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatCurrency(kotlin.math.abs(result.netProfit)),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isProfit) profitColor else lossColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Margin: ${formatPercentage(result.profitMargin)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Revenue & Expenses Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = profitColor.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📈", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Revenue",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(result.totalRevenue),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = profitColor
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = expenseColor.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("💸", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(result.totalExpenses),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = expenseColor
                    )
                }
            }
        }
    }
}

// ============================================================================
// P&L Cost Breakdown Card
// ============================================================================

@Composable
private fun PLCostBreakdownCard(costs: List<CostBreakdownItem>) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💰 Cost Breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (expanded) "Hide ▲" else "Show ▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
// Sticky Vehicle Header - Always visible at top with Change button (LEGACY - REMOVE)
// ============================================================================

@Composable
private fun StickyVehicleHeader(
    selectedVehicle: Vehicle?,
    vehicleCount: Int,
    isLoading: Boolean,
    onChangeVehicle: () -> Unit
) {
    // Legacy - not used anymore
}

// ============================================================================
// Vehicle Selector Bottom Sheet Content
// ============================================================================

@Composable
private fun VehicleSelectorContent(
    vehicles: List<Vehicle>,
    recentVehicles: List<Vehicle>,
    searchQuery: String,
    isLoading: Boolean,
    selectedVehicleId: String?,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onVehicleSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Vehicle",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "${vehicles.size}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Search Field
        FleetSearchField(
            query = searchQuery,
            onQueryChange = onSearchChange,
            placeholder = "Search vehicle number, make, driver...",
            modifier = Modifier.fillMaxWidth()
        )

        if (searchQuery.isNotBlank()) {
            Text(
                text = "${vehicles.size} result${if (vehicles.size != 1) "s" else ""} found",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (recentVehicles.isNotEmpty() && searchQuery.isBlank()) {
                    item {
                        Text(
                            text = "Recent",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            items(recentVehicles, key = { "recent_${it.id}" }) { vehicle ->
                                Surface(
                                    modifier = Modifier.clickable { onVehicleSelected(vehicle.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (vehicle.id == selectedVehicleId)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = vehicle.registrationNumber,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${vehicle.make ?: ""} ${vehicle.model ?: ""}".trim().take(12),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 6.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                item {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Results" else "All Vehicles",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (vehicles.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank())
                                    "No vehicles match \"$searchQuery\""
                                else "No vehicles available",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(vehicles, key = { it.id }) { vehicle ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onVehicleSelected(vehicle.id) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (vehicle.id == selectedVehicleId)
                                MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = vehicle.registrationNumber,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "${vehicle.make ?: ""} ${vehicle.model ?: ""}".trim().ifEmpty { "-" },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        vehicle.assignedDriverName?.let { driver ->
                                            Text(
                                                text = "• $driver",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                // Status dot
                                val colorScheme = VehicleStatus.getColorScheme(vehicle.status)
                                val statusColor = when (colorScheme) {
                                    com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.SUCCESS -> Color(0xFF10B981)
                                    com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.WARNING -> Color(0xFFF59E0B)
                                    com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.ERROR -> Color(0xFFEF4444)
                                    com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.INFO -> Color(0xFF3B82F6)
                                    com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.NEUTRAL -> Color(0xFF6B7280)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(statusColor, CircleShape)
                                )

                                if (vehicle.id == selectedVehicleId) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_check),
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// ============================================================================
// Recent Report Card
// ============================================================================

@Composable
private fun RecentReportCard(
    report: RecentReport,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (report.isProfit)
                Color(0xFF10B981).copy(alpha = 0.1f)
            else Color(0xFFEF4444).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatCurrency(kotlin.math.abs(report.profitLoss)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (report.isProfit) Color(0xFF10B981) else Color(0xFFEF4444)
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

@Composable
fun MultiVehicleSummaryCard(
    totalVehicles: Int,
    profitableCount: Int,
    lossMakingCount: Int,
    totalRevenue: Double,
    totalExpenses: Double,
    totalNetProfit: Double
) {
    val isOverallProfit = totalNetProfit >= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverallProfit)
                Color(0xFF10B981).copy(alpha = 0.08f)
            else Color(0xFFEF4444).copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 Fleet Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isOverallProfit) Color(0xFF10B981).copy(alpha = 0.2f)
                    else Color(0xFFEF4444).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (isOverallProfit) "NET PROFIT" else "NET LOSS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverallProfit) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🚛", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "$totalVehicles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "$profitableCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = "Profitable",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "$lossMakingCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                    Text(
                        text = "Loss Making",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Revenue",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(totalRevenue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Expenses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(totalExpenses),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isOverallProfit) "Net Profit" else "Net Loss",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(kotlin.math.abs(totalNetProfit)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverallProfit) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

// ============================================================================
// Sorting Filter Section
// ============================================================================

@Composable
fun SortingFilterSection(
    sortOption: SortOption,
    plStatusFilter: PLStatusFilter,
    resultCount: Int,
    onSortChange: (SortOption) -> Unit,
    onFilterChange: (PLStatusFilter) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$resultCount vehicle(s)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = sortOption.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PLStatusFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = plStatusFilter == filter,
                        onClick = { onFilterChange(filter) },
                        label = {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}

// ============================================================================
// Vehicle PL Result Card
// ============================================================================

@Composable
private fun VehiclePLResultCard(result: VehicleProfitLoss) {
    val isProfit = result.netProfit >= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🚛", style = MaterialTheme.typography.titleMedium)
                    Column {
                        Text(
                            text = result.vehicleNumber ?: "Vehicle #${result.vehicleId}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${result.totalTrips} trips",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isProfit) Color(0xFF10B981).copy(alpha = 0.15f)
                    else Color(0xFFEF4444).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = formatCurrency(kotlin.math.abs(result.netProfit)),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Revenue",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(result.totalRevenue),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF10B981)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(result.totalExpenses),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF59E0B)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Margin",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatPercentage(result.profitMargin),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}
