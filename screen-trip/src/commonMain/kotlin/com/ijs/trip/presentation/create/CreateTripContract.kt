package com.ijs.trip.presentation.create

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.data.datasource.location.PlacePrediction
import com.ijs.customer.domain.entity.Customer
import com.ijs.driver.domain.entity.Driver
import com.ijs.trip.domain.entity.CargoMaterial
import com.ijs.trip.domain.entity.UnitLabel
import com.ijs.vehicle.domain.entity.Vehicle

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
        // Permission flag (computed from PermissionChecker — never role names)
        val canViewTripPricePermission: Boolean = false,

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
        // Consignee / delivery (receiver) details — REQUIRED by the backend on create.
        // Distinct from the billing customer above, though they usually start the same.
        val deliveryAddress: String = "",
        val deliveryPersonName: String = "",
        val deliveryContactNumber: String = "",
        val priority: String = "normal",
        val notes: String = "",

        // Customer Selection (from local DB)
        val selectedCustomer: Customer? = null,
        val customerSearchQuery: String = "",
        val customerSuggestions: List<Customer> = emptyList(),
        val showCustomerDropdown: Boolean = false,
        val showCustomerBottomSheet: Boolean = false,
        val isSearchingCustomers: Boolean = false,
        val isRefreshingCustomers: Boolean = false,
        val isLoadingCustomers: Boolean = false,
        val allCustomers: List<Customer> = emptyList(),

        // Pricing
        // tripPrice = the quoted/expected price (expected_trip_price).
        val tripPrice: String = "",
        // actualPrice = the ACTUAL price (revenue) the customer owes; defaults to the
        // quote (tripPrice) but is user-overridable. Sent as selling_value to the backend.
        val actualPrice: String = "",
        // purchasePrice = the COGS (cost of goods sold) for this trip. Sent as
        // purchase_price to the backend; null when blank so the backend default applies.
        val purchasePrice: String = "",

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
        // Consignee / delivery (receiver) validation errors.
        val deliveryAddressError: String? = null,
        val deliveryPersonNameError: String? = null,
        val deliveryContactNumberError: String? = null,
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

        // Config-driven cargo materials (loaded in the background from the bundled asset).
        // Empty until loaded; the option helpers below fall back to the static lists meanwhile.
        val cargoMaterials: List<CargoMaterial> = emptyList(),

        // Config-driven, language-aware display labels for unit VALUES (loaded from the asset).
        // The unit VALUE (weightUnit) is never changed by this — labels are display only.
        val unitLabels: List<UnitLabel> = emptyList()
    ) : UiState {

        /**
         * Cargo-type ids for the cargo dropdown: config materials' ids when loaded, else the
         * static [cargoTypes].
         */
        val cargoTypeOptions: List<String>
            get() = if (cargoMaterials.isNotEmpty()) cargoMaterials.map { it.id } else cargoTypes

        /**
         * Units valid for the currently-selected cargo type. Resolution order:
         *  1. the selected material's own units (matched by id),
         *  2. the config's global unit set (allUnits, derived from the loaded materials),
         *  3. the static [weightUnits].
         * Empty when no cargo type is selected yet (the unit dropdown is gated on this).
         */
        val unitOptionsForSelectedCargo: List<String>
            get() {
                if (cargoType.isBlank()) return emptyList()
                cargoMaterials.firstOrNull { it.id == cargoType }?.let { material ->
                    if (material.units.isNotEmpty()) return material.units
                }
                val configAllUnits = cargoMaterials.flatMap { it.units }.distinct()
                return configAllUnits.ifEmpty { weightUnits }
            }

        /**
         * Resolve a cargo material's display label by id. Picks the Hindi label when [hindi]
         * is set (and present), else the English label, falling back to the capitalized id
         * when the material is unknown (e.g. config not loaded yet).
         */
        fun cargoLabelFor(id: String, hindi: Boolean = false): String {
            val material = cargoMaterials.firstOrNull { it.id == id }
            val name = if (hindi) material?.labelHi?.takeIf { it.isNotBlank() } ?: material?.label
                       else material?.label
            return name ?: id.replaceFirstChar { it.uppercaseChar() }
        }

        /**
         * Resolve a unit VALUE to its display label. Picks the Hindi label when [hindi] is set
         * (falling back to the English label when blank), else the English label. Falls back to
         * the raw [value] when no label entry exists. Never changes the value itself.
         */
        fun unitLabelFor(value: String, hindi: Boolean = false): String =
            unitLabels.firstOrNull { it.value == value }?.let {
                if (hindi) it.labelHi.ifBlank { it.label } else it.label
            } ?: value

        /**
         * Determines if the user can view and set trip_price.
         * Gated on the financials:read permission.
         */
        val canViewTripPrice: Boolean
            get() = canViewTripPricePermission

        /**
         * Form completion percentage for progress indicator.
         * Includes: Vehicle, Driver, Route (2), Schedule, Cargo (3), Customer (1),
         * Delivery/Consignee (3), Pricing = 13 fields.
         * Customer is REQUIRED (backend customer_id is binding:required), so it IS counted.
         * The three delivery/consignee fields are also REQUIRED, so each IS counted.
         */
        val formCompletionPercentage: Int
            get() {
                var completed = 0
                val total = if (canViewTripPrice) 13 else 12

                if (selectedVehicle != null) completed++
                if (selectedDriver != null) completed++
                if (startLocation.isNotBlank()) completed++
                if (endLocation.isNotBlank()) completed++
                if (departureDate.isNotBlank() && departureTime.isNotBlank()) completed++
                if (cargoType.isNotBlank()) completed++
                if (cargoWeight.isNotBlank()) completed++
                if (weightUnit.isNotBlank()) completed++
                if (selectedCustomer != null) completed++
                // Delivery / Consignee — all three are REQUIRED.
                if (deliveryAddress.isNotBlank()) completed++
                if (deliveryPersonName.isNotBlank()) completed++
                if (deliveryContactNumber.isNotBlank()) completed++
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
                        // Customer is REQUIRED — backend CreateTripRequest.customer_id is
                        // binding:"required", so a trip cannot be created without one.
                        isCustomerValid &&
                        // Consignee / delivery fields are REQUIRED (backend binding:required) —
                        // each must be non-blank AND have no validation error.
                        deliveryAddress.isNotBlank() &&
                        deliveryPersonName.isNotBlank() &&
                        deliveryContactNumber.isNotBlank() &&
                        deliveryAddressError == null &&
                        deliveryPersonNameError == null &&
                        deliveryContactNumberError == null &&
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
        // Consignee / delivery (receiver) updates — all three are REQUIRED.
        data class UpdateDeliveryAddress(val value: String) : Intent
        data class UpdateDeliveryPersonName(val value: String) : Intent
        data class UpdateDeliveryContactNumber(val value: String) : Intent
        data class UpdatePriority(val value: String) : Intent
        data class UpdateNotes(val value: String) : Intent

        // Customer selection from local DB
        data class SelectCustomer(val customer: Customer) : Intent
        /** Select a customer by id after it was just created from this screen (refreshes then selects). */
        data class SelectCustomerById(val customerId: String) : Intent
        data class SearchCustomers(val query: String) : Intent
        data class UpdateCustomerSearchQuery(val query: String) : Intent
        data object ToggleCustomerBottomSheet : Intent
        data object ClearCustomerSelection : Intent
        data object DismissCustomerDropdown : Intent
        data object NavigateToAddCustomer : Intent
        data object RefreshCustomers : Intent

        // Pricing updates
        data class UpdateTripPrice(val value: String) : Intent
        // Actual price (revenue) the customer owes — sent as selling_value.
        data class UpdateActualPrice(val value: String) : Intent
        // Purchase price (COGS) for the trip — sent as purchase_price.
        data class UpdatePurchasePrice(val value: String) : Intent

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
