package com.indusjs.fleet.presentation.reports.vehicle

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.reports.VehicleProfitLoss
import com.indusjs.fleet.domain.entity.vehicle.Vehicle

/**
 * MVI Contract for Vehicle P&L Screen - Enhanced wizard-like flow
 */
object VehiclePLContract {

    /**
     * Sorting options for vehicle P&L results
     */
    enum class SortOption(val label: String) {
        PROFIT_HIGH_LOW("Profit (High to Low)"),
        PROFIT_LOW_HIGH("Profit (Low to High)"),
        LOSS_HIGH_LOW("Loss (High to Low)"),
        REVENUE_HIGH_LOW("Revenue (High to Low)"),
        EXPENSE_HIGH_LOW("Expense (High to Low)"),
        TRIPS_HIGH_LOW("Trips (Most to Least)")
    }

    /**
     * Filter options for profit/loss status
     */
    enum class PLStatusFilter(val label: String) {
        ALL("All"),
        PROFITABLE("Profitable Only"),
        LOSS_MAKING("Loss Making Only")
    }

    /**
     * Recent report entry for quick one-tap access
     */
    data class RecentReport(
        val vehicleId: String,
        val vehicleNumber: String,
        val vehicleMakeModel: String,
        val period: String,
        val profitLoss: Double,
        val isProfit: Boolean,
        val generatedAt: Long
    )

    data class State(
        val isLoading: Boolean = false,
        val isLoadingVehicles: Boolean = false,
        val error: String? = null,
        val vehicles: List<Vehicle> = emptyList(),
        val selectedVehicleId: String? = null,
        val period: String = "monthly",
        val startDate: String = "",
        val endDate: String = "",
        val result: VehicleProfitLoss? = null,
        val multiResults: List<VehicleProfitLoss> = emptyList(),
        val selectedVehicleIds: Set<String> = emptySet(),
        val isMultiMode: Boolean = false,
        // Search functionality
        val vehicleSearchQuery: String = "",
        // Bottom sheet for vehicle selection
        val showVehicleSelector: Boolean = false,
        // Recent vehicles for quick access (persisted)
        val recentVehicleIds: List<String> = emptyList(),
        // Recent reports history for one-tap regeneration
        val recentReports: List<RecentReport> = emptyList(),
        // Custom date range mode
        val useCustomDateRange: Boolean = false,
        // Sorting and filtering
        val sortOption: SortOption = SortOption.PROFIT_HIGH_LOW,
        val plStatusFilter: PLStatusFilter = PLStatusFilter.ALL,
        val showSortMenu: Boolean = false
    ) : UiState {
        val hasResult: Boolean get() = result != null || multiResults.isNotEmpty()
        val selectedVehicle: Vehicle? get() = vehicles.find { it.id == selectedVehicleId }

        // Recent vehicles from stored IDs
        val recentVehicles: List<Vehicle> get() = recentVehicleIds
            .mapNotNull { id -> vehicles.find { it.id == id } }
            .take(5)

        // Filtered vehicles based on search query
        val filteredVehicles: List<Vehicle> get() {
            if (vehicleSearchQuery.isBlank()) return vehicles
            val query = vehicleSearchQuery.lowercase()
            return vehicles.filter {
                it.registrationNumber.lowercase().contains(query) ||
                (it.make?.lowercase()?.contains(query) == true) ||
                (it.model?.lowercase()?.contains(query) == true) ||
                (it.assignedDriverName?.lowercase()?.contains(query) == true)
            }
        }

        // Sorted and filtered multi-results
        val sortedFilteredResults: List<VehicleProfitLoss> get() {
            val filtered = when (plStatusFilter) {
                PLStatusFilter.ALL -> multiResults
                PLStatusFilter.PROFITABLE -> multiResults.filter { it.isProfitable }
                PLStatusFilter.LOSS_MAKING -> multiResults.filter { !it.isProfitable }
            }
            return when (sortOption) {
                SortOption.PROFIT_HIGH_LOW -> filtered.sortedByDescending { it.netProfit }
                SortOption.PROFIT_LOW_HIGH -> filtered.sortedBy { it.netProfit }
                SortOption.LOSS_HIGH_LOW -> filtered.sortedBy { it.netProfit } // Lowest (most negative) first
                SortOption.REVENUE_HIGH_LOW -> filtered.sortedByDescending { it.totalRevenue }
                SortOption.EXPENSE_HIGH_LOW -> filtered.sortedByDescending { it.totalExpenses }
                SortOption.TRIPS_HIGH_LOW -> filtered.sortedByDescending { it.totalTrips }
            }
        }

        // Summary stats for multi-results
        val totalProfitableCount: Int get() = multiResults.count { it.isProfitable }
        val totalLossMakingCount: Int get() = multiResults.count { !it.isProfitable }
        val totalRevenue: Double get() = multiResults.sumOf { it.totalRevenue }
        val totalExpenses: Double get() = multiResults.sumOf { it.totalExpenses }
        val totalNetProfit: Double get() = multiResults.sumOf { it.netProfit }

        // Can generate report check
        val canGenerateReport: Boolean get() = selectedVehicleId != null && !isLoading

        // Period display text for UI
        val periodDisplayText: String get() = when (period) {
            "today" -> "Today"
            "weekly" -> "This Week"
            "monthly" -> "This Month"
            "yearly" -> "This Year"
            "custom" -> if (startDate.isNotBlank() && endDate.isNotBlank())
                "$startDate - $endDate" else "Custom Range"
            else -> "This Month"
        }
    }

    val PERIOD_OPTIONS = listOf(
        "today" to "Today",
        "weekly" to "Week",
        "monthly" to "Month",
        "yearly" to "Year",
        "custom" to "Custom"
    )

    sealed interface Intent : UiIntent {
        data object LoadVehicles : Intent
        data class SelectVehicle(val vehicleId: String) : Intent
        data class ToggleVehicle(val vehicleId: String) : Intent
        data object SelectAllVehicles : Intent
        data object ClearVehicles : Intent
        data class UpdatePeriod(val period: String) : Intent
        data class UpdateStartDate(val date: String) : Intent
        data class UpdateEndDate(val date: String) : Intent
        data object ToggleMultiMode : Intent
        data object GenerateReport : Intent
        data object Refresh : Intent
        // Vehicle selector bottom sheet
        data object ShowVehicleSelector : Intent
        data object DismissVehicleSelector : Intent
        // Search
        data class UpdateVehicleSearch(val query: String) : Intent
        data object ClearVehicleSearch : Intent
        // Quick actions
        data class QuickReportFromRecent(val report: RecentReport) : Intent
        // Sorting and filtering
        data class UpdateSortOption(val option: SortOption) : Intent
        data class UpdatePLStatusFilter(val filter: PLStatusFilter) : Intent
        data object ToggleSortMenu : Intent
        data object DismissSortMenu : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
    }
}
