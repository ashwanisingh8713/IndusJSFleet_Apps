package com.ijs.reports.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs for Vehicle Profit & Loss APIs.
 * Split from ProfitLossDto.kt for 500-line compliance.
 */

/**
 * Multi-Vehicle Profit/Loss Response wrapper DTO
 * POST /reports/profit-loss/vehicles
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
    // Backend (domain.VehicleProfitLoss / application.VehiclePLResult) emits
    // "vehicle_make" / "vehicle_model" — NOT bare "make" / "model".
    @SerialName("vehicle_make")
    val make: String? = null,
    @SerialName("vehicle_model")
    val model: String? = null,
    @SerialName("period")
    val period: PeriodDto? = null,
    @SerialName("start_date")
    val startDate: Long? = null,
    @SerialName("end_date")
    val endDate: Long? = null,
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
    @SerialName("driver_cost")
    val driverCost: Double = 0.0,
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
 *
 * Mirrors backend application/report.FleetPLResult:
 * {
 *   "period": "monthly",            // period NAME (string), not an object
 *   "start_date": <epoch-millis>,
 *   "end_date": <epoch-millis>,
 *   "fleet_summary": { ...FleetTotal },
 *   "vehicles": [ ...VehicleProfitLoss ],
 *   "vehicle_count": <int>
 * }
 */
@Serializable
data class FleetProfitLossDto(
    // Backend emits the period NAME as a string ("monthly"), not a {start,end} object.
    @SerialName("period")
    val period: String? = null,
    @SerialName("start_date")
    val startDate: Long? = null,
    @SerialName("end_date")
    val endDate: Long? = null,
    @SerialName("fleet_summary")
    val fleetSummary: FleetPLSummaryDto? = null,
    @SerialName("vehicles")
    val vehicles: List<VehicleProfitLossDto>? = null,
    @SerialName("vehicle_count")
    val vehicleCount: Int = 0
)

/**
 * Fleet totals (backend application/report.FleetTotal). The backend emits:
 * total_trips, total_distance, total_revenue, total_cost, driver_cost,
 * net_profit, profit_margin, completed_trips, active_vehicles.
 *
 * NOTE: This is an opex P&L (no gross_profit / COGS at the fleet level — the
 * consolidated endpoint is the gross/COGS source).
 */
@Serializable
data class FleetPLSummaryDto(
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("total_distance")
    val totalDistance: Double = 0.0,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_cost")
    val totalCost: Double = 0.0,
    @SerialName("driver_cost")
    val driverCost: Double = 0.0,
    @SerialName("net_profit")
    val netProfit: Double = 0.0,
    @SerialName("profit_margin")
    val profitMargin: Double = 0.0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("active_vehicles")
    val activeVehicles: Int = 0
)

