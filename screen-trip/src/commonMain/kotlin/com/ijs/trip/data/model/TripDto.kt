package com.ijs.trip.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Trip DTO for API communication.
 * Uses snake_case to match API response format.
 */
@Serializable
data class TripDto(
    @SerialName("id")
    val id: Int,
    @SerialName("trip_number")
    val tripNumber: String? = null,
    @SerialName("vehicle_id")
    val vehicleId: Int,
    @SerialName("vehicle")
    val vehicle: TripVehicleDto? = null,
    @SerialName("driver_id")
    val driverId: Int,
    @SerialName("driver")
    val driver: TripDriverDto? = null,
    @SerialName("state")
    val state: String = "planned",
    @SerialName("start_location")
    val startLocation: String? = null,
    @SerialName("start_lat")
    val startLat: Double? = null,
    @SerialName("start_lng")
    val startLng: Double? = null,
    @SerialName("end_location")
    val endLocation: String? = null,
    @SerialName("end_lat")
    val endLat: Double? = null,
    @SerialName("end_lng")
    val endLng: Double? = null,
    @SerialName("current_lat")
    val currentLat: Double? = null,
    @SerialName("current_lng")
    val currentLng: Double? = null,
    @SerialName("estimated_distance")
    val estimatedDistance: Double? = null,
    @SerialName("actual_distance")
    val actualDistance: Double? = null,
    // All timestamps are UTC epoch-millis (JSON numbers). See UTC_MILLIS_APP_MIGRATION_PLAN.md.
    @SerialName("scheduled_date")
    val scheduledDate: Long? = null,
    @SerialName("start_time")
    val startTime: Long? = null,
    @SerialName("delivery_date")
    val deliveryDate: Long? = null,
    @SerialName("delivery_time")
    val deliveryTime: Long? = null,
    @SerialName("planned_start")
    val plannedStart: Long? = null,
    @SerialName("planned_end")
    val plannedEnd: Long? = null,
    @SerialName("actual_start")
    val actualStart: Long? = null,
    @SerialName("actual_end")
    val actualEnd: Long? = null,
    @SerialName("cargo_type")
    val cargoType: String? = null,
    @SerialName("cargo_description")
    val cargoDescription: String? = null,
    @SerialName("cargo_weight")
    val cargoWeight: Double? = null,
    @SerialName("cargo_loading_weight")
    val cargoLoadingWeight: Double? = null,
    @SerialName("cargo_unloading_weight")
    val cargoUnloadingWeight: Double? = null,
    @SerialName("vehicle_weight")
    val vehicleWeight: Double? = null,
    @SerialName("weight_unit")
    val weightUnit: String? = null,
    // Fuel info
    @SerialName("fuel_type")
    val fuelType: String? = null,
    @SerialName("filled_fuel_quantity")
    val filledFuelQuantity: Double? = null,
    @SerialName("used_fuel_quantity")
    val usedFuelQuantity: Double? = null,
    @SerialName("fuel_rate")
    val fuelRate: Double? = null,
    @SerialName("km_per_liter")
    val kmPerLiter: Double? = null,
    // Pricing - API returns expected_trip_price
    @SerialName("purchase_price")
    val purchasePrice: Double? = null,
    @SerialName("selling_value")
    val sellingValue: Double? = null,
    @SerialName("estimated_expense")
    val estimatedExpense: Double? = null,
    @SerialName("expected_trip_price")
    val expectedTripPrice: Double? = null,
    // Payment
    @SerialName("payment_status")
    val paymentStatus: String? = null,
    @SerialName("partial_payment_amount")
    val partialPaymentAmount: Double? = null,
    @SerialName("pending_amount")
    val pendingAmount: Double? = null,
    @SerialName("payment_received_date")
    val paymentReceivedDate: Long? = null,
    @SerialName("payment_mode")
    val paymentMode: String? = null,
    // Customer - backend sends customer_id plus snapshot fields customer_name / customer_contact
    // (and customer_email / customer_address). There is no embedded `customer` object.
    @SerialName("customer_id")
    val customerId: Int? = null,
    @SerialName("customer_name")
    val customerName: String? = null,
    @SerialName("customer_contact")
    val customerContact: String? = null,
    @SerialName("customer_email")
    val customerEmail: String? = null,
    @SerialName("customer_address")
    val customerAddress: String? = null,
    // Consignee / delivery (receiver) — distinct from the billing customer above.
    @SerialName("delivery_address")
    val deliveryAddress: String? = null,
    @SerialName("delivery_person_name")
    val deliveryPersonName: String? = null,
    @SerialName("delivery_contact_number")
    val deliveryContactNumber: String? = null,
    @SerialName("priority")
    val priority: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("owner_id")
    val ownerId: Int? = null,
    @SerialName("created_by_id")
    val createdById: Int? = null,
    @SerialName("created_at")
    val createdAt: Long? = null,
    @SerialName("updated_at")
    val updatedAt: Long? = null,
    // Cost summary - included in List/Get Trip responses
    @SerialName("cost_summary")
    val costSummary: TripCostSummaryEmbeddedDto? = null,
    // Display info - included in Get Trip response (detail view)
    @SerialName("display_info")
    val displayInfo: TripDisplayInfoDto? = null,
    // List-level display fields (for List Trips response)
    // These flat fields are returned by v2 API for list view
    @SerialName("vehicle_number")
    val vehicleNumber: String? = null,
    @SerialName("driver_name")
    val driverName: String? = null,
    @SerialName("state_label")
    val stateLabel: String? = null,
    // Distance display fields (list response)
    @SerialName("distance_display")
    val distanceDisplay: String? = null,
    @SerialName("estimated_distance_label")
    val estimatedDistanceLabel: String? = null,
    // Duration display fields (list response)
    @SerialName("duration_display")
    val durationDisplay: String? = null,
    @SerialName("estimated_duration_minutes")
    val estimatedDurationMinutes: Long? = null,
    @SerialName("estimated_duration_label")
    val estimatedDurationLabel: String? = null,
    // Cargo and cost fields
    @SerialName("cargo_type_label")
    val cargoTypeLabel: String? = null,
    @SerialName("total_cost")
    val totalCost: Double? = null,
    @SerialName("total_cost_label")
    val totalCostLabel: String? = null,
    @SerialName("has_costs")
    val hasCosts: Boolean? = null
) {
    val tripPrice: Double?
        get() = expectedTripPrice ?: sellingValue
}

/**
 * Cost summary embedded in Trip response.
 */
@Serializable
data class TripCostSummaryEmbeddedDto(
    @SerialName("total_cost")
    val totalCost: Double = 0.0,
    @SerialName("fuel_cost")
    val fuelCost: Double = 0.0,
    @SerialName("toll_cost")
    val tollCost: Double = 0.0,
    @SerialName("driver_allowance")
    val driverAllowance: Double = 0.0,
    @SerialName("parking_cost")
    val parkingCost: Double = 0.0,
    @SerialName("loading_charges")
    val loadingCharges: Double = 0.0,
    @SerialName("unloading_charges")
    val unloadingCharges: Double = 0.0,
    @SerialName("other_costs")
    val otherCosts: Double = 0.0,
    @SerialName("cost_count")
    val costCount: Int = 0,
    @SerialName("cost_by_type")
    val costByType: Map<String, Double> = emptyMap(),
    @SerialName("last_updated")
    val lastUpdated: Long? = null
)

/**
 * Display info object with state-based fields from API.
 * Returned in Get Trip detail response.
 */
@Serializable
data class TripDisplayInfoDto(
    @SerialName("distance_info")
    val distanceInfo: DistanceInfoDto? = null,
    @SerialName("duration_info")
    val durationInfo: DurationInfoDto? = null,
    @SerialName("cargo_info")
    val cargoInfo: CargoInfoDto? = null,
    @SerialName("cost_info")
    val costInfo: CostInfoDto? = null,
    @SerialName("progress_info")
    val progressInfo: ProgressInfoDto? = null
)

@Serializable
data class DistanceInfoDto(
    @SerialName("estimated_distance")
    val estimatedDistance: Double? = null,
    @SerialName("estimated_distance_label")
    val estimatedDistanceLabel: String? = null,
    @SerialName("covered_distance")
    val coveredDistance: Double? = null,
    @SerialName("covered_distance_label")
    val coveredDistanceLabel: String? = null,
    @SerialName("total_distance")
    val totalDistance: Double? = null,
    @SerialName("total_distance_label")
    val totalDistanceLabel: String? = null,
    @SerialName("display_value")
    val displayValue: String? = null,
    @SerialName("display_label")
    val displayLabel: String? = null
)

@Serializable
data class DurationInfoDto(
    @SerialName("planned_duration_minutes")
    val plannedDurationMinutes: Long? = null,
    @SerialName("planned_duration_label")
    val plannedDurationLabel: String? = null,
    @SerialName("actual_duration_minutes")
    val actualDurationMinutes: Long? = null,
    @SerialName("actual_duration_label")
    val actualDurationLabel: String? = null,
    @SerialName("display_value")
    val displayValue: String? = null,
    @SerialName("display_label")
    val displayLabel: String? = null
)

@Serializable
data class CargoInfoDto(
    @SerialName("cargo_type")
    val cargoType: String? = null,
    @SerialName("cargo_type_label")
    val cargoTypeLabel: String? = null,
    @SerialName("cargo_description")
    val cargoDescription: String? = null,
    @SerialName("loading_weight")
    val loadingWeight: Double? = null,
    @SerialName("loading_weight_label")
    val loadingWeightLabel: String? = null,
    @SerialName("weight_unit")
    val weightUnit: String? = null
)

@Serializable
data class CostInfoDto(
    @SerialName("has_costs")
    val hasCosts: Boolean = false,
    @SerialName("total_cost")
    val totalCost: Double = 0.0,
    @SerialName("total_cost_label")
    val totalCostLabel: String? = null,
    @SerialName("cost_count")
    val costCount: Int = 0,
    @SerialName("fuel_cost")
    val fuelCost: Double = 0.0,
    @SerialName("toll_cost")
    val tollCost: Double = 0.0,
    @SerialName("other_costs")
    val otherCosts: Double = 0.0
)

@Serializable
data class ProgressInfoDto(
    @SerialName("progress_percent")
    // Backend sends a fractional percent (float64, e.g. 33.33). Decoding it into an
    // Int throws and blows up the whole TripResponse, so this MUST be Double.
    val progressPercent: Double? = null,
    @SerialName("progress_percent_label")
    val progressPercentLabel: String? = null,
    @SerialName("remaining_distance")
    val remainingDistance: Double? = null,
    @SerialName("remaining_distance_label")
    val remainingDistanceLabel: String? = null,
    @SerialName("estimated_arrival")
    val estimatedArrival: String? = null,
    @SerialName("estimated_arrival_label")
    val estimatedArrivalLabel: String? = null
)

/**
 * Embedded vehicle info in trip response.
 */
@Serializable
data class TripVehicleDto(
    @SerialName("id")
    val id: Int,
    @SerialName("registration_number")
    val registrationNumber: String? = null,
    @SerialName("make")
    val make: String? = null,
    @SerialName("model")
    val model: String? = null
)

/**
 * Embedded driver info in trip response.
 */
@Serializable
data class TripDriverDto(
    @SerialName("id")
    val id: Int,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("mobile")
    val mobile: String? = null
)

/**
 * API response wrapper for trip operations.
 */
@Serializable
data class TripApiResponse<T>(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: T? = null
)

/**
 * Request body for creating a trip.
 * Updated to match API v2 fields.
 * Note: timestamps are sent as UTC epoch-millis (JSON numbers).
 */
@Serializable
data class CreateTripRequest(
    @SerialName("vehicle_id")
    val vehicleId: Int,
    @SerialName("driver_id")
    val driverId: Int,
    // Backend requires planned_start and planned_end (UTC epoch-millis), binding:required
    @SerialName("planned_start")
    val plannedStart: Long,
    @SerialName("planned_end")
    val plannedEnd: Long,
    // Backend requires scheduled_date and start_time (UTC epoch-millis), binding:required
    @SerialName("scheduled_date")
    val scheduledDate: Long,
    @SerialName("start_time")
    val startTime: Long,
    // delivery_date / delivery_time stay optional (backend *int64, no binding)
    @SerialName("delivery_date")
    val deliveryDate: Long? = null,
    @SerialName("delivery_time")
    val deliveryTime: Long? = null,
    @SerialName("start_location")
    val startLocation: String,
    // Backend requires start_lat/start_lng/end_lat/end_lng (float64, binding:required → must be non-zero)
    @SerialName("start_lat")
    val startLat: Double,
    @SerialName("start_lng")
    val startLng: Double,
    @SerialName("end_location")
    val endLocation: String,
    @SerialName("end_lat")
    val endLat: Double,
    @SerialName("end_lng")
    val endLng: Double,
    @SerialName("estimated_distance")
    val estimatedDistance: Double? = null,
    @SerialName("cargo_type")
    val cargoType: String,
    @SerialName("cargo_description")
    val cargoDescription: String? = null,
    @SerialName("cargo_loading_weight")
    val cargoLoadingWeight: Double? = null,
    @SerialName("cargo_unloading_weight")
    val cargoUnloadingWeight: Double? = null,
    @SerialName("vehicle_weight")
    val vehicleWeight: Double? = null,
    @SerialName("weight_unit")
    val weightUnit: String? = null,
    // Fuel info
    @SerialName("fuel_type")
    val fuelType: String? = null,
    @SerialName("filled_fuel_quantity")
    val filledFuelQuantity: Double? = null,
    @SerialName("used_fuel_quantity")
    val usedFuelQuantity: Double? = null,
    @SerialName("fuel_rate")
    val fuelRate: Double? = null,
    @SerialName("km_per_liter")
    val kmPerLiter: Double? = null,
    // Pricing
    @SerialName("purchase_price")
    val purchasePrice: Double? = null,
    // selling_value = the ACTUAL price (revenue) the customer owes. The backend uses
    // this as the revenue anchor, falling back to expected_trip_price when 0/absent.
    @SerialName("selling_value")
    val sellingValue: Double? = null,
    @SerialName("estimated_expense")
    val estimatedExpense: Double? = null,
    @SerialName("expected_trip_price")
    val tripPrice: Double? = null,
    // NOTE: payment_status / pending_amount / payment_mode are derived server-side from
    // payment records and are intentionally NOT sent on create.
    // Customer - backend requires customer_id (binding:required); the server snapshots
    // name/contact/email/address from the customer record. Free-text customer fields
    // (customer / customer_name / customer_contact) are no longer accepted by the backend.
    @SerialName("customer_id")
    val customerId: Int,
    // Consignee / delivery (receiver) details. Backend requires all three on create
    // (binding:required). DISTINCT from the billing customer (customer_id above).
    @SerialName("delivery_address")
    val deliveryAddress: String? = null,
    @SerialName("delivery_person_name")
    val deliveryPersonName: String? = null,
    @SerialName("delivery_contact_number")
    val deliveryContactNumber: String? = null,
    @SerialName("priority")
    val priority: String? = null,
    @SerialName("notes")
    val notes: String? = null
)

/**
 * Request body for updating a trip.
 * Updated to match API v2 - supports Vehicle, Driver, Schedule, Location, Cargo, Customer updates.
 *
 * Role-Based Permissions:
 * - Owner & General Manager: Can edit trips in ANY state
 * - Manager: Can only edit trips in PLANNED state
 * - Supervisor: Cannot edit trips
 */
@Serializable
data class UpdateTripRequest(
    // Vehicle & Driver - can be changed to another available vehicle/driver
    @SerialName("vehicle_id")
    val vehicleId: Int? = null,
    @SerialName("driver_id")
    val driverId: Int? = null,

    // Schedule - UTC epoch-millis
    @SerialName("scheduled_date")
    val scheduledDate: Long? = null,
    @SerialName("start_time")
    val startTime: Long? = null,
    @SerialName("delivery_date")
    val deliveryDate: Long? = null,
    @SerialName("delivery_time")
    val deliveryTime: Long? = null,
    @SerialName("planned_start")
    val plannedStart: Long? = null,
    @SerialName("planned_end")
    val plannedEnd: Long? = null,

    // Location
    @SerialName("start_location")
    val startLocation: String? = null,
    @SerialName("start_lat")
    val startLat: Double? = null,
    @SerialName("start_lng")
    val startLng: Double? = null,
    @SerialName("end_location")
    val endLocation: String? = null,
    @SerialName("end_lat")
    val endLat: Double? = null,
    @SerialName("end_lng")
    val endLng: Double? = null,
    @SerialName("estimated_distance")
    val estimatedDistance: Double? = null,

    // Cargo
    @SerialName("cargo_type")
    val cargoType: String? = null,
    @SerialName("cargo_description")
    val cargoDescription: String? = null,
    @SerialName("cargo_loading_weight")
    val cargoLoadingWeight: Double? = null,
    @SerialName("cargo_unloading_weight")
    val cargoUnloadingWeight: Double? = null,
    @SerialName("vehicle_weight")
    val vehicleWeight: Double? = null,
    @SerialName("weight_unit")
    val weightUnit: String? = null,

    // Customer - backend UpdateTripRequest binds only customer_id. A non-zero value
    // reassigns the trip to a different managed customer (re-snapshotted server-side);
    // omit/null leaves the customer unchanged. Free-text customer fields are not accepted.
    @SerialName("customer_id")
    val customerId: Int? = null,

    // Pricing - API expects expected_trip_price for update
    // purchase_price (COGS) is accepted on update (previously only on create).
    @SerialName("purchase_price")
    val purchasePrice: Double? = null,
    @SerialName("expected_trip_price")
    val tripPrice: Double? = null,
    // selling_value = the ACTUAL price (revenue) the customer owes.
    @SerialName("selling_value")
    val sellingValue: Double? = null,

    // Other
    @SerialName("priority")
    val priority: String? = null,
    @SerialName("notes")
    val notes: String? = null
)

/**
 * Request body for updating trip state.
 */
@Serializable
data class UpdateTripStateRequest(
    @SerialName("state")
    val state: String
)

/**
 * Request body for updating trip progress (in_progress trips only).
 */
@Serializable
data class UpdateTripProgressRequest(
    @SerialName("covered_distance")
    val coveredDistance: Double? = null,
    @SerialName("covered_duration_minutes")
    val coveredDurationMinutes: Long? = null,
    @SerialName("current_lat")
    val currentLat: Double? = null,
    @SerialName("current_lng")
    val currentLng: Double? = null
)

/**
 * Request body for updating trip location.
 */
@Serializable
data class UpdateTripLocationRequest(
    @SerialName("current_lat")
    val currentLat: Double,
    @SerialName("current_lng")
    val currentLng: Double
)

// ============ TRIP STOPS ============

/**
 * Trip Stops API response wrapper.
 */
@Serializable
data class TripStopsApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<TripStopDto>? = null
)

/**
 * Single Trip Stop API response wrapper.
 */
@Serializable
data class TripStopApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: TripStopDto? = null
)

/**
 * Trip Stop DTO for API communication.
 */
@Serializable
data class TripStopDto(
    val id: Int,
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("stop_order")
    val stopOrder: Int,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    // UTC epoch-millis
    @SerialName("arrival_time")
    val arrivalTime: Long? = null,
    @SerialName("departure_time")
    val departureTime: Long? = null,
    @SerialName("stop_duration")
    val stopDuration: Int? = null,
    val notes: String? = null,
    @SerialName("is_completed")
    val isCompleted: Boolean = false,
    @SerialName("completed_at")
    val completedAt: Long? = null,
    @SerialName("created_at")
    val createdAt: Long? = null,
    @SerialName("updated_at")
    val updatedAt: Long? = null
)

/**
 * Request body for creating a trip stop.
 */
@Serializable
data class CreateTripStopRequest(
    @SerialName("stop_order")
    val stopOrder: Int,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("arrival_time")
    val arrivalTime: Long? = null,
    @SerialName("stop_duration")
    val stopDuration: Int? = null,
    val notes: String? = null
)

/**
 * Request body for updating a trip stop.
 */
@Serializable
data class UpdateTripStopRequest(
    @SerialName("stop_order")
    val stopOrder: Int? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("arrival_time")
    val arrivalTime: Long? = null,
    @SerialName("stop_duration")
    val stopDuration: Int? = null,
    val notes: String? = null
)
