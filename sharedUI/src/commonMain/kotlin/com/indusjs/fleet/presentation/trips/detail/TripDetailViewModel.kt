package com.indusjs.fleet.presentation.trips.detail

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.entity.trip.TripLocation
import com.indusjs.fleet.domain.entity.trip.TripStatus
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.indusjs.fleet.domain.usecase.trip.CancelTripUseCase
import com.indusjs.fleet.domain.usecase.trip.GetTripByIdUseCase
import com.indusjs.fleet.domain.usecase.trip.UpdateTripStatusUseCase
import com.indusjs.fleet.domain.usecase.trip.UpdateTripUseCase
import com.indusjs.fleet.presentation.trips.detail.TripDetailContract.Effect
import com.indusjs.fleet.presentation.trips.detail.TripDetailContract.Intent
import com.indusjs.fleet.presentation.trips.detail.TripDetailContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Trip Detail screen implementing MVI pattern.
 */
@Inject
class TripDetailViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getTripByIdUseCase: GetTripByIdUseCase,
    private val updateTripUseCase: UpdateTripUseCase,
    private val updateTripStatusUseCase: UpdateTripStatusUseCase,
    private val cancelTripUseCase: CancelTripUseCase,
    private val costsRepository: CostsRepository
) : MviViewModel<State, Intent, Effect>(State()) {

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadTrip -> loadTrip(intent.tripId)
            is Intent.Refresh -> currentState.trip?.let { loadTrip(it.id) }

            // Edit mode
            is Intent.EnterEditMode -> enterEditMode()
            is Intent.ExitEditMode -> exitEditMode()

            // Field updates
            is Intent.UpdateStartLocation -> updateStartLocation(intent.address)
            is Intent.UpdateStartLat -> updateState { copy(startLat = intent.value) }
            is Intent.UpdateStartLng -> updateState { copy(startLng = intent.value) }
            is Intent.UpdateEndLocation -> updateEndLocation(intent.address)
            is Intent.UpdateEndLat -> updateState { copy(endLat = intent.value) }
            is Intent.UpdateEndLng -> updateState { copy(endLng = intent.value) }
            is Intent.UpdateDistance -> updateState { copy(distance = intent.value) }
            is Intent.UpdatePlannedStart -> updateState { copy(plannedStart = intent.value) }
            is Intent.UpdatePlannedEnd -> updateState { copy(plannedEnd = intent.value) }
            is Intent.UpdateCargoType -> updateState { copy(cargoType = intent.value) }
            is Intent.UpdateCargoDescription -> updateState { copy(cargoDescription = intent.value) }
            is Intent.UpdateCustomerName -> updateState { copy(customerName = intent.value) }
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
                    updateState {
                        copy(
                            isLoading = false,
                            trip = trip,
                            // Populate editable fields
                            startLocationAddress = trip.startLocation?.address ?: "",
                            startLat = trip.startLocation?.latitude?.toString() ?: "",
                            startLng = trip.startLocation?.longitude?.toString() ?: "",
                            endLocationAddress = trip.endLocation?.address ?: "",
                            endLat = trip.endLocation?.latitude?.toString() ?: "",
                            endLng = trip.endLocation?.longitude?.toString() ?: "",
                            distance = trip.distance.takeIf { it > 0 }?.toString() ?: "",
                            plannedStart = trip.scheduledStartTime ?: "",
                            plannedEnd = trip.actualEndTime ?: "",
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
                // Don't show error for costs - it's not critical
            }
            is Result.Loading -> { /* Not applicable */ }
        }
    }

    private fun enterEditMode() {
        // Only allow editing if trip is in PLANNED status
        val trip = currentState.trip
        if (trip != null && trip.status != TripStatus.PLANNED) {
            sendEffect(Effect.ShowSnackbar("Only planned trips can be edited"))
            return
        }
        updateState { copy(isEditMode = true) }
    }

    private fun exitEditMode() {
        // Reset to original values
        val trip = currentState.trip
        if (trip != null) {
            updateState {
                copy(
                    isEditMode = false,
                    startLocationAddress = trip.startLocation?.address ?: "",
                    startLat = trip.startLocation?.latitude?.toString() ?: "",
                    startLng = trip.startLocation?.longitude?.toString() ?: "",
                    endLocationAddress = trip.endLocation?.address ?: "",
                    endLat = trip.endLocation?.latitude?.toString() ?: "",
                    endLng = trip.endLocation?.longitude?.toString() ?: "",
                    distance = trip.distance.takeIf { it > 0 }?.toString() ?: "",
                    plannedStart = trip.scheduledStartTime ?: "",
                    plannedEnd = trip.actualEndTime ?: "",
                    cargoType = trip.cargoType ?: "",
                    cargoDescription = trip.cargoDescription ?: "",
                    customerName = trip.customerName ?: "",
                    priority = trip.priority ?: "",
                    notes = trip.notes ?: "",
                    // Clear errors
                    startLocationError = null,
                    endLocationError = null
                )
            }
        } else {
            updateState { copy(isEditMode = false) }
        }
    }

    private fun updateStartLocation(address: String) {
        val error = if (address.isBlank()) "Start location is required" else null
        updateState { copy(startLocationAddress = address, startLocationError = error) }
    }

    private fun updateEndLocation(address: String) {
        val error = if (address.isBlank()) "End location is required" else null
        updateState { copy(endLocationAddress = address, endLocationError = error) }
    }

    private suspend fun updateStatus(status: TripStatus) {
        val tripId = currentState.trip?.id ?: return
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            when (val result = updateTripStatusUseCase(tripId, status)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isSaving = false,
                            trip = result.data
                        )
                    }
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
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            val startLocation = TripLocation(
                latitude = currentState.startLat.toDoubleOrNull() ?: 0.0,
                longitude = currentState.startLng.toDoubleOrNull() ?: 0.0,
                address = currentState.startLocationAddress.trim()
            )

            val endLocation = TripLocation(
                latitude = currentState.endLat.toDoubleOrNull() ?: 0.0,
                longitude = currentState.endLng.toDoubleOrNull() ?: 0.0,
                address = currentState.endLocationAddress.trim()
            )

            val updatedTrip = currentTrip.copy(
                startLocation = startLocation,
                endLocation = endLocation,
                distance = currentState.distance.toDoubleOrNull() ?: currentTrip.distance,
                scheduledStartTime = currentState.plannedStart.takeIf { it.isNotBlank() },
                actualEndTime = currentState.plannedEnd.takeIf { it.isNotBlank() },
                cargoType = currentState.cargoType.takeIf { it.isNotBlank() },
                cargoDescription = currentState.cargoDescription.takeIf { it.isNotBlank() },
                customerName = currentState.customerName.takeIf { it.isNotBlank() },
                priority = currentState.priority.takeIf { it.isNotBlank() },
                notes = currentState.notes.takeIf { it.isNotBlank() }
            )

            when (val result = updateTripUseCase(updatedTrip)) {
                is Result.Success -> {
                    val trip = result.data
                    updateState {
                        copy(
                            isSaving = false,
                            isEditMode = false,
                            trip = trip,
                            startLocationAddress = trip.startLocation?.address ?: "",
                            endLocationAddress = trip.endLocation?.address ?: ""
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
        val startLocationError = if (currentState.startLocationAddress.isBlank()) "Start location is required" else null
        val endLocationError = if (currentState.endLocationAddress.isBlank()) "End location is required" else null

        updateState {
            copy(
                startLocationError = startLocationError,
                endLocationError = endLocationError
            )
        }

        return startLocationError == null && endLocationError == null
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
}

