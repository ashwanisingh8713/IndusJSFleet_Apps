package com.indusjs.fleet.data.model.costs

import com.indusjs.fleet.core.ui.CostTypeGroup
import com.indusjs.fleet.core.ui.CostTypeItem
import com.indusjs.fleet.data.model.Dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Trip Cost Types - Fallback values if API cache is empty.
 * These match the structure from /trip-costs/types API.
 */
object TripCostTypes {
    val types = listOf(
        // Fuel & Energy
        "TC-001-001" to "Petrol",
        "TC-001-002" to "Diesel",
        "TC-001-003" to "CNG / LPG",
        "TC-001-004" to "EV Charging",
        // Toll & Parking
        "TC-002-001" to "Toll Charges",
        "TC-002-002" to "Parking Fees",
        "TC-002-003" to "Entry Charges",
        // Loading & Unloading
        "TC-003-001" to "Loading Charges",
        "TC-003-002" to "Unloading Charges",
        "TC-003-003" to "Crane / Forklift",
        "TC-003-004" to "Labor Charges",
        // Driver Expenses
        "TC-004-001" to "Driver Allowance",
        "TC-004-002" to "Driver Food",
        "TC-004-003" to "Driver Accommodation",
        // Permits & Compliance
        "TC-005-001" to "State Permit",
        "TC-005-002" to "National Permit",
        "TC-005-003" to "Special Permit",
        "TC-005-004" to "Chalan / Fine",
        // Miscellaneous
        "TC-006-001" to "Police / RTO",
        "TC-006-002" to "Weighbridge",
        "TC-006-003" to "Commission / Brokerage",
        "TC-006-004" to "Other"
    )

    /**
     * Groups with proper group IDs matching the API structure.
     */
    val groups: List<CostTypeGroup> = listOf(
        CostTypeGroup(
            groupId = "TC-G-001",
            groupName = "Fuel & Energy",
            items = listOf(
                CostTypeItem("TC-001-001", "Petrol"),
                CostTypeItem("TC-001-002", "Diesel"),
                CostTypeItem("TC-001-003", "CNG / LPG"),
                CostTypeItem("TC-001-004", "EV Charging")
            )
        ),
        CostTypeGroup(
            groupId = "TC-G-002",
            groupName = "Toll & Parking",
            items = listOf(
                CostTypeItem("TC-002-001", "Toll Charges"),
                CostTypeItem("TC-002-002", "Parking Fees"),
                CostTypeItem("TC-002-003", "Entry Charges")
            )
        ),
        CostTypeGroup(
            groupId = "TC-G-003",
            groupName = "Loading & Unloading",
            items = listOf(
                CostTypeItem("TC-003-001", "Loading Charges"),
                CostTypeItem("TC-003-002", "Unloading Charges"),
                CostTypeItem("TC-003-003", "Crane / Forklift"),
                CostTypeItem("TC-003-004", "Labor Charges")
            )
        ),
        CostTypeGroup(
            groupId = "TC-G-004",
            groupName = "Driver Expenses",
            items = listOf(
                CostTypeItem("TC-004-001", "Driver Allowance"),
                CostTypeItem("TC-004-002", "Driver Food"),
                CostTypeItem("TC-004-003", "Driver Accommodation")
            )
        ),
        CostTypeGroup(
            groupId = "TC-G-005",
            groupName = "Permits & Compliance",
            items = listOf(
                CostTypeItem("TC-005-001", "State Permit"),
                CostTypeItem("TC-005-002", "National Permit"),
                CostTypeItem("TC-005-003", "Special Permit"),
                CostTypeItem("TC-005-004", "Chalan / Fine")
            )
        ),
        CostTypeGroup(
            groupId = "TC-G-006",
            groupName = "Miscellaneous",
            items = listOf(
                CostTypeItem("TC-006-001", "Police / RTO"),
                CostTypeItem("TC-006-002", "Weighbridge"),
                CostTypeItem("TC-006-003", "Commission / Brokerage"),
                CostTypeItem("TC-006-004", "Other")
            )
        )
    )
}

/**
 * Maintenance Cost Types - Fallback values if API cache is empty.
 * These match the structure from /maintenance-costs/types API.
 */
object MaintenanceCostTypes {
    val types = listOf(
        // Regular Maintenance
        "VMC-001-001" to "Engine Oil Change",
        "VMC-001-002" to "Oil Filter Replacement",
        "VMC-001-003" to "Air Filter Replacement",
        "VMC-001-004" to "Wheel Alignment & Balancing",
        "VMC-001-005" to "General Servicing Labor",
        // Repairs & Replacements
        "VMC-002-001" to "Brake Pads / Discs",
        "VMC-002-002" to "Battery Replacement",
        "VMC-002-003" to "Tyres Replacement",
        "VMC-002-004" to "Clutch Repair",
        "VMC-002-005" to "Suspension Repair"
    )

    /**
     * Groups with proper group IDs matching the API structure.
     */
    val groups: List<CostTypeGroup> = listOf(
        CostTypeGroup(
            groupId = "VMC-G-001",
            groupName = "Regular Maintenance",
            items = listOf(
                CostTypeItem("VMC-001-001", "Engine Oil Change"),
                CostTypeItem("VMC-001-002", "Oil Filter Replacement"),
                CostTypeItem("VMC-001-003", "Air Filter Replacement"),
                CostTypeItem("VMC-001-004", "Wheel Alignment & Balancing"),
                CostTypeItem("VMC-001-005", "General Servicing Labor")
            )
        ),
        CostTypeGroup(
            groupId = "VMC-G-002",
            groupName = "Repairs & Replacements",
            items = listOf(
                CostTypeItem("VMC-002-001", "Brake Pads / Discs"),
                CostTypeItem("VMC-002-002", "Battery Replacement"),
                CostTypeItem("VMC-002-003", "Tyres Replacement"),
                CostTypeItem("VMC-002-004", "Clutch Repair"),
                CostTypeItem("VMC-002-005", "Suspension Repair")
            )
        )
    )
}

/**
 * Fuel Types.
 */
object FuelTypes {
    val types = listOf(
        "TC-001-002" to "Diesel",
        "TC-001-001" to "Petrol",
        "TC-001-003" to "CNG / LPG",
        "TC-001-004" to "EV Charging"
    )
}

/**
 * Trip Cost API response.
 */
@Serializable
data class TripCostApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: TripCostDto? = null
)

/**
 * Cost Types API response - for /trip-costs/types and /maintenance-costs/types APIs.
 * Structure: { success, message, data: { category_id, category_name, groups: [...] } }
 */
@Serializable
data class CostTypesApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: CostTypeCategoryDto? = null
)

/**
 * Cost Type Category - top level wrapper for cost types.
 * Contains category info and grouped cost type items.
 */
@Serializable
data class CostTypeCategoryDto(
    @SerialName("category_id")
    val categoryId: String,
    @SerialName("category_name")
    val categoryName: String,
    val groups: List<CostTypeGroupDto> = emptyList()
) : Dto {
    /**
     * Flattens all cost types from all groups into a single list of value to label pairs.
     * Useful for dropdowns and selection.
     */
    fun toFlatList(): List<Pair<String, String>> {
        return groups.flatMap { group ->
            group.items.map { item -> item.value to item.label }
        }
    }

    /**
     * Converts to CostTypeGroup list with full group IDs for reliable category detection.
     */
    fun toCostTypeGroups(): List<CostTypeGroup> {
        return groups.map { group ->
            CostTypeGroup(
                groupId = group.groupId,
                groupName = group.groupName,
                items = group.items.map { item ->
                    CostTypeItem(id = item.value, label = item.label)
                }
            )
        }
    }
}

/**
 * Cost Type Group - groups related cost types (e.g., "Fuel & Energy", "Toll & Parking").
 */
@Serializable
data class CostTypeGroupDto(
    @SerialName("group_id")
    val groupId: String,
    @SerialName("group_name")
    val groupName: String,
    val items: List<CostTypeItemDto> = emptyList()
) : Dto

/**
 * Individual cost type item.
 * API returns: { id, value, label }
 */
@Serializable
data class CostTypeItemDto(
    val id: String,
    val value: String,
    val label: String
) : Dto


/**
 * Trip Cost DTO.
 * Updated to match API with cost_id, cost_label, group_id structure.
 */
@Serializable
data class TripCostDto(
    val id: Int = 0,
    @SerialName("trip_id")
    val tripId: Int = 0,
    @SerialName("vehicle_id")
    val vehicleId: Int = 0,
    // New structured cost fields per API
    @SerialName("cost_id")
    val costId: String? = null,
    @SerialName("cost_label")
    val costLabel: String? = null,
    @SerialName("group_id")
    val groupId: String? = null,
    @SerialName("custom_cost_label")
    val customCostLabel: String? = null,
    // Legacy field (kept for backward compatibility - now optional with default)
    @SerialName("cost_type")
    val costType: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val time: String? = null,
    val notes: String? = null,
    // Fuel specific fields
    @SerialName("fuel_type")
    val fuelType: String? = null,
    @SerialName("fuel_quantity")
    val fuelQuantity: Double? = null,
    @SerialName("fuel_rate")
    val fuelRate: Double? = null,
    @SerialName("km_per_liter")
    val kmPerLiter: Double? = null,
    // Created by info
    @SerialName("owner_id")
    val ownerId: Int? = null,
    @SerialName("created_by")
    val createdBy: Int? = null,
    @SerialName("created_by_user")
    val createdByUser: CreatedByUserDto? = null,
    @SerialName("created_at")
    val createdAt: String? = null
) : Dto {
    /**
     * Returns the display label - prefers custom_cost_label, then cost_label, falls back to cost_type.
     */
    val displayLabel: String
        get() = customCostLabel ?: costLabel ?: costType.replace("_", " ").replaceFirstChar { it.uppercase() }

    /**
     * Returns true if this is a fuel category cost (group TC-G-001).
     */
    val isFuelCost: Boolean
        get() = groupId == "TC-G-001"

    /**
     * Returns the effective cost type identifier - prefers cost_id over legacy cost_type.
     */
    val effectiveCostType: String
        get() = costId ?: costType
}

/**
 * Created By User DTO - contains info about who added the cost.
 */
@Serializable
data class CreatedByUserDto(
    val id: Int = 0,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val role: String? = null,
    val email: String? = null
) : Dto {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { "Unknown" }

    val roleLabel: String
        get() = role?.replaceFirstChar { it.uppercaseChar() } ?: ""
}

/**
 * Trip Costs List API response.
 */
@Serializable
data class TripCostsListApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: TripCostsListDto? = null
)

@Serializable
data class TripCostsListDto(
    val costs: List<TripCostDto> = emptyList(),
    val total: Double = 0.0,
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1
) : Dto

/**
 * Create Trip Cost request.
 * Updated to use structured cost fields (cost_id, cost_label, group_id).
 * Note: API expects trip_id and vehicle_id as integers.
 */
@Serializable
data class CreateTripCostRequest(
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("vehicle_id")
    val vehicleId: Int,
    // New structured cost fields per API
    @SerialName("cost_id")
    val costId: String,
    @SerialName("cost_label")
    val costLabel: String,
    @SerialName("group_id")
    val groupId: String,
    @SerialName("custom_cost_label")
    val customCostLabel: String? = null,
    // Legacy field - still required for backward compatibility
    @SerialName("cost_type")
    val costType: String,
    val amount: Double,
    val date: String,
    val time: String? = null,
    val notes: String? = null,
    // Fuel specific
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
 * Bulk Create Trip Costs request - for /trips/{trip_id}/costs/bulk API.
 */
@Serializable
data class BulkCreateTripCostsRequest(
    val costs: List<BulkCostItem>
)

/**
 * Individual cost item for bulk creation.
 * Updated with structured cost fields (cost_id, cost_label, group_id).
 * Note: trip_id is taken from URL path and vehicle_id from the trip record.
 */
@Serializable
data class BulkCostItem(
    // New structured cost fields per API
    @SerialName("cost_id")
    val costId: String,
    @SerialName("cost_label")
    val costLabel: String,
    @SerialName("group_id")
    val groupId: String,
    @SerialName("custom_cost_label")
    val customCostLabel: String? = null,
    // Legacy field
    @SerialName("cost_type")
    val costType: String,
    val amount: Double,
    val date: String,
    val time: String? = null,
    val notes: String? = null,
    // Fuel specific
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
 * Bulk Create Trip Costs API response.
 */
@Serializable
data class BulkTripCostsApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: BulkTripCostsResultDto? = null
)

@Serializable
data class BulkTripCostsResultDto(
    val created: Int = 0,
    val costs: List<TripCostDto> = emptyList()
) : Dto

/**
 * Maintenance Cost DTO.
 * Updated to match API with cost_id, cost_label, group_id structure.
 */
@Serializable
data class MaintenanceCostDto(
    val id: Int = 0,
    @SerialName("vehicle_id")
    val vehicleId: Int = 0,
    // New structured cost fields per API
    @SerialName("cost_id")
    val costId: String? = null,
    @SerialName("cost_label")
    val costLabel: String? = null,
    @SerialName("group_id")
    val groupId: String? = null,
    @SerialName("custom_cost_label")
    val customCostLabel: String? = null,
    // Legacy field (kept for backward compatibility - now optional with default)
    @SerialName("cost_type")
    val costType: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val time: String? = null,
    val description: String? = null,
    val notes: String? = null,
    @SerialName("vendor_name")
    val vendorName: String? = null,
    @SerialName("invoice_no")
    val invoiceNo: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
) : Dto {
    /**
     * Returns the display label - prefers custom_cost_label, then cost_label, falls back to cost_type.
     */
    val displayLabel: String
        get() = customCostLabel ?: costLabel ?: costType.replace("_", " ").replaceFirstChar { it.uppercase() }

    /**
     * Returns the effective cost type identifier - prefers cost_id over legacy cost_type.
     */
    val effectiveCostType: String
        get() = costId ?: costType
}

/**
 * Maintenance Cost API response.
 */
@Serializable
data class MaintenanceCostApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: MaintenanceCostDto? = null
)

/**
 * Maintenance Costs List API response.
 */
@Serializable
data class MaintenanceCostsListApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: MaintenanceCostsListDto? = null
)

@Serializable
data class MaintenanceCostsListDto(
    val costs: List<MaintenanceCostDto> = emptyList(),
    val total: Double = 0.0,
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1
) : Dto

/**
 * Create Maintenance Cost request.
 * Updated to use structured cost fields (cost_id, cost_label, group_id).
 * Note: API expects vehicle_id as integer.
 */
@Serializable
data class CreateMaintenanceCostRequest(
    @SerialName("vehicle_id")
    val vehicleId: Int,
    // New structured cost fields per API
    @SerialName("cost_id")
    val costId: String,
    @SerialName("cost_label")
    val costLabel: String,
    @SerialName("group_id")
    val groupId: String,
    @SerialName("custom_cost_label")
    val customCostLabel: String? = null,
    // Legacy field - still required for backward compatibility
    @SerialName("cost_type")
    val costType: String,
    val amount: Double,
    val date: String,
    val time: String? = null,
    val description: String? = null,
    val notes: String? = null,
    @SerialName("vendor_name")
    val vendorName: String? = null,
    @SerialName("invoice_no")
    val invoiceNo: String? = null
)

/**
 * Bulk Create Maintenance Costs request - for /vehicles/{vehicle_id}/maintenance-costs/bulk API.
 */
@Serializable
data class BulkCreateMaintenanceCostsRequest(
    val costs: List<BulkMaintenanceCostItem>
)

/**
 * Individual maintenance cost item for bulk creation.
 * Updated with structured cost fields (cost_id, cost_label, group_id).
 * Note: vehicle_id is passed in the URL path, not in the body.
 */
@Serializable
data class BulkMaintenanceCostItem(
    // New structured cost fields per API
    @SerialName("cost_id")
    val costId: String,
    @SerialName("cost_label")
    val costLabel: String,
    @SerialName("group_id")
    val groupId: String,
    @SerialName("custom_cost_label")
    val customCostLabel: String? = null,
    // Legacy field
    @SerialName("cost_type")
    val costType: String,
    val amount: Double,
    val date: String,
    val time: String? = null,
    val description: String? = null,
    val notes: String? = null,
    @SerialName("vendor_name")
    val vendorName: String? = null,
    @SerialName("invoice_no")
    val invoiceNo: String? = null
)

/**
 * Bulk Create Maintenance Costs API response.
 */
@Serializable
data class BulkMaintenanceCostsApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: BulkMaintenanceCostsResultDto? = null
)

@Serializable
data class BulkMaintenanceCostsResultDto(
    val created: Int = 0,
    val costs: List<MaintenanceCostDto> = emptyList()
) : Dto

/**
 * Trip Cost Summary API response - for /trips/{trip_id}/costs/summary API.
 */
@Serializable
data class TripCostSummaryApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: TripCostSummaryDto? = null
)

/**
 * Trip Cost Summary DTO.
 * Contains total cost and breakdown by cost type.
 */
@Serializable
data class TripCostSummaryDto(
    val total: Double = 0.0,
    @SerialName("by_type")
    val byType: Map<String, Double> = emptyMap(),
    @SerialName("cost_count")
    val costCount: Int = 0
) : Dto

// ==================== Vehicle Costs API DTOs ====================

/**
 * Cost breakdown by type - used in vehicle costs responses.
 */
@Serializable
data class CostByTypeDto(
    @SerialName("cost_type")
    val costType: String,
    @SerialName("total_cost")
    val totalCost: Double,
    val count: Int = 0
) : Dto

/**
 * Vehicle Trip Costs API response - for /vehicles/{id}/trip-costs API.
 */
@Serializable
data class VehicleTripCostsApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: VehicleTripCostsDataDto? = null
)

@Serializable
data class VehicleTripCostsDataDto(
    val costs: List<TripCostDto> = emptyList(),
    @SerialName("total_cost")
    val totalCost: Double? = null,
    @SerialName("filtered_total")
    val filteredTotal: Double? = null,
    @SerialName("cost_by_type")
    val costByType: List<CostByTypeDto> = emptyList(),
    val count: Int = 0,
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1,
    @SerialName("has_more")
    val hasMore: Boolean? = false,
    @SerialName("next_page")
    val nextPage: Int? = null,
    @SerialName("applied_filters")
    val appliedFilters: AppliedFiltersDto? = null
) : Dto

/**
 * Vehicle Maintenance Costs API response - for /vehicles/{id}/maintenance-costs API.
 */
@Serializable
data class VehicleMaintenanceCostsApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: VehicleMaintenanceCostsDataDto? = null
)

@Serializable
data class VehicleMaintenanceCostsDataDto(
    val costs: List<MaintenanceCostDto> = emptyList(),
    @SerialName("total_cost")
    val totalCost: Double? = null,
    @SerialName("filtered_total")
    val filteredTotal: Double? = null,
    @SerialName("cost_by_type")
    val costByType: List<CostByTypeDto> = emptyList(),
    val count: Int = 0,
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1,
    @SerialName("has_more")
    val hasMore: Boolean? = false,
    @SerialName("next_page")
    val nextPage: Int? = null,
    @SerialName("applied_filters")
    val appliedFilters: AppliedFiltersDto? = null
) : Dto

/**
 * Applied filters in response.
 */
@Serializable
data class AppliedFiltersDto(
    @SerialName("cost_type")
    val costType: String? = null,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    @SerialName("sort_by")
    val sortBy: String? = null,
    @SerialName("sort_order")
    val sortOrder: String? = null
) : Dto

/**
 * Delete Cost API response.
 */
@Serializable
data class DeleteCostApiResponse(
    val success: Boolean,
    val message: String? = null
)

