package com.indusjs.fleet.presentation.costs

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.data.model.costs.MaintenanceCostDto
import com.indusjs.fleet.data.model.costs.MaintenanceCostTypes
import com.indusjs.fleet.domain.entity.vehicle.Vehicle

/**
 * MVI Contract for Vehicle Maintenance Cost Entry Screen.
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

        // Cost entry fields
        val selectedCostType: String = "",
        val costTypeLabel: String = "",
        val showCostTypeDropdown: Boolean = false,
        val date: String = "",
        val time: String = "",
        val amount: String = "",
        val description: String = "",
        val notes: String = "",
        val vendorName: String = "",
        val invoiceNo: String = "",

        // Cost history
        val costHistory: List<MaintenanceCostDto> = emptyList(),
        val isLoadingHistory: Boolean = false,
        val showHistoryDialog: Boolean = false,

        // Form state
        val isLoading: Boolean = false,
        val isLoadingData: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,

        // Validation errors
        val vehicleError: String? = null,
        val costTypeError: String? = null,
        val dateError: String? = null,
        val amountError: String? = null,

        // Available options
        val costTypeOptions: List<Pair<String, String>> = MaintenanceCostTypes.types
    ) : UiState {

        val isFormValid: Boolean
            get() = selectedVehicle != null &&
                    selectedCostType.isNotBlank() &&
                    date.isNotBlank() &&
                    amount.isNotBlank() &&
                    amount.toDoubleOrNull() != null &&
                    (amount.toDoubleOrNull() ?: 0.0) > 0 &&
                    vehicleError == null &&
                    costTypeError == null &&
                    dateError == null &&
                    amountError == null

        val canSave: Boolean
            get() = isFormValid && !isSaving
    }

    /**
     * User intents for Maintenance Cost Entry screen.
     */
    sealed interface Intent : UiIntent {
        // Load data
        data object LoadVehicles : Intent

        // Vehicle selection
        data class SelectVehicle(val vehicle: Vehicle) : Intent
        data object ToggleVehicleDropdown : Intent

        // Cost entry
        data class SelectCostType(val costType: String, val label: String) : Intent
        data object ToggleCostTypeDropdown : Intent
        data class UpdateDate(val value: String) : Intent
        data class UpdateTime(val value: String) : Intent
        data class UpdateAmount(val value: String) : Intent
        data class UpdateDescription(val value: String) : Intent
        data class UpdateNotes(val value: String) : Intent
        data class UpdateVendorName(val value: String) : Intent
        data class UpdateInvoiceNo(val value: String) : Intent

        // History
        data object LoadCostHistory : Intent
        data object ShowHistory : Intent
        data object HideHistory : Intent

        // Actions
        data object SaveCost : Intent
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
        data object CostSaved : Effect
    }
}
