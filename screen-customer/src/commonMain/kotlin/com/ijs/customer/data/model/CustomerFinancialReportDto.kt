package com.ijs.customer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============= Customer Financial Report DTOs =============

/**
 * Revenue summary block.
 */
@Serializable
data class FinancialRevenueSummaryDto(
    @SerialName("total_expected")
    val totalExpected: Double = 0.0,
    @SerialName("total_received")
    val totalReceived: Double = 0.0,
    @SerialName("total_pending")
    val totalPending: Double = 0.0,
    // Billed revenue = SUM(selling_value) — the base behind net_profit/net_margin.
    // (total_expected = SUM(trip_price), the quote, stays as the collection-rate denominator.)
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0
)

/**
 * Cost summary block.
 */
@Serializable
data class FinancialCostSummaryDto(
    @SerialName("total_trip_costs")
    val totalTripCosts: Double = 0.0,
    // Driver costs are EXCLUDED from the P&L methodology, so the backend always
    // returns 0 here. Kept in the shape for parity, but it does not reduce profit.
    @SerialName("total_driver_costs")
    val totalDriverCosts: Double = 0.0,
    @SerialName("fuel_costs")
    val fuelCosts: Double = 0.0,
    @SerialName("toll_costs")
    val tollCosts: Double = 0.0,
    @SerialName("other_costs")
    val otherCosts: Double = 0.0
)

/**
 * Profit & loss block.
 */
@Serializable
data class FinancialProfitLossDto(
    // GROSS profit = BILLED total_revenue (SUM selling_value) − total_trip_costs.
    @SerialName("gross_profit")
    val grossProfit: Double = 0.0,
    // NET profit. Driver costs are EXCLUDED from the P&L (total_driver_costs = 0),
    // so net_profit always equals gross_profit.
    @SerialName("net_profit")
    val netProfit: Double = 0.0,
    // Gross margin computed against BILLED revenue: gross_profit / total_revenue * 100.
    @SerialName("profit_margin")
    val profitMargin: Double = 0.0,
    // Net margin computed against BILLED revenue: net_profit / total_revenue * 100.
    // Equals profit_margin since net_profit == gross_profit.
    @SerialName("net_margin")
    val netMargin: Double = 0.0,
    @SerialName("collection_rate")
    val collectionRate: Double = 0.0,
    // Backend derives this from net_profit > 0.
    @SerialName("is_profitable")
    val isProfitable: Boolean = false
)

/**
 * Trip summary block.
 */
@Serializable
data class FinancialReportTripSummaryDto(
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("cancelled_trips")
    val cancelledTrips: Int = 0,
    @SerialName("active_trips")
    val activeTrips: Int = 0
)

/**
 * Period breakdown item.
 */
@Serializable
data class FinancialPeriodBreakdownDto(
    @SerialName("period")
    val period: String? = null,
    @SerialName("revenue")
    val revenue: Double = 0.0,
    @SerialName("collected")
    val collected: Double = 0.0,
    // Trip costs bucketed by the trip's period (driver costs excluded, per P&L).
    @SerialName("costs")
    val costs: Double = 0.0,
    // Real per-period profit (= revenue − costs), not the collected amount.
    @SerialName("profit")
    val profit: Double = 0.0,
    @SerialName("trips")
    val trips: Int = 0
)

/**
 * Top vehicle item.
 */
@Serializable
data class FinancialTopVehicleDto(
    @SerialName("vehicle_id")
    val vehicleId: Int = 0,
    @SerialName("registration_number")
    val registrationNumber: String? = null,
    @SerialName("trip_count")
    val tripCount: Int = 0,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0
)

/**
 * Nested data object for customer financial report response.
 * Matches backend GetFinancialReport response shape.
 */
@Serializable
data class CustomerFinancialReportDataDto(
    @SerialName("customer")
    val customer: CustomerSummaryDto? = null,
    @SerialName("revenue_summary")
    val revenueSummary: FinancialRevenueSummaryDto? = null,
    @SerialName("cost_summary")
    val costSummary: FinancialCostSummaryDto? = null,
    @SerialName("profit_loss")
    val profitLoss: FinancialProfitLossDto? = null,
    @SerialName("trip_summary")
    val tripSummary: FinancialReportTripSummaryDto? = null,
    @SerialName("period_breakdown")
    val periodBreakdown: List<FinancialPeriodBreakdownDto> = emptyList(),
    @SerialName("top_vehicles")
    val topVehicles: List<FinancialTopVehicleDto> = emptyList(),
    @SerialName("start_date")
    val startDate: Long? = null,
    @SerialName("end_date")
    val endDate: Long? = null
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

