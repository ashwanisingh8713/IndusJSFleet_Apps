package com.indusjs.fleet.presentation.trips.detail

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.data.datasource.location.PlacePrediction
import com.indusjs.fleet.data.model.costs.TripCostDto
import com.indusjs.fleet.domain.entity.driver.Driver
import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.entity.trip.TripStatus
import com.indusjs.fleet.domain.entity.vehicle.Vehicle

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
    val cargoTypes = listOf("gitti", "balu", "bhakshi", "enta", "hazardous", "valuable", "others")

    /**
     * UI State for the Trip Detail screen.
     */
    data class State(
        // Trip data
        val trip: Trip? = null,
        val tripId: String = "",

        // Edit mode
        val isEditMode: Boolean = false,

        // Vehicle & Driver selection (for edit mode)
        val vehicles: List<Vehicle> = emptyList(),
        val drivers: List<Driver> = emptyList(),
        val selectedVehicle: Vehicle? = null,
        val selectedDriver: Driver? = null,
        val showVehicleDropdown: Boolean = false,
        val showDriverDropdown: Boolean = false,
        val isLoadingVehiclesDrivers: Boolean = false,

        // Editable fields - Location
        val startLocationAddress: String = "",
        val startLat: String = "",
        val startLng: String = "",
        val endLocationAddress: String = "",
        val endLat: String = "",
        val endLng: String = "",
        val estimatedDistance: String = "",

        // Location search (Google Places)
        val startLocationPredictions: List<PlacePrediction> = emptyList(),
        val endLocationPredictions: List<PlacePrediction> = emptyList(),
        val showStartLocationDropdown: Boolean = false,
        val showEndLocationDropdown: Boolean = false,
        val isSearchingStartLocation: Boolean = false,
        val isSearchingEndLocation: Boolean = false,
        val isCalculatingDistance: Boolean = false,
        val estimatedDuration: String = "", // e.g., "2h 30m"

        // Editable fields - Schedule
        val departureDate: String = "",
        val departureTime: String = "",
        val arrivalDate: String = "",
        val arrivalTime: String = "",

        // Editable fields - Cargo
        val cargoType: String = "",
        val cargoDescription: String = "",
        val cargoWeight: String = "",

        // Editable fields - Customer
        val customerName: String = "",
        val customerContact: String = "",

        // Editable fields - Other
        val priority: String = "",
        val notes: String = "",

        // Trip costs
        val costs: List<TripCostDto> = emptyList(),
        val totalCost: Double = 0.0,
        val costsByType: Map<String, List<TripCostDto>> = emptyMap(),
        val isLoadingCosts: Boolean = false,

        // Validation errors
        val vehicleError: String? = null,
        val driverError: String? = null,
        val startLocationError: String? = null,
        val endLocationError: String? = null,
        val departureDateError: String? = null,
        val departureTimeError: String? = null,

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
            get() = selectedVehicle != null &&
                    selectedDriver != null &&
                    startLocationAddress.isNotBlank() &&
                    endLocationAddress.isNotBlank() &&
                    departureDate.isNotBlank() &&
                    departureTime.isNotBlank() &&
                    vehicleError == null &&
                    driverError == null &&
                    startLocationError == null &&
                    endLocationError == null &&
                    departureDateError == null &&
                    departureTimeError == null

        val canSave: Boolean
            get() = isFormValid && !isSaving && isEditMode

        val hasCosts: Boolean
            get() = costs.isNotEmpty()
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

        // Vehicle & Driver selection
        data class SelectVehicle(val vehicle: Vehicle) : Intent
        data class SelectDriver(val driver: Driver) : Intent
        data object ToggleVehicleDropdown : Intent
        data object ToggleDriverDropdown : Intent

        // Location updates
        data class UpdateStartLocation(val address: String) : Intent
        data class UpdateStartLat(val value: String) : Intent
        data class UpdateStartLng(val value: String) : Intent
        data class UpdateEndLocation(val address: String) : Intent
        data class UpdateEndLat(val value: String) : Intent
        data class UpdateEndLng(val value: String) : Intent
        data class UpdateEstimatedDistance(val value: String) : Intent

        // Location search (Google Places)
        data class SearchStartLocation(val query: String) : Intent
        data class SearchEndLocation(val query: String) : Intent
        data class SelectStartLocationPrediction(val prediction: PlacePrediction) : Intent
        data class SelectEndLocationPrediction(val prediction: PlacePrediction) : Intent
        data object DismissStartLocationDropdown : Intent
        data object DismissEndLocationDropdown : Intent

        // Schedule updates
        data class UpdateDepartureDate(val value: String) : Intent
        data class UpdateDepartureTime(val value: String) : Intent
        data class UpdateArrivalDate(val value: String) : Intent
        data class UpdateArrivalTime(val value: String) : Intent

        // Cargo updates
        data class UpdateCargoType(val value: String) : Intent
        data class UpdateCargoDescription(val value: String) : Intent
        data class UpdateCargoWeight(val value: String) : Intent

        // Customer updates
        data class UpdateCustomerName(val value: String) : Intent
        data class UpdateCustomerContact(val value: String) : Intent

        // Other updates
        data class UpdatePriority(val value: String) : Intent
        data class UpdateNotes(val value: String) : Intent

        // Status update (quick action)
        data class UpdateStatus(val status: TripStatus) : Intent

        // Save changes
        data object SaveChanges : Intent

        // Cancel trip
        data object CancelTrip : Intent
        data object ConfirmCancel : Intent

        // PDF Export
        data object ExportCostsToPdf : Intent

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
        data class ExportPdf(val pdfData: TripCostsPdfData) : Effect
    }

    /**
     * Data class for PDF export containing all trip cost information.
     */
    data class TripCostsPdfData(
        val tripId: String,
        val tripNumber: String?,
        val vehicleNumber: String?,
        val driverName: String?,
        val startLocation: String?,
        val endLocation: String?,
        val scheduledDate: String?,
        val tripStatus: String?,
        val tripStatusLabel: String?,
        val costs: List<TripCostDto>,
        val totalCost: Double,
        val costsByType: Map<String, Double>,
        val exportDate: String,
        val exportTime: String
    )
}
