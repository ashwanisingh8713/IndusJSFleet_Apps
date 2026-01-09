package com.indusjs.fleet.domain.repository.dashboard

import com.indusjs.error.result.Result
import com.indusjs.fleet.data.model.dashboard.CostOverviewFilter
import com.indusjs.fleet.domain.entity.dashboard.AlertsSummary
import com.indusjs.fleet.domain.entity.dashboard.CostOverview
import com.indusjs.fleet.domain.entity.dashboard.DashboardStats
import com.indusjs.fleet.domain.entity.dashboard.DashboardUserInfo
import com.indusjs.fleet.domain.entity.dashboard.PendingPaymentsData
import com.indusjs.fleet.domain.repository.Repository
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for dashboard operations.
 *
 * Supports offline-first strategy:
 * - getDashboard(): Flow that emits cached then fresh data
 * - getCachedDashboard(): One-shot cached data retrieval
 * - refreshDashboard(): Force network refresh
 * - observeDashboard(): Reactive cache observation
 */
interface DashboardRepository : Repository {

    /**
     * Get dashboard with offline-first strategy.
     * Emits cached data first (if available), then fetches fresh from network.
     */
    fun getDashboard(): Flow<Result<DashboardData>>

    /**
     * Get cached dashboard data only.
     */
    suspend fun getCachedDashboard(): Result<DashboardData>

    /**
     * Force refresh from network (bypasses cache-first emission).
     */
    suspend fun refreshDashboard(): Result<DashboardData>

    /**
     * Observe cached dashboard changes reactively.
     */
    fun observeDashboard(): Flow<DashboardData?>

    /**
     * Check if cached data exists.
     */
    suspend fun hasCachedData(): Boolean

    /**
     * Clear all cached dashboard data.
     */
    suspend fun clearCache()

    /**
     * Get cost overview with filter (today/weekly/monthly).
     */
    suspend fun getCostOverview(filter: CostOverviewFilter): Result<CostOverview>

    /**
     * Get pending payments list with pagination.
     */
    suspend fun getPendingPayments(page: Int = 1, perPage: Int = 20): Result<PendingPaymentsData>

    /**
     * Get alerts status with detailed counts by type/priority.
     * Includes document expiry, license expiry, maintenance vehicles, etc.
     */
    suspend fun getAlertsStatus(): Result<AlertsSummary>
}

/**
 * Complete dashboard data including user info and stats.
 *
 * @property userInfo User information (name, role, email)
 * @property stats Dashboard statistics
 * @property isFromCache True if data is from local cache
 * @property cachedAt Timestamp when data was cached (null if fresh)
 */
data class DashboardData(
    val userInfo: DashboardUserInfo,
    val stats: DashboardStats,
    val isFromCache: Boolean = false,
    val cachedAt: Long? = null
)
