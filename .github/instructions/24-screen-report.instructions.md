# screen-report — IndusJS Fleet

## Purpose

Reports & analytics: Profit/Loss by vehicle/trip, cost analysis, consolidated P&L.
**Owner/GM only access.**

## Package: `com.ijs.reports`

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| ReportsHubScreen | `Reports` | Summary with period filter |
| VehiclePLScreen | `VehicleProfitLoss` | Revenue vs expenses per vehicle |
| TripPLScreen | `TripProfitLoss` | Revenue vs expenses per trip |
| CostAnalysisScreen | `CostAnalysis` | Breakdown by cost type |
| ConsolidatedPLScreen | `ConsolidatedPL` | Overall P&L statement |

## Key Files

| File | Purpose |
|------|---------|
| `presentation/ReportFeatureFacade.kt` | Facade — 5 entry points |
| `presentation/ReportsContract.kt` | Hub State/Intent/Effect |
| `presentation/ReportsViewModel.kt` | Hub ViewModel |
| `presentation/vehicle/VehiclePL*.kt` | Vehicle P&L Contract/VM/Screen |
| `presentation/trip/TripPL*.kt` | Trip P&L Contract/VM/Screen |
| `presentation/cost/CostAnalysis*.kt` | Cost Analysis Contract/VM/Screen |
| `presentation/consolidated/ConsolidatedPL*.kt` | Consolidated P&L Contract/VM/Screen |
| `data/datasource/ReportsRemoteDataSource.kt` | API calls |
| `data/repository/ReportsRepositoryImpl.kt` | Repository impl |
| `domain/repository/ReportsRepository.kt` | Repository interface |

## Report Periods

`today, weekly, 15_days, monthly, quarterly, half_yearly, yearly, custom`

## APIs

- `GET /reports/vehicle-pl` — Vehicle P&L
- `GET /reports/trip-pl` — Trip P&L
- `GET /reports/cost-analysis` — Cost breakdown
- `GET /reports/consolidated-pl` — Overall P&L

## Module Path

`screen-report/src/commonMain/kotlin/com/ijs/reports/`

## Depends On: `ijs-network-lib`

