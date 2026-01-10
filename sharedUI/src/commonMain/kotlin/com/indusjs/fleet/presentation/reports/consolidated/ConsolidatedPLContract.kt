package com.indusjs.fleet.presentation.reports.consolidated

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.reports.ConsolidatedPL
import com.indusjs.fleet.domain.entity.vehicle.Vehicle

/**
 * MVI Contract for Consolidated P&L Screen
 */
object ConsolidatedPLContract {

    data class State(
        val isLoading: Boolean = false,
        val isLoadingVehicles: Boolean = false,
        val error: String? = null,
        val vehicles: List<Vehicle> = emptyList(),
        val selectedVehicleIds: Set<String> = emptySet(),
        val selectedCostTypes: Set<String> = emptySet(),
        val startDate: String = "",
        val endDate: String = "",
        val groupBy: String = "month", // month, week, day
        val result: ConsolidatedPL? = null
    ) : UiState {
        val hasResult: Boolean get() = result != null
    }

    val GROUP_BY_OPTIONS = listOf("day", "week", "month")

    val COST_TYPES = listOf(
        "fuel", "toll", "driver_allowance", "parking",
        "loading_charges", "unloading_charges", "chalan",
        "permit", "insurance", "maintenance", "other"
    )

    sealed interface Intent : UiIntent {
        data object LoadVehicles : Intent
        data class ToggleVehicle(val vehicleId: String) : Intent
        data object SelectAllVehicles : Intent
        data object ClearVehicles : Intent
        data class ToggleCostType(val costType: String) : Intent
        data object SelectAllCostTypes : Intent
        data object ClearCostTypes : Intent
        data class UpdateStartDate(val date: String) : Intent
        data class UpdateEndDate(val date: String) : Intent
        data class UpdateGroupBy(val groupBy: String) : Intent
        data object GenerateReport : Intent
        data object Refresh : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
    }
}

