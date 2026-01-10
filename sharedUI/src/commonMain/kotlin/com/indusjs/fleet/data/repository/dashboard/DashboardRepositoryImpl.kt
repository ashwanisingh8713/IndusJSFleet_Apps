package com.indusjs.fleet.data.repository.dashboard

import com.indusjs.error.exception.ApiException
import com.indusjs.error.exception.NetworkException
import com.indusjs.error.exception.AuthException
import com.indusjs.fleet.core.network.ApiErrorHandler
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
                        filter = data.filter,
                        periodLabel = data.periodLabel,
                        totalExpenses = data.totalExpenses,
                        totalRevenue = data.totalRevenue,
                        profitLoss = data.profitLoss,
                        isProfit = data.isProfit,
                        completedTrips = data.completedTrips,
                        tripCosts = data.tripCosts,
                        maintenanceCosts = data.maintenanceCosts,
                        fuelCosts = data.fuelCosts,
                        tollCosts = data.tollCosts,
                        otherCosts = data.otherCosts,
                        // NEW: Detailed cost breakdowns
                        driverAllowanceExpenses = data.driverAllowanceExpenses,
                        parkingExpenses = data.parkingExpenses,
                        loadingCharges = data.loadingCharges,
                        unloadingCharges = data.unloadingCharges,
                        chalanExpenses = data.chalanExpenses,
                        permitExpenses = data.permitExpenses,
                        insuranceExpenses = data.insuranceExpenses,
                        tripCostBreakdown = data.tripCostBreakdown.map { dto ->
                            CostBreakdownItem(
                                costType = dto.costType,
                                amount = dto.amount,
                                count = dto.count
                            )
                        },
                        maintenanceCostBreakdown = data.maintenanceCostBreakdown.map { dto ->
                            CostBreakdownItem(
                                costType = dto.costType,
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
                                vehicleRegistration = dto.vehicleRegistration,
                                customerName = dto.customerName,
                                customerContact = dto.customerContact,
                                sellingValue = dto.sellingValue,
                                pendingAmount = dto.pendingAmount,
                                paymentStatus = dto.paymentStatus,
                                tripDate = dto.tripDate,
                                startLocation = dto.startLocation,
                                endLocation = dto.endLocation,
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
                        periodLabel = data.periodLabel,
                        totalRevenue = data.totalRevenue,
                        totalExpenses = data.totalExpenses,
                        netProfit = data.netProfit,
                        profitMargin = data.profitMargin,
                        profitStatus = data.profitStatus,
                        pendingPayments = data.pendingPayments,
                        completedTrips = data.completedTrips,
                        avgTripRevenue = data.avgTripRevenue,
                        avgTripProfit = data.avgTripProfit,
                        fuelCost = data.fuelCost,
                        tollCost = data.tollCost,
                        maintenanceCost = data.maintenanceCost,
                        otherCost = data.otherCost
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
        userLocalDataSource.getAuthToken() ?: throw AuthException.unauthenticated()
}

