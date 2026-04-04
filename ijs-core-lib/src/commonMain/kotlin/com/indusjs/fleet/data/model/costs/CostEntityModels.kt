package com.indusjs.fleet.data.model.costs

import com.indusjs.fleet.data.model.Dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ==================== Trip Cost DTOs ====================

/**
 * Trip Cost DTO - represents a single trip cost entry.
 */
@Serializable
data class TripCostDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("trip_id")
    val tripId: Int = 0,
    @SerialName("vehicle_id")
    val vehicleId: Int? = null,
    @SerialName("cost_type")
    val costType: String = "",
    @SerialName("cost_id")
    val costId: String? = null,
    @SerialName("cost_label")
    val costLabel: String? = null,
    @SerialName("group_id")
    val groupId: String? = null,
    @SerialName("custom_cost_label")
    val customCostLabel: String? = null,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("date")
    val date: String = "",
    @SerialName("time")
    val time: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("fuel_quantity")
    val fuelQuantity: Double? = null,
    @SerialName("fuel_rate")
    val fuelRate: Double? = null,
    @SerialName("km_per_liter")
    val kmPerLiter: Double? = null,
    @SerialName("fuel_type")
    val fuelType: String? = null,
    @SerialName("created_by_id")
    val createdById: Int? = null,
    @SerialName("created_by_user")
    val createdByUser: CostCreatedByUserDto? = null,
    @SerialName("created_at")
    val createdAt: String? = null
) : Dto {
    val displayLabel: String
        get() = customCostLabel ?: costLabel ?: costType.replace("_", " ").replaceFirstChar { it.uppercase() }

    /** Use cost_id if available, otherwise fall back to cost_type */
    val effectiveCostType: String
        get() = costId ?: costType

    /** Check if this is a fuel cost (group_id TC-G-001 = Fuel & Energy) */
    val isFuelCost: Boolean
        get() = groupId == "TC-G-001" || costType == "fuel"
}

/**
 * User info embedded in cost responses (created_by_user).
 */
@Serializable
data class CostCreatedByUserDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("role")
    val role: String? = null
) : Dto {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { "Unknown" }

    val roleLabel: String
        get() = role?.replaceFirstChar { it.uppercaseChar() } ?: ""
}

/**
 * Create Trip Cost request body.
 */
@Serializable
data class CreateTripCostRequest(
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("vehicle_id")
    val vehicleId: Int? = null,
    @SerialName("cost_id")
    val costId: String? = null,
    @SerialName("cost_label")
    val costLabel: String? = null,
    @SerialName("group_id")
    val groupId: String? = null,
    @SerialName("custom_cost_label")
    val customCostLabel: String? = null,
    @SerialName("cost_type")
    val costType: String = "",
    @SerialName("amount")
    val amount: Double,
    @SerialName("date")
    val date: String,
    @SerialName("time")
    val time: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("fuel_quantity")
    val fuelQuantity: Double? = null,
    @SerialName("fuel_rate")
    val fuelRate: Double? = null,
    @SerialName("km_per_liter")
    val kmPerLiter: Double? = null
)

/**
 * Bulk trip cost item for bulk creation.
 */
@Serializable
data class BulkTripCostItem(
    @SerialName("cost_id")
    val costId: String,
    @SerialName("cost_label")
    val costLabel: String,
    @SerialName("group_id")
    val groupId: String,
    @SerialName("custom_cost_label")
    val customCostLabel: String? = null,
    @SerialName("cost_type")
    val costType: String? = null,
    @SerialName("amount")
    val amount: Double,
    @SerialName("date")
    val date: String = "",
    @SerialName("time")
    val time: String? = null,
    @SerialName("date_time")
    val dateTime: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("fuel_type")
    val fuelType: String? = null,
    @SerialName("fuel_quantity")
    val fuelQuantity: Double? = null,
    @SerialName("fuel_rate")
    val fuelRate: Double? = null,
    @SerialName("km_per_liter")
    val kmPerLiter: Double? = null
)

/**
 * Type alias for backward compatibility.
 */
typealias BulkCostItem = BulkTripCostItem

/**
 * Bulk create trip costs request.
 */
@Serializable
data class BulkCreateTripCostsRequest(
    @SerialName("costs")
    val costs: List<BulkTripCostItem>
)

/**
 * Trip cost summary.
 */
@Serializable
data class TripCostSummaryDto(
    @SerialName("trip_id")
    val tripId: Int = 0,
    @SerialName("total_amount")
    val totalAmount: Double = 0.0,
    @SerialName("cost_count")
    val costCount: Int = 0,
    @SerialName("by_type")
    val byType: Map<String, Double>? = null
) : Dto

// ==================== Maintenance Cost DTOs ====================

/**
 * Maintenance Cost DTO - represents a single maintenance cost entry.
 */
@Serializable
data class MaintenanceCostDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("vehicle_id")
    val vehicleId: Int = 0,
    @SerialName("cost_type")
    val costType: String = "",
    @SerialName("cost_id")
    val costId: String? = null,
    @SerialName("cost_label")
    val costLabel: String? = null,
    @SerialName("group_id")
    val groupId: String? = null,
    @SerialName("custom_cost_label")
    val customCostLabel: String? = null,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("date")
    val date: String = "",
    @SerialName("time")
    val time: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("vendor_name")
    val vendorName: String? = null,
    @SerialName("invoice_no")
    val invoiceNo: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
) : Dto {
    val displayLabel: String
        get() = customCostLabel ?: costLabel ?: costType.replace("_", " ").replaceFirstChar { it.uppercase() }

    /** Use cost_id if available, otherwise fall back to cost_type */
    val effectiveCostType: String
        get() = costId ?: costType
}

/**
 * Create Maintenance Cost request body.
 */
@Serializable
data class CreateMaintenanceCostRequest(
    @SerialName("vehicle_id")
    val vehicleId: Int,
    @SerialName("cost_id")
    val costId: String? = null,
    @SerialName("cost_label")
    val costLabel: String? = null,
    @SerialName("group_id")
    val groupId: String? = null,
    @SerialName("custom_cost_label")
    val customCostLabel: String? = null,
    @SerialName("cost_type")
    val costType: String = "",
    @SerialName("amount")
    val amount: Double,
    @SerialName("date")
    val date: String,
    @SerialName("time")
    val time: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("vendor_name")
    val vendorName: String? = null,
    @SerialName("invoice_no")
    val invoiceNo: String? = null
)

/**
 * Update Maintenance Cost request body (partial update).
 */
@Serializable
data class UpdateMaintenanceCostRequest(
    @SerialName("cost_id")
    val costId: String? = null,
    @SerialName("cost_label")
    val costLabel: String? = null,
    @SerialName("group_id")
    val groupId: String? = null,
    @SerialName("amount")
    val amount: Double? = null,
    @SerialName("date")
    val date: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("vendor_name")
    val vendorName: String? = null
)

/**
 * Bulk maintenance cost item.
 */
@Serializable
data class BulkMaintenanceCostItem(
    @SerialName("cost_id")
    val costId: String,
    @SerialName("cost_label")
    val costLabel: String,
    @SerialName("group_id")
    val groupId: String,
    @SerialName("custom_cost_label")
    val customCostLabel: String? = null,
    @SerialName("amount")
    val amount: Double,
    @SerialName("date_time")
    val dateTime: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("vendor_name")
    val vendorName: String? = null,
    @SerialName("invoice_no")
    val invoiceNo: String? = null
)

/**
 * Bulk create maintenance costs request.
 */
@Serializable
data class BulkCreateMaintenanceCostsRequest(
    @SerialName("costs")
    val costs: List<BulkMaintenanceCostItem>
)

// ==================== Vehicle Costs Data DTOs ====================

/**
 * Vehicle trip costs response data with pagination.
 */
@Serializable
data class VehicleTripCostsDataDto(
    @SerialName("costs")
    val costs: List<TripCostDto> = emptyList(),
    @SerialName("total_amount")
    val totalAmount: Double = 0.0,
    @SerialName("filtered_total")
    val filteredTotal: Double? = null,
    @SerialName("total_cost")
    val totalCost: Double? = null,
    @SerialName("has_more")
    val hasMore: Boolean? = null,
    @SerialName("page")
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1,
    @SerialName("total_count")
    val totalCount: Int = 0
) : Dto

/**
 * Vehicle maintenance costs response data with pagination.
 */
@Serializable
data class VehicleMaintenanceCostsDataDto(
    @SerialName("costs")
    val costs: List<MaintenanceCostDto> = emptyList(),
    @SerialName("total_amount")
    val totalAmount: Double = 0.0,
    @SerialName("filtered_total")
    val filteredTotal: Double? = null,
    @SerialName("total_cost")
    val totalCost: Double? = null,
    @SerialName("has_more")
    val hasMore: Boolean? = null,
    @SerialName("page")
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1,
    @SerialName("total_count")
    val totalCount: Int = 0
) : Dto

