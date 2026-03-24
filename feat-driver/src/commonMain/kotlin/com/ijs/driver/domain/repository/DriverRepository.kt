package com.ijs.driver.domain.repository

import com.indusjs.error.result.Result
import com.indusjs.fleet.data.model.history.DriverHistoryDataDto
import com.indusjs.fleet.data.model.state.StateHistoryResponseDto
import com.ijs.driver.domain.entity.Driver
import com.ijs.driver.domain.entity.DriverStatus
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
     * Valid statuses: inactive, active, on_route, on_leave, suspended, terminated
     */
    suspend fun updateDriverStatus(id: String, status: DriverStatus): Result<Driver>

    /**
     * Update driver status with reason and notes.
     * PATCH /drivers/{id}/status
     *
     * @param id Driver ID
     * @param newStatus New status value (from StatusConstants.DriverState)
     * @param reason Optional reason for status change
     * @param notes Optional notes for status change
     */
    suspend fun updateDriverStatusWithReason(
        id: String,
        newStatus: String,
        reason: String? = null,
        notes: String? = null
    ): Result<Driver>

    /**
     * Toggle driver active state (activate/deactivate).
     */
    suspend fun toggleDriverActive(id: String): Result<Driver>

    /**
     * Delete a driver by ID.
     * Only Owners can delete drivers.
     */
    suspend fun deleteDriver(id: String): Result<Unit>

    // ==================== History APIs ====================

    /**
     * Get driver history with pagination.
     * GET /drivers/{id}/history?page=1&per_page=20
     */
    suspend fun getDriverHistory(
        id: String,
        page: Int = 1,
        perPage: Int = 20
    ): Result<DriverHistoryDataDto>

    /**
     * Get driver state change history.
     * GET /drivers/{id}/state-history
     */
    suspend fun getDriverStateHistory(
        id: String,
        page: Int = 1,
        perPage: Int = 20
    ): Result<StateHistoryResponseDto>
}

