package com.ijs.reports.presentation.consolidated

import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.ijs.reports.data.model.ConsolidatedPLRequest
import com.ijs.reports.domain.usecase.GetConsolidatedPLUseCase
import com.ijs.vehicle.domain.repository.VehicleRepository
import com.ijs.reports.presentation.consolidated.ConsolidatedPLContract.COST_TYPES
import com.ijs.reports.presentation.consolidated.ConsolidatedPLContract.Effect
import com.ijs.reports.presentation.consolidated.ConsolidatedPLContract.Intent
import com.ijs.reports.presentation.consolidated.ConsolidatedPLContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.collectLatest

/**
 * ViewModel for Consolidated P&L Screen
 */
@Inject
class ConsolidatedPLViewModel(
    private val getConsolidatedPLUseCase: GetConsolidatedPLUseCase,
    private val vehicleRepository: VehicleRepository
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        sendIntent(Intent.LoadVehicles)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadVehicles -> loadVehicles()
            is Intent.ToggleVehicle -> toggleVehicle(intent.vehicleId)
            is Intent.SelectAllVehicles -> updateState { copy(selectedVehicleIds = vehicles.map { it.id }.toSet()) }
            is Intent.ClearVehicles -> updateState { copy(selectedVehicleIds = emptySet()) }
            is Intent.ToggleCostType -> toggleCostType(intent.costType)
            is Intent.SelectAllCostTypes -> updateState { copy(selectedCostTypes = COST_TYPES.toSet()) }
            is Intent.ClearCostTypes -> updateState { copy(selectedCostTypes = emptySet()) }
            is Intent.UpdateStartDate -> updateState { copy(startDate = intent.date) }
            is Intent.UpdateEndDate -> updateState { copy(endDate = intent.date) }
            is Intent.UpdateGroupBy -> updateState { copy(groupBy = intent.groupBy) }
            is Intent.GenerateReport -> generateReport()
            is Intent.Refresh -> {
                loadVehicles()
                generateReport()
            }
        }
    }

    private suspend fun loadVehicles() {
        updateState { copy(isLoadingVehicles = true) }

        vehicleRepository.getVehicles().collectLatest { result ->
            when (result) {
                is Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val vehicles = result.data as? List<com.ijs.vehicle.domain.entity.Vehicle> ?: emptyList()
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

    private fun toggleCostType(costType: String) {
        updateState {
            val newSelection = if (selectedCostTypes.contains(costType)) {
                selectedCostTypes - costType
            } else {
                selectedCostTypes + costType
            }
            copy(selectedCostTypes = newSelection)
        }
    }

    private suspend fun generateReport() {
        val currentState = state.value

        if (currentState.startDate.isBlank() || currentState.endDate.isBlank()) {
            sendEffect(Effect.ShowSnackbar("Please select date range"))
            return
        }

        updateState { copy(isLoading = true, error = null) }

        val vehicleIds = currentState.selectedVehicleIds.mapNotNull { it.toIntOrNull() }

        val request = ConsolidatedPLRequest(
            vehicleIds = vehicleIds.takeIf { it.isNotEmpty() },
            costTypes = currentState.selectedCostTypes.toList().takeIf { it.isNotEmpty() },
            startDate = currentState.startDate,
            endDate = currentState.endDate,
            groupBy = currentState.groupBy
        )

        when (val result = getConsolidatedPLUseCase(request)) {
            is Result.Success -> {
                updateState { copy(isLoading = false, result = result.data) }
            }
            is Result.Error -> {
                updateState { copy(isLoading = false, error = result.message ?: "Failed to generate report") }
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }
}

