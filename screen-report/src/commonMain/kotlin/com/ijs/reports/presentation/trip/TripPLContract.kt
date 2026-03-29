package com.ijs.reports.presentation.trip

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.ijs.reports.domain.entity.TripProfitLoss
import com.ijs.reports.presentation.PLStatusFilter
import com.ijs.reports.presentation.TripPLSortOption
import com.ijs.trip.domain.entity.Trip

/**
 * MVI Contract for Trip P&L Screen
 * Trip-centric design: Select date range, then select trips to analyze.
 * Enums defined in ReportEnums.kt for cross-screen reuse.
 */
object TripPLContract {

    data class State(
        val isLoading: Boolean = false,
        val isLoadingTrips: Boolean = false,
        val error: String? = null,
        val startDate: String = "",
        val endDate: String = "",
        val trips: List<Trip> = emptyList(),
        val selectedTripIds: Set<String> = emptySet(),
        val tripSearchQuery: String = "",
        val results: List<TripProfitLoss> = emptyList(),
        val tripsLoaded: Boolean = false,
        val sortOption: TripPLSortOption = TripPLSortOption.PROFIT_HIGH_LOW,
        val plStatusFilter: PLStatusFilter = PLStatusFilter.ALL
    ) : UiState {
        val hasResults: Boolean get() = results.isNotEmpty()

        val filteredTrips: List<Trip> get() {
            if (tripSearchQuery.isBlank()) return trips
            val query = tripSearchQuery.lowercase()
            return trips.filter { trip ->
                trip.id.lowercase().contains(query) ||
                (trip.startLocation?.address?.lowercase()?.contains(query) == true) ||
                (trip.endLocation?.address?.lowercase()?.contains(query) == true) ||
                (trip.vehicleNumber?.lowercase()?.contains(query) == true) ||
                (trip.customerName?.lowercase()?.contains(query) == true)
            }
        }

        val sortedFilteredResults: List<TripProfitLoss> get() {
            val filtered = when (plStatusFilter) {
                PLStatusFilter.ALL -> results
                PLStatusFilter.PROFITABLE -> results.filter { it.isProfitable }
                PLStatusFilter.LOSS_MAKING -> results.filter { !it.isProfitable }
            }
            return when (sortOption) {
                TripPLSortOption.PROFIT_HIGH_LOW -> filtered.sortedByDescending { it.netProfit }
                TripPLSortOption.PROFIT_LOW_HIGH -> filtered.sortedBy { it.netProfit }
                TripPLSortOption.LOSS_HIGH_LOW -> filtered.sortedBy { it.netProfit }
                TripPLSortOption.DATE_NEWEST -> filtered.sortedByDescending { it.scheduledDate ?: "" }
                TripPLSortOption.DATE_OLDEST -> filtered.sortedBy { it.scheduledDate ?: "" }
                TripPLSortOption.REVENUE_HIGH_LOW -> filtered.sortedByDescending { it.sellingValue }
            }
        }

        val totalProfitableTrips: Int get() = results.count { it.isProfitable }
        val totalLossMakingTrips: Int get() = results.count { !it.isProfitable }
        val totalRevenue: Double get() = results.sumOf { it.sellingValue }
        val totalExpenses: Double get() = results.sumOf { it.totalExpenses }
        val totalNetProfit: Double get() = results.sumOf { it.netProfit }
        val averageMargin: Double get() = if (results.isNotEmpty())
            results.map { it.profitMargin }.average() else 0.0

        val canLoadTrips: Boolean get() = startDate.isNotBlank() && endDate.isNotBlank()
        val canGenerateReport: Boolean get() = selectedTripIds.isNotEmpty()
        val selectedCount: Int get() = selectedTripIds.size
    }

    sealed interface Intent : UiIntent {
        data class UpdateStartDate(val date: String) : Intent
        data class UpdateEndDate(val date: String) : Intent
        data object LoadTrips : Intent
        data class ToggleTrip(val tripId: String) : Intent
        data object SelectAllTrips : Intent
        data object ClearSelection : Intent
        data class UpdateTripSearch(val query: String) : Intent
        data object GenerateReport : Intent
        data object Refresh : Intent
        data class UpdateSortOption(val option: TripPLSortOption) : Intent
        data class UpdatePLStatusFilter(val filter: PLStatusFilter) : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
    }
}
