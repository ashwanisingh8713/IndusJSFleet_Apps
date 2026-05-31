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
    @SerialName("driver_id") val driverId: Int = 0,
    @SerialName("trip_id") val tripId: Int? = null,
    @SerialName("vehicle_id") val vehicleId: Int? = null,
    @SerialName("cost_id") val costId: String = "",
    @SerialName("cost_label") val costLabel: String = "",
    @SerialName("group_id") val groupId: String = "",
    @SerialName("custom_cost_label") val customCostLabel: String? = null,
    @SerialName("amount") val amount: Double = 0.0,
    /** Backend returns `time.Time` → ISO 8601 string (e.g. `"2025-12-31T00:00:00Z"`). */
    @SerialName("date") val date: String = "",
    @SerialName("month") val month: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("is_deduction") val isDeduction: Boolean = false,
    @SerialName("owner_id") val ownerId: Int? = null,
    @SerialName("created_by") val createdBy: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
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
    val success: Boolean = false,
    val message: String? = null,
    val data: DriverCostDto? = null
)

/**
 * Top-level envelope for `GET /drivers/{id}/costs`.
 *
 * Actual backend response shape (see Go handler `DriverCostHandler.ListCosts`):
 * ```
 * {
 *   "success": true,
 *   "message": "...",
 *   "data": {
 *     "data":        { "costs": [...], "summary": {...} },
 *     "page":        1,
 *     "per_page":    10,
 *     "total":       5,
 *     "total_pages": 1,
 *     "has_more":    false,
 *     "next_page":   null
 *   }
 * }
 * ```
 */
@Serializable
data class DriverCostsListApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: DriverCostsListEnvelopeDto? = null
)

/**
 * Inner envelope that holds the actual costs payload PLUS the pagination fields
 * the backend places at the same level (sibling of `data.data`).
 */
@Serializable
data class DriverCostsListEnvelopeDto(
    /** The actual `{ costs, summary }` payload nested under `data.data`. */
    @SerialName("data") val payload: DriverCostsListPayloadDto? = null,
    @SerialName("page") val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 20,
    @SerialName("total") val total: Int = 0,
    @SerialName("total_pages") val totalPages: Int = 1,
    @SerialName("has_more") val hasMore: Boolean = false,
    @SerialName("next_page") val nextPage: Int? = null
) : Dto

@Serializable
data class DriverCostsListPayloadDto(
    val costs: List<DriverCostDto> = emptyList(),
    val summary: DriverCostsSummaryDto? = null
) : Dto

/**
 * Flattened driver costs result returned to the domain/presentation layer.
 *
 * The repository builds this from [DriverCostsListEnvelopeDto] so the rest of
 * the app keeps using a stable, ergonomic shape (`dto.costs`, `dto.summary`,
 * `dto.page`, `dto.totalPages`, …).
 */
@Serializable
data class DriverCostsListDto(
    val costs: List<DriverCostDto> = emptyList(),
    val summary: DriverCostsSummaryDto? = null,
    @SerialName("page") val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 20,
    @SerialName("total") val total: Int = 0,
    @SerialName("total_pages") val totalPages: Int = 1,
    @SerialName("has_more") val hasMore: Boolean = false
) : Dto

/**
 * Summary returned by `GET /drivers/{id}/costs`.
 *
 * Backend field names (see Go `EarningsSummaryResponse`):
 *   total_salary, total_incentives, total_deductions, total_other, net_earnings.
 *
 * `totalEarnings` is a virtual convenience = salary + incentives + other.
 */
@Serializable
data class DriverCostsSummaryDto(
    @SerialName("total_salary") val totalSalary: Double = 0.0,
    @SerialName("total_incentives") val totalIncentives: Double = 0.0,
    @SerialName("total_deductions") val totalDeductions: Double = 0.0,
    @SerialName("total_other") val totalOther: Double = 0.0,
    @SerialName("net_earnings") val netEarnings: Double = 0.0,
    @SerialName("cost_count") val costCount: Int = 0
) : Dto {
    /** Earnings = salary + incentives + other (everything that is not a deduction). */
    val totalEarnings: Double get() = totalSalary + totalIncentives + totalOther
    /** Alias kept for older call sites. */
    val netAmount: Double get() = netEarnings
}

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
    val success: Boolean = false,
    val message: String? = null,
    val data: BulkDriverCostsResultDto? = null
)

/**
 * Bulk create result.
 *
 * Backend response (see Go `BulkCreateResponse`):
 *   { "created_count": N, "error_count": M, "costs": [...], "errors": [...] }
 */
@Serializable
data class BulkDriverCostsResultDto(
    @SerialName("created_count") val created: Int = 0,
    @SerialName("error_count") val errorCount: Int = 0,
    @SerialName("costs") val costs: List<DriverCostDto> = emptyList(),
    @SerialName("errors") val errors: List<String> = emptyList()
) : Dto

