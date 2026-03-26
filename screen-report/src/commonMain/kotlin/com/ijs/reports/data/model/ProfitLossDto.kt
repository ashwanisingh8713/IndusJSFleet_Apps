package com.ijs.reports.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs for Profit & Loss APIs
 */

/**
 * Response wrapper for P&L APIs
 */
@Serializable
data class ProfitLossResponse<T>(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: T? = null
)

/**
 * Period object in P&L responses
 */
@Serializable
data class PeriodDto(
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null
)

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
    @SerialName("start_location")
    val startLocation: String? = null,
    @SerialName("end_location")
    val endLocation: String? = null,
    @SerialName("scheduled_date")
    val scheduledDate: String? = null,
    @SerialName("trip_date")
    val tripDate: String? = null,
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
 * Multi-Vehicle Profit/Loss Response wrapper DTO
 * POST /reports/profit-loss/vehicles
 *
 * The API returns data in this structure:
 * {
 *   "period": {...},
 *   "vehicles": [...],
 *   "summary": {...}
 * }
 */
@Serializable
data class MultiVehiclePLResponseDto(
    @SerialName("period")
    val period: PeriodDto? = null,
    @SerialName("vehicles")
    val vehicles: List<VehicleProfitLossDto> = emptyList(),
    @SerialName("summary")
    val summary: MultiVehicleSummaryDto? = null
)

/**
 * Summary section of multi-vehicle P&L response
 */
@Serializable
data class MultiVehicleSummaryDto(
    @SerialName("total_vehicles")
    val totalVehicles: Int = 0,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_expenses")
    val totalExpenses: Double = 0.0,
    @SerialName("total_profit_loss")
    val totalProfitLoss: Double = 0.0,
    @SerialName("average_profit_margin")
    val averageProfitMargin: Double = 0.0,
    @SerialName("profitable_vehicles")
    val profitableVehicles: Int = 0,
    @SerialName("loss_making_vehicles")
    val lossMakingVehicles: Int = 0
)

/**
 * Single Vehicle Profit/Loss DTO
 * GET /vehicles/{vehicle_id}/profit-loss
 */
@Serializable
data class VehicleProfitLossDto(
    @SerialName("vehicle_id")
    val vehicleId: Int,
    @SerialName("vehicle_number")
    val vehicleNumber: String? = null,
    @SerialName("vehicle_registration")
    val vehicleRegistration: String? = null,
    @SerialName("make")
    val make: String? = null,
    @SerialName("model")
    val model: String? = null,
    @SerialName("period")
    val period: PeriodDto? = null,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("total_distance")
    val totalDistance: Double = 0.0,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_trip_costs")
    val totalTripCosts: Double = 0.0,
    @SerialName("total_maintenance_costs")
    val totalMaintenanceCosts: Double = 0.0,
    @SerialName("total_cost")
    val totalCost: Double = 0.0,
    @SerialName("fuel_cost")
    val fuelCost: Double = 0.0,
    @SerialName("maintenance_cost")
    val maintenanceCost: Double = 0.0,
    @SerialName("other_cost")
    val otherCost: Double = 0.0,
    @SerialName("total_expenses")
    val totalExpenses: Double = 0.0,
    @SerialName("gross_profit")
    val grossProfit: Double = 0.0,
    @SerialName("net_profit")
    val netProfit: Double = 0.0,
    @SerialName("profit_margin")
    val profitMargin: Double = 0.0,
    @SerialName("profit_status")
    val profitStatus: String? = null,
    @SerialName("is_profitable")
    val isProfitable: Boolean = false,
    @SerialName("avg_profit_per_trip")
    val avgProfitPerTrip: Double = 0.0,
    @SerialName("avg_profit_per_km")
    val avgProfitPerKm: Double = 0.0,
    @SerialName("cost_breakdown")
    val costBreakdown: List<CostBreakdownItemDto>? = null,
    @SerialName("trip_summary")
    val tripSummary: List<TripSummaryItemDto>? = null
)

/**
 * Fleet Profit/Loss DTO
 * GET /reports/profit-loss
 */
@Serializable
data class FleetProfitLossDto(
    @SerialName("period")
    val period: String? = null,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    @SerialName("total_vehicles")
    val totalVehicles: Int = 0,
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_expenses")
    val totalExpenses: Double = 0.0,
    @SerialName("total_trip_costs")
    val totalTripCosts: Double = 0.0,
    @SerialName("total_maintenance_costs")
    val totalMaintenanceCosts: Double = 0.0,
    @SerialName("gross_profit")
    val grossProfit: Double = 0.0,
    @SerialName("net_profit")
    val netProfit: Double = 0.0,
    @SerialName("profit_margin")
    val profitMargin: Double = 0.0,
    @SerialName("is_profitable")
    val isProfitable: Boolean = false,
    @SerialName("vehicle_breakdown")
    val vehicleBreakdown: List<VehicleProfitLossDto>? = null,
    @SerialName("cost_breakdown")
    val costBreakdown: List<CostBreakdownItemDto>? = null
)

/**
 * Cost Breakdown Item DTO.
 * Updated to include structured cost fields (cost_id, cost_label, group_id).
 */
@Serializable
data class CostBreakdownItemDto(
    // New structured cost fields per API
    @SerialName("cost_id")
    val costId: String? = null,
    @SerialName("cost_label")
    val costLabel: String? = null,
    @SerialName("group_id")
    val groupId: String? = null,
    // Legacy field (kept for backward compatibility)
    @SerialName("cost_type")
    val costType: String,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("percentage")
    val percentage: Double = 0.0
) {
    /**
     * Returns the display label - prefers cost_label, falls back to cost_type.
     */
    val displayLabel: String
        get() = costLabel ?: costType.replace("_", " ").replaceFirstChar { it.uppercase() }
}

/**
 * Trip Summary Item DTO
 */
@Serializable
data class TripSummaryItemDto(
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("scheduled_date")
    val scheduledDate: String? = null,
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

