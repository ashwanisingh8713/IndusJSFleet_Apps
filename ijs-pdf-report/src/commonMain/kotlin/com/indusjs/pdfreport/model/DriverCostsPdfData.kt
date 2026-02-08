package com.indusjs.pdfreport.model

/**
 * Data class for Driver Costs PDF report.
 * Contains driver information and cost breakdown for a specified period.
 */
data class DriverCostsPdfData(
    val driverId: Int,
    val driverName: String,
    val mobile: String?,
    val licenseNumber: String?,
    val period: String, // e.g., "01-01-2026 to 07-02-2026" or "January 2026"
    val costs: List<DriverCostItem>,
    val costsByGroup: Map<String, List<DriverCostItem>>,
    val totalEarnings: Double,
    val totalDeductions: Double,
    val netAmount: Double,
    val entryCount: Int,
    val categoryCount: Int,
    override val generatedAt: String
) : PdfReportData {
    override val reportType: PdfReportType = PdfReportType.DRIVER_COSTS
    override val title: String = "Driver Costs Report"
}

/**
 * Individual cost item for Driver Costs PDF.
 */
data class DriverCostItem(
    val id: Int,
    val costId: String,
    val costLabel: String,
    val groupId: String,
    val groupName: String,
    val amount: Double,
    val date: String,
    val isDeduction: Boolean,
    val tripId: Int?,
    val description: String?,
    val notes: String?
)

