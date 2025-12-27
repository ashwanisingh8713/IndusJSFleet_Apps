package com.indusjs.fleet.data.repository.dashboard

import com.indusjs.fleet.core.error.ApiException
import com.indusjs.fleet.core.error.NotAuthenticatedException
import com.indusjs.fleet.data.datasource.dashboard.DashboardRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.mapper.dashboard.DashboardMapper.toDomain
import com.indusjs.fleet.domain.repository.dashboard.DashboardData
import com.indusjs.fleet.domain.repository.dashboard.DashboardRepository
import dev.zacsweers.metro.Inject

/**
 * Implementation of DashboardRepository.
 * Handles dashboard data fetching from remote API.
 */
@Inject
class DashboardRepositoryImpl(
    private val remoteDataSource: DashboardRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource
) : DashboardRepository {

    override suspend fun getDashboard(): Result<DashboardData> = runCatching {
        val token = requireAuthToken()
        val response = remoteDataSource.getDashboard(token)

        if (!response.success || response.data == null) {
            throw ApiException(response.message ?: "Failed to fetch dashboard")
        }

        val dashboardData = response.data
        DashboardData(
            userInfo = dashboardData.userInfo.toDomain(),
            stats = dashboardData.toDomain()
        )
    }

    /**
     * Retrieves auth token or throws NotAuthenticatedException.
     */
    private suspend fun requireAuthToken(): String {
        return userLocalDataSource.getAuthToken()
            ?: throw NotAuthenticatedException()
    }
}

