package com.indusjs.fleet.data.repository.trip

import com.indusjs.fleet.core.error.ApiException
import com.indusjs.fleet.core.error.NotAuthenticatedException
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.data.datasource.trip.TripRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.mapper.trip.TripMapper
import com.indusjs.fleet.data.model.trip.CreateTripRequest
import com.indusjs.fleet.data.model.trip.UpdateTripRequest
import com.indusjs.fleet.domain.entity.trip.CreateTripData
import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.entity.trip.TripStatus
import com.indusjs.fleet.domain.repository.trip.TripRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementation of TripRepository.
 * Handles trip CRUD operations via remote data source.
 */
@Inject
class TripRepositoryImpl(
    private val remoteDataSource: TripRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val mapper: TripMapper
) : TripRepository {

    override fun getTrips(): Flow<Result<List<Trip>>> = flow {
        emit(Result.Loading)
        try {
            val token = requireAuthToken()
            val response = remoteDataSource.getTrips(token)

            if (response.success && response.data != null) {
                emit(Result.Success(mapper.mapToDomainList(response.data)))
            } else {
                emit(Result.Error(ApiException(response.message ?: "Failed to get trips"), response.message))
            }
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    override suspend fun getTripById(id: String): Result<Trip> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getTripById(token, id)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Trip not found"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun createTrip(trip: Trip): Result<Trip> {
        // TODO: Implement with proper CreateTripRequest mapping
        return Result.Error(
            ApiException("Create trip not yet implemented"),
            "Create trip not yet implemented"
        )
    }

    override suspend fun createTripWithData(data: CreateTripData): Result<Trip> {
        return try {
            val token = requireAuthToken()
            val request = CreateTripRequest(
                vehicleId = data.vehicleId,
                driverId = data.driverId,
                scheduledDate = data.scheduledDate,
                startTime = data.startTime,
                plannedStart = data.plannedStart,
                plannedEnd = data.plannedEnd,
                startLocation = data.startLocation,
                startLat = data.startLat,
                startLng = data.startLng,
                endLocation = data.endLocation,
                endLat = data.endLat,
                endLng = data.endLng,
                cargoType = data.cargoType,
                cargoDescription = data.cargoDescription,
                cargoWeight = data.cargoWeight,
                customerName = data.customerName,
                customerContact = data.customerContact,
                priority = data.priority,
                notes = data.notes
            )
            val response = remoteDataSource.createTrip(token, request)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to create trip"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun updateTrip(trip: Trip): Result<Trip> {
        return try {
            val token = requireAuthToken()
            val request = UpdateTripRequest(
                startLocation = trip.startLocation?.address,
                startLat = trip.startLocation?.latitude,
                startLng = trip.startLocation?.longitude,
                endLocation = trip.endLocation?.address,
                endLat = trip.endLocation?.latitude,
                endLng = trip.endLocation?.longitude,
                estimatedDistance = trip.distance.takeIf { it > 0 },
                plannedStart = trip.scheduledStartTime,
                plannedEnd = trip.actualEndTime,
                cargoType = trip.cargoType,
                cargoDescription = trip.cargoDescription,
                customerName = trip.customerName,
                priority = trip.priority,
                notes = trip.notes
            )
            val response = remoteDataSource.updateTrip(token, trip.id, request)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to update trip"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun updateTripStatus(id: String, status: TripStatus): Result<Trip> {
        return try {
            val token = requireAuthToken()
            val statusString = TripStatus.toApiString(status)
            val response = remoteDataSource.updateTripStatus(token, id, statusString)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to update trip status"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun cancelTrip(id: String): Result<Unit> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.cancelTrip(token, id)

            if (response.success) {
                Result.Success(Unit)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to cancel trip"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    /**
     * Retrieves auth token or throws NotAuthenticatedException.
     */
    private suspend fun requireAuthToken(): String {
        return userLocalDataSource.getAuthToken()
            ?: throw NotAuthenticatedException()
    }
}
