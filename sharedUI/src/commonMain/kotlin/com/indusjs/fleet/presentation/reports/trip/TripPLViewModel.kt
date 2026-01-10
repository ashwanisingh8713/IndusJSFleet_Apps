package com.indusjs.fleet.presentation.reports.trip

import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.data.model.reports.MultiTripPLRequest
import com.indusjs.fleet.domain.repository.reports.ReportsRepository
import com.indusjs.fleet.domain.repository.vehicle.VehicleRepository
import com.indusjs.fleet.presentation.reports.trip.TripPLContract.Effect
import com.indusjs.fleet.presentation.reports.trip.TripPLContract.Intent
import com.indusjs.fleet.presentation.reports.trip.TripPLContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.collectLatest

/**
 * ViewModel for Trip P&L Screen
 */
@Inject
class TripPLViewModel(
    private val reportsRepository: ReportsRepository,
    private val vehicleRepository: VehicleRepository
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        sendIntent(Intent.LoadVehicles)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadVehicles -> loadVehicles()
            is Intent.SelectVehicle -> updateState { copy(selectedVehicleId = intent.vehicleId) }
            is Intent.UpdateStartDate -> updateState { copy(startDate = intent.date) }
            is Intent.UpdateEndDate -> updateState { copy(endDate = intent.date) }
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

    private suspend fun generateReport() {
        val currentState = state.value

        if (currentState.selectedVehicleId == null) {
            sendEffect(Effect.ShowSnackbar("Please select a vehicle"))
            return
        }

        val vehicleId = currentState.selectedVehicleId.toIntOrNull()
        if (vehicleId == null) {
            sendEffect(Effect.ShowSnackbar("Invalid vehicle ID"))
            return
        }

        updateState { copy(isLoading = true, error = null) }

        val request = MultiTripPLRequest(
            vehicleId = vehicleId,
            startDate = currentState.startDate.takeIf { it.isNotBlank() },
            endDate = currentState.endDate.takeIf { it.isNotBlank() }
        )

        when (val result = reportsRepository.getMultiTripPL(request)) {
            is Result.Success -> {
                updateState { copy(isLoading = false, results = result.data) }
            }
            is Result.Error -> {
                updateState { copy(isLoading = false, error = result.message ?: "Failed to generate report") }
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }
}

