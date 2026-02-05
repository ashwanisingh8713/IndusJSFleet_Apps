package com.indusjs.pdfreport.model

/**
 * Data class for Customer Trips PDF report.
 */
data class CustomerTripsPdfData(
    val customerId: Int,
    val customerName: String,
    val companyName: String?,
    val contactNumber: String?,
    val trips: List<CustomerTripItem>,
    val totalTrips: Int,
    val totalRevenue: Double,
    val totalPaid: Double,
    val totalPending: Double,
    val dateRange: String?,
    override val generatedAt: String
) : PdfReportData {
    override val reportType: PdfReportType = PdfReportType.CUSTOMER_TRIPS
    override val title: String = "Customer Trips Report"
}

/**
 * Individual trip item for Customer Trips PDF.
 */
data class CustomerTripItem(
    val tripId: Int,
    val vehicleNumber: String,
    val startLocation: String,
    val endLocation: String,
    val startDate: String,
    val endDate: String?,
    val tripStatus: String,
    val tripPrice: Double,
    val paidAmount: Double,
    val pendingAmount: Double
)
