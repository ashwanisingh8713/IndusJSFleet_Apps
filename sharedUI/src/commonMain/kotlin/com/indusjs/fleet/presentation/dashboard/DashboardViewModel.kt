package com.indusjs.fleet.presentation.dashboard

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.repository.dashboard.DashboardRepository
import com.indusjs.fleet.presentation.dashboard.DashboardContract.Effect
import com.indusjs.fleet.presentation.dashboard.DashboardContract.Intent
import com.indusjs.fleet.presentation.dashboard.DashboardContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Dashboard screen implementing MVI pattern.
 * Supports offline mode by preserving cached data when errors occur.
 */
@Inject
class DashboardViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val dashboardRepository: DashboardRepository
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
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
        }
    }

    private suspend fun loadDashboard() {
        // Check current state BEFORE starting the load
        val hasExistingData = state.value.hasCachedData

        // Only show full loading spinner on initial load (no cached data)
        if (!hasExistingData) {
            updateState { copy(isLoading = true, error = null, isOffline = false) }
        }

        withContext(dispatcherProvider.io) {
            try {
                val result = dashboardRepository.getDashboard()

                result.fold(
                    onSuccess = { dashboardData ->
                        updateState {
                            copy(
                                isLoading = false,
                                isRefreshing = false,
                                stats = dashboardData.stats,
                                userName = dashboardData.userInfo.fullName,
                                userRole = dashboardData.userInfo.role.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase() else it.toString()
                                },
                                hasCachedData = true,
                                isOffline = false,
                                error = null,
                                lastUpdated = dashboardData.stats.lastUpdated ?: getCurrentTimestamp()
                            )
                        }
                    },
                    onFailure = { error ->
                        // Re-check current state as it might have changed
                        val currentHasCachedData = state.value.hasCachedData
                        handleLoadError(currentHasCachedData, error.message)
                    }
                )
            } catch (e: Exception) {
                // Re-check current state as it might have changed
                val currentHasCachedData = state.value.hasCachedData
                handleLoadError(currentHasCachedData, e.message)
            }
        }
    }

    private fun handleLoadError(hasCachedData: Boolean, errorMessage: String?) {
        if (hasCachedData) {
            // We have cached data - show it with offline banner
            // IMPORTANT: Don't clear stats, userName, userRole - keep the cached values
            updateState {
                copy(
                    isLoading = false,
                    isRefreshing = false,
                    isOffline = true,
                    error = errorMessage ?: "Connection failed"
                    // stats, userName, userRole remain unchanged (cached)
                )
            }
            sendEffect(Effect.ShowSnackbar("Offline mode - showing cached data"))
        } else {
            // No cached data - show full error screen
            updateState {
                copy(
                    isLoading = false,
                    isRefreshing = false,
                    isOffline = false,
                    error = errorMessage ?: "Failed to load dashboard"
                )
            }
        }
    }

    private suspend fun refreshDashboard() {
        // Don't show loading spinner, just show refreshing indicator
        updateState { copy(isRefreshing = true, error = null) }
        loadDashboard()
    }

    private fun dismissOfflineBanner() {
        updateState { copy(isOffline = false, error = null) }
    }

    private suspend fun retryConnection() {
        // Show refreshing state while retrying
        updateState { copy(isRefreshing = true, isOffline = false, error = null) }
        loadDashboard()
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

    private fun getCurrentTimestamp(): String {
        return "Just now"
    }
}
