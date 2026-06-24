package com.ijs.trip.presentation.create

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.trip.TAG_CREATE_TRIP_VM
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.permission.PermissionChecker
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.fleet.core.util.convertToEpochMillis
import com.indusjs.fleet.data.datasource.location.GooglePlacesService
import com.indusjs.fleet.data.datasource.location.PlacePrediction
import com.ijs.customer.domain.entity.Customer
import com.ijs.driver.domain.entity.DriverStatus
import com.ijs.trip.data.CargoConfigProvider
import com.ijs.trip.domain.entity.CreateTripData
import com.ijs.vehicle.domain.entity.VehicleStatus
import com.ijs.customer.domain.repository.CustomerRepository
import com.ijs.driver.domain.usecase.GetAvailableDriversUseCase
import com.ijs.trip.domain.usecase.CreateTripWithDataUseCase
import com.ijs.vehicle.domain.usecase.GetAvailableVehiclesUseCase
import com.ijs.trip.presentation.create.CreateTripContract.Effect
import com.ijs.trip.presentation.create.CreateTripContract.Intent
import com.ijs.trip.presentation.create.CreateTripContract.State
import dev.zacsweers.metro.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Create Trip screen implementing MVI pattern.
 */
@Inject
class CreateTripViewModel(
    private val dispatcherProvider: DispatcherProvider,

    private val getAvailableVehiclesUseCase: GetAvailableVehiclesUseCase,
    private val getAvailableDriversUseCase: GetAvailableDriversUseCase,
    private val createTripWithDataUseCase: CreateTripWithDataUseCase,
    private val permissionChecker: PermissionChecker,
    private val googlePlacesService: GooglePlacesService? = null,
    private val customerRepository: CustomerRepository? = null,
    private val logger: FleetLogger
) : MviViewModel<State, Intent, Effect>(State()) {
private var startLocationSearchJob: Job? = null
    private var endLocationSearchJob: Job? = null

    init {
        // Compute permission flag from the user's actual permission set
        updateState { copy(canViewTripPricePermission = permissionChecker.canViewTripPrice()) }

        viewModelScope.launch(dispatcherProvider.io) {
            // Load customers for autocomplete: local cache first (fast), then a silent API sync
            // so a server-side customer that isn't cached yet still appears — without forcing the
            // user to find the small manual "Refresh customers" icon.
            loadCustomersFromRepository()
            refreshCustomersFromApi(silent = true)
        }

        // Load the cargo material → unit config from the bundled asset in the background.
        // Non-fatal: the provider falls back to hardcoded defaults if the asset is missing/corrupt,
        // so the form never breaks.
        viewModelScope.launch {
            val cfg = CargoConfigProvider.load(dispatcherProvider)
            updateState { copy(cargoMaterials = cfg.materials, unitLabels = cfg.unitLabels) }
        }
    }

    /**
     * Load customers from repository for autocomplete.
     * Loads from local DB only - no API sync (user can manually refresh if needed).
     */
    private suspend fun loadCustomersFromRepository() {
        customerRepository?.let { repo ->
            updateState { copy(isLoadingCustomers = true) }
            try {
                // Load from local cache only
                val localCustomers = repo.getLocalCustomers()
                val activeLocal = localCustomers.filter { it.isActive }
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
                updateState { copy(allCustomers = activeLocal, isLoadingCustomers = false) }
                logger.d(TAG_CREATE_TRIP_VM, "Loaded ${activeLocal.size} customers from local cache")
            } catch (e: Exception) {
                logger.e(TAG_CREATE_TRIP_VM, "Error loading customers: ${e.message}")
                updateState { copy(isLoadingCustomers = false) }
            }
        }
    }

    /**
     * Refresh customers from API and update local cache.
     */
    private suspend fun refreshCustomersFromApi(silent: Boolean = false) {
        updateState { copy(isRefreshingCustomers = true) }
        customerRepository?.let { repo ->
            try {
                when (val result = repo.refreshCustomers()) {
                    is Result.Success -> {
                        val activeCustomers = result.data.filter { it.isActive }
                        updateState { copy(allCustomers = activeCustomers, isRefreshingCustomers = false) }
                        if (!silent) sendEffect(Effect.ShowSnackbar("Customers refreshed (${activeCustomers.size} found)"))
                        logger.d(TAG_CREATE_TRIP_VM, "Refreshed ${activeCustomers.size} customers from API")
                    }
                    is Result.Error -> {
                        updateState { copy(isRefreshingCustomers = false) }
                        if (!silent) sendEffect(Effect.ShowError("Failed to refresh customers: ${result.message}"))
                        logger.e(TAG_CREATE_TRIP_VM, "Failed to refresh customers: ${result.message}")
                    }
                    is Result.Loading -> { }
                }
            } catch (e: Exception) {
                updateState { copy(isRefreshingCustomers = false) }
                sendEffect(Effect.ShowError("Failed to refresh customers"))
                logger.e(TAG_CREATE_TRIP_VM, "Error refreshing customers: ${e.message}")
            }
        } ?: run {
            updateState { copy(isRefreshingCustomers = false) }
        }
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadVehiclesAndDrivers -> loadVehiclesAndDrivers()

            // Vehicle & Driver selection
            is Intent.SelectVehicle -> {
                if (intent.vehicle.isOccupied) {
                    sendEffect(Effect.ShowSnackbar("This vehicle is currently occupied"))
                } else {
                    updateState { copy(selectedVehicle = intent.vehicle, vehicleError = null, showVehicleDropdown = false) }
                }
            }
            is Intent.SelectDriver -> {
                if (intent.driver.isOccupied) {
                    sendEffect(Effect.ShowSnackbar("This driver is currently occupied"))
                } else {
                    updateState { copy(selectedDriver = intent.driver, driverError = null, showDriverDropdown = false) }
                }
            }
            is Intent.ToggleVehicleDropdown -> {
                updateState { copy(showVehicleDropdown = !showVehicleDropdown, showDriverDropdown = false) }
            }
            is Intent.ToggleDriverDropdown -> {
                updateState { copy(showDriverDropdown = !showDriverDropdown, showVehicleDropdown = false) }
            }

            // Route updates
            is Intent.UpdateStartLocation -> updateStartLocation(intent.value)
            is Intent.UpdateStartLat -> updateState { copy(startLat = intent.value) }
            is Intent.UpdateStartLng -> updateState { copy(startLng = intent.value) }
            is Intent.UpdateEndLocation -> updateEndLocation(intent.value)
            is Intent.UpdateEndLat -> updateState { copy(endLat = intent.value) }
            is Intent.UpdateEndLng -> updateState { copy(endLng = intent.value) }
            is Intent.UpdateEstimatedDistance -> updateState { copy(estimatedDistance = intent.value) }

            // Location search
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

            // Schedule updates - Departure (required)
            is Intent.UpdateDepartureDate -> updateDepartureDate(intent.value)
            is Intent.UpdateDepartureTime -> updateDepartureTime(intent.value)
            // Schedule updates - Arrival (optional)
            is Intent.UpdateArrivalDate -> {
                updateState { copy(arrivalDate = intent.value) }
                validateArrivalDateTime(intent.value, currentState.arrivalTime)
            }
            is Intent.UpdateArrivalTime -> {
                updateState { copy(arrivalTime = intent.value) }
                validateArrivalDateTime(currentState.arrivalDate, intent.value)
            }

            // Cargo & Customer updates
            is Intent.UpdateCargoType -> updateCargoType(intent.value)
            is Intent.UpdateCargoDescription -> updateState { copy(cargoDescription = intent.value) }
            is Intent.UpdateCargoWeight -> {
                updateState { copy(cargoWeight = intent.value) }
                validateCargoWeight(intent.value)
            }
            is Intent.UpdateWeightUnit -> updateState { copy(weightUnit = intent.value, weightUnitError = null) }
            is Intent.UpdateCustomerName -> {
                updateState { copy(customerName = intent.value, selectedCustomer = null) }
                validateCustomerName(intent.value)
            }
            is Intent.UpdateCustomerContact -> {
                updateState { copy(customerContact = intent.value, selectedCustomer = null) }
                validateCustomerContact(intent.value)
            }
            // Consignee / delivery (receiver) updates — all three REQUIRED.
            is Intent.UpdateDeliveryAddress -> {
                updateState { copy(deliveryAddress = intent.value) }
                validateDeliveryAddress(intent.value)
            }
            is Intent.UpdateDeliveryPersonName -> {
                updateState { copy(deliveryPersonName = intent.value) }
                validateDeliveryPersonName(intent.value)
            }
            is Intent.UpdateDeliveryContactNumber -> {
                // Digit-filter + cap at 10 like the other phone fields.
                val digits = intent.value.filter { it.isDigit() }.take(10)
                updateState { copy(deliveryContactNumber = digits) }
                validateDeliveryContactNumber(digits)
            }
            is Intent.UpdatePriority -> updateState { copy(priority = intent.value) }
            is Intent.UpdateNotes -> updateState { copy(notes = intent.value) }

            // Customer selection from local DB
            is Intent.SelectCustomer -> selectCustomer(intent.customer)
            is Intent.SelectCustomerById -> {
                // Just created from this screen: pull the latest list, then auto-select it.
                refreshCustomersFromApi(silent = true)
                currentState.allCustomers.firstOrNull { it.id == intent.customerId }?.let { selectCustomer(it) }
            }
            is Intent.SearchCustomers -> searchCustomers(intent.query)
            is Intent.UpdateCustomerSearchQuery -> updateState { copy(customerSearchQuery = intent.query) }
            is Intent.ToggleCustomerBottomSheet -> updateState {
                copy(showCustomerBottomSheet = !showCustomerBottomSheet, customerSearchQuery = "")
            }
            is Intent.ClearCustomerSelection -> clearCustomerSelection()
            is Intent.DismissCustomerDropdown -> updateState { copy(showCustomerDropdown = false) }
            is Intent.NavigateToAddCustomer -> {
                updateState { copy(showCustomerBottomSheet = false) }
                sendEffect(Effect.NavigateToAddCustomer)
            }
            is Intent.RefreshCustomers -> refreshCustomersFromApi()

            // Pricing updates
            is Intent.UpdateTripPrice -> {
                updateState { copy(tripPrice = intent.value) }
                validateTripPrice(intent.value)
            }
            is Intent.UpdateActualPrice -> updateState { copy(actualPrice = intent.value) }
            is Intent.UpdatePurchasePrice -> updateState { copy(purchasePrice = intent.value) }

            // Actions
            is Intent.CreateTrip -> createTrip()

            // Navigation & errors
            is Intent.NavigateBack -> sendEffect(Effect.NavigateBack)
            is Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // VALIDATION METHODS
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Validates arrival date/time - must be after departure date/time.
     */
    private fun validateArrivalDateTime(arrivalDate: String, arrivalTime: String) {
        // If both arrival fields are empty, no validation needed (optional)
        if (arrivalDate.isBlank() && arrivalTime.isBlank()) {
            updateState { copy(arrivalDateError = null) }
            return
        }

        // If one is filled but not the other
        if (arrivalDate.isNotBlank() && arrivalTime.isBlank()) {
            updateState { copy(arrivalDateError = "Please enter arrival time") }
            return
        }
        if (arrivalDate.isBlank() && arrivalTime.isNotBlank()) {
            updateState { copy(arrivalDateError = "Please enter arrival date") }
            return
        }

        // Both are filled - check if arrival is after departure
        val state = currentState
        if (state.departureDate.isBlank() || state.departureTime.isBlank()) {
            updateState { copy(arrivalDateError = null) }
            return
        }

        // Compare dates: format is DD-MM-YYYY HH:mm
        val isArrivalBeforeDeparture = compareDateTimes(
            state.departureDate, state.departureTime,
            arrivalDate, arrivalTime
        )

        if (isArrivalBeforeDeparture) {
            updateState { copy(arrivalDateError = "Arrival must be after departure") }
        } else {
            updateState { copy(arrivalDateError = null) }
        }
    }

    /**
     * Compares two date-times. Returns true if second is before or equal to first.
     * Format: DD-MM-YYYY for date, HH:mm for time
     */
    private fun compareDateTimes(
        date1: String, time1: String,
        date2: String, time2: String
    ): Boolean {
        try {
            // Parse date1 (DD-MM-YYYY)
            val dateParts1 = date1.split("-")
            val timeParts1 = time1.split(":")
            if (dateParts1.size != 3 || timeParts1.size != 2) return false

            val day1 = dateParts1[0].toIntOrNull() ?: return false
            val month1 = dateParts1[1].toIntOrNull() ?: return false
            val year1 = dateParts1[2].toIntOrNull() ?: return false
            val hour1 = timeParts1[0].toIntOrNull() ?: return false
            val min1 = timeParts1[1].toIntOrNull() ?: return false

            // Parse date2 (DD-MM-YYYY)
            val dateParts2 = date2.split("-")
            val timeParts2 = time2.split(":")
            if (dateParts2.size != 3 || timeParts2.size != 2) return false

            val day2 = dateParts2[0].toIntOrNull() ?: return false
            val month2 = dateParts2[1].toIntOrNull() ?: return false
            val year2 = dateParts2[2].toIntOrNull() ?: return false
            val hour2 = timeParts2[0].toIntOrNull() ?: return false
            val min2 = timeParts2[1].toIntOrNull() ?: return false

            // Compare: return true if date2 <= date1
            return when {
                year2 < year1 -> true
                year2 > year1 -> false
                month2 < month1 -> true
                month2 > month1 -> false
                day2 < day1 -> true
                day2 > day1 -> false
                hour2 < hour1 -> true
                hour2 > hour1 -> false
                min2 <= min1 -> true
                else -> false
            }
        } catch (e: Exception) {
            logger.e(TAG_CREATE_TRIP_VM, "Error comparing dates: ${e.message}")
            return false
        }
    }

    /**
     * Validates customer contact - must be 10 digits.
     */
    private fun validateCustomerContact(contact: String) {
        val error = if (contact.isNotBlank() && !ValidationUtils.isValidIndianMobile(contact)) {
            "Enter a valid 10-digit mobile number"
        } else null // Optional field
        updateState { copy(customerContactError = error) }
    }

    /**
     * Validates consignee/delivery address - REQUIRED, must be non-blank.
     */
    private fun validateDeliveryAddress(value: String) {
        val error = if (value.isBlank()) "Delivery address is required" else null
        updateState { copy(deliveryAddressError = error) }
    }

    /**
     * Validates consignee/delivery person name - REQUIRED, must be non-blank.
     */
    private fun validateDeliveryPersonName(value: String) {
        val error = if (value.isBlank()) "Delivery person name is required" else null
        updateState { copy(deliveryPersonNameError = error) }
    }

    /**
     * Validates consignee/delivery contact - REQUIRED, must be a valid 10-digit mobile.
     */
    private fun validateDeliveryContactNumber(value: String) {
        val error = when {
            value.isBlank() -> "Delivery contact number is required"
            !ValidationUtils.isValidIndianMobile(value) -> "Enter a valid 10-digit mobile number"
            else -> null
        }
        updateState { copy(deliveryContactNumberError = error) }
    }

    /**
     * Handle a cargo-type change. Units are config-driven per material, so recompute the valid
     * units for the newly-selected material and reconcile the weight unit:
     *  - keep the previously-selected unit if it's still valid for the new material,
     *  - otherwise use that material's defaultUnit,
     *  - or clear it if the material has no units (e.g. unknown id).
     * Clears the cargo-type and weight-unit errors.
     */
    private fun updateCargoType(value: String) {
        val state = currentState
        val material = state.cargoMaterials.firstOrNull { it.id == value }
        // Units available for the new material (config), falling back to whatever the State exposes.
        val units = material?.units?.takeIf { it.isNotEmpty() }
            ?: state.cargoMaterials.flatMap { it.units }.distinct()
        val newUnit = when {
            state.weightUnit.isNotBlank() && units.contains(state.weightUnit) -> state.weightUnit
            material != null && material.defaultUnit.isNotBlank() -> material.defaultUnit
            else -> ""
        }
        updateState {
            copy(
                cargoType = value,
                cargoTypeError = null,
                weightUnit = newUnit,
                weightUnitError = null
            )
        }
    }

    /**
     * Validates cargo weight - must be positive number.
     */
    private fun validateCargoWeight(weight: String) {
        val error = when {
            weight.isBlank() -> null // Optional field
            weight.toDoubleOrNull() == null -> "Enter valid weight"
            weight.toDoubleOrNull()!! <= 0 -> "Weight must be positive"
            else -> null
        }
        updateState { copy(cargoWeightError = error) }
    }

    /**
     * Validates customer name - OPTIONAL (backend treats customer as optional for trip creation).
     */
    private fun validateCustomerName(name: String) {
        // No error: customer name is optional. Kept for live-clearing of any stale error.
        updateState { copy(customerNameError = null) }
    }

    /**
     * Validates trip price - must be positive number if provided.
     */
    private fun validateTripPrice(price: String) {
        val error = when {
            price.isBlank() -> null // Optional field
            price.toDoubleOrNull() == null -> "Enter valid trip price"
            price.toDoubleOrNull()!! <= 0 -> "Trip price must be positive"
            else -> null
        }
        updateState { copy(tripPriceError = error) }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // DATA LOADING
    // ══════════════════════════════════════════════════════════════════════════════

    private suspend fun loadVehiclesAndDrivers() {
        updateState { copy(isLoadingData = true) }

        withContext(dispatcherProvider.io) {
            // Load available vehicles (only active status from API)
            getAvailableVehiclesUseCase().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        // Client-side fallback filtering to ensure only available vehicles are shown
                        val availableVehicles = result.data.filter { vehicle ->
                            VehicleStatus.isAvailableForAssignment(vehicle.status)
                        }
                        updateState { copy(vehicles = availableVehicles) }
                    }
                    is Result.Error -> {
                        sendEffect(Effect.ShowError("Failed to load vehicles: ${result.message}"))
                    }
                    is Result.Loading -> { /* Already handled */ }
                }
            }
        }

        withContext(dispatcherProvider.io) {
            // Load available drivers (only active status from API)
            getAvailableDriversUseCase().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        // Client-side fallback filtering to ensure only available drivers are shown
                        val availableDrivers = result.data.filter { driver ->
                            DriverStatus.isAvailableForAssignment(driver.status) && driver.isActive
                        }
                        updateState { copy(drivers = availableDrivers, isLoadingData = false) }
                    }
                    is Result.Error -> {
                        updateState { copy(isLoadingData = false) }
                        sendEffect(Effect.ShowError("Failed to load drivers: ${result.message}"))
                    }
                    is Result.Loading -> { /* Already handled */ }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // LOCATION HANDLING
    // ══════════════════════════════════════════════════════════════════════════════

    private fun updateStartLocation(value: String) {
        val error = if (value.isBlank()) "Start location is required" else null
        updateState { copy(startLocation = value, startLocationError = error) }
    }

    private fun updateEndLocation(value: String) {
        val error = if (value.isBlank()) "End location is required" else null
        updateState { copy(endLocation = value, endLocationError = error) }
    }

    private fun searchStartLocation(query: String) {
        updateState { copy(startLocation = query) }

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

        startLocationSearchJob = viewModelScope.launch {
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
        updateState { copy(endLocation = query) }

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

        endLocationSearchJob = viewModelScope.launch {
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
                startLocation = prediction.description,
                showStartLocationDropdown = false,
                startLocationPredictions = emptyList(),
                startLocationError = null
            )
        }

        // Fetch place details to get coordinates
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
                            // Calculate distance if end coordinates are available
                            calculateAndSetDistance()
                        }
                    },
                    onFailure = {
                        // Coordinates fetch failed, user can enter manually
                    }
                )
            }
        }
    }

    private suspend fun selectEndLocationPrediction(prediction: PlacePrediction) {
        updateState {
            copy(
                endLocation = prediction.description,
                showEndLocationDropdown = false,
                endLocationPredictions = emptyList(),
                endLocationError = null
            )
        }

        // Fetch place details to get coordinates
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
                            // Calculate distance if start coordinates are available
                            calculateAndSetDistance()
                        }
                    },
                    onFailure = {
                        // Coordinates fetch failed, user can enter manually
                    }
                )
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // DISTANCE CALCULATION
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Calculates the road distance between start and end coordinates using Google Distance Matrix API.
     * Falls back to Haversine formula if API is unavailable.
     */
    private fun calculateAndSetDistance() {
        val state = currentState

        val startLat = state.startLat.toDoubleOrNull()
        val startLng = state.startLng.toDoubleOrNull()
        val endLat = state.endLat.toDoubleOrNull()
        val endLng = state.endLng.toDoubleOrNull()

        if (startLat != null && startLng != null && endLat != null && endLng != null) {
            // Use Google Distance Matrix API for road distance
            if (googlePlacesService != null) {
                viewModelScope.launch {
                    updateState { copy(isCalculatingDistance = true) }
                    withContext(dispatcherProvider.io) {
                        googlePlacesService.getRoadDistance(startLat, startLng, endLat, endLng).fold(
                            onSuccess = { result ->
                                // Round to 1 decimal place
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
                                // Fallback to Haversine formula
                                val distance = calculateHaversineDistance(startLat, startLng, endLat, endLng)
                                val distanceKm = kotlin.math.round(distance * 10) / 10
                                updateState {
                                    copy(
                                        estimatedDistance = distanceKm.toString(),
                                        isCalculatingDistance = false
                                    )
                                }
                            }
                        )
                    }
                }
            } else {
                // Fallback to Haversine formula if API not available
                val distance = calculateHaversineDistance(startLat, startLng, endLat, endLng)
                val distanceKm = kotlin.math.round(distance * 10) / 10
                updateState { copy(estimatedDistance = distanceKm.toString()) }
            }
        }
    }

    /**
     * Calculates the straight-line distance between two points using Haversine formula.
     * Used as fallback when Distance Matrix API is unavailable.
     * @return Distance in kilometers
     */
    private fun calculateHaversineDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
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

    /**
     * Converts degrees to radians.
     */
    private fun toRadians(degrees: Double): Double {
        return degrees * kotlin.math.PI / 180.0
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // SCHEDULE HANDLING
    // ══════════════════════════════════════════════════════════════════════════════

    private fun updateDepartureDate(value: String) {
        val error = if (value.isBlank()) "Departure date is required" else null
        updateState { copy(departureDate = value, departureDateError = error) }
        // Re-validate arrival if set
        if (currentState.arrivalDate.isNotBlank()) {
            validateArrivalDateTime(currentState.arrivalDate, currentState.arrivalTime)
        }
    }

    private fun updateDepartureTime(value: String) {
        val error = if (value.isBlank()) "Departure time is required" else null
        updateState { copy(departureTime = value, departureTimeError = error) }
        // Re-validate arrival if set
        if (currentState.arrivalTime.isNotBlank()) {
            validateArrivalDateTime(currentState.arrivalDate, currentState.arrivalTime)
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // TRIP CREATION
    // ══════════════════════════════════════════════════════════════════════════════

    private suspend fun createTrip() {
        if (!validateForm()) {
            sendEffect(Effect.ShowSnackbar("Please fill all required fields correctly"))
            return
        }

        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            val state = currentState

            // Customer is REQUIRED by the backend (trip must link to a managed customer).
            val selectedCustomerId = state.selectedCustomer?.id?.toIntOrNull()
            if (selectedCustomerId == null) {
                updateState { copy(isSaving = false) }
                sendEffect(Effect.ShowError("Please select a valid, active customer"))
                return@withContext
            }

            // Convert picker (DD-MM-YYYY + HH:mm) to UTC epoch-millis for the API.
            // Backend expects planned_start and planned_end as epoch-millis (numbers).
            val plannedStartMs = convertToEpochMillis(state.departureDate, state.departureTime) ?: 0L
            val plannedEndMs = if (state.arrivalDate.isNotBlank() && state.arrivalTime.isNotBlank()) {
                convertToEpochMillis(state.arrivalDate, state.arrivalTime) ?: plannedStartMs
            } else {
                // If no arrival date, use departure as planned_end
                plannedStartMs
            }

            logger.d(TAG_CREATE_TRIP_VM, "Creating trip: plannedStart=$plannedStartMs, plannedEnd=$plannedEndMs")

            val vehicleId = state.selectedVehicle!!.id.toIntOrNull() ?: 0
            val driverId = state.selectedDriver!!.id.toIntOrNull() ?: 0

            logger.d(TAG_CREATE_TRIP_VM, "Creating trip with vehicleId: $vehicleId, driverId: $driverId")
            logger.d(TAG_CREATE_TRIP_VM, "Vehicle: ${state.selectedVehicle.registrationNumber}, isOccupied: ${state.selectedVehicle.isOccupied}")
            logger.d(TAG_CREATE_TRIP_VM, "Driver: ${state.selectedDriver.firstName} ${state.selectedDriver.lastName}, isOccupied: ${state.selectedDriver.isOccupied}")

            val createTripData = CreateTripData(
                vehicleId = vehicleId,
                driverId = driverId,
                // v2 API required fields
                plannedStart = plannedStartMs,
                plannedEnd = plannedEndMs,
                // Legacy fields (optional)
                scheduledDate = plannedStartMs,
                startTime = plannedStartMs,
                deliveryDate = plannedEndMs,
                deliveryTime = plannedEndMs,
                startLocation = state.startLocation.trim(),
                startLat = state.startLat.toDoubleOrNull(),
                startLng = state.startLng.toDoubleOrNull(),
                endLocation = state.endLocation.trim(),
                endLat = state.endLat.toDoubleOrNull(),
                endLng = state.endLng.toDoubleOrNull(),
                // Estimated distance calculated from Google Distance Matrix API
                estimatedDistance = state.estimatedDistance.toDoubleOrNull(),
                cargoType = state.cargoType.lowercase(),
                cargoDescription = state.cargoDescription.takeIf { it.isNotBlank() },
                cargoLoadingWeight = state.cargoWeight.toDoubleOrNull(),
                weightUnit = state.weightUnit.takeIf { it.isNotBlank() }?.lowercase(),
                // Customer: send ONLY customer_id. The backend snapshots name/contact/etc. from the
                // customer record, and rejects free-text customer fields, so we must not send them.
                customerId = selectedCustomerId,
                customerName = null,
                customerContact = null,
                // Consignee / delivery (receiver) details — REQUIRED by the backend on create.
                deliveryAddress = state.deliveryAddress.trim(),
                deliveryPersonName = state.deliveryPersonName.trim(),
                deliveryContactNumber = state.deliveryContactNumber.trim(),
                priority = state.priority.takeIf { it.isNotBlank() }?.lowercase(),
                notes = state.notes.takeIf { it.isNotBlank() },
                tripPrice = state.tripPrice.toDoubleOrNull(),
                // purchasePrice = the COGS for this trip. Null when blank so the
                // backend default applies. Sent as purchase_price.
                purchasePrice = state.purchasePrice.toDoubleOrNull(),
                // sellingValue = the ACTUAL price (revenue). Defaults to the quoted
                // tripPrice when the user left the actual-price field blank.
                sellingValue = state.actualPrice.toDoubleOrNull() ?: state.tripPrice.toDoubleOrNull()
            )

            when (val result = createTripWithDataUseCase(createTripData)) {
                is Result.Success -> {
                    logger.d(TAG_CREATE_TRIP_VM, "Trip created successfully: ${result.data.id}")
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowSnackbar("Trip created successfully"))
                    sendEffect(Effect.TripCreated(result.data.id))
                    sendEffect(Effect.NavigateBack)
                }
                is Result.Error -> {
                    logger.e(TAG_CREATE_TRIP_VM, "Failed to create trip: ${result.message}")
                    updateState { copy(isSaving = false) }
                    val msg = result.message ?: "Failed to create trip"
                    // Backend rejects unknown (404) / inactive (409) customers — surface a clear hint.
                    val display = if (msg.contains("customer", ignoreCase = true) ||
                        msg.contains("inactive", ignoreCase = true)
                    ) {
                        "Select a valid, active customer"
                    } else {
                        msg
                    }
                    sendEffect(Effect.ShowError(display))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    /**
     * Validates all form fields before submission.
     */
    private fun validateForm(): Boolean {
        val selectedVehicle = currentState.selectedVehicle
        val selectedDriver = currentState.selectedDriver

        val vehicleError = when {
            selectedVehicle == null -> "Please select a vehicle"
            selectedVehicle.id.toIntOrNull() == null || selectedVehicle.id.toIntOrNull() == 0 -> "Invalid vehicle selected"
            selectedVehicle.isOccupied -> "Selected vehicle is currently occupied"
            else -> null
        }

        val driverError = when {
            selectedDriver == null -> "Please select a driver"
            selectedDriver.id.toIntOrNull() == null || selectedDriver.id.toIntOrNull() == 0 -> "Invalid driver selected"
            selectedDriver.isOccupied -> "Selected driver is currently occupied"
            else -> null
        }

        val startLocationError = if (currentState.startLocation.isBlank()) "Start location is required" else null
        val endLocationError = if (currentState.endLocation.isBlank()) "End location is required" else null
        val departureDateError = if (currentState.departureDate.isBlank()) "Departure date is required" else null
        val departureTimeError = if (currentState.departureTime.isBlank()) "Departure time is required" else null
        val cargoTypeError = if (currentState.cargoType.isBlank()) "Please select cargo type" else null

        // Cargo weight - now required
        val cargoWeightError = currentState.cargoWeight.let { weight ->
            when {
                weight.isBlank() -> "Cargo weight is required"
                weight.toDoubleOrNull() == null -> "Enter valid weight"
                weight.toDoubleOrNull()!! <= 0 -> "Weight must be positive"
                else -> null
            }
        }

        // Weight unit - now required
        val weightUnitError = if (currentState.weightUnit.isBlank()) "Please select weight unit" else null

        // Customer is REQUIRED — the trip must link to a managed customer selected from the list.
        val customerNameError = if (currentState.selectedCustomer == null) "Please select a customer" else null
        val customerContactError: String? = null

        // Consignee / delivery (receiver) — all three REQUIRED by the backend (binding:required).
        val deliveryAddressError = if (currentState.deliveryAddress.isBlank()) {
            "Delivery address is required"
        } else null
        val deliveryPersonNameError = if (currentState.deliveryPersonName.isBlank()) {
            "Delivery person name is required"
        } else null
        val deliveryContactNumberError = currentState.deliveryContactNumber.let { contact ->
            when {
                contact.isBlank() -> "Delivery contact number is required"
                !ValidationUtils.isValidIndianMobile(contact) -> "Enter a valid 10-digit mobile number"
                else -> null
            }
        }

        // Trip price - required for owner/manager
        val tripPriceError = if (currentState.canViewTripPrice) {
            currentState.tripPrice.let { price ->
                when {
                    price.isBlank() -> "Trip price is required"
                    price.toDoubleOrNull() == null -> "Enter valid amount"
                    price.toDoubleOrNull()!! <= 0 -> "Price must be positive"
                    else -> null
                }
            }
        } else null

        // Validate arrival date if provided
        val arrivalDateError = when {
            currentState.arrivalDate.isNotBlank() && currentState.arrivalTime.isBlank() -> "Please enter arrival time"
            currentState.arrivalDate.isBlank() && currentState.arrivalTime.isNotBlank() -> "Please enter arrival date"
            currentState.arrivalDate.isNotBlank() && currentState.arrivalTime.isNotBlank() -> {
                // Check if arrival is after departure
                if (compareDateTimes(
                        currentState.departureDate, currentState.departureTime,
                        currentState.arrivalDate, currentState.arrivalTime
                    )) {
                    "Arrival must be after departure"
                } else null
            }
            else -> null
        }

        updateState {
            copy(
                vehicleError = vehicleError,
                driverError = driverError,
                startLocationError = startLocationError,
                endLocationError = endLocationError,
                departureDateError = departureDateError,
                departureTimeError = departureTimeError,
                cargoTypeError = cargoTypeError,
                cargoWeightError = cargoWeightError,
                weightUnitError = weightUnitError,
                customerNameError = customerNameError,
                customerContactError = customerContactError,
                deliveryAddressError = deliveryAddressError,
                deliveryPersonNameError = deliveryPersonNameError,
                deliveryContactNumberError = deliveryContactNumberError,
                tripPriceError = tripPriceError,
                arrivalDateError = arrivalDateError
            )
        }

        val baseValid = vehicleError == null &&
                driverError == null &&
                startLocationError == null &&
                endLocationError == null &&
                departureDateError == null &&
                departureTimeError == null &&
                cargoTypeError == null &&
                cargoWeightError == null &&
                weightUnitError == null &&
                customerNameError == null &&
                customerContactError == null &&
                deliveryAddressError == null &&
                deliveryPersonNameError == null &&
                deliveryContactNumberError == null &&
                arrivalDateError == null

        return if (currentState.canViewTripPrice) {
            baseValid && tripPriceError == null
        } else {
            baseValid
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ══════════════════════════════════════════════════════════════════════════════

    // ══════════════════════════════════════════════════════════════════════════════
    // CUSTOMER SELECTION METHODS
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Select a customer from the local database.
     * Auto-populates customerName and customerContact.
     */
    private fun selectCustomer(customer: com.ijs.customer.domain.entity.Customer) {
        // Pre-fill the consignee/delivery fields from the customer as EDITABLE defaults.
        // The consignee is conceptually distinct from the billing customer but usually starts
        // as the same party, so we seed blank delivery fields and never overwrite user input.
        val prefilledAddress = if (currentState.deliveryAddress.isBlank()) {
            customer.companyAddress.orEmpty()
        } else currentState.deliveryAddress
        val prefilledPersonName = if (currentState.deliveryPersonName.isBlank()) {
            customer.personName.ifBlank { customer.companyName }
        } else currentState.deliveryPersonName
        val prefilledContact = if (currentState.deliveryContactNumber.isBlank()) {
            customer.primaryContact.filter { it.isDigit() }.take(10)
        } else currentState.deliveryContactNumber

        updateState {
            copy(
                selectedCustomer = customer,
                customerName = customer.companyName,
                customerContact = customer.primaryContact,
                customerSearchQuery = customer.companyName,
                showCustomerDropdown = false,
                showCustomerBottomSheet = false,
                customerNameError = null,
                customerContactError = null,
                deliveryAddress = prefilledAddress,
                deliveryPersonName = prefilledPersonName,
                deliveryContactNumber = prefilledContact,
                // Clear stale errors for any field we just pre-filled with a value.
                deliveryAddressError = if (prefilledAddress.isNotBlank()) null else deliveryAddressError,
                deliveryPersonNameError = if (prefilledPersonName.isNotBlank()) null else deliveryPersonNameError,
                deliveryContactNumberError = if (prefilledContact.isNotBlank() &&
                    ValidationUtils.isValidIndianMobile(prefilledContact)
                ) null else deliveryContactNumberError
            )
        }
        logger.d(TAG_CREATE_TRIP_VM, "Selected customer: ${customer.companyName} (ID: ${customer.id}")
    }

    /**
     * Search customers from the local database based on query.
     */
    private fun searchCustomers(query: String) {
        updateState {
            copy(
                customerSearchQuery = query,
                showCustomerDropdown = query.isNotBlank()
            )
        }

        if (query.isBlank()) {
            updateState { copy(customerSuggestions = emptyList(), showCustomerDropdown = false) }
            return
        }

        // Filter customers from allCustomers list
        val filteredCustomers = currentState.allCustomers.filter { customer ->
            customer.companyName.contains(query, ignoreCase = true) ||
            customer.personName.contains(query, ignoreCase = true) ||
            customer.primaryContact.contains(query, ignoreCase = true)
        }.take(5) // Limit to 5 suggestions

        updateState {
            copy(
                customerSuggestions = filteredCustomers,
                showCustomerDropdown = filteredCustomers.isNotEmpty()
            )
        }
    }

    /**
     * Clear customer selection and allow manual entry.
     */
    private fun clearCustomerSelection() {
        updateState {
            copy(
                selectedCustomer = null,
                customerName = "",
                customerContact = "",
                customerSearchQuery = "",
                customerSuggestions = emptyList(),
                showCustomerDropdown = false
            )
        }
    }

    /**
     * Load customers from local database for autocomplete.
     * Should be called during initialization or when customers list needs refresh.
     */
    fun loadCustomers(customers: List<com.ijs.customer.domain.entity.Customer>) {
        updateState { copy(allCustomers = customers) }
        logger.d(TAG_CREATE_TRIP_VM, "Loaded ${customers.size} customers for autocomplete")
    }
}
