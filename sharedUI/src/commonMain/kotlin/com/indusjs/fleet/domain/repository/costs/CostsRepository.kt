package com.indusjs.fleet.domain.repository.costs

import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.data.model.costs.BulkCreateMaintenanceCostsRequest
import com.indusjs.fleet.data.model.costs.BulkCreateTripCostsRequest
import com.indusjs.fleet.data.model.costs.CreateMaintenanceCostRequest
import com.indusjs.fleet.data.model.costs.CreateTripCostRequest
import com.indusjs.fleet.data.model.costs.MaintenanceCostDto
import com.indusjs.fleet.data.model.costs.TripCostDto
import com.indusjs.fleet.domain.repository.Repository

/**
 * Repository interface for cost operations.
 */
interface CostsRepository : Repository {

    /**
     * Create a new trip cost entry.
     */
    suspend fun createTripCost(request: CreateTripCostRequest): Result<TripCostDto>

    /**
     * Bulk create trip costs for a specific trip.
     */
    suspend fun bulkCreateTripCosts(tripId: String, request: BulkCreateTripCostsRequest): Result<Int>

    /**
     * Get costs for a specific trip.
     */
    suspend fun getTripCosts(tripId: String): Result<List<TripCostDto>>

    /**
     * Create a new maintenance cost entry.
     */
    suspend fun createMaintenanceCost(request: CreateMaintenanceCostRequest): Result<MaintenanceCostDto>

    /**
     * Bulk create maintenance costs for a specific vehicle.
     */
    suspend fun bulkCreateMaintenanceCosts(vehicleId: String, request: BulkCreateMaintenanceCostsRequest): Result<Int>

    /**
     * Get maintenance costs for a specific vehicle.
     */
    suspend fun getMaintenanceCosts(vehicleId: String): Result<List<MaintenanceCostDto>>
}
