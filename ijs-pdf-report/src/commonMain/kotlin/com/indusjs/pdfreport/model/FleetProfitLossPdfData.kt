package com.indusjs.pdfreport.model

/**
 * Data class for Fleet Profit & Loss PDF Report.
 * Used for generating fleet-wide financial overview reports.
 */
data class FleetProfitLossPdfData(
    val dateRange: String, // e.g., "01-Jan-2026 to 31-Jan-2026"
    val periodType: String, // "weekly", "monthly", "quarterly", "yearly", "custom"
    val totalVehicles: Int,
    val profitableVehicles: Int,
    val lossMakingVehicles: Int,
    val totalRevenue: Double,
    val totalExpenses: Double,
    val netProfitLoss: Double,
    val profitMargin: Double,
    val vehicles: List<VehiclePLPdfItem>,
    val costBreakdown: List<CostBreakdownPdfItem>,
    override val generatedAt: String
) : PdfReportData {
    override val reportType: PdfReportType = PdfReportType.FLEET_PROFIT_LOSS
    override val title: String = "Fleet Profit & Loss Report"
}

/**
 * Individual vehicle P&L item for PDF.
 */
data class VehiclePLPdfItem(
    val vehicleId: Int,
    val registrationNumber: String,
    val vehicleName: String,
    val totalTrips: Int,
    val totalDistance: Double,
    val totalRevenue: Double,
    val totalCost: Double,
    val fuelCost: Double,
    val maintenanceCost: Double,
    val otherCost: Double,
    val netProfit: Double,
    val profitMargin: Double,
    val profitStatus: String // "profit" or "loss"
)

/**
 * Cost breakdown item for PDF.
 */
data class CostBreakdownPdfItem(
    val costId: String,
    val costLabel: String,
    val amount: Double,
    val count: Int,
    val percentage: Double
)
