package com.indusjs.fleet.presentation.alerts

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.dashboard.Alert
import com.indusjs.fleet.domain.entity.dashboard.AlertsSummary

/**
 * MVI Contract for Alerts List Screen.
 */
object AlertsListContract {

    /**
     * Filter options for alerts list.
     */
    enum class AlertFilter {
        ALL,
        CRITICAL,
        WARNING,
        INFO,
        DOCUMENTS,
        LICENSES
    }

    /**
     * UI State for Alerts List screen.
     */
    data class State(
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val alerts: List<Alert> = emptyList(),
        val alertsSummary: AlertsSummary = AlertsSummary(),
        val selectedFilter: AlertFilter = AlertFilter.ALL,
        val error: String? = null
    ) : UiState {

        val filteredAlerts: List<Alert>
            get() = when (selectedFilter) {
                AlertFilter.ALL -> alerts
                AlertFilter.CRITICAL -> alerts.filter { it.priority == com.indusjs.fleet.domain.entity.dashboard.AlertPriority.CRITICAL }
                AlertFilter.WARNING -> alerts.filter { it.priority == com.indusjs.fleet.domain.entity.dashboard.AlertPriority.WARNING }
                AlertFilter.INFO -> alerts.filter { it.priority == com.indusjs.fleet.domain.entity.dashboard.AlertPriority.INFO }
                AlertFilter.DOCUMENTS -> alerts.filter { it.type == com.indusjs.fleet.domain.entity.dashboard.AlertType.DOCUMENT_EXPIRY }
                AlertFilter.LICENSES -> alerts.filter { it.type == com.indusjs.fleet.domain.entity.dashboard.AlertType.LICENSE_EXPIRY }
            }

        val hasAlerts: Boolean get() = alerts.isNotEmpty()
    }

    /**
     * User intents for Alerts List screen.
     */
    sealed interface Intent : UiIntent {
        data object LoadAlerts : Intent
        data object RefreshAlerts : Intent
        data class ChangeFilter(val filter: AlertFilter) : Intent
        data class DismissAlert(val alertId: String) : Intent
        data object NavigateBack : Intent
        data object ClearError : Intent
    }

    /**
     * Side effects for Alerts List screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data class ShowError(val message: String) : Effect
        data object NavigateBack : Effect
    }
}

