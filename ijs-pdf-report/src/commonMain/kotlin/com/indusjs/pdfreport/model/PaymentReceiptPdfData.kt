package com.indusjs.pdfreport.model

/**
 * Data class for Payment Receipt PDF.
 */
data class PaymentReceiptPdfData(
    val paymentId: Int,
    val receiptNumber: String,
    val tripId: Int,
    val vehicleNumber: String,
    val driverName: String?,
    val customerName: String,
    val customerCompany: String?,
    val customerContact: String?,
    val startLocation: String,
    val endLocation: String,
    val tripDate: String,
    val amount: Double,
    val tdsAmount: Double,
    val discountAmount: Double,
    val netAmount: Double,
    val paymentType: String,
    val paymentMode: String,
    val paymentDate: String,
    val paymentStatus: String,
    val transactionId: String?,
    val bankName: String?,
    val notes: String?,
    val createdBy: String?,
    override val generatedAt: String
) : PdfReportData {
    override val reportType: PdfReportType = PdfReportType.PAYMENT_RECEIPT
    override val title: String = "Payment Receipt"
}
