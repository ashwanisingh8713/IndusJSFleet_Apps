package com.indusjs.fleet.presentation.dashboard

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.dashboard.Alert
import com.indusjs.fleet.domain.entity.dashboard.DashboardStats

/**
 * MVI Contract for the Dashboard screen.
 */
object DashboardContract {

    /**
     * UI State for the Dashboard screen.
     * Supports offline mode by preserving cached data when errors occur.
     */
    data class State(
        val isLoading: Boolean = false,
        val stats: DashboardStats = DashboardStats(),
        val error: String? = null,
        val isRefreshing: Boolean = false,
        val userName: String = "", // User's full name for display
        val userRole: String = "",  // User's role for display
        val hasCachedData: Boolean = false,
        val isOffline: Boolean = false,
        val lastUpdated: String? = null
    ) : UiState

    /**
     * User intents for the Dashboard screen.
     */
    sealed interface Intent : UiIntent {
        data object LoadDashboard : Intent
        data object RefreshDashboard : Intent
        data object NavigateToVehicles : Intent
        data object NavigateToDrivers : Intent
        data object NavigateToTrips : Intent
        data object NavigateToMaps : Intent
        data class MarkAlertAsRead(val alertId: String) : Intent
        data class DismissAlert(val alertId: String) : Intent
        data object DismissOfflineBanner : Intent
        data object RetryConnection : Intent
    }

    /**
     * Side effects for the Dashboard screen.
     */
    sealed interface Effect : UiEffect {
        data object NavigateToVehicles : Effect
        data object NavigateToDrivers : Effect
        data object NavigateToTrips : Effect
        data object NavigateToMaps : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}
