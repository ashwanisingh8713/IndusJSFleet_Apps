package com.ijs.reports.domain.usecase

import com.indusjs.error.result.Result
import com.ijs.reports.data.model.ConsolidatedPLRequest
import com.ijs.reports.data.model.MultiCostTypePLRequest
import com.ijs.reports.data.model.MultiTripPLRequest
import com.ijs.reports.data.model.MultiVehiclePLRequest
import com.ijs.reports.domain.entity.*
import com.ijs.reports.domain.repository.ReportsRepository
import dev.zacsweers.metro.Inject

/**
 * Use case for fetching P&L summary (Reports Hub).
 */
@Inject
class GetPLSummaryUseCase(
    private val reportsRepository: ReportsRepository
) {
    suspend operator fun invoke(
        startDate: Long? = null,
        endDate: Long? = null
    ): Result<PLSummary> {
        return reportsRepository.getPLSummary(startDate, endDate)
    }
}

/**
 * Use case for fetching single trip P&L.
 */
@Inject
class GetTripProfitLossUseCase(
    private val reportsRepository: ReportsRepository
) {
    suspend operator fun invoke(tripId: Int): Result<TripProfitLoss> {
        return reportsRepository.getTripProfitLoss(tripId)
    }
}

/**
 * Use case for fetching multi-trip P&L.
 */
@Inject
class GetMultiTripPLUseCase(
    private val reportsRepository: ReportsRepository
) {
    suspend operator fun invoke(request: MultiTripPLRequest): Result<List<TripProfitLoss>> {
        return reportsRepository.getMultiTripPL(request)
    }
}

/**
 * Use case for fetching single vehicle P&L.
 */
@Inject
class GetVehicleProfitLossUseCase(
    private val reportsRepository: ReportsRepository
) {
    suspend operator fun invoke(
        vehicleId: Int,
        period: String = "monthly"
    ): Result<VehicleProfitLoss> {
        return reportsRepository.getVehicleProfitLoss(vehicleId, period)
    }
}

/**
 * Use case for fetching multi-vehicle P&L.
 */
@Inject
class GetMultiVehiclePLUseCase(
    private val reportsRepository: ReportsRepository
) {
    suspend operator fun invoke(request: MultiVehiclePLRequest): Result<List<VehicleProfitLoss>> {
        return reportsRepository.getMultiVehiclePL(request)
    }
}

/**
 * Use case for fetching single cost type analysis.
 */
@Inject
class GetCostTypeAnalysisUseCase(
    private val reportsRepository: ReportsRepository
) {
    suspend operator fun invoke(
        costType: String,
        startDate: Long? = null,
        endDate: Long? = null
    ): Result<CostTypeAnalysis> {
        return reportsRepository.getCostTypeAnalysis(costType, startDate, endDate)
    }
}

/**
 * Use case for fetching multi cost type analysis.
 */
@Inject
class GetMultiCostTypeAnalysisUseCase(
    private val reportsRepository: ReportsRepository
) {
    suspend operator fun invoke(request: MultiCostTypePLRequest): Result<List<CostTypeAnalysis>> {
        return reportsRepository.getMultiCostTypeAnalysis(request)
    }
}

/**
 * Use case for fetching fleet-wide P&L.
 */
@Inject
class GetFleetProfitLossUseCase(
    private val reportsRepository: ReportsRepository
) {
    suspend operator fun invoke(
        period: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Result<FleetProfitLoss> {
        return reportsRepository.getFleetProfitLoss(period, startDate, endDate)
    }
}

/**
 * Use case for fetching consolidated P&L report.
 */
@Inject
class GetConsolidatedPLUseCase(
    private val reportsRepository: ReportsRepository
) {
    suspend operator fun invoke(request: ConsolidatedPLRequest): Result<ConsolidatedPL> {
        return reportsRepository.getConsolidatedPL(request)
    }
}

class GetCustomerPLUseCase(
    private val reportsRepository: ReportsRepository
) {
    suspend operator fun invoke(period: String): Result<CustomerPLReport> {
        return reportsRepository.getCustomerProfitLoss(period)
    }
}

