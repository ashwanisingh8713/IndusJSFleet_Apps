package com.indusjs.fleet.presentation.trips.create

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.data.datasource.location.PlacePrediction
import com.indusjs.fleet.domain.entity.customer.Customer
import com.indusjs.fleet.domain.entity.driver.Driver
import com.indusjs.fleet.domain.entity.vehicle.Vehicle

/**
 * MVI Contract for the Create Trip screen.
 */
object CreateTripContract {

    /**
     * Priority options.
     */
    val priorities = listOf("low", "normal", "high", "urgent")

    /**
     * Cargo type options.
     */
    val cargoTypes = listOf("gitti", "balu", "bhakshi", "enta", "hazardous", "valuable", "others")

    /**
     * Weight unit options.
     */
    val weightUnits = listOf("KG", "M.Ton", "Quintal", "Liter", "CFT", "Bags")

    /**
     * UI State for the Create Trip screen.
     */
    data class State(
        // User role for permission checks
        val userRole: String = "",

        // Vehicle & Driver selection
        val vehicles: List<Vehicle> = emptyList(),
        val drivers: List<Driver> = emptyList(),
        val selectedVehicle: Vehicle? = null,
        val selectedDriver: Driver? = null,

        // Route
        val startLocation: String = "",
        val startLat: String = "",
        val startLng: String = "",
        val endLocation: String = "",
        val endLat: String = "",
        val endLng: String = "",
        val estimatedDistance: String = "",

        // Schedule - Departure (required)
        val departureDate: String = "",
        val departureTime: String = "",
        // Schedule - Arrival (optional)
        val arrivalDate: String = "",
        val arrivalTime: String = "",

        // Cargo & Customer
        val cargoType: String = "",
        val cargoDescription: String = "",
        val cargoWeight: String = "",
        val weightUnit: String = "",
        val customerName: String = "",
        val customerContact: String = "",
        val priority: String = "normal",
        val notes: String = "",

        // Customer Selection (from local DB)
        val selectedCustomer: Customer? = null,
        val customerSearchQuery: String = "",
        val customerSuggestions: List<Customer> = emptyList(),
        val showCustomerDropdown: Boolean = false,
        val isSearchingCustomers: Boolean = false,
        val isRefreshingCustomers: Boolean = false,
        val allCustomers: List<Customer> = emptyList(),

        // Pricing
        val tripPrice: String = "",

        // Validation errors
        val vehicleError: String? = null,
        val driverError: String? = null,
        val startLocationError: String? = null,
        val endLocationError: String? = null,
        val departureDateError: String? = null,
        val departureTimeError: String? = null,
        val arrivalDateError: String? = null,
        val cargoTypeError: String? = null,
        val cargoWeightError: String? = null,
        val weightUnitError: String? = null,
        val customerNameError: String? = null,
        val customerContactError: String? = null,
        val tripPriceError: String? = null,

        // Form state
        val isLoading: Boolean = false,
        val isLoadingData: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,

        // Dropdown states
        val showVehicleDropdown: Boolean = false,
        val showDriverDropdown: Boolean = false,

        // Location search states
        val startLocationPredictions: List<PlacePrediction> = emptyList(),
        val endLocationPredictions: List<PlacePrediction> = emptyList(),
        val isSearchingStartLocation: Boolean = false,
        val isSearchingEndLocation: Boolean = false,
        val showStartLocationDropdown: Boolean = false,
        val showEndLocationDropdown: Boolean = false,

        // Distance calculation state
        val isCalculatingDistance: Boolean = false,
        val estimatedDuration: String = "", // e.g., "2 hours 30 mins"

        // Available options
        val priorityOptions: List<String> = priorities,
        val cargoTypeOptions: List<String> = cargoTypes,
        val weightUnitOptions: List<String> = weightUnits
    ) : UiState {

        /**
         * Determines if the user can view and set trip_price.
         * Only Owner and General Manager can see trip pricing.
         */
        val canViewTripPrice: Boolean
            get() {
                val role = userRole.lowercase().replace("_", "")
                return role == "owner" || role == "generalmanager"
            }

        /**
         * Form completion percentage for progress indicator.
         * Includes: Vehicle, Driver, Route (2), Schedule, Cargo (3), Customer (1), Pricing = 10 fields
         */
        val formCompletionPercentage: Int
            get() {
                var completed = 0
                val total = if (canViewTripPrice) 10 else 9

                if (selectedVehicle != null) completed++
                if (selectedDriver != null) completed++
                if (startLocation.isNotBlank()) completed++
                if (endLocation.isNotBlank()) completed++
                if (departureDate.isNotBlank() && departureTime.isNotBlank()) completed++
                if (cargoType.isNotBlank()) completed++
                if (cargoWeight.isNotBlank()) completed++
                if (weightUnit.isNotBlank()) completed++
                // Customer is valid only if selected from DB
                if (selectedCustomer != null) completed++
                if (canViewTripPrice && tripPrice.isNotBlank()) completed++

                return (completed * 100) / total
            }

        /**
         * Checks if customer is valid - must be selected from DB.
         */
        val isCustomerValid: Boolean
            get() = selectedCustomer != null

        /**
         * Checks if all required fields are filled without errors.
         */
        val isFormValid: Boolean
            get() {
                val baseValid = selectedVehicle != null &&
                        selectedDriver != null &&
                        startLocation.isNotBlank() &&
                        endLocation.isNotBlank() &&
                        departureDate.isNotBlank() &&
                        departureTime.isNotBlank() &&
                        cargoType.isNotBlank() &&
                        cargoWeight.isNotBlank() &&
                        weightUnit.isNotBlank() &&
                        isCustomerValid &&
                        vehicleError == null &&
                        driverError == null &&
                        startLocationError == null &&
                        endLocationError == null &&
                        departureDateError == null &&
                        departureTimeError == null &&
                        arrivalDateError == null &&
                        cargoTypeError == null &&
                        cargoWeightError == null &&
                        weightUnitError == null &&
                        customerNameError == null &&
                        customerContactError == null &&
                        tripPriceError == null

                return if (canViewTripPrice) {
                    baseValid && tripPrice.isNotBlank()
                } else {
                    baseValid
                }
            }

        /**
         * Checks if arrival date/time is provided but incomplete.
         */
        val hasIncompleteArrival: Boolean
            get() = (arrivalDate.isNotBlank() && arrivalTime.isBlank()) ||
                    (arrivalDate.isBlank() && arrivalTime.isNotBlank())

        val canSave: Boolean
            get() = isFormValid && !isSaving
    }

    /**
     * User intents for the Create Trip screen.
     */
    sealed interface Intent : UiIntent {
        // Load data
        data object LoadVehiclesAndDrivers : Intent

        // Vehicle & Driver selection
        data class SelectVehicle(val vehicle: Vehicle) : Intent
        data class SelectDriver(val driver: Driver) : Intent
        data object ToggleVehicleDropdown : Intent
        data object ToggleDriverDropdown : Intent

        // Route updates
        data class UpdateStartLocation(val value: String) : Intent
        data class UpdateStartLat(val value: String) : Intent
        data class UpdateStartLng(val value: String) : Intent
        data class UpdateEndLocation(val value: String) : Intent
        data class UpdateEndLat(val value: String) : Intent
        data class UpdateEndLng(val value: String) : Intent
        data class UpdateEstimatedDistance(val value: String) : Intent

        // Location search
        data class SearchStartLocation(val query: String) : Intent
        data class SearchEndLocation(val query: String) : Intent
        data class SelectStartLocationPrediction(val prediction: PlacePrediction) : Intent
        data class SelectEndLocationPrediction(val prediction: PlacePrediction) : Intent
        data object DismissStartLocationDropdown : Intent
        data object DismissEndLocationDropdown : Intent

        // Schedule updates - Departure (required)
        data class UpdateDepartureDate(val value: String) : Intent
        data class UpdateDepartureTime(val value: String) : Intent
        // Schedule updates - Arrival (optional)
        data class UpdateArrivalDate(val value: String) : Intent
        data class UpdateArrivalTime(val value: String) : Intent

        // Cargo & Customer updates
        data class UpdateCargoType(val value: String) : Intent
        data class UpdateCargoDescription(val value: String) : Intent
        data class UpdateCargoWeight(val value: String) : Intent
        data class UpdateWeightUnit(val value: String) : Intent
        data class UpdateCustomerName(val value: String) : Intent
        data class UpdateCustomerContact(val value: String) : Intent
        data class UpdatePriority(val value: String) : Intent
        data class UpdateNotes(val value: String) : Intent

        // Customer selection from local DB
        data class SelectCustomer(val customer: Customer) : Intent
        data class SearchCustomers(val query: String) : Intent
        data object ClearCustomerSelection : Intent
        data object DismissCustomerDropdown : Intent
        data object NavigateToAddCustomer : Intent
        data object RefreshCustomers : Intent

        // Pricing updates
        data class UpdateTripPrice(val value: String) : Intent

        // Actions
        data object CreateTrip : Intent

        // Navigation
        data object NavigateBack : Intent

        // Error handling
        data object ClearError : Intent
    }

    /**
     * Side effects for the Create Trip screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data class ShowError(val message: String) : Effect
        data object NavigateBack : Effect
        data class TripCreated(val tripId: String) : Effect
        data object NavigateToAddCustomer : Effect
    }
}
