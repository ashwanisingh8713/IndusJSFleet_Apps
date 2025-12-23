package com.indusjs.fleet.domain.repository.trip

import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.entity.trip.TripStatus
import com.indusjs.fleet.domain.repository.Repository
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Trip operations.
 * Defined in the domain layer to be implemented by the data layer.
 */
interface TripRepository : Repository {

    /**
     * Get all trips as a Flow.
     */
    fun getTrips(): Flow<Result<List<Trip>>>

    /**
     * Get a specific trip by ID.
     */
    suspend fun getTripById(id: String): Result<Trip>

    /**
     * Create a new trip.
     */
    suspend fun createTrip(trip: Trip): Result<Trip>

    /**
     * Update trip status.
     */
    suspend fun updateTripStatus(id: String, status: TripStatus): Result<Trip>

    /**
     * Cancel a trip by ID.
     */
    suspend fun cancelTrip(id: String): Result<Unit>
}

