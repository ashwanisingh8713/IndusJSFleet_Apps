package com.ijs.reports.presentation.vehicle

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.reports.TAG_VEHICLE_PL_VM
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.uicomponents.components.UiText
import com.ijs.reports.domain.usecase.GetMultiVehiclePLUseCase
import com.ijs.reports.domain.usecase.GetVehicleProfitLossUseCase
import com.ijs.reports.presentation.RecentReport
import com.ijs.reports.presentation.ReportExportFormat
import com.ijs.vehicle.domain.repository.VehicleRepository
import com.ijs.reports.presentation.vehicle.VehiclePLContract.Effect
import com.ijs.reports.presentation.vehicle.VehiclePLContract.Intent
import com.ijs.reports.presentation.vehicle.VehiclePLContract.State
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_select_vehicle
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_failed_load_vehicles
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_select_one_vehicle
import kotlinx.coroutines.flow.collectLatest

/**
 * ViewModel for Vehicle P&L Screen - Enhanced with Fleet Overview mode.
 * Report generation delegated to [VehiclePLReportGenerator].
 * Date calculations delegated to [VehiclePLDateCalculator].
 */
@Inject
class VehiclePLViewModel(
    private val getVehicleProfitLossUseCase: GetVehicleProfitLossUseCase,
    private val getMultiVehiclePLUseCase: GetMultiVehiclePLUseCase,
    private val vehicleRepository: VehicleRepository,
    private val logger: FleetLogger
) : MviViewModel<State, Intent, Effect>(State()) {

    private val reportGenerator = VehiclePLReportGenerator(
        getVehicleProfitLossUseCase, getMultiVehiclePLUseCase, logger
    )

    init {
        sendIntent(Intent.InitFleetOverview)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.InitFleetOverview -> initFleetOverview()
            is Intent.LoadVehicles -> loadVehicles()
            is Intent.LoadFleetOverview -> loadFleetOverview()
            is Intent.SelectVehicle -> selectVehicle(intent.vehicleId)
            is Intent.ToggleVehicle -> toggleVehicle(intent.vehicleId)
            is Intent.SelectAllVehicles -> updateState { copy(selectedVehicleIds = vehicles.map { it.id }.toSet()) }
            is Intent.ClearVehicles -> updateState { copy(selectedVehicleIds = emptySet()) }
            is Intent.UpdatePeriod -> handlePeriodUpdate(intent.period)
            is Intent.UpdateStartDate -> updateState { copy(startDate = intent.date) }
            is Intent.UpdateEndDate -> updateState { copy(endDate = intent.date) }
            is Intent.ToggleMultiMode -> updateState { copy(isMultiMode = !isMultiMode, result = null, multiResults = emptyList()) }
            is Intent.GenerateReport -> generateReport()
            is Intent.Refresh -> {
                updateState { copy(error = null, initialLoadComplete = false) }
                loadVehicles()
            }
            is Intent.ShowVehicleSelector -> updateState { copy(showVehicleSelector = true, isFleetOverviewMode = false) }
            is Intent.DismissVehicleSelector -> updateState { copy(showVehicleSelector = false, vehicleSearchQuery = "") }
            is Intent.UpdateVehicleSearch -> updateState { copy(vehicleSearchQuery = intent.query) }
            is Intent.ClearVehicleSearch -> updateState { copy(vehicleSearchQuery = "") }
            is Intent.QuickReportFromRecent -> quickReportFromRecent(intent.report)
            is Intent.UpdateSortOption -> updateState { copy(sortOption = intent.option, showSortMenu = false) }
            is Intent.UpdatePLStatusFilter -> updateState { copy(plStatusFilter = intent.filter) }
            is Intent.ToggleSortMenu -> updateState { copy(showSortMenu = !showSortMenu) }
            is Intent.DismissSortMenu -> updateState { copy(showSortMenu = false) }
            is Intent.ShowVehicleFilterSheet -> updateState {
                copy(
                    showVehicleFilterSheet = true,
                    tempSelectedVehicleIds = selectedVehicleIds.ifEmpty { vehicles.map { it.id }.toSet() },
                    vehicleSearchQuery = ""
                )
            }
            is Intent.DismissVehicleFilterSheet -> updateState { copy(showVehicleFilterSheet = false, vehicleSearchQuery = "") }
            is Intent.ToggleVehicleInFilter -> {
                updateState {
                    val newSelection = if (tempSelectedVehicleIds.contains(intent.vehicleId)) {
                        tempSelectedVehicleIds - intent.vehicleId
                    } else {
                        tempSelectedVehicleIds + intent.vehicleId
                    }
                    copy(tempSelectedVehicleIds = newSelection)
                }
            }
            is Intent.SelectAllVehiclesInFilter -> updateState { copy(tempSelectedVehicleIds = vehicles.map { it.id }.toSet()) }
            is Intent.ClearVehicleFilter -> updateState { copy(tempSelectedVehicleIds = emptySet()) }
            is Intent.ApplyVehicleFilter -> applyVehicleFilter()
            is Intent.UpdateViewMode -> updateState { copy(viewMode = intent.mode) }
            is Intent.UpdateChartType -> updateState { copy(chartType = intent.type) }
            is Intent.ShowDateRangePicker -> updateState {
                copy(showDateRangePicker = true, pickerStartDate = startDate, pickerEndDate = endDate)
            }
            is Intent.HideDateRangePicker -> updateState { copy(showDateRangePicker = false) }
            is Intent.UpdatePickerStartDate -> updateState { copy(pickerStartDate = intent.date) }
            is Intent.UpdatePickerEndDate -> updateState { copy(pickerEndDate = intent.date) }
            is Intent.ApplyCustomDateRange -> applyCustomDateRange(intent.startDate, intent.endDate)
            is Intent.ShowExportOptions -> updateState { copy(showExportOptions = true) }
            is Intent.DismissExportOptions -> updateState { copy(showExportOptions = false) }
            is Intent.ExportReport -> exportReport(intent.format)
        }
    }

    private suspend fun handlePeriodUpdate(period: String) {
        if (period == "custom") {
            updateState { copy(period = period, useCustomDateRange = true, showDateRangePicker = true, pickerStartDate = startDate, pickerEndDate = endDate) }
            return
        }
        updateState { copy(period = period, useCustomDateRange = false) }
        if (currentState.isFleetOverviewMode && currentState.initialLoadComplete) {
            loadFleetOverview()
        }
    }

    private suspend fun applyCustomDateRange(startDate: String, endDate: String) {
        updateState { copy(startDate = startDate, endDate = endDate, showDateRangePicker = false) }
        if (currentState.isFleetOverviewMode && currentState.initialLoadComplete) {
            loadFleetOverview()
        }
    }

    private suspend fun initFleetOverview() {
        logger.d(TAG_VEHICLE_PL_VM, "initFleetOverview: Resetting state to fleet overview mode")
        updateState {
            copy(
                isFleetOverviewMode = true,
                selectedVehicleId = null,
                result = null,
                error = null,
                initialLoadComplete = false,
                isLoading = false,
                showVehicleSelector = false
            )
        }
        loadVehicles()
    }

    private suspend fun loadFleetOverview() {
        logger.d(TAG_VEHICLE_PL_VM, "loadFleetOverview: vehicles=${currentState.vehicles.size}")

        if (currentState.vehicles.isEmpty() && !currentState.isLoadingVehicles) {
            updateState { copy(isLoading = false, isFleetOverviewMode = true, initialLoadComplete = true, error = null) }
            return
        }
        if (currentState.isLoadingVehicles) return

        updateState { copy(isLoading = true, error = null, isFleetOverviewMode = true) }

        val periodLabel = VehiclePLDateCalculator.getCurrentPeriodLabel(
            currentState.period, currentState.startDate, currentState.endDate
        )

        when (val result = reportGenerator.loadFleetOverview(
            vehicles = currentState.vehicles,
            selectedVehicleIds = currentState.selectedVehicleIds,
            period = currentState.period,
            customStartDate = currentState.startDate,
            customEndDate = currentState.endDate
        )) {
            is VehiclePLReportGenerator.FleetOverviewResult.Success -> {
                updateState { copy(isLoading = false, error = null, multiResults = result.data, initialLoadComplete = true, currentPeriodLabel = periodLabel) }
            }
            is VehiclePLReportGenerator.FleetOverviewResult.Error -> {
                updateState { copy(isLoading = false, error = result.message, initialLoadComplete = true) }
            }
            is VehiclePLReportGenerator.FleetOverviewResult.Empty -> {
                updateState { copy(isLoading = false, error = null, multiResults = emptyList(), initialLoadComplete = true, currentPeriodLabel = periodLabel) }
            }
            is VehiclePLReportGenerator.FleetOverviewResult.Loading -> { /* Already handled */ }
        }
    }

    private suspend fun applyVehicleFilter() {
        updateState { copy(selectedVehicleIds = tempSelectedVehicleIds, showVehicleFilterSheet = false, vehicleSearchQuery = "") }
        loadFleetOverview()
    }

    private suspend fun exportReport(format: ReportExportFormat) {
        updateState { copy(isGeneratingExport = true, showExportOptions = false) }
        kotlinx.coroutines.delay(1500)
        val fileName = "Fleet_PL_Report_${currentState.currentPeriodLabel.replace(" ", "_")}.${format.extension}"
        updateState { copy(isGeneratingExport = false) }
        // Single snackbar via ExportGenerated (screen maps it to "Report exported: <path>");
        // a second ShowSnackbar here would overwrite it in the single pendingSnackbar slot.
        sendEffect(Effect.ExportGenerated(fileName, format))
    }

    private fun selectVehicle(vehicleId: String) {
        val currentRecentIds = currentState.recentVehicleIds.toMutableList()
        currentRecentIds.remove(vehicleId)
        currentRecentIds.add(0, vehicleId)
        updateState {
            copy(
                selectedVehicleId = vehicleId,
                showVehicleSelector = false,
                vehicleSearchQuery = "",
                recentVehicleIds = currentRecentIds.take(5),
                isFleetOverviewMode = false
            )
        }
    }

    private suspend fun quickReportFromRecent(report: RecentReport) {
        updateState {
            copy(selectedVehicleId = report.vehicleId, period = report.period, useCustomDateRange = report.period == "custom", isFleetOverviewMode = false)
        }
        generateReport()
    }

    private suspend fun loadVehicles() {
        logger.d(TAG_VEHICLE_PL_VM, "loadVehicles: Starting")
        updateState { copy(isLoadingVehicles = true) }

        vehicleRepository.getVehicles().collectLatest { result ->
            when (result) {
                is Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val vehicles = result.data as? List<com.ijs.vehicle.domain.entity.Vehicle> ?: emptyList()
                    logger.d(TAG_VEHICLE_PL_VM, "loadVehicles: ${vehicles.size} vehicles loaded")
                    updateState { copy(isLoadingVehicles = false, vehicles = vehicles) }

                    if (!currentState.initialLoadComplete) {
                        when {
                            currentState.isFleetOverviewMode -> loadFleetOverview()
                            currentState.selectedVehicleId != null -> generateReport()
                            else -> updateState { copy(initialLoadComplete = true) }
                        }
                    }
                }
                is Result.Error -> {
                    logger.e(TAG_VEHICLE_PL_VM, "loadVehicles: Error - ${result.message}")
                    updateState { copy(isLoadingVehicles = false, initialLoadComplete = true, error = result.message?.let { UiText.Raw(it) } ?: UiText.StringRes(Res.string.report_failed_load_vehicles)) }
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    private fun toggleVehicle(vehicleId: String) {
        updateState {
            val newSelection = if (selectedVehicleIds.contains(vehicleId)) selectedVehicleIds - vehicleId else selectedVehicleIds + vehicleId
            copy(selectedVehicleIds = newSelection)
        }
    }

    private suspend fun generateReport() {
        if (currentState.isMultiMode) {
            if (currentState.selectedVehicleIds.isEmpty()) {
                sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.report_select_one_vehicle)))
                return
            }
            generateMultiVehicleReport()
        } else {
            if (currentState.selectedVehicleId == null) {
                sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.error_select_vehicle)))
                return
            }
            generateSingleVehicleReport()
        }
    }

    private suspend fun generateSingleVehicleReport() {
        updateState { copy(isLoading = true, error = null) }
        when (val result = reportGenerator.generateSingleVehicleReport(
            vehicleId = currentState.selectedVehicleId!!,
            period = currentState.period,
            vehicle = currentState.selectedVehicle,
            existingRecentReports = currentState.recentReports
        )) {
            is VehiclePLReportGenerator.SingleVehicleResult.Success -> {
                updateState { copy(isLoading = false, result = result.data, recentReports = result.updatedRecentReports) }
            }
            is VehiclePLReportGenerator.SingleVehicleResult.Error -> {
                updateState { copy(isLoading = false, error = result.message) }
            }
            is VehiclePLReportGenerator.SingleVehicleResult.Loading -> { /* Already handled */ }
        }
    }

    private suspend fun generateMultiVehicleReport() {
        updateState { copy(isLoading = true, error = null) }
        when (val result = reportGenerator.generateMultiVehicleReport(
            vehicleIds = currentState.selectedVehicleIds,
            startDate = currentState.startDate,
            endDate = currentState.endDate
        )) {
            is VehiclePLReportGenerator.MultiVehicleResult.Success -> {
                updateState { copy(isLoading = false, multiResults = result.data) }
            }
            is VehiclePLReportGenerator.MultiVehicleResult.Error -> {
                updateState { copy(isLoading = false, error = result.message) }
            }
            is VehiclePLReportGenerator.MultiVehicleResult.Loading -> { /* Already handled */ }
        }
    }
}
