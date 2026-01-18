package com.indusjs.fleet.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for creating a trip payment.
 * POST /trip-payments or POST /trips/{id}/payments
 */
@Serializable
data class CreateTripPaymentRequest(
    @SerialName("trip_id") val tripId: Int? = null,
    @SerialName("vehicle_id") val vehicleId: Int? = null,
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
    @SerialName("payment_date") val paymentDate: String,
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("bank_name") val bankName: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("received_by") val receivedBy: String? = null,
    @SerialName("received_at_location") val receivedAtLocation: String? = null
)

/**
 * Request body for updating a trip payment.
 * PUT /trip-payments/{id}
 */
@Serializable
data class UpdateTripPaymentRequest(
    @SerialName("amount") val amount: Double? = null,
    @SerialName("tds_amount") val tdsAmount: Double? = null,
    @SerialName("discount_amount") val discountAmount: Double? = null,
    @SerialName("payment_type") val paymentType: String? = null,
    @SerialName("payment_mode") val paymentMode: String? = null,
    @SerialName("payment_source") val paymentSource: String? = null,
    @SerialName("payment_date") val paymentDate: String? = null,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("bank_name") val bankName: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("received_by") val receivedBy: String? = null,
    @SerialName("received_at_location") val receivedAtLocation: String? = null
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
    @SerialName("payment_date") val paymentDate: String,
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("bank_name") val bankName: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("received_by") val receivedBy: String? = null,
    @SerialName("received_at_location") val receivedAtLocation: String? = null
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
 */
@Serializable
data class PaymentSummaryReportDto(
    @SerialName("period") val period: PaymentPeriodDto? = null,
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    @SerialName("total_tds") val totalTds: Double = 0.0,
    @SerialName("total_discount") val totalDiscount: Double = 0.0,
    @SerialName("total_net_amount") val totalNetAmount: Double = 0.0,
    @SerialName("payment_count") val paymentCount: Int = 0,
    @SerialName("by_mode") val byMode: List<PaymentBreakdownDto>? = null,
    @SerialName("by_type") val byType: List<PaymentBreakdownDto>? = null,
    @SerialName("by_status") val byStatus: List<PaymentBreakdownDto>? = null,
    @SerialName("monthly_trend") val monthlyTrend: List<MonthlyPaymentDto>? = null
)

/**
 * Period info for reports.
 */
@Serializable
data class PaymentPeriodDto(
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null
)

/**
 * Breakdown item for payment reports.
 */
@Serializable
data class PaymentBreakdownDto(
    @SerialName("key") val key: String,
    @SerialName("label") val label: String? = null,
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("count") val count: Int = 0,
    @SerialName("percentage") val percentage: Double = 0.0
)

/**
 * Monthly payment data for trend chart.
 */
@Serializable
data class MonthlyPaymentDto(
    @SerialName("month") val month: String,
    @SerialName("year") val year: Int,
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("count") val count: Int = 0
)

/**
 * TDS report response.
 * GET /trip-payments/tds-report
 */
@Serializable
data class TdsReportResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: TdsReportDto? = null
)

/**
 * TDS report data.
 */
@Serializable
data class TdsReportDto(
    @SerialName("financial_year") val financialYear: String,
    @SerialName("total_tds") val totalTds: Double = 0.0,
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    @SerialName("by_customer") val byCustomer: List<CustomerTdsDto>? = null,
    @SerialName("by_quarter") val byQuarter: List<QuarterlyTdsDto>? = null
)

/**
 * Customer-wise TDS data.
 */
@Serializable
data class CustomerTdsDto(
    @SerialName("customer_id") val customerId: Int,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("customer_gst") val customerGst: String? = null,
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    @SerialName("total_tds") val totalTds: Double = 0.0,
    @SerialName("payment_count") val paymentCount: Int = 0
)

/**
 * Quarterly TDS data.
 */
@Serializable
data class QuarterlyTdsDto(
    @SerialName("quarter") val quarter: String,
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    @SerialName("total_tds") val totalTds: Double = 0.0,
    @SerialName("payment_count") val paymentCount: Int = 0
)
