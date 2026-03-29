package com.ijs.reports.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs for P&L Summary API (Reports Hub).
 * Split from ProfitLossDto.kt for 500-line compliance.
 */

/**
 * P&L Summary DTO
 * GET /reports/profit-loss/summary
 *
 * API Response structure:
 * {
 *   "period": {"start_date": "...", "end_date": "..."},
 *   "overview": {...},
 *   "fleet_summary": {...},
 *   "trip_summary": {...},
 *   "expense_breakdown": {...},
 *   "top_performers": {...},
 *   "alerts": {...}
 * }
 */
@Serializable
data class PLSummaryDto(
    @SerialName("period")
    val period: PLPeriodDto? = null,
    @SerialName("overview")
    val overview: PLOverviewDto? = null,
    @SerialName("fleet_summary")
    val fleetSummary: PLFleetSummaryDto? = null,
    @SerialName("trip_summary")
    val tripSummary: PLTripSummaryDto? = null,
    @SerialName("expense_breakdown")
    val expenseBreakdown: Map<String, PLExpenseItemDto>? = null,
    @SerialName("top_performers")
    val topPerformers: PLTopPerformersDto? = null,
    @SerialName("alerts")
    val alerts: PLAlertsContainerDto? = null
)

@Serializable
data class PLPeriodDto(
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null
)

@Serializable
data class PLOverviewDto(
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_expenses")
    val totalExpenses: Double = 0.0,
    @SerialName("gross_profit")
    val grossProfit: Double = 0.0,
    @SerialName("profit_margin_percentage")
    val profitMarginPercentage: Double = 0.0,
    @SerialName("status")
    val status: String? = null // "profit" or "loss"
)

@Serializable
data class PLFleetSummaryDto(
    @SerialName("total_vehicles")
    val totalVehicles: Int = 0,
    @SerialName("active_vehicles")
    val activeVehicles: Int = 0,
    @SerialName("profitable_vehicles")
    val profitableVehicles: Int = 0,
    @SerialName("loss_making_vehicles")
    val lossMakingVehicles: Int = 0
)

@Serializable
data class PLTripSummaryDto(
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("profitable_trips")
    val profitableTrips: Int = 0,
    @SerialName("loss_making_trips")
    val lossMakingTrips: Int = 0
)

@Serializable
data class PLExpenseItemDto(
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("percentage")
    val percentage: Double = 0.0
)

@Serializable
data class PLTopPerformersDto(
    @SerialName("most_profitable_vehicle")
    val mostProfitableVehicle: PLVehiclePerformerDto? = null,
    @SerialName("least_profitable_vehicle")
    val leastProfitableVehicle: PLVehiclePerformerDto? = null
)

@Serializable
data class PLVehiclePerformerDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("registration_number")
    val registrationNumber: String? = null,
    @SerialName("profit")
    val profit: Double = 0.0,
    @SerialName("loss")
    val loss: Double = 0.0
)

@Serializable
data class PLAlertsContainerDto(
    @SerialName("loss_making_vehicles")
    val lossMakingVehicles: List<PLVehiclePerformerDto>? = null
)

/**
 * P&L Alert DTO (kept for backward compatibility)
 */
@Serializable
data class PLAlertDto(
    @SerialName("type")
    val type: String = "",
    @SerialName("severity")
    val severity: String = "",
    @SerialName("message")
    val message: String = "",
    @SerialName("vehicle_id")
    val vehicleId: Int? = null,
    @SerialName("vehicle_number")
    val vehicleNumber: String? = null
)

