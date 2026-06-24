package com.ijs.trip.presentation.detail

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.trip.TAG_TRIP_DETAIL_ACTION
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.util.convertToEpochMillis
import com.ijs.trip.presentation.detail.TripDetailContract.Effect
import com.ijs.trip.data.model.UpdateTripRequest
import com.ijs.trip.domain.entity.TripStatus
import com.ijs.trip.domain.repository.TripRepository
import com.ijs.trip.domain.usecase.CancelTripUseCase
import com.ijs.trip.domain.usecase.UpdateTripStatusUseCase
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.pdfreport.model.TripCostItem
import com.indusjs.pdfreport.model.TripCostsPdfData
import kotlinx.coroutines.withContext

/**
 * Handles trip actions: save, validate, cancel, status updates, PDF export, and navigation.
 */
internal class TripDetailActionHandler(
    private val stateManager: TripDetailStateManager,
    private val dispatcherProvider: DispatcherProvider,
    private val tripRepository: TripRepository,
    private val updateTripStatusUseCase: UpdateTripStatusUseCase,
    private val cancelTripUseCase: CancelTripUseCase,
    private val logger: FleetLogger
) {
suspend fun saveChanges() {
        if (!validateForm()) {
            stateManager.emitEffect(Effect.ShowSnackbar("Please fill all required fields correctly"))
            return
        }

        val currentTrip = stateManager.currentTripState.trip ?: return
        val state = stateManager.currentTripState
        stateManager.updateTripState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            val plannedStartMs = if (state.departureDate.isNotBlank() && state.departureTime.isNotBlank()) {
                convertToEpochMillis(state.departureDate, state.departureTime)
            } else null

            val plannedEndMs = if (state.arrivalDate.isNotBlank() && state.arrivalTime.isNotBlank()) {
                convertToEpochMillis(state.arrivalDate, state.arrivalTime)
            } else null

            val request = UpdateTripRequest(
                vehicleId = state.selectedVehicle?.id?.toIntOrNull(),
                driverId = state.selectedDriver?.id?.toIntOrNull(),
                plannedStart = plannedStartMs,
                plannedEnd = plannedEndMs,
                scheduledDate = plannedStartMs,
                startTime = plannedStartMs,
                deliveryDate = plannedEndMs,
                deliveryTime = plannedEndMs,
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
                // Customer: send only customer_id (optional reassign; null/omitted leaves it unchanged).
                // Backend snapshots name/contact from the customer record — do not send free-text.
                customerId = state.selectedCustomer?.id?.toIntOrNull(),
                // purchase_price (COGS) — now forwarded on update too, not only on create.
                purchasePrice = state.purchasePrice.toDoubleOrNull(),
                tripPrice = state.tripPrice.toDoubleOrNull(),
                // selling_value = the ACTUAL price (revenue). Use the editable actual-price
                // field, falling back to the quoted tripPrice when the user left it blank.
                sellingValue = state.actualPrice.toDoubleOrNull() ?: state.tripPrice.toDoubleOrNull(),
                priority = state.priority.lowercase().takeIf { it.isNotBlank() },
                notes = state.notes.takeIf { it.isNotBlank() }
            )

            logger.d(TAG_TRIP_DETAIL_ACTION, "=== UPDATE TRIP REQUEST ===")
            logger.d(TAG_TRIP_DETAIL_ACTION, "TripPrice: '${state.tripPrice}' -> ${state.tripPrice.toDoubleOrNull()}")
            logger.d(TAG_TRIP_DETAIL_ACTION, "WeightUnit: '${state.weightUnit}', CargoWeight: '${state.cargoWeight}'")

            when (val result = tripRepository.updateTripWithRequest(currentTrip.id, request)) {
                is Result.Success -> {
                    val trip = result.data
                    logger.d(TAG_TRIP_DETAIL_ACTION, "Trip updated. tripPrice: ${trip.tripPrice}, weightUnit: ${trip.weightUnit}")
                    stateManager.updateTripState {
                        copy(
                            isSaving = false,
                            isEditMode = false,
                            trip = trip,
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
                            // Re-seed the actual-price (revenue) and purchase-price (COGS) editors
                            // from the saved trip, defaulting the actual price to the quote.
                            actualPrice = (trip.sellingValue ?: trip.tripPrice)?.toString() ?: "",
                            purchasePrice = trip.purchasePrice?.toString() ?: "",
                            vehicles = emptyList(),
                            drivers = emptyList(),
                            selectedVehicle = null,
                            selectedDriver = null
                        )
                    }
                    stateManager.emitEffect(Effect.ShowSnackbar("Trip updated successfully"))
                    stateManager.emitEffect(Effect.TripUpdated)
                }
                is Result.Error -> {
                    stateManager.updateTripState { copy(isSaving = false) }
                    stateManager.emitEffect(Effect.ShowError(result.message ?: "Failed to update trip"))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    fun validateForm(): Boolean {
        val state = stateManager.currentTripState

        val vehicleError = if (state.selectedVehicle == null) "Please select a vehicle" else null
        val driverError = if (state.selectedDriver == null) "Please select a driver" else null
        val startLocationError = if (state.startLocationAddress.isBlank()) "Start location is required" else null
        val endLocationError = if (state.endLocationAddress.isBlank()) "End location is required" else null

        stateManager.updateTripState {
            copy(
                vehicleError = vehicleError,
                driverError = driverError,
                startLocationError = startLocationError,
                endLocationError = endLocationError
            )
        }

        return vehicleError == null && driverError == null &&
                startLocationError == null && endLocationError == null
    }

    suspend fun updateStatus(status: TripStatus) {
        val tripId = stateManager.currentTripState.trip?.id ?: return
        stateManager.updateTripState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            when (val result = updateTripStatusUseCase(tripId, status)) {
                is Result.Success -> {
                    stateManager.updateTripState { copy(isSaving = false, trip = result.data) }
                    stateManager.emitEffect(
                        Effect.ShowSnackbar("Status updated to ${TripStatus.toApiString(status)}")
                    )
                }
                is Result.Error -> {
                    stateManager.updateTripState { copy(isSaving = false) }
                    stateManager.emitEffect(Effect.ShowError(result.message ?: "Failed to update status"))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    suspend fun updateTripState(newState: String, reason: String?) {
        val tripId = stateManager.currentTripState.tripId
        if (tripId.isBlank()) return

        stateManager.updateTripState { copy(isUpdatingState = true) }

        withContext(dispatcherProvider.io) {
            when (val result = tripRepository.updateTripState(tripId, newState, reason)) {
                is Result.Success -> {
                    stateManager.updateTripState {
                        copy(isUpdatingState = false, showStateChangeDialog = false, trip = result.data)
                    }
                    stateManager.emitEffect(Effect.StateUpdated(newState))
                    val label = stateManager.currentTripState.stateLabels[newState]
                        ?: com.indusjs.fleet.core.constants.StatusConstants.TripState.getDisplayLabel(newState)
                    stateManager.emitEffect(
                        Effect.ShowSnackbar("Status updated to $label")
                    )
                }
                is Result.Error -> {
                    stateManager.updateTripState { copy(isUpdatingState = false) }
                    stateManager.emitEffect(
                        Effect.ShowError(result.message ?: "Failed to update trip state")
                    )
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    suspend fun confirmCancel() {
        val tripId = stateManager.currentTripState.trip?.id ?: return
        stateManager.updateTripState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            when (val result = cancelTripUseCase(tripId)) {
                is Result.Success -> {
                    stateManager.updateTripState { copy(isSaving = false) }
                    stateManager.emitEffect(Effect.ShowSnackbar("Trip cancelled successfully"))
                    stateManager.emitEffect(Effect.TripCancelled(tripId))
                    stateManager.emitEffect(Effect.NavigateBack)
                }
                is Result.Error -> {
                    stateManager.updateTripState { copy(isSaving = false) }
                    stateManager.emitEffect(Effect.ShowError(result.message ?: "Failed to cancel trip"))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    fun exportCostsToPdf() {
        val state = stateManager.currentTripState
        val trip = state.trip

        logger.d(TAG_TRIP_DETAIL_ACTION, "exportCostsToPdf - hasCosts: ${state.hasCosts}, costsCount: ${state.costs.size}")

        if (!state.hasCosts) {
            stateManager.emitEffect(Effect.ShowSnackbar("No costs to export"))
            return
        }

        val costsByTypeWithTotals = state.costsByType.mapValues { (_, costs) ->
            costs.sumOf { it.amount }
        }


        val departureDate = resolveDate(trip?.plannedStart, trip?.scheduledDate)
        val arrivalDate = resolveDate(trip?.plannedEnd, trip?.deliveryDate)

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
                    amount = cost.amount,
                    date = cost.date?.takeIf { it > 0L }
                        ?.let { FleetDateTime.timestampToDateString(it) } ?: "",
                    time = cost.time,
                    notes = cost.notes
                )
            },
            totalCost = state.totalCost,
            fuelCost = costsByTypeWithTotals.filterKeys {
                it.contains("fuel", ignoreCase = true)
            }.values.sumOf { it },
            tollCost = costsByTypeWithTotals.filterKeys {
                it.contains("toll", ignoreCase = true)
            }.values.sumOf { it },
            otherCost = state.totalCost - costsByTypeWithTotals.filterKeys {
                it.contains("fuel", ignoreCase = true) || it.contains("toll", ignoreCase = true)
            }.values.sumOf { it },
            generatedAt = FleetDateTime.formatDisplayDateTime12Hour(FleetDateTime.now())
        )

        logger.d(TAG_TRIP_DETAIL_ACTION, "Sending ExportPdf effect with tripId: ${pdfData.tripId}, totalCost: ${pdfData.totalCost}")
        stateManager.emitEffect(Effect.ExportPdf(pdfData))
    }

    fun navigateToAddTripCost() {
        val trip = stateManager.currentTripState.trip ?: return
        stateManager.emitEffect(
            Effect.NavigateToAddTripCost(tripId = trip.id, vehicleId = trip.vehicleId)
        )
    }

    fun navigateToAddPayment() {
        val trip = stateManager.currentTripState.trip ?: return
        stateManager.emitEffect(
            Effect.NavigateToAddPayment(tripId = trip.id, vehicleId = trip.vehicleId)
        )
    }

    /**
     * Resolve a display date (DD-MM-YYYY) from epoch-millis fields, preferring
     * [primary] then [fallback]. Treats null/0 as unset.
     */
    private fun resolveDate(primary: Long?, fallback: Long?): String? {
        val ms = primary?.takeIf { it > 0L } ?: fallback?.takeIf { it > 0L } ?: return null
        return FleetDateTime.timestampToDateString(ms)
    }
}

