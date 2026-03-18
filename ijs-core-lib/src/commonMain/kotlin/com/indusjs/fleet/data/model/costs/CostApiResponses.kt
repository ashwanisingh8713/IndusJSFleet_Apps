package com.indusjs.fleet.data.model.costs

import com.indusjs.fleet.data.model.Dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ==================== Cost Types API Response ====================

/**
 * API response for cost types endpoints.
 */
@Serializable
data class CostTypesApiResponse(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: CostTypeCategoryDto? = null
)

// ==================== Trip Cost API Responses ====================

@Serializable
data class TripCostApiResponse(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: TripCostDto? = null
)

@Serializable
data class TripCostsListApiResponse(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: TripCostsListDataDto? = null
)

@Serializable
data class TripCostsListDataDto(
    @SerialName("costs")
    val costs: List<TripCostDto> = emptyList(),
    @SerialName("total_amount")
    val totalAmount: Double = 0.0
) : Dto

@Serializable
data class TripCostSummaryApiResponse(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: TripCostSummaryDto? = null
)

@Serializable
data class BulkTripCostsApiResponse(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: BulkCostsResultDto? = null
)

// ==================== Maintenance Cost API Responses ====================

@Serializable
data class MaintenanceCostApiResponse(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: MaintenanceCostDto? = null
)

@Serializable
data class MaintenanceCostsListApiResponse(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: MaintenanceCostsListDataDto? = null
)

@Serializable
data class MaintenanceCostsListDataDto(
    @SerialName("costs")
    val costs: List<MaintenanceCostDto> = emptyList(),
    @SerialName("total_amount")
    val totalAmount: Double = 0.0
) : Dto

@Serializable
data class BulkMaintenanceCostsApiResponse(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: BulkCostsResultDto? = null
)

// ==================== Vehicle Costs API Responses ====================

@Serializable
data class VehicleTripCostsApiResponse(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: VehicleTripCostsDataDto? = null
)

@Serializable
data class VehicleMaintenanceCostsApiResponse(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: VehicleMaintenanceCostsDataDto? = null
)

// ==================== Delete Cost API Response ====================

@Serializable
data class DeleteCostApiResponse(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("message")
    val message: String? = null
)

// ==================== Shared Bulk Result ====================

@Serializable
data class BulkCostsResultDto(
    @SerialName("created")
    val created: Int = 0
)

// ==================== Fallback Cost Types ====================

/**
 * Trip Cost Types - Fallback values if API cache is empty.
 */
object TripCostTypes {
    /** Flat list of (id, label) pairs derived from groups. */
    val types: List<Pair<String, String>>
        get() = groups.flatMap { group -> group.items.map { it.id to it.label } }

    val groups: List<CostTypeGroupDto> = listOf(
        CostTypeGroupDto(
            groupId = "TC-G-001",
            groupName = "Fuel & Energy",
            items = listOf(
                CostTypeItemDto("TC-001-001", "TC-001-001", "Petrol"),
                CostTypeItemDto("TC-001-002", "TC-001-002", "Diesel"),
                CostTypeItemDto("TC-001-003", "TC-001-003", "CNG/LPG"),
                CostTypeItemDto("TC-001-004", "TC-001-004", "EV Charging"),
                CostTypeItemDto("TC-001-005", "TC-001-005", "AdBlue/DEF")
            )
        ),
        CostTypeGroupDto(
            groupId = "TC-G-002",
            groupName = "Toll & Parking",
            items = listOf(
                CostTypeItemDto("TC-002-001", "TC-002-001", "Toll Charges"),
                CostTypeItemDto("TC-002-002", "TC-002-002", "Parking Fees"),
                CostTypeItemDto("TC-002-003", "TC-002-003", "Entry Charges")
            )
        ),
        CostTypeGroupDto(
            groupId = "TC-G-003",
            groupName = "Loading & Unloading",
            items = listOf(
                CostTypeItemDto("TC-003-001", "TC-003-001", "Loading Charges"),
                CostTypeItemDto("TC-003-002", "TC-003-002", "Unloading Charges"),
                CostTypeItemDto("TC-003-003", "TC-003-003", "Crane/Forklift"),
                CostTypeItemDto("TC-003-004", "TC-003-004", "Labor Charges")
            )
        ),
        CostTypeGroupDto(
            groupId = "TC-G-004",
            groupName = "Driver Expenses",
            items = listOf(
                CostTypeItemDto("TC-004-001", "TC-004-001", "Driver Allowance"),
                CostTypeItemDto("TC-004-002", "TC-004-002", "Food & Meals"),
                CostTypeItemDto("TC-004-003", "TC-004-003", "Accommodation")
            )
        ),
        CostTypeGroupDto(
            groupId = "TC-G-005",
            groupName = "Permits & Compliance",
            items = listOf(
                CostTypeItemDto("TC-005-001", "TC-005-001", "State Permit"),
                CostTypeItemDto("TC-005-002", "TC-005-002", "National Permit"),
                CostTypeItemDto("TC-005-003", "TC-005-003", "Chalan/Fine"),
                CostTypeItemDto("TC-005-004", "TC-005-004", "Weighbridge")
            )
        ),
        CostTypeGroupDto(
            groupId = "TC-G-006",
            groupName = "Miscellaneous",
            items = listOf(
                CostTypeItemDto("TC-006-001", "TC-006-001", "Police/RTO"),
                CostTypeItemDto("TC-006-002", "TC-006-002", "Commission"),
                CostTypeItemDto("TC-006-003", "TC-006-003", "Other")
            )
        )
    )
}

/**
 * Maintenance Cost Types - Fallback values if API cache is empty.
 */
object MaintenanceCostTypes {
    /** Flat list of (id, label) pairs derived from groups. */
    val types: List<Pair<String, String>>
        get() = groups.flatMap { group -> group.items.map { it.id to it.label } }

    val groups: List<CostTypeGroupDto> = listOf(
        CostTypeGroupDto(
            groupId = "MC-G-001",
            groupName = "Engine & Mechanical",
            items = listOf(
                CostTypeItemDto("MC-001-001", "MC-001-001", "Engine Repair"),
                CostTypeItemDto("MC-001-002", "MC-001-002", "Transmission"),
                CostTypeItemDto("MC-001-003", "MC-001-003", "Clutch/Brake"),
                CostTypeItemDto("MC-001-004", "MC-001-004", "Suspension")
            )
        ),
        CostTypeGroupDto(
            groupId = "MC-G-002",
            groupName = "Body & Exterior",
            items = listOf(
                CostTypeItemDto("MC-002-001", "MC-002-001", "Body Repair"),
                CostTypeItemDto("MC-002-002", "MC-002-002", "Paint Job"),
                CostTypeItemDto("MC-002-003", "MC-002-003", "Glass/Mirror")
            )
        ),
        CostTypeGroupDto(
            groupId = "MC-G-003",
            groupName = "Tyres & Wheels",
            items = listOf(
                CostTypeItemDto("MC-003-001", "MC-003-001", "Tyre Replacement"),
                CostTypeItemDto("MC-003-002", "MC-003-002", "Tyre Repair"),
                CostTypeItemDto("MC-003-003", "MC-003-003", "Wheel Alignment"),
                CostTypeItemDto("MC-003-004", "MC-003-004", "Wheel Balancing")
            )
        ),
        CostTypeGroupDto(
            groupId = "MC-G-004",
            groupName = "Electrical & Electronics",
            items = listOf(
                CostTypeItemDto("MC-004-001", "MC-004-001", "Battery"),
                CostTypeItemDto("MC-004-002", "MC-004-002", "Alternator/Starter"),
                CostTypeItemDto("MC-004-003", "MC-004-003", "Wiring/Lights"),
                CostTypeItemDto("MC-004-004", "MC-004-004", "AC Repair")
            )
        ),
        CostTypeGroupDto(
            groupId = "MC-G-005",
            groupName = "Fuel & Fluids",
            items = listOf(
                CostTypeItemDto("MC-005-001", "MC-005-001", "Engine Oil"),
                CostTypeItemDto("MC-005-002", "MC-005-002", "Coolant"),
                CostTypeItemDto("MC-005-003", "MC-005-003", "Brake Fluid"),
                CostTypeItemDto("MC-005-004", "MC-005-004", "Gear Oil")
            )
        ),
        CostTypeGroupDto(
            groupId = "MC-G-006",
            groupName = "Routine Service",
            items = listOf(
                CostTypeItemDto("MC-006-001", "MC-006-001", "Regular Service"),
                CostTypeItemDto("MC-006-002", "MC-006-002", "Washing/Cleaning"),
                CostTypeItemDto("MC-006-003", "MC-006-003", "Inspection"),
                CostTypeItemDto("MC-006-004", "MC-006-004", "Other")
            )
        )
    )
}

/**
 * Extension function to convert CostTypeCategoryDto to a flat list of pairs (id, label).
 */
fun CostTypeCategoryDto.toFlatList(): List<Pair<String, String>> {
    return groups.flatMap { group ->
        group.items.map { item -> item.id to item.label }
    }
}

/**
 * Extension function to convert CostTypeCategoryDto to a list of CostTypeGroupDto.
 * Used by ViewModels to populate grouped cost type selectors.
 */
fun CostTypeCategoryDto.toCostTypeGroups(): List<CostTypeGroupDto> {
    return groups
}

/**
 * Fuel Types - Fallback fuel type options for fuel cost entries.
 */
object FuelTypes {
    val types: List<Pair<String, String>> = listOf(
        "TC-001-001" to "Petrol",
        "TC-001-002" to "Diesel",
        "TC-001-003" to "CNG/LPG",
        "TC-001-004" to "EV Charging",
        "TC-001-005" to "AdBlue/DEF"
    )
}

