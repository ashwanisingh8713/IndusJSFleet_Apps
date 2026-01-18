package com.indusjs.fleet.data.model.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Trip payment DTO from API response.
 */
@Serializable
data class TripPaymentDto(
    @SerialName("id") val id: Int,
    @SerialName("trip_id") val tripId: Int,
    @SerialName("vehicle_id") val vehicleId: Int? = null,
    @SerialName("driver_id") val driverId: Int? = null,
    @SerialName("customer_id") val customerId: Int? = null,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("customer_contact") val customerContact: String? = null,
    @SerialName("customer_company") val customerCompany: String? = null,
    @SerialName("customer_gst") val customerGst: String? = null,
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("tds_amount") val tdsAmount: Double = 0.0,
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    @SerialName("net_amount") val netAmount: Double = 0.0,
    @SerialName("payment_type") val paymentType: String? = null,
    @SerialName("payment_mode") val paymentMode: String? = null,
    @SerialName("payment_source") val paymentSource: String? = null,
    @SerialName("payment_date") val paymentDate: String? = null,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("bank_name") val bankName: String? = null,
    @SerialName("receipt_number") val receiptNumber: String? = null,
    @SerialName("financial_year") val financialYear: String? = null,
    @SerialName("financial_month") val financialMonth: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("received_by") val receivedBy: String? = null,
    @SerialName("received_at_location") val receivedAtLocation: String? = null,
    @SerialName("owner_id") val ownerId: Int? = null,
    @SerialName("created_by") val createdBy: Int? = null,
    @SerialName("created_by_user") val createdByUser: CreatedByUserDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    // Embedded trip info
    @SerialName("trip") val trip: TripPaymentTripInfoDto? = null
)

/**
 * Created by user info DTO.
 */
@Serializable
data class CreatedByUserDto(
    @SerialName("id") val id: Int,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("role") val role: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { "Unknown" }
}

/**
 * Trip info embedded in payment response.
 */
@Serializable
data class TripPaymentTripInfoDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("vehicle_id") val vehicleId: Int? = null,
    @SerialName("vehicle") val vehicle: TripPaymentVehicleDto? = null,
    @SerialName("driver_id") val driverId: Int? = null,
    @SerialName("driver") val driver: TripPaymentDriverDto? = null,
    @SerialName("start_location") val startLocation: String? = null,
    @SerialName("end_location") val endLocation: String? = null,
    @SerialName("expected_trip_price") val expectedTripPrice: Double? = null,
    @SerialName("paid_trip_price") val paidTripPrice: Double? = null,
    @SerialName("pending_amount") val pendingAmount: Double? = null,
    @SerialName("state") val state: String? = null
)

/**
 * Vehicle info in payment response.
 */
@Serializable
data class TripPaymentVehicleDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("registration_number") val registrationNumber: String? = null,
    @SerialName("make") val make: String? = null,
    @SerialName("model") val model: String? = null
)

/**
 * Driver info in payment response.
 */
@Serializable
data class TripPaymentDriverDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { "Unknown" }
}

/**
 * API response for single trip payment.
 */
@Serializable
data class TripPaymentResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: TripPaymentDto? = null
)

/**
 * API response for trip payment list.
 * Response structure: { success, message, data: { count, has_more, items: [...], summary, page, per_page, total, total_pages } }
 */
@Serializable
data class TripPaymentListResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: TripPaymentListDataDto? = null
)

/**
 * Nested data object in trip payment list response.
 */
@Serializable
data class TripPaymentListDataDto(
    @SerialName("count") val count: Int = 0,
    @SerialName("has_more") val hasMore: Boolean = false,
    @SerialName("items") val items: List<TripPaymentDto> = emptyList(),
    @SerialName("summary") val summary: TripPaymentSummaryDto? = null,
    @SerialName("page") val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 20,
    @SerialName("total") val total: Int = 0,
    @SerialName("total_pages") val totalPages: Int = 1
)

/**
 * Payment summary DTO.
 */
@Serializable
data class TripPaymentSummaryDto(
    @SerialName("total_received") val totalReceived: Double = 0.0,
    @SerialName("total_pending") val totalPending: Double = 0.0,
    @SerialName("total_cancelled") val totalCancelled: Double = 0.0,
    @SerialName("total_tds") val totalTds: Double = 0.0,
    @SerialName("total_discount") val totalDiscount: Double = 0.0,
    @SerialName("total_net_amount") val totalNetAmount: Double = 0.0,
    @SerialName("payment_count") val paymentCount: Int = 0,
    @SerialName("received_count") val receivedCount: Int = 0,
    @SerialName("pending_count") val pendingCount: Int = 0,
    @SerialName("this_month_total") val thisMonthTotal: Double = 0.0,
    @SerialName("by_mode") val byMode: Map<String, Double>? = null,
    @SerialName("by_type") val byType: Map<String, Double>? = null
)

/**
 * Trip payments history response (for GET /trips/{id}/payments).
 */
@Serializable
data class TripPaymentsHistoryResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: TripPaymentsHistoryDataDto? = null
)

/**
 * Trip payments history data.
 */
@Serializable
data class TripPaymentsHistoryDataDto(
    @SerialName("trip_id") val tripId: Int,
    @SerialName("trip_info") val tripInfo: TripPaymentTripInfoDto? = null,
    @SerialName("expected_trip_price") val expectedTripPrice: Double = 0.0,
    @SerialName("paid_trip_price") val paidTripPrice: Double = 0.0,
    @SerialName("pending_amount") val pendingAmount: Double = 0.0,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("payment_count") val paymentCount: Int = 0,
    @SerialName("payments") val payments: List<TripPaymentDto> = emptyList(),
    @SerialName("page") val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 20,
    @SerialName("total") val total: Int = 0,
    @SerialName("total_pages") val totalPages: Int = 1
)
