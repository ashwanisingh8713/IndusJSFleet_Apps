package com.ijs.reports.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request DTOs for Profit & Loss APIs
 */

/**
 * Request for Multi-Vehicle P&L
 * POST /reports/profit-loss/vehicles
 *
 * Backend MultiVehicleProfitLossRequest binds start_date/end_date as
 * binding:"required" non-pointer int64, so a non-zero epoch-millis value MUST
 * always be sent (null/omitted/0 -> 400). Callers must supply real dates.
 */
@Serializable
data class MultiVehiclePLRequest(
    @SerialName("vehicle_ids")
    val vehicleIds: List<Int>,
    @SerialName("start_date")
    val startDate: Long = 0L,
    @SerialName("end_date")
    val endDate: Long = 0L
)

/**
 * Request for Multi-Trip P&L
 * POST /reports/profit-loss/trips
 */
@Serializable
data class MultiTripPLRequest(
    @SerialName("trip_ids")
    val tripIds: List<Int>? = null,
    @SerialName("vehicle_id")
    val vehicleId: Int? = null,
    @SerialName("start_date")
    val startDate: Long? = null,
    @SerialName("end_date")
    val endDate: Long? = null
)

/**
 * Request for Multi Cost Type Analysis
 * POST /reports/profit-loss/cost-types
 *
 * Backend CostTypeProfitLossRequest binds start_date/end_date as
 * binding:"required" non-pointer int64 -> a non-zero value MUST always be sent.
 * Backend request field is `cost_ids` only (the `cost_types` alias was removed).
 */
@Serializable
data class MultiCostTypePLRequest(
    @SerialName("cost_ids")
    val costIds: List<String>,
    @SerialName("start_date")
    val startDate: Long = 0L,
    @SerialName("end_date")
    val endDate: Long = 0L,
    @SerialName("vehicle_ids")
    val vehicleIds: List<Int>? = null
)

/**
 * Request for Consolidated P&L Report
 * POST /reports/profit-loss/consolidated
 *
 * Backend ConsolidatedProfitLossRequest binds start_date/end_date as
 * binding:"required" non-pointer int64 -> a non-zero value MUST always be sent.
 * group_by is optional; valid values: day|week|month|quarter|year (backend
 * defaults to month).
 */
@Serializable
data class ConsolidatedPLRequest(
    @SerialName("vehicle_ids")
    val vehicleIds: List<Int>? = null,
    @SerialName("trip_ids")
    val tripIds: List<Int>? = null,
    @SerialName("cost_ids")
    val costIds: List<String>? = null,
    @SerialName("start_date")
    val startDate: Long = 0L,
    @SerialName("end_date")
    val endDate: Long = 0L,
    @SerialName("group_by")
    val groupBy: String? = null // "month", "week", "day" — grouping LABEL, not a timestamp
)

