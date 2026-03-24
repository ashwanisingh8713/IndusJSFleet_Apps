package com.ijs.trip.presentation.cost

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.error.result.Result
import com.indusjs.uicomponents.components.CostTypeSelection
import com.ijs.trip.presentation.cost.util.TripCostToDriverCostMapper
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.fleet.core.util.convertFormattedToIsoDateTime
import com.indusjs.fleet.data.model.costs.BulkCostItem
import com.indusjs.fleet.data.model.costs.BulkCreateTripCostsRequest
import com.indusjs.fleet.data.model.costs.TripCostTypes
import com.indusjs.fleet.data.model.costs.toFlatList
import com.indusjs.fleet.data.model.costs.toCostTypeGroups
import com.ijs.trip.domain.entity.Trip
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.indusjs.fleet.domain.repository.costs.CostTypesRepository
import com.ijs.trip.domain.repository.TripRepository
import com.indusjs.fleet.domain.usecase.costs.GetTripCostTypesUseCase
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for Trip Cost Entry screen with multi-row cost entries.
 */
@Inject
class TripCostEntryViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val tripRepository: TripRepository,
    private val costsRepository: CostsRepository,
    private val costTypesRepository: CostTypesRepository,
    private val getTripCostTypesUseCase: GetTripCostTypesUseCase? = null
) : MviViewModel<TripCostEntryContract.State, TripCostEntryContract.Intent, TripCostEntryContract.Effect>(
    TripCostEntryContract.State()
) {

    init {
        sendIntent(TripCostEntryContract.Intent.LoadTrips)
        loadCostTypes()
    }

    /**
     * Load cost types from local cache (populated on app launch).
     * Falls back to hardcoded types if cache is empty or use case is not provided.
     */
    private fun loadCostTypes() {
        viewModelScope.launch(dispatcherProvider.io) {
            try {
                // Try to get grouped cost types first
                val groupedDto = getTripCostTypesUseCase?.getGrouped()
                if (groupedDto != null && groupedDto.groups.isNotEmpty()) {
                    val flat = groupedDto.toFlatList()
                    val groups = groupedDto.toCostTypeGroups()
                    updateState {
                        copy(
                            costTypeOptions = flat,
                            costTypeGroups = groups
                        )
                    }
                } else {
                    // Fallback to flat list and create grouped structure
                    val costTypes = getTripCostTypesUseCase?.invoke()
                    if (!costTypes.isNullOrEmpty()) {
                        updateState {
                            copy(
                                costTypeOptions = costTypes,
                                costTypeGroups = TripCostTypes.groups
                            )
                        }
                    } else {
                        // Use hardcoded fallback with grouped structure
                        updateState {
                            copy(
                                costTypeOptions = TripCostTypes.types,
                                costTypeGroups = TripCostTypes.groups
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                // Use hardcoded fallback on error
                updateState {
                    copy(
                        costTypeOptions = TripCostTypes.types,
                        costTypeGroups = TripCostTypes.groups
                    )
                }
            }
        }
    }

    override suspend fun handleIntent(intent: TripCostEntryContract.Intent) {
        when (intent) {
            is TripCostEntryContract.Intent.LoadTrips -> loadTrips()
            is TripCostEntryContract.Intent.RefreshCostTypes -> refreshCostTypes()
            is TripCostEntryContract.Intent.SelectTrip -> selectTrip(intent.trip)
            is TripCostEntryContract.Intent.PreSelectTripById -> preSelectTripById(intent.tripId)
            is TripCostEntryContract.Intent.ToggleTripDropdown -> updateState { copy(showTripDropdown = !showTripDropdown) }

            // Row management
            is TripCostEntryContract.Intent.AddCostRow -> addCostRow()
            is TripCostEntryContract.Intent.RemoveCostRow -> removeCostRow(intent.rowId)
            is TripCostEntryContract.Intent.ToggleRowExpanded -> toggleRowExpanded(intent.rowId)

            // Cost entry updates
            is TripCostEntryContract.Intent.SelectCostType -> selectCostType(intent.rowId, intent.selection)
            is TripCostEntryContract.Intent.UpdateSelectedCategory -> updateSelectedCategory(intent.rowId, intent.groupId, intent.groupName)
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

    /**
     * Refresh cost types from API and reload into UI.
     */
    private suspend fun refreshCostTypes() {
        updateState { copy(isRefreshingCostTypes = true) }

        withContext(dispatcherProvider.io) {
            when (val result = costTypesRepository.refreshTripCostTypes()) {
                is Result.Success -> {
                    // Reload cost types from local DB
                    val groupedDto = costTypesRepository.getTripCostTypes()
                    if (groupedDto != null && groupedDto.groups.isNotEmpty()) {
                        val flat = groupedDto.toFlatList()
                        val groups = groupedDto.toCostTypeGroups()
                        updateState {
                            copy(
                                isRefreshingCostTypes = false,
                                costTypeOptions = flat,
                                costTypeGroups = groups
                            )
                        }
                        sendEffect(TripCostEntryContract.Effect.ShowSnackbar("Cost types updated"))
                    } else {
                        updateState { copy(isRefreshingCostTypes = false) }
                        sendEffect(TripCostEntryContract.Effect.ShowSnackbar("Cost types updated"))
                    }
                }
                is Result.Error -> {
                    updateState { copy(isRefreshingCostTypes = false) }
                    sendEffect(TripCostEntryContract.Effect.ShowError(result.message ?: "Failed to refresh cost types"))
                }
                is Result.Loading -> { /* ignore */ }
            }
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

    /**
     * Pre-select a trip by its ID. Used when navigating from Trip Detail screen.
     * Waits for trips to be loaded if needed.
     */
    private fun preSelectTripById(tripId: String) {
        viewModelScope.launch(dispatcherProvider.io) {
            // If trips are already loaded, find and select
            val existingTrip = currentState.trips.find { it.id == tripId }
            if (existingTrip != null) {
                selectTrip(existingTrip)
                return@launch
            }

            // If trips are loading, wait and try again
            if (currentState.isLoadingData) {
                // Wait a bit for trips to load
                kotlinx.coroutines.delay(500)
                val trip = currentState.trips.find { it.id == tripId }
                if (trip != null) {
                    selectTrip(trip)
                }
            }
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

    private fun selectCostType(rowId: String, selection: CostTypeSelection) {
        updateRowField(rowId) {
            it.copy(
                costType = selection.costTypeId,
                costTypeLabel = selection.costTypeLabel,
                selectedGroupId = selection.groupId,
                selectedGroupName = selection.groupName,
                showCostTypeDropdown = false,
                costTypeError = null,
                // Auto-set fuelType to match selected cost type (used as read-only display)
                fuelType = if (selection.isFuelCategory) selection.costTypeId else it.fuelType,
                // Clear fuel fields if switching away from fuel type
                fuelQuantity = if (selection.isFuelCategory) it.fuelQuantity else "",
                fuelRate = if (selection.isFuelCategory) it.fuelRate else "",
                kmPerLiter = if (selection.isFuelCategory) it.kmPerLiter else ""
            )
        }
    }

    /**
     * Update selected category immediately when category dropdown changes.
     * This enables immediate show/hide of Fuel Details section.
     */
    private fun updateSelectedCategory(rowId: String, groupId: String, groupName: String) {
        updateRowField(rowId) {
            val isFuelCategory = groupId == CostTypeSelection.FUEL_ENERGY_GROUP_ID

            it.copy(
                selectedGroupId = groupId,
                selectedGroupName = groupName,
                // Clear cost type when category changes (user needs to select from new chips)
                costType = "",
                costTypeLabel = "",
                // Clear fuel fields if switching away from fuel category
                fuelQuantity = if (isFuelCategory) it.fuelQuantity else "",
                fuelRate = if (isFuelCategory) it.fuelRate else "",
                kmPerLiter = if (isFuelCategory) it.kmPerLiter else ""
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
                // New structured cost fields
                costId = entry.costType,
                costLabel = entry.costTypeLabel.ifBlank { entry.costType },
                groupId = entry.selectedGroupId.ifBlank { "TC-G-006" }, // Default to Miscellaneous
                customCostLabel = if (entry.isOtherCostType && entry.customCostTypeName.isNotBlank()) {
                    entry.customCostTypeName
                } else null,
                // Legacy field
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
                    // Sync driver expenses after successful trip cost save
                    // This creates corresponding entries in the driver's cost history
                    syncDriverExpenses(currentState.selectedTrip, validEntries)

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

    /**
     * Sync driver expense entries from trip costs to driver costs.
     *
     * This creates corresponding entries in the driver's cost history
     * with a reference to the trip for traceability.
     *
     * Implementation details:
     * - One-way sync: Trip Cost → Driver Cost
     * - Only syncs costs from the Driver Expenses group (TC-G-004)
     * - Maps trip cost types to appropriate driver cost types (DC-G-004)
     * - Includes trip_id reference for traceability
     * - Failures are logged but don't affect trip cost save success
     *
     * @param trip The trip associated with the costs
     * @param entries The list of cost entries to check for driver expenses
     */
    private suspend fun syncDriverExpenses(trip: Trip, entries: List<CostEntryRow>) {
        val log = Logger.withTag("TripCostEntryVM")

        val driverIdInt = trip.driverId.toIntOrNull()
        val tripIdInt = trip.id.toIntOrNull()

        if (driverIdInt == null || driverIdInt == 0) {
            log.w { "Cannot sync driver expenses: no driver assigned to trip ${trip.id}" }
            return
        }

        if (tripIdInt == null) {
            log.w { "Cannot sync driver expenses: invalid trip ID ${trip.id}" }
            return
        }

        // Filter driver expense entries (TC-G-004 group)
        val driverExpenseEntries = entries.filter { TripCostToDriverCostMapper.isDriverExpense(it) }

        if (driverExpenseEntries.isEmpty()) {
            log.d { "No driver expenses to sync for trip ${trip.id}" }
            return
        }

        log.d { "Syncing ${driverExpenseEntries.size} driver expense(s) for driver $driverIdInt from trip ${trip.id}" }

        var successCount = 0
        var failureCount = 0

        for (entry in driverExpenseEntries) {
            try {
                val driverCostRequest = TripCostToDriverCostMapper.mapToDriverCost(
                    entry = entry,
                    driverId = driverIdInt,
                    tripId = tripIdInt,
                    date = entry.date // DD-MM-YYYY format for driver cost API
                )

                if (driverCostRequest != null) {
                    when (val result = costsRepository.createDriverCost(
                        driverId = driverIdInt.toString(),
                        request = driverCostRequest
                    )) {
                        is Result.Success -> {
                            successCount++
                            log.d { "Driver expense synced: ${entry.costTypeLabel} - ₹${entry.amount}" }
                        }
                        is Result.Error -> {
                            failureCount++
                            // Log error but don't fail the trip cost save
                            log.w { "Failed to sync driver expense '${entry.costTypeLabel}': ${result.message}" }
                        }
                        is Result.Loading -> { /* ignore */ }
                    }
                }
            } catch (e: Exception) {
                failureCount++
                // Log error but don't fail the trip cost save
                log.e(e) { "Error syncing driver expense '${entry.costTypeLabel}': ${e.message}" }
            }
        }

        if (successCount > 0) {
            log.i { "Driver expense sync complete: $successCount synced, $failureCount failed" }
        }
    }
}
