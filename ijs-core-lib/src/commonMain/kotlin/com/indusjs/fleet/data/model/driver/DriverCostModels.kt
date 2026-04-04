package com.indusjs.fleet.data.model.driver

import com.indusjs.fleet.data.model.costs.CostTypeGroupDto
import com.indusjs.fleet.data.model.costs.CostTypeItemDto
import com.indusjs.fleet.data.model.Dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Driver Cost Types - Fallback values if API cache is empty.
 * These match the structure from /cost-types/driver API.
 */
object DriverCostTypes {
    val types = listOf(
        "DC-001-001" to "Monthly Salary", "DC-001-002" to "Daily Wages",
        "DC-001-003" to "Overtime", "DC-001-004" to "Holiday Pay",
        "DC-002-001" to "Trip Bonus", "DC-002-002" to "Performance Bonus",
        "DC-002-003" to "Fuel Savings", "DC-002-004" to "On-Time Delivery",
        "DC-002-005" to "Safety Bonus",
        "DC-003-001" to "Advance Recovery", "DC-003-002" to "Damage Deduction",
        "DC-003-003" to "Fine", "DC-003-004" to "Loan EMI", "DC-003-005" to "Insurance",
        "DC-004-001" to "Training", "DC-004-002" to "Uniform",
        "DC-004-003" to "Medical", "DC-004-004" to "License Renewal", "DC-004-005" to "Other"
    )

    val groups: List<CostTypeGroupDto> = listOf(
        CostTypeGroupDto("DC-G-001", "Salary & Wages", listOf(
            CostTypeItemDto("DC-001-001", "DC-001-001", "Monthly Salary"),
            CostTypeItemDto("DC-001-002", "DC-001-002", "Daily Wages"),
            CostTypeItemDto("DC-001-003", "DC-001-003", "Overtime"),
            CostTypeItemDto("DC-001-004", "DC-001-004", "Holiday Pay")
        )),
        CostTypeGroupDto("DC-G-002", "Incentives & Bonuses", listOf(
            CostTypeItemDto("DC-002-001", "DC-002-001", "Trip Bonus"),
            CostTypeItemDto("DC-002-002", "DC-002-002", "Performance Bonus"),
            CostTypeItemDto("DC-002-003", "DC-002-003", "Fuel Savings"),
            CostTypeItemDto("DC-002-004", "DC-002-004", "On-Time Delivery"),
            CostTypeItemDto("DC-002-005", "DC-002-005", "Safety Bonus")
        )),
        CostTypeGroupDto("DC-G-003", "Deductions", listOf(
            CostTypeItemDto("DC-003-001", "DC-003-001", "Advance Recovery"),
            CostTypeItemDto("DC-003-002", "DC-003-002", "Damage Deduction"),
            CostTypeItemDto("DC-003-003", "DC-003-003", "Fine"),
            CostTypeItemDto("DC-003-004", "DC-003-004", "Loan EMI"),
            CostTypeItemDto("DC-003-005", "DC-003-005", "Insurance")
        )),
        CostTypeGroupDto("DC-G-004", "Other", listOf(
            CostTypeItemDto("DC-004-001", "DC-004-001", "Training"),
            CostTypeItemDto("DC-004-002", "DC-004-002", "Uniform"),
            CostTypeItemDto("DC-004-003", "DC-004-003", "Medical"),
            CostTypeItemDto("DC-004-004", "DC-004-004", "License Renewal"),
            CostTypeItemDto("DC-004-005", "DC-004-005", "Other")
        ))
    )

    const val DEDUCTION_GROUP_ID = "DC-G-003"
}

// ==================== Driver Cost DTOs ====================

@Serializable
data class DriverCostDto(
    val id: Int = 0,
    @SerialName("driver_id") val driverId: Int,
    @SerialName("trip_id") val tripId: Int? = null,
    @SerialName("vehicle_id") val vehicleId: Int? = null,
    @SerialName("cost_id") val costId: String,
    @SerialName("cost_label") val costLabel: String,
    @SerialName("group_id") val groupId: String,
    @SerialName("custom_cost_label") val customCostLabel: String? = null,
    @SerialName("amount") val amount: Double,
    @SerialName("date") val date: String,
    @SerialName("month") val month: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("is_deduction") val isDeduction: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
) : Dto {
    val displayLabel: String get() = customCostLabel ?: costLabel
    val isDeductionCost: Boolean get() = groupId == DriverCostTypes.DEDUCTION_GROUP_ID || isDeduction
}

@Serializable
data class CreateDriverCostRequest(
    @SerialName("driver_id") val driverId: Int,
    @SerialName("trip_id") val tripId: Int? = null,
    @SerialName("vehicle_id") val vehicleId: Int? = null,
    @SerialName("cost_id") val costId: String,
    @SerialName("cost_label") val costLabel: String,
    @SerialName("group_id") val groupId: String,
    @SerialName("custom_cost_label") val customCostLabel: String? = null,
    @SerialName("amount") val amount: Double,
    @SerialName("date") val date: String,
    @SerialName("month") val month: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("is_deduction") val isDeduction: Boolean = false
)

@Serializable
data class DriverCostApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: DriverCostDto? = null
)

@Serializable
data class DriverCostsListApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: DriverCostsListDto? = null
)

@Serializable
data class DriverCostsListDto(
    val costs: List<DriverCostDto> = emptyList(),
    val summary: DriverCostsSummaryDto? = null,
    val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 20,
    @SerialName("total_pages") val totalPages: Int = 1
) : Dto

@Serializable
data class DriverCostsSummaryDto(
    @SerialName("total_earnings") val totalEarnings: Double = 0.0,
    @SerialName("total_deductions") val totalDeductions: Double = 0.0,
    @SerialName("net_amount") val netAmount: Double = 0.0,
    @SerialName("cost_count") val costCount: Int = 0
) : Dto

@Serializable
data class BulkCreateDriverCostsRequest(
    val costs: List<BulkDriverCostItem>
)

@Serializable
data class BulkDriverCostItem(
    @SerialName("trip_id") val tripId: Int? = null,
    @SerialName("vehicle_id") val vehicleId: Int? = null,
    @SerialName("cost_id") val costId: String,
    @SerialName("cost_label") val costLabel: String,
    @SerialName("group_id") val groupId: String,
    @SerialName("custom_cost_label") val customCostLabel: String? = null,
    @SerialName("amount") val amount: Double,
    @SerialName("date") val date: String,
    @SerialName("month") val month: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("is_deduction") val isDeduction: Boolean = false
)

@Serializable
data class BulkDriverCostsApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: BulkDriverCostsResultDto? = null
)

@Serializable
data class BulkDriverCostsResultDto(
    val created: Int = 0,
    val costs: List<DriverCostDto> = emptyList()
) : Dto

