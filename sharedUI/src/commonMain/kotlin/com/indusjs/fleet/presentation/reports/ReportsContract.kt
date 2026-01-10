package com.indusjs.fleet.presentation.reports

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.reports.CostBreakdownItem
import com.indusjs.fleet.domain.entity.reports.PLSummary

/**
 * Report period options for filtering.
 */
enum class ReportPeriod(val value: String, val label: String) {
    TODAY("today", "Today"),
    WEEKLY("weekly", "This Week"),
    MONTHLY("monthly", "This Month"),
    YEARLY("yearly", "This Year"),
    CUSTOM("custom", "Custom")
}

/**
 * MVI Contract for Reports Hub Screen
 */
object ReportsContract {

    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val summary: PLSummary? = null,
        // Period selection
        val selectedPeriod: ReportPeriod = ReportPeriod.MONTHLY,
        val startDate: String = "",
        val endDate: String = "",
        val showDateRangePicker: Boolean = false,
        // Expense breakdown for pie chart
        val expenseBreakdown: List<CostBreakdownItem> = emptyList(),
        // Export
        val isExporting: Boolean = false,
        val exportSuccess: Boolean = false,
        val exportedFilePath: String? = null
    ) : UiState {
        val hasSummary: Boolean get() = summary != null
        val hasExpenseData: Boolean get() = expenseBreakdown.isNotEmpty()
    }

    sealed interface Intent : UiIntent {
        data object LoadSummary : Intent
        data object Refresh : Intent
        // Period selection
        data class SelectPeriod(val period: ReportPeriod) : Intent
        data class SetCustomDateRange(val startDate: String, val endDate: String) : Intent
        data object ShowDateRangePicker : Intent
        data object HideDateRangePicker : Intent
        data class UpdateStartDate(val date: String) : Intent
        data class UpdateEndDate(val date: String) : Intent
        // Navigation
        data object NavigateToVehiclePL : Intent
        data object NavigateToTripPL : Intent
        data object NavigateToCostAnalysis : Intent
        data object NavigateToConsolidatedPL : Intent
        // Export
        data object ExportToPdf : Intent
        data object DismissExportDialog : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data object NavigateToVehiclePL : Effect
        data object NavigateToTripPL : Effect
        data object NavigateToCostAnalysis : Effect
        data object NavigateToConsolidatedPL : Effect
        data class ShowExportSuccess(val filePath: String) : Effect
        data class ShowExportError(val message: String) : Effect
    }
}
