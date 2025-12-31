package com.indusjs.fleet.data.repository.costs

import com.indusjs.fleet.core.error.ApiException
import com.indusjs.fleet.core.error.NetworkException
import com.indusjs.fleet.core.error.NotAuthenticatedException
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.data.datasource.costs.CostsRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.model.costs.BulkCreateMaintenanceCostsRequest
import com.indusjs.fleet.data.model.costs.BulkCreateTripCostsRequest
import com.indusjs.fleet.data.model.costs.CreateMaintenanceCostRequest
import com.indusjs.fleet.data.model.costs.CreateTripCostRequest
import com.indusjs.fleet.data.model.costs.MaintenanceCostDto
import com.indusjs.fleet.data.model.costs.TripCostDto
import com.indusjs.fleet.data.model.costs.TripCostSummaryDto
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

            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to create trip cost"))
            }
        } catch (e: NotAuthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(e.message ?: "Network error"))
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
        } catch (e: NotAuthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(e.message ?: "Network error"))
        }
    }

    override suspend fun getTripCosts(tripId: String): Result<List<TripCostDto>> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getTripCosts(token, tripId)

            if (response.success && response.data != null) {
                Result.Success(response.data.costs)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to fetch trip costs"))
            }
        } catch (e: NotAuthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(e.message ?: "Network error"))
        }
    }

    override suspend fun getTripCostSummary(tripId: String): Result<TripCostSummaryDto> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getTripCostSummary(token, tripId)

            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                // If no costs, return empty summary instead of error
                Result.Success(TripCostSummaryDto())
            }
        } catch (e: NotAuthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(e.message ?: "Network error"))
        }
    }

    override suspend fun createMaintenanceCost(request: CreateMaintenanceCostRequest): Result<MaintenanceCostDto> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.createMaintenanceCost(token, request)

            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to create maintenance cost"))
            }
        } catch (e: NotAuthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(e.message ?: "Network error"))
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
        } catch (e: NotAuthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(e.message ?: "Network error"))
        }
    }

    override suspend fun getMaintenanceCosts(vehicleId: String): Result<List<MaintenanceCostDto>> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getMaintenanceCosts(token, vehicleId)

            if (response.success && response.data != null) {
                Result.Success(response.data.costs)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to fetch maintenance costs"))
            }
        } catch (e: NotAuthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(e.message ?: "Network error"))
        }
    }

    private suspend fun requireAuthToken(): String =
        userLocalDataSource.getAuthToken() ?: throw NotAuthenticatedException()
}
