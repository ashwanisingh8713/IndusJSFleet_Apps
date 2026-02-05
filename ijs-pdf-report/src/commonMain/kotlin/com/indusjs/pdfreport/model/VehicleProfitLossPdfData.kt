package com.indusjs.pdfreport.model

/**
 * Data class for Vehicle Profit & Loss PDF Report.
 * Used for generating single vehicle financial reports.
 */
data class VehicleProfitLossPdfData(
    val vehicleId: Int,
    val registrationNumber: String,
    val vehicleName: String,
    val dateRange: String,
    val periodType: String,
    val totalTrips: Int,
    val totalDistance: Double,
    val totalRevenue: Double,
    val totalCost: Double,
    val fuelCost: Double,
    val maintenanceCost: Double,
    val otherCost: Double,
    val netProfitLoss: Double,
    val profitMargin: Double,
    val avgProfitPerTrip: Double,
    val avgProfitPerKm: Double,
    val profitStatus: String,
    val tripDetails: List<TripPLPdfItem>,
    val costBreakdown: List<CostBreakdownPdfItem>,
    override val generatedAt: String
) : PdfReportData {
    override val reportType: PdfReportType = PdfReportType.VEHICLE_PROFIT_LOSS
    override val title: String = "Vehicle P&L Report - $registrationNumber"
}

/**
 * Individual trip P&L item for PDF.
 */
data class TripPLPdfItem(
    val tripId: Int,
    val tripDate: String,
    val startLocation: String,
    val endLocation: String,
    val distance: Double,
    val revenue: Double,
    val cost: Double,
    val profit: Double
)
