package com.indusjs.fleet.domain.repository.trip

import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.data.model.trip.UpdateTripRequest
import com.indusjs.fleet.domain.entity.trip.CreateTripData
import com.indusjs.fleet.domain.entity.trip.CreateTripStopData
import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.entity.trip.TripStatus
import com.indusjs.fleet.domain.entity.trip.TripStop
import com.indusjs.fleet.domain.entity.trip.UpdateTripStopData
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
     * Create a new trip with CreateTripData.
     */
    suspend fun createTripWithData(data: CreateTripData): Result<Trip>

    /**
     * Update an existing trip.
     */
    suspend fun updateTrip(trip: Trip): Result<Trip>

    /**
     * Update an existing trip with UpdateTripRequest.
     * Supports updating Vehicle, Driver, Schedule, Location, Cargo, Customer, Priority.
     * Only works for trips in 'planned' state.
     */
    suspend fun updateTripWithRequest(id: String, request: UpdateTripRequest): Result<Trip>

    /**
     * Update trip status.
     */
    suspend fun updateTripStatus(id: String, status: TripStatus): Result<Trip>

    /**
     * Update trip progress (for in_progress trips).
     * @param id Trip ID
     * @param coveredDistance Distance covered so far in km
     * @param coveredDurationMinutes Duration covered in minutes
     * @param currentLat Current latitude
     * @param currentLng Current longitude
     */
    suspend fun updateTripProgress(
        id: String,
        coveredDistance: Double? = null,
        coveredDurationMinutes: Long? = null,
        currentLat: Double? = null,
        currentLng: Double? = null
    ): Result<Trip>

    /**
     * Update trip location.
     */
    suspend fun updateTripLocation(id: String, lat: Double, lng: Double): Result<Trip>

    /**
     * Cancel a trip by ID.
     */
    suspend fun cancelTrip(id: String): Result<Unit>

    /**
     * Get trips by vehicle ID.
     */
    suspend fun getTripsByVehicle(vehicleId: String): Result<List<Trip>>

    // ============ TRIP STOPS ============

    /**
     * Get all stops for a trip.
     */
    suspend fun getTripStops(tripId: String): Result<List<TripStop>>

    /**
     * Add a stop to a trip.
     */
    suspend fun createTripStop(tripId: String, data: CreateTripStopData): Result<TripStop>

    /**
     * Update a trip stop.
     */
    suspend fun updateTripStop(tripId: String, stopId: String, data: UpdateTripStopData): Result<TripStop>

    /**
     * Mark a stop as completed.
     */
    suspend fun markStopCompleted(tripId: String, stopId: String): Result<TripStop>

    /**
     * Delete a trip stop.
     */
    suspend fun deleteTripStop(tripId: String, stopId: String): Result<Unit>
}
