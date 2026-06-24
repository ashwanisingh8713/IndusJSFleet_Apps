package com.indusjs.fleet.data.repository.dashboard

import com.indusjs.error.exception.ApiException
import com.indusjs.error.exception.AuthException
import com.indusjs.error.exception.NetworkException
import com.indusjs.fleet.core.auth.AuthTokenHelper
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.fleet.core.util.formatDateToHumanReadable
import com.indusjs.error.result.Result
import com.indusjs.fleet.data.datasource.dashboard.DashboardLocalDataSource
import com.indusjs.fleet.data.datasource.dashboard.DashboardRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.mapper.dashboard.DashboardMapper.toDomain
import com.indusjs.fleet.data.model.dashboard.CostOverviewFilter
import com.indusjs.fleet.data.model.dashboard.DashboardDataDto
import com.indusjs.fleet.data.model.dashboard.FinancialPeriod
import com.indusjs.fleet.domain.entity.dashboard.AlertsSummary
import com.indusjs.fleet.domain.entity.dashboard.CostBreakdownItem
import com.indusjs.fleet.domain.entity.dashboard.CostOverview
import com.indusjs.fleet.domain.entity.dashboard.FinancialSummary
import com.indusjs.fleet.domain.entity.dashboard.PendingPayment
import com.indusjs.fleet.domain.entity.dashboard.PendingPaymentsData
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
        } catch (e: AuthException) {
            emit(Result.Error(e))
        } catch (e: Exception) {
            if (cached == null) {
                emit(Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e))))
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
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override fun observeDashboard(): Flow<DashboardData?> {
        return localDataSource.observeDashboard().map { dto ->
            dto?.toDashboardData(isFromCache = true, cachedAt = localDataSource.getCacheTime())
        }
    }

    override suspend fun hasCachedData(): Boolean = localDataSource.hasCachedData()

    override suspend fun clearCache() = localDataSource.clearCache()

    override suspend fun getCostOverview(filter: CostOverviewFilter): Result<CostOverview> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getCostOverview(token, filter)

            if (response.success && response.data != null) {
                val data = response.data
                Result.Success(
                    CostOverview(
                        // Use the requested filter; wire payload carries `period`.
                        filter = filter.value,
                        periodLabel = "",
                        totalExpenses = data.totalExpenses,
                        totalRevenue = data.totalRevenue,
                        totalProfit = data.totalProfit,
                        totalLoss = data.totalLoss,
                        netProfitLoss = data.netProfitLoss,
                        // Backend reports total_profit/total_loss/net_profit_loss;
                        // derive a single signed profit/loss figure for the UI.
                        profitLoss = data.netProfitLoss,
                        isProfit = data.netProfitLoss >= 0.0,
                        completedTrips = data.completedTrips,
                        fuelExpenses = data.fuelExpenses,
                        tollExpenses = data.tollExpenses,
                        maintenanceExpenses = data.maintenanceExpenses,
                        otherExpenses = data.otherExpenses,
                        pendingPayments = data.pendingPayments,
                        receivedPayments = data.receivedPayments,
                        // Detailed trip-cost expense breakdowns
                        driverAllowanceExpenses = data.driverAllowanceExpenses,
                        parkingExpenses = data.parkingExpenses,
                        loadingCharges = data.loadingCharges,
                        unloadingCharges = data.unloadingCharges,
                        chalanExpenses = data.chalanExpenses,
                        permitExpenses = data.permitExpenses,
                        insuranceExpenses = data.insuranceExpenses,
                        tripCostBreakdown = data.tripCostBreakdown.map { dto ->
                            CostBreakdownItem(
                                costId = dto.costId,
                                costLabel = dto.costLabel,
                                groupId = dto.groupId,
                                amount = dto.amount,
                                count = dto.count
                            )
                        },
                        maintenanceCostBreakdown = data.maintenanceCostBreakdown.map { dto ->
                            CostBreakdownItem(
                                costId = dto.costId,
                                costLabel = dto.costLabel,
                                groupId = dto.groupId,
                                amount = dto.amount,
                                count = dto.count
                            )
                        }
                    )
                )
            } else {
                Result.Error(ApiException(response.message ?: "Failed to fetch cost overview"))
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override suspend fun getPendingPayments(page: Int, perPage: Int): Result<PendingPaymentsData> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getPendingPayments(token, page, perPage)

            if (response.success && response.data != null) {
                val data = response.data
                Result.Success(
                    PendingPaymentsData(
                        payments = data.payments.map { dto ->
                            PendingPayment(
                                tripId = dto.tripId,
                                vehicleId = dto.vehicleId,
                                vehicleRegistration = dto.vehicleRegistration,
                                customerName = dto.customerName,
                                customerContact = dto.customerContact,
                                totalAmount = dto.totalAmount,
                                receivedAmount = dto.receivedAmount,
                                pendingAmount = dto.pendingAmount,
                                tripDate = formatDateToHumanReadable(dto.tripDate),
                                daysOverdue = dto.daysOverdue
                            )
                        },
                        totalPending = data.totalPending,
                        totalCount = data.totalCount
                    )
                )
            } else {
                Result.Error(ApiException(response.message ?: "Failed to fetch pending payments"))
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override suspend fun getAlertsStatus(): Result<AlertsSummary> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getAlertsStatus(token)

            if (response.success && response.data != null) {
                Result.Success(response.data.toDomain())
            } else {
                Result.Error(ApiException(response.message ?: "Failed to fetch alerts status"))
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    override suspend fun getFinancialSummary(period: FinancialPeriod): Result<FinancialSummary> {
        return try {
            val token = requireAuthToken()
            val response = remoteDataSource.getFinancialSummary(token, period)

            if (response.success && response.data != null) {
                val data = response.data
                Result.Success(
                    FinancialSummary(
                        period = data.period,
                        // Backend report.FinancialSummary has no period_label.
                        periodLabel = "",
                        totalRevenue = data.totalRevenue,
                        totalExpenses = data.totalExpenses,
                        tripCosts = data.tripCosts,
                        maintenanceCosts = data.maintenanceCosts,
                        driverCosts = data.driverCosts,
                        netProfit = data.netProfit,
                        profitMargin = data.profitMargin,
                        profitStatus = data.profitStatus,
                        pendingPayments = data.pendingPayments,
                        receivedPayments = data.receivedPayments,
                        completedTrips = data.completedTrips,
                        totalTrips = data.totalTrips,
                        avgTripRevenue = data.avgTripRevenue,
                        avgTripCost = data.avgTripCost,
                        avgTripProfit = data.avgTripProfit
                    )
                )
            } else {
                Result.Error(ApiException(response.message ?: "Failed to fetch financial summary"))
            }
        } catch (e: AuthException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(NetworkException(ApiErrorHandler.extractErrorMessage(e)))
        }
    }

    private fun DashboardDataDto.toDashboardData(isFromCache: Boolean, cachedAt: Long? = null) =
        DashboardData(
            userInfo = userInfo.toDomain(),
            stats = toDomain(),
            isFromCache = isFromCache,
            cachedAt = cachedAt
        )

    private suspend fun requireAuthToken(): String =
        AuthTokenHelper.requireAuthTokenOrRedirect { userLocalDataSource.getAuthToken() }
}

