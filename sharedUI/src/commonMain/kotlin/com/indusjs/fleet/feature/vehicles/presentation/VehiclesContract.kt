package com.indusjs.fleet.feature.vehicles.presentation

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.feature.vehicles.domain.entity.Vehicle
import com.indusjs.fleet.feature.vehicles.domain.entity.VehicleStatus

/**
 * MVI Contract for the Vehicles List screen.
 */
object VehiclesContract {

    /**
     * UI State for the Vehicles screen.
     */
    data class State(
        val isLoading: Boolean = false,
        val vehicles: List<Vehicle> = emptyList(),
        val filteredVehicles: List<Vehicle> = emptyList(),
        val error: String? = null,
        val searchQuery: String = "",
        val selectedStatusFilter: VehicleStatus? = null,
        val isRefreshing: Boolean = false
    ) : UiState

    /**
     * User intents for the Vehicles screen.
     */
    sealed interface Intent : UiIntent {
        data object LoadVehicles : Intent
        data object RefreshVehicles : Intent
        data class SearchVehicles(val query: String) : Intent
        data class FilterByStatus(val status: VehicleStatus?) : Intent
        data class SelectVehicle(val vehicleId: String) : Intent
        data class DeleteVehicle(val vehicleId: String) : Intent
        data object AddVehicle : Intent
        data object ClearFilters : Intent
    }

    /**
     * Side effects for the Vehicles screen.
     */
    sealed interface Effect : UiEffect {
        data class NavigateToVehicleDetail(val vehicleId: String) : Effect
        data object NavigateToAddVehicle : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}

