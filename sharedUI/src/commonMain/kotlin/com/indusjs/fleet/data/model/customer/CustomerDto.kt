package com.indusjs.fleet.data.model.customer

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
 * Customer statistics DTO from API.
 */
@Serializable
data class CustomerStatisticsDto(
    @SerialName("customer_id")
    val customerId: Int,
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("active_trips")
    val activeTrips: Int = 0,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_pending_payment")
    val totalPendingPayment: Double = 0.0,
    @SerialName("total_received_payment")
    val totalReceivedPayment: Double = 0.0,
    @SerialName("average_trip_value")
    val averageTripValue: Double = 0.0,
    @SerialName("last_trip_date")
    val lastTripDate: String? = null
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
 * API response wrapper for customer list.
 */
@Serializable
data class CustomerListResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: List<CustomerDto>? = null,
    @SerialName("page")
    val page: Int? = null,
    @SerialName("per_page")
    val perPage: Int? = null,
    @SerialName("total")
    val total: Int? = null,
    @SerialName("total_pages")
    val totalPages: Int? = null
)

/**
 * API response wrapper for customer statistics.
 */
@Serializable
data class CustomerStatisticsResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: CustomerStatisticsDto? = null
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
 */
@Serializable
data class CustomerPendingPaymentDto(
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("vehicle_registration")
    val vehicleRegistration: String? = null,
    @SerialName("start_location")
    val startLocation: String? = null,
    @SerialName("end_location")
    val endLocation: String? = null,
    @SerialName("trip_date")
    val tripDate: String? = null,
    @SerialName("expected_trip_price")
    val expectedTripPrice: Double = 0.0,
    @SerialName("paid_trip_price")
    val paidTripPrice: Double = 0.0,
    @SerialName("pending_amount")
    val pendingAmount: Double = 0.0,
    @SerialName("days_overdue")
    val daysOverdue: Int = 0,
    @SerialName("state")
    val state: String? = null,
    @SerialName("payment_status")
    val paymentStatus: String? = null
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
    val data: List<CustomerPendingPaymentDto>? = null,
    @SerialName("total_pending")
    val totalPending: Double? = null,
    @SerialName("overdue_count")
    val overdueCount: Int? = null,
    @SerialName("page")
    val page: Int? = null,
    @SerialName("per_page")
    val perPage: Int? = null,
    @SerialName("total")
    val total: Int? = null,
    @SerialName("total_pages")
    val totalPages: Int? = null
)

// ============= Customer Payments DTOs =============

/**
 * Customer payment DTO from API.
 */
@Serializable
data class CustomerPaymentDto(
    @SerialName("id")
    val id: Int,
    @SerialName("trip_id")
    val tripId: Int? = null,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("payment_mode")
    val paymentMode: String? = null,
    @SerialName("payment_status")
    val paymentStatus: String? = null,
    @SerialName("payment_date")
    val paymentDate: String? = null,
    @SerialName("reference_number")
    val referenceNumber: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
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
    val data: List<CustomerPaymentDto>? = null,
    @SerialName("total_received")
    val totalReceived: Double? = null,
    @SerialName("page")
    val page: Int? = null,
    @SerialName("per_page")
    val perPage: Int? = null,
    @SerialName("total")
    val total: Int? = null,
    @SerialName("total_pages")
    val totalPages: Int? = null
)

// ============= Customer Payment Summary DTOs =============

/**
 * Payment breakdown by mode.
 */
@Serializable
data class PaymentByModeDto(
    @SerialName("mode")
    val mode: String,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("percentage")
    val percentage: Double = 0.0
)

/**
 * Monthly payment breakdown.
 */
@Serializable
data class MonthlyPaymentDto(
    @SerialName("month")
    val month: String,
    @SerialName("year")
    val year: Int,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("count")
    val count: Int = 0
)

/**
 * TDS summary.
 */
@Serializable
data class TdsSummaryDto(
    @SerialName("total_tds")
    val totalTds: Double = 0.0,
    @SerialName("tds_percentage")
    val tdsPercentage: Double = 0.0
)

/**
 * Customer payment summary DTO from API.
 */
@Serializable
data class CustomerPaymentSummaryDto(
    @SerialName("by_mode")
    val byMode: List<PaymentByModeDto>? = null,
    @SerialName("by_month")
    val byMonth: List<MonthlyPaymentDto>? = null,
    @SerialName("tds_summary")
    val tdsSummary: TdsSummaryDto? = null,
    @SerialName("total_amount")
    val totalAmount: Double = 0.0,
    @SerialName("total_payments")
    val totalPayments: Int = 0
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
    val data: CustomerPaymentSummaryDto? = null
)

// ============= Customer Financial Report DTOs =============

/**
 * Period breakdown item.
 */
@Serializable
data class PeriodBreakdownDto(
    @SerialName("period")
    val period: String,
    @SerialName("revenue")
    val revenue: Double = 0.0,
    @SerialName("costs")
    val costs: Double = 0.0,
    @SerialName("profit")
    val profit: Double = 0.0,
    @SerialName("trips")
    val trips: Int = 0
)

/**
 * Top vehicle item in financial report.
 */
@Serializable
data class TopVehicleDto(
    @SerialName("vehicle_id")
    val vehicleId: Int,
    @SerialName("vehicle_registration")
    val vehicleRegistration: String,
    @SerialName("trips")
    val trips: Int = 0,
    @SerialName("revenue")
    val revenue: Double = 0.0,
    @SerialName("costs")
    val costs: Double = 0.0,
    @SerialName("profit")
    val profit: Double = 0.0
)

/**
 * Trip summary in financial report.
 */
@Serializable
data class FinancialTripSummaryDto(
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("average_trip_value")
    val averageTripValue: Double = 0.0,
    @SerialName("total_distance")
    val totalDistance: Double = 0.0
)

/**
 * Customer financial report DTO from API.
 */
@Serializable
data class CustomerFinancialReportDto(
    @SerialName("customer_id")
    val customerId: Int,
    @SerialName("customer_name")
    val customerName: String? = null,
    @SerialName("period")
    val period: String? = null,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_costs")
    val totalCosts: Double = 0.0,
    @SerialName("net_profit")
    val netProfit: Double = 0.0,
    @SerialName("profit_margin")
    val profitMargin: Double = 0.0,
    @SerialName("trip_summary")
    val tripSummary: FinancialTripSummaryDto? = null,
    @SerialName("period_breakdown")
    val periodBreakdown: List<PeriodBreakdownDto>? = null,
    @SerialName("top_vehicles")
    val topVehicles: List<TopVehicleDto>? = null,
    @SerialName("payment_received")
    val paymentReceived: Double = 0.0,
    @SerialName("payment_pending")
    val paymentPending: Double = 0.0
)

/**
 * API response for customer financial report.
 */
@Serializable
data class CustomerFinancialReportResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: CustomerFinancialReportDto? = null
)
