# Reports Feature

> Use this prompt when working on P&L reports, cost analysis, or financial reporting.

## Access Control

**Owner and General Manager only.** Manager and Supervisor must NOT see reports or any financial data (trip_price, revenue, profit).

## Screens & Routes

| Screen | Route | Description |
|--------|-------|-------------|
| `ReportsScreen` | `Reports` | Hub with summary cards and period filter |
| `VehicleProfitLossScreen` | `VehicleProfitLoss` | Revenue vs expenses per vehicle |
| `TripProfitLossScreen` | `TripProfitLoss` | Revenue vs expenses per trip |
| `CostAnalysisScreen` | `CostAnalysis` | Cost breakdown by type with pie chart |
| `ConsolidatedPLScreen` | `ConsolidatedPL` | Overall fleet P&L statement |

## API Endpoints

```
POST   /reports/profit-loss            → Consolidated P&L for period
GET    /vehicles/{id}/profit-loss      → Vehicle-specific P&L
GET    /trips/{id}/profit-loss         → Trip-specific P&L
POST   /reports/cost-analysis          → Cost breakdown by type
```

## Report Periods

```kotlin
sealed class ReportPeriod {
    data object Daily : ReportPeriod()
    data object Weekly : ReportPeriod()           // Last 7 days
    data object FifteenDays : ReportPeriod()      // Last 15 days
    data object Monthly : ReportPeriod()          // Current month
    data object Quarterly : ReportPeriod()        // Current quarter (Apr-Jun, Jul-Sep, Oct-Dec, Jan-Mar)
    data object HalfYearly : ReportPeriod()       // Last 6 months
    data object Yearly : ReportPeriod()           // Financial year (Apr-Mar)
    data class Custom(val startDate: LocalDate, val endDate: LocalDate) : ReportPeriod()
}
```

**Financial year:** April 1 – March 31 (Indian standard)

## P&L Formula

```
Revenue = SUM(trip_price) for completed trips in period
├── Trip Costs = SUM(fuel, toll, driver_allowance, loading, etc.)
├── Maintenance Costs = SUM(tyre, battery, servicing, etc.)
├── Driver Costs = SUM(salary, bonus, advance, etc.)
└── EMI Costs = SUM(vehicle loan EMI payments in period)
Total Expenses = Trip + Maintenance + Driver + EMI Costs
Net Profit = Revenue - Total Expenses
Profit Margin = (Net Profit / Revenue) × 100%
```

## Cost Analysis

Breaks down expenses by category with:
- Pie chart visualization (using `PieChart` component)
- Category totals
- Percentage of total
- Date range filtering

## PDF Export

Reports can be exported as PDF via `ijs-pdf-report`:
```kotlin
PdfReportFacade.generateVehicleProfitLossReport(data)
PdfReportFacade.generateFleetProfitLossReport(data)
```

## Key Files

| Layer | File |
|-------|------|
| Repository | `domain/repository/reports/ReportsRepository.kt` |
| DataSource | `data/datasource/reports/ReportsRemoteDataSource.kt` |
| Repository Impl | `data/repository/reports/ReportsRepositoryImpl.kt` |
| Hub | `presentation/reports/ReportsViewModel.kt` + `ReportsScreen.kt` |
| Vehicle P&L | `presentation/reports/vehicle/` |
| Trip P&L | `presentation/reports/trip/` |
| Cost Analysis | `presentation/reports/cost/` |
| Consolidated | `presentation/reports/consolidated/` |

