package com.ijs.vehicle.presentation

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.error.result.Result
import com.ijs.vehicle.domain.entity.VehicleStatus
import com.ijs.vehicle.domain.usecase.DeleteVehicleUseCase
import com.ijs.vehicle.domain.usecase.GetVehiclesUseCase
import com.ijs.vehicle.presentation.VehiclesContract.Effect
import com.ijs.vehicle.presentation.VehiclesContract.Intent
import com.ijs.vehicle.presentation.VehiclesContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Vehicles screen implementing MVI pattern.
 *
 * Dependencies are provided via DefaultViewModelProvider.
 */
@Inject
class VehiclesViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        sendIntent(Intent.LoadVehicles)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadVehicles -> loadVehicles()
            is Intent.RefreshVehicles -> refreshVehicles()
            is Intent.SearchVehicles -> searchVehicles(intent.query)
            is Intent.FilterByStatus -> filterByStatus(intent.status)
            is Intent.SelectVehicle -> sendEffect(Effect.NavigateToVehicleDetail(intent.vehicleId))
            is Intent.DeleteVehicle -> updateState {
                copy(vehicleToDelete = intent.vehicleId, showDeleteConfirmation = true)
            }
            is Intent.ConfirmDelete -> {
                val id = currentState.vehicleToDelete
                updateState { copy(showDeleteConfirmation = false, vehicleToDelete = null) }
                if (id != null) deleteVehicle(id)
            }
            is Intent.DismissDelete -> updateState {
                copy(showDeleteConfirmation = false, vehicleToDelete = null)
            }
            is Intent.AddVehicle -> sendEffect(Effect.NavigateToAddVehicle)
            is Intent.ClearFilters -> clearFilters()
        }
    }

    private suspend fun loadVehicles() {
        withContext(dispatcherProvider.io) {
            getVehiclesUseCase().collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        updateState { copy(isLoading = true, error = null) }
                    }
                    is Result.Success -> {
                        updateState {
                            copy(
                                isLoading = false,
                                vehicles = result.data,
                                filteredVehicles = result.data,
                                error = null
                            )
                        }
                        applyFilters()
                    }
                    is Result.Error -> {
                        updateState {
                            copy(
                                isLoading = false,
                                error = result.message ?: "Failed to load vehicles"
                            )
                        }
                        sendEffect(Effect.ShowSnackbar(result.message ?: "Failed to load vehicles"))
                    }
                }
            }
        }
    }

    private suspend fun refreshVehicles() {
        updateState { copy(isRefreshing = true) }
        loadVehicles()
        updateState { copy(isRefreshing = false) }
    }

    private fun searchVehicles(query: String) {
        updateState { copy(searchQuery = query) }
        applyFilters()
    }

    private fun filterByStatus(status: VehicleStatus?) {
        updateState { copy(selectedStatusFilter = status) }
        applyFilters()
    }

    private fun clearFilters() {
        updateState {
            copy(
                searchQuery = "",
                selectedStatusFilter = null,
                filteredVehicles = vehicles
            )
        }
    }

    private fun applyFilters() {
        val currentState = currentState
        val filtered = currentState.vehicles.filter { vehicle ->
            val matchesSearch = currentState.searchQuery.isEmpty() ||
                    vehicle.registrationNumber.contains(currentState.searchQuery, ignoreCase = true) ||
                    vehicle.make.contains(currentState.searchQuery, ignoreCase = true) ||
                    vehicle.model.contains(currentState.searchQuery, ignoreCase = true)

            val matchesStatus = currentState.selectedStatusFilter == null ||
                    vehicle.status == currentState.selectedStatusFilter

            matchesSearch && matchesStatus
        }
        updateState { copy(filteredVehicles = filtered) }
    }

    private suspend fun deleteVehicle(vehicleId: String) {
        withContext(dispatcherProvider.io) {
            val result = deleteVehicleUseCase(vehicleId)
            when (result) {
                is Result.Success -> {
                    updateState {
                        val updatedVehicles = vehicles.filter { it.id != vehicleId }
                        copy(
                            vehicles = updatedVehicles,
                            filteredVehicles = updatedVehicles
                        )
                    }
                    applyFilters()
                    sendEffect(Effect.ShowSnackbar("Vehicle deleted successfully"))
                }
                is Result.Error -> {
                    sendEffect(Effect.ShowSnackbar(result.message ?: "Failed to delete vehicle"))
                }
                is Result.Loading -> { /* Not applicable for suspend function */ }
            }
        }
    }
}

