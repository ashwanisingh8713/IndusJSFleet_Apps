package com.ijs.reports.presentation

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.reports.TAG_REPORTS_VM
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.util.convertToEpochMillis
import com.indusjs.fleet.core.util.currentTimeMillis
import com.ijs.reports.domain.entity.CostBreakdownItem
import com.ijs.reports.domain.usecase.GetPLSummaryUseCase
import com.indusjs.pdfreport.model.CostBreakdownPdfItem
import com.indusjs.pdfreport.model.FleetProfitLossPdfData
import com.indusjs.uicomponents.components.UiText
import com.ijs.reports.presentation.ReportsContract.Effect
import com.ijs.reports.presentation.ReportsContract.Intent
import com.ijs.reports.presentation.ReportsContract.State
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_failed_load_summary
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_no_data_export
import kotlinx.datetime.*

private const val MILLIS_PER_DAY = 86_400_000L

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
            is Intent.NavigateToCustomerPL -> sendEffect(Effect.NavigateToCustomerPL)
            // Navigation - Cost Analysis Reports
            is Intent.NavigateToCostAnalysis -> sendEffect(Effect.NavigateToCostAnalysis)
            is Intent.NavigateToMaintenanceCostReport -> sendEffect(Effect.NavigateToMaintenanceCostReport)
            is Intent.NavigateToTripCostReport -> sendEffect(Effect.NavigateToTripCostReport)
            is Intent.NavigateToDriverCostReport -> sendEffect(Effect.NavigateToDriverCostReport)
            // Navigation - Combined Reports
            is Intent.NavigateToCombinedReport -> sendEffect(Effect.NavigateToCombinedReport)
            // Export
            is Intent.ExportToPdf -> exportToPdf()
            is Intent.DismissExportDialog -> updateState {
                copy(exportSuccess = false, exportedFilePath = null, pdfExportData = null, isExporting = false)
            }
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
        logger.d(TAG_REPORTS_VM, "handleCustomDateRange (DD-MM-YYYY input): $startDate to $endDate")
        // Convert DD-MM-YYYY from picker to YYYY-MM-DD for API
        val apiStartDate = convertDdMmYyyyToYyyyMmDd(startDate) ?: startDate
        val apiEndDate = convertDdMmYyyyToYyyyMmDd(endDate) ?: endDate
        logger.d(TAG_REPORTS_VM, "handleCustomDateRange (YYYY-MM-DD for API): $apiStartDate to $apiEndDate")
        updateState {
            copy(
                startDate = apiStartDate,
                endDate = apiEndDate,
                showDateRangePicker = false,
                selectedPeriod = ReportPeriod.CUSTOM
            )
        }
        loadSummary(startDateOverride = apiStartDate, endDateOverride = apiEndDate)
    }

    /**
     * Converts DD-MM-YYYY → YYYY-MM-DD for the API.
     * Returns null if the input is not in DD-MM-YYYY format.
     */
    private fun convertDdMmYyyyToYyyyMmDd(date: String): String? {
        val parts = date.split("-")
        if (parts.size != 3) return null
        // DD-MM-YYYY: parts[0]=day(2), parts[1]=month(2), parts[2]=year(4)
        return if (parts[0].length == 2 && parts[2].length == 4) {
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } else null // Already in YYYY-MM-DD or unknown format
    }

    /**
     * Converts YYYY-MM-DD → DD-MM-YYYY for UI display/picker.
     */
    private fun convertYyyyMmDdToDdMmYyyy(date: String): String {
        val parts = date.split("-")
        if (parts.size != 3) return date
        // YYYY-MM-DD: parts[0]=year(4), parts[1]=month(2), parts[2]=day(2)
        return if (parts[0].length == 4) {
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } else date // Already in DD-MM-YYYY or unknown format
    }

    /**
     * Converts an internal YYYY-MM-DD range string to UTC epoch millis for the
     * request boundary. Returns null when blank/invalid.
     */
    private fun rangeStringToEpochMillis(date: String): Long? {
        if (date.isBlank()) return null
        return convertToEpochMillis(convertYyyyMmDdToDdMmYyyy(date))
    }

    /**
     * Calculate date range based on the selected period.
     * Returns Pair(startDate, endDate) in YYYY-MM-DD format per OpenAPI spec.
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

        // Use YYYY-MM-DD format per OpenAPI spec (Docs/api_modules/openapi.json)
        val todayStr = today.toString()

        val result = when (period) {
            ReportPeriod.TODAY -> {
                todayStr to todayStr
            }
            ReportPeriod.WEEKLY -> {
                val startDate = today.minus(DatePeriod(days = 6))
                startDate.toString() to todayStr
            }
            ReportPeriod.FIFTEEN_DAYS -> {
                val startDate = today.minus(DatePeriod(days = 14))
                startDate.toString() to todayStr
            }
            ReportPeriod.MONTHLY -> {
                val startOfMonth = LocalDate(today.year, today.month, 1)
                startOfMonth.toString() to todayStr
            }
            ReportPeriod.QUARTERLY -> {
                val quarterStart = when (today.monthNumber) {
                    in 4..6 -> LocalDate(today.year, Month.APRIL, 1)
                    in 7..9 -> LocalDate(today.year, Month.JULY, 1)
                    in 10..12 -> LocalDate(today.year, Month.OCTOBER, 1)
                    else -> LocalDate(today.year - 1, Month.JANUARY, 1)
                }
                quarterStart.toString() to todayStr
            }
            ReportPeriod.HALF_YEARLY -> {
                val startDate = today.minus(DatePeriod(months = 6))
                startDate.toString() to todayStr
            }
            ReportPeriod.YEARLY -> {
                val fyStart = if (today.monthNumber >= 4) {
                    LocalDate(today.year, Month.APRIL, 1)
                } else {
                    LocalDate(today.year - 1, Month.APRIL, 1)
                }
                fyStart.toString() to todayStr
            }
            ReportPeriod.CUSTOM -> {
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

        // Call API with start_date and end_date only (no period param).
        // Internal range strings are YYYY-MM-DD; convert to epoch millis at the boundary.
        // start_date = start-of-day; end_date = INCLUSIVE end-of-day (start-of-day + 23:59:59.999).
        // Without the end-of-day adjustment "Today" becomes a zero-width window and "This Month"
        // ends at start-of-today, excluding everything that happened today (e.g. a trip completed
        // today shows Revenue/Expenses ₹0).
        val startMs = rangeStringToEpochMillis(startDate)
        val endMs = rangeStringToEpochMillis(endDate)?.let { it + MILLIS_PER_DAY - 1 }
        when (val result = getPLSummaryUseCase(startMs, endMs)) {
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
                updateState { copy(isLoading = false, error = result.message?.let { UiText.Raw(it) } ?: UiText.StringRes(Res.string.report_failed_load_summary)) }
            }
            is Result.Loading -> {
                logger.d(TAG_REPORTS_VM, "Result.Loading received")
            }
        }
    }

    private fun exportToPdf() {
        val s = state.value
        val summary = s.summary ?: run {
            sendEffect(Effect.ShowExportError(UiText.StringRes(Res.string.report_no_data_export)))
            return
        }
        val now = Instant.fromEpochMilliseconds(currentTimeMillis())
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val dateRange = if (s.startDate.isNotBlank() && s.endDate.isNotBlank()) {
            "${s.startDate} to ${s.endDate}"
        } else {
            s.selectedPeriod.label
        }
        // Build the real PDF model from the loaded summary. The summary screen has no
        // per-vehicle rows, so `vehicles` is empty — the report shows fleet totals + the
        // expense breakdown. Setting pdfExportData drives FleetProfitLossPdfHandler in the
        // screen, which generates the PDF and shows the open/share dialog.
        val pdfData = FleetProfitLossPdfData(
            dateRange = dateRange,
            periodType = s.selectedPeriod.value,
            totalVehicles = summary.totalVehicles,
            profitableVehicles = summary.profitableVehicles,
            lossMakingVehicles = summary.lossMakingVehicles,
            totalRevenue = summary.totalRevenue,
            totalExpenses = summary.totalExpenses,
            netProfitLoss = summary.netProfit,
            profitMargin = summary.profitMarginPercentage,
            vehicles = emptyList(),
            costBreakdown = summary.expenseBreakdown.map { item ->
                CostBreakdownPdfItem(
                    costId = item.type,
                    costLabel = item.type,
                    amount = item.amount,
                    count = 0,
                    percentage = item.percentage
                )
            },
            generatedAt = now.date.toString()
        )
        updateState { copy(isExporting = true, pdfExportData = pdfData) }
    }
}
