package com.ijs.reports.presentation

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.ijs.reports.domain.entity.CostBreakdownItem
import com.ijs.reports.domain.entity.PLSummary
// ReportPeriod and ProfitStatus are now in ReportEnums.kt (same package)

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

        // Calculate profit status from summary margin
        val profitStatus: ProfitStatus
            get() = summary?.let {
                ProfitStatus.fromMargin(it.profitMarginPercentage)
            } ?: ProfitStatus.BREAK_EVEN
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
        // Navigation - P&L Reports
        data object NavigateToVehiclePL : Intent
        data object NavigateToTripPL : Intent
        data object NavigateToConsolidatedPL : Intent
        // Navigation - Cost Analysis Reports
        data object NavigateToCostAnalysis : Intent
        data object NavigateToMaintenanceCostReport : Intent
        data object NavigateToTripCostReport : Intent
        data object NavigateToDriverCostReport : Intent
        // Navigation - Combined Reports
        data object NavigateToCombinedReport : Intent
        // Export
        data object ExportToPdf : Intent
        data object DismissExportDialog : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        // Navigation effects - P&L Reports
        data object NavigateToVehiclePL : Effect
        data object NavigateToTripPL : Effect
        data object NavigateToConsolidatedPL : Effect
        // Navigation effects - Cost Analysis Reports
        data object NavigateToCostAnalysis : Effect
        data object NavigateToMaintenanceCostReport : Effect
        data object NavigateToTripCostReport : Effect
        data object NavigateToDriverCostReport : Effect
        // Navigation effects - Combined Reports
        data object NavigateToCombinedReport : Effect
        // Export
        data class ShowExportSuccess(val filePath: String) : Effect
        data class ShowExportError(val message: String) : Effect
    }
}
