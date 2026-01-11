package com.indusjs.fleet.domain.repository.reports

import com.indusjs.error.result.Result
import com.indusjs.fleet.data.model.reports.ConsolidatedPLRequest
import com.indusjs.fleet.data.model.reports.MultiCostTypePLRequest
import com.indusjs.fleet.data.model.reports.MultiTripPLRequest
import com.indusjs.fleet.data.model.reports.MultiVehiclePLRequest
import com.indusjs.fleet.domain.entity.reports.*
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
        startDate: String? = null,
        endDate: String? = null
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
        startDate: String? = null,
        endDate: String? = null
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
     * Get P&L summary with alerts
     * @param period Period filter: "today", "weekly", "monthly", "yearly"
     * @param startDate Custom start date (for custom period)
     * @param endDate Custom end date (for custom period)
     */
    suspend fun getPLSummary(
        startDate: String? = null,
        endDate: String? = null,
        period: String? = null
    ): Result<PLSummary>
}
