package com.ijs.reports.presentation.cost

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.CostTypeGroup
import com.indusjs.uicomponents.components.UiText
import com.indusjs.fleet.data.model.costs.MaintenanceCostTypes
import com.indusjs.fleet.data.model.costs.TripCostTypes
import com.ijs.reports.domain.entity.CostTypeAnalysis

/**
 * MVI Contract for Cost Analysis Screen
 */
object CostAnalysisContract {

    data class State(
        val isLoading: Boolean = false,
        val error: UiText? = null,
        val selectedCostTypes: Set<String> = emptySet(),
        val startDate: String = "",
        val endDate: String = "",
        val results: List<CostTypeAnalysis> = emptyList(),
        // True once a report has completed at least once (drives the empty state).
        val hasGenerated: Boolean = false,
        // Dynamic cost types from database
        val tripCostTypeGroups: List<CostTypeGroup> = emptyList(),
        val maintenanceCostTypeGroups: List<CostTypeGroup> = emptyList(),
        val isLoadingCostTypes: Boolean = false
    ) : UiState {
        val hasResults: Boolean get() = results.isNotEmpty()
        val totalAmount: Double get() = results.sumOf { it.totalAmount }
        val totalCount: Int get() = results.sumOf { it.totalCount }

        /**
         * Get all cost types as flat list - prefers dynamic types, falls back to static.
         */
        val allCostTypes: List<Pair<String, String>>
            get() {
                val tripTypes = if (tripCostTypeGroups.isNotEmpty()) {
                    tripCostTypeGroups.flatMap { group -> group.items.map { it.id to it.label } }
                } else {
                    TripCostTypes.types
                }
                val maintenanceTypes = if (maintenanceCostTypeGroups.isNotEmpty()) {
                    maintenanceCostTypeGroups.flatMap { group -> group.items.map { it.id to it.label } }
                } else {
                    MaintenanceCostTypes.types
                }
                return tripTypes + maintenanceTypes
            }
    }

    /**
     * Fallback static COST_TYPES - combines trip and maintenance cost types.
     * Prefer using State.allCostTypes which uses dynamic types when available.
     */
    val COST_TYPES: List<Pair<String, String>>
        get() = TripCostTypes.types + MaintenanceCostTypes.types

    sealed interface Intent : UiIntent {
        data object LoadCostTypes : Intent
        data class ToggleCostType(val costType: String) : Intent
        data object SelectAllCostTypes : Intent
        data object ClearCostTypes : Intent
        data class UpdateStartDate(val date: String) : Intent
        data class UpdateEndDate(val date: String) : Intent
        data object GenerateReport : Intent
        data object Refresh : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: UiText) : Effect
    }
}

