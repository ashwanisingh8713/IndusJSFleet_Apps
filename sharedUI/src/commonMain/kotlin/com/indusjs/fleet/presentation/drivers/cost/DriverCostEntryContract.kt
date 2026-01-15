package com.indusjs.fleet.presentation.drivers.cost

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.core.ui.CostTypeGroup
import com.indusjs.fleet.core.ui.CostTypeSelection
import com.indusjs.fleet.data.model.driver.DriverCostDto
import com.indusjs.fleet.data.model.driver.DriverCostTypes
import com.indusjs.fleet.domain.entity.driver.Driver
import kotlin.random.Random

/**
 * Represents a single driver cost entry row.
 */
data class DriverCostEntryRow(
    val id: String = Random.nextLong().toString(),
    val costType: String = "",
    val costTypeLabel: String = "",
    val selectedGroupId: String = "",   // group_id for reliable category detection
    val selectedGroupName: String = "", // group_name for display
    val date: String = "",
    val time: String = "",
    val amount: String = "",
    val notes: String = "",
    val customCostTypeName: String = "",
    // Validation
    val costTypeError: String? = null,
    val dateError: String? = null,
    val amountError: String? = null,
    // Expanded state
    val isExpanded: Boolean = true,
    val showCostTypeDropdown: Boolean = false
) {
    // Deduction group detection using group_id (DC-G-003)
    val isDeductionType: Boolean
        get() = selectedGroupId == DriverCostTypes.DEDUCTION_GROUP_ID ||
                costType.startsWith("DC-003")

    // Other cost type is DC-004-005
    val isOtherCostType: Boolean
        get() = costType == "DC-004-005" || costType == "other"

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
 * MVI Contract for Driver Cost Entry Screen.
 */
object DriverCostEntryContract {

    /**
     * UI State for Driver Cost Entry screen.
     */
    data class State(
        // Driver selection
        val drivers: List<Driver> = emptyList(),
        val selectedDriver: Driver? = null,
        val showDriverDropdown: Boolean = false,
        val driverError: String? = null,

        // Multi-row cost entries
        val costEntries: List<DriverCostEntryRow> = listOf(DriverCostEntryRow()),

        // Cost history
        val costHistory: List<DriverCostDto> = emptyList(),
        val isLoadingHistory: Boolean = false,
        val showHistoryDialog: Boolean = false,

        // Form state
        val isLoading: Boolean = false,
        val isLoadingData: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,

        // Refresh cost types
        val isRefreshingCostTypes: Boolean = false,

        // Available options
        val costTypeOptions: List<Pair<String, String>> = DriverCostTypes.types,
        val costTypeGroups: List<CostTypeGroup> = DriverCostTypes.groups
    ) : UiState {

        val hasValidEntries: Boolean
            get() = costEntries.any { it.isValid }

        val canSave: Boolean
            get() = selectedDriver != null && hasValidEntries && !isSaving

        val driverInfo: String
            get() = selectedDriver?.let {
                "${it.firstName} ${it.lastName} - ${it.mobile}"
            } ?: ""
    }

    /**
     * User intents for Driver Cost Entry screen.
     */
    sealed interface Intent : UiIntent {
        // Load data
        data object LoadDrivers : Intent
        data object RefreshCostTypes : Intent

        // Driver selection
        data class SelectDriver(val driver: Driver) : Intent
        data object ToggleDriverDropdown : Intent

        // Cost entry row management
        data object AddCostRow : Intent
        data class RemoveCostRow(val rowId: String) : Intent
        data class ToggleRowExpanded(val rowId: String) : Intent

        // Cost entry field updates
        data class SelectCostType(val rowId: String, val selection: CostTypeSelection) : Intent
        data class UpdateSelectedCategory(val rowId: String, val groupId: String, val groupName: String) : Intent
        data class ToggleCostTypeDropdown(val rowId: String) : Intent
        data class UpdateDate(val rowId: String, val value: String) : Intent
        data class UpdateTime(val rowId: String, val value: String) : Intent
        data class UpdateAmount(val rowId: String, val value: String) : Intent
        data class UpdateNotes(val rowId: String, val value: String) : Intent
        data class UpdateCustomCostTypeName(val rowId: String, val value: String) : Intent

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
     * Side effects for Driver Cost Entry screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data class ShowError(val message: String) : Effect
        data object NavigateBack : Effect
        data class CostsSaved(val count: Int) : Effect
    }
}

