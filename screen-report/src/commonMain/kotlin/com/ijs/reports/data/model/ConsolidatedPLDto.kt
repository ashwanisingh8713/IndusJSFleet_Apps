package com.ijs.reports.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs for Consolidated P&L and Cost Type Analysis APIs.
 * Split from ProfitLossDto.kt for 500-line compliance.
 */

/**
 * Single cost-type analysis response wrapper.
 * GET /reports/profit-loss/cost-type/{type}
 *
 * Mirrors backend application/report.CostTypePLResult =
 * { period: PeriodInfo, cost_type_analysis: CostTypeProfitLoss }.
 */
@Serializable
data class CostTypePLResponseDto(
    @SerialName("period")
    val period: PeriodDto? = null,
    @SerialName("cost_type_analysis")
    val costTypeAnalysis: CostTypeAnalysisDto? = null
)

/**
 * Multi cost-type analysis response wrapper.
 * POST /reports/profit-loss/cost-types
 *
 * Mirrors backend domain.CostTypeProfitLossResponse =
 * { period: PeriodInfo, cost_types: []CostTypeProfitLoss, summary: CostTypesSummary }.
 */
@Serializable
data class MultiCostTypePLResponseDto(
    @SerialName("period")
    val period: PeriodDto? = null,
    @SerialName("cost_types")
    val costTypes: List<CostTypeAnalysisDto> = emptyList(),
    @SerialName("summary")
    val summary: CostTypesSummaryDto? = null
)

/**
 * Aggregated summary for multi cost-type analysis (backend domain.CostTypesSummary).
 */
@Serializable
data class CostTypesSummaryDto(
    @SerialName("total_expenses")
    val totalExpenses: Double = 0.0,
    @SerialName("total_transactions")
    val totalTransactions: Int = 0,
    @SerialName("highest_cost_type")
    val highestCostType: String? = null,
    @SerialName("highest_cost_amount")
    val highestCostAmount: Double = 0.0
)

/**
 * Cost Type Analysis DTO.
 *
 * Mirrors backend domain.CostTypeProfitLoss: cost_id, cost_label, cost_type
 * (deprecated/omitempty), total_amount, transaction_count, percentage_of_total,
 * by_vehicle[], by_month[], statistics{}.
 */
@Serializable
data class CostTypeAnalysisDto(
    @SerialName("cost_id")
    val costId: String? = null,
    @SerialName("cost_label")
    val costLabel: String? = null,
    // Deprecated on backend (omitempty); defensive fallback only.
    @SerialName("cost_type")
    val costType: String? = null,
    @SerialName("total_amount")
    val totalAmount: Double = 0.0,
    @SerialName("transaction_count")
    val transactionCount: Int = 0,
    @SerialName("percentage_of_total")
    val percentageOfTotal: Double = 0.0,
    @SerialName("by_vehicle")
    val byVehicle: List<VehicleCostBreakdownDto>? = null,
    @SerialName("by_month")
    val byMonth: List<MonthlyTrendDto>? = null,
    @SerialName("statistics")
    val statistics: CostStatisticsDto? = null
) {
    /** Display label - prefers cost_label, then cost_id, then cost_type. */
    val displayLabel: String
        get() = costLabel ?: costId ?: costType ?: ""
}

/**
 * Per-vehicle cost breakdown (backend domain.VehicleCostBreakdown).
 */
@Serializable
data class VehicleCostBreakdownDto(
    @SerialName("vehicle_id")
    val vehicleId: Int = 0,
    @SerialName("registration_number")
    val registrationNumber: String? = null,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("count")
    val count: Int = 0
)

/**
 * Per-month cost breakdown (backend domain.MonthlyCostBreakdown).
 * month is "YYYY-MM".
 */
@Serializable
data class MonthlyTrendDto(
    @SerialName("month")
    val month: String? = null,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("count")
    val count: Int = 0
)

/**
 * Statistical analysis of a cost type (backend domain.CostStatistics).
 */
@Serializable
data class CostStatisticsDto(
    @SerialName("average_per_trip")
    val averagePerTrip: Double = 0.0,
    @SerialName("average_per_vehicle")
    val averagePerVehicle: Double = 0.0,
    @SerialName("highest_single_expense")
    val highestSingleExpense: Double = 0.0,
    @SerialName("lowest_single_expense")
    val lowestSingleExpense: Double = 0.0
)

/**
 * Consolidated P&L Report DTO
 * POST /reports/profit-loss/consolidated
 *
 * Mirrors backend domain.ConsolidatedProfitLossResponse (DEEPLY NESTED):
 * { period, filters_applied, revenue, expenses, profit_loss, trends }.
 * The previous flat shape matched none of these keys, so the screen blanked.
 */
@Serializable
data class ConsolidatedPLDto(
    @SerialName("period")
    val period: PeriodDto? = null,
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("filters_applied")
    val filtersApplied: FiltersAppliedDto? = null,
    @SerialName("revenue")
    val revenue: RevenueBreakdownDto? = null,
    @SerialName("expenses")
    val expenses: ExpenseBreakdownDto? = null,
    @SerialName("profit_loss")
    val profitLoss: ConsolidatedProfitLossDto? = null,
    @SerialName("trends")
    val trends: TrendAnalysisDto? = null
)

/**
 * Filters applied to the consolidated report (backend domain.FiltersApplied).
 */
@Serializable
data class FiltersAppliedDto(
    @SerialName("vehicles")
    val vehicles: Int = 0,
    @SerialName("trips")
    val trips: Int = 0,
    @SerialName("cost_ids")
    val costIds: Int = 0
)

/**
 * Revenue breakdown section (backend domain.RevenueBreakdown).
 */
@Serializable
data class RevenueBreakdownDto(
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("by_vehicle")
    val byVehicle: List<VehicleRevenueDto>? = null,
    @SerialName("by_period")
    val byPeriod: List<PeriodRevenueDto>? = null
)

@Serializable
data class VehicleRevenueDto(
    @SerialName("vehicle_id")
    val vehicleId: Int = 0,
    @SerialName("registration_number")
    val registrationNumber: String? = null,
    @SerialName("revenue")
    val revenue: Double = 0.0
)

@Serializable
data class PeriodRevenueDto(
    @SerialName("period")
    val period: String? = null,
    @SerialName("revenue")
    val revenue: Double = 0.0
)

/**
 * Expense breakdown section (backend domain.ExpenseBreakdown).
 */
@Serializable
data class ExpenseBreakdownDto(
    @SerialName("total_expenses")
    val totalExpenses: Double = 0.0,
    @SerialName("by_cost_type")
    val byCostType: Map<String, Double>? = null,
    @SerialName("by_vehicle")
    val byVehicle: List<VehicleExpenseDto>? = null,
    @SerialName("by_period")
    val byPeriod: List<PeriodExpenseDto>? = null
)

@Serializable
data class VehicleExpenseDto(
    @SerialName("vehicle_id")
    val vehicleId: Int = 0,
    @SerialName("registration_number")
    val registrationNumber: String? = null,
    @SerialName("expenses")
    val expenses: Double = 0.0
)

@Serializable
data class PeriodExpenseDto(
    @SerialName("period")
    val period: String? = null,
    @SerialName("expenses")
    val expenses: Double = 0.0
)

/**
 * Overall profit/loss section (backend domain.ConsolidatedProfitLoss).
 */
@Serializable
data class ConsolidatedProfitLossDto(
    @SerialName("gross_profit")
    val grossProfit: Double = 0.0,
    @SerialName("net_profit")
    val netProfit: Double = 0.0,
    @SerialName("profit_margin_percentage")
    val profitMarginPercentage: Double = 0.0,
    @SerialName("status")
    val status: String? = null,
    @SerialName("by_vehicle")
    val byVehicle: List<VehicleProfitLossDto>? = null,
    @SerialName("by_period")
    val byPeriod: List<PeriodProfitLossDto>? = null
)

/**
 * Per-period P&L row (backend domain.PeriodProfitLoss).
 */
@Serializable
data class PeriodProfitLossDto(
    @SerialName("period")
    val period: String? = null,
    @SerialName("revenue")
    val revenue: Double = 0.0,
    @SerialName("expenses")
    val expenses: Double = 0.0,
    @SerialName("profit_loss")
    val profitLoss: Double = 0.0,
    @SerialName("margin")
    val margin: Double = 0.0,
    @SerialName("status")
    val status: String? = null
)

/**
 * Trend analysis section (backend domain.TrendAnalysis).
 */
@Serializable
data class TrendAnalysisDto(
    @SerialName("revenue_trend")
    val revenueTrend: String? = null,
    @SerialName("expense_trend")
    val expenseTrend: String? = null,
    @SerialName("profit_trend")
    val profitTrend: String? = null
)

