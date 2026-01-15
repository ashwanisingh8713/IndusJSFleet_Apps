package com.indusjs.fleet.presentation.alerts

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.error.result.Result
import com.indusjs.fleet.domain.usecase.dashboard.GetAlertsStatusUseCase
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

/**
 * ViewModel for Alerts List screen.
 */
@Inject
class AlertsListViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getAlertsStatusUseCase: GetAlertsStatusUseCase
) : MviViewModel<AlertsListContract.State, AlertsListContract.Intent, AlertsListContract.Effect>(
    AlertsListContract.State()
) {

    init {
        sendIntent(AlertsListContract.Intent.LoadAlerts)
    }

    override suspend fun handleIntent(intent: AlertsListContract.Intent) {
        when (intent) {
            is AlertsListContract.Intent.LoadAlerts -> loadAlerts()
            is AlertsListContract.Intent.RefreshAlerts -> refreshAlerts()
            is AlertsListContract.Intent.ChangeFilter -> updateState { copy(selectedFilter = intent.filter) }
            is AlertsListContract.Intent.DismissAlert -> dismissAlert(intent.alertId)
            is AlertsListContract.Intent.NavigateBack -> sendEffect(AlertsListContract.Effect.NavigateBack)
            is AlertsListContract.Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    private suspend fun loadAlerts() {
        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            when (val result = getAlertsStatusUseCase()) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isLoading = false,
                            alertsSummary = result.data,
                            alerts = result.data.alerts
                        )
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                else -> {}
            }
        }
    }

    private suspend fun refreshAlerts() {
        updateState { copy(isRefreshing = true) }

        withContext(dispatcherProvider.io) {
            when (val result = getAlertsStatusUseCase()) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isRefreshing = false,
                            alertsSummary = result.data,
                            alerts = result.data.alerts
                        )
                    }
                    sendEffect(AlertsListContract.Effect.ShowSnackbar("Alerts refreshed"))
                }
                is Result.Error -> {
                    updateState { copy(isRefreshing = false) }
                    sendEffect(AlertsListContract.Effect.ShowError("Failed to refresh: ${result.message}"))
                }
                else -> {
                    updateState { copy(isRefreshing = false) }
                }
            }
        }
    }

    private fun dismissAlert(alertId: String) {
        // Remove alert from list (client-side only for now)
        updateState {
            copy(
                alerts = alerts.filter { it.id != alertId }
            )
        }
    }
}

