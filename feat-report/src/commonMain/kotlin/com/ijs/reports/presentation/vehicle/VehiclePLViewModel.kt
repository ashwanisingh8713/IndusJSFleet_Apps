package com.ijs.reports.presentation.vehicle

import co.touchlab.kermit.Logger
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.util.currentTimeMillis
import com.ijs.reports.data.model.MultiVehiclePLRequest
import com.ijs.reports.domain.repository.ReportsRepository
import com.ijs.vehicle.domain.repository.VehicleRepository
import com.ijs.reports.presentation.vehicle.VehiclePLContract.Effect
import com.ijs.reports.presentation.vehicle.VehiclePLContract.ExportFormat
import com.ijs.reports.presentation.vehicle.VehiclePLContract.Intent
import com.ijs.reports.presentation.vehicle.VehiclePLContract.RecentReport
import com.ijs.reports.presentation.vehicle.VehiclePLContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.collectLatest

private val log = Logger.withTag("VehiclePLViewModel")

/**
 * ViewModel for Vehicle P&L Screen - Enhanced with Fleet Overview mode
 */
@Inject
class VehiclePLViewModel(
    private val reportsRepository: ReportsRepository,
    private val vehicleRepository: VehicleRepository
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        // Always start fresh in fleet overview mode
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
            is Intent.UpdatePeriod -> {
                updateState {
                    copy(
                        period = intent.period,
                        useCustomDateRange = intent.period == "custom"
                    )
                }
                // Auto-refresh when period changes in fleet overview mode
                if (currentState.isFleetOverviewMode && currentState.initialLoadComplete) {
                    loadFleetOverview()
                }
            }
            is Intent.UpdateStartDate -> updateState { copy(startDate = intent.date) }
            is Intent.UpdateEndDate -> updateState { copy(endDate = intent.date) }
            is Intent.ToggleMultiMode -> updateState { copy(isMultiMode = !isMultiMode, result = null, multiResults = emptyList()) }
            is Intent.GenerateReport -> generateReport()
            is Intent.Refresh -> {
                // Clear error and reset for fresh load
                updateState { copy(error = null, initialLoadComplete = false) }
                // loadVehicles will trigger loadFleetOverview after vehicles load
                loadVehicles()
            }
            // Vehicle selector bottom sheet
            is Intent.ShowVehicleSelector -> updateState { copy(showVehicleSelector = true, isFleetOverviewMode = false) }
            is Intent.DismissVehicleSelector -> updateState {
                copy(
                    showVehicleSelector = false,
                    vehicleSearchQuery = ""
                )
            }
            // Search
            is Intent.UpdateVehicleSearch -> updateState { copy(vehicleSearchQuery = intent.query) }
            is Intent.ClearVehicleSearch -> updateState { copy(vehicleSearchQuery = "") }
            // Quick actions
            is Intent.QuickReportFromRecent -> quickReportFromRecent(intent.report)
            // Sorting and filtering
            is Intent.UpdateSortOption -> updateState { copy(sortOption = intent.option, showSortMenu = false) }
            is Intent.UpdatePLStatusFilter -> updateState { copy(plStatusFilter = intent.filter) }
            is Intent.ToggleSortMenu -> updateState { copy(showSortMenu = !showSortMenu) }
            is Intent.DismissSortMenu -> updateState { copy(showSortMenu = false) }
            // Vehicle filter sheet for fleet overview
            is Intent.ShowVehicleFilterSheet -> updateState {
                copy(
                    showVehicleFilterSheet = true,
                    tempSelectedVehicleIds = selectedVehicleIds.ifEmpty { vehicles.map { it.id }.toSet() },
                    vehicleSearchQuery = ""
                )
            }
            is Intent.DismissVehicleFilterSheet -> updateState {
                copy(showVehicleFilterSheet = false, vehicleSearchQuery = "")
            }
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
            is Intent.SelectAllVehiclesInFilter -> updateState {
                copy(tempSelectedVehicleIds = vehicles.map { it.id }.toSet())
            }
            is Intent.ClearVehicleFilter -> updateState { copy(tempSelectedVehicleIds = emptySet()) }
            is Intent.ApplyVehicleFilter -> applyVehicleFilter()
            // View mode
            is Intent.UpdateViewMode -> updateState { copy(viewMode = intent.mode) }
            is Intent.UpdateChartType -> updateState { copy(chartType = intent.type) }
            // Export
            is Intent.ShowExportOptions -> updateState { copy(showExportOptions = true) }
            is Intent.DismissExportOptions -> updateState { copy(showExportOptions = false) }
            is Intent.ExportReport -> exportReport(intent.format)
        }
    }

    /**
     * Initialize the screen in fleet overview mode.
     * This resets any previous single-vehicle selection state.
     */
    private suspend fun initFleetOverview() {
        log.d { "initFleetOverview: Resetting state to fleet overview mode" }

        // Reset to fleet overview mode, clearing any previous single-vehicle state
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

        // Now load vehicles (which will trigger loadFleetOverview after success)
        loadVehicles()
    }

    private suspend fun loadFleetOverview() {
        log.d { "loadFleetOverview: Starting - vehicles=${currentState.vehicles.size}, isLoadingVehicles=${currentState.isLoadingVehicles}" }

        // Don't try to load if no vehicles available yet - vehicles will trigger this when loaded
        if (currentState.vehicles.isEmpty() && !currentState.isLoadingVehicles) {
            log.d { "loadFleetOverview: No vehicles, returning early" }
            updateState {
                copy(
                    isLoading = false,
                    isFleetOverviewMode = true,
                    initialLoadComplete = true,
                    error = null
                )
            }
            return
        }

        // Still loading vehicles - wait for vehicles to load first
        if (currentState.isLoadingVehicles) {
            log.d { "loadFleetOverview: Still loading vehicles, returning" }
            return
        }

        updateState { copy(isLoading = true, error = null, isFleetOverviewMode = true) }

        // Get vehicle IDs to filter by (empty means all vehicles)
        val selectedIds: List<Int> = currentState.selectedVehicleIds.mapNotNull { it.toIntOrNull() }
        val allVehicleIds: List<Int> = currentState.vehicles.mapNotNull { it.id.toIntOrNull() }
        val vehicleIdsToUse: List<Int> = if (selectedIds.isEmpty()) allVehicleIds else selectedIds

        log.d { "loadFleetOverview: vehicleIdsToUse=$vehicleIdsToUse" }

        // Don't make API call with empty vehicle IDs - show empty state instead
        if (vehicleIdsToUse.isEmpty()) {
            log.d { "loadFleetOverview: Empty vehicle IDs, showing empty state" }
            updateState {
                copy(
                    isLoading = false,
                    error = null,
                    multiResults = emptyList(),
                    initialLoadComplete = true,
                    currentPeriodLabel = getCurrentPeriodLabel()
                )
            }
            return
        }

        // Calculate date range based on period
        val (startDate, endDate) = getDateRangeForPeriod()
        log.d { "loadFleetOverview: Date range - startDate=$startDate, endDate=$endDate, period=${currentState.period}" }

        val request = MultiVehiclePLRequest(
            vehicleIds = vehicleIdsToUse,
            startDate = startDate,
            endDate = endDate
        )

        log.d { "loadFleetOverview: Calling getMultiVehiclePL with ${request.vehicleIds}" }

        when (val result = reportsRepository.getMultiVehiclePL(request)) {
            is Result.Success -> {
                updateState {
                    copy(
                        isLoading = false,
                        error = null,
                        multiResults = result.data,
                        initialLoadComplete = true,
                        currentPeriodLabel = getCurrentPeriodLabel()
                    )
                }
            }
            is Result.Error -> {
                updateState {
                    copy(
                        isLoading = false,
                        error = result.message ?: "Failed to load fleet overview",
                        initialLoadComplete = true
                    )
                }
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }

    private fun getCurrentPeriodLabel(): String {
        // Use kotlinx-datetime for actual date - current date is January 13, 2026
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val currentMonth = "January"
        val currentYear = "2026"
        val currentDay = "13"

        return when (currentState.period) {
            "today" -> "$currentMonth $currentDay, $currentYear"
            "weekly" -> "Week of ${currentMonth.take(3)} $currentDay, $currentYear"
            "monthly" -> "$currentMonth $currentYear"
            "yearly" -> currentYear
            "all" -> "All Time"
            "custom" -> {
                val start = currentState.startDate.ifBlank { "Start" }
                val end = currentState.endDate.ifBlank { "End" }
                "$start - $end"
            }
            else -> "$currentMonth $currentYear"
        }
    }

    /**
     * Calculate start and end dates based on the selected period.
     * Current date is January 13, 2026.
     * Returns dates in DD-MM-YYYY format as required by the API.
     */
    private fun getDateRangeForPeriod(): Pair<String, String> {
        // Current date: January 13, 2026
        val currentDay = 13
        val currentMonth = 1
        val currentYear = 2026

        // Helper function to format date as DD-MM-YYYY
        fun formatDate(day: Int, month: Int, year: Int): String {
            val dayStr = day.toString().padStart(2, '0')
            val monthStr = month.toString().padStart(2, '0')
            return "$dayStr-$monthStr-$year"
        }

        return when (currentState.period) {
            "today" -> {
                // Just today: 13-01-2026
                val dateStr = formatDate(currentDay, currentMonth, currentYear)
                Pair(dateStr, dateStr)
            }
            "weekly" -> {
                // Last 7 days: Jan 7, 2026 - Jan 13, 2026
                val startDay = currentDay - 6 // 7
                val startDateStr = formatDate(startDay, currentMonth, currentYear)
                val endDateStr = formatDate(currentDay, currentMonth, currentYear)
                Pair(startDateStr, endDateStr)
            }
            "monthly" -> {
                // Current month: Jan 1, 2026 - Jan 31, 2026
                val startDateStr = formatDate(1, currentMonth, currentYear)
                val endDateStr = formatDate(31, currentMonth, currentYear)
                Pair(startDateStr, endDateStr)
            }
            "yearly" -> {
                // Current year: Jan 1, 2026 - Dec 31, 2026
                val startDateStr = formatDate(1, 1, currentYear)
                val endDateStr = formatDate(31, 12, currentYear)
                Pair(startDateStr, endDateStr)
            }
            "all" -> {
                // All time: from year 2020 to now
                val startDateStr = formatDate(1, 1, 2020)
                val endDateStr = formatDate(currentDay, currentMonth, currentYear)
                Pair(startDateStr, endDateStr)
            }
            "custom" -> {
                // Use custom dates if provided, otherwise default to monthly
                val startDate = currentState.startDate.ifBlank {
                    formatDate(1, currentMonth, currentYear)
                }
                val endDate = currentState.endDate.ifBlank {
                    formatDate(31, currentMonth, currentYear)
                }
                Pair(startDate, endDate)
            }
            else -> {
                // Default to monthly
                val startDateStr = formatDate(1, currentMonth, currentYear)
                val endDateStr = formatDate(31, currentMonth, currentYear)
                Pair(startDateStr, endDateStr)
            }
        }
    }

    private suspend fun applyVehicleFilter() {
        updateState {
            copy(
                selectedVehicleIds = tempSelectedVehicleIds,
                showVehicleFilterSheet = false,
                vehicleSearchQuery = ""
            )
        }
        // Reload with new filter
        loadFleetOverview()
    }

    private suspend fun exportReport(format: ExportFormat) {
        updateState { copy(isGeneratingExport = true, showExportOptions = false) }

        // TODO: Implement actual export logic with a report generation service
        // For now, simulate export generation
        kotlinx.coroutines.delay(1500)

        val fileName = "Fleet_PL_Report_${currentState.currentPeriodLabel.replace(" ", "_")}.${format.extension}"

        updateState { copy(isGeneratingExport = false) }
        sendEffect(Effect.ExportGenerated(fileName, format))
        sendEffect(Effect.ShowSnackbar("Report generated: $fileName"))
    }

    private fun selectVehicle(vehicleId: String) {
        val currentRecentIds = currentState.recentVehicleIds.toMutableList()
        currentRecentIds.remove(vehicleId)
        currentRecentIds.add(0, vehicleId)
        val updatedRecentIds = currentRecentIds.take(5)

        updateState {
            copy(
                selectedVehicleId = vehicleId,
                showVehicleSelector = false,
                vehicleSearchQuery = "",
                recentVehicleIds = updatedRecentIds,
                isFleetOverviewMode = false
            )
        }
    }

    private suspend fun quickReportFromRecent(report: RecentReport) {
        updateState {
            copy(
                selectedVehicleId = report.vehicleId,
                period = report.period,
                useCustomDateRange = report.period == "custom",
                isFleetOverviewMode = false
            )
        }
        generateReport()
    }

    private suspend fun loadVehicles() {
        log.d { "loadVehicles: Starting" }
        updateState { copy(isLoadingVehicles = true) }

        vehicleRepository.getVehicles().collectLatest { result ->
            when (result) {
                is Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val vehicles = result.data as? List<com.ijs.vehicle.domain.entity.Vehicle> ?: emptyList()
                    log.d { "loadVehicles: Success - ${vehicles.size} vehicles loaded" }
                    updateState { copy(isLoadingVehicles = false, vehicles = vehicles) }

                    // Log current state for debugging
                    log.d { "loadVehicles: After update - isFleetOverviewMode=${currentState.isFleetOverviewMode}, initialLoadComplete=${currentState.initialLoadComplete}, selectedVehicleId=${currentState.selectedVehicleId}" }

                    // Auto-load data after vehicles are loaded
                    if (!currentState.initialLoadComplete) {
                        if (currentState.isFleetOverviewMode) {
                            log.d { "loadVehicles: Calling loadFleetOverview" }
                            loadFleetOverview()
                        } else if (currentState.selectedVehicleId != null) {
                            log.d { "loadVehicles: Calling generateReport for ${currentState.selectedVehicleId}" }
                            generateReport()
                        } else {
                            log.d { "loadVehicles: No specific mode, marking complete" }
                            // No specific mode, just mark as complete
                            updateState { copy(initialLoadComplete = true) }
                        }
                    } else {
                        log.d { "loadVehicles: initialLoadComplete is already true, skipping auto-load" }
                    }
                }
                is Result.Error -> {
                    log.e { "loadVehicles: Error - ${result.message}" }
                    updateState {
                        copy(
                            isLoadingVehicles = false,
                            initialLoadComplete = true,
                            error = result.message ?: "Failed to load vehicles"
                        )
                    }
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    private fun toggleVehicle(vehicleId: String) {
        updateState {
            val newSelection = if (selectedVehicleIds.contains(vehicleId)) {
                selectedVehicleIds - vehicleId
            } else {
                selectedVehicleIds + vehicleId
            }
            copy(selectedVehicleIds = newSelection)
        }
    }

    private suspend fun generateReport() {
        val currentState = state.value

        if (currentState.isMultiMode) {
            if (currentState.selectedVehicleIds.isEmpty()) {
                sendEffect(Effect.ShowSnackbar("Please select at least one vehicle"))
                return
            }
            generateMultiVehicleReport()
        } else {
            if (currentState.selectedVehicleId == null) {
                sendEffect(Effect.ShowSnackbar("Please select a vehicle"))
                return
            }
            generateSingleVehicleReport()
        }
    }

    private suspend fun generateSingleVehicleReport() {
        val currentState = state.value
        updateState { copy(isLoading = true, error = null) }

        val vehicleId = currentState.selectedVehicleId?.toIntOrNull()
        if (vehicleId == null) {
            updateState { copy(isLoading = false, error = "Invalid vehicle ID") }
            return
        }

        when (val result = reportsRepository.getVehicleProfitLoss(vehicleId, currentState.period)) {
            is Result.Success -> {
                val plResult = result.data
                val vehicle = currentState.selectedVehicle
                if (vehicle != null && plResult != null) {
                    val recentReport = RecentReport(
                        vehicleId = vehicle.id,
                        vehicleNumber = vehicle.registrationNumber,
                        vehicleMakeModel = "${vehicle.make ?: ""} ${vehicle.model ?: ""}".trim(),
                        period = currentState.period,
                        profitLoss = plResult.netProfit,
                        isProfit = plResult.netProfit >= 0,
                        generatedAt = currentTimeMillis()
                    )
                    val updatedReports = listOf(recentReport) + currentState.recentReports
                        .filter { it.vehicleId != vehicle.id || it.period != currentState.period }
                        .take(9)
                    updateState { copy(isLoading = false, result = plResult, recentReports = updatedReports) }
                } else {
                    updateState { copy(isLoading = false, result = plResult) }
                }
            }
            is Result.Error -> {
                updateState { copy(isLoading = false, error = result.message ?: "Failed to generate report") }
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }

    private suspend fun generateMultiVehicleReport() {
        val currentState = state.value
        updateState { copy(isLoading = true, error = null) }

        val vehicleIds = currentState.selectedVehicleIds.mapNotNull { it.toIntOrNull() }
        if (vehicleIds.isEmpty()) {
            updateState { copy(isLoading = false, error = "No valid vehicle IDs") }
            return
        }

        val request = MultiVehiclePLRequest(
            vehicleIds = vehicleIds,
            startDate = currentState.startDate.takeIf { it.isNotBlank() },
            endDate = currentState.endDate.takeIf { it.isNotBlank() }
        )

        when (val result = reportsRepository.getMultiVehiclePL(request)) {
            is Result.Success -> {
                updateState { copy(isLoading = false, multiResults = result.data) }
            }
            is Result.Error -> {
                updateState { copy(isLoading = false, error = result.message ?: "Failed to generate report") }
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }
}
