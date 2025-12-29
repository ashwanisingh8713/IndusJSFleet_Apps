package com.indusjs.fleet.presentation.dashboard

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.data.model.dashboard.CostOverviewFilter
import com.indusjs.fleet.domain.entity.dashboard.Alert
import com.indusjs.fleet.domain.entity.dashboard.CostOverview
import com.indusjs.fleet.domain.entity.dashboard.DashboardStats
import com.indusjs.fleet.domain.entity.dashboard.DriverStatusSummary
import com.indusjs.fleet.domain.entity.dashboard.ExpiryAlert
import com.indusjs.fleet.domain.entity.dashboard.PendingPayment
import com.indusjs.fleet.domain.entity.dashboard.TripSummary
import com.indusjs.fleet.domain.entity.dashboard.VehicleStatusSummary

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
        val userName: String = "",
        val userRole: String = "",
        val hasCachedData: Boolean = false,
        val isOffline: Boolean = false,
        val lastUpdated: String? = null,

        // Cost Overview
        val costOverview: CostOverview = CostOverview(),
        val selectedCostFilter: CostOverviewFilter = CostOverviewFilter.TODAY,
        val isLoadingCostOverview: Boolean = false,
        val costOverviewError: String? = null,

        // Pending Payments
        val pendingPayments: List<PendingPayment> = emptyList(),
        val totalPendingAmount: Double = 0.0,
        val pendingPaymentsCount: Int = 0,
        val isLoadingPendingPayments: Boolean = false,
        val pendingPaymentsError: String? = null,

        // Vehicle Status Summary
        val vehicleStatus: VehicleStatusSummary = VehicleStatusSummary(),

        // Driver Status Summary
        val driverStatus: DriverStatusSummary = DriverStatusSummary(),

        // Trip Summary
        val tripSummary: TripSummary = TripSummary(),

        // Expiry Alerts (for documents)
        val expiryAlerts: List<ExpiryAlert> = emptyList(),

        // Notification count (for badge)
        val notificationCount: Int = 0
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

        // Cost Overview
        data class ChangeCostFilter(val filter: CostOverviewFilter) : Intent
        data object LoadCostOverview : Intent

        // Pending Payments
        data object LoadPendingPayments : Intent

        // Navigation to Add Cost screens
        data object NavigateToAddTripCost : Intent
        data object NavigateToAddVehicleCost : Intent

        // Notifications
        data object NavigateToNotifications : Intent
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

        // New navigation effects
        data object NavigateToAddTripCost : Effect
        data object NavigateToAddVehicleCost : Effect
        data object NavigateToNotifications : Effect
    }
}
