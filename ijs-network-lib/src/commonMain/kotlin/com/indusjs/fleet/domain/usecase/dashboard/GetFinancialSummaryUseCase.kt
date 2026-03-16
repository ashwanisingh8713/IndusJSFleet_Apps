package com.indusjs.fleet.domain.usecase.dashboard

import com.indusjs.error.result.Result
import com.indusjs.fleet.data.model.dashboard.FinancialPeriod
import com.indusjs.fleet.domain.entity.dashboard.FinancialSummary
import com.indusjs.fleet.domain.repository.dashboard.DashboardRepository
import dev.zacsweers.metro.Inject

/**
 * Use case for fetching financial summary with KPIs.
 * Available to Owner and General Manager only.
 */
@Inject
class GetFinancialSummaryUseCase(
    private val repository: DashboardRepository
) {
    /**
     * Get financial summary for the specified period.
     * @param period The time period for the summary (today, weekly, monthly, yearly)
     * @return Result containing FinancialSummary or error
     */
    suspend operator fun invoke(period: FinancialPeriod = FinancialPeriod.MONTHLY): Result<FinancialSummary> {
        return repository.getFinancialSummary(period)
    }
}

