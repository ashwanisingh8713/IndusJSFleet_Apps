package com.indusjs.fleet.presentation.trips.create

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.entity.trip.CreateTripData
import com.indusjs.fleet.domain.usecase.driver.GetDriversUseCase
import com.indusjs.fleet.domain.usecase.trip.CreateTripWithDataUseCase
import com.indusjs.fleet.domain.usecase.vehicle.GetVehiclesUseCase
import com.indusjs.fleet.presentation.trips.create.CreateTripContract.Effect
import com.indusjs.fleet.presentation.trips.create.CreateTripContract.Intent
import com.indusjs.fleet.presentation.trips.create.CreateTripContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Create Trip screen implementing MVI pattern.
 */
@Inject
class CreateTripViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getVehiclesUseCase: GetVehiclesUseCase,
    private val getDriversUseCase: GetDriversUseCase,
    private val createTripWithDataUseCase: CreateTripWithDataUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadVehiclesAndDrivers -> loadVehiclesAndDrivers()

            // Vehicle & Driver selection
            is Intent.SelectVehicle -> {
                updateState { copy(selectedVehicle = intent.vehicle, vehicleError = null, showVehicleDropdown = false) }
            }
            is Intent.SelectDriver -> {
                updateState { copy(selectedDriver = intent.driver, driverError = null, showDriverDropdown = false) }
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

            // Convert Indian date format (DD-MM-YYYY) to ISO format (YYYY-MM-DD)
            val departureDateISO = convertToISODate(state.departureDate)
            val arrivalDateISO = convertToISODate(state.arrivalDate)

            // Build planned start from departure date and time
            val plannedStart = "${departureDateISO}T${state.departureTime}:00Z"

            // Build planned end from arrival date and time (if provided), otherwise use departure
            val plannedEnd = if (state.arrivalDate.isNotBlank() && state.arrivalTime.isNotBlank()) {
                "${arrivalDateISO}T${state.arrivalTime}:00Z"
            } else if (state.arrivalTime.isNotBlank()) {
                // If only arrival time is provided, use departure date
                "${departureDateISO}T${state.arrivalTime}:00Z"
            } else {
                plannedStart
            }

            val createTripData = CreateTripData(
                vehicleId = state.selectedVehicle!!.id.toIntOrNull() ?: 0,
                driverId = state.selectedDriver!!.id.toIntOrNull() ?: 0,
                scheduledDate = "${departureDateISO}T00:00:00Z",
                startTime = plannedStart,
                plannedStart = plannedStart,
                plannedEnd = plannedEnd,
                startLocation = state.startLocation.trim(),
                startLat = state.startLat.toDoubleOrNull(),
                startLng = state.startLng.toDoubleOrNull(),
                endLocation = state.endLocation.trim(),
                endLat = state.endLat.toDoubleOrNull(),
                endLng = state.endLng.toDoubleOrNull(),
                cargoType = state.cargoType.lowercase(),
                cargoDescription = state.cargoDescription.takeIf { it.isNotBlank() },
                cargoWeight = state.cargoWeight.toDoubleOrNull(),
                customerName = state.customerName.takeIf { it.isNotBlank() },
                customerContact = state.customerContact.takeIf { it.isNotBlank() },
                priority = state.priority.takeIf { it.isNotBlank() }?.lowercase(),
                notes = state.notes.takeIf { it.isNotBlank() }
            )

            when (val result = createTripWithDataUseCase(createTripData)) {
                is Result.Success -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowSnackbar("Trip created successfully"))
                    sendEffect(Effect.TripCreated(result.data.id))
                    sendEffect(Effect.NavigateBack)
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to create trip"))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    private fun validateForm(): Boolean {
        val vehicleError = if (currentState.selectedVehicle == null) "Please select a vehicle" else null
        val driverError = if (currentState.selectedDriver == null) "Please select a driver" else null
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
     * Converts Indian date format (DD-MM-YYYY) to ISO format (YYYY-MM-DD).
     * If the input is empty or invalid, returns empty string.
     */
    private fun convertToISODate(indianDate: String): String {
        if (indianDate.isBlank()) return ""

        val parts = indianDate.split("-")
        return if (parts.size == 3 && parts[0].length == 2 && parts[1].length == 2 && parts[2].length == 4) {
            "${parts[2]}-${parts[1]}-${parts[0]}" // YYYY-MM-DD
        } else {
            indianDate // Return as-is if not in expected format
        }
    }
}

