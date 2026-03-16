package com.indusjs.fleet.data.datasource.dashboard

import com.indusjs.fleet.data.datasource.LocalDataSource
import com.indusjs.fleet.data.model.dashboard.DashboardDataDto
import kotlinx.coroutines.flow.Flow

/**
 * Local data source interface for Dashboard caching.
 * Abstracts the underlying storage mechanism.
 *
 * NOTE: Interface lives in ijs-network-lib.
 * Implementation (DashboardLocalDataSourceImpl) lives in sharedUI (uses Room DAOs).
 */
interface DashboardLocalDataSource : LocalDataSource {
    suspend fun saveDashboard(data: DashboardDataDto)
    suspend fun getCachedDashboard(): DashboardDataDto?
    fun observeDashboard(): Flow<DashboardDataDto?>
    suspend fun hasCachedData(): Boolean
    suspend fun clearCache()
    suspend fun getCacheTime(): Long?
}

