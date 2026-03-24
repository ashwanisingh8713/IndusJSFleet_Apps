package com.ijs.customer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Customer DTO for API communication.
 */
@Serializable
data class CustomerDto(
    @SerialName("id")
    val id: Int,
    @SerialName("company_name")
    val companyName: String,
    @SerialName("person_name")
    val personName: String,
    @SerialName("primary_contact")
    val primaryContact: String,
    @SerialName("secondary_contact")
    val secondaryContact: String? = null,
    @SerialName("company_address")
    val companyAddress: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("gst_number")
    val gstNumber: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("owner_id")
    val ownerId: Int? = null,
    @SerialName("created_by_id")
    val createdById: Int? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Customer summary DTO used in nested API responses.
 */
@Serializable
data class CustomerSummaryDto(
    @SerialName("id")
    val id: Int,
    @SerialName("company_name")
    val companyName: String? = null,
    @SerialName("person_name")
    val personName: String? = null,
    @SerialName("primary_contact")
    val primaryContact: String? = null
)

/**
 * Request to create a new customer.
 */
@Serializable
data class CreateCustomerRequest(
    @SerialName("company_name")
    val companyName: String,
    @SerialName("person_name")
    val personName: String,
    @SerialName("primary_contact")
    val primaryContact: String,
    @SerialName("secondary_contact")
    val secondaryContact: String? = null,
    @SerialName("company_address")
    val companyAddress: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("gst_number")
    val gstNumber: String? = null,
    @SerialName("notes")
    val notes: String? = null
)

/**
 * Request to update a customer.
 */
@Serializable
data class UpdateCustomerRequest(
    @SerialName("company_name")
    val companyName: String? = null,
    @SerialName("person_name")
    val personName: String? = null,
    @SerialName("primary_contact")
    val primaryContact: String? = null,
    @SerialName("secondary_contact")
    val secondaryContact: String? = null,
    @SerialName("company_address")
    val companyAddress: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("gst_number")
    val gstNumber: String? = null,
    @SerialName("notes")
    val notes: String? = null
)

/**
 * Customer statistics - trips breakdown.
 */
@Serializable
data class CustomerStatisticsTripsDto(
    @SerialName("total")
    val total: Int = 0,
    @SerialName("completed")
    val completed: Int = 0,
    @SerialName("on_route")
    val onRoute: Int = 0,
    @SerialName("planned")
    val planned: Int = 0,
    @SerialName("cancelled")
    val cancelled: Int = 0
)

/**
 * Customer statistics - financials breakdown.
 */
@Serializable
data class CustomerStatisticsFinancialsDto(
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_received")
    val totalReceived: Double = 0.0,
    @SerialName("total_pending")
    val totalPending: Double = 0.0,
    @SerialName("average_trip_value")
    val averageTripValue: Double = 0.0
)

/**
 * Customer statistics - performance breakdown.
 */
@Serializable
data class CustomerStatisticsPerformanceDto(
    @SerialName("on_time_delivery_rate")
    val onTimeDeliveryRate: Double = 0.0,
    @SerialName("relationship_since")
    val relationshipSince: String? = null,
    @SerialName("last_trip_date")
    val lastTripDate: String? = null
)

/**
 * Customer statistics data DTO from API.
 * New API response format with nested objects.
 */
@Serializable
data class CustomerStatisticsDataDto(
    @SerialName("customer")
    val customer: CustomerSummaryDto? = null,
    @SerialName("trips")
    val trips: CustomerStatisticsTripsDto? = null,
    @SerialName("financials")
    val financials: CustomerStatisticsFinancialsDto? = null,
    @SerialName("performance")
    val performance: CustomerStatisticsPerformanceDto? = null
)

/**
 * API response wrapper for customer operations.
 */
@Serializable
data class CustomerResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: CustomerDto? = null
)

/**
 * Nested data object for customer list response.
 */
@Serializable
data class CustomerListDataDto(
    @SerialName("items")
    val items: List<CustomerDto> = emptyList(),
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
 * API response wrapper for customer list.
 * Response: { success, message, data: { items, count, page, per_page, total_pages, has_more } }
 */
@Serializable
data class CustomerListResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: CustomerListDataDto? = null
) {
    // Convenience accessors
    val customers: List<CustomerDto>
        get() = data?.items ?: emptyList()

    val page: Int
        get() = data?.page ?: 1

    val perPage: Int
        get() = data?.perPage ?: 20

    val total: Int
        get() = data?.count ?: 0

    val totalPages: Int
        get() = data?.totalPages ?: 1

    val hasMore: Boolean
        get() = data?.hasMore ?: false
}

/**
 * API response wrapper for customer statistics.
 * Response: { success, message, data: { customer, trips, financials, performance } }
 */
@Serializable
data class CustomerStatisticsResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: CustomerStatisticsDataDto? = null
)

// ============= Customer Trips DTOs =============

/**
 * Customer trip DTO from API.
 */
@Serializable
data class CustomerTripDto(
    @SerialName("id")
    val id: Int,
    @SerialName("vehicle_id")
    val vehicleId: Int? = null,
    @SerialName("vehicle_registration")
    val vehicleRegistration: String? = null,
    @SerialName("driver_id")
    val driverId: Int? = null,
    @SerialName("driver_name")
    val driverName: String? = null,
    @SerialName("start_location")
    val startLocation: String? = null,
    @SerialName("end_location")
    val endLocation: String? = null,
    @SerialName("estimated_distance")
    val estimatedDistance: Double? = null,
    @SerialName("scheduled_date")
    val scheduledDate: String? = null,
    @SerialName("planned_start")
    val plannedStart: String? = null,
    @SerialName("planned_end")
    val plannedEnd: String? = null,
    @SerialName("actual_start")
    val actualStart: String? = null,
    @SerialName("actual_end")
    val actualEnd: String? = null,
    @SerialName("cargo_type")
    val cargoType: String? = null,
    @SerialName("expected_trip_price")
    val expectedTripPrice: Double? = null,
    @SerialName("paid_trip_price")
    val paidTripPrice: Double? = null,
    @SerialName("pending_amount")
    val pendingAmount: Double? = null,
    @SerialName("payment_status")
    val paymentStatus: String? = null,
    @SerialName("state")
    val state: String? = null,
    @SerialName("priority")
    val priority: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

/**
 * Customer trips summary DTO.
 */
@Serializable
data class CustomerTripsSummaryDto(
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("active_trips")
    val activeTrips: Int = 0,
    @SerialName("planned_trips")
    val plannedTrips: Int = 0,
    @SerialName("cancelled_trips")
    val cancelledTrips: Int = 0,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_pending")
    val totalPending: Double = 0.0
)

/**
 * Customer info in trips response.
 */
@Serializable
data class CustomerTripsCustomerInfoDto(
    @SerialName("id")
    val id: Int,
    @SerialName("company_name")
    val companyName: String? = null,
    @SerialName("person_name")
    val personName: String? = null,
    @SerialName("primary_contact")
    val primaryContact: String? = null
)

/**
 * Wrapper for customer trips data in API response.
 */
@Serializable
data class CustomerTripsDataDto(
    @SerialName("count")
    val count: Int = 0,
    @SerialName("customer")
    val customer: CustomerTripsCustomerInfoDto? = null,
    @SerialName("has_more")
    val hasMore: Boolean = false,
    @SerialName("items")
    val items: List<CustomerTripDto> = emptyList(),
    @SerialName("page")
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1,
    @SerialName("summary")
    val summary: CustomerTripsSummaryDto? = null
)

/**
 * API response for customer trips.
 */
@Serializable
data class CustomerTripsResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: CustomerTripsDataDto? = null
) {
    // Convenience accessors for backward compatibility
    val trips: List<CustomerTripDto>
        get() = data?.items ?: emptyList()

    val summary: CustomerTripsSummaryDto?
        get() = data?.summary

    val page: Int?
        get() = data?.page

    val perPage: Int?
        get() = data?.perPage

    val total: Int?
        get() = data?.count

    val totalPages: Int?
        get() = data?.totalPages

    val hasMore: Boolean
        get() = data?.hasMore ?: false
}

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
 * Response: { success, message, data: { customer, items, summary, pagination... } }
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
 * Response: { success, message, data: { customer, items, summary, pagination... } }
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
 * Response: { success, message, data: { customer, totals, by_mode, by_type, by_month, date_range } }
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

// ============= Customer Financial Report DTOs =============

/**
 * Financial summary DTO.
 */
@Serializable
data class FinancialSummaryDto(
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_costs")
    val totalCosts: Double = 0.0,
    @SerialName("gross_profit")
    val grossProfit: Double = 0.0,
    @SerialName("profit_margin")
    val profitMargin: Double = 0.0,
    @SerialName("total_received")
    val totalReceived: Double = 0.0,
    @SerialName("total_pending")
    val totalPending: Double = 0.0
)

/**
 * Cost breakdown DTO.
 */
@Serializable
data class CostBreakdownDto(
    @SerialName("fuel_costs")
    val fuelCosts: Double = 0.0,
    @SerialName("toll_costs")
    val tollCosts: Double = 0.0,
    @SerialName("driver_costs")
    val driverCosts: Double = 0.0,
    @SerialName("maintenance_costs")
    val maintenanceCosts: Double = 0.0,
    @SerialName("other_costs")
    val otherCosts: Double = 0.0
)

/**
 * Monthly trend item.
 */
@Serializable
data class MonthlyTrendDto(
    @SerialName("month")
    val month: String,
    @SerialName("trips")
    val trips: Int = 0,
    @SerialName("revenue")
    val revenue: Double = 0.0,
    @SerialName("costs")
    val costs: Double = 0.0,
    @SerialName("profit")
    val profit: Double = 0.0,
    @SerialName("margin")
    val margin: Double = 0.0
)

/**
 * Nested data object for customer financial report response.
 */
@Serializable
data class CustomerFinancialReportDataDto(
    @SerialName("customer")
    val customer: CustomerSummaryDto? = null,
    @SerialName("summary")
    val summary: FinancialSummaryDto? = null,
    @SerialName("cost_breakdown")
    val costBreakdown: CostBreakdownDto? = null,
    @SerialName("monthly_trend")
    val monthlyTrend: List<MonthlyTrendDto> = emptyList(),
    @SerialName("date_range")
    val dateRange: DateRangeDto? = null,
    @SerialName("period")
    val period: String? = null
)

/**
 * API response for customer financial report.
 * Response: { success, message, data: { customer, summary, cost_breakdown, monthly_trend, date_range, period } }
 */
@Serializable
data class CustomerFinancialReportResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: CustomerFinancialReportDataDto? = null
)
