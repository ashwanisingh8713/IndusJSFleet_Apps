package com.indusjs.fleet.data.repository.costs

import com.indusjs.error.exception.ApiException
import com.indusjs.error.exception.AuthException
import com.indusjs.error.exception.NetworkException
import com.indusjs.fleet.core.auth.AuthTokenHelper
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.error.result.Result
import com.indusjs.fleet.data.datasource.costs.CostsRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.model.costs.BulkCreateMaintenanceCostsRequest
import com.indusjs.fleet.data.model.costs.BulkCreateTripCostsRequest
import com.indusjs.fleet.data.model.costs.CreateMaintenanceCostRequest
import com.indusjs.fleet.data.model.costs.CreateTripCostRequest
import com.indusjs.fleet.data.model.costs.MaintenanceCostDto
import com.indusjs.fleet.data.model.costs.TripCostDto
import com.indusjs.fleet.data.model.costs.TripCostSummaryDto
import com.indusjs.fleet.data.model.costs.VehicleMaintenanceCostsDataDto
import com.indusjs.fleet.data.model.costs.VehicleTripCostsDataDto
import com.indusjs.fleet.data.model.driver.DriverCostsListDto
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import dev.zacsweers.metro.Inject

/**
 * Implementation of CostsRepository.
 */
@Inject
class CostsRepositoryImpl(
    private val remoteDataSource: CostsRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource
) : CostsRepository {

    override suspend fun createTripCost(request: CreateTripCostRequest): Result<TripCostDto> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.createTripCost(token, request)
            val data = response.data

            if (response.success && data != null) {
                Result.Success(data)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to create trip cost"))
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override suspend fun bulkCreateTripCosts(tripId: String, request: BulkCreateTripCostsRequest): Result<Int> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.bulkCreateTripCosts(token, tripId, request)

            if (response.success) {
                Result.Success(response.data?.created ?: request.costs.size)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to create trip costs"))
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override suspend fun getTripCosts(tripId: String): Result<List<TripCostDto>> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getTripCosts(token, tripId)
            val data = response.data

            if (response.success && data != null) {
                Result.Success(data.costs)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to fetch trip costs"))
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override suspend fun getTripCostSummary(tripId: String): Result<TripCostSummaryDto> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getTripCostSummary(token, tripId)
            val data = response.data

            if (response.success && data != null) {
                Result.Success(data)
            } else {
                // If no costs, return empty summary instead of error
                Result.Success(TripCostSummaryDto())
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override suspend fun createMaintenanceCost(request: CreateMaintenanceCostRequest): Result<MaintenanceCostDto> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.createMaintenanceCost(token, request)
            val data = response.data

            if (response.success && data != null) {
                Result.Success(data)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to create maintenance cost"))
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override suspend fun bulkCreateMaintenanceCosts(vehicleId: String, request: BulkCreateMaintenanceCostsRequest): Result<Int> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.bulkCreateMaintenanceCosts(token, vehicleId, request)

            if (response.success) {
                Result.Success(response.data?.created ?: request.costs.size)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to create maintenance costs"))
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override suspend fun getMaintenanceCosts(vehicleId: String): Result<List<MaintenanceCostDto>> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getMaintenanceCosts(token, vehicleId)
            val data = response.data

            if (response.success && data != null) {
                Result.Success(data.costs)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to fetch maintenance costs"))
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    // ==================== Vehicle Costs APIs ====================

    override suspend fun getVehicleTripCosts(
        vehicleId: String,
        page: Int,
        perPage: Int,
        costType: String?,
        startDate: String?,
        endDate: String?,
        sortBy: String,
        sortOrder: String
    ): Result<VehicleTripCostsDataDto> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getVehicleTripCosts(
                token = token,
                vehicleId = vehicleId,
                page = page,
                perPage = perPage,
                costType = costType,
                startDate = startDate,
                endDate = endDate,
                sortBy = sortBy,
                sortOrder = sortOrder
            )
            val data = response.data

            if (response.success && data != null) {
                Result.Success(data)
            } else {
                // Return empty data instead of error
                Result.Success(VehicleTripCostsDataDto())
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override suspend fun getVehicleMaintenanceCosts(
        vehicleId: String,
        page: Int,
        perPage: Int,
        costType: String?,
        startDate: String?,
        endDate: String?,
        sortBy: String,
        sortOrder: String
    ): Result<VehicleMaintenanceCostsDataDto> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getVehicleMaintenanceCosts(
                token = token,
                vehicleId = vehicleId,
                page = page,
                perPage = perPage,
                costType = costType,
                startDate = startDate,
                endDate = endDate,
                sortBy = sortBy,
                sortOrder = sortOrder
            )
            val data = response.data

            if (response.success && data != null) {
                Result.Success(data)
            } else {
                // Return empty data instead of error
                Result.Success(VehicleMaintenanceCostsDataDto())
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override suspend fun deleteTripCost(costId: String): Result<Unit> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.deleteTripCost(token, costId)

            if (response.success) {
                Result.Success(Unit)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to delete trip cost"))
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override suspend fun deleteMaintenanceCost(costId: String): Result<Unit> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.deleteMaintenanceCost(token, costId)

            if (response.success) {
                Result.Success(Unit)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to delete maintenance cost"))
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    // ==================== Driver Costs APIs ====================

    override suspend fun getDriverCosts(
        driverId: String,
        page: Int,
        perPage: Int,
        groupId: String?,
        month: String?,
        startDate: String?,
        endDate: String?
    ): Result<DriverCostsListDto> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getDriverCosts(
                token = token,
                driverId = driverId,
                page = page,
                perPage = perPage,
                groupId = groupId,
                month = month,
                startDate = startDate,
                endDate = endDate
            )
            val envelope = response.data

            if (response.success && envelope != null) {
                // Backend response is double-nested: { data: { data: { costs, summary }, page, total, ... } }
                // Flatten into the legacy DriverCostsListDto consumed by the app.
                val payload = envelope.payload
                Result.Success(
                    DriverCostsListDto(
                        costs = payload?.costs.orEmpty(),
                        summary = payload?.summary,
                        page = envelope.page,
                        perPage = envelope.perPage,
                        total = envelope.total,
                        totalPages = envelope.totalPages,
                        hasMore = envelope.hasMore
                    )
                )
            } else if (response.success) {
                Result.Success(DriverCostsListDto())
            } else {
                Result.Error(
                    ApiException(response.message ?: "Failed to load driver costs")
                )
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override suspend fun createDriverCost(
        driverId: String,
        request: com.indusjs.fleet.data.model.driver.CreateDriverCostRequest
    ): Result<com.indusjs.fleet.data.model.driver.DriverCostDto> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.createDriverCost(token, driverId, request)
            val data = response.data

            if (response.success && data != null) {
                Result.Success(data)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to create driver cost"))
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override suspend fun bulkCreateDriverCosts(
        driverId: String,
        request: com.indusjs.fleet.data.model.driver.BulkCreateDriverCostsRequest
    ): Result<Int> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.bulkCreateDriverCosts(token, driverId, request)

            if (response.success) {
                Result.Success(response.data?.created ?: request.costs.size)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to create driver costs"))
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    private suspend fun requireAuthToken(): String =
        AuthTokenHelper.requireAuthTokenOrRedirect { userLocalDataSource.getAuthToken() }
}
