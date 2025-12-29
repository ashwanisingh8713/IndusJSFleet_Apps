package com.indusjs.fleet.presentation.costs

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.data.model.costs.CreateMaintenanceCostRequest
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.indusjs.fleet.domain.repository.vehicle.VehicleRepository
import com.indusjs.fleet.presentation.costs.MaintenanceCostEntryContract.Effect
import com.indusjs.fleet.presentation.costs.MaintenanceCostEntryContract.Intent
import com.indusjs.fleet.presentation.costs.MaintenanceCostEntryContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

/**
 * ViewModel for Vehicle Maintenance Cost Entry screen.
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

            is Intent.SelectCostType -> selectCostType(intent.costType, intent.label)
            is Intent.ToggleCostTypeDropdown -> updateState { copy(showCostTypeDropdown = !showCostTypeDropdown) }
            is Intent.UpdateDate -> updateState { copy(date = intent.value, dateError = null) }
            is Intent.UpdateTime -> updateState { copy(time = intent.value) }
            is Intent.UpdateAmount -> updateAmount(intent.value)
            is Intent.UpdateDescription -> updateState { copy(description = intent.value) }
            is Intent.UpdateNotes -> updateState { copy(notes = intent.value) }
            is Intent.UpdateVendorName -> updateState { copy(vendorName = intent.value) }
            is Intent.UpdateInvoiceNo -> updateState { copy(invoiceNo = intent.value) }

            is Intent.LoadCostHistory -> loadCostHistory()
            is Intent.ShowHistory -> {
                updateState { copy(showHistoryDialog = true) }
                loadCostHistory()
            }
            is Intent.HideHistory -> updateState { copy(showHistoryDialog = false) }

            is Intent.SaveCost -> saveCost()
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

    private fun selectCostType(costType: String, label: String) {
        updateState {
            copy(
                selectedCostType = costType,
                costTypeLabel = label,
                showCostTypeDropdown = false,
                costTypeError = null
            )
        }
    }

    private fun updateAmount(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        val error = if (filtered.isNotEmpty() && filtered.toDoubleOrNull() == null) {
            "Invalid amount"
        } else null

        updateState { copy(amount = filtered, amountError = error) }
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

    private suspend fun saveCost() {
        val currentState = state.value

        // Validate
        if (currentState.selectedVehicle == null) {
            updateState { copy(vehicleError = "Please select a vehicle") }
            return
        }
        if (currentState.selectedCostType.isBlank()) {
            updateState { copy(costTypeError = "Please select a cost type") }
            return
        }
        if (currentState.date.isBlank()) {
            updateState { copy(dateError = "Please enter a date") }
            return
        }
        val amount = currentState.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            updateState { copy(amountError = "Please enter a valid amount") }
            return
        }

        updateState { copy(isSaving = true) }

        val request = CreateMaintenanceCostRequest(
            vehicleId = currentState.selectedVehicle.id,
            costType = currentState.selectedCostType,
            amount = amount,
            date = currentState.date,
            time = currentState.time.takeIf { it.isNotBlank() },
            description = currentState.description.takeIf { it.isNotBlank() },
            notes = currentState.notes.takeIf { it.isNotBlank() },
            vendorName = currentState.vendorName.takeIf { it.isNotBlank() },
            invoiceNo = currentState.invoiceNo.takeIf { it.isNotBlank() }
        )

        withContext(dispatcherProvider.io) {
            when (val result = costsRepository.createMaintenanceCost(request)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isSaving = false,
                            // Clear form fields for next entry
                            selectedCostType = "",
                            costTypeLabel = "",
                            date = "",
                            time = "",
                            amount = "",
                            description = "",
                            notes = "",
                            vendorName = "",
                            invoiceNo = ""
                        )
                    }
                    sendEffect(Effect.CostSaved)
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isSaving = false,
                            error = result.message ?: "Failed to save maintenance cost"
                        )
                    }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to save maintenance cost"))
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }
}

