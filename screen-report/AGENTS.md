# AGENTS.md — screen-report

## Purpose

Full-stack **Reports & Analytics** feature module. Provides financial reporting for fleet owners and general managers: Vehicle Profit/Loss, Trip Profit/Loss, Cost Analysis, and Consolidated P&L. This is the largest `screen-*` module by file count (43 files) due to the complexity of the Vehicle P&L wizard and report visualization.

**Package:** `com.ijs.reports`
**Targets:** Android, iOS, JS, WasmJS

---

## Source Tree

```
src/commonMain/kotlin/com/ijs/reports/
├── LogTags.kt
├── data/
│   ├── datasource/
│   │   └── ReportsRemoteDataSource.kt     # API: vehicle PL, trip PL, consolidated PL, cost analysis
│   ├── mapper/
│   │   └── ProfitLossMapper.kt            # DTO ↔ domain entity mapping for all report types
│   ├── model/
│   │   ├── ConsolidatedPLDto.kt           # Consolidated P&L response DTO
│   │   ├── PLSummaryDto.kt                # Summary DTO shared across report types
│   │   ├── ProfitLossDto.kt               # Vehicle P&L response DTO
│   │   ├── ProfitLossRequest.kt           # Request DTO with date range + filters
│   │   ├── TripPLDto.kt                   # Trip P&L response DTO
│   │   └── VehiclePLDto.kt                # Vehicle P&L detailed DTO
│   └── repository/
│       └── ReportsRepositoryImpl.kt       # Repository impl with auth token pattern
├── domain/
│   ├── entity/
│   │   └── ProfitLossEntities.kt          # All report domain entities (VehiclePL, TripPL, CostBreakdown, etc.)
│   ├── repository/
│   │   └── ReportsRepository.kt           # Repository interface
│   └── usecase/
│       └── ReportsUseCases.kt             # GetVehiclePL, GetTripPL, GetConsolidatedPL, GetCostAnalysis
└── presentation/
    ├── ReportsFeatureFacade.kt            # DI entry point
    ├── ReportEnums.kt                     # ReportPeriod enum (today, weekly, 15days, monthly, quarterly, etc.)
    ├── ReportsColors.kt                   # Report-specific color scheme (profit green, loss red, etc.)
    ├── ReportsContract.kt                 # Hub screen MVI contract
    ├── ReportsScreen.kt                   # Reports hub with period selector + summary cards
    ├── ReportsViewModel.kt                # Hub ViewModel
    ├── ReportsSummaryCards.kt             # Summary cards for hub screen
    ├── ReportsDetailCards.kt              # Detailed report cards shared across sub-screens
    ├── ReportDateRangeDialog.kt           # Custom date range selection dialog
    ├── ReportsDateRangePickerDialog.kt    # Date range picker integration
    ├── consolidated/
    │   ├── ConsolidatedPLContract.kt
    │   ├── ConsolidatedPLScreen.kt        # Overall fleet P&L statement
    │   └── ConsolidatedPLViewModel.kt
    ├── cost/
    │   ├── CostAnalysisContract.kt
    │   ├── CostAnalysisScreen.kt          # Cost breakdown by type with pie chart
    │   └── CostAnalysisViewModel.kt
    ├── trip/
    │   ├── TripPLContract.kt
    │   ├── TripProfitLossScreen.kt        # Revenue vs expenses per trip
    │   └── TripPLViewModel.kt
    └── vehicle/
        ├── VehiclePLContract.kt
        ├── VehicleProfitLossScreen.kt     # ★ Multi-step wizard: select vehicle → date range → view results
        ├── VehiclePLViewModel.kt          # Handles wizard state + API calls + PDF generation
        ├── VehiclePLDateCalculator.kt     # Date range calculation from period enum
        ├── VehiclePLVehicleSelector.kt    # Vehicle selection step UI
        ├── VehiclePLWizardContent.kt      # Wizard orchestration composable
        ├── VehiclePLFleetOverviewContent.kt # Fleet-wide P&L overview
        ├── VehiclePLResultComponents.kt   # P&L result display (revenue, expenses, profit)
        ├── VehiclePLSummaryComponents.kt  # Summary cards for vehicle P&L
        ├── VehiclePLListComponents.kt     # Vehicle P&L list items
        └── VehiclePLReportGenerator.kt    # PDF report generation via ijs-pdf-report
```

---

## Report Types

| Report | API Endpoint | Description |
|--------|-------------|-------------|
| Vehicle P&L | `/reports/vehicle-profit-loss` | Revenue vs expenses per vehicle with cost breakdown |
| Trip P&L | `/reports/trip-profit-loss` | Revenue vs expenses per trip |
| Consolidated P&L | `/reports/consolidated-profit-loss` | Overall fleet P&L statement |
| Cost Analysis | `/reports/cost-analysis` | Cost breakdown by type (fuel, maintenance, driver, etc.) |

### Report Periods

`today` | `weekly` | `15_days` | `monthly` | `quarterly` | `half_yearly` | `yearly` | `custom`

Custom period uses `ReportDateRangeDialog` with `DD-MM-YYYY` format.

---

## Module Dependencies

| Kind | Dependency |
|------|------------|
| api | `:ijs-network-lib` |
| implementation | `:screen-vehicle`, `:screen-trip` |
| implementation | `:ijs-pdf-report`, `:ijs-datetime-picker` |
| libs | kotlinx-coroutines, kotlinx-serialization, kermit, ktor-client, kotlinx-datetime |

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| ReportsScreen | `Reports` | Hub with period selector + summary cards |
| VehicleProfitLossScreen | `VehicleProfitLoss` | Multi-step wizard: vehicle → period → results |
| TripProfitLossScreen | `TripProfitLoss` | Trip-level P&L list with date filter |
| CostAnalysisScreen | `CostAnalysis` | Pie chart + cost breakdown by type |
| ConsolidatedPLScreen | `ConsolidatedPL` | Overall fleet financial statement |

---

## Key Patterns

- **Access control** — Reports are Owner/GM-only. The hub screen checks `UserRole` and hides financial data for Manager/Supervisor roles.
- **Vehicle P&L wizard** — The most complex UI flow: vehicle selection → date range → API call → result display → PDF export. Split across 11 files in `vehicle/`.
- **Report colors** — `ReportsColors` provides semantic colors (profit green, loss red, neutral) used consistently across all report screens.
- **PDF generation** — `VehiclePLReportGenerator` delegates to `ijs-pdf-report` for HTML→PDF conversion with fleet branding.
- **Date calculation** — `VehiclePLDateCalculator` computes start/end dates from `ReportPeriod` enum using `kotlinx-datetime`.
