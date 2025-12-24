package com.indusjs.fleet.presentation.vehicles.detail

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.entity.vehicle.VehicleStatus
import com.indusjs.fleet.domain.entity.vehicle.VehicleType

/**
 * MVI Contract for the Vehicle Detail screen.
 */
object VehicleDetailContract {

    /**
     * Fuel type options.
     */
    val fuelTypes = listOf("Diesel", "Petrol", "CNG", "Electric", "Hybrid")

    /**
     * UI State for the Vehicle Detail screen.
     */
    data class State(
        // Vehicle data
        val vehicle: Vehicle? = null,
        val vehicleId: String = "",

        // Edit mode
        val isEditMode: Boolean = false,

        // Editable fields
        val registrationNumber: String = "",
        val make: String = "",
        val model: String = "",
        val year: String = "",
        val vehicleType: VehicleType = VehicleType.CAR,
        val fuelType: String = "Diesel",
        val color: String = "",
        val capacity: String = "",

        // Validation errors
        val makeError: String? = null,
        val modelError: String? = null,
        val yearError: String? = null,

        // Form state
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,

        // Available options
        val vehicleTypes: List<VehicleType> = VehicleType.entries,
        val fuelTypeOptions: List<String> = fuelTypes,
        val statusOptions: List<VehicleStatus> = VehicleStatus.entries
    ) : UiState {

        val isFormValid: Boolean
            get() = make.isNotBlank() &&
                    model.isNotBlank() &&
                    year.isNotBlank() &&
                    year.toIntOrNull() != null &&
                    makeError == null &&
                    modelError == null &&
                    yearError == null

        val canSave: Boolean
            get() = isFormValid && !isSaving && isEditMode
    }

    /**
     * User intents for the Vehicle Detail screen.
     */
    sealed interface Intent : UiIntent {
        // Load vehicle
        data class LoadVehicle(val vehicleId: String) : Intent

        // Edit mode
        data object EnterEditMode : Intent
        data object ExitEditMode : Intent

        // Field updates (in edit mode)
        data class UpdateMake(val value: String) : Intent
        data class UpdateModel(val value: String) : Intent
        data class UpdateYear(val value: String) : Intent
        data class UpdateVehicleType(val type: VehicleType) : Intent
        data class UpdateFuelType(val value: String) : Intent
        data class UpdateColor(val value: String) : Intent
        data class UpdateCapacity(val value: String) : Intent

        // Save changes
        data object SaveChanges : Intent

        // Delete vehicle
        data object DeleteVehicle : Intent
        data object ConfirmDelete : Intent

        // Navigation
        data object NavigateBack : Intent

        // Error handling
        data object ClearError : Intent
        data object Refresh : Intent
    }

    /**
     * Side effects for the Vehicle Detail screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data class ShowError(val message: String) : Effect
        data object NavigateBack : Effect
        data object ShowDeleteConfirmation : Effect
        data class VehicleDeleted(val vehicleId: String) : Effect
        data object VehicleUpdated : Effect
    }
}

