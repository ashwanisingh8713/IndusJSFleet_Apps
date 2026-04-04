package com.ijs.customer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============= Customer Pending Payments DTOs =============

/**
 * Customer pending payment DTO from API.
 * Maps to items in /customers/:id/pending-payments response.
 */
@Serializable
data class CustomerPendingPaymentDto(
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("scheduled_date")
    val scheduledDate: String? = null,
    @SerialName("start_location")
    val startLocation: String? = null,
    @SerialName("end_location")
    val endLocation: String? = null,
    @SerialName("vehicle_number")
    val vehicleNumber: String? = null,
    @SerialName("expected_price")
    val expectedPrice: Double = 0.0,
    @SerialName("paid_amount")
    val paidAmount: Double = 0.0,
    @SerialName("pending_amount")
    val pendingAmount: Double = 0.0,
    @SerialName("days_overdue")
    val daysOverdue: Int = 0,
    @SerialName("payment_status")
    val paymentStatus: String? = null
)

/**
 * Summary for pending payments.
 */
@Serializable
data class CustomerPendingPaymentsSummaryDto(
    @SerialName("total_pending")
    val totalPending: Double = 0.0,
    @SerialName("pending_count")
    val pendingCount: Int = 0
)

/**
 * Nested data object for customer pending payments response.
 */
@Serializable
data class CustomerPendingPaymentsDataDto(
    @SerialName("customer")
    val customer: CustomerSummaryDto? = null,
    @SerialName("items")
    val items: List<CustomerPendingPaymentDto> = emptyList(),
    @SerialName("summary")
    val summary: CustomerPendingPaymentsSummaryDto? = null,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("page")
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1,
    @SerialName("has_more")
    val hasMore: Boolean = false
)

/**
 * API response for customer pending payments.
 */
@Serializable
data class CustomerPendingPaymentsResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: CustomerPendingPaymentsDataDto? = null
) {
    val payments: List<CustomerPendingPaymentDto>
        get() = data?.items ?: emptyList()
    val totalPending: Double
        get() = data?.summary?.totalPending ?: 0.0
    val overdueCount: Int
        get() = payments.count { it.daysOverdue > 0 }
    val page: Int
        get() = data?.page ?: 1
    val totalPages: Int
        get() = data?.totalPages ?: 1
    val total: Int
        get() = data?.count ?: 0
    val hasMore: Boolean
        get() = data?.hasMore ?: false
}

// ============= Customer Payments DTOs =============

/**
 * Customer payment DTO from API.
 * Maps to items in /customers/:id/payments response.
 */
@Serializable
data class CustomerPaymentDto(
    @SerialName("id")
    val id: Int,
    @SerialName("trip_id")
    val tripId: Int? = null,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("tds_amount")
    val tdsAmount: Double = 0.0,
    @SerialName("discount_amount")
    val discountAmount: Double = 0.0,
    @SerialName("net_amount")
    val netAmount: Double = 0.0,
    @SerialName("payment_type")
    val paymentType: String? = null,
    @SerialName("payment_mode")
    val paymentMode: String? = null,
    @SerialName("payment_source")
    val paymentSource: String? = null,
    @SerialName("payment_status")
    val paymentStatus: String? = null,
    @SerialName("payment_date")
    val paymentDate: String? = null,
    @SerialName("receipt_number")
    val receiptNumber: String? = null,
    @SerialName("financial_year")
    val financialYear: String? = null,
    @SerialName("financial_month")
    val financialMonth: String? = null,
    @SerialName("reference_number")
    val referenceNumber: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

/**
 * Summary for customer payments.
 */
@Serializable
data class CustomerPaymentsSummaryDto(
    @SerialName("total_received")
    val totalReceived: Double = 0.0,
    @SerialName("total_pending")
    val totalPending: Double = 0.0,
    @SerialName("payment_count")
    val paymentCount: Int = 0
)

/**
 * Nested data object for customer payments response.
 */
@Serializable
data class CustomerPaymentsDataDto(
    @SerialName("customer")
    val customer: CustomerSummaryDto? = null,
    @SerialName("items")
    val items: List<CustomerPaymentDto> = emptyList(),
    @SerialName("summary")
    val summary: CustomerPaymentsSummaryDto? = null,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("page")
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1,
    @SerialName("has_more")
    val hasMore: Boolean = false
)

/**
 * API response for customer payments.
 */
@Serializable
data class CustomerPaymentsResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: CustomerPaymentsDataDto? = null
) {
    val payments: List<CustomerPaymentDto>
        get() = data?.items ?: emptyList()
    val totalReceived: Double
        get() = data?.summary?.totalReceived ?: 0.0
    val totalPending: Double
        get() = data?.summary?.totalPending ?: 0.0
    val page: Int
        get() = data?.page ?: 1
    val totalPages: Int
        get() = data?.totalPages ?: 1
    val total: Int
        get() = data?.count ?: 0
    val hasMore: Boolean
        get() = data?.hasMore ?: false
}

// ============= Customer Payment Summary DTOs =============

/**
 * Payment breakdown by mode (cash, upi, bank_transfer).
 */
@Serializable
data class PaymentByModeDto(
    @SerialName("payment_mode")
    val paymentMode: String? = null,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("count")
    val count: Int = 0
)

/**
 * Payment breakdown by type (advance, partial, final).
 */
@Serializable
data class PaymentByTypeDto(
    @SerialName("payment_type")
    val paymentType: String? = null,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("count")
    val count: Int = 0
)

/**
 * Monthly payment breakdown.
 */
@Serializable
data class MonthlyPaymentDto(
    @SerialName("month")
    val month: String,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("count")
    val count: Int = 0
)

/**
 * Payment totals.
 */
@Serializable
data class PaymentTotalsDto(
    @SerialName("gross_amount")
    val grossAmount: Double = 0.0,
    @SerialName("tds_amount")
    val tdsAmount: Double = 0.0,
    @SerialName("discount_amount")
    val discountAmount: Double = 0.0,
    @SerialName("net_amount")
    val netAmount: Double = 0.0
)

/**
 * Date range for payment summary.
 */
@Serializable
data class DateRangeDto(
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null
)

/**
 * Nested data object for customer payment summary response.
 */
@Serializable
data class CustomerPaymentSummaryDataDto(
    @SerialName("customer")
    val customer: CustomerSummaryDto? = null,
    @SerialName("totals")
    val totals: PaymentTotalsDto? = null,
    @SerialName("by_mode")
    val byMode: List<PaymentByModeDto> = emptyList(),
    @SerialName("by_type")
    val byType: List<PaymentByTypeDto> = emptyList(),
    @SerialName("by_month")
    val byMonth: List<MonthlyPaymentDto> = emptyList(),
    @SerialName("date_range")
    val dateRange: DateRangeDto? = null
)

/**
 * API response for customer payment summary.
 */
@Serializable
data class CustomerPaymentSummaryResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: CustomerPaymentSummaryDataDto? = null
)

