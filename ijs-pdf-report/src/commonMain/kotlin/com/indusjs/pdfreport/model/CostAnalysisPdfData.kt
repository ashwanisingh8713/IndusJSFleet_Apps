package com.indusjs.pdfreport.model

/**
 * Data class for Cost Analysis PDF Report.
 * Used for generating detailed cost breakdown reports.
 */
data class CostAnalysisPdfData(
    val analysisType: String, // "trip", "maintenance", "driver", "all"
    val dateRange: String,
    val totalCost: Double,
    val costGroups: List<CostGroupPdfItem>,
    val topCostItems: List<TopCostPdfItem>,
    val monthlyTrend: List<MonthlyCostPdfItem>,
    val vehicleWiseCosts: List<VehicleCostPdfItem>? = null,
    val driverWiseCosts: List<DriverCostPdfItem>? = null,
    override val generatedAt: String
) : PdfReportData {
    override val reportType: PdfReportType = PdfReportType.COST_ANALYSIS
    override val title: String = "Cost Analysis Report"
}

/**
 * Cost group for PDF.
 */
data class CostGroupPdfItem(
    val groupId: String,
    val groupLabel: String,
    val totalAmount: Double,
    val itemCount: Int,
    val percentage: Double,
    val items: List<CostItemPdfItem>
)

/**
 * Individual cost item for PDF.
 */
data class CostItemPdfItem(
    val costId: String,
    val costLabel: String,
    val amount: Double,
    val count: Int
)

/**
 * Top cost item for PDF.
 */
data class TopCostPdfItem(
    val rank: Int,
    val costLabel: String,
    val amount: Double,
    val percentage: Double
)

/**
 * Monthly cost trend item for PDF.
 */
data class MonthlyCostPdfItem(
    val month: String, // "Jan 2026"
    val amount: Double
)

/**
 * Vehicle-wise cost item for PDF.
 */
data class VehicleCostPdfItem(
    val vehicleId: Int,
    val registrationNumber: String,
    val totalCost: Double,
    val tripCost: Double,
    val maintenanceCost: Double
)

/**
 * Driver-wise cost item for PDF.
 */
data class DriverCostPdfItem(
    val driverId: Int,
    val driverName: String,
    val totalCost: Double,
    val allowance: Double,
    val otherCost: Double
)
