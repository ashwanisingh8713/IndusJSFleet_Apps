package com.indusjs.fleet.presentation.reports.vehicle

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.reports.VehicleProfitLoss
import com.indusjs.fleet.domain.entity.vehicle.Vehicle

/**
 * MVI Contract for Vehicle P&L Screen
 */
object VehiclePLContract {

    data class State(
        val isLoading: Boolean = false,
        val isLoadingVehicles: Boolean = false,
        val error: String? = null,
        val vehicles: List<Vehicle> = emptyList(),
        val selectedVehicleId: String? = null,
        val period: String = "monthly",
        val startDate: String = "",
        val endDate: String = "",
        val result: VehicleProfitLoss? = null,
        val multiResults: List<VehicleProfitLoss> = emptyList(),
        val selectedVehicleIds: Set<String> = emptySet(),
        val isMultiMode: Boolean = false,
        // Search functionality for scalability (150+ vehicles)
        val vehicleSearchQuery: String = "",
        val showVehicleDropdown: Boolean = false,
        // Custom date range mode
        val useCustomDateRange: Boolean = false
    ) : UiState {
        val hasResult: Boolean get() = result != null || multiResults.isNotEmpty()
        val selectedVehicle: Vehicle? get() = vehicles.find { it.id == selectedVehicleId }

        // Filtered vehicles based on search query
        val filteredVehicles: List<Vehicle> get() {
            if (vehicleSearchQuery.isBlank()) return vehicles
            val query = vehicleSearchQuery.lowercase()
            return vehicles.filter {
                it.registrationNumber.lowercase().contains(query) ||
                (it.make?.lowercase()?.contains(query) == true) ||
                (it.model?.lowercase()?.contains(query) == true)
            }
        }
    }

    val PERIOD_OPTIONS = listOf("today", "weekly", "monthly", "yearly", "custom")

    sealed interface Intent : UiIntent {
        data object LoadVehicles : Intent
        data class SelectVehicle(val vehicleId: String) : Intent
        data class ToggleVehicle(val vehicleId: String) : Intent
        data object SelectAllVehicles : Intent
        data object ClearVehicles : Intent
        data class UpdatePeriod(val period: String) : Intent
        data class UpdateStartDate(val date: String) : Intent
        data class UpdateEndDate(val date: String) : Intent
        data object ToggleMultiMode : Intent
        data object GenerateReport : Intent
        data object Refresh : Intent
        // Search intents
        data class UpdateVehicleSearch(val query: String) : Intent
        data object ToggleVehicleDropdown : Intent
        data object ClearVehicleSearch : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
    }
}

