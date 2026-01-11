package com.indusjs.fleet.presentation.reports.vehicle

import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.util.currentTimeMillis
import com.indusjs.fleet.data.model.reports.MultiVehiclePLRequest
import com.indusjs.fleet.domain.repository.reports.ReportsRepository
import com.indusjs.fleet.domain.repository.vehicle.VehicleRepository
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLContract.Effect
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLContract.Intent
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLContract.RecentReport
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.collectLatest

/**
 * ViewModel for Vehicle P&L Screen - Enhanced wizard-like flow
 */
@Inject
class VehiclePLViewModel(
    private val reportsRepository: ReportsRepository,
    private val vehicleRepository: VehicleRepository
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        sendIntent(Intent.LoadVehicles)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadVehicles -> loadVehicles()
            is Intent.SelectVehicle -> selectVehicle(intent.vehicleId)
            is Intent.ToggleVehicle -> toggleVehicle(intent.vehicleId)
            is Intent.SelectAllVehicles -> updateState { copy(selectedVehicleIds = vehicles.map { it.id }.toSet()) }
            is Intent.ClearVehicles -> updateState { copy(selectedVehicleIds = emptySet()) }
            is Intent.UpdatePeriod -> updateState {
                copy(
                    period = intent.period,
                    useCustomDateRange = intent.period == "custom"
                )
            }
            is Intent.UpdateStartDate -> updateState { copy(startDate = intent.date) }
            is Intent.UpdateEndDate -> updateState { copy(endDate = intent.date) }
            is Intent.ToggleMultiMode -> updateState { copy(isMultiMode = !isMultiMode, result = null, multiResults = emptyList()) }
            is Intent.GenerateReport -> generateReport()
            is Intent.Refresh -> {
                loadVehicles()
                if (currentState.selectedVehicleId != null) {
                    generateReport()
                }
            }
            // Vehicle selector bottom sheet
            is Intent.ShowVehicleSelector -> updateState { copy(showVehicleSelector = true) }
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
        }
    }

    private fun selectVehicle(vehicleId: String) {
        val currentRecentIds = currentState.recentVehicleIds.toMutableList()
        // Add to front, remove duplicates, limit to 5
        currentRecentIds.remove(vehicleId)
        currentRecentIds.add(0, vehicleId)
        val updatedRecentIds = currentRecentIds.take(5)

        updateState {
            copy(
                selectedVehicleId = vehicleId,
                showVehicleSelector = false,
                vehicleSearchQuery = "",
                recentVehicleIds = updatedRecentIds
            )
        }
    }

    private suspend fun quickReportFromRecent(report: RecentReport) {
        // Set the vehicle and period from the recent report
        updateState {
            copy(
                selectedVehicleId = report.vehicleId,
                period = report.period,
                useCustomDateRange = report.period == "custom"
            )
        }
        // Auto-generate the report
        generateReport()
    }

    private suspend fun loadVehicles() {
        updateState { copy(isLoadingVehicles = true) }

        vehicleRepository.getVehicles().collectLatest { result ->
            when (result) {
                is Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val vehicles = result.data as? List<com.indusjs.fleet.domain.entity.vehicle.Vehicle> ?: emptyList()
                    updateState { copy(isLoadingVehicles = false, vehicles = vehicles) }
                }
                is Result.Error -> {
                    updateState { copy(isLoadingVehicles = false) }
                    sendEffect(Effect.ShowSnackbar(result.message ?: "Failed to load vehicles"))
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
                // Add to recent reports
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
