package com.indusjs.fleet.domain.repository.dashboard

import com.indusjs.fleet.domain.entity.dashboard.DashboardStats
import com.indusjs.fleet.domain.entity.dashboard.DashboardUserInfo
import com.indusjs.fleet.domain.repository.Repository

/**
 * Repository interface for dashboard operations.
 */
interface DashboardRepository : Repository {

    /**
     * Get unified dashboard data.
     * Auto-detects user role and returns appropriate statistics.
     */
    suspend fun getDashboard(): Result<DashboardData>
}

/**
 * Complete dashboard data including user info and stats.
 */
data class DashboardData(
    val userInfo: DashboardUserInfo,
    val stats: DashboardStats
)

