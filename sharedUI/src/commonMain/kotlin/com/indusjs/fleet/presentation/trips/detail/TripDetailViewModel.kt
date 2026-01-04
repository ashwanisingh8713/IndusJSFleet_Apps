package com.indusjs.fleet.presentation.trips.detail

import co.touchlab.kermit.Logger
import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.core.util.convertToIsoDateTime
import com.indusjs.fleet.data.datasource.location.GooglePlacesService
import com.indusjs.fleet.data.datasource.location.PlacePrediction
import com.indusjs.fleet.data.model.trip.UpdateTripRequest
import com.indusjs.fleet.domain.entity.trip.TripStatus
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.entity.driver.Driver
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.indusjs.fleet.domain.repository.trip.TripRepository
import com.indusjs.fleet.domain.usecase.driver.GetDriversUseCase
import com.indusjs.fleet.domain.usecase.trip.CancelTripUseCase
import com.indusjs.fleet.domain.usecase.trip.GetTripByIdUseCase
import com.indusjs.fleet.domain.usecase.trip.UpdateTripStatusUseCase
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
    private val googlePlacesService: GooglePlacesService? = null
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

            // Customer updates
            is Intent.UpdateCustomerName -> updateState { copy(customerName = intent.value) }
            is Intent.UpdateCustomerContact -> updateState { copy(customerContact = intent.value) }

            // Other updates
            is Intent.UpdatePriority -> updateState { copy(priority = intent.value) }
            is Intent.UpdateNotes -> updateState { copy(notes = intent.value) }

            // Actions
            is Intent.UpdateStatus -> updateStatus(intent.status)
            is Intent.SaveChanges -> saveChanges()
            is Intent.CancelTrip -> sendEffect(Effect.ShowCancelConfirmation)
            is Intent.ConfirmCancel -> confirmCancel()

            // Navigation & errors
            is Intent.NavigateBack -> sendEffect(Effect.NavigateBack)
            is Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    private suspend fun loadTrip(tripId: String) {
        updateState { copy(isLoading = true, error = null, tripId = tripId) }

        withContext(dispatcherProvider.io) {
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
                            customerName = trip.customerName ?: "",
                            priority = trip.priority ?: "",
                            notes = trip.notes ?: ""
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
        if (trip != null && trip.status != TripStatus.PLANNED) {
            sendEffect(Effect.ShowSnackbar("Only planned trips can be edited"))
            return
        }

        updateState { copy(isEditMode = true, isLoadingVehiclesDrivers = true) }

        // Load vehicles and drivers for selection
        loadVehiclesAndDrivers()
    }

    private suspend fun loadVehiclesAndDrivers() {
        withContext(dispatcherProvider.io) {
            // Load vehicles
            getVehiclesUseCase().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val trip = currentState.trip
                        val currentVehicle = result.data.find { it.id == trip?.vehicleId }
                        updateState {
                            copy(
                                vehicles = result.data,
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
            // Load drivers
            getDriversUseCase().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val trip = currentState.trip
                        val currentDriver = result.data.find { it.id == trip?.driverId }
                        updateState {
                            copy(
                                drivers = result.data,
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
                    cargoWeight = "",
                    customerName = trip.customerName ?: "",
                    customerContact = "",
                    priority = trip.priority ?: "",
                    notes = trip.notes ?: "",
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
                customerName = state.customerName.takeIf { it.isNotBlank() },
                customerContact = state.customerContact.takeIf { it.isNotBlank() },
                priority = state.priority.lowercase().takeIf { it.isNotBlank() },
                notes = state.notes.takeIf { it.isNotBlank() }
            )

            log.d { "Updating trip ${currentTrip.id} with request: $request" }

            when (val result = tripRepository.updateTripWithRequest(currentTrip.id, request)) {
                is Result.Success -> {
                    val trip = result.data
                    updateState {
                        copy(
                            isSaving = false,
                            isEditMode = false,
                            trip = trip,
                            startLocationAddress = trip.startLocation?.address ?: "",
                            endLocationAddress = trip.endLocation?.address ?: "",
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
}
