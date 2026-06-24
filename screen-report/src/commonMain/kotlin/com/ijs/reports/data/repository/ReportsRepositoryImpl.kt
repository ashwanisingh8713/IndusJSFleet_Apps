package com.ijs.reports.data.repository

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.reports.TAG_REPORTS_REPO
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.auth.AuthTokenHelper
import com.ijs.reports.data.datasource.ReportsRemoteDataSource
import com.ijs.reports.data.mapper.ProfitLossMapper
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.ijs.reports.data.model.*
import com.ijs.reports.domain.entity.*
import com.ijs.reports.domain.repository.ReportsRepository
import dev.zacsweers.metro.Inject

/**
 * Implementation of ReportsRepository.
 * Fetches P&L data from remote API and maps DTOs to domain entities
 * using [ProfitLossMapper].
 */
@Inject
class ReportsRepositoryImpl(
    private val remoteDataSource: ReportsRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val logger: FleetLogger
) : ReportsRepository {

    /**
     * Retrieves auth token or emits session expired event and throws AuthException.
     */
    private suspend fun requireAuthToken(): String {
        return AuthTokenHelper.requireAuthTokenOrRedirect {
            userLocalDataSource.getAuthToken()
        }
    }

    override suspend fun getTripProfitLoss(tripId: Int): Result<TripProfitLoss> {
        return try {
            val token = requireAuthToken()
            val dto = remoteDataSource.getTripProfitLoss(token, tripId)
            if (dto != null) {
                Result.Success(ProfitLossMapper.toTripProfitLoss(dto))
            } else {
                Result.Error(Exception("Trip P&L not found"), "Failed to fetch trip profit/loss")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to fetch trip profit/loss")
        }
    }

    override suspend fun getVehicleProfitLoss(vehicleId: Int, period: String): Result<VehicleProfitLoss> {
        return try {
            val token = requireAuthToken()
            val dto = remoteDataSource.getVehicleProfitLoss(token, vehicleId, period)
            if (dto != null) {
                Result.Success(ProfitLossMapper.toVehicleProfitLoss(dto))
            } else {
                Result.Error(Exception("Vehicle P&L not found"), "Failed to fetch vehicle profit/loss")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to fetch vehicle profit/loss")
        }
    }

    override suspend fun getFleetProfitLoss(
        period: String?,
        startDate: Long?,
        endDate: Long?
    ): Result<FleetProfitLoss> {
        return try {
            val token = requireAuthToken()
            val dto = remoteDataSource.getFleetProfitLoss(token, period, startDate, endDate)
            if (dto != null) {
                Result.Success(ProfitLossMapper.toFleetProfitLoss(dto))
            } else {
                Result.Error(Exception("Fleet P&L not found"), "Failed to fetch fleet profit/loss")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to fetch fleet profit/loss")
        }
    }

    override suspend fun getMultiVehiclePL(request: MultiVehiclePLRequest): Result<List<VehicleProfitLoss>> {
        return try {
            val token = requireAuthToken()
            val dtos = remoteDataSource.getMultiVehicleProfitLoss(token, request)
            if (dtos != null) {
                Result.Success(dtos.map { ProfitLossMapper.toVehicleProfitLoss(it) })
            } else {
                Result.Error(Exception("Multi-vehicle P&L not found"), "Failed to fetch multi-vehicle profit/loss")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to fetch multi-vehicle profit/loss")
        }
    }

    override suspend fun getMultiTripPL(request: MultiTripPLRequest): Result<List<TripProfitLoss>> {
        return try {
            val token = requireAuthToken()
            val dtos = remoteDataSource.getMultiTripProfitLoss(token, request)
            if (dtos != null) {
                Result.Success(dtos.map { ProfitLossMapper.toTripProfitLoss(it) })
            } else {
                Result.Error(Exception("Multi-trip P&L not found"), "Failed to fetch multi-trip profit/loss")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to fetch multi-trip profit/loss")
        }
    }

    override suspend fun getCostTypeAnalysis(
        costType: String,
        startDate: Long?,
        endDate: Long?
    ): Result<CostTypeAnalysis> {
        return try {
            val token = requireAuthToken()
            val dto = remoteDataSource.getCostTypeAnalysis(token, costType, startDate, endDate)
            if (dto != null) {
                Result.Success(ProfitLossMapper.toCostTypeAnalysis(dto, startDate, endDate))
            } else {
                Result.Error(Exception("Cost type analysis not found"), "Failed to fetch cost type analysis")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to fetch cost type analysis")
        }
    }

    override suspend fun getMultiCostTypeAnalysis(request: MultiCostTypePLRequest): Result<List<CostTypeAnalysis>> {
        return try {
            val token = requireAuthToken()
            val dtos = remoteDataSource.getMultiCostTypeAnalysis(token, request)
            if (dtos != null) {
                Result.Success(dtos.map { ProfitLossMapper.toCostTypeAnalysis(it, request.startDate, request.endDate) })
            } else {
                Result.Error(Exception("Multi cost type analysis not found"), "Failed to fetch multi cost type analysis")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to fetch multi cost type analysis")
        }
    }

    override suspend fun getConsolidatedPL(request: ConsolidatedPLRequest): Result<ConsolidatedPL> {
        return try {
            val token = requireAuthToken()
            val dto = remoteDataSource.getConsolidatedPL(token, request)
            if (dto != null) {
                Result.Success(ProfitLossMapper.toConsolidatedPL(dto))
            } else {
                Result.Error(Exception("Consolidated P&L not found"), "Failed to fetch consolidated report")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to fetch consolidated report")
        }
    }

    override suspend fun getCustomerProfitLoss(period: String): Result<CustomerPLReport> {
        return try {
            val token = requireAuthToken()
            val dto = remoteDataSource.getCustomerProfitLoss(token, period)
            if (dto != null) {
                Result.Success(ProfitLossMapper.toCustomerPLReport(dto))
            } else {
                Result.Error(Exception("Customer P&L not found"), "Failed to fetch customer P&L report")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to fetch customer P&L report")
        }
    }

    override suspend fun getPLSummary(
        startDate: Long?,
        endDate: Long?
    ): Result<PLSummary> {
        logger.d(TAG_REPORTS_REPO, "=== getPLSummary Repository ===")
        logger.d(TAG_REPORTS_REPO, "Request: startDate=$startDate, endDate=$endDate")
        return try {
            val token = requireAuthToken()
            logger.d(TAG_REPORTS_REPO, "Auth token retrieved successfully")
            val dto = remoteDataSource.getPLSummary(token, startDate, endDate)
            if (dto != null) {
                logger.d(TAG_REPORTS_REPO, "DTO received, converting to domain")
                val domain = ProfitLossMapper.toPLSummary(dto)
                logger.d(TAG_REPORTS_REPO, "Domain conversion complete: revenue=${domain.totalRevenue}, expenses=${domain.totalExpenses}")
                Result.Success(domain)
            } else {
                logger.e(TAG_REPORTS_REPO, "DTO is null - API returned no data")
                Result.Error(Exception("P&L summary not found"), "Failed to fetch P&L summary")
            }
        } catch (e: Exception) {
            logger.e(TAG_REPORTS_REPO, "Exception in getPLSummary: ${e.message}")
            logger.e(TAG_REPORTS_REPO, "Exception type: ${e::class.simpleName}")
            Result.Error(e, e.message ?: "Failed to fetch P&L summary")
        }
    }
}

