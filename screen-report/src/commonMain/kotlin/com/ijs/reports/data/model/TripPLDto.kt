package com.ijs.reports.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs for Trip Profit & Loss APIs.
 * Split from ProfitLossDto.kt for 500-line compliance.
 */

/**
 * Single Trip Profit/Loss DTO
 * GET /trips/{trip_id}/profit-loss
 */
@Serializable
data class TripProfitLossDto(
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("vehicle_id")
    val vehicleId: Int? = null,
    @SerialName("vehicle_number")
    val vehicleNumber: String? = null,
    @SerialName("vehicle_registration")
    val vehicleRegistration: String? = null,
    @SerialName("driver_id")
    val driverId: Int? = null,
    @SerialName("driver_name")
    val driverName: String? = null,
    @SerialName("customer_id")
    val customerId: Int? = null,
    @SerialName("customer_name")
    val customerName: String? = null,
    @SerialName("start_location")
    val startLocation: String? = null,
    @SerialName("end_location")
    val endLocation: String? = null,
    @SerialName("scheduled_date")
    val scheduledDate: Long? = null,
    @SerialName("trip_date")
    val tripDate: Long? = null,
    @SerialName("state")
    val state: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("distance")
    val distance: Double? = null,
    @SerialName("trip_price")
    val tripPrice: Double = 0.0,
    @SerialName("purchase_price")
    val purchasePrice: Double = 0.0,
    @SerialName("selling_value")
    val sellingValue: Double = 0.0,
    @SerialName("total_trip_costs")
    val totalTripCosts: Double = 0.0,
    @SerialName("total_cost")
    val totalCost: Double = 0.0,
    @SerialName("driver_cost")
    val driverCost: Double = 0.0,
    @SerialName("total_expenses")
    val totalExpenses: Double = 0.0,
    @SerialName("gross_profit")
    val grossProfit: Double = 0.0,
    @SerialName("net_profit")
    val netProfit: Double = 0.0,
    @SerialName("profit_margin")
    val profitMargin: Double = 0.0,
    @SerialName("is_profitable")
    val isProfitable: Boolean = false,
    @SerialName("payment_status")
    val paymentStatus: String? = null,
    @SerialName("pending_amount")
    val pendingAmount: Double = 0.0,
    @SerialName("cost_breakdown")
    val costBreakdown: List<CostBreakdownItemDto>? = null
)

/**
 * Multi-Trip Profit/Loss Response wrapper DTO
 * POST /reports/profit-loss/trips
 *
 * Mirrors backend domain.MultiTripProfitLossResponse = { period, trips, summary }.
 * The backend returns this object, NOT a bare array of trips.
 */
@Serializable
data class MultiTripPLResponseDto(
    @SerialName("period")
    val period: PeriodDto? = null,
    @SerialName("trips")
    val trips: List<TripProfitLossDto> = emptyList(),
    @SerialName("summary")
    val summary: MultiTripSummaryDto? = null
)

/**
 * Summary section of multi-trip P&L response (backend domain.TripsSummary).
 */
@Serializable
data class MultiTripSummaryDto(
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_expenses")
    val totalExpenses: Double = 0.0,
    @SerialName("total_profit_loss")
    val totalProfitLoss: Double = 0.0,
    @SerialName("average_profit_margin")
    val averageProfitMargin: Double = 0.0,
    @SerialName("profitable_trips")
    val profitableTrips: Int = 0,
    @SerialName("loss_making_trips")
    val lossMakingTrips: Int = 0
)

/**
 * Trip Summary Item DTO
 */
@Serializable
data class TripSummaryItemDto(
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("scheduled_date")
    val scheduledDate: Long? = null,
    @SerialName("start_location")
    val startLocation: String? = null,
    @SerialName("end_location")
    val endLocation: String? = null,
    @SerialName("revenue")
    val revenue: Double = 0.0,
    @SerialName("expenses")
    val expenses: Double = 0.0,
    @SerialName("profit")
    val profit: Double = 0.0,
    @SerialName("is_profitable")
    val isProfitable: Boolean = false
)

