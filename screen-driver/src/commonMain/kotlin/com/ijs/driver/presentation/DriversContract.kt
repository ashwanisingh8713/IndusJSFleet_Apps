package com.ijs.driver.presentation

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.ijs.driver.domain.entity.Driver
import com.ijs.driver.domain.entity.DriverStatus

/**
 * MVI Contract for the Drivers List screen.
 */
object DriversContract {

    /**
     * UI State for the Drivers screen.
     */
    data class State(
        val isLoading: Boolean = false,
        val drivers: List<Driver> = emptyList(),
        val filteredDrivers: List<Driver> = emptyList(),
        val error: String? = null,
        val searchQuery: String = "",
        val selectedStatusFilter: DriverStatus? = null,
        val isRefreshing: Boolean = false,
        val isDeleting: Boolean = false,
        val driverToDelete: String? = null,
        val showDeleteConfirmation: Boolean = false
    ) : UiState

    /**
     * User intents for the Drivers screen.
     */
    sealed interface Intent : UiIntent {
        data object LoadDrivers : Intent
        data object RefreshDrivers : Intent
        data class SearchDrivers(val query: String) : Intent
        data class FilterByStatus(val status: DriverStatus?) : Intent
        data class SelectDriver(val driverId: String) : Intent
        data class DeleteDriver(val driverId: String) : Intent
        data object ConfirmDelete : Intent
        data object DismissDelete : Intent
        data class UpdateDriverStatus(val driverId: String, val status: DriverStatus) : Intent
        data class ToggleDriverActive(val driverId: String) : Intent
        data object AddDriver : Intent
        data object ClearFilters : Intent
    }

    /**
     * Side effects for the Drivers screen.
     */
    sealed interface Effect : UiEffect {
        data class NavigateToDriverDetail(val driverId: String) : Effect
        data object NavigateToAddDriver : Effect
        data class ShowSnackbar(val message: String) : Effect
        data class ShowError(val message: String) : Effect
    }
}

