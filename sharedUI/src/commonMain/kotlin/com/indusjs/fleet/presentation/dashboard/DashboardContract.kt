package com.indusjs.fleet.presentation.dashboard

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.data.model.dashboard.CostOverviewFilter
import com.indusjs.fleet.data.model.dashboard.FinancialPeriod
import com.indusjs.fleet.domain.entity.dashboard.Alert
import com.indusjs.fleet.domain.entity.dashboard.AlertsSummary
import com.indusjs.fleet.domain.entity.dashboard.CostOverview
import com.indusjs.fleet.domain.entity.dashboard.DashboardStats
import com.indusjs.fleet.domain.entity.dashboard.DriverStatusSummary
import com.indusjs.fleet.domain.entity.dashboard.ExpiryAlert
import com.indusjs.fleet.domain.entity.dashboard.FinancialSummary
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

        // Financial Summary (Owner/GM only)
        val financialSummary: FinancialSummary? = null,
        val selectedFinancialPeriod: FinancialPeriod = FinancialPeriod.MONTHLY,
        val isLoadingFinancialSummary: Boolean = false,
        val financialSummaryError: String? = null,

        // Pending Payments
        val pendingPayments: List<PendingPayment> = emptyList(),
        val totalPendingAmount: Double = 0.0,
        val pendingPaymentsCount: Int = 0,
        val isLoadingPendingPayments: Boolean = false,
        val pendingPaymentsError: String? = null,
        val hasPendingPaymentsLoaded: Boolean = false,  // Track if loaded once

        // Vehicle Status Summary
        val vehicleStatus: VehicleStatusSummary = VehicleStatusSummary(),

        // Driver Status Summary
        val driverStatus: DriverStatusSummary = DriverStatusSummary(),

        // Trip Summary
        val tripSummary: TripSummary = TripSummary(),

        // Expiry Alerts (for documents)
        val expiryAlerts: List<ExpiryAlert> = emptyList(),

        // Alerts Summary with detailed counts
        val alertsSummary: AlertsSummary = AlertsSummary(),
        val isLoadingAlertsSummary: Boolean = false,

        // Notification count (for badge)
        val notificationCount: Int = 0
    ) : UiState {
        /**
         * Determines if the user can view financial data.
         * Only Owner and General Manager have financial access.
         */
        val hasFinancialAccess: Boolean
            get() {
                val role = userRole.lowercase()
                return role == "owner" || role == "general_manager" || role == "generalmanager" || role == "general manager"
            }

        /**
         * Whether financial summary is loaded and available.
         */
        val hasFinancialSummary: Boolean get() = financialSummary != null
    }

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

        // Financial Summary
        data class ChangeFinancialPeriod(val period: FinancialPeriod) : Intent
        data object LoadFinancialSummary : Intent

        // Pending Payments
        data object LoadPendingPayments : Intent

        // Alerts Status
        data object LoadAlertsStatus : Intent

        // Navigation to Add Cost screens
        data object NavigateToAddTripCost : Intent
        data object NavigateToAddVehicleCost : Intent

        // Navigation to Add entities (from empty states)
        data object NavigateToAddVehicle : Intent
        data object NavigateToAddDriver : Intent
        data object NavigateToCreateTrip : Intent
        data object NavigateToAddDriverCost : Intent

        // Navigation to Alerts list
        data object NavigateToAlertsList : Intent

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

        // Navigation to Add entities (from empty states)
        data object NavigateToAddVehicle : Effect
        data object NavigateToAddDriver : Effect
        data object NavigateToCreateTrip : Effect
        data object NavigateToAddDriverCost : Effect

        // Navigation to Alerts list
        data object NavigateToAlertsList : Effect
    }
}
