package com.indusjs.fleet.domain.usecase.dashboard

import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.repository.dashboard.DashboardData
import com.indusjs.fleet.domain.repository.dashboard.DashboardRepository
import com.indusjs.fleet.domain.usecase.UseCase
import com.indusjs.fleet.domain.usecase.SuspendUseCase
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Get dashboard with offline-first strategy.
 * Emits cached data first, then fresh data from network.
 */
@Inject
class GetDashboardUseCase(
    private val repository: DashboardRepository
) : UseCase<DashboardData> {
    override fun invoke(): Flow<Result<DashboardData>> = repository.getDashboard()
}

/**
 * Get cached dashboard data only (no network).
 */
@Inject
class GetCachedDashboardUseCase(
    private val repository: DashboardRepository
) : SuspendUseCase<DashboardData> {
    override suspend fun invoke(): Result<DashboardData> = repository.getCachedDashboard()
}

/**
 * Force refresh from network.
 */
@Inject
class RefreshDashboardUseCase(
    private val repository: DashboardRepository
) : SuspendUseCase<DashboardData> {
    override suspend fun invoke(): Result<DashboardData> = repository.refreshDashboard()
}

/**
 * Observe cached dashboard changes reactively.
 */
@Inject
class ObserveDashboardUseCase(
    private val repository: DashboardRepository
) {
    operator fun invoke(): Flow<DashboardData?> = repository.observeDashboard()
}

/**
 * Check if cached data exists.
 */
@Inject
class HasCachedDashboardUseCase(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(): Boolean = repository.hasCachedData()
}

/**
 * Clear dashboard cache.
 */
@Inject
class ClearDashboardCacheUseCase(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke() = repository.clearCache()
}
