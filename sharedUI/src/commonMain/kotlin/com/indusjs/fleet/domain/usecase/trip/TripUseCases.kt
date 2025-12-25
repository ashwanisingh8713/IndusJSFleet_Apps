package com.indusjs.fleet.domain.usecase.trip

import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.entity.trip.TripStatus
import com.indusjs.fleet.domain.repository.trip.TripRepository
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
    suspend operator fun invoke(data: com.indusjs.fleet.domain.entity.trip.CreateTripData): Result<Trip> {
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

