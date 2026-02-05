package com.indusjs.pdfreport.model

/**
 * Data class for Vehicle Finance/EMI PDF Report.
 * Used for generating vehicle purchase and EMI payment reports.
 */
data class VehicleFinancePdfData(
    val vehicleId: Int,
    val registrationNumber: String,
    val vehicleName: String, // Make + Model
    val purchaseDate: String,
    val purchasePrice: Double,
    val paymentType: String, // "loan" or "cash"
    val downPayment: Double? = null,
    val loanAmount: Double? = null,
    val interestRate: Double? = null,
    val tenureMonths: Int? = null,
    val emiAmount: Double? = null,
    val financierName: String? = null,
    val totalPaidAmount: Double,
    val remainingAmount: Double,
    val paidEmisCount: Int,
    val remainingEmisCount: Int,
    val nextEmiDueDate: String? = null,
    val emiPayments: List<EmiPaymentPdfItem>,
    override val generatedAt: String
) : PdfReportData {
    override val reportType: PdfReportType = PdfReportType.VEHICLE_FINANCE
    override val title: String = "Vehicle Finance Report - $registrationNumber"
}

/**
 * Individual EMI payment item for PDF.
 */
data class EmiPaymentPdfItem(
    val emiNumber: Int,
    val paymentDate: String,
    val amount: Double,
    val paymentMode: String,
    val status: String // "paid", "pending", "overdue"
)
