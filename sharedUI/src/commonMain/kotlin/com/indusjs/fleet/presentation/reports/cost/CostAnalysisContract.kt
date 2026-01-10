package com.indusjs.fleet.presentation.reports.cost

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.reports.CostTypeAnalysis

/**
 * MVI Contract for Cost Analysis Screen
 */
object CostAnalysisContract {

    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val selectedCostTypes: Set<String> = emptySet(),
        val startDate: String = "",
        val endDate: String = "",
        val results: List<CostTypeAnalysis> = emptyList()
    ) : UiState {
        val hasResults: Boolean get() = results.isNotEmpty()
        val totalAmount: Double get() = results.sumOf { it.totalAmount }
        val totalCount: Int get() = results.sumOf { it.totalCount }
    }

    val COST_TYPES = listOf(
        "fuel" to "Fuel",
        "toll" to "Toll",
        "driver_allowance" to "Driver Allowance",
        "parking" to "Parking",
        "loading_charges" to "Loading Charges",
        "unloading_charges" to "Unloading Charges",
        "chalan" to "Chalan",
        "permit" to "Permit",
        "insurance" to "Insurance",
        "tyre" to "Tyre",
        "battery" to "Battery",
        "servicing" to "Servicing",
        "engine_repair" to "Engine Repair",
        "body_repair" to "Body Repair",
        "electrical" to "Electrical",
        "ac_repair" to "AC Repair",
        "other" to "Other"
    )

    sealed interface Intent : UiIntent {
        data class ToggleCostType(val costType: String) : Intent
        data object SelectAllCostTypes : Intent
        data object ClearCostTypes : Intent
        data class UpdateStartDate(val date: String) : Intent
        data class UpdateEndDate(val date: String) : Intent
        data object GenerateReport : Intent
        data object Refresh : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
    }
}

