package com.indusjs.fleet.presentation.costs

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.data.model.costs.TripCostDto
import com.indusjs.fleet.data.model.costs.TripCostTypes
import com.indusjs.fleet.data.model.costs.FuelTypes
import com.indusjs.fleet.domain.entity.trip.Trip

/**
 * Represents a single cost entry row.
 */
data class CostEntryRow(
    val id: String = kotlin.random.Random.nextLong().toString(),
    val costType: String = "",
    val costTypeLabel: String = "",
    val date: String = "",
    val time: String = "",
    val amount: String = "",
    val notes: String = "",
    val customCostTypeName: String = "",
    // Fuel specific
    val fuelType: String = "diesel",
    val fuelQuantity: String = "",
    val fuelRate: String = "",
    val kmPerLiter: String = "",
    // Validation
    val costTypeError: String? = null,
    val dateError: String? = null,
    val amountError: String? = null,
    // Expanded state
    val isExpanded: Boolean = true,
    val showCostTypeDropdown: Boolean = false,
    val showFuelTypeDropdown: Boolean = false
) {
    val isFuelCostType: Boolean get() = costType == "fuel"
    val isOtherCostType: Boolean get() = costType == "other"

    val isValid: Boolean
        get() = costType.isNotBlank() &&
                date.isNotBlank() &&
                amount.isNotBlank() &&
                amount.toDoubleOrNull() != null &&
                (amount.toDoubleOrNull() ?: 0.0) > 0 &&
                costTypeError == null &&
                dateError == null &&
                amountError == null
}

/**
 * MVI Contract for Trip Cost Entry Screen.
 */
object TripCostEntryContract {

    /**
     * UI State for Trip Cost Entry screen.
     */
    data class State(
        // Trip selection
        val trips: List<Trip> = emptyList(),
        val selectedTrip: Trip? = null,
        val showTripDropdown: Boolean = false,
        val tripError: String? = null,

        // Multi-row cost entries
        val costEntries: List<CostEntryRow> = listOf(CostEntryRow()),

        // Cost history
        val costHistory: List<TripCostDto> = emptyList(),
        val isLoadingHistory: Boolean = false,
        val showHistoryDialog: Boolean = false,

        // Form state
        val isLoading: Boolean = false,
        val isLoadingData: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,

        // Available options
        val costTypeOptions: List<Pair<String, String>> = TripCostTypes.types,
        val fuelTypeOptions: List<Pair<String, String>> = FuelTypes.types
    ) : UiState {

        val hasValidEntries: Boolean
            get() = costEntries.any { it.isValid }

        val canSave: Boolean
            get() = selectedTrip != null && hasValidEntries && !isSaving

        val tripInfo: String
            get() = selectedTrip?.let {
                val start = it.startLocation?.address ?: "Unknown"
                val end = it.endLocation?.address ?: "Unknown"
                "$start → $end"
            } ?: ""

        val vehicleNumber: String
            get() = selectedTrip?.vehicleNumber ?: ""
    }

    /**
     * User intents for Trip Cost Entry screen.
     */
    sealed interface Intent : UiIntent {
        // Load data
        data object LoadTrips : Intent

        // Trip selection
        data class SelectTrip(val trip: Trip) : Intent
        data object ToggleTripDropdown : Intent

        // Cost entry row management
        data object AddCostRow : Intent
        data class RemoveCostRow(val rowId: String) : Intent
        data class ToggleRowExpanded(val rowId: String) : Intent

        // Cost entry field updates
        data class SelectCostType(val rowId: String, val costType: String, val label: String) : Intent
        data class ToggleCostTypeDropdown(val rowId: String) : Intent
        data class UpdateDate(val rowId: String, val value: String) : Intent
        data class UpdateTime(val rowId: String, val value: String) : Intent
        data class UpdateAmount(val rowId: String, val value: String) : Intent
        data class UpdateNotes(val rowId: String, val value: String) : Intent
        data class UpdateCustomCostTypeName(val rowId: String, val value: String) : Intent

        // Fuel specific
        data class SelectFuelType(val rowId: String, val fuelType: String) : Intent
        data class ToggleFuelTypeDropdown(val rowId: String) : Intent
        data class UpdateFuelQuantity(val rowId: String, val value: String) : Intent
        data class UpdateFuelRate(val rowId: String, val value: String) : Intent
        data class UpdateKmPerLiter(val rowId: String, val value: String) : Intent

        // History
        data object LoadCostHistory : Intent
        data object ShowHistory : Intent
        data object HideHistory : Intent

        // Actions
        data object SaveCosts : Intent
        data object NavigateBack : Intent
        data object ClearError : Intent
    }

    /**
     * Side effects for Trip Cost Entry screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data class ShowError(val message: String) : Effect
        data object NavigateBack : Effect
        data class CostsSaved(val count: Int) : Effect
    }
}

