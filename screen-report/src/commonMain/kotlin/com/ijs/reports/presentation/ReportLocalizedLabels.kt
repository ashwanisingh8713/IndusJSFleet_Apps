package com.ijs.reports.presentation

import androidx.compose.runtime.Composable
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReportPeriod.localizedLabel(): String = when (this) {
    ReportPeriod.TODAY -> stringResource(Res.string.report_period_today)
    ReportPeriod.WEEKLY -> stringResource(Res.string.report_period_this_week)
    ReportPeriod.FIFTEEN_DAYS -> stringResource(Res.string.report_period_15_days)
    ReportPeriod.MONTHLY -> stringResource(Res.string.report_period_this_month)
    ReportPeriod.QUARTERLY -> stringResource(Res.string.report_period_quarterly)
    ReportPeriod.HALF_YEARLY -> stringResource(Res.string.report_period_half_year)
    ReportPeriod.YEARLY -> stringResource(Res.string.report_period_this_year)
    ReportPeriod.CUSTOM -> stringResource(Res.string.report_period_custom)
}

@Composable
fun ProfitStatus.localizedLabel(): String = when (this) {
    ProfitStatus.HIGHLY_PROFITABLE -> stringResource(Res.string.profit_status_highly_profitable)
    ProfitStatus.PROFITABLE -> stringResource(Res.string.profit_status_profitable)
    ProfitStatus.BREAK_EVEN -> stringResource(Res.string.profit_status_break_even)
    ProfitStatus.LOSS -> stringResource(Res.string.profit_status_loss)
    ProfitStatus.SEVERE_LOSS -> stringResource(Res.string.profit_status_severe_loss)
}

@Composable
fun PLStatusFilter.localizedLabel(): String = when (this) {
    PLStatusFilter.ALL -> stringResource(Res.string.pl_filter_all)
    PLStatusFilter.PROFITABLE -> stringResource(Res.string.pl_filter_profitable)
    PLStatusFilter.LOSS_MAKING -> stringResource(Res.string.pl_filter_loss_making)
}

@Composable
fun VehiclePLSortOption.localizedLabel(): String = when (this) {
    VehiclePLSortOption.PROFIT_HIGH_LOW -> stringResource(Res.string.sort_profit_high_low)
    VehiclePLSortOption.PROFIT_LOW_HIGH -> stringResource(Res.string.sort_profit_low_high)
    VehiclePLSortOption.LOSS_HIGH_LOW -> stringResource(Res.string.sort_loss_high_low)
    VehiclePLSortOption.REVENUE_HIGH_LOW -> stringResource(Res.string.sort_revenue_high_low)
    VehiclePLSortOption.EXPENSE_HIGH_LOW -> stringResource(Res.string.sort_expense_high_low)
    VehiclePLSortOption.TRIPS_HIGH_LOW -> stringResource(Res.string.sort_trips_high_low)
}

@Composable
fun TripPLSortOption.localizedLabel(): String = when (this) {
    TripPLSortOption.PROFIT_HIGH_LOW -> stringResource(Res.string.sort_profit_high_low)
    TripPLSortOption.PROFIT_LOW_HIGH -> stringResource(Res.string.sort_profit_low_high)
    TripPLSortOption.LOSS_HIGH_LOW -> stringResource(Res.string.sort_loss_high_low)
    TripPLSortOption.DATE_NEWEST -> stringResource(Res.string.sort_date_newest)
    TripPLSortOption.DATE_OLDEST -> stringResource(Res.string.sort_date_oldest)
    TripPLSortOption.REVENUE_HIGH_LOW -> stringResource(Res.string.sort_revenue_high_low)
}

@Composable
fun ReportViewMode.localizedLabel(): String = when (this) {
    ReportViewMode.SUMMARY -> stringResource(Res.string.report_view_summary)
    ReportViewMode.LIST -> stringResource(Res.string.report_view_list)
    ReportViewMode.CHART -> stringResource(Res.string.report_view_chart)
}

@Composable
fun ReportChartType.localizedLabel(): String = when (this) {
    ReportChartType.BAR -> stringResource(Res.string.report_chart_bar)
    ReportChartType.PIE -> stringResource(Res.string.report_chart_pie)
}

@Composable
fun ReportExportFormat.localizedLabel(): String = when (this) {
    ReportExportFormat.PDF -> stringResource(Res.string.export_format_pdf)
    ReportExportFormat.CSV -> stringResource(Res.string.export_format_csv)
    ReportExportFormat.EXCEL -> stringResource(Res.string.export_format_excel)
}
