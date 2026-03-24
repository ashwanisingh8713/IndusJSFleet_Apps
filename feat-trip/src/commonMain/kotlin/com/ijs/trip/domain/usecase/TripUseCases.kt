package com.ijs.trip.domain.usecase

import com.indusjs.error.result.Result
import com.ijs.trip.domain.entity.CreateTripStopData
import com.ijs.trip.domain.entity.Trip
import com.ijs.trip.domain.entity.TripStatus
import com.ijs.trip.domain.entity.TripStop
import com.ijs.trip.domain.entity.UpdateTripStopData
import com.ijs.trip.domain.repository.TripRepository
import com.indusjs.fleet.domain.usecase.UseCase
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting the list of trips.
 */
@Inject
class GetTripsUseCase(
    private val tripRepository: TripRepository
) : UseCase<List<Trip>> {

    override operator fun invoke(): Flow<Result<List<Trip>>> {
        return tripRepository.getTrips()
    }
}

/**
 * Use case for getting a trip by ID.
 */
@Inject
class GetTripByIdUseCase(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(id: String): Result<Trip> {
        return tripRepository.getTripById(id)
    }
}

/**
 * Use case for creating a new trip.
 */
@Inject
class CreateTripUseCase(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(trip: Trip): Result<Trip> {
        return tripRepository.createTrip(trip)
    }
}

/**
 * Use case for creating a new trip with CreateTripData.
 */
@Inject
class CreateTripWithDataUseCase(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(data: com.ijs.trip.domain.entity.CreateTripData): Result<Trip> {
        return tripRepository.createTripWithData(data)
    }
}

/**
 * Use case for updating an existing trip.
 */
@Inject
class UpdateTripUseCase(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(trip: Trip): Result<Trip> {
        return tripRepository.updateTrip(trip)
    }
}

/**
 * Use case for updating trip status.
 */
@Inject
class UpdateTripStatusUseCase(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(id: String, status: TripStatus): Result<Trip> {
        return tripRepository.updateTripStatus(id, status)
    }
}

/**
 * Use case for cancelling a trip.
 */
@Inject
class CancelTripUseCase(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return tripRepository.cancelTrip(id)
    }
}

// ============ TRIP STOPS USE CASES ============

/**
 * Use case for getting stops for a trip.
 */
@Inject
class GetTripStopsUseCase(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(tripId: String): Result<List<TripStop>> {
        return tripRepository.getTripStops(tripId)
    }
}

/**
 * Use case for creating a new trip stop.
 */
@Inject
class CreateTripStopUseCase(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(tripId: String, data: CreateTripStopData): Result<TripStop> {
        return tripRepository.createTripStop(tripId, data)
    }
}

/**
 * Use case for updating a trip stop.
 */
@Inject
class UpdateTripStopUseCase(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(tripId: String, stopId: String, data: UpdateTripStopData): Result<TripStop> {
        return tripRepository.updateTripStop(tripId, stopId, data)
    }
}

/**
 * Use case for marking a stop as completed.
 */
@Inject
class MarkStopCompletedUseCase(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(tripId: String, stopId: String): Result<TripStop> {
        return tripRepository.markStopCompleted(tripId, stopId)
    }
}

/**
 * Use case for deleting a trip stop.
 */
@Inject
class DeleteTripStopUseCase(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(tripId: String, stopId: String): Result<Unit> {
        return tripRepository.deleteTripStop(tripId, stopId)
    }
}

