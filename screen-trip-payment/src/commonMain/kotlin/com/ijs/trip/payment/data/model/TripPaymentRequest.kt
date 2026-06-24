package com.ijs.trip.payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for creating a trip payment.
 * POST /trip-payments or POST /trips/{id}/payments
 */
@Serializable
data class CreateTripPaymentRequest(
    // trip_id and vehicle_id are REQUIRED by the backend (binding:"required");
    // sending null/omitting them yields a 400.
    @SerialName("trip_id") val tripId: Int,
    @SerialName("vehicle_id") val vehicleId: Int,
    @SerialName("driver_id") val driverId: Int? = null,
    @SerialName("customer_id") val customerId: Int? = null,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("customer_contact") val customerContact: String? = null,
    @SerialName("customer_company") val customerCompany: String? = null,
    @SerialName("customer_gst") val customerGst: String? = null,
    @SerialName("amount") val amount: Double,
    @SerialName("tds_amount") val tdsAmount: Double = 0.0,
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    @SerialName("payment_type") val paymentType: String,
    @SerialName("payment_mode") val paymentMode: String,
    @SerialName("payment_source") val paymentSource: String? = null,
    @SerialName("payment_date") val paymentDate: Long,
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("bank_name") val bankName: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("received_by") val receivedBy: String? = null,
    @SerialName("received_at_location") val receivedAtLocation: String? = null,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("due_date") val dueDate: Long? = null,
    @SerialName("reference_number") val referenceNumber: String? = null,
    @SerialName("bank_branch") val bankBranch: String? = null,
    @SerialName("account_number") val accountNumber: String? = null,
    @SerialName("ifsc_code") val ifscCode: String? = null,
    @SerialName("invoice_number") val invoiceNumber: String? = null,
    @SerialName("invoice_date") val invoiceDate: Long? = null,
    @SerialName("invoice_amount") val invoiceAmount: Double? = null,
    @SerialName("remarks") val remarks: String? = null
)

/**
 * Request body for updating a trip payment.
 * PUT /trip-payments/{id}
 *
 * Mirrors the backend UpdatePaymentRequest exactly (snake_case, partial update).
 * payment_mode is required by the backend; the edit screen is responsible for
 * always sending it.
 */
@Serializable
data class UpdateTripPaymentRequest(
    @SerialName("tds_amount") val tdsAmount: Double? = null,
    @SerialName("discount_amount") val discountAmount: Double? = null,
    // payment_mode is REQUIRED by the backend (binding:"required") on update;
    // the edit screen must always supply it or the PUT returns 400.
    @SerialName("payment_mode") val paymentMode: String,
    @SerialName("payment_source") val paymentSource: String? = null,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("reference_number") val referenceNumber: String? = null,
    @SerialName("bank_name") val bankName: String? = null,
    @SerialName("bank_branch") val bankBranch: String? = null,
    @SerialName("account_number") val accountNumber: String? = null,
    @SerialName("ifsc_code") val ifscCode: String? = null,
    @SerialName("invoice_number") val invoiceNumber: String? = null,
    @SerialName("invoice_date") val invoiceDate: Long? = null,
    @SerialName("invoice_amount") val invoiceAmount: Double? = null,
    @SerialName("received_by") val receivedBy: String? = null,
    @SerialName("received_at_location") val receivedAtLocation: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("remarks") val remarks: String? = null
)

/**
 * Request for adding payment to a specific trip.
 * POST /trip-payments
 * Customer info is auto-filled from trip.
 */
@Serializable
data class AddPaymentToTripRequest(
    @SerialName("trip_id") val tripId: Int,
    @SerialName("vehicle_id") val vehicleId: Int,
    @SerialName("driver_id") val driverId: Int,
    @SerialName("customer_id") val customerId: Int? = null,
    @SerialName("amount") val amount: Double,
    @SerialName("tds_amount") val tdsAmount: Double = 0.0,
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    @SerialName("payment_type") val paymentType: String,
    @SerialName("payment_mode") val paymentMode: String,
    @SerialName("payment_source") val paymentSource: String? = null,
    @SerialName("payment_date") val paymentDate: Long,
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("bank_name") val bankName: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("received_by") val receivedBy: String? = null,
    @SerialName("received_at_location") val receivedAtLocation: String? = null,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("due_date") val dueDate: Long? = null,
    @SerialName("reference_number") val referenceNumber: String? = null,
    @SerialName("bank_branch") val bankBranch: String? = null,
    @SerialName("account_number") val accountNumber: String? = null,
    @SerialName("ifsc_code") val ifscCode: String? = null,
    @SerialName("invoice_number") val invoiceNumber: String? = null,
    @SerialName("invoice_date") val invoiceDate: Long? = null,
    @SerialName("invoice_amount") val invoiceAmount: Double? = null,
    @SerialName("remarks") val remarks: String? = null
)

/**
 * Payment summary report response.
 * GET /trip-payments/summary
 */
@Serializable
data class PaymentSummaryReportResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: PaymentSummaryReportDto? = null
)

/**
 * Payment summary report data.
 *
 * Mirrors backend SummaryReportResponse exactly:
 * total_received, receipt_count, total_tds, total_discount, net_payments,
 * total_pending, total_cancelled,
 * by_mode ([{payment_mode, amount, count}]), by_type ([{payment_type, amount, count}]).
 * The backend does NOT emit period / by_status / monthly_trend.
 */
@Serializable
data class PaymentSummaryReportDto(
    @SerialName("total_received") val totalReceived: Double = 0.0,
    @SerialName("total_tds") val totalTds: Double = 0.0,
    @SerialName("total_discount") val totalDiscount: Double = 0.0,
    @SerialName("net_payments") val netPayments: Double = 0.0,
    // P5: backend SummaryReportResponse now carries total_pending + total_cancelled.
    @SerialName("total_pending") val totalPending: Double = 0.0,
    @SerialName("total_cancelled") val totalCancelled: Double = 0.0,
    @SerialName("receipt_count") val receiptCount: Int = 0,
    @SerialName("by_mode") val byMode: List<ModeBreakdownDto>? = null,
    @SerialName("by_type") val byType: List<TypeBreakdownDto>? = null
)

/**
 * Mode breakdown item for payment summary (backend ModeBreakdown).
 */
@Serializable
data class ModeBreakdownDto(
    @SerialName("payment_mode") val paymentMode: String? = null,
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("count") val count: Int = 0
)

/**
 * Type breakdown item for payment summary (backend TypeBreakdown).
 */
@Serializable
data class TypeBreakdownDto(
    @SerialName("payment_type") val paymentType: String? = null,
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("count") val count: Int = 0
)

/**
 * TDS report response.
 * GET /trip-payments/tds-report
 *
 * Backend returns `data` as a JSON ARRAY of flat per-payment rows
 * ([]map[string]any), NOT an object. Each row is one TDS-bearing payment.
 */
@Serializable
data class TdsReportResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: List<TdsReportItemDto>? = null
)

/**
 * One TDS report row (matches the backend repo map keys).
 */
@Serializable
data class TdsReportItemDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("receipt_number") val receiptNumber: String? = null,
    @SerialName("payment_date") val paymentDate: Long? = null,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("customer_gst") val customerGst: String? = null,
    @SerialName("trip_id") val tripId: Int? = null,
    @SerialName("trip_number") val tripNumber: String? = null,
    @SerialName("gross_amount") val grossAmount: Double = 0.0,
    @SerialName("tds_amount") val tdsAmount: Double = 0.0,
    @SerialName("net_amount") val netAmount: Double = 0.0
)
