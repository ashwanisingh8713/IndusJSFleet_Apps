package com.ijs.reports.presentation.consolidated

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText
import com.ijs.reports.domain.entity.ConsolidatedPL
import com.ijs.vehicle.domain.entity.Vehicle

/**
 * MVI Contract for Consolidated P&L Screen
 */
object ConsolidatedPLContract {

    data class State(
        val isLoading: Boolean = false,
        val isLoadingVehicles: Boolean = false,
        val error: UiText? = null,
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

    /**
     * Structured cost IDs (from trip_costs.json & vehicle_maintenance_costs.json).
     * API expects these in `cost_ids` array, NOT legacy names like "fuel", "toll".
     */
    val COST_TYPES = listOf(
        "TC-001-002",  // Diesel
        "TC-002-001",  // Toll Charges
        "TC-004-001",  // Driver Allowance
        "TC-002-002",  // Parking Fees
        "TC-003-001",  // Loading Charges
        "TC-003-002",  // Unloading Charges
        "TC-005-004",  // Chalan / Fine
        "TC-005-001",  // State Permit
        "VMC-002-003", // Tyres Replacement
        "VMC-002-002", // Battery Replacement
        "VMC-001-001", // Engine Oil Change
        "TC-006-004"   // Other
    )

    /** Display labels for structured cost IDs. */
    val COST_ID_LABELS = mapOf(
        "TC-001-001" to "Petrol",
        "TC-001-002" to "Diesel",
        "TC-001-003" to "CNG / LPG",
        "TC-001-004" to "EV Charging",
        "TC-002-001" to "Toll Charges",
        "TC-002-002" to "Parking Fees",
        "TC-002-003" to "Entry Charges",
        "TC-003-001" to "Loading Charges",
        "TC-003-002" to "Unloading Charges",
        "TC-003-003" to "Crane / Forklift",
        "TC-003-004" to "Labor Charges",
        "TC-004-001" to "Driver Allowance",
        "TC-004-002" to "Driver Food",
        "TC-004-003" to "Driver Accommodation",
        "TC-005-001" to "State Permit",
        "TC-005-002" to "National Permit",
        "TC-005-003" to "Special Permit",
        "TC-005-004" to "Chalan / Fine",
        "TC-006-001" to "Police / RTO",
        "TC-006-002" to "Weighbridge",
        "TC-006-003" to "Commission / Brokerage",
        "TC-006-004" to "Other",
        "VMC-001-001" to "Engine Oil Change",
        "VMC-001-002" to "Oil Filter",
        "VMC-001-003" to "Air Filter",
        "VMC-001-004" to "Wheel Alignment",
        "VMC-001-005" to "General Servicing",
        "VMC-002-001" to "Brake Pads / Discs",
        "VMC-002-002" to "Battery Replacement",
        "VMC-002-003" to "Tyres Replacement",
        "VMC-002-004" to "Clutch Repair",
        "VMC-002-005" to "Suspension Repair"
    )

    /** Get display label for a cost ID. Falls back to the raw ID if not found. */
    fun costIdLabel(costId: String): String = COST_ID_LABELS[costId] ?: costId

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
        data class ShowSnackbar(val message: UiText) : Effect
    }
}

