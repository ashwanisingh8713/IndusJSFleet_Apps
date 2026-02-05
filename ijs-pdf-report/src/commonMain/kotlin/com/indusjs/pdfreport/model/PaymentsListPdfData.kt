package com.indusjs.pdfreport.model

/**
 * Data class for Payments List PDF report.
 */
data class PaymentsListPdfData(
    val payments: List<PaymentListItem>,
    val totalPayments: Int,
    val totalReceived: Double,
    val totalPending: Double,
    val dateRange: String?,
    val filterInfo: String?,
    override val generatedAt: String
) : PdfReportData {
    override val reportType: PdfReportType = PdfReportType.PAYMENTS_LIST
    override val title: String = "Payments Report"
}

/**
 * Individual payment item for Payments List PDF.
 */
data class PaymentListItem(
    val paymentId: Int,
    val tripId: Int,
    val vehicleNumber: String,
    val customerName: String,
    val amount: Double,
    val paymentType: String,
    val paymentMode: String,
    val paymentDate: String,
    val paymentStatus: String,
    val receiptNumber: String?,
    val startLocation: String?,
    val endLocation: String?
)
