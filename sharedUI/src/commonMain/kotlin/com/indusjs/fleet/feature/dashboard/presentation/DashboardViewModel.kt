package com.indusjs.fleet.feature.dashboard.presentation

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.feature.dashboard.domain.entity.Alert
import com.indusjs.fleet.feature.dashboard.domain.entity.AlertType
import com.indusjs.fleet.feature.dashboard.domain.entity.DashboardStats
import com.indusjs.fleet.feature.dashboard.presentation.DashboardContract.Effect
import com.indusjs.fleet.feature.dashboard.presentation.DashboardContract.Intent
import com.indusjs.fleet.feature.dashboard.presentation.DashboardContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Dashboard screen implementing MVI pattern.
 */
@Inject
class DashboardViewModel(
    private val dispatcherProvider: DispatcherProvider
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
}

