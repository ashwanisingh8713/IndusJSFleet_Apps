package com.ijs.reports.presentation.customer

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText
import com.ijs.reports.domain.entity.CustomerPLReport

/**
 * MVI contract for the P&L-by-customer report.
 */
object CustomerPLContract {

    data class State(
        val isLoading: Boolean = false,
        val error: UiText? = null,
        val selectedPeriod: String = "monthly",
        val report: CustomerPLReport? = null
    ) : UiState {
        val hasData: Boolean get() = report?.customers?.isNotEmpty() == true
    }

    /** Periods accepted by GET /reports/profit-loss/customers. */
    val PERIODS = listOf("daily", "weekly", "monthly", "quarterly", "yearly")

    sealed interface Intent : UiIntent {
        data object Load : Intent
        data object Refresh : Intent
        data class SelectPeriod(val period: String) : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: UiText) : Effect
    }
}
