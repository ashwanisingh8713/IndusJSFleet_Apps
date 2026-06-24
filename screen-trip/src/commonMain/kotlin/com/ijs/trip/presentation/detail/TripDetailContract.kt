package com.ijs.trip.presentation.detail

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.data.datasource.location.PlacePrediction
import com.indusjs.fleet.data.model.costs.TripCostDto
import com.ijs.customer.domain.entity.Customer
import com.ijs.driver.domain.entity.Driver
import com.ijs.trip.payment.domain.entity.TripPayment
import com.ijs.trip.payment.domain.entity.TripPaymentSummary
import com.ijs.trip.domain.entity.Trip
import com.ijs.trip.domain.entity.TripStatus
import com.ijs.vehicle.domain.entity.Vehicle
import com.indusjs.pdfreport.model.TripCostsPdfData

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
     * Weight unit options.
     */
    val weightUnits = listOf("KG", "M.Ton", "Quintal", "Liter", "ft3")

    /**
     * UI State for the Trip Detail screen.
     */
    data class State(
        // Trip data
        val trip: Trip? = null,
        val tripId: String = "",

        // Permission flags (computed from PermissionChecker — never role names)
        val canEditTripPermission: Boolean = false,
        val canViewTripPricePermission: Boolean = false,

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

        // Customer selection (for edit mode)
        val customers: List<Customer> = emptyList(),
        val selectedCustomer: Customer? = null,
        val showCustomerBottomSheet: Boolean = false,
        val isLoadingCustomers: Boolean = false,
        val isRefreshingCustomers: Boolean = false,
        val customerSearchQuery: String = "",

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
        val weightUnit: String = "",
        val weightUnitOptions: List<String> = weightUnits,

        // Editable fields - Customer
        val customerName: String = "",
        val customerContact: String = "",

        // Editable fields - Other
        val priority: String = "",
        val notes: String = "",

        // Editable fields - Pricing
        // tripPrice = the quoted/expected price (expected_trip_price).
        val tripPrice: String = "",
        // actualPrice = the ACTUAL price (revenue) the customer owes; defaults to the quote
        // (tripPrice) but is user-overridable. Sent to the backend as selling_value.
        val actualPrice: String = "",
        // purchasePrice = COGS (purchase_price); forwarded on update too, not only create.
        val purchasePrice: String = "",

        // Trip costs
        val costs: List<TripCostDto> = emptyList(),
        val totalCost: Double = 0.0,
        val costsByType: Map<String, List<TripCostDto>> = emptyMap(),
        val isLoadingCosts: Boolean = false,
        // Non-null when the costs fetch failed (e.g. backend 500) — drives an error/retry state
        // distinct from the empty (200 + []) state, per the loading/error/empty rule.
        val costsError: String? = null,

        // Trip payments
        val payments: List<TripPayment> = emptyList(),
        val paymentSummary: TripPaymentSummary? = null,
        val isLoadingPayments: Boolean = false,
        val totalPaid: Double = 0.0,
        val pendingAmount: Double = 0.0,

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
        val statusOptions: List<TripStatus> = TripStatus.entries,

        // State Change
        val showStateChangeDialog: Boolean = false,
        val isUpdatingState: Boolean = false,

        /** DB-cached trip state labels (apiValue -> displayLabel) */
        val stateLabels: Map<String, String> = emptyMap(),

        /** DB-cached payment state labels (apiValue -> displayLabel) */
        val paymentStateLabels: Map<String, String> = emptyMap()
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

        val hasPayments: Boolean
            get() = payments.isNotEmpty()

        /**
         * Get filtered customers based on search query.
         */
        val filteredCustomers: List<Customer>
            get() = if (customerSearchQuery.isBlank()) {
                customers
            } else {
                customers.filter { customer ->
                    customer.companyName.contains(customerSearchQuery, ignoreCase = true) ||
                    customer.personName.contains(customerSearchQuery, ignoreCase = true) ||
                    customer.primaryContact.contains(customerSearchQuery, ignoreCase = true)
                }
            }

        /**
         * Determines if the user can edit this trip.
         * Gated purely on the trips:update permission (backend enforces any
         * state-based rules). State is never combined with a role name.
         */
        val canEdit: Boolean
            get() = trip != null && canEditTripPermission

        /**
         * Determines if the user can view trip_price.
         * Gated on the financials:read permission.
         */
        val canViewTripPrice: Boolean
            get() = canViewTripPricePermission

        /**
         * Determines if the user can edit trip_price only (not full trip).
         * Gated on the financials:read permission so price can be adjusted in any
         * state including completed.
         */
        val canEditTripPriceOnly: Boolean
            get() = trip != null && canViewTripPricePermission

        /**
         * Shows edit button if user can fully edit OR can edit price only.
         */
        val showEditButton: Boolean
            get() = canEdit || canEditTripPriceOnly
    }

    /**
     * User intents for the Trip Detail screen.
     */
    sealed interface Intent : UiIntent {
        // Load trip
        data class LoadTrip(val tripId: String) : Intent

        // Retry just the trip-costs fetch after an error
        data object RetryLoadCosts : Intent

        // Edit mode
        data object EnterEditMode : Intent
        data object ExitEditMode : Intent

        // Vehicle & Driver selection
        data class SelectVehicle(val vehicle: Vehicle) : Intent
        data class SelectDriver(val driver: Driver) : Intent
        data object ToggleVehicleDropdown : Intent
        data object ToggleDriverDropdown : Intent

        // Customer selection
        data object LoadCustomers : Intent
        data object ToggleCustomerBottomSheet : Intent
        data class SelectCustomer(val customer: Customer) : Intent
        data class UpdateCustomerSearchQuery(val query: String) : Intent
        data object ClearCustomerSelection : Intent
        data object RefreshCustomers : Intent
        data object NavigateToAddCustomer : Intent

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
        data class UpdateWeightUnit(val value: String) : Intent

        // Customer updates
        data class UpdateCustomerName(val value: String) : Intent
        data class UpdateCustomerContact(val value: String) : Intent

        // Other updates
        data class UpdatePriority(val value: String) : Intent
        data class UpdateNotes(val value: String) : Intent

        // Pricing updates
        data class UpdateTripPrice(val value: String) : Intent
        // Actual price (revenue) the customer owes — sent as selling_value.
        data class UpdateActualPrice(val value: String) : Intent
        // Purchase price (COGS).
        data class UpdatePurchasePrice(val value: String) : Intent

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
        data object NavigateToAddTripCost : Intent
        data object NavigateToAddPayment : Intent
        data class NavigateToPaymentDetail(val paymentId: String) : Intent

        // Error handling
        data object ClearError : Intent
        data object Refresh : Intent

        // State Change
        data object ShowStateChangeDialog : Intent
        data object HideStateChangeDialog : Intent
        data class UpdateTripState(val newState: String, val reason: String? = null) : Intent
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
        data class StateUpdated(val newState: String) : Effect
        data class NavigateToAddTripCost(val tripId: String, val vehicleId: String) : Effect
        data class NavigateToAddPayment(val tripId: String, val vehicleId: String) : Effect
        data class NavigateToPaymentDetail(val paymentId: String) : Effect
        data object NavigateToAddCustomer : Effect
    }
}
