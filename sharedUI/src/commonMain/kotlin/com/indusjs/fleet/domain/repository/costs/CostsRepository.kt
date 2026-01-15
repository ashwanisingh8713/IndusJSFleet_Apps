package com.indusjs.fleet.domain.repository.costs

import com.indusjs.error.result.Result
import com.indusjs.fleet.data.model.costs.BulkCreateMaintenanceCostsRequest
import com.indusjs.fleet.data.model.costs.BulkCreateTripCostsRequest
import com.indusjs.fleet.data.model.costs.CreateMaintenanceCostRequest
import com.indusjs.fleet.data.model.costs.CreateTripCostRequest
import com.indusjs.fleet.data.model.costs.MaintenanceCostDto
import com.indusjs.fleet.data.model.costs.TripCostDto
import com.indusjs.fleet.data.model.costs.TripCostSummaryDto
import com.indusjs.fleet.data.model.costs.VehicleMaintenanceCostsDataDto
import com.indusjs.fleet.data.model.costs.VehicleTripCostsDataDto
import com.indusjs.fleet.data.model.driver.DriverCostsListDto
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
     * Get cost summary for a specific trip.
     */
    suspend fun getTripCostSummary(tripId: String): Result<TripCostSummaryDto>

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

    // ==================== Vehicle Costs APIs ====================

    /**
     * Get trip costs for a specific vehicle with pagination and filtering.
     */
    suspend fun getVehicleTripCosts(
        vehicleId: String,
        page: Int = 1,
        perPage: Int = 20,
        costType: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        sortBy: String = "date",
        sortOrder: String = "desc"
    ): Result<VehicleTripCostsDataDto>

    /**
     * Get maintenance costs for a specific vehicle with pagination and filtering.
     */
    suspend fun getVehicleMaintenanceCosts(
        vehicleId: String,
        page: Int = 1,
        perPage: Int = 20,
        costType: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        sortBy: String = "date",
        sortOrder: String = "desc"
    ): Result<VehicleMaintenanceCostsDataDto>

    /**
     * Delete a trip cost by ID.
     */
    suspend fun deleteTripCost(costId: String): Result<Unit>

    /**
     * Delete a maintenance cost by ID.
     */
    suspend fun deleteMaintenanceCost(costId: String): Result<Unit>

    // ==================== Driver Costs APIs ====================

    /**
     * Get costs for a specific driver with pagination and filtering.
     * GET /drivers/{driver_id}/costs
     *
     * @param driverId Driver ID
     * @param page Page number (default: 1)
     * @param perPage Items per page (default: 20)
     * @param groupId Filter by group_id (e.g., DC-G-001 for Salary)
     * @param month Filter by month (format: YYYY-MM)
     * @param startDate Filter by start date (DD-MM-YYYY)
     * @param endDate Filter by end date (DD-MM-YYYY)
     */
    suspend fun getDriverCosts(
        driverId: String,
        page: Int = 1,
        perPage: Int = 20,
        groupId: String? = null,
        month: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<DriverCostsListDto>

    /**
     * Bulk create driver costs for a specific driver.
     * POST /drivers/{driver_id}/costs/bulk
     *
     * @param driverId Driver ID
     * @param request Bulk create request containing list of costs
     */
    suspend fun bulkCreateDriverCosts(
        driverId: String,
        request: com.indusjs.fleet.data.model.driver.BulkCreateDriverCostsRequest
    ): Result<Int>
}
