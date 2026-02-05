package com.indusjs.pdfreport.model

/**
 * Data class for Customer Financials PDF Report.
 * Used for generating customer financial summary reports.
 */
data class CustomerFinancialsPdfData(
    val customerId: Int,
    val customerName: String,
    val companyName: String,
    val contactNumber: String,
    val dateRange: String,
    val totalTrips: Int,
    val completedTrips: Int,
    val activeTrips: Int,
    val totalRevenue: Double,
    val totalReceived: Double,
    val totalPending: Double,
    val paymentStats: CustomerPaymentStatsPdf,
    val recentPayments: List<CustomerPaymentPdfItem>,
    val tripSummary: List<CustomerTripSummaryPdfItem>,
    override val generatedAt: String
) : PdfReportData {
    override val reportType: PdfReportType = PdfReportType.CUSTOMER_FINANCIALS
    override val title: String = "Customer Financial Report - $customerName"
}

/**
 * Customer payment statistics for PDF.
 */
data class CustomerPaymentStatsPdf(
    val totalPayments: Int,
    val advancePayments: Int,
    val partialPayments: Int,
    val finalPayments: Int,
    val avgPaymentAmount: Double
)

/**
 * Customer payment item for financials PDF.
 */
data class CustomerPaymentPdfItem(
    val paymentId: Int,
    val tripId: Int,
    val paymentDate: String,
    val amount: Double,
    val paymentType: String,
    val paymentMode: String,
    val receiptNumber: String
)

/**
 * Customer trip summary item for PDF.
 */
data class CustomerTripSummaryPdfItem(
    val tripId: Int,
    val tripDate: String,
    val route: String,
    val tripPrice: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val paymentStatus: String
)
