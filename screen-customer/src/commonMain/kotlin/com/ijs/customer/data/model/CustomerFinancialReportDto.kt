package com.ijs.customer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============= Customer Financial Report DTOs =============

/**
 * Financial summary DTO.
 */
@Serializable
data class FinancialSummaryDto(
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_costs")
    val totalCosts: Double = 0.0,
    @SerialName("gross_profit")
    val grossProfit: Double = 0.0,
    @SerialName("profit_margin")
    val profitMargin: Double = 0.0,
    @SerialName("total_received")
    val totalReceived: Double = 0.0,
    @SerialName("total_pending")
    val totalPending: Double = 0.0
)

/**
 * Cost breakdown DTO.
 */
@Serializable
data class CostBreakdownDto(
    @SerialName("fuel_costs")
    val fuelCosts: Double = 0.0,
    @SerialName("toll_costs")
    val tollCosts: Double = 0.0,
    @SerialName("driver_costs")
    val driverCosts: Double = 0.0,
    @SerialName("maintenance_costs")
    val maintenanceCosts: Double = 0.0,
    @SerialName("other_costs")
    val otherCosts: Double = 0.0
)

/**
 * Monthly trend item.
 */
@Serializable
data class MonthlyTrendDto(
    @SerialName("month")
    val month: String,
    @SerialName("trips")
    val trips: Int = 0,
    @SerialName("revenue")
    val revenue: Double = 0.0,
    @SerialName("costs")
    val costs: Double = 0.0,
    @SerialName("profit")
    val profit: Double = 0.0,
    @SerialName("margin")
    val margin: Double = 0.0
)

/**
 * Nested data object for customer financial report response.
 */
@Serializable
data class CustomerFinancialReportDataDto(
    @SerialName("customer")
    val customer: CustomerSummaryDto? = null,
    @SerialName("summary")
    val summary: FinancialSummaryDto? = null,
    @SerialName("cost_breakdown")
    val costBreakdown: CostBreakdownDto? = null,
    @SerialName("monthly_trend")
    val monthlyTrend: List<MonthlyTrendDto> = emptyList(),
    @SerialName("date_range")
    val dateRange: DateRangeDto? = null,
    @SerialName("period")
    val period: String? = null
)

/**
 * API response for customer financial report.
 */
@Serializable
data class CustomerFinancialReportResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: CustomerFinancialReportDataDto? = null
)

