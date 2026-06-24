package com.ijs.reports.domain.repository

import com.indusjs.error.result.Result
import com.ijs.reports.data.model.ConsolidatedPLRequest
import com.ijs.reports.data.model.MultiCostTypePLRequest
import com.ijs.reports.data.model.MultiTripPLRequest
import com.ijs.reports.data.model.MultiVehiclePLRequest
import com.ijs.reports.domain.entity.*
import com.indusjs.fleet.domain.repository.Repository

/**
 * Repository interface for Profit & Loss reports.
 * Defined in the domain layer to be implemented by the data layer.
 */
interface ReportsRepository : Repository {

    /**
     * Get profit/loss for a single trip
     */
    suspend fun getTripProfitLoss(tripId: Int): Result<TripProfitLoss>

    /**
     * Get profit/loss for a single vehicle
     */
    suspend fun getVehicleProfitLoss(vehicleId: Int, period: String = "monthly"): Result<VehicleProfitLoss>

    /**
     * Get fleet-wide profit/loss report
     */
    suspend fun getFleetProfitLoss(
        period: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Result<FleetProfitLoss>

    /**
     * Get profit/loss for multiple vehicles
     */
    suspend fun getMultiVehiclePL(request: MultiVehiclePLRequest): Result<List<VehicleProfitLoss>>

    /**
     * Get profit/loss for multiple trips
     */
    suspend fun getMultiTripPL(request: MultiTripPLRequest): Result<List<TripProfitLoss>>

    /**
     * Get analysis for a single cost type
     */
    suspend fun getCostTypeAnalysis(
        costType: String,
        startDate: Long? = null,
        endDate: Long? = null
    ): Result<CostTypeAnalysis>

    /**
     * Get analysis for multiple cost types
     */
    suspend fun getMultiCostTypeAnalysis(request: MultiCostTypePLRequest): Result<List<CostTypeAnalysis>>

    /**
     * Get consolidated P&L report
     */
    suspend fun getConsolidatedPL(request: ConsolidatedPLRequest): Result<ConsolidatedPL>

    /**
     * Get P&L grouped by customer for a period (daily|weekly|monthly|quarterly|yearly).
     */
    suspend fun getCustomerProfitLoss(period: String): Result<CustomerPLReport>

    /**
     * Get P&L summary with alerts
     * @param startDate Start of range as UTC epoch millis (null = unset)
     * @param endDate End of range as UTC epoch millis (null = unset)
     */
    suspend fun getPLSummary(
        startDate: Long? = null,
        endDate: Long? = null
    ): Result<PLSummary>
}
