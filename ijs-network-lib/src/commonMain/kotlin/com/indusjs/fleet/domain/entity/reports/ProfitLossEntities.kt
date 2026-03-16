package com.indusjs.fleet.domain.entity.reports

/**
 * Domain entities for Profit & Loss reports
 */

/**
 * Single Trip Profit/Loss
 */
data class TripProfitLoss(
    val tripId: Int,
    val vehicleId: Int? = null,
    val vehicleNumber: String? = null,
    val driverId: Int? = null,
    val driverName: String? = null,
    val startLocation: String? = null,
    val endLocation: String? = null,
    val scheduledDate: String? = null,
    val state: String? = null,
    val purchasePrice: Double = 0.0,
    val sellingValue: Double = 0.0,
    val totalTripCosts: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val grossProfit: Double = 0.0,
    val netProfit: Double = 0.0,
    val profitMargin: Double = 0.0,
    val isProfitable: Boolean = false,
    val costBreakdown: List<CostBreakdownItem> = emptyList()
)

/**
 * Single Vehicle Profit/Loss
 */
data class VehicleProfitLoss(
    val vehicleId: Int,
    val vehicleNumber: String? = null,
    val make: String? = null,
    val model: String? = null,
    val period: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val totalTrips: Int = 0,
    val completedTrips: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalTripCosts: Double = 0.0,
    val totalMaintenanceCosts: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val grossProfit: Double = 0.0,
    val netProfit: Double = 0.0,
    val profitMargin: Double = 0.0,
    val isProfitable: Boolean = false,
    val costBreakdown: List<CostBreakdownItem> = emptyList(),
    val tripSummary: List<TripSummaryItem> = emptyList()
)

/**
 * Fleet Profit/Loss
 */
data class FleetProfitLoss(
    val period: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val totalVehicles: Int = 0,
    val totalTrips: Int = 0,
    val completedTrips: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalTripCosts: Double = 0.0,
    val totalMaintenanceCosts: Double = 0.0,
    val grossProfit: Double = 0.0,
    val netProfit: Double = 0.0,
    val profitMargin: Double = 0.0,
    val isProfitable: Boolean = false,
    val vehicleBreakdown: List<VehicleProfitLoss> = emptyList(),
    val costBreakdown: List<CostBreakdownItem> = emptyList()
)

/**
 * Cost Breakdown Item
 */
data class CostBreakdownItem(
    val costType: String,
    val amount: Double = 0.0,
    val count: Int = 0,
    val percentage: Double = 0.0
)

/**
 * Trip Summary Item
 */
data class TripSummaryItem(
    val tripId: Int,
    val scheduledDate: String? = null,
    val startLocation: String? = null,
    val endLocation: String? = null,
    val revenue: Double = 0.0,
    val expenses: Double = 0.0,
    val profit: Double = 0.0,
    val isProfitable: Boolean = false
)

/**
 * Cost Type Analysis
 */
data class CostTypeAnalysis(
    val costType: String,
    val startDate: String? = null,
    val endDate: String? = null,
    val totalAmount: Double = 0.0,
    val totalCount: Int = 0,
    val averagePerEntry: Double = 0.0,
    val vehicleBreakdown: List<VehicleCostBreakdown> = emptyList(),
    val monthlyTrend: List<MonthlyTrend> = emptyList()
) {
    /**
     * Human-readable display name for the cost type
     */
    val displayName: String get() = costType
        .replace("_", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}

/**
 * Vehicle Cost Breakdown
 */
data class VehicleCostBreakdown(
    val vehicleId: Int,
    val vehicleNumber: String? = null,
    val amount: Double = 0.0,
    val count: Int = 0,
    val percentage: Double = 0.0
)

/**
 * Monthly Trend
 */
data class MonthlyTrend(
    val month: String,
    val year: Int,
    val amount: Double = 0.0,
    val count: Int = 0
)

/**
 * Consolidated P&L Report
 */
data class ConsolidatedPL(
    val startDate: String? = null,
    val endDate: String? = null,
    val groupBy: String? = null,
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val profitMargin: Double = 0.0,
    val isProfitable: Boolean = false,
    val totalVehicles: Int = 0,
    val totalTrips: Int = 0,
    val completedTrips: Int = 0,
    val vehicleSummary: List<VehiclePLSummary> = emptyList(),
    val tripSummary: List<TripPLSummary> = emptyList(),
    val costBreakdown: List<CostBreakdownItem> = emptyList(),
    val periodBreakdown: List<PeriodBreakdown> = emptyList()
)

/**
 * Vehicle P&L Summary (for consolidated report)
 */
data class VehiclePLSummary(
    val vehicleId: Int,
    val vehicleNumber: String? = null,
    val revenue: Double = 0.0,
    val expenses: Double = 0.0,
    val profit: Double = 0.0,
    val profitMargin: Double = 0.0,
    val isProfitable: Boolean = false,
    val tripCount: Int = 0
)

/**
 * Trip P&L Summary (for consolidated report)
 */
data class TripPLSummary(
    val tripId: Int,
    val vehicleNumber: String? = null,
    val route: String? = null,
    val scheduledDate: String? = null,
    val revenue: Double = 0.0,
    val expenses: Double = 0.0,
    val profit: Double = 0.0,
    val isProfitable: Boolean = false
)

/**
 * Period Breakdown (monthly/weekly grouping)
 */
data class PeriodBreakdown(
    val period: String,
    val label: String? = null,
    val revenue: Double = 0.0,
    val expenses: Double = 0.0,
    val profit: Double = 0.0,
    val tripCount: Int = 0,
    val isProfitable: Boolean = false
)

/**
 * P&L Summary - matches new API structure
 */
data class PLSummary(
    val startDate: String? = null,
    val endDate: String? = null,
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val grossProfit: Double = 0.0,
    val profitMarginPercentage: Double = 0.0,
    val status: String = "neutral", // "profit" or "loss"
    val isProfitable: Boolean = false,
    val totalVehicles: Int = 0,
    val activeVehicles: Int = 0,
    val profitableVehicles: Int = 0,
    val lossMakingVehicles: Int = 0,
    val totalTrips: Int = 0,
    val completedTrips: Int = 0,
    val profitableTrips: Int = 0,
    val lossMakingTrips: Int = 0,
    val expenseBreakdown: List<ExpenseBreakdownItem> = emptyList(),
    val topPerformingVehicle: VehiclePerformer? = null,
    val lossMakingVehiclesList: List<VehiclePerformer> = emptyList()
)

/**
 * Expense Breakdown Item
 */
data class ExpenseBreakdownItem(
    val type: String,
    val amount: Double = 0.0,
    val percentage: Double = 0.0
)

/**
 * Vehicle Performer (top/bottom performer)
 */
data class VehiclePerformer(
    val id: Int = 0,
    val registrationNumber: String? = null,
    val profit: Double = 0.0,
    val loss: Double = 0.0
)

/**
 * P&L Alert
 */
data class PLAlert(
    val type: String = "",
    val severity: String = "",
    val message: String = "",
    val vehicleId: Int? = null,
    val vehicleNumber: String? = null
)

