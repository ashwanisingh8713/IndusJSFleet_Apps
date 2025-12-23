package com.indusjs.fleet.data.repository.vehicle

import com.indusjs.fleet.core.error.ApiException
import com.indusjs.fleet.core.error.NotAuthenticatedException
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.datasource.vehicle.VehicleRemoteDataSource
import com.indusjs.fleet.data.mapper.vehicle.VehicleMapper
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.repository.vehicle.VehicleRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementation of VehicleRepository.
 * Handles vehicle CRUD operations via remote data source.
 */
@Inject
class VehicleRepositoryImpl(
    private val remoteDataSource: VehicleRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val mapper: VehicleMapper
) : VehicleRepository {

    override fun getVehicles(): Flow<Result<List<Vehicle>>> = flow {
        emit(Result.Loading)
        try {
            val token = requireAuthToken()
            val response = remoteDataSource.getVehicles(token)

            if (response.success && response.data != null) {
                emit(Result.Success(mapper.mapToDomainList(response.data)))
            } else {
                emit(Result.Error(ApiException(response.message ?: "Failed to get vehicles"), response.message))
            }
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    override suspend fun getVehicleById(id: String): Result<Vehicle> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getVehicleById(token, id)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Vehicle not found"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun createVehicle(vehicle: Vehicle): Result<Vehicle> {
        return try {
            val token = requireAuthToken()
            val request = mapper.mapToCreateRequest(vehicle)
            val response = remoteDataSource.createVehicle(token, request)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to create vehicle"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun updateVehicle(vehicle: Vehicle): Result<Vehicle> {
        return try {
            val token = requireAuthToken()
            val request = mapper.mapToCreateRequest(vehicle)
            val response = remoteDataSource.updateVehicle(token, vehicle.id, request)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to update vehicle"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun deleteVehicle(id: String): Result<Unit> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.deleteVehicle(token, id)

            if (response.success) {
                Result.Success(Unit)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to delete vehicle"), response.message)
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

