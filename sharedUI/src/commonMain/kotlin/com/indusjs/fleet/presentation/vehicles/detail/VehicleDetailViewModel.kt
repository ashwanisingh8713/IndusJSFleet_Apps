package com.indusjs.fleet.presentation.vehicles.detail

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.entity.vehicle.VehicleType
import com.indusjs.fleet.domain.usecase.vehicle.DeleteVehicleUseCase
import com.indusjs.fleet.domain.usecase.vehicle.GetVehicleByIdUseCase
import com.indusjs.fleet.domain.usecase.vehicle.UpdateVehicleUseCase
import com.indusjs.fleet.presentation.vehicles.detail.VehicleDetailContract.Effect
import com.indusjs.fleet.presentation.vehicles.detail.VehicleDetailContract.Intent
import com.indusjs.fleet.presentation.vehicles.detail.VehicleDetailContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Vehicle Detail screen implementing MVI pattern.
 */
@Inject
class VehicleDetailViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getVehicleByIdUseCase: GetVehicleByIdUseCase,
    private val updateVehicleUseCase: UpdateVehicleUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadVehicle -> loadVehicle(intent.vehicleId)
            is Intent.Refresh -> currentState.vehicle?.let { loadVehicle(it.id) }

            // Edit mode
            is Intent.EnterEditMode -> enterEditMode()
            is Intent.ExitEditMode -> exitEditMode()

            // Field updates
            is Intent.UpdateMake -> updateMake(intent.value)
            is Intent.UpdateModel -> updateModel(intent.value)
            is Intent.UpdateYear -> updateYear(intent.value)
            is Intent.UpdateVehicleType -> updateState { copy(vehicleType = intent.type) }
            is Intent.UpdateFuelType -> updateState { copy(fuelType = intent.value) }
            is Intent.UpdateColor -> updateState { copy(color = intent.value) }
            is Intent.UpdateCapacity -> updateState { copy(capacity = intent.value) }

            // Actions
            is Intent.SaveChanges -> saveChanges()
            is Intent.DeleteVehicle -> sendEffect(Effect.ShowDeleteConfirmation)
            is Intent.ConfirmDelete -> confirmDelete()

            // Navigation & errors
            is Intent.NavigateBack -> sendEffect(Effect.NavigateBack)
            is Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    private suspend fun loadVehicle(vehicleId: String) {
        updateState { copy(isLoading = true, error = null, vehicleId = vehicleId) }

        withContext(dispatcherProvider.io) {
            when (val result = getVehicleByIdUseCase(vehicleId)) {
                is Result.Success -> {
                    val vehicle = result.data
                    updateState {
                        copy(
                            isLoading = false,
                            vehicle = vehicle,
                            // Populate editable fields
                            registrationNumber = vehicle.registrationNumber,
                            make = vehicle.make,
                            model = vehicle.model,
                            year = vehicle.year.toString(),
                            vehicleType = vehicle.type,
                            fuelType = vehicle.fuelType.replaceFirstChar { it.uppercaseChar() },
                            color = vehicle.color.replaceFirstChar { it.uppercaseChar() },
                            capacity = vehicle.capacity.toString()
                        )
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoading = false,
                            error = result.message ?: "Failed to load vehicle"
                        )
                    }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to load vehicle"))
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    private fun enterEditMode() {
        updateState { copy(isEditMode = true) }
    }

    private fun exitEditMode() {
        // Reset to original values
        val vehicle = currentState.vehicle
        if (vehicle != null) {
            updateState {
                copy(
                    isEditMode = false,
                    make = vehicle.make,
                    model = vehicle.model,
                    year = vehicle.year.toString(),
                    vehicleType = vehicle.type,
                    fuelType = vehicle.fuelType.replaceFirstChar { it.uppercaseChar() },
                    color = vehicle.color.replaceFirstChar { it.uppercaseChar() },
                    capacity = vehicle.capacity.toString(),
                    // Clear errors
                    makeError = null,
                    modelError = null,
                    yearError = null
                )
            }
        } else {
            updateState { copy(isEditMode = false) }
        }
    }

    private fun updateMake(value: String) {
        val error = if (value.isBlank()) "Make is required" else null
        updateState { copy(make = value, makeError = error) }
    }

    private fun updateModel(value: String) {
        val error = if (value.isBlank()) "Model is required" else null
        updateState { copy(model = value, modelError = error) }
    }

    private fun updateYear(value: String) {
        val error = when {
            value.isBlank() -> "Year is required"
            value.toIntOrNull() == null -> "Invalid year"
            value.toInt() < 1900 || value.toInt() > 2030 -> "Year must be between 1900 and 2030"
            else -> null
        }
        updateState { copy(year = value, yearError = error) }
    }

    private suspend fun saveChanges() {
        if (!validateForm()) {
            sendEffect(Effect.ShowSnackbar("Please fill all required fields correctly"))
            return
        }

        val currentVehicle = currentState.vehicle ?: return
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            val updatedVehicle = currentVehicle.copy(
                make = currentState.make.trim(),
                model = currentState.model.trim(),
                year = currentState.year.toIntOrNull() ?: currentVehicle.year,
                type = currentState.vehicleType,
                fuelType = currentState.fuelType.lowercase(),
                color = currentState.color.lowercase(),
                capacity = currentState.capacity.toIntOrNull() ?: currentVehicle.capacity
            )

            when (val result = updateVehicleUseCase(updatedVehicle)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isSaving = false,
                            isEditMode = false,
                            vehicle = result.data,
                            make = result.data.make,
                            model = result.data.model,
                            year = result.data.year.toString(),
                            vehicleType = result.data.type,
                            fuelType = result.data.fuelType.replaceFirstChar { it.uppercaseChar() },
                            color = result.data.color.replaceFirstChar { it.uppercaseChar() },
                            capacity = result.data.capacity.toString()
                        )
                    }
                    sendEffect(Effect.ShowSnackbar("Vehicle updated successfully"))
                    sendEffect(Effect.VehicleUpdated)
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to update vehicle"))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    private fun validateForm(): Boolean {
        val makeError = if (currentState.make.isBlank()) "Make is required" else null
        val modelError = if (currentState.model.isBlank()) "Model is required" else null
        val yearError = when {
            currentState.year.isBlank() -> "Year is required"
            currentState.year.toIntOrNull() == null -> "Invalid year"
            else -> null
        }

        updateState {
            copy(
                makeError = makeError,
                modelError = modelError,
                yearError = yearError
            )
        }

        return makeError == null && modelError == null && yearError == null
    }

    private suspend fun confirmDelete() {
        val vehicleId = currentState.vehicle?.id ?: return
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            when (val result = deleteVehicleUseCase(vehicleId)) {
                is Result.Success -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowSnackbar("Vehicle deleted successfully"))
                    sendEffect(Effect.VehicleDeleted(vehicleId))
                    sendEffect(Effect.NavigateBack)
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowError(result.message ?: "Failed to delete vehicle"))
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }
}

