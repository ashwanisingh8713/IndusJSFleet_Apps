package com.ijs.reports.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Shared DTOs for Profit & Loss APIs.
 * Feature-specific DTOs have been split into:
 * - TripPLDto.kt — Trip P&L DTOs
 * - VehiclePLDto.kt — Vehicle and Fleet P&L DTOs
 * - ConsolidatedPLDto.kt — Consolidated, Cost Analysis DTOs
 * - PLSummaryDto.kt — P&L Summary (Reports Hub) DTOs
 */

/**
 * Response wrapper for P&L APIs
 */
@Serializable
data class ProfitLossResponse<T>(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("message")
    val message: String = "",
    @SerialName("data")
    val data: T? = null
)

/**
 * Period object in P&L responses
 */
@Serializable
data class PeriodDto(
    @SerialName("start_date")
    val startDate: Long? = null,
    @SerialName("end_date")
    val endDate: Long? = null
)

/**
 * Cost Breakdown Item DTO.
 *
 * Mirrors backend domain.CostTypeBreakdown (cost_breakdown in trip / vehicle /
 * fleet / consolidated P&L responses): cost_id, cost_label, group_id (omitempty),
 * amount, count. The backend does NOT send cost_type or percentage here, so both
 * are optional with defaults (cost_type kept only as a defensive fallback;
 * required-no-default would crash the whole response decode when absent).
 */
@Serializable
data class CostBreakdownItemDto(
    // Structured cost fields per backend domain.CostTypeBreakdown
    @SerialName("cost_id")
    val costId: String? = null,
    @SerialName("cost_label")
    val costLabel: String? = null,
    @SerialName("group_id")
    val groupId: String? = null,
    // Legacy/defensive field (backend no longer emits cost_type in cost_breakdown)
    @SerialName("cost_type")
    val costType: String? = null,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("percentage")
    val percentage: Double = 0.0
) {
    /**
     * Returns the display label - prefers cost_label, then cost_id, then cost_type.
     */
    val displayLabel: String
        get() = costLabel
            ?: (costId ?: costType)?.replace("_", " ")?.replaceFirstChar { it.uppercase() }
            ?: ""
}
