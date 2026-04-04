package com.ijs.trip.payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Trip payment DTO from API response.
 * Updated to support both flat fields (list response) and nested objects (detail response).
 */
@Serializable
data class TripPaymentDto(
    @SerialName("id") val id: Int,
    // trip_id is optional - in detail response it's inside the nested trip object
    @SerialName("trip_id") val tripId: Int? = null,
    @SerialName("vehicle_id") val vehicleId: Int? = null,
    @SerialName("driver_id") val driverId: Int? = null,
    @SerialName("customer_id") val customerId: Int? = null,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("customer_contact") val customerContact: String? = null,
    @SerialName("customer_company") val customerCompany: String? = null,
    @SerialName("customer_gst") val customerGst: String? = null,
    // New flat fields from updated API (for list response)
    @SerialName("vehicle_registration_number") val vehicleRegistrationNumber: String? = null,
    @SerialName("trip_start_location") val tripStartLocation: String? = null,
    @SerialName("trip_end_location") val tripEndLocation: String? = null,
    @SerialName("trip_scheduled_date") val tripScheduledDate: String? = null,
    @SerialName("trip_start_time") val tripStartTime: String? = null,
    @SerialName("trip_delivery_date") val tripDeliveryDate: String? = null,
    @SerialName("trip_delivery_time") val tripDeliveryTime: String? = null,
    @SerialName("trip_state") val tripState: String? = null,
    // Payment amounts
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("tds_amount") val tdsAmount: Double = 0.0,
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    @SerialName("net_amount") val netAmount: Double = 0.0,
    @SerialName("payment_type") val paymentType: String? = null,
    @SerialName("payment_mode") val paymentMode: String? = null,
    @SerialName("payment_source") val paymentSource: String? = null,
    @SerialName("payment_date") val paymentDate: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("payment_status") val paymentStatus: String? = null,
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("reference_number") val referenceNumber: String? = null,
    @SerialName("bank_name") val bankName: String? = null,
    @SerialName("bank_branch") val bankBranch: String? = null,
    @SerialName("account_number") val accountNumber: String? = null,
    @SerialName("ifsc_code") val ifscCode: String? = null,
    @SerialName("invoice_number") val invoiceNumber: String? = null,
    @SerialName("invoice_date") val invoiceDate: String? = null,
    @SerialName("invoice_amount") val invoiceAmount: Double = 0.0,
    @SerialName("receipt_number") val receiptNumber: String? = null,
    @SerialName("financial_year") val financialYear: String? = null,
    @SerialName("financial_month") val financialMonth: String? = null,
    @SerialName("financial_quarter") val financialQuarter: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("remarks") val remarks: String? = null,
    @SerialName("received_by") val receivedBy: String? = null,
    @SerialName("received_at_location") val receivedAtLocation: String? = null,
    @SerialName("owner_id") val ownerId: Int? = null,
    @SerialName("created_by") val createdBy: Int? = null,
    @SerialName("created_by_user") val createdByUser: CreatedByUserDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    // Nested objects for detail response
    @SerialName("trip") val trip: TripPaymentTripInfoDto? = null,
    @SerialName("vehicle") val vehicle: TripPaymentVehicleDto? = null,
    @SerialName("driver") val driver: TripPaymentDriverDto? = null,
    @SerialName("customer") val customer: TripPaymentCustomerDto? = null,
    @SerialName("related_payments") val relatedPayments: List<TripPaymentDto>? = null
) {
    /**
     * Get the effective trip ID from either flat field or nested object.
     */
    val effectiveTripId: Int
        get() = tripId ?: trip?.id ?: 0
}

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
    @SerialName("vehicle_registration") val vehicleRegistration: String? = null,
    @SerialName("driver_id") val driverId: Int? = null,
    @SerialName("driver") val driver: TripPaymentDriverDto? = null,
    @SerialName("driver_name") val driverName: String? = null,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("start_location") val startLocation: String? = null,
    @SerialName("end_location") val endLocation: String? = null,
    @SerialName("planned_start") val plannedStart: String? = null,
    @SerialName("planned_end") val plannedEnd: String? = null,
    @SerialName("scheduled_date") val scheduledDate: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("delivery_date") val deliveryDate: String? = null,
    @SerialName("delivery_time") val deliveryTime: String? = null,
    @SerialName("expected_trip_price") val expectedTripPrice: Double? = null,
    @SerialName("paid_trip_price") val paidTripPrice: Double? = null,
    @SerialName("pending_amount") val pendingAmount: Double? = null,
    @SerialName("payment_status") val paymentStatus: String? = null,
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
    @SerialName("model") val model: String? = null,
    @SerialName("vehicle_type") val vehicleType: String? = null
)

/**
 * Driver info in payment response.
 */
@Serializable
data class TripPaymentDriverDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("mobile") val mobile: String? = null,
    @SerialName("license_number") val licenseNumber: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { "Unknown" }
}

/**
 * Customer info in payment response (for detail view).
 */
@Serializable
data class TripPaymentCustomerDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("company_name") val companyName: String? = null,
    @SerialName("person_name") val personName: String? = null,
    @SerialName("primary_contact") val primaryContact: String? = null,
    @SerialName("secondary_contact") val secondaryContact: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("gst_number") val gstNumber: String? = null,
    @SerialName("company_address") val companyAddress: String? = null
) {
    val displayName: String
        get() = companyName?.ifBlank { null } ?: personName?.ifBlank { null } ?: "Unknown Customer"
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
