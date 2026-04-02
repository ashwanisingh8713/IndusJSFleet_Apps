package com.ijs.reports.presentation

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.reports.TAG_REPORTS_VM
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.util.currentTimeMillis
import com.ijs.reports.domain.entity.CostBreakdownItem
import com.ijs.reports.domain.usecase.GetPLSummaryUseCase
import com.ijs.reports.presentation.ReportsContract.Effect
import com.ijs.reports.presentation.ReportsContract.Intent
import com.ijs.reports.presentation.ReportsContract.State
import dev.zacsweers.metro.Inject
import kotlinx.datetime.*

/**
 * ViewModel for Reports Hub Screen
 */
@Inject
class ReportsViewModel(
    private val getPLSummaryUseCase: GetPLSummaryUseCase,
    private val logger: FleetLogger
) : MviViewModel<State, Intent, Effect>(State()) {
init {
        logger.d(TAG_REPORTS_VM, "ReportsViewModel initialized, sending LoadSummary intent")
        sendIntent(Intent.LoadSummary)
    }

    override suspend fun handleIntent(intent: Intent) {
        logger.d(TAG_REPORTS_VM, "handleIntent: $intent")
        when (intent) {
            is Intent.LoadSummary -> loadSummary()
            is Intent.Refresh -> loadSummary()
            is Intent.SelectPeriod -> {
                logger.d(TAG_REPORTS_VM, "SelectPeriod: ${intent.period.label} (${intent.period.value}")
                handlePeriodSelection(intent.period)
            }
            is Intent.SetCustomDateRange -> handleCustomDateRange(intent.startDate, intent.endDate)
            is Intent.ShowDateRangePicker -> updateState { copy(showDateRangePicker = true) }
            is Intent.HideDateRangePicker -> updateState { copy(showDateRangePicker = false) }
            // Navigation - P&L Reports
            is Intent.NavigateToVehiclePL -> sendEffect(Effect.NavigateToVehiclePL)
            is Intent.NavigateToTripPL -> sendEffect(Effect.NavigateToTripPL)
            is Intent.NavigateToConsolidatedPL -> sendEffect(Effect.NavigateToConsolidatedPL)
            // Navigation - Cost Analysis Reports
            is Intent.NavigateToCostAnalysis -> sendEffect(Effect.NavigateToCostAnalysis)
            is Intent.NavigateToMaintenanceCostReport -> sendEffect(Effect.NavigateToMaintenanceCostReport)
            is Intent.NavigateToTripCostReport -> sendEffect(Effect.NavigateToTripCostReport)
            is Intent.NavigateToDriverCostReport -> sendEffect(Effect.NavigateToDriverCostReport)
            // Navigation - Combined Reports
            is Intent.NavigateToCombinedReport -> sendEffect(Effect.NavigateToCombinedReport)
            // Export
            is Intent.ExportToPdf -> exportToPdf()
            is Intent.DismissExportDialog -> updateState { copy(exportSuccess = false, exportedFilePath = null) }
        }
    }

    private suspend fun handlePeriodSelection(period: ReportPeriod) {
        logger.d(TAG_REPORTS_VM, "handlePeriodSelection: period=${period.label}")
        if (period == ReportPeriod.CUSTOM) {
            logger.d(TAG_REPORTS_VM, "Custom period selected, showing date picker")
            updateState { copy(selectedPeriod = period, showDateRangePicker = true) }
        } else {
            // Calculate date range for the selected period
            val (startDate, endDate) = calculateDateRangeForPeriod(period)
            logger.d(TAG_REPORTS_VM, "Calculated date range for ${period.label}: $startDate to $endDate")
            updateState {
                copy(selectedPeriod = period, startDate = startDate, endDate = endDate)
            }
            logger.d(TAG_REPORTS_VM, "State updated, now calling loadSummary with dates: $startDate to $endDate")
            // Pass calculated dates directly to avoid stale state
            loadSummary(startDateOverride = startDate, endDateOverride = endDate)
        }
    }

    private suspend fun handleCustomDateRange(startDate: String, endDate: String) {
        logger.d(TAG_REPORTS_VM, "handleCustomDateRange: $startDate to $endDate")
        updateState {
            copy(
                startDate = startDate,
                endDate = endDate,
                showDateRangePicker = false,
                selectedPeriod = ReportPeriod.CUSTOM
            )
        }
        loadSummary(startDateOverride = startDate, endDateOverride = endDate)
    }

    /**
     * Calculate date range based on the selected period.
     * Returns Pair(startDate, endDate) in YYYY-MM-DD format for API.
     * The P&L Summary API only accepts start_date and end_date, NOT period parameter.
     *
     * As per report-pl-screen.prompt.md Date Range Periods:
     * - Daily: Current date only
     * - Weekly: Last 7 days (rolling)
     * - 15 Days: Last 15 days (rolling)
     * - Monthly: Current calendar month
     * - Quarterly: Current quarter (Q1: Apr-Jun, Q2: Jul-Sep, Q3: Oct-Dec, Q4: Jan-Mar)
     * - Half Yearly: Last 6 months (rolling)
     * - Yearly: Financial year (Apr 1 - Mar 31)
     */
    private fun calculateDateRangeForPeriod(period: ReportPeriod): Pair<String, String> {
        val nowMs = currentTimeMillis()
        logger.d(TAG_REPORTS_VM, "Current time millis: $nowMs")
        val today = Instant.fromEpochMilliseconds(nowMs)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        logger.d(TAG_REPORTS_VM, "Today's date: $today")

        val result = when (period) {
            ReportPeriod.TODAY -> {
                val dateStr = today.toString() // YYYY-MM-DD
                dateStr to dateStr
            }
            ReportPeriod.WEEKLY -> {
                // Last 7 days (rolling)
                val startDate = today.minus(DatePeriod(days = 6))
                startDate.toString() to today.toString()
            }
            ReportPeriod.FIFTEEN_DAYS -> {
                // Last 15 days (rolling)
                val startDate = today.minus(DatePeriod(days = 14))
                startDate.toString() to today.toString()
            }
            ReportPeriod.MONTHLY -> {
                // Start of current month to today
                val startOfMonth = LocalDate(today.year, today.month, 1)
                startOfMonth.toString() to today.toString()
            }
            ReportPeriod.QUARTERLY -> {
                // Current quarter (Indian financial quarters)
                // Q1: Apr-Jun, Q2: Jul-Sep, Q3: Oct-Dec, Q4: Jan-Mar
                val quarterStart = when (today.monthNumber) {
                    in 4..6 -> LocalDate(today.year, Month.APRIL, 1)      // Q1
                    in 7..9 -> LocalDate(today.year, Month.JULY, 1)       // Q2
                    in 10..12 -> LocalDate(today.year, Month.OCTOBER, 1) // Q3
                    else -> LocalDate(today.year - 1, Month.JANUARY, 1)  // Q4 (Jan-Mar of previous FY)
                }
                quarterStart.toString() to today.toString()
            }
            ReportPeriod.HALF_YEARLY -> {
                // Last 6 months (rolling)
                val startDate = today.minus(DatePeriod(months = 6))
                startDate.toString() to today.toString()
            }
            ReportPeriod.YEARLY -> {
                // Financial year (April 1 to March 31)
                val fyStart = if (today.monthNumber >= 4) {
                    LocalDate(today.year, Month.APRIL, 1)
                } else {
                    LocalDate(today.year - 1, Month.APRIL, 1)
                }
                fyStart.toString() to today.toString()
            }
            ReportPeriod.CUSTOM -> {
                // Custom dates handled separately
                "" to ""
            }
        }
        logger.d(TAG_REPORTS_VM, "calculateDateRangeForPeriod(${period.label}) = ${result.first} to ${result.second}")
        return result
    }

    /**
     * Load P&L summary with date range.
     * The API uses start_date and end_date for filtering (period param not supported).
     */
    private suspend fun loadSummary(
        startDateOverride: String? = null,
        endDateOverride: String? = null
    ) {
        val currentState = state.value
        logger.d(TAG_REPORTS_VM, "loadSummary called: startDateOverride=$startDateOverride, endDateOverride=$endDateOverride")
        logger.d(TAG_REPORTS_VM, "Current state: selectedPeriod=${currentState.selectedPeriod.label}, startDate=${currentState.startDate}, endDate=${currentState.endDate}")

        updateState { copy(isLoading = true, error = null) }

        // Use overrides if provided, otherwise calculate from current period
        val (startDate, endDate) = if (!startDateOverride.isNullOrBlank() && !endDateOverride.isNullOrBlank()) {
            logger.d(TAG_REPORTS_VM, "Using override dates: $startDateOverride to $endDateOverride")
            startDateOverride to endDateOverride
        } else if (currentState.startDate.isNotBlank() && currentState.endDate.isNotBlank()) {
            logger.d(TAG_REPORTS_VM, "Using state dates: ${currentState.startDate} to ${currentState.endDate}")
            currentState.startDate to currentState.endDate
        } else {
            logger.d(TAG_REPORTS_VM, "Calculating dates from period: ${currentState.selectedPeriod.label}")
            // Calculate based on current period
            calculateDateRangeForPeriod(currentState.selectedPeriod)
        }

        logger.d(TAG_REPORTS_VM, "=== CALLING API ===")
        logger.d(TAG_REPORTS_VM, "API Request: startDate=$startDate, endDate=$endDate")

        // Call API with start_date and end_date only (no period param)
        when (val result = getPLSummaryUseCase(startDate, endDate)) {
            is Result.Success -> {
                val summary = result.data
                logger.d(TAG_REPORTS_VM, "=== API SUCCESS ===")
                logger.d(TAG_REPORTS_VM, "Summary received: totalRevenue=${summary.totalRevenue}, totalExpenses=${summary.totalExpenses}")
                logger.d(TAG_REPORTS_VM, "Summary period: startDate=${summary.startDate}, endDate=${summary.endDate}")
                logger.d(TAG_REPORTS_VM, "Summary stats: totalTrips=${summary.totalTrips}, completedTrips=${summary.completedTrips}")
                logger.d(TAG_REPORTS_VM, "Summary profit: grossProfit=${summary.grossProfit}, margin=${summary.profitMarginPercentage}%")
                logger.d(TAG_REPORTS_VM, "Expense breakdown count: ${summary.expenseBreakdown.size}")

                // Convert expense breakdown to CostBreakdownItem for pie chart
                val breakdown = summary.expenseBreakdown.map { item ->
                    logger.d(TAG_REPORTS_VM, "Expense: ${item.type} = ${item.amount} (${item.percentage}%")
                    CostBreakdownItem(
                        costType = item.type,
                        amount = item.amount,
                        percentage = item.percentage
                    )
                }
                updateState {
                    copy(
                        isLoading = false,
                        summary = summary,
                        expenseBreakdown = breakdown
                    )
                }
                logger.d(TAG_REPORTS_VM, "State updated with new summary data")
            }
            is Result.Error -> {
                logger.e(TAG_REPORTS_VM, "=== API ERROR ===")
                logger.e(TAG_REPORTS_VM, "Failed to load summary: ${result.message}")
                logger.e(TAG_REPORTS_VM, "Exception: ${result.exception}")
                updateState { copy(isLoading = false, error = result.message ?: "Failed to load summary") }
            }
            is Result.Loading -> {
                logger.d(TAG_REPORTS_VM, "Result.Loading received")
            }
        }
    }

    private suspend fun exportToPdf() {
        val summary = state.value.summary ?: run {
            sendEffect(Effect.ShowExportError("No data to export"))
            return
        }

        updateState { copy(isExporting = true) }

        try {
            updateState {
                copy(
                    isExporting = false,
                    exportSuccess = true,
                    exportedFilePath = "/sdcard/IndusJSFleet/reports/report.pdf"
                )
            }
            sendEffect(Effect.ShowExportSuccess("/sdcard/IndusJSFleet/reports/report.pdf"))
        } catch (e: Exception) {
            logger.e(TAG_REPORTS_VM, "Failed to export PDF: ${e.message}", e)
            updateState { copy(isExporting = false) }
            sendEffect(Effect.ShowExportError(e.message ?: "Failed to export PDF"))
        }
    }
}
