package com.indusjs.fleet.presentation.drivers.cost

import androidx.lifecycle.viewModelScope
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.ui.CostTypeSelection
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.fleet.core.util.convertFormattedToIsoDateTime
import com.indusjs.fleet.data.model.driver.BulkCreateDriverCostsRequest
import com.indusjs.fleet.data.model.driver.BulkDriverCostItem
import com.indusjs.fleet.data.model.driver.DriverCostTypes
import com.indusjs.fleet.data.model.costs.toFlatList
import com.indusjs.fleet.data.model.costs.toCostTypeGroups
import com.indusjs.fleet.domain.entity.driver.Driver
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.indusjs.fleet.domain.repository.costs.CostTypesRepository
import com.indusjs.fleet.domain.repository.driver.DriverRepository
import com.indusjs.fleet.domain.usecase.costs.GetDriverCostTypesUseCase
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for Driver Cost Entry screen with multi-row cost entries.
 */
@Inject
class DriverCostEntryViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val driverRepository: DriverRepository,
    private val costsRepository: CostsRepository,
    private val costTypesRepository: CostTypesRepository,
    private val getDriverCostTypesUseCase: GetDriverCostTypesUseCase? = null
) : MviViewModel<DriverCostEntryContract.State, DriverCostEntryContract.Intent, DriverCostEntryContract.Effect>(
    DriverCostEntryContract.State()
) {

    init {
        sendIntent(DriverCostEntryContract.Intent.LoadDrivers)
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
                val groupedDto = getDriverCostTypesUseCase?.getGrouped()
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
                    val costTypes = getDriverCostTypesUseCase?.invoke()
                    if (!costTypes.isNullOrEmpty()) {
                        updateState {
                            copy(
                                costTypeOptions = costTypes,
                                costTypeGroups = DriverCostTypes.groups
                            )
                        }
                    } else {
                        // Use hardcoded fallback with grouped structure
                        updateState {
                            copy(
                                costTypeOptions = DriverCostTypes.types,
                                costTypeGroups = DriverCostTypes.groups
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                // Use hardcoded fallback on error
                updateState {
                    copy(
                        costTypeOptions = DriverCostTypes.types,
                        costTypeGroups = DriverCostTypes.groups
                    )
                }
            }
        }
    }

    override suspend fun handleIntent(intent: DriverCostEntryContract.Intent) {
        when (intent) {
            is DriverCostEntryContract.Intent.LoadDrivers -> loadDrivers()
            is DriverCostEntryContract.Intent.RefreshCostTypes -> refreshCostTypes()
            is DriverCostEntryContract.Intent.SetInitialDriver -> setInitialDriver(intent.driverId)
            is DriverCostEntryContract.Intent.SelectDriver -> selectDriver(intent.driver)
            is DriverCostEntryContract.Intent.ToggleDriverDropdown -> updateState { copy(showDriverDropdown = !showDriverDropdown) }

            // Row management
            is DriverCostEntryContract.Intent.AddCostRow -> addCostRow()
            is DriverCostEntryContract.Intent.RemoveCostRow -> removeCostRow(intent.rowId)
            is DriverCostEntryContract.Intent.ToggleRowExpanded -> toggleRowExpanded(intent.rowId)

            // Cost entry updates
            is DriverCostEntryContract.Intent.SelectCostType -> selectCostType(intent.rowId, intent.selection)
            is DriverCostEntryContract.Intent.UpdateSelectedCategory -> updateSelectedCategory(intent.rowId, intent.groupId, intent.groupName)
            is DriverCostEntryContract.Intent.ToggleCostTypeDropdown -> toggleCostTypeDropdown(intent.rowId)
            is DriverCostEntryContract.Intent.UpdateDate -> updateRowField(intent.rowId) { it.copy(date = intent.value, dateError = null) }
            is DriverCostEntryContract.Intent.UpdateTime -> updateRowField(intent.rowId) { it.copy(time = intent.value) }
            is DriverCostEntryContract.Intent.UpdateAmount -> updateAmount(intent.rowId, intent.value)
            is DriverCostEntryContract.Intent.UpdateNotes -> updateRowField(intent.rowId) { it.copy(notes = intent.value) }
            is DriverCostEntryContract.Intent.UpdateCustomCostTypeName -> updateRowField(intent.rowId) { it.copy(customCostTypeName = intent.value) }

            // History
            is DriverCostEntryContract.Intent.LoadCostHistory -> loadCostHistory()
            is DriverCostEntryContract.Intent.ShowHistory -> {
                updateState { copy(showHistoryDialog = true) }
                loadCostHistory()
            }
            is DriverCostEntryContract.Intent.HideHistory -> updateState { copy(showHistoryDialog = false) }

            // Actions
            is DriverCostEntryContract.Intent.SaveCosts -> saveCosts()
            is DriverCostEntryContract.Intent.NavigateBack -> sendEffect(DriverCostEntryContract.Effect.NavigateBack)
            is DriverCostEntryContract.Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    /**
     * Refresh cost types from API and reload into UI.
     */
    private suspend fun refreshCostTypes() {
        updateState { copy(isRefreshingCostTypes = true) }

        withContext(dispatcherProvider.io) {
            when (val result = costTypesRepository.refreshDriverCostTypes()) {
                is Result.Success<*> -> {
                    // Reload cost types from local DB
                    val groupedDto = costTypesRepository.getDriverCostTypes()
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
                        sendEffect(DriverCostEntryContract.Effect.ShowSnackbar("Cost types refreshed"))
                    } else {
                        updateState { copy(isRefreshingCostTypes = false) }
                        sendEffect(DriverCostEntryContract.Effect.ShowSnackbar("No cost types found"))
                    }
                }
                is Result.Error -> {
                    updateState { copy(isRefreshingCostTypes = false) }
                    sendEffect(DriverCostEntryContract.Effect.ShowError("Failed to refresh: ${result.message}"))
                }
                else -> {
                    updateState { copy(isRefreshingCostTypes = false) }
                }
            }
        }
    }

    private suspend fun loadDrivers() {
        updateState { copy(isLoadingData = true) }

        withContext(dispatcherProvider.io) {
            driverRepository.getDrivers().collect { result ->
                when (result) {
                    is Result.Success -> {
                        // Filter only active drivers
                        val activeDrivers = result.data.filter { it.isActive }
                        updateState {
                            copy(
                                drivers = activeDrivers,
                                isLoadingData = false
                            )
                        }
                    }
                    is Result.Error -> {
                        updateState { copy(isLoadingData = false) }
                        sendEffect(DriverCostEntryContract.Effect.ShowError("Failed to load drivers: ${result.message}"))
                    }
                    is Result.Loading -> { /* ignore */ }
                }
            }
        }
    }

    private fun selectDriver(driver: Driver) {
        updateState {
            copy(
                selectedDriver = driver,
                showDriverDropdown = false,
                driverError = null
            )
        }
    }

    private fun addCostRow() {
        updateState {
            copy(
                costEntries = costEntries + DriverCostEntryRow()
            )
        }
    }

    private fun removeCostRow(rowId: String) {
        updateState {
            val newEntries = costEntries.filter { it.id != rowId }
            // Always keep at least one row
            copy(costEntries = newEntries.ifEmpty { listOf(DriverCostEntryRow()) })
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
                costTypeError = null
            )
        }
    }

    private fun updateSelectedCategory(rowId: String, groupId: String, groupName: String) {
        updateRowField(rowId) {
            it.copy(
                selectedGroupId = groupId,
                selectedGroupName = groupName
            )
        }
    }

    private fun toggleCostTypeDropdown(rowId: String) {
        updateRowField(rowId) { it.copy(showCostTypeDropdown = !it.showCostTypeDropdown) }
    }

    private fun updateAmount(rowId: String, value: String) {
        // Validate and update
        val error = if (value.isNotEmpty() && (value.toDoubleOrNull() == null || (value.toDoubleOrNull() ?: 0.0) <= 0)) {
            "Enter a valid amount"
        } else null

        updateRowField(rowId) { it.copy(amount = value, amountError = error) }
    }

    private fun updateRowField(rowId: String, transform: (DriverCostEntryRow) -> DriverCostEntryRow) {
        updateState {
            copy(
                costEntries = costEntries.map {
                    if (it.id == rowId) transform(it) else it
                }
            )
        }
    }

    private suspend fun loadCostHistory() {
        val driverId = state.value.selectedDriver?.id?.toString() ?: return
        updateState { copy(isLoadingHistory = true) }

        withContext(dispatcherProvider.io) {
            when (val result = costsRepository.getDriverCosts(driverId)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            costHistory = result.data.costs,
                            isLoadingHistory = false
                        )
                    }
                }
                is Result.Error -> {
                    updateState { copy(isLoadingHistory = false) }
                }
                else -> {}
            }
        }
    }

    private suspend fun saveCosts() {
        val driver = state.value.selectedDriver
        if (driver == null) {
            updateState { copy(driverError = "Please select a driver") }
            return
        }

        // Validate all entries
        val validatedEntries = state.value.costEntries.map { entry ->
            var updated = entry

            // Validate cost type
            if (entry.costType.isBlank()) {
                updated = updated.copy(costTypeError = "Select a cost type")
            }

            // Validate date
            if (entry.date.isBlank()) {
                updated = updated.copy(dateError = "Date is required")
            } else {
                val dateValidation = ValidationUtils.validateDate(entry.date)
                if (dateValidation is com.indusjs.fleet.core.util.ValidationResult.Error) {
                    updated = updated.copy(dateError = dateValidation.message)
                }
            }

            // Validate amount
            if (entry.amount.isBlank() || (entry.amount.toDoubleOrNull() ?: 0.0) <= 0) {
                updated = updated.copy(amountError = "Enter a valid amount")
            }

            updated
        }

        // Update state with validated entries
        updateState { copy(costEntries = validatedEntries) }

        // Check if all entries are valid
        val validEntries = validatedEntries.filter { it.isValid }
        if (validEntries.isEmpty()) {
            sendEffect(DriverCostEntryContract.Effect.ShowError("Please fill in all required fields correctly"))
            return
        }

        updateState { copy(isSaving = true) }

        // Build bulk request
        val costItems = validEntries.map { entry ->
            // Determine if this is a deduction cost
            val isDeduction = entry.selectedGroupId == DriverCostTypes.DEDUCTION_GROUP_ID ||
                    entry.costType.startsWith("DC-003")

            // Convert to ISO 8601 date format for API
            val isoDateTime = convertFormattedToIsoDateTime(entry.date, entry.time.ifBlank { "00:00" })

            BulkDriverCostItem(
                costId = entry.costType,
                costLabel = entry.costTypeLabel,
                groupId = entry.selectedGroupId,
                customCostLabel = entry.customCostTypeName.takeIf { it.isNotBlank() },
                amount = entry.amount.toDouble(),
                date = isoDateTime,
                notes = entry.notes.takeIf { it.isNotBlank() },
                isDeduction = isDeduction
            )
        }

        val request = BulkCreateDriverCostsRequest(costs = costItems)

        withContext(dispatcherProvider.io) {
            when (val result = costsRepository.bulkCreateDriverCosts(driver.id.toString(), request)) {
                is Result.Success -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(DriverCostEntryContract.Effect.CostsSaved(result.data))
                    sendEffect(DriverCostEntryContract.Effect.NavigateBack)
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(DriverCostEntryContract.Effect.ShowError("Failed to save: ${result.message}"))
                }
                else -> {
                    updateState { copy(isSaving = false) }
                }
            }
        }
    }

    private fun setInitialDriver(driverId: String) {
        // Find the driver by ID
        val driver = state.value.drivers.find { it.id == driverId }

        if (driver != null) {
            // Driver found, select the driver
            selectDriver(driver)
        } else {
            // Driver not found, clear the selection
            updateState { copy(selectedDriver = null) }
        }
    }
}
