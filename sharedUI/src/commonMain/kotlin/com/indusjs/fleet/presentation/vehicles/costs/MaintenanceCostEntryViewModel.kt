package com.indusjs.fleet.presentation.vehicles.costs

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.fleet.core.util.convertFormattedToIsoDateTime
import com.indusjs.fleet.data.model.costs.BulkCreateMaintenanceCostsRequest
import com.indusjs.fleet.data.model.costs.BulkMaintenanceCostItem
import com.indusjs.fleet.data.model.costs.CreateMaintenanceCostRequest
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.indusjs.fleet.domain.repository.vehicle.VehicleRepository
import com.indusjs.fleet.presentation.vehicles.costs.MaintenanceCostEntryContract.Effect
import com.indusjs.fleet.presentation.vehicles.costs.MaintenanceCostEntryContract.Intent
import com.indusjs.fleet.presentation.vehicles.costs.MaintenanceCostEntryContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

/**
 * ViewModel for Maintenance Cost Entry screen with multi-row support.
 */
@Inject
class MaintenanceCostEntryViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val vehicleRepository: VehicleRepository,
    private val costsRepository: CostsRepository
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        sendIntent(Intent.LoadVehicles)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadVehicles -> loadVehicles()
            is Intent.SelectVehicle -> selectVehicle(intent.vehicle)
            is Intent.ToggleVehicleDropdown -> updateState { copy(showVehicleDropdown = !showVehicleDropdown) }

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
            is Intent.UpdateDescription -> updateRowField(intent.rowId) { it.copy(description = intent.value) }
            is Intent.UpdateNotes -> updateRowField(intent.rowId) { it.copy(notes = intent.value) }
            is Intent.UpdateVendorName -> updateRowField(intent.rowId) { it.copy(vendorName = intent.value) }
            is Intent.UpdateInvoiceNo -> updateRowField(intent.rowId) { it.copy(invoiceNo = intent.value) }

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

    private suspend fun loadVehicles() {
        updateState { copy(isLoadingData = true) }

        withContext(dispatcherProvider.io) {
            vehicleRepository.getVehicles().collect { result ->
                when (result) {
                    is Result.Success -> {
                        updateState {
                            copy(
                                isLoadingData = false,
                                vehicles = result.data
                            )
                        }
                    }
                    is Result.Error -> {
                        updateState {
                            copy(
                                isLoadingData = false,
                                error = result.message ?: "Failed to load vehicles"
                            )
                        }
                    }
                    is Result.Loading -> { /* ignore */ }
                }
            }
        }
    }

    private fun selectVehicle(vehicle: Vehicle) {
        updateState {
            copy(
                selectedVehicle = vehicle,
                showVehicleDropdown = false,
                vehicleError = null
            )
        }
    }

    private fun addCostRow() {
        updateState {
            // Add new row at the top of the list
            copy(costEntries = listOf(MaintenanceCostRow()) + costEntries)
        }
    }

    private fun removeCostRow(rowId: String) {
        updateState {
            val newEntries = costEntries.filter { it.id != rowId }
            // Ensure at least one row exists
            copy(costEntries = if (newEntries.isEmpty()) listOf(MaintenanceCostRow()) else newEntries)
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
                costTypeError = null
            )
        }
    }

    private fun toggleCostTypeDropdown(rowId: String) {
        updateRowField(rowId) { it.copy(showCostTypeDropdown = !it.showCostTypeDropdown) }
    }

    private fun updateAmount(rowId: String, value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        val error = if (filtered.isNotEmpty() && filtered.toDoubleOrNull() == null) {
            "Invalid amount"
        } else null

        updateRowField(rowId) { it.copy(amount = filtered, amountError = error) }
    }

    private fun updateRowField(rowId: String, transform: (MaintenanceCostRow) -> MaintenanceCostRow) {
        updateState {
            copy(costEntries = costEntries.map {
                if (it.id == rowId) transform(it) else it
            })
        }
    }

    private suspend fun loadCostHistory() {
        val vehicleId = state.value.selectedVehicle?.id ?: return

        updateState { copy(isLoadingHistory = true) }

        withContext(dispatcherProvider.io) {
            when (val result = costsRepository.getMaintenanceCosts(vehicleId)) {
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

        // Validate vehicle selection
        if (currentState.selectedVehicle == null) {
            updateState { copy(vehicleError = "Please select a vehicle") }
            return
        }

        // Get valid entries
        val validEntries = currentState.costEntries.filter { it.isValid }
        if (validEntries.isEmpty()) {
            sendEffect(Effect.ShowError("Please add at least one valid cost entry"))
            return
        }

        // Validate date and time format for each entry using centralized validation
        for ((index, entry) in validEntries.withIndex()) {
            val dateError = ValidationUtils.getDateError(entry.date)
            if (dateError != null) {
                sendEffect(Effect.ShowError("Entry ${index + 1}: $dateError"))
                return
            }

            val timeError = ValidationUtils.getTimeError(entry.time, required = false)
            if (timeError != null) {
                sendEffect(Effect.ShowError("Entry ${index + 1}: $timeError"))
                return
            }

            val amountError = ValidationUtils.getAmountError(entry.amount)
            if (amountError != null) {
                sendEffect(Effect.ShowError("Entry ${index + 1}: $amountError"))
                return
            }
        }

        updateState { copy(isSaving = true) }

        // Build bulk request
        // Convert date/time to ISO 8601 format for v2 API
        val bulkItems = validEntries.map { entry ->
            BulkMaintenanceCostItem(
                costType = entry.costType,
                amount = entry.amount.toDoubleOrNull() ?: 0.0,
                date = convertFormattedToIsoDateTime(entry.date, entry.time),
                time = if (entry.time.isNotBlank()) convertFormattedToIsoDateTime(entry.date, entry.time) else null,
                description = entry.description.takeIf { it.isNotBlank() },
                notes = entry.notes.takeIf { it.isNotBlank() },
                vendorName = entry.vendorName.takeIf { it.isNotBlank() },
                invoiceNo = entry.invoiceNo.takeIf { it.isNotBlank() }
            )
        }

        val request = BulkCreateMaintenanceCostsRequest(costs = bulkItems)

        withContext(dispatcherProvider.io) {
            when (val result = costsRepository.bulkCreateMaintenanceCosts(currentState.selectedVehicle.id, request)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isSaving = false,
                            // Reset to single empty row after successful save
                            costEntries = listOf(MaintenanceCostRow())
                        )
                    }
                    sendEffect(Effect.CostsSaved(result.data))
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isSaving = false,
                            error = result.message ?: "Failed to save maintenance costs"
                        )
                    }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to save maintenance costs"))
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }
}
