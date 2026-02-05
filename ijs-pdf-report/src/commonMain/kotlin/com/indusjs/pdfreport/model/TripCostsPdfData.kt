package com.indusjs.pdfreport.model

/**
 * Data class for Trip Costs PDF report.
 */
data class TripCostsPdfData(
    val tripId: Int,
    val tripNumber: String,
    val vehicleNumber: String,
    val driverName: String?,
    val startLocation: String,
    val endLocation: String,
    val departureDate: String,
    val arrivalDate: String?,
    val tripStatus: String,
    val costs: List<TripCostItem>,
    val totalCost: Double,
    val fuelCost: Double,
    val tollCost: Double,
    val otherCost: Double,
    override val generatedAt: String
) : PdfReportData {
    override val reportType: PdfReportType = PdfReportType.TRIP_COSTS
    override val title: String = "Trip Costs Report"
}

/**
 * Individual cost item for Trip Costs PDF.
 */
data class TripCostItem(
    val costId: String,
    val costLabel: String,
    val amount: Double,
    val date: String,
    val time: String?,
    val notes: String?
)
