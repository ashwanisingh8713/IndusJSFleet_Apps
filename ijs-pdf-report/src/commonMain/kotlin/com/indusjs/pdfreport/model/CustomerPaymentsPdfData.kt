package com.indusjs.pdfreport.model

/**
 * Data class for Customer Payments PDF report.
 */
data class CustomerPaymentsPdfData(
    val customerId: Int,
    val customerName: String,
    val companyName: String?,
    val contactNumber: String?,
    val payments: List<CustomerPaymentItem>,
    val totalPayments: Int,
    val totalAmount: Double,
    val dateRange: String?,
    override val generatedAt: String
) : PdfReportData {
    override val reportType: PdfReportType = PdfReportType.CUSTOMER_PAYMENTS
    override val title: String = "Customer Payments Report"
}

/**
 * Individual payment item for Customer Payments PDF.
 */
data class CustomerPaymentItem(
    val paymentId: Int,
    val tripId: Int,
    val vehicleNumber: String,
    val amount: Double,
    val paymentType: String,
    val paymentMode: String,
    val paymentDate: String,
    val receiptNumber: String?,
    val startLocation: String?,
    val endLocation: String?
)
