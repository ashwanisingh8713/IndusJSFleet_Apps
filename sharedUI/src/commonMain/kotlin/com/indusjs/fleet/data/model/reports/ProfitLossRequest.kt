package com.indusjs.fleet.data.model.reports

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request DTOs for Profit & Loss APIs
 */

/**
 * Request for Multi-Vehicle P&L
 * POST /reports/profit-loss/vehicles
 */
@Serializable
data class MultiVehiclePLRequest(
    @SerialName("vehicle_ids")
    val vehicleIds: List<Int>,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null
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
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null
)

/**
 * Request for Multi Cost Type Analysis
 * POST /reports/profit-loss/cost-types
 */
@Serializable
data class MultiCostTypePLRequest(
    @SerialName("cost_types")
    val costTypes: List<String>,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    @SerialName("vehicle_ids")
    val vehicleIds: List<Int>? = null
)

/**
 * Request for Consolidated P&L Report
 * POST /reports/profit-loss/consolidated
 */
@Serializable
data class ConsolidatedPLRequest(
    @SerialName("vehicle_ids")
    val vehicleIds: List<Int>? = null,
    @SerialName("trip_ids")
    val tripIds: List<Int>? = null,
    @SerialName("cost_types")
    val costTypes: List<String>? = null,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    @SerialName("group_by")
    val groupBy: String? = null // "month", "week", "day"
)

