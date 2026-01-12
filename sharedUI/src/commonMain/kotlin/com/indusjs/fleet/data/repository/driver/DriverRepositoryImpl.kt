package com.indusjs.fleet.data.repository.driver

import com.indusjs.error.exception.ApiException
import com.indusjs.error.exception.AuthException
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.error.result.Result
import com.indusjs.fleet.data.datasource.driver.DriverRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.mapper.driver.DriverMapper
import com.indusjs.fleet.data.model.history.DriverHistoryDataDto
import com.indusjs.fleet.domain.entity.driver.Driver
import com.indusjs.fleet.domain.entity.driver.DriverStatus
import com.indusjs.fleet.domain.repository.driver.DriverRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementation of DriverRepository.
 * Handles driver CRUD operations via remote data source.
 */
@Inject
class DriverRepositoryImpl(
    private val remoteDataSource: DriverRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val mapper: DriverMapper
) : DriverRepository {

    override fun getDrivers(status: DriverStatus?, search: String?): Flow<Result<List<Driver>>> = flow {
        emit(Result.Loading)
        try {
            val token = requireAuthToken()
            val statusParam = status?.let { DriverStatus.toApiString(it) }
            val response = remoteDataSource.getDrivers(
                token = token,
                status = statusParam,
                search = search
            )

            if (response.success && response.data != null) {
                emit(Result.Success(mapper.mapToDomainList(response.data)))
            } else {
                emit(Result.Error(ApiException(response.message ?: "Failed to get drivers"), response.message))
            }
        } catch (e: Exception) {
            emit(Result.Error(e, ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override fun getAvailableDrivers(): Flow<Result<List<Driver>>> = flow {
        emit(Result.Loading)
        try {
            val token = requireAuthToken()
            val response = remoteDataSource.getAvailableDrivers(token)

            if (response.success && response.data != null) {
                emit(Result.Success(mapper.mapToDomainList(response.data)))
            } else {
                emit(Result.Error(ApiException(response.message ?: "Failed to get available drivers"), response.message))
            }
        } catch (e: Exception) {
            emit(Result.Error(e, ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override suspend fun getDriverById(id: String): Result<Driver> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getDriverById(token, id)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Driver not found"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun createDriver(driver: Driver): Result<Driver> {
        return try {
            val token = requireAuthToken()
            val request = mapper.mapToCreateRequest(driver)
            co.touchlab.kermit.Logger.d("DriverRepository") { "Creating driver with request: $request" }
            val response = remoteDataSource.createDriver(token, request)
            co.touchlab.kermit.Logger.d("DriverRepository") { "Create driver response - success: ${response.success}, message: ${response.message}" }

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                val errorMessage = response.message ?: "Failed to create driver"
                co.touchlab.kermit.Logger.e("DriverRepository") { "Create driver failed: $errorMessage" }
                Result.Error(ApiException(errorMessage), errorMessage)
            }
        } catch (e: Exception) {
            co.touchlab.kermit.Logger.e("DriverRepository", e) { "Exception creating driver: ${e.message}" }
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun updateDriver(driver: Driver): Result<Driver> {
        return try {
            val token = requireAuthToken()
            val request = mapper.mapToUpdateRequest(driver)
            val response = remoteDataSource.updateDriver(token, driver.id, request)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to update driver"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun updateDriverStatus(id: String, status: DriverStatus): Result<Driver> {
        return try {
            val token = requireAuthToken()
            val statusString = DriverStatus.toApiString(status)
            val response = remoteDataSource.updateDriverStatus(token, id, statusString)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to update driver status"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun toggleDriverActive(id: String): Result<Driver> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.toggleDriverActive(token, id)

            if (response.success && response.data != null) {
                Result.Success(mapper.mapToDomain(response.data))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to toggle driver active state"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun deleteDriver(id: String): Result<Unit> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.deleteDriver(token, id)

            if (response.success) {
                Result.Success(Unit)
            } else {
                Result.Error(ApiException(response.message ?: "Failed to delete driver"), response.message)
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun getDriverHistory(
        id: String,
        page: Int,
        perPage: Int
    ): Result<DriverHistoryDataDto> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getDriverHistory(token, id, page, perPage)

            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(
                    ApiException(response.message ?: "Failed to get driver history"),
                    response.message
                )
            }
        } catch (e: Exception) {
            Result.Error(e, ApiErrorHandler.extractErrorMessage(e))
        }
    }

    /**
     * Retrieves auth token or throws AuthException.
     */
    private suspend fun requireAuthToken(): String {
        return userLocalDataSource.getAuthToken()
            ?: throw AuthException.unauthenticated()
    }
}

