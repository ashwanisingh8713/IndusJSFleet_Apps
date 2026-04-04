package com.ijs.customer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

