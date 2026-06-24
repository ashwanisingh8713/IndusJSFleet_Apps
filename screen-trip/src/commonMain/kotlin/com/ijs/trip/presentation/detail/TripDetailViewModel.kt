package com.ijs.trip.presentation.detail

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.trip.TAG_TRIP_DETAIL_VM
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.permission.PermissionChecker
import com.indusjs.fleet.data.datasource.location.GooglePlacesService
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.indusjs.fleet.domain.repository.states.StatesRepository
import com.ijs.customer.domain.entity.Customer
import com.ijs.customer.domain.repository.CustomerRepository
import com.ijs.driver.domain.entity.Driver
import com.ijs.driver.domain.usecase.GetDriversUseCase
import com.ijs.trip.payment.domain.repository.TripPaymentRepository
import com.ijs.trip.domain.repository.TripRepository
import com.ijs.trip.domain.usecase.CancelTripUseCase
import com.ijs.trip.domain.usecase.GetTripByIdUseCase
import com.ijs.trip.domain.usecase.UpdateTripStatusUseCase
import com.ijs.vehicle.domain.entity.Vehicle
import com.ijs.vehicle.domain.usecase.GetVehiclesUseCase
import com.ijs.trip.presentation.detail.TripDetailContract.Effect
import com.ijs.trip.presentation.detail.TripDetailContract.Intent
import com.ijs.trip.presentation.detail.TripDetailContract.State
import dev.zacsweers.metro.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * ViewModel for the Trip Detail screen implementing MVI pattern.
 * Delegates to handler classes for data loading, location search, and actions.
 *
 * @see TripDetailDataLoader for trip/costs/vehicles/drivers/customers loading
 * @see TripDetailLocationHandler for Google Places search and distance calculation
 * @see TripDetailActionHandler for save, validate, cancel, status, and PDF export
 */
@Inject
class TripDetailViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getTripByIdUseCase: GetTripByIdUseCase,
    private val updateTripStatusUseCase: UpdateTripStatusUseCase,
    private val cancelTripUseCase: CancelTripUseCase,
    private val costsRepository: CostsRepository,
    private val tripRepository: TripRepository,
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val getDriversUseCase: GetDriversUseCase,
    private val permissionChecker: PermissionChecker,
    private val googlePlacesService: GooglePlacesService? = null,
    private val customerRepository: CustomerRepository? = null,
    private val tripPaymentRepository: TripPaymentRepository? = null,
    private val logger: FleetLogger,
    private val statesRepository: StatesRepository? = null
) : MviViewModel<State, Intent, Effect>(State()), TripDetailStateManager {
// ==================== TripDetailStateManager Implementation ====================

    override val currentTripState: State get() = currentState
    override fun updateTripState(reducer: State.() -> State) = updateState(reducer)
    override fun emitEffect(effect: Effect) = sendEffect(effect)

    // ==================== Handler Delegates ====================

    private val dataLoader = TripDetailDataLoader(
        stateManager = this,
        dispatcherProvider = dispatcherProvider,
        scope = viewModelScope,
        getTripByIdUseCase = getTripByIdUseCase,
        costsRepository = costsRepository,
        getVehiclesUseCase = getVehiclesUseCase,
        getDriversUseCase = getDriversUseCase,
        permissionChecker = permissionChecker,
        customerRepository = customerRepository,
        tripPaymentRepository = tripPaymentRepository,
        logger = logger
    )

    private val locationHandler = TripDetailLocationHandler(
        stateManager = this,
        dispatcherProvider = dispatcherProvider,
        scope = viewModelScope,
        googlePlacesService = googlePlacesService
    )

    private val actionHandler = TripDetailActionHandler(
        stateManager = this,
        dispatcherProvider = dispatcherProvider,
        tripRepository = tripRepository,
        updateTripStatusUseCase = updateTripStatusUseCase,
        cancelTripUseCase = cancelTripUseCase,
        logger = logger
    )

    init {
        // Load DB-cached trip state labels
        viewModelScope.launch {
            try {
                val labels = statesRepository?.getTripStatesFlat()?.toMap() ?: emptyMap()
                if (labels.isNotEmpty()) updateState { copy(stateLabels = labels) }
            } catch (_: Exception) { /* fallback to empty */ }
        }
        // Load DB-cached payment state labels
        viewModelScope.launch {
            try {
                val labels = statesRepository?.getPaymentStatesFlat()?.toMap() ?: emptyMap()
                if (labels.isNotEmpty()) updateState { copy(paymentStateLabels = labels) }
            } catch (_: Exception) { /* fallback to empty */ }
        }
    }

    // ==================== Intent Handling ====================

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadTrip -> dataLoader.loadTrip(intent.tripId)
            is Intent.RetryLoadCosts -> {
                val tid = currentState.trip?.id ?: currentState.tripId
                tid?.let { dataLoader.loadTripCosts(it) }
            }
            is Intent.Refresh -> {
                if (!currentState.isLoading && !currentState.isLoadingPayments && !currentState.isLoadingCosts) {
                    val tid = currentState.trip?.id ?: currentState.tripId
                    tid?.let { dataLoader.loadTrip(it, isSilent = true) }
                }
            }

            // Edit mode
            is Intent.EnterEditMode -> enterEditMode()
            is Intent.ExitEditMode -> exitEditMode()

            // Vehicle & Driver selection
            is Intent.SelectVehicle -> selectVehicle(intent.vehicle)
            is Intent.SelectDriver -> selectDriver(intent.driver)
            is Intent.ToggleVehicleDropdown -> updateState {
                copy(showVehicleDropdown = !showVehicleDropdown, showDriverDropdown = false)
            }
            is Intent.ToggleDriverDropdown -> updateState {
                copy(showDriverDropdown = !showDriverDropdown, showVehicleDropdown = false)
            }

            // Customer selection
            is Intent.LoadCustomers -> dataLoader.loadCustomers()
            is Intent.ToggleCustomerBottomSheet -> updateState {
                copy(showCustomerBottomSheet = !showCustomerBottomSheet, customerSearchQuery = "")
            }
            is Intent.SelectCustomer -> selectCustomer(intent.customer)
            is Intent.UpdateCustomerSearchQuery -> updateState { copy(customerSearchQuery = intent.query) }
            is Intent.ClearCustomerSelection -> clearCustomerSelection()
            is Intent.RefreshCustomers -> dataLoader.refreshCustomers()
            is Intent.NavigateToAddCustomer -> navigateToAddCustomer()

            // Location updates
            is Intent.UpdateStartLocation -> updateState { copy(startLocationAddress = intent.address) }
            is Intent.UpdateStartLat -> updateState { copy(startLat = intent.value) }
            is Intent.UpdateStartLng -> updateState { copy(startLng = intent.value) }
            is Intent.UpdateEndLocation -> updateState { copy(endLocationAddress = intent.address) }
            is Intent.UpdateEndLat -> updateState { copy(endLat = intent.value) }
            is Intent.UpdateEndLng -> updateState { copy(endLng = intent.value) }
            is Intent.UpdateEstimatedDistance -> updateState { copy(estimatedDistance = intent.value) }

            // Location search (Google Places)
            is Intent.SearchStartLocation -> locationHandler.searchStartLocation(intent.query)
            is Intent.SearchEndLocation -> locationHandler.searchEndLocation(intent.query)
            is Intent.SelectStartLocationPrediction ->
                locationHandler.selectStartLocationPrediction(intent.prediction)
            is Intent.SelectEndLocationPrediction ->
                locationHandler.selectEndLocationPrediction(intent.prediction)
            is Intent.DismissStartLocationDropdown -> updateState {
                copy(showStartLocationDropdown = false, startLocationPredictions = emptyList())
            }
            is Intent.DismissEndLocationDropdown -> updateState {
                copy(showEndLocationDropdown = false, endLocationPredictions = emptyList())
            }

            // Schedule updates
            is Intent.UpdateDepartureDate -> updateState { copy(departureDate = intent.value) }
            is Intent.UpdateDepartureTime -> updateState { copy(departureTime = intent.value) }
            is Intent.UpdateArrivalDate -> updateState { copy(arrivalDate = intent.value) }
            is Intent.UpdateArrivalTime -> updateState { copy(arrivalTime = intent.value) }

            // Cargo updates
            is Intent.UpdateCargoType -> updateState { copy(cargoType = intent.value) }
            is Intent.UpdateCargoDescription -> updateState { copy(cargoDescription = intent.value) }
            is Intent.UpdateCargoWeight -> updateState { copy(cargoWeight = intent.value) }
            is Intent.UpdateWeightUnit -> updateState { copy(weightUnit = intent.value) }

            // Customer updates
            is Intent.UpdateCustomerName -> updateState { copy(customerName = intent.value) }
            is Intent.UpdateCustomerContact -> updateState { copy(customerContact = intent.value) }

            // Other updates
            is Intent.UpdatePriority -> updateState { copy(priority = intent.value) }
            is Intent.UpdateNotes -> updateState { copy(notes = intent.value) }

            // Pricing updates
            is Intent.UpdateTripPrice -> updateState { copy(tripPrice = intent.value) }
            is Intent.UpdateActualPrice -> updateState { copy(actualPrice = intent.value) }
            is Intent.UpdatePurchasePrice -> updateState { copy(purchasePrice = intent.value) }

            // Actions
            is Intent.UpdateStatus -> actionHandler.updateStatus(intent.status)
            is Intent.SaveChanges -> actionHandler.saveChanges()
            is Intent.CancelTrip -> sendEffect(Effect.ShowCancelConfirmation)
            is Intent.ConfirmCancel -> actionHandler.confirmCancel()
            is Intent.ExportCostsToPdf -> actionHandler.exportCostsToPdf()

            // Navigation & errors
            is Intent.NavigateBack -> sendEffect(Effect.NavigateBack)
            is Intent.NavigateToAddTripCost -> actionHandler.navigateToAddTripCost()
            is Intent.NavigateToAddPayment -> actionHandler.navigateToAddPayment()
            is Intent.NavigateToPaymentDetail -> sendEffect(Effect.NavigateToPaymentDetail(intent.paymentId))
            is Intent.ClearError -> updateState { copy(error = null) }

            // State change intents
            is Intent.ShowStateChangeDialog -> updateState { copy(showStateChangeDialog = true) }
            is Intent.HideStateChangeDialog -> updateState { copy(showStateChangeDialog = false) }
            is Intent.UpdateTripState -> actionHandler.updateTripState(intent.newState, intent.reason)
        }
    }

    // ==================== Edit Mode ====================

    private suspend fun enterEditMode() {
        val trip = currentState.trip
        if (trip == null) {
            sendEffect(Effect.ShowSnackbar("Trip not loaded"))
            return
        }

        if (!currentState.canEdit) {
            sendEffect(Effect.ShowSnackbar("You don't have permission to edit this trip"))
            return
        }

        updateState { copy(isEditMode = true, isLoadingVehiclesDrivers = true) }
        dataLoader.loadVehiclesAndDrivers()
        dataLoader.loadCustomers()
    }

    private fun exitEditMode() {
        val trip = currentState.trip
        if (trip != null) {
            val (depDate, depTime) = dataLoader.parseScheduleDateTime(
                timestampMs = trip.plannedStart ?: trip.scheduledDate, fallbackTimeMs = trip.startTime
            )
            val (arrDate, arrTime) = dataLoader.parseScheduleDateTime(
                timestampMs = trip.plannedEnd ?: trip.deliveryDate, fallbackTimeMs = trip.deliveryTime
            )

            updateState {
                copy(
                    isEditMode = false,
                    selectedVehicle = null,
                    selectedDriver = null,
                    vehicles = emptyList(),
                    drivers = emptyList(),
                    startLocationAddress = trip.startLocation?.address ?: "",
                    startLat = trip.startLocation?.latitude?.toString() ?: "",
                    startLng = trip.startLocation?.longitude?.toString() ?: "",
                    endLocationAddress = trip.endLocation?.address ?: "",
                    endLat = trip.endLocation?.latitude?.toString() ?: "",
                    endLng = trip.endLocation?.longitude?.toString() ?: "",
                    estimatedDistance = trip.distance.takeIf { it > 0 }?.toString() ?: "",
                    estimatedDuration = trip.displayInfo.durationValue.takeIf { it != "NA" } ?: "",
                    departureDate = depDate,
                    departureTime = depTime,
                    arrivalDate = arrDate,
                    arrivalTime = arrTime,
                    cargoType = trip.cargoType ?: "",
                    cargoDescription = trip.cargoDescription ?: "",
                    cargoWeight = trip.cargoLoadingWeight?.toString() ?: "",
                    weightUnit = trip.weightUnit ?: "KG",
                    customerName = trip.customerName ?: "",
                    customerContact = "",
                    priority = trip.priority ?: "",
                    notes = trip.notes ?: "",
                    tripPrice = trip.tripPrice?.toString() ?: "",
                    // Actual price (revenue) defaults to the quote; purchase price = COGS.
                    actualPrice = (trip.sellingValue ?: trip.tripPrice)?.toString() ?: "",
                    purchasePrice = trip.purchasePrice?.toString() ?: "",
                    vehicleError = null,
                    driverError = null,
                    startLocationError = null,
                    endLocationError = null,
                    departureDateError = null,
                    departureTimeError = null,
                    startLocationPredictions = emptyList(),
                    endLocationPredictions = emptyList(),
                    showStartLocationDropdown = false,
                    showEndLocationDropdown = false
                )
            }
        } else {
            updateState { copy(isEditMode = false) }
        }
    }

    // ==================== Selection Helpers ====================

    private fun selectVehicle(vehicle: Vehicle) {
        if (vehicle.isOccupied) {
            sendEffect(Effect.ShowSnackbar("This vehicle is currently occupied"))
        } else {
            updateState { copy(selectedVehicle = vehicle, vehicleError = null, showVehicleDropdown = false) }
        }
    }

    private fun selectDriver(driver: Driver) {
        if (driver.isOccupied) {
            sendEffect(Effect.ShowSnackbar("This driver is currently occupied"))
        } else {
            updateState { copy(selectedDriver = driver, driverError = null, showDriverDropdown = false) }
        }
    }

    private fun selectCustomer(customer: Customer) {
        updateState {
            copy(
                selectedCustomer = customer,
                customerName = customer.companyName,
                customerContact = customer.primaryContact,
                showCustomerBottomSheet = false,
                customerSearchQuery = ""
            )
        }
        logger.d(TAG_TRIP_DETAIL_VM, "Selected customer: ${customer.companyName}")
    }

    private fun clearCustomerSelection() {
        updateState {
            copy(
                selectedCustomer = null,
                customerName = "",
                customerContact = "",
                customerSearchQuery = ""
            )
        }
        logger.d(TAG_TRIP_DETAIL_VM, "Cleared customer selection")
    }

    private fun navigateToAddCustomer() {
        updateState { copy(showCustomerBottomSheet = false) }
        sendEffect(Effect.NavigateToAddCustomer)
    }
}
