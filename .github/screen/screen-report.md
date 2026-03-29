# screen-report

## Overview

**Package:** `com.ijs.reports`
**Module type:** Full-stack feature module (Data + Domain + Presentation)
**Purpose:** Financial reporting and analytics — Vehicle Profit/Loss, Trip Profit/Loss, Cost Analysis by type, and Consolidated P&L statements. **Owner and General Manager access only.** Supports period filters (today, weekly, monthly, quarterly, yearly, custom), PDF export, and detailed cost breakdowns.

---

## Architecture

| Layer | Contents |
|-------|----------|
| **Presentation** | `ReportsFeatureFacade`, Hub (Contract/VM/Screen), Vehicle PL (Contract/VM/Screen), Trip PL (Contract/VM/Screen), Cost Analysis (Contract/VM/Screen), Consolidated PL (Contract/VM/Screen) |
| **Domain** | `TripProfitLoss`, `VehicleProfitLoss`, `FleetProfitLoss`, `ConsolidatedPL`, `CostTypeAnalysis`, `CostBreakdownItem`, `PLSummary`; `ReportsRepository` |
| **Data** | `ReportsRemoteDataSource`, `ReportsRepositoryImpl`, `ProfitLossDto`, `ProfitLossRequest` |

---

## Dependencies

```
screen-report → ijs-network-lib → ijs-core-lib
screen-report → screen-vehicle (Vehicle entity for vehicle selection)
screen-report → screen-trip (Trip entity for trip selection)
screen-report → ijs-pdf-report, ijs-datetime-picker
```

**Cross-feature note:** Depends on `screen-vehicle` and `screen-trip` for entity types used in vehicle/trip selection dropdowns in report generation forms.

---

## Screens

### 1. ReportsScreen (Hub)

| Property | Value |
|----------|-------|
| Route | `FleetRoute.Reports` |
| ViewModel | `ReportsViewModel` |
| Contract | `ReportsContract` |

**Features:**
- Period filter: Today, Weekly, 15 Days, Monthly, Quarterly, Half-Yearly, Yearly, Custom
- Quick summary card: total revenue, expenses, profit/loss, margin
- Expense breakdown (pie chart data)
- Navigation cards to: Vehicle P&L, Trip P&L, Cost Analysis, Consolidated P&L
- PDF export of summary report

**Profit Status thresholds:**
| Status | Margin | Color |
|--------|--------|-------|
| Highly Profitable | > 20% | Dark Green |
| Profitable | 10–20% | Green |
| Break-even | 0–10% | Amber |
| Loss | 0 to -20% | Red |
| Severe Loss | < -20% | Dark Red |

---

### 2. VehicleProfitLossScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.VehicleProfitLoss` |
| ViewModel | `VehiclePLViewModel` |
| Contract | `VehiclePLContract` |

**Features:**
- Vehicle multi-select dropdown
- Date range picker
- Period filter
- P&L cards per vehicle: revenue, trip costs, maintenance costs, gross/net profit, margin
- Cost breakdown per vehicle
- Trip summary within each vehicle
- Sort by profit margin

---

### 3. TripProfitLossScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.TripProfitLoss` |
| ViewModel | `TripPLViewModel` |
| Contract | `TripPLContract` |

**Features:**
- Vehicle filter (optional)
- Date range picker
- Trip P&L cards: route, vehicle, driver, purchase price vs selling value, costs, profit
- Cost breakdown per trip
- Sort by profit margin

---

### 4. CostAnalysisScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.CostAnalysis` |
| ViewModel | `CostAnalysisViewModel` |
| Contract | `CostAnalysisContract` |

**Features:**
- Cost type multi-select (dynamic types from API + static fallback)
- Trip cost types: fuel, toll, driver_allowance, parking, loading/unloading, chalan, permit, insurance, other
- Maintenance cost types: tyre, battery, servicing, engine_repair, body_repair, electrical, ac_repair, other
- Date range picker
- Results: cost type breakdown with total amount, count, average per entry
- Grouped by cost type category

---

### 5. ConsolidatedPLScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.ConsolidatedPL` |
| ViewModel | `ConsolidatedPLViewModel` |
| Contract | `ConsolidatedPLContract` |

**Features:**
- Vehicle multi-select
- Cost type multi-select
- Date range picker
- Group by: day, week, month
- Consolidated P&L table with period breakdown
- Total fleet revenue, expenses, profit, margin
- Vehicle-level breakdown within consolidated view

---

## Facade

```kotlin
object ReportsFeatureFacade {
    fun ReportsHubEntry(viewModel, onNavigateBack, onNavigateToVehiclePL, onNavigateToTripPL, onNavigateToCostAnalysis, onNavigateToConsolidatedPL)
    fun VehicleProfitLossEntry(viewModel, onNavigateBack)
    fun TripProfitLossEntry(viewModel, onNavigateBack)
    fun CostAnalysisEntry(viewModel, onNavigateBack)
    fun ConsolidatedPLEntry(viewModel, onNavigateBack)
}
```

---

## Domain Entities

| Entity | Description |
|--------|-------------|
| `TripProfitLoss` | P&L for a single trip |
| `VehicleProfitLoss` | P&L for a single vehicle over period |
| `FleetProfitLoss` | Overall fleet P&L |
| `ConsolidatedPL` | Multi-vehicle consolidated with period grouping |
| `CostTypeAnalysis` | Cost breakdown by type with amounts and counts |
| `CostBreakdownItem` | Individual cost category with amount |
| `PLSummary` | High-level revenue/expense/profit summary |
| `TripSummaryItem` | Trip-level summary within vehicle P&L |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/reports/summary` | POST | Fleet P&L summary for period |
| `/reports/vehicle-pl` | POST | Vehicle-level P&L |
| `/reports/trip-pl` | POST | Trip-level P&L |
| `/reports/cost-analysis` | POST | Cost type breakdown |
| `/reports/consolidated-pl` | POST | Consolidated P&L with grouping |

---

## Access Control

**Owner and General Manager only.** Manager and Supervisor roles cannot access any report screens. The Dashboard navigation drawer hides the Reports item for unauthorized roles. The API also enforces role checks server-side.

