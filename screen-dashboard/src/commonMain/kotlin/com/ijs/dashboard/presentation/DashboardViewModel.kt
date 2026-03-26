package com.ijs.dashboard.presentation

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.error.result.Result
import com.indusjs.fleet.data.model.dashboard.CostOverviewFilter
import com.indusjs.fleet.data.model.dashboard.FinancialPeriod
import com.indusjs.fleet.domain.entity.dashboard.CostOverview
import com.indusjs.fleet.domain.entity.dashboard.DriverStatusSummary
import com.indusjs.fleet.domain.entity.dashboard.FinancialSummary
import com.indusjs.fleet.domain.entity.dashboard.PendingPayment
import com.indusjs.fleet.domain.entity.dashboard.TripSummary
import com.indusjs.fleet.domain.entity.dashboard.VehicleStatusSummary
import com.indusjs.fleet.domain.usecase.dashboard.GetAlertsStatusUseCase
import com.indusjs.fleet.domain.usecase.dashboard.GetCostOverviewUseCase
import com.indusjs.fleet.domain.usecase.dashboard.GetDashboardUseCase
import com.indusjs.fleet.domain.usecase.dashboard.GetFinancialSummaryUseCase
import com.indusjs.fleet.domain.usecase.dashboard.GetPendingPaymentsUseCase
import com.indusjs.fleet.domain.usecase.dashboard.RefreshDashboardUseCase
import com.ijs.dashboard.presentation.DashboardContract.Effect
import com.ijs.dashboard.presentation.DashboardContract.Intent
import com.ijs.dashboard.presentation.DashboardContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Dashboard screen implementing MVI pattern.
 */
@Inject
class DashboardViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getDashboardUseCase: GetDashboardUseCase,
    private val refreshDashboardUseCase: RefreshDashboardUseCase,
    private val getCostOverviewUseCase: GetCostOverviewUseCase? = null,
    private val getPendingPaymentsUseCase: GetPendingPaymentsUseCase? = null,
    private val getAlertsStatusUseCase: GetAlertsStatusUseCase? = null,
    private val getFinancialSummaryUseCase: GetFinancialSummaryUseCase? = null
) : MviViewModel<State, Intent, Effect>(State()) {

    companion object {
        // Cache pending payments data to survive ViewModel recreation
        private var cachedPendingPayments: List<PendingPayment>? = null
        private var cachedTotalPendingAmount: Double = 0.0
        private var cachedPendingPaymentsCount: Int = 0
        private var pendingPaymentsLoaded: Boolean = false
    }

    // Track if we've received fresh data from network
    private var hasReceivedFreshData = false

    init {
        // Restore cached pending payments data if available (survives ViewModel recreation)
        if (pendingPaymentsLoaded && cachedPendingPayments != null) {
            updateState {
                copy(
                    pendingPayments = cachedPendingPayments ?: emptyList(),
                    totalPendingAmount = cachedTotalPendingAmount,
                    pendingPaymentsCount = cachedPendingPaymentsCount,
                    hasPendingPaymentsLoaded = true
                )
            }
        }
        sendIntent(Intent.LoadDashboard)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadDashboard -> loadDashboard()
            is Intent.RefreshDashboard -> refreshDashboard()
            is Intent.NavigateToVehicles -> sendEffect(Effect.NavigateToVehicles)
            is Intent.NavigateToDrivers -> sendEffect(Effect.NavigateToDrivers)
            is Intent.NavigateToTrips -> sendEffect(Effect.NavigateToTrips)
            is Intent.NavigateToMaps -> sendEffect(Effect.NavigateToMaps)
            is Intent.MarkAlertAsRead -> markAlertAsRead(intent.alertId)
            is Intent.DismissAlert -> dismissAlert(intent.alertId)
            is Intent.DismissOfflineBanner -> dismissOfflineBanner()
            is Intent.RetryConnection -> retryConnection()

            // Cost Overview
            is Intent.ChangeCostFilter -> changeCostFilter(intent.filter)
            is Intent.LoadCostOverview -> loadCostOverview()

            // Financial Summary
            is Intent.ChangeFinancialPeriod -> changeFinancialPeriod(intent.period)
            is Intent.LoadFinancialSummary -> loadFinancialSummary()

            // Other intents
            is Intent.LoadPendingPayments -> loadPendingPayments()
            is Intent.LoadAlertsStatus -> loadAlertsStatus()
            is Intent.NavigateToAddTripCost -> sendEffect(Effect.NavigateToAddTripCost)
            is Intent.NavigateToAddVehicleCost -> sendEffect(Effect.NavigateToAddVehicleCost)
            is Intent.NavigateToNotifications -> sendEffect(Effect.NavigateToNotifications)

            // Navigation to Add entities (from empty states)
            is Intent.NavigateToAddVehicle -> sendEffect(Effect.NavigateToAddVehicle)
            is Intent.NavigateToAddDriver -> sendEffect(Effect.NavigateToAddDriver)
            is Intent.NavigateToCreateTrip -> sendEffect(Effect.NavigateToCreateTrip)
            is Intent.NavigateToAddDriverCost -> sendEffect(Effect.NavigateToAddDriverCost)

            // Navigation to Alerts list
            is Intent.NavigateToAlertsList -> sendEffect(Effect.NavigateToAlertsList)

            // Navigation to Customers
            is Intent.NavigateToCustomers -> sendEffect(Effect.NavigateToCustomers)
        }
    }

    /**
     * Build VehicleStatusSummary from dashboard stats.
     */
    private fun buildVehicleStatus(stats: com.indusjs.fleet.domain.entity.dashboard.DashboardStats) = VehicleStatusSummary(
        onTripPlanned = stats.plannedTrips,
        onTripInProgress = stats.ongoingTrips,
        underMaintenance = stats.maintenanceVehicles,
        available = stats.activeVehicles - stats.ongoingTrips,
        inactive = stats.inactiveVehicles,
        total = stats.totalVehicles
    )

    /**
     * Build DriverStatusSummary from dashboard stats.
     */
    private fun buildDriverStatus(stats: com.indusjs.fleet.domain.entity.dashboard.DashboardStats) = DriverStatusSummary(
        onTripPlanned = 0,
        onTripInProgress = stats.driversOnTrip,
        available = stats.activeDrivers - stats.driversOnTrip,
        onLeave = stats.driversOnLeave,
        total = stats.totalDrivers
    )

    /**
     * Build TripSummary from dashboard stats.
     */
    private fun buildTripSummary(stats: com.indusjs.fleet.domain.entity.dashboard.DashboardStats) = TripSummary(
        inProgress = stats.ongoingTrips,
        planned = stats.plannedTrips,
        delayed = 0,
        completed = stats.completedTrips,
        total = stats.totalTrips
    )

    /**
     * Load dashboard with offline-first strategy.
     */
    private suspend fun loadDashboard() {
        hasReceivedFreshData = false
        var hasCachedEmission = false

        getDashboardUseCase()
            .flowOn(dispatcherProvider.io)
            .collect { result ->
                when (result) {
                    is Result.Loading -> {
                        if (!state.value.hasCachedData) {
                            updateState { copy(isLoading = true, error = null) }
                        }
                    }
                    is Result.Success -> {
                        val data = result.data
                        val vehicleStatus = buildVehicleStatus(data.stats)
                        val driverStatus = buildDriverStatus(data.stats)
                        val tripSummary = buildTripSummary(data.stats)

                        if (data.isFromCache) {
                            hasCachedEmission = true
                            updateState {
                                copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    stats = data.stats,
                                    userName = data.userInfo.fullName,
                                    userRole = data.userInfo.role.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase() else it.toString()
                                    },
                                    hasCachedData = true,
                                    error = null,
                                    lastUpdated = data.stats.lastUpdated ?: formatCacheTime(data.cachedAt),
                                    vehicleStatus = vehicleStatus,
                                    driverStatus = driverStatus,
                                    tripSummary = tripSummary,
                                    notificationCount = data.stats.totalAlerts
                                )
                            }
                        } else {
                            hasReceivedFreshData = true
                            updateState {
                                copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    stats = data.stats,
                                    userName = data.userInfo.fullName,
                                    userRole = data.userInfo.role.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase() else it.toString()
                                    },
                                    hasCachedData = true,
                                    isOffline = false,
                                    error = null,
                                    lastUpdated = data.stats.lastUpdated ?: "Just now",
                                    vehicleStatus = vehicleStatus,
                                    driverStatus = driverStatus,
                                    tripSummary = tripSummary,
                                    notificationCount = data.stats.totalAlerts
                                )
                            }

                            // Load additional data after main dashboard loads
                            loadCostOverview()
                            loadPendingPayments()
                            loadAlertsStatus()
                            // Load financial summary for Owner/GM
                            if (data.userInfo.role.lowercase() in listOf("owner", "general_manager", "generalmanager")) {
                                loadFinancialSummary()
                            }
                        }
                    }
                    is Result.Error -> {
                        handleError(result, hasCachedEmission)
                    }
                }
            }
    }

    /**
     * Load cost overview with current filter.
     */
    private suspend fun loadCostOverview() {
        val useCase = getCostOverviewUseCase ?: return

        updateState { copy(isLoadingCostOverview = true, costOverviewError = null) }

        withContext(dispatcherProvider.io) {
            when (val result = useCase(state.value.selectedCostFilter)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isLoadingCostOverview = false,
                            costOverview = result.data,
                            costOverviewError = null
                        )
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoadingCostOverview = false,
                            costOverviewError = result.message ?: "Failed to load cost overview"
                        )
                    }
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }

    /**
     * Load pending payments - only once when app launches.
     * Does not show loading UI to avoid flickering.
     * Uses companion object cache to survive ViewModel recreation.
     */
    private suspend fun loadPendingPayments() {
        val useCase = getPendingPaymentsUseCase ?: return

        // Skip if already loaded - check companion object flag
        if (pendingPaymentsLoaded) {
            return
        }
        pendingPaymentsLoaded = true  // Set immediately to prevent race conditions

        // Don't show loading state to avoid flickering

        withContext(dispatcherProvider.io) {
            when (val result = useCase()) {
                is Result.Success -> {
                    // Cache in companion object for ViewModel recreation
                    cachedPendingPayments = result.data.payments
                    cachedTotalPendingAmount = result.data.totalPending
                    cachedPendingPaymentsCount = result.data.totalCount

                    updateState {
                        copy(
                            isLoadingPendingPayments = false,
                            pendingPayments = result.data.payments,
                            totalPendingAmount = result.data.totalPending,
                            pendingPaymentsCount = result.data.totalCount,
                            pendingPaymentsError = null,
                            hasPendingPaymentsLoaded = true
                        )
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoadingPendingPayments = false,
                            pendingPaymentsError = result.message ?: "Failed to load pending payments",
                            hasPendingPaymentsLoaded = true
                        )
                    }
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }

    /**
     * Load alerts status with detailed counts.
     */
    private suspend fun loadAlertsStatus() {
        val useCase = getAlertsStatusUseCase ?: return

        updateState { copy(isLoadingAlertsSummary = true) }

        withContext(dispatcherProvider.io) {
            when (val result = useCase()) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isLoadingAlertsSummary = false,
                            alertsSummary = result.data,
                            notificationCount = result.data.totalAlerts
                        )
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(isLoadingAlertsSummary = false)
                    }
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }

    /**
     * Change cost overview filter and reload data.
     */
    private suspend fun changeCostFilter(filter: CostOverviewFilter) {
        updateState { copy(selectedCostFilter = filter) }
        loadCostOverview()
    }

    /**
     * Change financial period and reload summary.
     */
    private suspend fun changeFinancialPeriod(period: FinancialPeriod) {
        updateState { copy(selectedFinancialPeriod = period) }
        loadFinancialSummary()
    }

    /**
     * Load financial summary for Owner/General Manager.
     */
    private suspend fun loadFinancialSummary() {
        val useCase = getFinancialSummaryUseCase ?: return

        updateState { copy(isLoadingFinancialSummary = true, financialSummaryError = null) }

        withContext(dispatcherProvider.io) {
            when (val result = useCase(state.value.selectedFinancialPeriod)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isLoadingFinancialSummary = false,
                            financialSummary = result.data,
                            financialSummaryError = null
                        )
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoadingFinancialSummary = false,
                            financialSummaryError = result.message ?: "Failed to load financial summary"
                        )
                    }
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }

    /**
     * Handle errors - only show offline banner if we have cached data and network failed.
     */
    private fun handleError(result: Result.Error, hadCachedData: Boolean) {
        val hasCachedData = state.value.hasCachedData || hadCachedData
        val errorMessage = result.message ?: result.exception.message ?: "Failed to load dashboard"

        if (hasCachedData) {
            updateState {
                copy(
                    isLoading = false,
                    isRefreshing = false,
                    isOffline = true,
                    error = errorMessage
                )
            }
            sendEffect(Effect.ShowSnackbar("Offline mode - showing cached data"))
        } else {
            updateState {
                copy(
                    isLoading = false,
                    isRefreshing = false,
                    isOffline = false,
                    error = errorMessage
                )
            }
        }
    }

    /**
     * Force refresh from network.
     */
    private suspend fun refreshDashboard() {
        updateState { copy(isRefreshing = true, error = null) }

        withContext(dispatcherProvider.io) {
            when (val result = refreshDashboardUseCase()) {
                is Result.Success -> {
                    val data = result.data
                    val vehicleStatus = buildVehicleStatus(data.stats)
                    val driverStatus = buildDriverStatus(data.stats)
                    val tripSummary = buildTripSummary(data.stats)

                    updateState {
                        copy(
                            isRefreshing = false,
                            stats = data.stats,
                            userName = data.userInfo.fullName,
                            userRole = data.userInfo.role.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase() else it.toString()
                            },
                            hasCachedData = true,
                            isOffline = false,
                            error = null,
                            lastUpdated = data.stats.lastUpdated ?: "Just now",
                            vehicleStatus = vehicleStatus,
                            driverStatus = driverStatus,
                            tripSummary = tripSummary,
                            notificationCount = data.stats.totalAlerts
                        )
                    }

                    // Reload additional data
                    loadCostOverview()
                    loadPendingPayments()
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isRefreshing = false,
                            isOffline = true,
                            error = result.message ?: "Failed to refresh"
                        )
                    }
                    sendEffect(Effect.ShowSnackbar("Offline mode - showing cached data"))
                }
                is Result.Loading -> { /* ignore */ }
            }
        }
    }

    private suspend fun retryConnection() {
        updateState { copy(isRefreshing = true, isOffline = false, error = null) }
        refreshDashboard()
    }

    private fun dismissOfflineBanner() {
        updateState { copy(isOffline = false, error = null) }
    }

    private fun markAlertAsRead(alertId: String) {
        updateState {
            copy(
                stats = stats.copy(
                    alerts = stats.alerts.map { alert ->
                        if (alert.id == alertId) alert.copy(isRead = true) else alert
                    }
                )
            )
        }
    }

    private fun dismissAlert(alertId: String) {
        updateState {
            copy(
                stats = stats.copy(
                    alerts = stats.alerts.filter { it.id != alertId }
                )
            )
        }
        sendEffect(Effect.ShowSnackbar("Alert dismissed"))
    }

    private fun formatCacheTime(timestamp: Long?): String {
        return com.indusjs.fleet.core.util.formatRelativeTime(timestamp)
    }
}

