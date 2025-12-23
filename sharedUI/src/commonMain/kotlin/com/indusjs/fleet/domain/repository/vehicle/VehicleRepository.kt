package com.indusjs.fleet.domain.repository.vehicle

import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.repository.Repository
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Vehicle operations.
 * Defined in the domain layer to be implemented by the data layer.
 */
interface VehicleRepository : Repository {

    /**
     * Get all vehicles as a Flow.
     */
    fun getVehicles(): Flow<Result<List<Vehicle>>>

    /**
     * Get a specific vehicle by ID.
     */
    suspend fun getVehicleById(id: String): Result<Vehicle>

    /**
     * Create a new vehicle.
     */
    suspend fun createVehicle(vehicle: Vehicle): Result<Vehicle>

    /**
     * Update an existing vehicle.
     */
    suspend fun updateVehicle(vehicle: Vehicle): Result<Vehicle>

    /**
     * Delete a vehicle by ID.
     */
    suspend fun deleteVehicle(id: String): Result<Unit>
}

