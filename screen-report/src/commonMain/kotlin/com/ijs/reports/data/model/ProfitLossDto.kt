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
    val success: Boolean,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: T? = null
)

/**
 * Period object in P&L responses
 */
@Serializable
data class PeriodDto(
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null
)

/**
 * Cost Breakdown Item DTO.
 * Updated to include structured cost fields (cost_id, cost_label, group_id).
 */
@Serializable
data class CostBreakdownItemDto(
    // New structured cost fields per API
    @SerialName("cost_id")
    val costId: String? = null,
    @SerialName("cost_label")
    val costLabel: String? = null,
    @SerialName("group_id")
    val groupId: String? = null,
    // Legacy field (kept for backward compatibility)
    @SerialName("cost_type")
    val costType: String,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("percentage")
    val percentage: Double = 0.0
) {
    /**
     * Returns the display label - prefers cost_label, falls back to cost_type.
     */
    val displayLabel: String
        get() = costLabel ?: costType.replace("_", " ").replaceFirstChar { it.uppercase() }
}
