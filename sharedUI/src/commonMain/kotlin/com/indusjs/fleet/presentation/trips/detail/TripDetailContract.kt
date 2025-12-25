package com.indusjs.fleet.presentation.trips.detail

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.entity.trip.TripStatus

/**
 * MVI Contract for the Trip Detail screen.
 */
object TripDetailContract {

    /**
     * Priority options.
     */
    val priorities = listOf("low", "normal", "high", "urgent")

    /**
     * Cargo type options.
     */
    val cargoTypes = listOf("general", "fragile", "perishable", "hazardous", "valuable", "bulk")

    /**
     * UI State for the Trip Detail screen.
     */
    data class State(
        // Trip data
        val trip: Trip? = null,
        val tripId: String = "",

        // Edit mode
        val isEditMode: Boolean = false,

        // Editable fields
        val startLocationAddress: String = "",
        val startLat: String = "",
        val startLng: String = "",
        val endLocationAddress: String = "",
        val endLat: String = "",
        val endLng: String = "",
        val distance: String = "",
        val plannedStart: String = "",
        val plannedEnd: String = "",
        val cargoType: String = "",
        val cargoDescription: String = "",
        val customerName: String = "",
        val priority: String = "",
        val notes: String = "",

        // Validation errors
        val startLocationError: String? = null,
        val endLocationError: String? = null,

        // Form state
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,

        // Available options
        val priorityOptions: List<String> = priorities,
        val cargoTypeOptions: List<String> = cargoTypes,
        val statusOptions: List<TripStatus> = TripStatus.entries
    ) : UiState {

        val isFormValid: Boolean
            get() = startLocationAddress.isNotBlank() &&
                    endLocationAddress.isNotBlank() &&
                    startLocationError == null &&
                    endLocationError == null

        val canSave: Boolean
            get() = isFormValid && !isSaving && isEditMode
    }

    /**
     * User intents for the Trip Detail screen.
     */
    sealed interface Intent : UiIntent {
        // Load trip
        data class LoadTrip(val tripId: String) : Intent

        // Edit mode
        data object EnterEditMode : Intent
        data object ExitEditMode : Intent

        // Field updates (in edit mode)
        data class UpdateStartLocation(val address: String) : Intent
        data class UpdateStartLat(val value: String) : Intent
        data class UpdateStartLng(val value: String) : Intent
        data class UpdateEndLocation(val address: String) : Intent
        data class UpdateEndLat(val value: String) : Intent
        data class UpdateEndLng(val value: String) : Intent
        data class UpdateDistance(val value: String) : Intent
        data class UpdatePlannedStart(val value: String) : Intent
        data class UpdatePlannedEnd(val value: String) : Intent
        data class UpdateCargoType(val value: String) : Intent
        data class UpdateCargoDescription(val value: String) : Intent
        data class UpdateCustomerName(val value: String) : Intent
        data class UpdatePriority(val value: String) : Intent
        data class UpdateNotes(val value: String) : Intent

        // Status update (quick action)
        data class UpdateStatus(val status: TripStatus) : Intent

        // Save changes
        data object SaveChanges : Intent

        // Cancel trip
        data object CancelTrip : Intent
        data object ConfirmCancel : Intent

        // Navigation
        data object NavigateBack : Intent

        // Error handling
        data object ClearError : Intent
        data object Refresh : Intent
    }

    /**
     * Side effects for the Trip Detail screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data class ShowError(val message: String) : Effect
        data object NavigateBack : Effect
        data object ShowCancelConfirmation : Effect
        data class TripCancelled(val tripId: String) : Effect
        data object TripUpdated : Effect
    }
}

