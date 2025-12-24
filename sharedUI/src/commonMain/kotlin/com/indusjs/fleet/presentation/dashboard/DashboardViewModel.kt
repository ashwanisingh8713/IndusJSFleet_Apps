package com.indusjs.fleet.presentation.dashboard

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.entity.dashboard.Alert
import com.indusjs.fleet.domain.entity.dashboard.AlertType
import com.indusjs.fleet.domain.entity.dashboard.DashboardStats
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.fleet.presentation.dashboard.DashboardContract.Effect
import com.indusjs.fleet.presentation.dashboard.DashboardContract.Intent
import com.indusjs.fleet.presentation.dashboard.DashboardContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Dashboard screen implementing MVI pattern.
 */
@Inject
class DashboardViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val userRepository: UserRepository
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
        }
    }

    private suspend fun loadDashboard() {
        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                // Load user profile to get user name
                loadUserProfile()

                // TODO: Replace with actual repository call
                delay(500) // Simulate network delay

                // Using a simple timestamp for mock data
                val currentTime = 1734700000000L // Dec 20, 2024 approximate
                val mockStats = DashboardStats(
                    totalVehicles = 45,
                    activeVehicles = 38,
                    totalDrivers = 52,
                    activeDrivers = 41,
                    ongoingTrips = 12,
                    completedTripsToday = 28,
                    totalDistance = 1245.5,
                    fuelConsumption = 342.8,
                    alerts = listOf(
                        Alert(
                            id = "1",
                            title = "Maintenance Due",
                            message = "Vehicle TRK-001 is due for maintenance",
                            type = AlertType.MAINTENANCE,
                            timestamp = currentTime
                        ),
                        Alert(
                            id = "2",
                            title = "Low Fuel",
                            message = "Vehicle TRK-005 has low fuel level",
                            type = AlertType.FUEL_LOW,
                            timestamp = currentTime - 3600000
                        )
                    )
                )

                updateState { copy(isLoading = false, stats = mockStats) }
            } catch (e: Exception) {
                updateState { copy(isLoading = false, error = e.message ?: "Failed to load dashboard") }
                sendEffect(Effect.ShowSnackbar("Failed to load dashboard"))
            }
        }
    }

    private suspend fun refreshDashboard() {
        updateState { copy(isRefreshing = true) }
        loadDashboard()
        updateState { copy(isRefreshing = false) }
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
     * Load user profile to get user name and role for display.
     */
    private suspend fun loadUserProfile() {
        try {
            val result = userRepository.getProfile()
            result.fold(
                onSuccess = { profile ->
                    updateState {
                        copy(
                            userName = profile.user.fullName,
                            userRole = profile.user.role.name.lowercase()
                                .replaceFirstChar { it.uppercase() }
                        )
                    }
                },
                onFailure = {
                    // If profile fetch fails, use default values
                    updateState { copy(userName = "User", userRole = "") }
                }
            )
        } catch (e: Exception) {
            // If profile fetch fails, use default values
            updateState { copy(userName = "User", userRole = "") }
        }
    }
}

