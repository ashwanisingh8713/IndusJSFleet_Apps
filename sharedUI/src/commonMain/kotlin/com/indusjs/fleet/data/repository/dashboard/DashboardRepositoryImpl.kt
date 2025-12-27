package com.indusjs.fleet.data.repository.dashboard

import com.indusjs.fleet.core.error.ApiException
import com.indusjs.fleet.core.error.NetworkException
import com.indusjs.fleet.core.error.NotAuthenticatedException
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.data.datasource.dashboard.DashboardLocalDataSource
import com.indusjs.fleet.data.datasource.dashboard.DashboardRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.mapper.dashboard.DashboardMapper.toDomain
import com.indusjs.fleet.data.model.dashboard.DashboardDataDto
import com.indusjs.fleet.domain.repository.dashboard.DashboardData
import com.indusjs.fleet.domain.repository.dashboard.DashboardRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Repository implementing offline-first strategy for Dashboard.
 *
 * Data flow:
 * 1. Emit cached data immediately (instant UI)
 * 2. Fetch fresh data from network
 * 3. On success: cache and emit fresh data
 * 4. On error with cache: keep cached data visible
 * 5. On error without cache: emit error
 */
@Inject
class DashboardRepositoryImpl(
    private val remoteDataSource: DashboardRemoteDataSource,
    private val localDataSource: DashboardLocalDataSource,
    private val userLocalDataSource: UserLocalDataSource
) : DashboardRepository {

    override fun getDashboard(): Flow<Result<DashboardData>> = flow {
        val cached = localDataSource.getCachedDashboard()
        val cacheTime = localDataSource.getCacheTime()

        // Emit cached data first for instant UI
        if (cached != null) {
            emit(Result.Success(cached.toDashboardData(isFromCache = true, cachedAt = cacheTime)))
        } else {
            emit(Result.Loading)
        }

        // Fetch fresh data
        try {
            val token = requireAuthToken()
            val response = remoteDataSource.getDashboard(token)

            when {
                response.success && response.data != null -> {
                    localDataSource.saveDashboard(response.data)
                    emit(Result.Success(response.data.toDashboardData(isFromCache = false)))
                }
                cached == null -> {
                    emit(Result.Error(ApiException(response.message ?: "Failed to fetch dashboard")))
                }
            }
        } catch (e: NotAuthenticatedException) {
            emit(Result.Error(e))
        } catch (e: Exception) {
            if (cached == null) {
                emit(Result.Error(NetworkException(e.message ?: "Network error")))
            }
        }
    }

    override suspend fun getCachedDashboard(): Result<DashboardData> {
        val cached = localDataSource.getCachedDashboard()
        return if (cached != null) {
            Result.Success(cached.toDashboardData(isFromCache = true, cachedAt = localDataSource.getCacheTime()))
        } else {
            Result.Error(NetworkException("No cached data available"))
        }
    }

    override suspend fun refreshDashboard(): Result<DashboardData> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getDashboard(token)

            if (response.success && response.data != null) {
                localDataSource.saveDashboard(response.data)
                Result.Success(response.data.toDashboardData(isFromCache = false))
            } else {
                Result.Error(ApiException(response.message ?: "Failed to refresh"))
            }
        } catch (e: NotAuthenticatedException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(e.message ?: "Network error"))
        }
    }

    override fun observeDashboard(): Flow<DashboardData?> {
        return localDataSource.observeDashboard().map { dto ->
            dto?.toDashboardData(isFromCache = true, cachedAt = localDataSource.getCacheTime())
        }
    }

    override suspend fun hasCachedData(): Boolean = localDataSource.hasCachedData()

    override suspend fun clearCache() = localDataSource.clearCache()

    private fun DashboardDataDto.toDashboardData(isFromCache: Boolean, cachedAt: Long? = null) =
        DashboardData(
            userInfo = userInfo.toDomain(),
            stats = toDomain(),
            isFromCache = isFromCache,
            cachedAt = cachedAt
        )

    private suspend fun requireAuthToken(): String =
        userLocalDataSource.getAuthToken() ?: throw NotAuthenticatedException()
}

