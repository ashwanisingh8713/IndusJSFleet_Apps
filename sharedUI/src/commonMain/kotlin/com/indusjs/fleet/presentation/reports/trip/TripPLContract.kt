package com.indusjs.fleet.presentation.reports.trip

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.reports.TripProfitLoss
import com.indusjs.fleet.domain.entity.vehicle.Vehicle

/**
 * MVI Contract for Trip P&L Screen
 */
object TripPLContract {

    data class State(
        val isLoading: Boolean = false,
        val isLoadingVehicles: Boolean = false,
        val error: String? = null,
        val vehicles: List<Vehicle> = emptyList(),
        val selectedVehicleId: String? = null,
        val startDate: String = "",
        val endDate: String = "",
        val results: List<TripProfitLoss> = emptyList()
    ) : UiState {
        val hasResults: Boolean get() = results.isNotEmpty()
        val selectedVehicle: Vehicle? get() = vehicles.find { it.id == selectedVehicleId }
    }

    sealed interface Intent : UiIntent {
        data object LoadVehicles : Intent
        data class SelectVehicle(val vehicleId: String) : Intent
        data class UpdateStartDate(val date: String) : Intent
        data class UpdateEndDate(val date: String) : Intent
        data object GenerateReport : Intent
        data object Refresh : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
    }
}

