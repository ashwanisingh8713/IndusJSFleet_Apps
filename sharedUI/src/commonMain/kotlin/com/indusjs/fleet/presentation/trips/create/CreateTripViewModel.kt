package com.indusjs.fleet.presentation.trips.create

import co.touchlab.kermit.Logger
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.util.convertToIsoDateTime
import com.indusjs.fleet.data.datasource.location.GooglePlacesService
import com.indusjs.fleet.data.datasource.location.PlacePrediction
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.domain.entity.trip.CreateTripData
import com.indusjs.fleet.domain.usecase.driver.GetDriversUseCase
import com.indusjs.fleet.domain.usecase.trip.CreateTripWithDataUseCase
import com.indusjs.fleet.domain.usecase.vehicle.GetVehiclesUseCase
import com.indusjs.fleet.presentation.trips.create.CreateTripContract.Effect
import com.indusjs.fleet.presentation.trips.create.CreateTripContract.Intent
import com.indusjs.fleet.presentation.trips.create.CreateTripContract.State
import dev.zacsweers.metro.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
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
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val getDriversUseCase: GetDriversUseCase,
    private val createTripWithDataUseCase: CreateTripWithDataUseCase,
    private val userLocalDataSource: UserLocalDataSource,
    private val googlePlacesService: GooglePlacesService? = null
) : MviViewModel<State, Intent, Effect>(State()) {

    private val log = Logger.withTag("CreateTripViewModel")
    private var startLocationSearchJob: Job? = null
    private var endLocationSearchJob: Job? = null

    init {
        // Load user role on init
        viewModelScope.launch(dispatcherProvider.io) {
            val userRole = try {
                userLocalDataSource.getUserRole() ?: ""
            } catch (e: Exception) {
                log.e { "Failed to get user role: ${e.message}" }
                ""
            }
            val normalizedRole = userRole.lowercase().replace("_", "")
            log.d { "CreateTripViewModel - userRole: '$userRole', normalized: '$normalizedRole', canViewTripPrice: ${normalizedRole == "owner" || normalizedRole == "generalmanager"}" }
            updateState { copy(userRole = userRole) }
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
            is Intent.UpdateArrivalDate -> updateState { copy(arrivalDate = intent.value) }
            is Intent.UpdateArrivalTime -> updateState { copy(arrivalTime = intent.value) }

            // Cargo & Customer updates
            is Intent.UpdateCargoType -> updateState { copy(cargoType = intent.value) }
            is Intent.UpdateCargoDescription -> updateState { copy(cargoDescription = intent.value) }
            is Intent.UpdateCargoWeight -> updateState { copy(cargoWeight = intent.value) }
            is Intent.UpdateCustomerName -> updateState { copy(customerName = intent.value) }
            is Intent.UpdateCustomerContact -> updateState { copy(customerContact = intent.value) }
            is Intent.UpdatePriority -> updateState { copy(priority = intent.value) }
            is Intent.UpdateNotes -> updateState { copy(notes = intent.value) }

            // Pricing updates
            is Intent.UpdateTripPrice -> updateState { copy(tripPrice = intent.value) }

            // Actions
            is Intent.CreateTrip -> createTrip()

            // Navigation & errors
            is Intent.NavigateBack -> sendEffect(Effect.NavigateBack)
            is Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    private suspend fun loadVehiclesAndDrivers() {
        updateState { copy(isLoadingData = true) }

        withContext(dispatcherProvider.io) {
            // Load vehicles
            getVehiclesUseCase().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        updateState { copy(vehicles = result.data) }
                    }
                    is Result.Error -> {
                        sendEffect(Effect.ShowError("Failed to load vehicles: ${result.message}"))
                    }
                    is Result.Loading -> { /* Already handled */ }
                }
            }
        }

        withContext(dispatcherProvider.io) {
            // Load drivers
            getDriversUseCase().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        updateState { copy(drivers = result.data, isLoadingData = false) }
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
                CoroutineScope(dispatcherProvider.main).launch {
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

    private fun updateDepartureDate(value: String) {
        val error = if (value.isBlank()) "Departure date is required" else null
        updateState { copy(departureDate = value, departureDateError = error) }
    }

    private fun updateDepartureTime(value: String) {
        val error = if (value.isBlank()) "Departure time is required" else null
        updateState { copy(departureTime = value, departureTimeError = error) }
    }

    private suspend fun createTrip() {
        if (!validateForm()) {
            sendEffect(Effect.ShowSnackbar("Please fill all required fields correctly"))
            return
        }

        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            val state = currentState

            // Convert to ISO 8601 format for v2 API: YYYY-MM-DDTHH:MM:00Z
            // Backend expects planned_start and planned_end in ISO 8601 format
            val plannedStartIso = convertToIsoDateTime(state.departureDate, state.departureTime)
            val plannedEndIso = if (state.arrivalDate.isNotBlank()) {
                convertToIsoDateTime(state.arrivalDate, state.arrivalTime)
            } else {
                // If no arrival date, use departure date as planned_end
                plannedStartIso
            }

            log.d { "Creating trip with ISO: plannedStart=$plannedStartIso, plannedEnd=$plannedEndIso" }

            val vehicleId = state.selectedVehicle!!.id.toIntOrNull() ?: 0
            val driverId = state.selectedDriver!!.id.toIntOrNull() ?: 0

            log.d { "Creating trip with vehicleId: $vehicleId, driverId: $driverId" }
            log.d { "Vehicle: ${state.selectedVehicle.registrationNumber}, isOccupied: ${state.selectedVehicle.isOccupied}" }
            log.d { "Driver: ${state.selectedDriver.firstName} ${state.selectedDriver.lastName}, isOccupied: ${state.selectedDriver.isOccupied}" }

            val createTripData = CreateTripData(
                vehicleId = vehicleId,
                driverId = driverId,
                // v2 API required fields
                plannedStart = plannedStartIso,
                plannedEnd = plannedEndIso,
                // Legacy fields (optional)
                scheduledDate = plannedStartIso,
                startTime = plannedStartIso,
                deliveryDate = plannedEndIso,
                deliveryTime = plannedEndIso,
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
                customerName = state.customerName.takeIf { it.isNotBlank() },
                customerContact = state.customerContact.takeIf { it.isNotBlank() },
                priority = state.priority.takeIf { it.isNotBlank() }?.lowercase(),
                notes = state.notes.takeIf { it.isNotBlank() },
                tripPrice = state.tripPrice.toDoubleOrNull()
            )

            when (val result = createTripWithDataUseCase(createTripData)) {
                is Result.Success -> {
                    log.d { "Trip created successfully: ${result.data.id}" }
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowSnackbar("Trip created successfully"))
                    sendEffect(Effect.TripCreated(result.data.id))
                    sendEffect(Effect.NavigateBack)
                }
                is Result.Error -> {
                    log.e { "Failed to create trip: ${result.message}" }
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to create trip"))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

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

        updateState {
            copy(
                vehicleError = vehicleError,
                driverError = driverError,
                startLocationError = startLocationError,
                endLocationError = endLocationError,
                departureDateError = departureDateError,
                departureTimeError = departureTimeError
            )
        }

        return vehicleError == null &&
                driverError == null &&
                startLocationError == null &&
                endLocationError == null &&
                departureDateError == null &&
                departureTimeError == null
    }

    /**
     * Converts raw date digits (DDMMYYYY) and time (HHMM) to ISO 8601 format.
     * The v2 API expects dates in ISO 8601 format: YYYY-MM-DDTHH:MM:SSZ
     * If time is not provided, uses 00:00:00.
     */
    private fun convertToIsoDateTime(rawDate: String, rawTime: String = ""): String {
        if (rawDate.isBlank()) return ""

        // Raw date format is DDMMYYYY (8 digits)
        val dateDigits = rawDate.filter { it.isDigit() }
        if (dateDigits.length != 8) return rawDate

        val day = dateDigits.substring(0, 2)
        val month = dateDigits.substring(2, 4)
        val year = dateDigits.substring(4, 8)

        // Raw time format is HHMM (4 digits)
        val timeDigits = rawTime.filter { it.isDigit() }
        val hours = if (timeDigits.length >= 2) timeDigits.substring(0, 2) else "00"
        val minutes = if (timeDigits.length >= 4) timeDigits.substring(2, 4) else "00"

        // Return ISO 8601 format: YYYY-MM-DDTHH:MM:00Z
        return "$year-$month-${day}T$hours:$minutes:00Z"
    }

    /**
     * Formats raw time digits (HHMM) to HH:MM format.
     * If the input is empty or invalid, returns empty string.
     */
    private fun formatTimeForApi(rawTime: String): String {
        if (rawTime.isBlank()) return ""

        // Raw format is HHMM (4 digits)
        val digitsOnly = rawTime.filter { it.isDigit() }
        return if (digitsOnly.length == 4) {
            val hours = digitsOnly.substring(0, 2)
            val minutes = digitsOnly.substring(2, 4)
            "$hours:$minutes" // HH:MM
        } else if (digitsOnly.length == 2) {
            "${digitsOnly}:00" // Just hours provided
        } else {
            rawTime // Return as-is if not in expected format
        }
    }
}
