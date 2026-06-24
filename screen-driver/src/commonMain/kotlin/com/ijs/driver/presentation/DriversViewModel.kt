package com.ijs.driver.presentation

import androidx.lifecycle.viewModelScope
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.error.result.Result
import com.indusjs.fleet.domain.repository.states.StatesRepository
import com.indusjs.uicomponents.components.UiText
import com.ijs.driver.domain.entity.DriverStatus
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.success_driver_deleted
import indusjsfleet.ijs_ui_components_lib.generated.resources.success_driver_activated
import indusjsfleet.ijs_ui_components_lib.generated.resources.success_driver_deactivated
import indusjsfleet.ijs_ui_components_lib.generated.resources.success_driver_status_updated
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_load_drivers
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_delete_driver
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_update_driver_status
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_toggle_driver_active
import com.ijs.driver.domain.usecase.DeleteDriverUseCase
import com.ijs.driver.domain.usecase.GetDriversUseCase
import com.ijs.driver.domain.usecase.ToggleDriverActiveUseCase
import com.ijs.driver.domain.usecase.UpdateDriverStatusUseCase
import com.ijs.driver.presentation.DriversContract.Effect
import com.ijs.driver.presentation.DriversContract.Intent
import com.ijs.driver.presentation.DriversContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Drivers screen implementing MVI pattern.
 *
 * Dependencies are provided via DefaultViewModelProvider.
 */
@Inject
class DriversViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getDriversUseCase: GetDriversUseCase,
    private val deleteDriverUseCase: DeleteDriverUseCase,
    private val updateDriverStatusUseCase: UpdateDriverStatusUseCase,
    private val toggleDriverActiveUseCase: ToggleDriverActiveUseCase,
    private val statesRepository: StatesRepository
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        loadStateLabels()
        sendIntent(Intent.LoadDrivers)
    }

    private fun loadStateLabels() {
        viewModelScope.launch {
            try {
                val labels = statesRepository.getDriverStatesFlat().toMap()
                updateState { copy(stateLabels = labels) }
            } catch (_: Exception) { /* fallback to empty → StatusConstants used */ }
        }
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadDrivers -> loadDrivers()
            is Intent.RefreshDrivers -> refreshDrivers()
            is Intent.SearchDrivers -> searchDrivers(intent.query)
            is Intent.FilterByStatus -> filterByStatus(intent.status)
            is Intent.SelectDriver -> selectDriver(intent.driverId)
            is Intent.DeleteDriver -> requestDeleteDriver(intent.driverId)
            is Intent.ConfirmDelete -> confirmDeleteDriver()
            is Intent.DismissDelete -> dismissDeleteDriver()
            is Intent.UpdateDriverStatus -> updateDriverStatus(intent.driverId, intent.status)
            is Intent.ToggleDriverActive -> toggleDriverActive(intent.driverId)
            is Intent.AddDriver -> sendEffect(Effect.NavigateToAddDriver)
            is Intent.ClearFilters -> clearFilters()
        }
    }

    private suspend fun loadDrivers() {
        withContext(dispatcherProvider.io) {
            getDriversUseCase().collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        updateState { copy(isLoading = true, error = null) }
                    }
                    is Result.Success -> {
                        updateState {
                            copy(
                                isLoading = false,
                                drivers = result.data,
                                filteredDrivers = result.data,
                                error = null
                            )
                        }
                        applyFilters()
                    }
                    is Result.Error -> {
                        val errorText = result.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.error_load_drivers)
                        updateState {
                            copy(
                                isLoading = false,
                                error = errorText
                            )
                        }
                        sendEffect(Effect.ShowError(errorText))
                    }
                }
            }
        }
    }

    private suspend fun refreshDrivers() {
        updateState { copy(isRefreshing = true) }
        loadDrivers()
        updateState { copy(isRefreshing = false) }
    }

    private fun searchDrivers(query: String) {
        updateState { copy(searchQuery = query) }
        applyFilters()
    }

    private fun filterByStatus(status: DriverStatus?) {
        updateState { copy(selectedStatusFilter = status) }
        applyFilters()
    }

    private fun selectDriver(driverId: String) {
        sendEffect(Effect.NavigateToDriverDetail(driverId))
    }

    private fun clearFilters() {
        updateState {
            copy(
                searchQuery = "",
                selectedStatusFilter = null,
                filteredDrivers = drivers
            )
        }
    }

    private fun applyFilters() {
        val currentState = currentState
        val filtered = currentState.drivers.filter { driver ->
            val matchesSearch = currentState.searchQuery.isEmpty() ||
                    driver.fullName.contains(currentState.searchQuery, ignoreCase = true) ||
                    driver.mobile.contains(currentState.searchQuery, ignoreCase = true) ||
                    driver.licenseNumber.contains(currentState.searchQuery, ignoreCase = true) ||
                    driver.email.contains(currentState.searchQuery, ignoreCase = true)

            val matchesStatus = currentState.selectedStatusFilter == null ||
                    driver.status == currentState.selectedStatusFilter

            matchesSearch && matchesStatus
        }
        updateState { copy(filteredDrivers = filtered) }
    }

    private fun requestDeleteDriver(driverId: String) {
        updateState { copy(driverToDelete = driverId, showDeleteConfirmation = true) }
    }

    private suspend fun confirmDeleteDriver() {
        val driverId = currentState.driverToDelete ?: return
        updateState { copy(showDeleteConfirmation = false, driverToDelete = null) }
        deleteDriver(driverId)
    }

    private fun dismissDeleteDriver() {
        updateState { copy(showDeleteConfirmation = false, driverToDelete = null) }
    }

    private suspend fun deleteDriver(driverId: String) {
        updateState { copy(isDeleting = true) }
        withContext(dispatcherProvider.io) {
            when (val result = deleteDriverUseCase(driverId)) {
                is Result.Success -> {
                    updateState {
                        val updatedDrivers = drivers.filter { it.id != driverId }
                        copy(
                            isDeleting = false,
                            drivers = updatedDrivers,
                            filteredDrivers = updatedDrivers.filter { driver ->
                                val matchesSearch = searchQuery.isEmpty() ||
                                        driver.fullName.contains(searchQuery, ignoreCase = true) ||
                                        driver.mobile.contains(searchQuery, ignoreCase = true)
                                val matchesStatus = selectedStatusFilter == null ||
                                        driver.status == selectedStatusFilter
                                matchesSearch && matchesStatus
                            }
                        )
                    }
                    sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.success_driver_deleted)))
                }
                is Result.Error -> {
                    updateState { copy(isDeleting = false) }
                    sendEffect(
                        Effect.ShowError(
                            result.message?.let { UiText.Raw(it) }
                                ?: UiText.StringRes(Res.string.error_delete_driver)
                        )
                    )
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    private suspend fun updateDriverStatus(driverId: String, status: DriverStatus) {
        withContext(dispatcherProvider.io) {
            when (val result = updateDriverStatusUseCase(driverId, status)) {
                is Result.Success -> {
                    updateState {
                        val updatedDrivers = drivers.map { driver ->
                            if (driver.id == driverId) result.data else driver
                        }
                        copy(
                            drivers = updatedDrivers,
                            filteredDrivers = updatedDrivers.filter { driver ->
                                val matchesSearch = searchQuery.isEmpty() ||
                                        driver.fullName.contains(searchQuery, ignoreCase = true) ||
                                        driver.mobile.contains(searchQuery, ignoreCase = true)
                                val matchesStatus = selectedStatusFilter == null ||
                                        driver.status == selectedStatusFilter
                                matchesSearch && matchesStatus
                            }
                        )
                    }
                    val apiValue = DriverStatus.toApiString(status)
                    val label = currentState.stateLabels[apiValue]
                        ?: DriverStatus.getDisplayLabel(status)
                    sendEffect(
                        Effect.ShowSnackbar(
                            UiText.StringRes(
                                Res.string.success_driver_status_updated,
                                args = listOf(label)
                            )
                        )
                    )
                }
                is Result.Error -> {
                    sendEffect(
                        Effect.ShowError(
                            result.message?.let { UiText.Raw(it) }
                                ?: UiText.StringRes(Res.string.error_update_driver_status)
                        )
                    )
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    private suspend fun toggleDriverActive(driverId: String) {
        withContext(dispatcherProvider.io) {
            when (val result = toggleDriverActiveUseCase(driverId)) {
                is Result.Success -> {
                    updateState {
                        val updatedDrivers = drivers.map { driver ->
                            if (driver.id == driverId) result.data else driver
                        }
                        copy(
                            drivers = updatedDrivers,
                            filteredDrivers = updatedDrivers.filter { driver ->
                                val matchesSearch = searchQuery.isEmpty() ||
                                        driver.fullName.contains(searchQuery, ignoreCase = true) ||
                                        driver.mobile.contains(searchQuery, ignoreCase = true)
                                val matchesStatus = selectedStatusFilter == null ||
                                        driver.status == selectedStatusFilter
                                matchesSearch && matchesStatus
                            }
                        )
                    }
                    val message = if (result.data.isActive) {
                        UiText.StringRes(Res.string.success_driver_activated)
                    } else {
                        UiText.StringRes(Res.string.success_driver_deactivated)
                    }
                    sendEffect(Effect.ShowSnackbar(message))
                }
                is Result.Error -> {
                    sendEffect(
                        Effect.ShowError(
                            result.message?.let { UiText.Raw(it) }
                                ?: UiText.StringRes(Res.string.error_toggle_driver_active)
                        )
                    )
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }
}

