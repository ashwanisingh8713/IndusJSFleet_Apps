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
    val updatedAt: String? = null,
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
