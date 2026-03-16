package com.indusjs.fleet.domain.repository.trip

import com.indusjs.error.result.Result
import com.indusjs.fleet.data.model.state.StateHistoryResponseDto
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
     * Only works for trips in 'planned' or 'assigned' state.
     */
    suspend fun updateTripWithRequest(id: String, request: UpdateTripRequest): Result<Trip>

    /**
     * Update trip status (legacy method).
     */
    suspend fun updateTripStatus(id: String, status: TripStatus): Result<Trip>

    /**
     * Update trip state with reason and notes.
     * PATCH /trips/{id}/state
     *
     * @param id Trip ID
     * @param newState New state value (from StatusConstants.TripState)
     * @param reason Optional reason for state change
     * @param notes Optional notes for state change
     *
     * Note: This also updates Vehicle and Driver states automatically:
     * - Trip → on_route: Vehicle → on_route, Driver → on_route
     * - Trip → completed/cancelled/failed: Vehicle → active, Driver → active
     */
    suspend fun updateTripState(
        id: String,
        newState: String,
        reason: String? = null,
        notes: String? = null
    ): Result<Trip>

    /**
     * Get trip state change history.
     * GET /trips/{id}/state-history
     */
    suspend fun getTripStateHistory(
        id: String,
        page: Int = 1,
        perPage: Int = 20
    ): Result<StateHistoryResponseDto>

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
