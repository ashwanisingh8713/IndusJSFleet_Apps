package com.ijs.reports.presentation.trip

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.ijs.reports.domain.entity.TripProfitLoss
import com.ijs.trip.domain.entity.Trip

/**
 * MVI Contract for Trip P&L Screen
 * Trip-centric design: Select date range, then select trips to analyze
 */
object TripPLContract {

    /**
     * Sorting options for trip P&L results
     */
    enum class SortOption(val label: String) {
        PROFIT_HIGH_LOW("Profit (High to Low)"),
        PROFIT_LOW_HIGH("Profit (Low to High)"),
        LOSS_HIGH_LOW("Loss (High to Low)"),
        DATE_NEWEST("Date (Newest First)"),
        DATE_OLDEST("Date (Oldest First)"),
        REVENUE_HIGH_LOW("Revenue (High to Low)")
    }

    /**
     * Filter options for profit/loss status
     */
    enum class PLStatusFilter(val label: String) {
        ALL("All"),
        PROFITABLE("Profitable"),
        LOSS_MAKING("Loss Making")
    }

    data class State(
        val isLoading: Boolean = false,
        val isLoadingTrips: Boolean = false,
        val error: String? = null,
        // Date range for filtering trips
        val startDate: String = "",
        val endDate: String = "",
        // Trip listing and selection
        val trips: List<Trip> = emptyList(),
        val selectedTripIds: Set<String> = emptySet(),
        val tripSearchQuery: String = "",
        // Results
        val results: List<TripProfitLoss> = emptyList(),
        // UI state
        val tripsLoaded: Boolean = false,
        // Sorting and filtering
        val sortOption: SortOption = SortOption.PROFIT_HIGH_LOW,
        val plStatusFilter: PLStatusFilter = PLStatusFilter.ALL
    ) : UiState {
        val hasResults: Boolean get() = results.isNotEmpty()

        // Filtered trips based on search query
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

        // Sorted and filtered results
        val sortedFilteredResults: List<TripProfitLoss> get() {
            val filtered = when (plStatusFilter) {
                PLStatusFilter.ALL -> results
                PLStatusFilter.PROFITABLE -> results.filter { it.isProfitable }
                PLStatusFilter.LOSS_MAKING -> results.filter { !it.isProfitable }
            }
            return when (sortOption) {
                SortOption.PROFIT_HIGH_LOW -> filtered.sortedByDescending { it.netProfit }
                SortOption.PROFIT_LOW_HIGH -> filtered.sortedBy { it.netProfit }
                SortOption.LOSS_HIGH_LOW -> filtered.sortedBy { it.netProfit }
                SortOption.DATE_NEWEST -> filtered.sortedByDescending { it.scheduledDate ?: "" }
                SortOption.DATE_OLDEST -> filtered.sortedBy { it.scheduledDate ?: "" }
                SortOption.REVENUE_HIGH_LOW -> filtered.sortedByDescending { it.sellingValue }
            }
        }

        // Summary stats
        val totalProfitableTrips: Int get() = results.count { it.isProfitable }
        val totalLossMakingTrips: Int get() = results.count { !it.isProfitable }
        val totalRevenue: Double get() = results.sumOf { it.sellingValue }
        val totalExpenses: Double get() = results.sumOf { it.totalExpenses }
        val totalNetProfit: Double get() = results.sumOf { it.netProfit }
        val averageMargin: Double get() = if (results.isNotEmpty())
            results.map { it.profitMargin }.average() else 0.0

        // Computed properties
        val canLoadTrips: Boolean get() = startDate.isNotBlank() && endDate.isNotBlank()
        val canGenerateReport: Boolean get() = selectedTripIds.isNotEmpty()
        val selectedCount: Int get() = selectedTripIds.size
    }

    sealed interface Intent : UiIntent {
        // Date range
        data class UpdateStartDate(val date: String) : Intent
        data class UpdateEndDate(val date: String) : Intent
        // Load trips based on date range
        data object LoadTrips : Intent
        // Trip selection
        data class ToggleTrip(val tripId: String) : Intent
        data object SelectAllTrips : Intent
        data object ClearSelection : Intent
        // Search
        data class UpdateTripSearch(val query: String) : Intent
        // Generate report
        data object GenerateReport : Intent
        data object Refresh : Intent
        // Sorting and filtering
        data class UpdateSortOption(val option: SortOption) : Intent
        data class UpdatePLStatusFilter(val filter: PLStatusFilter) : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
    }
}

