package com.indusjs.fleet.presentation.reports

import co.touchlab.kermit.Logger
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.util.currentTimeMillis
import com.indusjs.fleet.domain.entity.reports.CostBreakdownItem
import com.indusjs.fleet.domain.repository.reports.ReportsRepository
import com.indusjs.fleet.presentation.reports.ReportsContract.Effect
import com.indusjs.fleet.presentation.reports.ReportsContract.Intent
import com.indusjs.fleet.presentation.reports.ReportsContract.State
import dev.zacsweers.metro.Inject
import kotlinx.datetime.*

/**
 * ViewModel for Reports Hub Screen
 */
@Inject
class ReportsViewModel(
    private val reportsRepository: ReportsRepository
) : MviViewModel<State, Intent, Effect>(State()) {

    private val log = Logger.withTag("ReportsViewModel")

    init {
        log.d { "ReportsViewModel initialized, sending LoadSummary intent" }
        sendIntent(Intent.LoadSummary)
    }

    override suspend fun handleIntent(intent: Intent) {
        log.d { "handleIntent: $intent" }
        when (intent) {
            is Intent.LoadSummary -> loadSummary()
            is Intent.Refresh -> loadSummary()
            is Intent.SelectPeriod -> {
                log.d { "SelectPeriod: ${intent.period.label} (${intent.period.value})" }
                handlePeriodSelection(intent.period)
            }
            is Intent.SetCustomDateRange -> handleCustomDateRange(intent.startDate, intent.endDate)
            is Intent.ShowDateRangePicker -> updateState { copy(showDateRangePicker = true) }
            is Intent.HideDateRangePicker -> updateState { copy(showDateRangePicker = false) }
            is Intent.UpdateStartDate -> updateState { copy(startDate = intent.date) }
            is Intent.UpdateEndDate -> updateState { copy(endDate = intent.date) }
            is Intent.NavigateToVehiclePL -> sendEffect(Effect.NavigateToVehiclePL)
            is Intent.NavigateToTripPL -> sendEffect(Effect.NavigateToTripPL)
            is Intent.NavigateToCostAnalysis -> sendEffect(Effect.NavigateToCostAnalysis)
            is Intent.NavigateToConsolidatedPL -> sendEffect(Effect.NavigateToConsolidatedPL)
            is Intent.ExportToPdf -> exportToPdf()
            is Intent.DismissExportDialog -> updateState { copy(exportSuccess = false, exportedFilePath = null) }
        }
    }

    private suspend fun handlePeriodSelection(period: ReportPeriod) {
        log.d { "handlePeriodSelection: period=${period.label}" }
        if (period == ReportPeriod.CUSTOM) {
            log.d { "Custom period selected, showing date picker" }
            updateState { copy(selectedPeriod = period, showDateRangePicker = true) }
        } else {
            // Calculate date range for the selected period
            val (startDate, endDate) = calculateDateRangeForPeriod(period)
            log.d { "Calculated date range for ${period.label}: $startDate to $endDate" }
            updateState {
                copy(selectedPeriod = period, startDate = startDate, endDate = endDate)
            }
            log.d { "State updated, now calling loadSummary with dates: $startDate to $endDate" }
            // Pass calculated dates directly to avoid stale state
            loadSummary(startDateOverride = startDate, endDateOverride = endDate)
        }
    }

    private suspend fun handleCustomDateRange(startDate: String, endDate: String) {
        log.d { "handleCustomDateRange: $startDate to $endDate" }
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
     */
    private fun calculateDateRangeForPeriod(period: ReportPeriod): Pair<String, String> {
        val nowMs = currentTimeMillis()
        log.d { "Current time millis: $nowMs" }
        val today = Instant.fromEpochMilliseconds(nowMs)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        log.d { "Today's date: $today" }

        val result = when (period) {
            ReportPeriod.TODAY -> {
                val dateStr = today.toString() // YYYY-MM-DD
                dateStr to dateStr
            }
            ReportPeriod.WEEKLY -> {
                // Start of current week (Monday) to today
                val dayOfWeek = today.dayOfWeek.isoDayNumber // Monday = 1, Sunday = 7
                log.d { "Day of week: $dayOfWeek (${today.dayOfWeek})" }
                val startOfWeek = today.minus(DatePeriod(days = dayOfWeek - 1))
                startOfWeek.toString() to today.toString()
            }
            ReportPeriod.MONTHLY -> {
                // Start of current month to today
                val startOfMonth = LocalDate(today.year, today.month, 1)
                startOfMonth.toString() to today.toString()
            }
            ReportPeriod.YEARLY -> {
                // Start of current year to today
                val startOfYear = LocalDate(today.year, Month.JANUARY, 1)
                startOfYear.toString() to today.toString()
            }
            ReportPeriod.CUSTOM -> {
                // Custom dates handled separately
                "" to ""
            }
        }
        log.d { "calculateDateRangeForPeriod(${period.label}) = ${result.first} to ${result.second}" }
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
        log.d { "loadSummary called: startDateOverride=$startDateOverride, endDateOverride=$endDateOverride" }
        log.d { "Current state: selectedPeriod=${currentState.selectedPeriod.label}, startDate=${currentState.startDate}, endDate=${currentState.endDate}" }

        updateState { copy(isLoading = true, error = null) }

        // Use overrides if provided, otherwise calculate from current period
        val (startDate, endDate) = if (!startDateOverride.isNullOrBlank() && !endDateOverride.isNullOrBlank()) {
            log.d { "Using override dates: $startDateOverride to $endDateOverride" }
            startDateOverride to endDateOverride
        } else if (currentState.startDate.isNotBlank() && currentState.endDate.isNotBlank()) {
            log.d { "Using state dates: ${currentState.startDate} to ${currentState.endDate}" }
            currentState.startDate to currentState.endDate
        } else {
            log.d { "Calculating dates from period: ${currentState.selectedPeriod.label}" }
            // Calculate based on current period
            calculateDateRangeForPeriod(currentState.selectedPeriod)
        }

        log.d { "=== CALLING API ===" }
        log.d { "API Request: startDate=$startDate, endDate=$endDate" }

        // Call API with start_date and end_date only (no period param)
        when (val result = reportsRepository.getPLSummary(startDate, endDate)) {
            is Result.Success -> {
                val summary = result.data
                log.d { "=== API SUCCESS ===" }
                log.d { "Summary received: totalRevenue=${summary.totalRevenue}, totalExpenses=${summary.totalExpenses}" }
                log.d { "Summary period: startDate=${summary.startDate}, endDate=${summary.endDate}" }
                log.d { "Summary stats: totalTrips=${summary.totalTrips}, completedTrips=${summary.completedTrips}" }
                log.d { "Summary profit: grossProfit=${summary.grossProfit}, margin=${summary.profitMarginPercentage}%" }
                log.d { "Expense breakdown count: ${summary.expenseBreakdown.size}" }

                // Convert expense breakdown to CostBreakdownItem for pie chart
                val breakdown = summary.expenseBreakdown.map { item ->
                    log.d { "Expense: ${item.type} = ${item.amount} (${item.percentage}%)" }
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
                log.d { "State updated with new summary data" }
            }
            is Result.Error -> {
                log.e { "=== API ERROR ===" }
                log.e { "Failed to load summary: ${result.message}" }
                log.e { "Exception: ${result.exception}" }
                updateState { copy(isLoading = false, error = result.message ?: "Failed to load summary") }
            }
            is Result.Loading -> {
                log.d { "Result.Loading received" }
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
            log.e(e) { "Failed to export PDF: ${e.message}" }
            updateState { copy(isExporting = false) }
            sendEffect(Effect.ShowExportError(e.message ?: "Failed to export PDF"))
        }
    }
}
