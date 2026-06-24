package com.ijs.reports.presentation.vehicle

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText
import com.ijs.reports.domain.entity.VehicleProfitLoss
import com.ijs.reports.presentation.PLStatusFilter
import com.ijs.reports.presentation.RecentReport
import com.ijs.reports.presentation.ReportChartType
import com.ijs.reports.presentation.ReportExportFormat
import com.ijs.reports.presentation.ReportViewMode
import com.ijs.reports.presentation.VehiclePLSortOption
import com.ijs.vehicle.domain.entity.Vehicle

/**
 * MVI Contract for Vehicle P&L Screen - Enhanced with Fleet Overview mode.
 * Enums are defined in ReportEnums.kt for cross-screen reuse.
 */
object VehiclePLContract {

    data class State(
        val isLoading: Boolean = false,
        val isLoadingVehicles: Boolean = false,
        val error: UiText? = null,
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
        // Date range picker dialog visibility
        val showDateRangePicker: Boolean = false,
        val pickerStartDate: String = "",
        val pickerEndDate: String = "",
        // Sorting and filtering
        val sortOption: VehiclePLSortOption = VehiclePLSortOption.PROFIT_HIGH_LOW,
        val plStatusFilter: PLStatusFilter = PLStatusFilter.ALL,
        val showSortMenu: Boolean = false,
        // Fleet overview mode - shows all vehicles by default
        val isFleetOverviewMode: Boolean = true,
        // Filter bottom sheet for multi-vehicle selection
        val showVehicleFilterSheet: Boolean = false,
        // Temporary selection in filter sheet before applying
        val tempSelectedVehicleIds: Set<String> = emptySet(),
        // Export/Report generation state
        val isGeneratingExport: Boolean = false,
        val showExportOptions: Boolean = false,
        // View mode - summary/list/chart
        val viewMode: ReportViewMode = ReportViewMode.SUMMARY,
        // Chart type for visualization
        val chartType: ReportChartType = ReportChartType.BAR,
        // Current period label for display
        val currentPeriodLabel: String = "",
        // Initial load completed
        val initialLoadComplete: Boolean = false
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
                VehiclePLSortOption.PROFIT_HIGH_LOW -> filtered.sortedByDescending { it.netProfit }
                VehiclePLSortOption.PROFIT_LOW_HIGH -> filtered.sortedBy { it.netProfit }
                VehiclePLSortOption.LOSS_HIGH_LOW -> filtered.sortedBy { it.netProfit }
                VehiclePLSortOption.REVENUE_HIGH_LOW -> filtered.sortedByDescending { it.totalRevenue }
                VehiclePLSortOption.EXPENSE_HIGH_LOW -> filtered.sortedByDescending { it.totalExpenses }
                VehiclePLSortOption.TRIPS_HIGH_LOW -> filtered.sortedByDescending { it.totalTrips }
            }
        }

        // Summary stats for multi-results
        val totalProfitableCount: Int get() = multiResults.count { it.isProfitable }
        val totalLossMakingCount: Int get() = multiResults.count { !it.isProfitable }
        val totalRevenue: Double get() = multiResults.sumOf { it.totalRevenue }
        val totalExpenses: Double get() = multiResults.sumOf { it.totalExpenses }
        val totalNetProfit: Double get() = multiResults.sumOf { it.netProfit }

        // Fleet overview stats
        val fleetProfitMargin: Double get() = if (totalRevenue > 0) (totalNetProfit / totalRevenue) * 100 else 0.0
        val averageProfitPerVehicle: Double get() = if (multiResults.isNotEmpty()) totalNetProfit / multiResults.size else 0.0
        val topPerformer: VehicleProfitLoss? get() = multiResults.maxByOrNull { it.netProfit }
        val worstPerformer: VehicleProfitLoss? get() = multiResults.minByOrNull { it.netProfit }

        // Can generate report check
        val canGenerateReport: Boolean get() = (selectedVehicleId != null || isFleetOverviewMode) && !isLoading

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

        // Active filter count for badge
        val activeFilterCount: Int get() {
            var count = 0
            if (tempSelectedVehicleIds.isNotEmpty() && tempSelectedVehicleIds.size != vehicles.size) count++
            if (plStatusFilter != PLStatusFilter.ALL) count++
            if (useCustomDateRange) count++
            return count
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
        // Initialize screen in fleet overview mode (call on screen entry)
        data object InitFleetOverview : Intent
        data object LoadVehicles : Intent
        data object LoadFleetOverview : Intent
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
        data class UpdateSortOption(val option: VehiclePLSortOption) : Intent
        data class UpdatePLStatusFilter(val filter: PLStatusFilter) : Intent
        data object ToggleSortMenu : Intent
        data object DismissSortMenu : Intent
        // Vehicle filter sheet for fleet overview
        data object ShowVehicleFilterSheet : Intent
        data object DismissVehicleFilterSheet : Intent
        data class ToggleVehicleInFilter(val vehicleId: String) : Intent
        data object SelectAllVehiclesInFilter : Intent
        data object ClearVehicleFilter : Intent
        data object ApplyVehicleFilter : Intent
        // View mode
        data class UpdateViewMode(val mode: ReportViewMode) : Intent
        data class UpdateChartType(val type: ReportChartType) : Intent
        // Date range picker dialog
        data object ShowDateRangePicker : Intent
        data object HideDateRangePicker : Intent
        data class UpdatePickerStartDate(val date: String) : Intent
        data class UpdatePickerEndDate(val date: String) : Intent
        data class ApplyCustomDateRange(val startDate: String, val endDate: String) : Intent
        // Export
        data object ShowExportOptions : Intent
        data object DismissExportOptions : Intent
        data class ExportReport(val format: ReportExportFormat) : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: UiText) : Effect
        data class ExportGenerated(val fileName: String, val format: ReportExportFormat) : Effect
    }
}
