package com.ijs.reports.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs for Consolidated P&L and Cost Type Analysis APIs.
 * Split from ProfitLossDto.kt for 500-line compliance.
 */

/**
 * Cost Type Analysis DTO
 * GET /reports/profit-loss/cost-type/{type}
 */
@Serializable
data class CostTypeAnalysisDto(
    @SerialName("cost_type")
    val costType: String,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    @SerialName("total_amount")
    val totalAmount: Double = 0.0,
    @SerialName("total_count")
    val totalCount: Int = 0,
    @SerialName("average_per_entry")
    val averagePerEntry: Double = 0.0,
    @SerialName("vehicle_breakdown")
    val vehicleBreakdown: List<VehicleCostBreakdownDto>? = null,
    @SerialName("monthly_trend")
    val monthlyTrend: List<MonthlyTrendDto>? = null
)

/**
 * Vehicle Cost Breakdown DTO
 */
@Serializable
data class VehicleCostBreakdownDto(
    @SerialName("vehicle_id")
    val vehicleId: Int,
    @SerialName("vehicle_number")
    val vehicleNumber: String? = null,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("percentage")
    val percentage: Double = 0.0
)

/**
 * Monthly Trend DTO
 */
@Serializable
data class MonthlyTrendDto(
    @SerialName("month")
    val month: String,
    @SerialName("year")
    val year: Int,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("count")
    val count: Int = 0
)

/**
 * Consolidated P&L Report DTO
 * POST /reports/profit-loss/consolidated
 */
@Serializable
data class ConsolidatedPLDto(
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    @SerialName("group_by")
    val groupBy: String? = null,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_expenses")
    val totalExpenses: Double = 0.0,
    @SerialName("net_profit")
    val netProfit: Double = 0.0,
    @SerialName("profit_margin")
    val profitMargin: Double = 0.0,
    @SerialName("is_profitable")
    val isProfitable: Boolean = false,
    @SerialName("total_vehicles")
    val totalVehicles: Int = 0,
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("vehicle_summary")
    val vehicleSummary: List<VehiclePLSummaryDto>? = null,
    @SerialName("trip_summary")
    val tripSummary: List<TripPLSummaryDto>? = null,
    @SerialName("cost_breakdown")
    val costBreakdown: List<CostBreakdownItemDto>? = null,
    @SerialName("period_breakdown")
    val periodBreakdown: List<PeriodBreakdownDto>? = null
)

/**
 * Vehicle P&L Summary DTO (for consolidated report)
 */
@Serializable
data class VehiclePLSummaryDto(
    @SerialName("vehicle_id")
    val vehicleId: Int,
    @SerialName("vehicle_number")
    val vehicleNumber: String? = null,
    @SerialName("revenue")
    val revenue: Double = 0.0,
    @SerialName("expenses")
    val expenses: Double = 0.0,
    @SerialName("profit")
    val profit: Double = 0.0,
    @SerialName("profit_margin")
    val profitMargin: Double = 0.0,
    @SerialName("is_profitable")
    val isProfitable: Boolean = false,
    @SerialName("trip_count")
    val tripCount: Int = 0
)

/**
 * Trip P&L Summary DTO (for consolidated report)
 */
@Serializable
data class TripPLSummaryDto(
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("vehicle_number")
    val vehicleNumber: String? = null,
    @SerialName("route")
    val route: String? = null,
    @SerialName("scheduled_date")
    val scheduledDate: String? = null,
    @SerialName("revenue")
    val revenue: Double = 0.0,
    @SerialName("expenses")
    val expenses: Double = 0.0,
    @SerialName("profit")
    val profit: Double = 0.0,
    @SerialName("is_profitable")
    val isProfitable: Boolean = false
)

/**
 * Period Breakdown DTO (monthly/weekly grouping)
 */
@Serializable
data class PeriodBreakdownDto(
    @SerialName("period")
    val period: String,
    @SerialName("label")
    val label: String? = null,
    @SerialName("revenue")
    val revenue: Double = 0.0,
    @SerialName("expenses")
    val expenses: Double = 0.0,
    @SerialName("profit")
    val profit: Double = 0.0,
    @SerialName("trip_count")
    val tripCount: Int = 0,
    @SerialName("is_profitable")
    val isProfitable: Boolean = false
)

