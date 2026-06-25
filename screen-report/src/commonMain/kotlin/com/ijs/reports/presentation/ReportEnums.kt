package com.ijs.reports.presentation

import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_dashboard
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_menu
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_trending_down
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_trending_up
import org.jetbrains.compose.resources.DrawableResource

/**
 * Shared enums and data classes used across multiple report screens.
 * Extracted from individual contracts to avoid duplication and keep
 * Contract files focused on State/Intent/Effect only.
 */

/**
 * Report period options for filtering.
 * Matches the periods defined in report-pl-screen.prompt.md
 */
enum class ReportPeriod(val value: String, val label: String) {
    TODAY("today", "Today"),
    WEEKLY("weekly", "This Week"),
    FIFTEEN_DAYS("fifteen_days", "15 Days"),
    MONTHLY("monthly", "This Month"),
    QUARTERLY("quarterly", "Quarterly"),
    HALF_YEARLY("half_yearly", "Half Year"),
    YEARLY("yearly", "This Year"),
    CUSTOM("custom", "Custom")
}

/**
 * Profit status thresholds as defined in report-pl-screen.prompt.md
 *
 * | Status | Profit Margin | Color Code |
 * |--------|---------------|------------|
 * | Highly Profitable | > 20% | Dark Green |
 * | Profitable | 10% - 20% | Green |
 * | Break-even | 0% - 10% | Amber |
 * | Loss | 0% to -20% | Red |
 * | Severe Loss | < -20% | Dark Red |
 */
enum class ProfitStatus(
    val label: String,
    val colorHex: Long,
    val icon: DrawableResource
) {
    HIGHLY_PROFITABLE("Highly Profitable", 0xFF2E7D32, Res.drawable.ic_trending_up),
    PROFITABLE("Profitable", 0xFF4CAF50, Res.drawable.ic_trending_up),
    BREAK_EVEN("Break-even", 0xFFFFC107, Res.drawable.ic_dashboard),
    LOSS("Loss", 0xFFF44336, Res.drawable.ic_trending_down),
    SEVERE_LOSS("Severe Loss", 0xFFB71C1C, Res.drawable.ic_trending_down);

    companion object {
        fun fromMargin(margin: Double): ProfitStatus = when {
            margin > 20 -> HIGHLY_PROFITABLE
            margin > 10 -> PROFITABLE
            margin > 0 -> BREAK_EVEN
            margin > -20 -> LOSS
            else -> SEVERE_LOSS
        }
    }
}

/**
 * View mode for displaying P&L results (used in Vehicle P&L).
 */
enum class ReportViewMode(val label: String, val icon: DrawableResource) {
    SUMMARY("Summary", Res.drawable.ic_dashboard),
    LIST("List", Res.drawable.ic_menu),
    CHART("Chart", Res.drawable.ic_trending_up)
}

/**
 * Chart type for P&L visualization.
 */
enum class ReportChartType(val label: String) {
    BAR("Bar Chart"),
    PIE("Pie Chart")
}

/**
 * Export format options for report generation.
 */
enum class ReportExportFormat(val label: String, val icon: String, val extension: String) {
    PDF("PDF Report", "📄", "pdf"),
    CSV("CSV Data", "📊", "csv"),
    EXCEL("Excel Sheet", "📗", "xlsx")
}

/**
 * Sorting options for vehicle P&L results.
 */
enum class VehiclePLSortOption(val label: String) {
    PROFIT_HIGH_LOW("Profit (High to Low)"),
    PROFIT_LOW_HIGH("Profit (Low to High)"),
    LOSS_HIGH_LOW("Loss (High to Low)"),
    REVENUE_HIGH_LOW("Revenue (High to Low)"),
    EXPENSE_HIGH_LOW("Expense (High to Low)"),
    TRIPS_HIGH_LOW("Trips (Most to Least)")
}

/**
 * Sorting options for trip P&L results.
 */
enum class TripPLSortOption(val label: String) {
    PROFIT_HIGH_LOW("Profit (High to Low)"),
    PROFIT_LOW_HIGH("Profit (Low to High)"),
    LOSS_HIGH_LOW("Loss (High to Low)"),
    DATE_NEWEST("Date (Newest First)"),
    DATE_OLDEST("Date (Oldest First)"),
    REVENUE_HIGH_LOW("Revenue (High to Low)")
}

/**
 * Filter options for profit/loss status (used in both Vehicle P&L and Trip P&L).
 */
enum class PLStatusFilter(val label: String) {
    ALL("All"),
    PROFITABLE("Profitable Only"),
    LOSS_MAKING("Loss Making Only")
}

/**
 * Recent report entry for quick one-tap access (Vehicle P&L).
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

