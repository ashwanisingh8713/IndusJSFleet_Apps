package com.indusjs.fleet.data.datasource.dashboard

import com.indusjs.fleet.data.database.dao.DashboardDao
import com.indusjs.fleet.data.datasource.LocalDataSource
import com.indusjs.fleet.data.mapper.dashboard.DashboardCacheMapper
import com.indusjs.fleet.data.model.dashboard.DashboardDataDto
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Local data source interface for Dashboard caching.
 * Abstracts the underlying storage mechanism (Settings-based).
 */
interface DashboardLocalDataSource : LocalDataSource {
    suspend fun saveDashboard(data: DashboardDataDto)
    suspend fun getCachedDashboard(): DashboardDataDto?
    fun observeDashboard(): Flow<DashboardDataDto?>
    suspend fun hasCachedData(): Boolean
    suspend fun clearCache()
    suspend fun getCacheTime(): Long?
}

/**
 * Implementation of DashboardLocalDataSource using Settings-based DAO.
 */
@Inject
class DashboardLocalDataSourceImpl(
    private val dashboardDao: DashboardDao,
    private val mapper: DashboardCacheMapper
) : DashboardLocalDataSource {

    override suspend fun saveDashboard(data: DashboardDataDto) {
        dashboardDao.insertOrUpdate(mapper.toEntity(data))
    }

    override suspend fun getCachedDashboard(): DashboardDataDto? {
        return dashboardDao.getDashboardCache()?.let { mapper.toDto(it) }
    }

    override fun observeDashboard(): Flow<DashboardDataDto?> {
        return dashboardDao.observeDashboardCache().map { it?.let { mapper.toDto(it) } }
    }

    override suspend fun hasCachedData(): Boolean = dashboardDao.hasCachedData()

    override suspend fun clearCache() = dashboardDao.clearCache()

    override suspend fun getCacheTime(): Long? = dashboardDao.getCacheTime()
}
