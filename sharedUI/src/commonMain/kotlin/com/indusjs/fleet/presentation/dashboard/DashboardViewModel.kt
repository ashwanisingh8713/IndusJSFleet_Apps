package com.indusjs.fleet.presentation.dashboard

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.usecase.dashboard.GetDashboardUseCase
import com.indusjs.fleet.domain.usecase.dashboard.RefreshDashboardUseCase
import com.indusjs.fleet.presentation.dashboard.DashboardContract.Effect
import com.indusjs.fleet.presentation.dashboard.DashboardContract.Intent
import com.indusjs.fleet.presentation.dashboard.DashboardContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Dashboard screen implementing MVI pattern.
 */
@Inject
class DashboardViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getDashboardUseCase: GetDashboardUseCase,
    private val refreshDashboardUseCase: RefreshDashboardUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    // Track if we've received fresh data from network
    private var hasReceivedFreshData = false

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

    /**
     * Load dashboard with offline-first strategy.
     *
     * Important: Don't show offline banner just because initial data came from cache.
     * Only show offline banner when network fetch fails after we already have cache.
     */
    private suspend fun loadDashboard() {
        // Reset state tracking for new load
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

                        if (data.isFromCache) {
                            // This is cached data - show it but don't set offline yet
                            // We'll only show offline if network fetch fails after this
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
                                    // Don't change isOffline here - wait for network result
                                    error = null,
                                    lastUpdated = data.stats.lastUpdated ?: formatCacheTime(data.cachedAt)
                                )
                            }
                        } else {
                            // This is fresh data from network - we're definitely online
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
                                    isOffline = false, // Definitely online - got fresh data
                                    error = null,
                                    lastUpdated = data.stats.lastUpdated ?: "Just now"
                                )
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
     * Handle errors - only show offline banner if we have cached data and network failed.
     */
    private fun handleError(result: Result.Error, hadCachedData: Boolean) {
        val hasCachedData = state.value.hasCachedData || hadCachedData
        val errorMessage = result.message ?: result.exception.message ?: "Failed to load dashboard"

        if (hasCachedData) {
            // We have cache and network failed - NOW show offline banner
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
            // No cache - show error screen
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
     * Force refresh from network using RefreshDashboardUseCase.
     */
    private suspend fun refreshDashboard() {
        updateState { copy(isRefreshing = true, error = null) }

        withContext(dispatcherProvider.io) {
            when (val result = refreshDashboardUseCase()) {
                is Result.Success -> {
                    val data = result.data
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
                            lastUpdated = data.stats.lastUpdated ?: "Just now"
                        )
                    }
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

    /**
     * Format cache timestamp for display.
     */
    private fun formatCacheTime(timestamp: Long?): String {
        return com.indusjs.fleet.core.util.formatRelativeTime(timestamp)
    }
}
