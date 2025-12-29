package com.indusjs.fleet.presentation.costs

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.data.model.costs.BulkCostItem
import com.indusjs.fleet.data.model.costs.BulkCreateTripCostsRequest
import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.indusjs.fleet.domain.repository.trip.TripRepository
import com.indusjs.fleet.presentation.costs.TripCostEntryContract.Effect
import com.indusjs.fleet.presentation.costs.TripCostEntryContract.Intent
import com.indusjs.fleet.presentation.costs.TripCostEntryContract.State
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
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        sendIntent(Intent.LoadTrips)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadTrips -> loadTrips()
            is Intent.SelectTrip -> selectTrip(intent.trip)
            is Intent.ToggleTripDropdown -> updateState { copy(showTripDropdown = !showTripDropdown) }

            // Row management
            is Intent.AddCostRow -> addCostRow()
            is Intent.RemoveCostRow -> removeCostRow(intent.rowId)
            is Intent.ToggleRowExpanded -> toggleRowExpanded(intent.rowId)

            // Cost entry updates
            is Intent.SelectCostType -> selectCostType(intent.rowId, intent.costType, intent.label)
            is Intent.ToggleCostTypeDropdown -> toggleCostTypeDropdown(intent.rowId)
            is Intent.UpdateDate -> updateRowField(intent.rowId) { it.copy(date = intent.value, dateError = null) }
            is Intent.UpdateTime -> updateRowField(intent.rowId) { it.copy(time = intent.value) }
            is Intent.UpdateAmount -> updateAmount(intent.rowId, intent.value)
            is Intent.UpdateNotes -> updateRowField(intent.rowId) { it.copy(notes = intent.value) }
            is Intent.UpdateCustomCostTypeName -> updateRowField(intent.rowId) { it.copy(customCostTypeName = intent.value) }

            // Fuel specific
            is Intent.SelectFuelType -> updateRowField(intent.rowId) { it.copy(fuelType = intent.fuelType, showFuelTypeDropdown = false) }
            is Intent.ToggleFuelTypeDropdown -> toggleFuelTypeDropdown(intent.rowId)
            is Intent.UpdateFuelQuantity -> updateRowField(intent.rowId) { it.copy(fuelQuantity = intent.value) }
            is Intent.UpdateFuelRate -> updateRowField(intent.rowId) { it.copy(fuelRate = intent.value) }
            is Intent.UpdateKmPerLiter -> updateRowField(intent.rowId) { it.copy(kmPerLiter = intent.value) }

            // History
            is Intent.LoadCostHistory -> loadCostHistory()
            is Intent.ShowHistory -> {
                updateState { copy(showHistoryDialog = true) }
                loadCostHistory()
            }
            is Intent.HideHistory -> updateState { copy(showHistoryDialog = false) }

            // Actions
            is Intent.SaveCosts -> saveCosts()
            is Intent.NavigateBack -> sendEffect(Effect.NavigateBack)
            is Intent.ClearError -> updateState { copy(error = null) }
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
            sendEffect(Effect.ShowError("Please add at least one valid cost entry"))
            return
        }

        updateState { copy(isSaving = true) }

        // Build bulk request
        val bulkItems = validEntries.map { entry ->
            BulkCostItem(
                costType = if (entry.isOtherCostType && entry.customCostTypeName.isNotBlank()) {
                    entry.customCostTypeName
                } else {
                    entry.costType
                },
                amount = entry.amount.toDoubleOrNull() ?: 0.0,
                date = entry.date,
                time = entry.time.takeIf { it.isNotBlank() },
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
                    sendEffect(Effect.CostsSaved(result.data))
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isSaving = false,
                            error = result.message ?: "Failed to save costs"
                        )
                    }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to save costs"))
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }
}

