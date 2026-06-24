package com.ijs.trip.data.repository

import com.indusjs.error.exception.ApiException
import com.indusjs.fleet.core.auth.AuthTokenHelper
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.error.result.Result
import com.ijs.trip.data.datasource.TripRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.ijs.trip.data.mapper.TripMapper
import com.ijs.trip.data.mapper.TripStopMapper
import com.indusjs.fleet.data.model.state.StateHistoryResponseDto
import com.ijs.trip.data.model.CreateTripRequest
import com.ijs.trip.data.model.UpdateTripRequest
import com.ijs.trip.domain.entity.CreateTripData
import com.ijs.trip.domain.entity.CreateTripStopData
import com.ijs.trip.domain.entity.Trip
import com.ijs.trip.domain.entity.TripStatus
import com.ijs.trip.domain.entity.TripStop
import com.ijs.trip.domain.entity.UpdateTripStopData
import com.ijs.trip.domain.repository.TripRepository
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
    private val mapper: TripMapper,
    private val stopMapper: TripStopMapper
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
            emit(Result.Error(e, ApiErrorHandler.extractErrorMessage(e)))
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
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
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
            // Backend requires a managed customer_id (binding:required, rejects 0).
            val customerId = data.customerId
            if (customerId == null || customerId == 0) {
                return Result.Error(
                    ApiException("A customer must be selected"),
                    "A customer must be selected"
                )
            }
            // Backend requires non-zero start/end coordinates (float64 binding:required).
            val startLat = data.startLat
            val startLng = data.startLng
            val endLat = data.endLat
            val endLng = data.endLng
            if (startLat == null || startLng == null || endLat == null || endLng == null) {
                return Result.Error(
                    ApiException("Start and end locations must have coordinates"),
                    "Start and end locations must have coordinates"
                )
            }
            val request = CreateTripRequest(
                vehicleId = data.vehicleId,
                driverId = data.driverId,
                // Backend requires planned_start, planned_end, scheduled_date, start_time
                plannedStart = data.plannedStart,
                plannedEnd = data.plannedEnd,
                scheduledDate = data.scheduledDate ?: data.plannedStart,
                startTime = data.startTime ?: data.plannedStart,
                deliveryDate = data.deliveryDate,
                deliveryTime = data.deliveryTime,
                startLocation = data.startLocation,
                startLat = startLat,
                startLng = startLng,
                endLocation = data.endLocation,
                endLat = endLat,
                endLng = endLng,
                estimatedDistance = data.estimatedDistance,
                cargoType = data.cargoType,
                cargoDescription = data.cargoDescription,
                cargoLoadingWeight = data.cargoLoadingWeight,
                cargoUnloadingWeight = data.cargoUnloadingWeight,
                vehicleWeight = data.vehicleWeight,
                weightUnit = data.weightUnit,
                fuelType = data.fuelType,
                filledFuelQuantity = data.filledFuelQuantity,
                usedFuelQuantity = data.usedFuelQuantity,
                fuelRate = data.fuelRate,
                kmPerLiter = data.kmPerLiter,
                purchasePrice = data.purchasePrice,
                // selling_value = the ACTUAL price (revenue). Defaults to the quoted
                // tripPrice when the user did not override it.
                sellingValue = data.sellingValue ?: data.tripPrice,
                estimatedExpense = data.estimatedExpense,
                tripPrice = data.tripPrice,
                // payment_status / pending_amount / payment_mode are derived server-side.
                // Customer - backend requires customer_id only; it snapshots name/contact server-side.
                customerId = customerId,
                // Consignee / delivery (receiver) details — backend requires all three on create.
                deliveryAddress = data.deliveryAddress,
                deliveryPersonName = data.deliveryPersonName,
                deliveryContactNumber = data.deliveryContactNumber,
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
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
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
                cargoType = trip.cargoType,
                cargoDescription = trip.cargoDescription,
                // Backend update binds customer_id only (re-snapshots name/contact server-side).
                customerId = trip.customerId?.toIntOrNull(),
                // purchase_price (COGS) — forwarded on update too, not just create.
                purchasePrice = trip.purchasePrice,
                tripPrice = trip.tripPrice,
                // selling_value = the ACTUAL price (revenue). This Trip-based builder has no
                // separate actual field, so it falls back to the quoted tripPrice.
                sellingValue = trip.sellingValue ?: trip.tripPrice,
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
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun updateTripWithRequest(id: String, request: UpdateTripRequest): Result<Trip> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.updateTrip(token, id, request)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to update trip"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
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
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun updateTripProgress(
        id: String,
        coveredDistance: Double?,
        coveredDurationMinutes: Long?,
        currentLat: Double?,
        currentLng: Double?
    ): Result<Trip> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.updateTripProgress(
                token = token,
                tripId = id,
                coveredDistance = coveredDistance,
                coveredDurationMinutes = coveredDurationMinutes,
                currentLat = currentLat,
                currentLng = currentLng
            )

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to update trip progress"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun updateTripLocation(id: String, lat: Double, lng: Double): Result<Trip> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.updateTripLocation(token, id, lat, lng)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to update trip location"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
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
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun getTripsByVehicle(vehicleId: String): Result<List<Trip>> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getTripsByVehicleId(token, vehicleId)

            if (response.success && response.data != null) {
                Result.Success(response.data.map { mapper.mapToDomain(it) })
            } else {
                Result.Error(ApiException(response.message ?: "Failed to fetch trips"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    // ============ TRIP STOPS ============

    override suspend fun getTripStops(tripId: String): Result<List<TripStop>> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getTripStops(token, tripId)

            if (response.success && response.data != null) {
                Result.Success(stopMapper.mapToDomainList(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to get trip stops"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun createTripStop(tripId: String, data: CreateTripStopData): Result<TripStop> {
        return try {
            val token = requireAuthToken()
            val request = stopMapper.mapToRequest(data)
            val response = remoteDataSource.createTripStop(token, tripId, request)

            if (response.success && response.data != null) {
                Result.Success(stopMapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to create trip stop"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun updateTripStop(tripId: String, stopId: String, data: UpdateTripStopData): Result<TripStop> {
        return try {
            val token = requireAuthToken()
            val request = stopMapper.mapToUpdateRequest(data)
            val response = remoteDataSource.updateTripStop(token, tripId, stopId, request)

            if (response.success && response.data != null) {
                Result.Success(stopMapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to update trip stop"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun markStopCompleted(tripId: String, stopId: String): Result<TripStop> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.markStopCompleted(token, tripId, stopId)

            if (response.success && response.data != null) {
                Result.Success(stopMapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to mark stop completed"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun deleteTripStop(tripId: String, stopId: String): Result<Unit> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.deleteTripStop(token, tripId, stopId)

            if (response.success) {
                Result.Success(Unit)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to delete trip stop"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    // ==================== State Management ====================

    override suspend fun updateTripState(
        id: String,
        newState: String,
        reason: String?,
        notes: String?
    ): Result<Trip> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.updateTripState(token, id, newState, reason, notes)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(
                    ApiException(response.message ?: "Failed to update trip state"),
                    response.message
                )
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun getTripStateHistory(
        id: String,
        page: Int,
        perPage: Int
    ): Result<StateHistoryResponseDto> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getTripStateHistory(token, id, page, perPage)

            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(
                    ApiException(response.message ?: "Failed to get trip state history"),
                    response.message
                )
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    /**
     * Retrieves auth token or emits session expired event and throws AuthException.
     */
    private suspend fun requireAuthToken(): String {
        return AuthTokenHelper.requireAuthTokenOrRedirect {
            userLocalDataSource.getAuthToken()
        }
    }
}
