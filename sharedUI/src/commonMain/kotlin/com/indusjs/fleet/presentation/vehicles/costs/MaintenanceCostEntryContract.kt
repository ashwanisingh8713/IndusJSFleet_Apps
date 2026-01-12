package com.indusjs.fleet.presentation.vehicles.costs

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.core.ui.CostTypeGroup
import com.indusjs.fleet.core.ui.CostTypeSelection
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.fleet.data.model.costs.FuelTypes
import com.indusjs.fleet.data.model.costs.MaintenanceCostDto
import com.indusjs.fleet.data.model.costs.MaintenanceCostTypes
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * MVI Contract for Maintenance Cost Entry Screen with multi-row support.
 */
object MaintenanceCostEntryContract {

    /**
     * UI State for Maintenance Cost Entry screen.
     */
    data class State(
        // Vehicle selection
        val vehicles: List<Vehicle> = emptyList(),
        val selectedVehicle: Vehicle? = null,
        val showVehicleDropdown: Boolean = false,

        // Multi-row cost entries
        val costEntries: List<MaintenanceCostRow> = listOf(MaintenanceCostRow()),

        // Cost history
        val costHistory: List<MaintenanceCostDto> = emptyList(),
        val isLoadingHistory: Boolean = false,
        val showHistoryDialog: Boolean = false,

        // Form state
        val isLoading: Boolean = false,
        val isLoadingData: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,

        // Refresh cost types
        val isRefreshingCostTypes: Boolean = false,

        // Validation errors
        val vehicleError: String? = null,

        // Available options
        val costTypeOptions: List<Pair<String, String>> = MaintenanceCostTypes.types,
        val costTypeGroups: List<CostTypeGroup> = MaintenanceCostTypes.groups,
        val fuelTypeOptions: List<Pair<String, String>> = FuelTypes.types
    ) : UiState {

        val hasValidEntries: Boolean
            get() = costEntries.any { it.isValid }

        val canSave: Boolean
            get() = selectedVehicle != null && hasValidEntries && !isSaving
    }

    /**
     * User intents for Maintenance Cost Entry screen.
     */
    sealed interface Intent : UiIntent {
        // Load data
        data object LoadVehicles : Intent
        data object RefreshCostTypes : Intent

        // Vehicle selection
        data class SelectVehicle(val vehicle: Vehicle) : Intent
        data object ToggleVehicleDropdown : Intent

        // Row management
        data object AddCostRow : Intent
        data class RemoveCostRow(val rowId: String) : Intent
        data class ToggleRowExpanded(val rowId: String) : Intent

        // Cost entry updates (with row ID)
        data class SelectCostType(val rowId: String, val selection: CostTypeSelection) : Intent
        data class UpdateSelectedCategory(val rowId: String, val groupId: String, val groupName: String) : Intent
        data class ToggleCostTypeDropdown(val rowId: String) : Intent
        data class UpdateDate(val rowId: String, val value: String) : Intent
        data class UpdateTime(val rowId: String, val value: String) : Intent
        data class UpdateAmount(val rowId: String, val value: String) : Intent
        data class UpdateDescription(val rowId: String, val value: String) : Intent
        data class UpdateNotes(val rowId: String, val value: String) : Intent
        data class UpdateVendorName(val rowId: String, val value: String) : Intent
        data class UpdateInvoiceNo(val rowId: String, val value: String) : Intent

        // Fuel specific intents
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
     * Side effects for Maintenance Cost Entry screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data class ShowError(val message: String) : Effect
        data object NavigateBack : Effect
        data class CostsSaved(val count: Int) : Effect
    }
}

/**
 * Data class representing a single maintenance cost entry row.
 */
data class MaintenanceCostRow @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toString(),
    val isExpanded: Boolean = true,

    // Cost type
    val costType: String = "",
    val costTypeLabel: String = "",
    val selectedGroupId: String = "",   // group_id for reliable category detection
    val selectedGroupName: String = "", // group_name for display
    val showCostTypeDropdown: Boolean = false,
    val costTypeError: String? = null,

    // Date and Time
    val date: String = "",
    val dateError: String? = null,
    val time: String = "",

    // Amount
    val amount: String = "",
    val amountError: String? = null,

    // Fuel specific fields (shown when Fuel & Energy category is selected)
    val fuelType: String = "TC-001-002", // Default to Diesel
    val fuelQuantity: String = "",
    val fuelRate: String = "",
    val kmPerLiter: String = "",
    val showFuelTypeDropdown: Boolean = false,

    // Optional fields
    val description: String = "",
    val notes: String = "",
    val vendorName: String = "",
    val invoiceNo: String = ""
) {
    /**
     * Check if this is a Fuel & Energy cost type using group_id.
     */
    val isFuelCostType: Boolean
        get() = selectedGroupId == CostTypeSelection.FUEL_ENERGY_GROUP_ID ||
                costType.startsWith("TC-001")

    /**
     * Check if this row has valid required data.
     */
    val isValid: Boolean
        get() = costType.isNotBlank() &&
                date.isNotBlank() &&
                ValidationUtils.isValidDate(date) &&
                (time.isBlank() || ValidationUtils.isValidTime(time)) &&
                amount.isNotBlank() &&
                amount.toDoubleOrNull() != null &&
                (amount.toDoubleOrNull() ?: 0.0) > 0

    /**
     * Get display label for cost type.
     */
    val costTypeDisplayLabel: String
        get() = costTypeLabel.ifBlank {
            MaintenanceCostTypes.types.find { it.first == costType }?.second ?: costType
        }
}
