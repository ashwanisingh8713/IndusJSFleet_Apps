package com.indusjs.fleet.domain.repository.driver

import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.entity.driver.Driver
import com.indusjs.fleet.domain.entity.driver.DriverStatus
import com.indusjs.fleet.domain.repository.Repository
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Driver operations.
 * Defined in the domain layer to be implemented by the data layer.
 */
interface DriverRepository : Repository {

    /**
     * Get all drivers as a Flow.
     * @param status Optional status filter
     * @param search Optional search query (name, mobile, license)
     */
    fun getDrivers(status: DriverStatus? = null, search: String? = null): Flow<Result<List<Driver>>>

    /**
     * Get available drivers for trip assignment.
     */
    fun getAvailableDrivers(): Flow<Result<List<Driver>>>

    /**
     * Get a specific driver by ID.
     */
    suspend fun getDriverById(id: String): Result<Driver>

    /**
     * Create a new driver.
     * Only Owners and Managers can create drivers.
     */
    suspend fun createDriver(driver: Driver): Result<Driver>

    /**
     * Update an existing driver.
     * Only Owners and Managers can update drivers.
     */
    suspend fun updateDriver(driver: Driver): Result<Driver>

    /**
     * Update driver status.
     * Valid statuses: active, inactive, on_trip, on_leave, suspended
     */
    suspend fun updateDriverStatus(id: String, status: DriverStatus): Result<Driver>

    /**
     * Toggle driver active state (activate/deactivate).
     */
    suspend fun toggleDriverActive(id: String): Result<Driver>

    /**
     * Delete a driver by ID.
     * Only Owners can delete drivers.
     */
    suspend fun deleteDriver(id: String): Result<Unit>
}

