package com.indusjs.fleet.presentation.trips.detail

import co.touchlab.kermit.Logger
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.util.convertToIsoDateTime
import com.indusjs.fleet.data.datasource.location.GooglePlacesService
import com.indusjs.fleet.data.datasource.location.PlacePrediction
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.model.trip.UpdateTripRequest
import com.indusjs.fleet.domain.entity.customer.Customer
import com.indusjs.fleet.domain.entity.driver.Driver
import com.indusjs.fleet.domain.entity.driver.DriverStatus
import com.indusjs.fleet.domain.entity.trip.TripStatus
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.entity.vehicle.VehicleStatus
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.indusjs.fleet.domain.repository.customer.CustomerRepository
import com.indusjs.fleet.domain.repository.trip.TripRepository
import com.indusjs.fleet.domain.usecase.driver.GetDriversUseCase
import com.indusjs.fleet.domain.usecase.trip.CancelTripUseCase
import com.indusjs.fleet.domain.usecase.trip.GetTripByIdUseCase
import com.indusjs.fleet.domain.usecase.trip.UpdateTripStatusUseCase
import com.indusjs.pdfreport.model.TripCostsPdfData
import com.indusjs.pdfreport.model.TripCostItem
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.fleet.domain.usecase.vehicle.GetVehiclesUseCase
import com.indusjs.fleet.presentation.trips.detail.TripDetailContract.Effect
import com.indusjs.fleet.presentation.trips.detail.TripDetailContract.Intent
import com.indusjs.fleet.presentation.trips.detail.TripDetailContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Trip Detail screen implementing MVI pattern.
 * Supports full edit functionality with Vehicle, Driver, Location (Google Places), Schedule updates.
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
    private val userLocalDataSource: UserLocalDataSource,
    private val googlePlacesService: GooglePlacesService? = null,
    private val customerRepository: CustomerRepository? = null
) : MviViewModel<State, Intent, Effect>(State()) {

    private val log = Logger.withTag("TripDetailViewModel")
    private var startLocationSearchJob: Job? = null
    private var endLocationSearchJob: Job? = null

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadTrip -> loadTrip(intent.tripId)
            is Intent.Refresh -> currentState.trip?.let { loadTrip(it.id) }

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
            is Intent.LoadCustomers -> loadCustomers()
            is Intent.ToggleCustomerBottomSheet -> updateState {
                copy(showCustomerBottomSheet = !showCustomerBottomSheet, customerSearchQuery = "")
            }
            is Intent.SelectCustomer -> selectCustomer(intent.customer)
            is Intent.UpdateCustomerSearchQuery -> updateState { copy(customerSearchQuery = intent.query) }
            is Intent.ClearCustomerSelection -> clearCustomerSelection()
            is Intent.RefreshCustomers -> refreshCustomers()
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
            is Intent.SearchStartLocation -> searchStartLocation(intent.query)
            is Intent.SearchEndLocation -> searchEndLocation(intent.query)
            is Intent.SelectStartLocationPrediction -> selectStartLocationPrediction(intent.prediction)
            is Intent.SelectEndLocationPrediction -> selectEndLocationPrediction(intent.prediction)
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

            // Actions
            is Intent.UpdateStatus -> updateStatus(intent.status)
            is Intent.SaveChanges -> saveChanges()
            is Intent.CancelTrip -> sendEffect(Effect.ShowCancelConfirmation)
            is Intent.ConfirmCancel -> confirmCancel()
            is Intent.ExportCostsToPdf -> exportCostsToPdf()

            // Navigation & errors
            is Intent.NavigateBack -> sendEffect(Effect.NavigateBack)
            is Intent.NavigateToAddTripCost -> navigateToAddTripCost()
            is Intent.NavigateToAddPayment -> navigateToAddPayment()
            is Intent.ClearError -> updateState { copy(error = null) }

            // State change intents
            is Intent.ShowStateChangeDialog -> updateState { copy(showStateChangeDialog = true) }
            is Intent.HideStateChangeDialog -> updateState { copy(showStateChangeDialog = false) }
            is Intent.UpdateTripState -> updateTripState(intent.newState, intent.reason)
        }
    }

    private suspend fun loadTrip(tripId: String) {
        updateState { copy(isLoading = true, error = null, tripId = tripId) }

        withContext(dispatcherProvider.io) {
            // Load user role for permission check
            val userRole = try {
                userLocalDataSource.getUserRole() ?: ""
            } catch (e: Exception) {
                log.e { "Failed to get user role: ${e.message}" }
                ""
            }
            val normalizedRole = userRole.lowercase().replace("_", "")
            log.d { "TripDetailViewModel - userRole: '$userRole', normalized: '$normalizedRole', canEdit: ${normalizedRole == "owner" || normalizedRole == "generalmanager" || normalizedRole == "manager"}" }
            updateState { copy(userRole = userRole) }

            when (val result = getTripByIdUseCase(tripId)) {
                is Result.Success -> {
                    val trip = result.data

                    // Parse departure date/time from ISO or separate fields
                    val (depDate, depTime) = parseScheduleDateTime(
                        isoDateTime = trip.plannedStart,
                        date = trip.scheduledDate,
                        time = trip.startTime
                    )

                    // Parse arrival date/time from ISO or separate fields
                    val (arrDate, arrTime) = parseScheduleDateTime(
                        isoDateTime = trip.plannedEnd,
                        date = trip.deliveryDate,
                        time = trip.deliveryTime
                    )

                    updateState {
                        copy(
                            isLoading = false,
                            trip = trip,
                            // Populate editable fields from trip data
                            startLocationAddress = trip.startLocation?.address ?: "",
                            startLat = trip.startLocation?.latitude?.toString() ?: "",
                            startLng = trip.startLocation?.longitude?.toString() ?: "",
                            endLocationAddress = trip.endLocation?.address ?: "",
                            endLat = trip.endLocation?.latitude?.toString() ?: "",
                            endLng = trip.endLocation?.longitude?.toString() ?: "",
                            estimatedDistance = trip.distance.takeIf { it > 0 }?.toString() ?: "",
                            // Duration from trip display info
                            estimatedDuration = trip.displayInfo.durationValue.takeIf { it != "NA" } ?: "",
                            // Schedule - Departure
                            departureDate = depDate,
                            departureTime = depTime,
                            // Schedule - Arrival
                            arrivalDate = arrDate,
                            arrivalTime = arrTime,
                            cargoType = trip.cargoType ?: "",
                            cargoDescription = trip.cargoDescription ?: "",
                            cargoWeight = trip.cargoLoadingWeight?.toString() ?: "",
                            weightUnit = trip.weightUnit ?: "KG",
                            customerName = trip.customerName ?: "",
                            priority = trip.priority ?: "",
                            notes = trip.notes ?: "",
                            tripPrice = trip.tripPrice?.toString() ?: ""
                        )
                    }
                    // Load trip costs
                    loadTripCosts(tripId)
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoading = false,
                            error = result.message ?: "Failed to load trip"
                        )
                    }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to load trip"))
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    private suspend fun loadTripCosts(tripId: String) {
        updateState { copy(isLoadingCosts = true) }

        when (val result = costsRepository.getTripCosts(tripId)) {
            is Result.Success -> {
                val costs = result.data
                val total = costs.sumOf { it.amount }
                val byType = costs.groupBy { it.costType }

                updateState {
                    copy(
                        costs = costs,
                        totalCost = total,
                        costsByType = byType,
                        isLoadingCosts = false
                    )
                }
            }
            is Result.Error -> {
                updateState { copy(isLoadingCosts = false) }
            }
            is Result.Loading -> { /* Not applicable */ }
        }
    }

    private suspend fun enterEditMode() {
        val trip = currentState.trip
        if (trip == null) {
            sendEffect(Effect.ShowSnackbar("Trip not loaded"))
            return
        }

        // Use the canEdit property which checks user role
        // Owner and GM can edit any state, Manager only planned
        if (!currentState.canEdit) {
            val message = if (currentState.userRole.lowercase().replace("_", "") == "supervisor") {
                "Supervisors cannot edit trips"
            } else {
                "Only planned trips can be edited"
            }
            sendEffect(Effect.ShowSnackbar(message))
            return
        }

        updateState { copy(isEditMode = true, isLoadingVehiclesDrivers = true) }

        // Load vehicles and drivers for selection
        loadVehiclesAndDrivers()

        // Load customers for selection
        loadCustomers()
    }

    private suspend fun loadVehiclesAndDrivers() {
        val trip = currentState.trip
        val currentVehicleId = trip?.vehicleId
        val currentDriverId = trip?.driverId

        withContext(dispatcherProvider.io) {
            // Load ALL vehicles, then filter client-side
            // This ensures currently assigned vehicle is included even if inactive
            getVehiclesUseCase().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        // Client-side filtering: show only available vehicles
                        // But always include the currently assigned vehicle (even if inactive) for edit mode
                        val availableVehicles = result.data.filter { vehicle ->
                            VehicleStatus.isAvailableForAssignment(vehicle.status) || vehicle.id == currentVehicleId
                        }
                        val currentVehicle = availableVehicles.find { it.id == currentVehicleId }
                        updateState {
                            copy(
                                vehicles = availableVehicles,
                                selectedVehicle = currentVehicle
                            )
                        }
                    }
                    is Result.Error -> {
                        log.e { "Failed to load vehicles: ${result.message}" }
                    }
                    is Result.Loading -> { /* Already handled */ }
                }
            }
        }

        withContext(dispatcherProvider.io) {
            // Load ALL drivers, then filter client-side
            // This ensures currently assigned driver is included even if inactive
            getDriversUseCase().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        // Client-side filtering: show only available drivers
                        // But always include the currently assigned driver (even if inactive) for edit mode
                        val availableDrivers = result.data.filter { driver ->
                            (DriverStatus.isAvailableForAssignment(driver.status) && driver.isActive) || driver.id == currentDriverId
                        }
                        val currentDriver = availableDrivers.find { it.id == currentDriverId }
                        updateState {
                            copy(
                                drivers = availableDrivers,
                                selectedDriver = currentDriver,
                                isLoadingVehiclesDrivers = false
                            )
                        }
                    }
                    is Result.Error -> {
                        updateState { copy(isLoadingVehiclesDrivers = false) }
                        log.e { "Failed to load drivers: ${result.message}" }
                    }
                    is Result.Loading -> { /* Already handled */ }
                }
            }
        }
    }

    /**
     * Load customers from local database for customer selection.
     * Loads from local DB only - no API sync (user can manually refresh if needed).
     */
    private suspend fun loadCustomers() {
        if (customerRepository == null) {
            log.w { "CustomerRepository not available" }
            return
        }

        updateState { copy(isLoadingCustomers = true) }

        withContext(dispatcherProvider.io) {
            try {
                // Load from local cache only
                val localCustomers = customerRepository.getLocalCustomers()
                val activeCustomers = localCustomers.filter { it.isActive }
                    .map { summary ->
                        Customer(
                            id = summary.id,
                            companyName = summary.companyName,
                            personName = summary.personName,
                            primaryContact = summary.primaryContact,
                            secondaryContact = null,
                            companyAddress = null,
                            email = null,
                            gstNumber = null,
                            notes = null,
                            isActive = summary.isActive,
                            createdAt = null,
                            updatedAt = null
                        )
                    }
                updateState {
                    copy(
                        customers = activeCustomers,
                        isLoadingCustomers = false
                    )
                }
                log.d { "Loaded ${activeCustomers.size} customers from local cache" }
            } catch (e: Exception) {
                updateState { copy(isLoadingCustomers = false) }
                log.e { "Failed to load customers: ${e.message}" }
            }
        }
    }

    /**
     * Select a customer and populate the customer fields.
     */
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
        log.d { "Selected customer: ${customer.companyName}" }
    }

    /**
     * Clear customer selection.
     */
    private fun clearCustomerSelection() {
        updateState {
            copy(
                selectedCustomer = null,
                customerName = "",
                customerContact = "",
                customerSearchQuery = ""
            )
        }
        log.d { "Cleared customer selection" }
    }

    /**
     * Refresh customers from API and update local DB.
     */
    private fun refreshCustomers() {
        val repository = customerRepository ?: run {
            log.e { "CustomerRepository not available" }
            sendEffect(Effect.ShowError("Customer feature not available"))
            return
        }

        CoroutineScope(dispatcherProvider.main).launch {
            updateState { copy(isRefreshingCustomers = true) }
            try {
                // Refresh customers from API (which updates local DB)
                when (val result = repository.refreshCustomers()) {
                    is Result.Success -> {
                        // Reload from local DB
                        loadCustomers()
                        sendEffect(Effect.ShowSnackbar("Customers refreshed"))
                    }
                    is Result.Error -> {
                        sendEffect(Effect.ShowError("Failed to refresh customers: ${result.message}"))
                    }
                    is Result.Loading -> { /* Already handled */ }
                }
            } catch (e: Exception) {
                log.e { "Failed to refresh customers: ${e.message}" }
                sendEffect(Effect.ShowError("Failed to refresh customers"))
            } finally {
                updateState { copy(isRefreshingCustomers = false) }
            }
        }
    }

    /**
     * Navigate to add customer screen.
     */
    private fun navigateToAddCustomer() {
        updateState { copy(showCustomerBottomSheet = false) }
        sendEffect(Effect.NavigateToAddCustomer)
    }

    private fun exitEditMode() {
        val trip = currentState.trip
        if (trip != null) {
            // Parse departure date/time from ISO or separate fields
            val (depDate, depTime) = parseScheduleDateTime(
                isoDateTime = trip.plannedStart,
                date = trip.scheduledDate,
                time = trip.startTime
            )

            // Parse arrival date/time from ISO or separate fields
            val (arrDate, arrTime) = parseScheduleDateTime(
                isoDateTime = trip.plannedEnd,
                date = trip.deliveryDate,
                time = trip.deliveryTime
            )

            updateState {
                copy(
                    isEditMode = false,
                    // Reset all fields to original values
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
                    // Restore schedule fields
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
                    // Restore pricing
                    tripPrice = trip.tripPrice?.toString() ?: "",
                    // Clear errors
                    vehicleError = null,
                    driverError = null,
                    startLocationError = null,
                    endLocationError = null,
                    departureDateError = null,
                    departureTimeError = null,
                    // Clear search state
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

    private fun selectVehicle(vehicle: Vehicle) {
        if (vehicle.isOccupied) {
            sendEffect(Effect.ShowSnackbar("This vehicle is currently occupied"))
        } else {
            updateState {
                copy(
                    selectedVehicle = vehicle,
                    vehicleError = null,
                    showVehicleDropdown = false
                )
            }
        }
    }

    private fun selectDriver(driver: Driver) {
        if (driver.isOccupied) {
            sendEffect(Effect.ShowSnackbar("This driver is currently occupied"))
        } else {
            updateState {
                copy(
                    selectedDriver = driver,
                    driverError = null,
                    showDriverDropdown = false
                )
            }
        }
    }

    // ============ Location Search (Google Places) ============

    private fun searchStartLocation(query: String) {
        updateState { copy(startLocationAddress = query) }

        if (googlePlacesService == null) return

        startLocationSearchJob?.cancel()

        if (query.length < 3) {
            updateState {
                copy(
                    startLocationPredictions = emptyList(),
                    showStartLocationDropdown = false,
                    isSearchingStartLocation = false
                )
            }
            return
        }

        updateState { copy(isSearchingStartLocation = true, showStartLocationDropdown = true) }

        startLocationSearchJob = CoroutineScope(dispatcherProvider.main).launch {
            delay(300) // Debounce
            withContext(dispatcherProvider.io) {
                googlePlacesService.searchPlaces(query).fold(
                    onSuccess = { predictions ->
                        updateState {
                            copy(
                                startLocationPredictions = predictions,
                                isSearchingStartLocation = false,
                                showStartLocationDropdown = predictions.isNotEmpty()
                            )
                        }
                    },
                    onFailure = {
                        updateState {
                            copy(
                                startLocationPredictions = emptyList(),
                                isSearchingStartLocation = false,
                                showStartLocationDropdown = false
                            )
                        }
                    }
                )
            }
        }
    }

    private fun searchEndLocation(query: String) {
        updateState { copy(endLocationAddress = query) }

        if (googlePlacesService == null) return

        endLocationSearchJob?.cancel()

        if (query.length < 3) {
            updateState {
                copy(
                    endLocationPredictions = emptyList(),
                    showEndLocationDropdown = false,
                    isSearchingEndLocation = false
                )
            }
            return
        }

        updateState { copy(isSearchingEndLocation = true, showEndLocationDropdown = true) }

        endLocationSearchJob = CoroutineScope(dispatcherProvider.main).launch {
            delay(300) // Debounce
            withContext(dispatcherProvider.io) {
                googlePlacesService.searchPlaces(query).fold(
                    onSuccess = { predictions ->
                        updateState {
                            copy(
                                endLocationPredictions = predictions,
                                isSearchingEndLocation = false,
                                showEndLocationDropdown = predictions.isNotEmpty()
                            )
                        }
                    },
                    onFailure = {
                        updateState {
                            copy(
                                endLocationPredictions = emptyList(),
                                isSearchingEndLocation = false,
                                showEndLocationDropdown = false
                            )
                        }
                    }
                )
            }
        }
    }

    private suspend fun selectStartLocationPrediction(prediction: PlacePrediction) {
        updateState {
            copy(
                startLocationAddress = prediction.description,
                showStartLocationDropdown = false,
                startLocationPredictions = emptyList(),
                startLocationError = null
            )
        }

        googlePlacesService?.let { service ->
            withContext(dispatcherProvider.io) {
                service.getPlaceDetails(prediction.placeId).fold(
                    onSuccess = { details ->
                        details.geometry?.location?.let { latLng ->
                            updateState {
                                copy(
                                    startLat = latLng.lat.toString(),
                                    startLng = latLng.lng.toString()
                                )
                            }
                            calculateAndSetDistance()
                        }
                    },
                    onFailure = { /* Coordinates fetch failed */ }
                )
            }
        }
    }

    private suspend fun selectEndLocationPrediction(prediction: PlacePrediction) {
        updateState {
            copy(
                endLocationAddress = prediction.description,
                showEndLocationDropdown = false,
                endLocationPredictions = emptyList(),
                endLocationError = null
            )
        }

        googlePlacesService?.let { service ->
            withContext(dispatcherProvider.io) {
                service.getPlaceDetails(prediction.placeId).fold(
                    onSuccess = { details ->
                        details.geometry?.location?.let { latLng ->
                            updateState {
                                copy(
                                    endLat = latLng.lat.toString(),
                                    endLng = latLng.lng.toString()
                                )
                            }
                            calculateAndSetDistance()
                        }
                    },
                    onFailure = { /* Coordinates fetch failed */ }
                )
            }
        }
    }

    private fun calculateAndSetDistance() {
        val state = currentState

        val startLat = state.startLat.toDoubleOrNull()
        val startLng = state.startLng.toDoubleOrNull()
        val endLat = state.endLat.toDoubleOrNull()
        val endLng = state.endLng.toDoubleOrNull()

        if (startLat != null && startLng != null && endLat != null && endLng != null) {
            if (googlePlacesService != null) {
                CoroutineScope(dispatcherProvider.main).launch {
                    updateState { copy(isCalculatingDistance = true) }
                    withContext(dispatcherProvider.io) {
                        googlePlacesService.getRoadDistance(startLat, startLng, endLat, endLng).fold(
                            onSuccess = { result ->
                                val distanceKm = kotlin.math.round(result.distanceKm * 10) / 10
                                updateState {
                                    copy(
                                        estimatedDistance = distanceKm.toString(),
                                        estimatedDuration = result.durationText,
                                        isCalculatingDistance = false
                                    )
                                }
                            },
                            onFailure = {
                                val distance = calculateHaversineDistance(startLat, startLng, endLat, endLng)
                                val distanceKm = kotlin.math.round(distance * 10) / 10
                                updateState {
                                    copy(
                                        estimatedDistance = distanceKm.toString(),
                                        estimatedDuration = "", // Can't calculate duration without API
                                        isCalculatingDistance = false
                                    )
                                }
                            }
                        )
                    }
                }
            } else {
                val distance = calculateHaversineDistance(startLat, startLng, endLat, endLng)
                val distanceKm = kotlin.math.round(distance * 10) / 10
                updateState { copy(estimatedDistance = distanceKm.toString(), estimatedDuration = "") }
            }
        }
    }

    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = toRadians(lat2 - lat1)
        val dLon = toRadians(lon2 - lon1)
        val lat1Rad = toRadians(lat1)
        val lat2Rad = toRadians(lat2)

        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2) *
                kotlin.math.cos(lat1Rad) * kotlin.math.cos(lat2Rad)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadiusKm * c
    }

    private fun toRadians(degrees: Double): Double {
        return degrees * kotlin.math.PI / 180.0
    }

    // ============ Status & Save ============

    private suspend fun updateStatus(status: TripStatus) {
        val tripId = currentState.trip?.id ?: return
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            when (val result = updateTripStatusUseCase(tripId, status)) {
                is Result.Success -> {
                    updateState { copy(isSaving = false, trip = result.data) }
                    sendEffect(Effect.ShowSnackbar("Status updated to ${TripStatus.toApiString(status)}"))
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to update status"))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    private suspend fun saveChanges() {
        if (!validateForm()) {
            sendEffect(Effect.ShowSnackbar("Please fill all required fields correctly"))
            return
        }

        val currentTrip = currentState.trip ?: return
        val state = currentState
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            // Build ISO 8601 datetime strings
            val plannedStartIso = if (state.departureDate.isNotBlank() && state.departureTime.isNotBlank()) {
                convertToIsoDateTime(state.departureDate, state.departureTime)
            } else null

            val plannedEndIso = if (state.arrivalDate.isNotBlank() && state.arrivalTime.isNotBlank()) {
                convertToIsoDateTime(state.arrivalDate, state.arrivalTime)
            } else null

            val request = UpdateTripRequest(
                vehicleId = state.selectedVehicle?.id?.toIntOrNull(),
                driverId = state.selectedDriver?.id?.toIntOrNull(),
                plannedStart = plannedStartIso,
                plannedEnd = plannedEndIso,
                scheduledDate = plannedStartIso,
                startTime = plannedStartIso,
                deliveryDate = plannedEndIso,
                deliveryTime = plannedEndIso,
                startLocation = state.startLocationAddress.trim().takeIf { it.isNotBlank() },
                startLat = state.startLat.toDoubleOrNull(),
                startLng = state.startLng.toDoubleOrNull(),
                endLocation = state.endLocationAddress.trim().takeIf { it.isNotBlank() },
                endLat = state.endLat.toDoubleOrNull(),
                endLng = state.endLng.toDoubleOrNull(),
                estimatedDistance = state.estimatedDistance.toDoubleOrNull(),
                cargoType = state.cargoType.lowercase().takeIf { it.isNotBlank() },
                cargoDescription = state.cargoDescription.takeIf { it.isNotBlank() },
                cargoLoadingWeight = state.cargoWeight.toDoubleOrNull(),
                weightUnit = state.weightUnit.takeIf { it.isNotBlank() },
                customerName = state.customerName.takeIf { it.isNotBlank() },
                customerContact = state.customerContact.takeIf { it.isNotBlank() },
                tripPrice = state.tripPrice.toDoubleOrNull(),
                priority = state.priority.lowercase().takeIf { it.isNotBlank() },
                notes = state.notes.takeIf { it.isNotBlank() }
            )

            log.d { "=== UPDATE TRIP REQUEST ===" }
            log.d { "TripPrice state: '${state.tripPrice}' -> parsed: ${state.tripPrice.toDoubleOrNull()}" }
            log.d { "WeightUnit state: '${state.weightUnit}' -> sent: ${state.weightUnit.takeIf { it.isNotBlank() }}" }
            log.d { "CargoWeight state: '${state.cargoWeight}' -> parsed: ${state.cargoWeight.toDoubleOrNull()}" }
            log.d { "Full request: $request" }

            when (val result = tripRepository.updateTripWithRequest(currentTrip.id, request)) {
                is Result.Success -> {
                    val trip = result.data
                    log.d { "Trip updated successfully. Response tripPrice: ${trip.tripPrice}, weightUnit: ${trip.weightUnit}" }
                    updateState {
                        copy(
                            isSaving = false,
                            isEditMode = false,
                            trip = trip,
                            // Restore all fields from the updated trip
                            startLocationAddress = trip.startLocation?.address ?: "",
                            endLocationAddress = trip.endLocation?.address ?: "",
                            cargoType = trip.cargoType ?: "",
                            cargoDescription = trip.cargoDescription ?: "",
                            cargoWeight = trip.cargoLoadingWeight?.toString() ?: "",
                            weightUnit = trip.weightUnit ?: "KG",
                            customerName = trip.customerName ?: "",
                            priority = trip.priority ?: "",
                            notes = trip.notes ?: "",
                            tripPrice = trip.tripPrice?.toString() ?: "",
                            // Clear selection state
                            vehicles = emptyList(),
                            drivers = emptyList(),
                            selectedVehicle = null,
                            selectedDriver = null
                        )
                    }
                    sendEffect(Effect.ShowSnackbar("Trip updated successfully"))
                    sendEffect(Effect.TripUpdated)
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to update trip"))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    private fun validateForm(): Boolean {
        val state = currentState

        val vehicleError = if (state.selectedVehicle == null) "Please select a vehicle" else null
        val driverError = if (state.selectedDriver == null) "Please select a driver" else null
        val startLocationError = if (state.startLocationAddress.isBlank()) "Start location is required" else null
        val endLocationError = if (state.endLocationAddress.isBlank()) "End location is required" else null

        updateState {
            copy(
                vehicleError = vehicleError,
                driverError = driverError,
                startLocationError = startLocationError,
                endLocationError = endLocationError
            )
        }

        return vehicleError == null &&
               driverError == null &&
               startLocationError == null &&
               endLocationError == null
    }

    private suspend fun confirmCancel() {
        val tripId = currentState.trip?.id ?: return
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            when (val result = cancelTripUseCase(tripId)) {
                is Result.Success -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowSnackbar("Trip cancelled successfully"))
                    sendEffect(Effect.TripCancelled(tripId))
                    sendEffect(Effect.NavigateBack)
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to cancel trip"))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    /**
     * Export trip costs to PDF.
     * Creates PDF data and sends an effect for the screen to handle platform-specific PDF generation.
     */
    private fun exportCostsToPdf() {
        val state = currentState
        val trip = state.trip

        log.d { "exportCostsToPdf called - hasCosts: ${state.hasCosts}, costsCount: ${state.costs.size}" }

        if (!state.hasCosts) {
            log.d { "No costs to export" }
            sendEffect(Effect.ShowSnackbar("No costs to export"))
            return
        }

        // Calculate costs by type with totals
        val costsByTypeWithTotals = state.costsByType.mapValues { (_, costs) ->
            costs.sumOf { it.amount }
        }

        // Get current date and time for export using TimeUtils
        val exportDate = com.indusjs.fleet.core.util.getCurrentFormattedDateHumanReadable()
        val exportTime = com.indusjs.fleet.core.util.getCurrentFormattedTime()

        // Extract departure date and time
        val (departureDate, departureTime) = extractDateTime(
            isoDateTime = trip?.plannedStart,
            date = trip?.scheduledDate,
            time = trip?.startTime
        )

        // Extract arrival date and time
        val (arrivalDate, arrivalTime) = extractDateTime(
            isoDateTime = trip?.plannedEnd,
            date = trip?.deliveryDate,
            time = trip?.deliveryTime
        )

        val pdfData = TripCostsPdfData(
            tripId = (trip?.id ?: state.tripId).toIntOrNull() ?: 0,
            tripNumber = trip?.tripNumber ?: "Trip #${trip?.id ?: state.tripId}",
            vehicleNumber = trip?.vehicleNumber ?: "N/A",
            driverName = trip?.driverName,
            startLocation = trip?.startLocation?.address ?: "N/A",
            endLocation = trip?.endLocation?.address ?: "N/A",
            departureDate = departureDate ?: "N/A",
            arrivalDate = arrivalDate,
            tripStatus = trip?.status?.let { TripStatus.toApiString(it) } ?: "N/A",
            costs = state.costs.map { cost ->
                TripCostItem(
                    costId = cost.costId ?: "",
                    costLabel = cost.costLabel ?: "Unknown",
                    amount = cost.amount ?: 0.0,
                    date = cost.date ?: "",
                    time = cost.time,
                    notes = cost.notes
                )
            },
            totalCost = state.totalCost,
            fuelCost = costsByTypeWithTotals.filterKeys { it.contains("fuel", ignoreCase = true) }.values.sumOf { it },
            tollCost = costsByTypeWithTotals.filterKeys { it.contains("toll", ignoreCase = true) }.values.sumOf { it },
            otherCost = state.totalCost - costsByTypeWithTotals.filterKeys {
                it.contains("fuel", ignoreCase = true) || it.contains("toll", ignoreCase = true)
            }.values.sumOf { it },
            generatedAt = FleetDateTime.formatDisplayDateTime12Hour(FleetDateTime.now())
        )

        log.d { "Sending ExportPdf effect with tripId: ${pdfData.tripId}, totalCost: ${pdfData.totalCost}" }
        sendEffect(Effect.ExportPdf(pdfData))
    }

    /**
     * Extract date and time from ISO datetime or separate date/time fields.
     * Returns Pair(date as DD-MM-YYYY, time as HH:MM)
     */
    private fun extractDateTime(
        isoDateTime: String?,
        date: String?,
        time: String?
    ): Pair<String?, String?> {
        if (!isoDateTime.isNullOrBlank()) {
            return try {
                // Parse ISO 8601: 2026-01-04T11:11:00Z
                val parts = isoDateTime.replace("Z", "").split("T")
                if (parts.size == 2) {
                    val datePart = parts[0] // 2026-01-04
                    val timePart = parts[1].take(5) // 11:11

                    // Convert date to DD-MM-YYYY
                    val dateComponents = datePart.split("-")
                    val formattedDate = if (dateComponents.size == 3) {
                        "${dateComponents[2]}-${dateComponents[1]}-${dateComponents[0]}"
                    } else {
                        datePart
                    }

                    Pair(formattedDate, timePart)
                } else {
                    Pair(isoDateTime, null)
                }
            } catch (e: Exception) {
                Pair(isoDateTime, null)
            }
        }

        return Pair(date, time)
    }

    /**
     * Get human-readable status label for trip status.
     */
    private fun getStatusLabel(status: TripStatus): String {
        return TripStatus.getDisplayLabel(status)
    }

    /**
     * Get current date formatted as DD-MM-YYYY.
     * Uses a simple approach to get today's date.
     */
    private fun getCurrentFormattedDate(): String {
        // Get current date from the trip's created_at or use fallback
        return currentState.trip?.createdAt?.let { createdAt ->
            // If we have a createdAt, use today's approximation
            formatDateForDisplay(createdAt).substringBefore(" at").takeIf { it.isNotBlank() }
        } ?: run {
            // Fallback: return a static "today" that will be updated by the effect handler
            "09-01-2026"
        }
    }

    /**
     * Format date for display (DD-MM-YYYY).
     */
    private fun formatDateForDisplay(isoDate: String): String {
        return try {
            if (isoDate.contains("T")) {
                val datePart = isoDate.substringBefore("T")
                val parts = datePart.split("-")
                if (parts.size == 3) {
                    "${parts[2]}-${parts[1]}-${parts[0]}"
                } else isoDate
            } else isoDate
        } catch (e: Exception) {
            isoDate
        }
    }

    /**
     * Parses schedule date/time from ISO format or separate date/time fields.
     * Returns a Pair of (date in DDMMYYYY raw format, time in HHMM raw format).
     */
    private fun parseScheduleDateTime(
        isoDateTime: String?,
        date: String?,
        time: String?
    ): Pair<String, String> {
        // Try to parse from ISO 8601 format first (e.g., 2026-01-04T11:11:00Z)
        if (!isoDateTime.isNullOrBlank()) {
            try {
                val parts = isoDateTime.replace("Z", "").split("T")
                if (parts.size == 2) {
                    val datePart = parts[0] // 2026-01-04
                    val timePart = parts[1] // 11:11:00

                    // Convert date from YYYY-MM-DD to DDMMYYYY raw format
                    val dateComponents = datePart.split("-")
                    if (dateComponents.size == 3) {
                        val rawDate = "${dateComponents[2]}${dateComponents[1]}${dateComponents[0]}"

                        // Extract time HH:MM and convert to HHMM raw format
                        val timeComponents = timePart.split(":")
                        if (timeComponents.size >= 2) {
                            val rawTime = "${timeComponents[0]}${timeComponents[1]}"
                            return Pair(rawDate, rawTime)
                        }
                    }
                }
            } catch (e: Exception) {
                log.e { "Failed to parse ISO datetime: $isoDateTime" }
            }
        }

        // Fallback to separate date/time fields
        val rawDate = date?.replace("-", "")?.filter { it.isDigit() }?.take(8) ?: ""
        val rawTime = time?.replace(":", "")?.filter { it.isDigit() }?.take(4) ?: ""

        return Pair(rawDate, rawTime)
    }

    // ==================== Navigation to Add Trip Cost / Payment ====================

    private suspend fun navigateToAddTripCost() {
        val trip = currentState.trip ?: return
        val tripId = trip.id
        val vehicleId = trip.vehicleId?.toString() ?: ""

        sendEffect(Effect.NavigateToAddTripCost(tripId = tripId, vehicleId = vehicleId))
    }

    private suspend fun navigateToAddPayment() {
        val trip = currentState.trip ?: return
        val tripId = trip.id
        val vehicleId = trip.vehicleId?.toString() ?: ""

        sendEffect(Effect.NavigateToAddPayment(tripId = tripId, vehicleId = vehicleId))
    }

    // ==================== State Change ====================

    private suspend fun updateTripState(newState: String, reason: String?) {
        val tripId = currentState.tripId
        if (tripId.isBlank()) return

        updateState { copy(isUpdatingState = true) }

        withContext(dispatcherProvider.io) {
            when (val result = tripRepository.updateTripState(tripId, newState, reason)) {
                is Result.Success -> {
                    val updatedTrip = result.data
                    updateState {
                        copy(
                            isUpdatingState = false,
                            showStateChangeDialog = false,
                            trip = updatedTrip
                        )
                    }
                    sendEffect(Effect.StateUpdated(newState))
                    sendEffect(Effect.ShowSnackbar("Status updated to ${com.indusjs.fleet.core.constants.StatusConstants.TripState.getDisplayLabel(newState)}"))
                }
                is Result.Error -> {
                    updateState { copy(isUpdatingState = false) }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to update trip state"))
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }
}
