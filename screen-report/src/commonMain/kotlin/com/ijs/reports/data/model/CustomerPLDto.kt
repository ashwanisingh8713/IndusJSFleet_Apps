package com.ijs.reports.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs for the "P&L by customer" report.
 * GET /api/v1/reports/profit-loss/customers?period=monthly
 */
@Serializable
data class CustomerPLReportDto(
    @SerialName("period")
    val period: String? = null,
    @SerialName("start_date")
    val startDate: Long? = null,
    @SerialName("end_date")
    val endDate: Long? = null,
    @SerialName("summary")
    val summary: CustomerPLSummaryDto? = null,
    @SerialName("customers")
    val customers: List<CustomerPLItemDto>? = null,
    @SerialName("customer_count")
    val customerCount: Int = 0
)

@Serializable
data class CustomerPLSummaryDto(
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_cost")
    val totalCost: Double = 0.0,
    @SerialName("driver_cost")
    val driverCost: Double = 0.0,
    @SerialName("net_profit")
    val netProfit: Double = 0.0,
    @SerialName("total_paid")
    val totalPaid: Double = 0.0,
    @SerialName("total_pending")
    val totalPending: Double = 0.0,
    @SerialName("active_customers")
    val activeCustomers: Int = 0
)

@Serializable
data class CustomerPLItemDto(
    @SerialName("customer_id")
    val customerId: Int = 0,
    @SerialName("customer_name")
    val customerName: String? = null,
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_cost")
    val totalCost: Double = 0.0,
    @SerialName("fuel_cost")
    val fuelCost: Double = 0.0,
    @SerialName("other_cost")
    val otherCost: Double = 0.0,
    @SerialName("driver_cost")
    val driverCost: Double = 0.0,
    @SerialName("net_profit")
    val netProfit: Double = 0.0,
    @SerialName("profit_margin")
    val profitMargin: Double = 0.0,
    @SerialName("total_paid")
    val totalPaid: Double = 0.0,
    @SerialName("total_pending")
    val totalPending: Double = 0.0,
    @SerialName("collection_rate")
    val collectionRate: Double = 0.0,
    @SerialName("avg_profit_per_trip")
    val avgProfitPerTrip: Double = 0.0
)
