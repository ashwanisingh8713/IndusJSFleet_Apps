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
    val createdAt: Long? = null,
    @SerialName("updated_at")
    val updatedAt: Long? = null,
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("pending_payment")
    val pendingPayment: Double = 0.0
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
 * Customer statistics - summary block.
 * Backend keys: counts are int, money is Double.
 */
@Serializable
data class CustomerStatisticsSummaryDto(
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("cancelled_trips")
    val cancelledTrips: Int = 0,
    @SerialName("active_trips")
    val activeTrips: Int = 0,
    @SerialName("planned_trips")
    val plannedTrips: Int = 0,
    @SerialName("total_revenue")
    val totalRevenue: Double = 0.0,
    @SerialName("total_paid")
    val totalPaid: Double = 0.0,
    @SerialName("total_pending")
    val totalPending: Double = 0.0,
    @SerialName("collection_rate")
    val collectionRate: Double = 0.0,
    @SerialName("total_trip_costs")
    val totalTripCosts: Double = 0.0,
    // Additive backend field: driver costs attributed to this customer's trips (net of deductions).
    // net_profit already subtracts both trip and driver costs server-side.
    @SerialName("total_driver_costs")
    val totalDriverCosts: Double = 0.0,
    @SerialName("net_profit")
    val netProfit: Double = 0.0
)

/**
 * Customer statistics - trips grouped by state.
 */
@Serializable
data class CustomerStatisticsTripsByStateDto(
    @SerialName("completed")
    val completed: Int = 0,
    @SerialName("cancelled")
    val cancelled: Int = 0,
    // Backend emits canonical trip state key "in_progress" (was "on_route", which is not a valid backend state).
    @SerialName("in_progress")
    val inProgress: Int = 0,
    @SerialName("planned")
    val planned: Int = 0
)

/**
 * Customer statistics - monthly revenue item.
 */
@Serializable
data class CustomerStatisticsMonthlyRevenueDto(
    @SerialName("month")
    val month: String? = null,
    @SerialName("trips")
    val trips: Int = 0,
    @SerialName("revenue")
    val revenue: Double = 0.0,
    @SerialName("collected")
    val collected: Double = 0.0
)

/**
 * Customer statistics data DTO from API.
 * Matches backend GetCustomerStatistics response shape.
 */
@Serializable
data class CustomerStatisticsDataDto(
    @SerialName("summary")
    val summary: CustomerStatisticsSummaryDto? = null,
    @SerialName("trips_by_state")
    val tripsByState: CustomerStatisticsTripsByStateDto? = null,
    @SerialName("payment_by_mode")
    val paymentByMode: Map<String, Double> = emptyMap(),
    @SerialName("monthly_revenue")
    val monthlyRevenue: List<CustomerStatisticsMonthlyRevenueDto> = emptyList(),
    @SerialName("start_date")
    val startDate: Long? = null,
    @SerialName("end_date")
    val endDate: Long? = null
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
 * Response: { success, message, data: { summary, trips_by_state, payment_by_mode, monthly_revenue, start_date, end_date } }
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
