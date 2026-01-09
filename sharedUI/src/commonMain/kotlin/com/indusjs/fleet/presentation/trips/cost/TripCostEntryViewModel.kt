package com.indusjs.fleet.presentation.trips.cost

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.fleet.core.util.convertFormattedToIsoDateTime
import com.indusjs.fleet.data.model.costs.BulkCostItem
import com.indusjs.fleet.data.model.costs.BulkCreateTripCostsRequest
import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.indusjs.fleet.domain.repository.trip.TripRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

/**
 * ViewModel for Trip Cost Entry screen with multi-row cost entries.
 */
@Inject
class TripCostEntryViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val tripRepository: TripRepository,
    private val costsRepository: CostsRepository
) : MviViewModel<TripCostEntryContract.State, TripCostEntryContract.Intent, TripCostEntryContract.Effect>(
    TripCostEntryContract.State()
) {

    init {
        sendIntent(TripCostEntryContract.Intent.LoadTrips)
    }

    override suspend fun handleIntent(intent: TripCostEntryContract.Intent) {
        when (intent) {
            is TripCostEntryContract.Intent.LoadTrips -> loadTrips()
            is TripCostEntryContract.Intent.SelectTrip -> selectTrip(intent.trip)
            is TripCostEntryContract.Intent.ToggleTripDropdown -> updateState { copy(showTripDropdown = !showTripDropdown) }

            // Row management
            is TripCostEntryContract.Intent.AddCostRow -> addCostRow()
            is TripCostEntryContract.Intent.RemoveCostRow -> removeCostRow(intent.rowId)
            is TripCostEntryContract.Intent.ToggleRowExpanded -> toggleRowExpanded(intent.rowId)

            // Cost entry updates
            is TripCostEntryContract.Intent.SelectCostType -> selectCostType(intent.rowId, intent.costType, intent.label)
            is TripCostEntryContract.Intent.ToggleCostTypeDropdown -> toggleCostTypeDropdown(intent.rowId)
            is TripCostEntryContract.Intent.UpdateDate -> updateRowField(intent.rowId) { it.copy(date = intent.value, dateError = null) }
            is TripCostEntryContract.Intent.UpdateTime -> updateRowField(intent.rowId) { it.copy(time = intent.value) }
            is TripCostEntryContract.Intent.UpdateAmount -> updateAmount(intent.rowId, intent.value)
            is TripCostEntryContract.Intent.UpdateNotes -> updateRowField(intent.rowId) { it.copy(notes = intent.value) }
            is TripCostEntryContract.Intent.UpdateCustomCostTypeName -> updateRowField(intent.rowId) { it.copy(customCostTypeName = intent.value) }

            // Fuel specific
            is TripCostEntryContract.Intent.SelectFuelType -> updateRowField(intent.rowId) { it.copy(fuelType = intent.fuelType, showFuelTypeDropdown = false) }
            is TripCostEntryContract.Intent.ToggleFuelTypeDropdown -> toggleFuelTypeDropdown(intent.rowId)
            is TripCostEntryContract.Intent.UpdateFuelQuantity -> updateRowField(intent.rowId) { it.copy(fuelQuantity = intent.value) }
            is TripCostEntryContract.Intent.UpdateFuelRate -> updateRowField(intent.rowId) { it.copy(fuelRate = intent.value) }
            is TripCostEntryContract.Intent.UpdateKmPerLiter -> updateRowField(intent.rowId) { it.copy(kmPerLiter = intent.value) }

            // History
            is TripCostEntryContract.Intent.LoadCostHistory -> loadCostHistory()
            is TripCostEntryContract.Intent.ShowHistory -> {
                updateState { copy(showHistoryDialog = true) }
                loadCostHistory()
            }
            is TripCostEntryContract.Intent.HideHistory -> updateState { copy(showHistoryDialog = false) }

            // Actions
            is TripCostEntryContract.Intent.SaveCosts -> saveCosts()
            is TripCostEntryContract.Intent.NavigateBack -> sendEffect(TripCostEntryContract.Effect.NavigateBack)
            is TripCostEntryContract.Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    private suspend fun loadTrips() {
        updateState { copy(isLoadingData = true) }

        withContext(dispatcherProvider.io) {
            tripRepository.getTrips().collect { result ->
                when (result) {
                    is Result.Success -> {
                        updateState {
                            copy(
                                isLoadingData = false,
                                trips = result.data
                            )
                        }
                    }
                    is Result.Error -> {
                        updateState {
                            copy(
                                isLoadingData = false,
                                error = result.message ?: "Failed to load trips"
                            )
                        }
                    }
                    is Result.Loading -> { /* ignore */ }
                }
            }
        }
    }

    private fun selectTrip(trip: Trip) {
        updateState {
            copy(
                selectedTrip = trip,
                showTripDropdown = false,
                tripError = null
            )
        }
    }

    private fun addCostRow() {
        updateState {
            copy(costEntries = costEntries + CostEntryRow())
        }
    }

    private fun removeCostRow(rowId: String) {
        updateState {
            val newEntries = costEntries.filter { it.id != rowId }
            // Ensure at least one row exists
            copy(costEntries = if (newEntries.isEmpty()) listOf(CostEntryRow()) else newEntries)
        }
    }

    private fun toggleRowExpanded(rowId: String) {
        updateRowField(rowId) { it.copy(isExpanded = !it.isExpanded) }
    }

    private fun selectCostType(rowId: String, costType: String, label: String) {
        updateRowField(rowId) {
            it.copy(
                costType = costType,
                costTypeLabel = label,
                showCostTypeDropdown = false,
                costTypeError = null,
                // Reset fuel fields if not fuel type
                fuelQuantity = if (costType != "fuel") "" else it.fuelQuantity,
                fuelRate = if (costType != "fuel") "" else it.fuelRate,
                kmPerLiter = if (costType != "fuel") "" else it.kmPerLiter
            )
        }
    }

    private fun toggleCostTypeDropdown(rowId: String) {
        updateRowField(rowId) { it.copy(showCostTypeDropdown = !it.showCostTypeDropdown) }
    }

    private fun toggleFuelTypeDropdown(rowId: String) {
        updateRowField(rowId) { it.copy(showFuelTypeDropdown = !it.showFuelTypeDropdown) }
    }

    private fun updateAmount(rowId: String, value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        val error = if (filtered.isNotEmpty() && filtered.toDoubleOrNull() == null) {
            "Invalid amount"
        } else null

        updateRowField(rowId) { it.copy(amount = filtered, amountError = error) }
    }

    private fun updateRowField(rowId: String, transform: (CostEntryRow) -> CostEntryRow) {
        updateState {
            copy(costEntries = costEntries.map {
                if (it.id == rowId) transform(it) else it
            })
        }
    }

    private suspend fun loadCostHistory() {
        val tripId = state.value.selectedTrip?.id ?: return

        updateState { copy(isLoadingHistory = true) }

        withContext(dispatcherProvider.io) {
            when (val result = costsRepository.getTripCosts(tripId)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isLoadingHistory = false,
                            costHistory = result.data
                        )
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoadingHistory = false,
                            costHistory = emptyList()
                        )
                    }
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }

    private suspend fun saveCosts() {
        val currentState = state.value

        // Validate trip selection
        if (currentState.selectedTrip == null) {
            updateState { copy(tripError = "Please select a trip") }
            return
        }

        // Get valid entries
        val validEntries = currentState.costEntries.filter { it.isValid }
        if (validEntries.isEmpty()) {
            sendEffect(TripCostEntryContract.Effect.ShowError("Please add at least one valid cost entry"))
            return
        }

        // Validate date and time format for each entry using centralized validation
        for ((index, entry) in validEntries.withIndex()) {
            val dateError = ValidationUtils.getDateError(entry.date)
            if (dateError != null) {
                sendEffect(TripCostEntryContract.Effect.ShowError("Entry ${index + 1}: $dateError"))
                return
            }

            val timeError = ValidationUtils.getTimeError(entry.time, required = false)
            if (timeError != null) {
                sendEffect(TripCostEntryContract.Effect.ShowError("Entry ${index + 1}: $timeError"))
                return
            }

            val amountError = ValidationUtils.getAmountError(entry.amount)
            if (amountError != null) {
                sendEffect(TripCostEntryContract.Effect.ShowError("Entry ${index + 1}: $amountError"))
                return
            }
        }

        updateState { copy(isSaving = true) }

        // Build bulk request - trip_id is in URL, vehicle_id from trip record
        // Convert date/time to ISO 8601 format for v2 API
        val bulkItems = validEntries.map { entry ->
            BulkCostItem(
                costType = if (entry.isOtherCostType && entry.customCostTypeName.isNotBlank()) {
                    entry.customCostTypeName
                } else {
                    entry.costType
                },
                amount = entry.amount.toDoubleOrNull() ?: 0.0,
                date = convertFormattedToIsoDateTime(entry.date, entry.time),
                time = if (entry.time.isNotBlank()) convertFormattedToIsoDateTime(entry.date, entry.time) else null,
                notes = entry.notes.takeIf { it.isNotBlank() },
                fuelType = if (entry.isFuelCostType) entry.fuelType else null,
                fuelQuantity = if (entry.isFuelCostType) entry.fuelQuantity.toDoubleOrNull() else null,
                fuelRate = if (entry.isFuelCostType) entry.fuelRate.toDoubleOrNull() else null,
                kmPerLiter = if (entry.isFuelCostType) entry.kmPerLiter.toDoubleOrNull() else null
            )
        }

        val request = BulkCreateTripCostsRequest(costs = bulkItems)

        withContext(dispatcherProvider.io) {
            when (val result = costsRepository.bulkCreateTripCosts(currentState.selectedTrip.id, request)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isSaving = false,
                            // Reset to single empty row after successful save
                            costEntries = listOf(CostEntryRow())
                        )
                    }
                    sendEffect(TripCostEntryContract.Effect.CostsSaved(result.data))
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isSaving = false,
                            error = result.message ?: "Failed to save costs"
                        )
                    }
                    sendEffect(TripCostEntryContract.Effect.ShowError(result.message ?: "Failed to save costs"))
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }
}
