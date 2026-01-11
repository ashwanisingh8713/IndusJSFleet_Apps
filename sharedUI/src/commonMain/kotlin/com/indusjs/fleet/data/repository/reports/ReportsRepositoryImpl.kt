package com.indusjs.fleet.data.repository.reports

import com.indusjs.error.exception.AuthException
import com.indusjs.error.result.Result
import com.indusjs.fleet.data.datasource.reports.ReportsRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.model.reports.*
import com.indusjs.fleet.domain.entity.reports.*
import com.indusjs.fleet.domain.repository.reports.ReportsRepository
import dev.zacsweers.metro.Inject

/**
 * Implementation of ReportsRepository.
 * Fetches P&L data from remote API and maps DTOs to domain entities.
 */
@Inject
class ReportsRepositoryImpl(
    private val remoteDataSource: ReportsRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource
) : ReportsRepository {

    /**
     * Retrieves auth token or throws AuthException.
     */
    private suspend fun requireAuthToken(): String {
        return userLocalDataSource.getAuthToken()
            ?: throw AuthException.unauthenticated()
    }

    override suspend fun getTripProfitLoss(tripId: Int): Result<TripProfitLoss> {
        return try {
            val token = requireAuthToken()
            val dto = remoteDataSource.getTripProfitLoss(token, tripId)
            if (dto != null) {
                Result.Success(dto.toDomain())
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
                Result.Success(dto.toDomain())
            } else {
                Result.Error(Exception("Vehicle P&L not found"), "Failed to fetch vehicle profit/loss")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to fetch vehicle profit/loss")
        }
    }

    override suspend fun getFleetProfitLoss(
        period: String?,
        startDate: String?,
        endDate: String?
    ): Result<FleetProfitLoss> {
        return try {
            val token = requireAuthToken()
            val dto = remoteDataSource.getFleetProfitLoss(token, period, startDate, endDate)
            if (dto != null) {
                Result.Success(dto.toDomain())
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
                Result.Success(dtos.map { it.toDomain() })
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
                Result.Success(dtos.map { it.toDomain() })
            } else {
                Result.Error(Exception("Multi-trip P&L not found"), "Failed to fetch multi-trip profit/loss")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to fetch multi-trip profit/loss")
        }
    }

    override suspend fun getCostTypeAnalysis(
        costType: String,
        startDate: String?,
        endDate: String?
    ): Result<CostTypeAnalysis> {
        return try {
            val token = requireAuthToken()
            val dto = remoteDataSource.getCostTypeAnalysis(token, costType, startDate, endDate)
            if (dto != null) {
                Result.Success(dto.toDomain())
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
                Result.Success(dtos.map { it.toDomain() })
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
                Result.Success(dto.toDomain())
            } else {
                Result.Error(Exception("Consolidated P&L not found"), "Failed to fetch consolidated report")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to fetch consolidated report")
        }
    }

    override suspend fun getPLSummary(
        startDate: String?,
        endDate: String?
    ): Result<PLSummary> {
        return try {
            val token = requireAuthToken()
            val dto = remoteDataSource.getPLSummary(token, startDate, endDate)
            if (dto != null) {
                Result.Success(dto.toDomain())
            } else {
                Result.Error(Exception("P&L summary not found"), "Failed to fetch P&L summary")
            }
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Failed to fetch P&L summary")
        }
    }
}

// Extension functions to map DTOs to domain entities

private fun TripProfitLossDto.toDomain() = TripProfitLoss(
    tripId = tripId,
    vehicleId = vehicleId,
    vehicleNumber = vehicleNumber,
    driverId = driverId,
    driverName = driverName,
    startLocation = startLocation,
    endLocation = endLocation,
    scheduledDate = scheduledDate,
    state = state,
    purchasePrice = purchasePrice,
    sellingValue = sellingValue,
    totalTripCosts = totalTripCosts,
    totalExpenses = totalExpenses,
    grossProfit = grossProfit,
    netProfit = netProfit,
    profitMargin = profitMargin,
    isProfitable = isProfitable,
    costBreakdown = costBreakdown?.map { it.toDomain() } ?: emptyList()
)

private fun VehicleProfitLossDto.toDomain() = VehicleProfitLoss(
    vehicleId = vehicleId,
    vehicleNumber = vehicleNumber ?: vehicleRegistration,
    make = make,
    model = model,
    period = period?.let { "${it.startDate ?: ""} to ${it.endDate ?: ""}" },
    startDate = startDate ?: period?.startDate,
    endDate = endDate ?: period?.endDate,
    totalTrips = totalTrips,
    completedTrips = completedTrips,
    totalRevenue = totalRevenue,
    totalTripCosts = totalTripCosts,
    totalMaintenanceCosts = totalMaintenanceCosts,
    totalExpenses = totalExpenses,
    grossProfit = grossProfit,
    netProfit = netProfit,
    profitMargin = profitMargin,
    isProfitable = isProfitable,
    costBreakdown = costBreakdown?.map { it.toDomain() } ?: emptyList(),
    tripSummary = tripSummary?.map { it.toDomain() } ?: emptyList()
)

private fun FleetProfitLossDto.toDomain() = FleetProfitLoss(
    period = period,
    startDate = startDate,
    endDate = endDate,
    totalVehicles = totalVehicles,
    totalTrips = totalTrips,
    completedTrips = completedTrips,
    totalRevenue = totalRevenue,
    totalExpenses = totalExpenses,
    totalTripCosts = totalTripCosts,
    totalMaintenanceCosts = totalMaintenanceCosts,
    grossProfit = grossProfit,
    netProfit = netProfit,
    profitMargin = profitMargin,
    isProfitable = isProfitable,
    vehicleBreakdown = vehicleBreakdown?.map { it.toDomain() } ?: emptyList(),
    costBreakdown = costBreakdown?.map { it.toDomain() } ?: emptyList()
)

private fun CostBreakdownItemDto.toDomain() = CostBreakdownItem(
    costType = costType,
    amount = amount,
    count = count,
    percentage = percentage
)

private fun TripSummaryItemDto.toDomain() = TripSummaryItem(
    tripId = tripId,
    scheduledDate = scheduledDate,
    startLocation = startLocation,
    endLocation = endLocation,
    revenue = revenue,
    expenses = expenses,
    profit = profit,
    isProfitable = isProfitable
)

private fun CostTypeAnalysisDto.toDomain() = CostTypeAnalysis(
    costType = costType,
    startDate = startDate,
    endDate = endDate,
    totalAmount = totalAmount,
    totalCount = totalCount,
    averagePerEntry = averagePerEntry,
    vehicleBreakdown = vehicleBreakdown?.map { it.toDomain() } ?: emptyList(),
    monthlyTrend = monthlyTrend?.map { it.toDomain() } ?: emptyList()
)

private fun VehicleCostBreakdownDto.toDomain() = VehicleCostBreakdown(
    vehicleId = vehicleId,
    vehicleNumber = vehicleNumber,
    amount = amount,
    count = count,
    percentage = percentage
)

private fun MonthlyTrendDto.toDomain() = MonthlyTrend(
    month = month,
    year = year,
    amount = amount,
    count = count
)

private fun ConsolidatedPLDto.toDomain() = ConsolidatedPL(
    startDate = startDate,
    endDate = endDate,
    groupBy = groupBy,
    totalRevenue = totalRevenue,
    totalExpenses = totalExpenses,
    netProfit = netProfit,
    profitMargin = profitMargin,
    isProfitable = isProfitable,
    totalVehicles = totalVehicles,
    totalTrips = totalTrips,
    completedTrips = completedTrips,
    vehicleSummary = vehicleSummary?.map { it.toDomain() } ?: emptyList(),
    tripSummary = tripSummary?.map { it.toDomain() } ?: emptyList(),
    costBreakdown = costBreakdown?.map { it.toDomain() } ?: emptyList(),
    periodBreakdown = periodBreakdown?.map { it.toDomain() } ?: emptyList()
)

private fun VehiclePLSummaryDto.toDomain() = VehiclePLSummary(
    vehicleId = vehicleId,
    vehicleNumber = vehicleNumber,
    revenue = revenue,
    expenses = expenses,
    profit = profit,
    profitMargin = profitMargin,
    isProfitable = isProfitable,
    tripCount = tripCount
)

private fun TripPLSummaryDto.toDomain() = TripPLSummary(
    tripId = tripId,
    vehicleNumber = vehicleNumber,
    route = route,
    scheduledDate = scheduledDate,
    revenue = revenue,
    expenses = expenses,
    profit = profit,
    isProfitable = isProfitable
)

private fun PeriodBreakdownDto.toDomain() = PeriodBreakdown(
    period = period,
    label = label,
    revenue = revenue,
    expenses = expenses,
    profit = profit,
    tripCount = tripCount,
    isProfitable = isProfitable
)

private fun PLSummaryDto.toDomain() = PLSummary(
    startDate = period?.startDate,
    endDate = period?.endDate,
    totalRevenue = overview?.totalRevenue ?: 0.0,
    totalExpenses = overview?.totalExpenses ?: 0.0,
    grossProfit = overview?.grossProfit ?: 0.0,
    profitMarginPercentage = overview?.profitMarginPercentage ?: 0.0,
    status = overview?.status ?: "neutral",
    isProfitable = overview?.status == "profit",
    totalVehicles = fleetSummary?.totalVehicles ?: 0,
    activeVehicles = fleetSummary?.activeVehicles ?: 0,
    profitableVehicles = fleetSummary?.profitableVehicles ?: 0,
    lossMakingVehicles = fleetSummary?.lossMakingVehicles ?: 0,
    totalTrips = tripSummary?.totalTrips ?: 0,
    completedTrips = tripSummary?.completedTrips ?: 0,
    profitableTrips = tripSummary?.profitableTrips ?: 0,
    lossMakingTrips = tripSummary?.lossMakingTrips ?: 0,
    expenseBreakdown = expenseBreakdown?.map { (type, item) ->
        ExpenseBreakdownItem(
            type = type,
            amount = item.amount,
            percentage = item.percentage
        )
    } ?: emptyList(),
    topPerformingVehicle = topPerformers?.mostProfitableVehicle?.toDomain(),
    lossMakingVehiclesList = alerts?.lossMakingVehicles?.map { it.toDomain() } ?: emptyList()
)

private fun PLVehiclePerformerDto.toDomain() = VehiclePerformer(
    id = id,
    registrationNumber = registrationNumber,
    profit = profit,
    loss = loss
)

private fun PLAlertDto.toDomain() = PLAlert(
    type = type,
    severity = severity,
    message = message,
    vehicleId = vehicleId,
    vehicleNumber = vehicleNumber
)

