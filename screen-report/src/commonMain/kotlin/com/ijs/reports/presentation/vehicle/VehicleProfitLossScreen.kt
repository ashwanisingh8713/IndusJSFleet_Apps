package com.ijs.reports.presentation.vehicle

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetDateRangePickerDialog
import com.indusjs.uicomponents.components.FleetSearchField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.core.util.formatPercentage
import com.ijs.reports.domain.entity.CostBreakdownItem
import com.ijs.reports.domain.entity.VehicleProfitLoss
import com.ijs.vehicle.domain.entity.Vehicle
import com.ijs.vehicle.domain.entity.VehicleStatus
import com.ijs.reports.presentation.vehicle.VehiclePLContract.Effect
import com.ijs.reports.presentation.vehicle.VehiclePLContract.Intent
import com.ijs.reports.presentation.vehicle.VehiclePLContract.State
import com.ijs.reports.presentation.PLStatusFilter
import com.ijs.reports.presentation.RecentReport
import com.ijs.reports.presentation.localizedLabel
import com.ijs.reports.presentation.ReportChartType as ChartType
import com.ijs.reports.presentation.ReportExportFormat as ExportFormat
import com.ijs.reports.presentation.ReportViewMode as ViewMode
import com.ijs.reports.presentation.VehiclePLSortOption as SortOption
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
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
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val vehicleSelectorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
                }
                is Effect.ExportGenerated -> {
                    pendingSnackbar = UiText.StringRes(Res.string.reports_exported_path, args = listOf(effect.fileName))
                }
            }
        }
    }

    // Date Range Picker Dialog for Custom period
    FleetDateRangePickerDialog(
        isVisible = state.showDateRangePicker,
        startDate = state.pickerStartDate,
        endDate = state.pickerEndDate,
        onStartDateChange = { viewModel.sendIntent(Intent.UpdatePickerStartDate(it)) },
        onEndDateChange = { viewModel.sendIntent(Intent.UpdatePickerEndDate(it)) },
        onApply = { start, end ->
            viewModel.sendIntent(Intent.ApplyCustomDateRange(start, end))
        },
        onDismiss = { viewModel.sendIntent(Intent.HideDateRangePicker) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(Res.string.reports_vehicle_pl_title),
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
                                text = stringResource(Res.string.vehicle_pl_analysis_title),
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
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
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
                                Icon(
                                    painter = painterResource(Res.drawable.ic_filter),
                                    contentDescription = stringResource(Res.string.reports_cd_filter),
                                    modifier = Modifier.size(FleetTokens.IconSize.Default)
                                )
                            }
                        }
                    }
                    // Export button
                    if (state.hasResult) {
                        IconButton(onClick = { viewModel.sendIntent(Intent.ShowExportOptions) }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_download),
                                contentDescription = stringResource(Res.string.reports_cd_export),
                                modifier = Modifier.size(FleetTokens.IconSize.Default)
                            )
                        }
                    }
                    // Refresh button
                    IconButton(onClick = { viewModel.sendIntent(Intent.Refresh) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = stringResource(Res.string.refresh),
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
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
                        message = if (state.isLoadingVehicles) stringResource(Res.string.reports_loading_vehicles) else stringResource(Res.string.reports_loading_fleet)
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
                    com.indusjs.uicomponents.components.ErrorContent(
                        error = state.error!!.resolve(),
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
                    Surface(
                        shape = RoundedCornerShape(FleetTokens.Radius.XL),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = FleetTokens.Elevation.Dialog
                    ) {
                        Column(
                            modifier = Modifier.padding(FleetTokens.Spacing.XXL),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
                            Text(stringResource(Res.string.reports_updating), fontWeight = FontWeight.Medium)
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
                    Surface(
                        shape = RoundedCornerShape(FleetTokens.Radius.XL),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = FleetTokens.Elevation.Dialog
                    ) {
                        Column(
                            modifier = Modifier.padding(FleetTokens.Spacing.XXL),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
                            Text(stringResource(Res.string.reports_generating), fontWeight = FontWeight.Medium)
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

@Composable
private fun NoVehiclesContent(onRefresh: () -> Unit) {
    FleetSectionCard(
        modifier = Modifier.padding(FleetTokens.Spacing.L),
        contentPadding = FleetTokens.Spacing.XL
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(Res.string.reports_no_vehicles_available), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
            FleetButton(
                text = stringResource(Res.string.refresh),
                onClick = onRefresh
            )
        }
    }
}

@Composable
private fun ExportOptionsDialog(
    onDismiss: () -> Unit,
    onExport: (ExportFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(FleetTokens.Radius.XL),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = stringResource(Res.string.reports_export_dialog_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
                ExportFormat.entries.forEach { format ->
                    FleetButton(
                        text = format.localizedLabel(),
                        onClick = { onExport(format) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            FleetButton(
                text = stringResource(Res.string.close),
                onClick = onDismiss,
                variant = ButtonVariant.GHOST
            )
        }
    )
}
