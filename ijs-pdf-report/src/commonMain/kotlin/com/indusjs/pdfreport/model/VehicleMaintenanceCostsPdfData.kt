package com.indusjs.pdfreport.model

/**
 * Data class for Vehicle Maintenance Costs PDF report.
 * Contains vehicle information and maintenance cost breakdown for a specified period.
 */
data class VehicleMaintenanceCostsPdfData(
    val vehicleId: Int,
    val registrationNumber: String,
    val vehicleMake: String,
    val vehicleModel: String,
    val period: String, // e.g., "01-01-2026 to 07-02-2026" or "All Time"
    val costs: List<VehicleMaintenanceCostItem>,
    val costsByType: Map<String, List<VehicleMaintenanceCostItem>>,
    val totalAmount: Double,
    val entryCount: Int,
    val categoryCount: Int,
    override val generatedAt: String
) : PdfReportData {
    override val reportType: PdfReportType = PdfReportType.VEHICLE_MAINTENANCE_COSTS
    override val title: String = "Vehicle Maintenance Costs Report"
}

/**
 * Individual cost item for Vehicle Maintenance Costs PDF.
 */
data class VehicleMaintenanceCostItem(
    val id: Int,
    val costId: String,
    val costLabel: String,
    val amount: Double,
    val date: String,
    val vendorName: String?,
    val description: String?,
    val notes: String?
)

