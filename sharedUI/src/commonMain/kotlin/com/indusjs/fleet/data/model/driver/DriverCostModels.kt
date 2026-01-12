package com.indusjs.fleet.data.model.driver

import com.indusjs.fleet.core.ui.CostTypeGroup
import com.indusjs.fleet.core.ui.CostTypeItem
import com.indusjs.fleet.data.model.Dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Driver Cost Types - Fallback values if API cache is empty.
 * These match the structure from /cost-types/driver API.
 *
 * Cost Groups:
 * - DC-G-001: Salary & Wages
 * - DC-G-002: Incentives & Bonuses
 * - DC-G-003: Deductions
 * - DC-G-004: Other
 */
object DriverCostTypes {
    val types = listOf(
        // Salary & Wages
        "DC-001-001" to "Monthly Salary",
        "DC-001-002" to "Daily Wages",
        "DC-001-003" to "Overtime",
        "DC-001-004" to "Holiday Pay",
        // Incentives & Bonuses
        "DC-002-001" to "Trip Bonus",
        "DC-002-002" to "Performance Bonus",
        "DC-002-003" to "Fuel Savings",
        "DC-002-004" to "On-Time Delivery",
        "DC-002-005" to "Safety Bonus",
        // Deductions
        "DC-003-001" to "Advance Recovery",
        "DC-003-002" to "Damage Deduction",
        "DC-003-003" to "Fine",
        "DC-003-004" to "Loan EMI",
        "DC-003-005" to "Insurance",
        // Other
        "DC-004-001" to "Training",
        "DC-004-002" to "Uniform",
        "DC-004-003" to "Medical",
        "DC-004-004" to "License Renewal",
        "DC-004-005" to "Other"
    )

    /**
     * Groups with proper group IDs matching the API structure.
     */
    val groups: List<CostTypeGroup> = listOf(
        CostTypeGroup(
            groupId = "DC-G-001",
            groupName = "Salary & Wages",
            items = listOf(
                CostTypeItem("DC-001-001", "Monthly Salary"),
                CostTypeItem("DC-001-002", "Daily Wages"),
                CostTypeItem("DC-001-003", "Overtime"),
                CostTypeItem("DC-001-004", "Holiday Pay")
            )
        ),
        CostTypeGroup(
            groupId = "DC-G-002",
            groupName = "Incentives & Bonuses",
            items = listOf(
                CostTypeItem("DC-002-001", "Trip Bonus"),
                CostTypeItem("DC-002-002", "Performance Bonus"),
                CostTypeItem("DC-002-003", "Fuel Savings"),
                CostTypeItem("DC-002-004", "On-Time Delivery"),
                CostTypeItem("DC-002-005", "Safety Bonus")
            )
        ),
        CostTypeGroup(
            groupId = "DC-G-003",
            groupName = "Deductions",
            items = listOf(
                CostTypeItem("DC-003-001", "Advance Recovery"),
                CostTypeItem("DC-003-002", "Damage Deduction"),
                CostTypeItem("DC-003-003", "Fine"),
                CostTypeItem("DC-003-004", "Loan EMI"),
                CostTypeItem("DC-003-005", "Insurance")
            )
        ),
        CostTypeGroup(
            groupId = "DC-G-004",
            groupName = "Other",
            items = listOf(
                CostTypeItem("DC-004-001", "Training"),
                CostTypeItem("DC-004-002", "Uniform"),
                CostTypeItem("DC-004-003", "Medical"),
                CostTypeItem("DC-004-004", "License Renewal"),
                CostTypeItem("DC-004-005", "Other")
            )
        )
    )

    /**
     * Deduction group ID - costs in this group are deducted from earnings.
     */
    const val DEDUCTION_GROUP_ID = "DC-G-003"
}

// ==================== Driver Cost DTOs ====================

/**
 * Driver Cost DTO.
 * Represents a single driver cost entry (salary, bonus, deduction, etc.).
 */
@Serializable
data class DriverCostDto(
    val id: Int = 0,
    @SerialName("driver_id")
    val driverId: Int,
    @SerialName("trip_id")
    val tripId: Int? = null,
    // Structured cost fields
    @SerialName("cost_id")
    val costId: String,
    @SerialName("cost_label")
    val costLabel: String,
    @SerialName("group_id")
    val groupId: String,
    @SerialName("custom_cost_label")
    val customCostLabel: String? = null,
    val amount: Double,
    val date: String,
    val month: String? = null,
    val description: String? = null,
    val notes: String? = null,
    @SerialName("is_deduction")
    val isDeduction: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null
) : Dto {
    /**
     * Returns the display label - prefers custom_cost_label, falls back to cost_label.
     */
    val displayLabel: String
        get() = customCostLabel ?: costLabel

    /**
     * Returns true if this is a deduction cost (group DC-G-003).
     */
    val isDeductionCost: Boolean
        get() = groupId == DriverCostTypes.DEDUCTION_GROUP_ID || isDeduction
}

/**
 * Create Driver Cost request.
 * Used for POST /drivers/{driver_id}/costs
 */
@Serializable
data class CreateDriverCostRequest(
    @SerialName("driver_id")
    val driverId: Int,
    @SerialName("trip_id")
    val tripId: Int? = null,
    @SerialName("cost_id")
    val costId: String,
    @SerialName("cost_label")
    val costLabel: String,
    @SerialName("group_id")
    val groupId: String,
    @SerialName("custom_cost_label")
    val customCostLabel: String? = null,
    val amount: Double,
    val date: String,
    val month: String? = null,
    val description: String? = null,
    val notes: String? = null,
    @SerialName("is_deduction")
    val isDeduction: Boolean = false
)

/**
 * Driver Cost API response.
 */
@Serializable
data class DriverCostApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: DriverCostDto? = null
)

/**
 * Driver Costs List API response.
 * Used for GET /drivers/{driver_id}/costs
 */
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
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1
) : Dto

/**
 * Driver Costs Summary DTO.
 * Provides aggregated totals for driver costs.
 */
@Serializable
data class DriverCostsSummaryDto(
    @SerialName("total_earnings")
    val totalEarnings: Double = 0.0,
    @SerialName("total_deductions")
    val totalDeductions: Double = 0.0,
    @SerialName("net_amount")
    val netAmount: Double = 0.0,
    @SerialName("cost_count")
    val costCount: Int = 0
) : Dto

/**
 * Bulk Create Driver Costs request.
 */
@Serializable
data class BulkCreateDriverCostsRequest(
    val costs: List<BulkDriverCostItem>
)

/**
 * Individual driver cost item for bulk creation.
 */
@Serializable
data class BulkDriverCostItem(
    @SerialName("cost_id")
    val costId: String,
    @SerialName("cost_label")
    val costLabel: String,
    @SerialName("group_id")
    val groupId: String,
    @SerialName("custom_cost_label")
    val customCostLabel: String? = null,
    val amount: Double,
    val date: String,
    val month: String? = null,
    val description: String? = null,
    val notes: String? = null,
    @SerialName("is_deduction")
    val isDeduction: Boolean = false
)

/**
 * Bulk Create Driver Costs API response.
 */
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

// ==================== Driver Financial Summary DTOs ====================

/**
 * Driver Financial Summary API response.
 * Used for GET /drivers/{driver_id}/financial-summary
 */
@Serializable
data class DriverFinancialSummaryApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: DriverFinancialSummaryDto? = null
)

/**
 * Driver Financial Summary DTO.
 * Comprehensive financial overview for a driver.
 */
@Serializable
data class DriverFinancialSummaryDto(
    @SerialName("driver_id")
    val driverId: Int,
    @SerialName("driver_name")
    val driverName: String? = null,
    val period: String? = null,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    // Trip stats
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("total_distance")
    val totalDistance: Double = 0.0,
    // Financial metrics
    @SerialName("total_salary")
    val totalSalary: Double = 0.0,
    @SerialName("total_incentives")
    val totalIncentives: Double = 0.0,
    @SerialName("total_deductions")
    val totalDeductions: Double = 0.0,
    @SerialName("total_earnings")
    val totalEarnings: Double = 0.0,
    @SerialName("net_earnings")
    val netEarnings: Double = 0.0,
    // Efficiency metrics
    @SerialName("avg_earning_per_trip")
    val avgEarningPerTrip: Double = 0.0,
    @SerialName("avg_earning_per_km")
    val avgEarningPerKm: Double = 0.0,
    // Breakdown
    @SerialName("cost_breakdown")
    val costBreakdown: List<DriverCostBreakdownDto>? = null,
    @SerialName("monthly_breakdown")
    val monthlyBreakdown: List<DriverMonthlyBreakdownDto>? = null
) : Dto

/**
 * Driver Cost Breakdown DTO.
 * Shows breakdown by cost type.
 */
@Serializable
data class DriverCostBreakdownDto(
    @SerialName("cost_id")
    val costId: String? = null,
    @SerialName("cost_label")
    val costLabel: String? = null,
    @SerialName("group_id")
    val groupId: String? = null,
    @SerialName("group_name")
    val groupName: String? = null,
    val amount: Double = 0.0,
    val count: Int = 0,
    val percentage: Double = 0.0
) : Dto

/**
 * Driver Monthly Breakdown DTO.
 * Shows earnings/deductions by month.
 */
@Serializable
data class DriverMonthlyBreakdownDto(
    val month: String,
    val year: Int,
    val earnings: Double = 0.0,
    val deductions: Double = 0.0,
    @SerialName("net_amount")
    val netAmount: Double = 0.0,
    @SerialName("trip_count")
    val tripCount: Int = 0
) : Dto

// ==================== Driver Earnings DTOs ====================

/**
 * Driver Earnings API response.
 * Used for GET /drivers/{driver_id}/earnings
 */
@Serializable
data class DriverEarningsApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: DriverEarningsDto? = null
)

/**
 * Driver Earnings DTO.
 * Summary of driver earnings.
 */
@Serializable
data class DriverEarningsDto(
    @SerialName("driver_id")
    val driverId: Int,
    val period: String? = null,
    @SerialName("total_earnings")
    val totalEarnings: Double = 0.0,
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("avg_per_trip")
    val avgPerTrip: Double = 0.0,
    @SerialName("monthly_breakdown")
    val monthlyBreakdown: List<DriverMonthlyEarningsDto>? = null
) : Dto

/**
 * Driver Monthly Earnings DTO.
 */
@Serializable
data class DriverMonthlyEarningsDto(
    val month: String,
    val year: Int,
    val amount: Double = 0.0,
    @SerialName("trip_count")
    val tripCount: Int = 0
) : Dto

