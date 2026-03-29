# 07 — Profit & Loss Analysis (Comprehensive)

This document covers the complete P&L reporting system: how profit and loss are calculated across **Trip**, **Vehicle**, **Driver**, and **Customer** dimensions, what filters and sorts are available, and the data flow from API to screen.

> **Access:** Owner and General Manager only. Manager and Supervisor roles cannot access P&L reports.

---

## Table of Contents

1. [P&L Overview](#1-pl-overview)
2. [Trip P&L](#2-trip-pl)
3. [Vehicle P&L](#3-vehicle-pl)
4. [Driver P&L (Cost Analysis)](#4-driver-pl)
5. [Customer P&L (Financial Report)](#5-customer-pl)
6. [Consolidated P&L (Fleet-wide)](#6-consolidated-pl)
7. [Cost Type Analysis](#7-cost-type-analysis)
8. [Reports Hub (Summary)](#8-reports-hub)
9. [Profitability Status Thresholds](#9-profitability-thresholds)
10. [Master Filter & Sort Reference](#10-master-filter-sort-reference)
11. [What's Included vs Excluded](#11-included-vs-excluded)
12. [API Reference](#12-api-reference)
13. [Source Files](#13-source-files)

---

## 1. P&L Overview

### The Revenue-Expense Model

```
                    ┌─────────────────────────────────────────────┐
                    │              REVENUE SIDE                    │
                    │                                             │
                    │  Trip Price (selling_value / purchase_price) │
                    │  = What customer agreed to pay              │
                    │                                             │
                    │  ⚠ NOT the sum of collected payments        │
                    │    (payments track collection, not revenue) │
                    └───────────────────┬─────────────────────────┘
                                        │
                         ┌──────────────┴──────────────┐
                         │         GROSS PROFIT         │
                         │  = Revenue − Total Expenses  │
                         └──────────────┬──────────────┘
                                        │
                    ┌───────────────────┴─────────────────────────┐
                    │              EXPENSE SIDE                    │
                    │                                             │
                    │  Trip Costs (TC-*)                          │
                    │    Fuel, Toll, Loading, Driver Allow, etc.  │
                    │                                             │
                    │  Maintenance Costs (MC-*)                   │
                    │    Tyre, Battery, Servicing, Body Repair    │
                    │                                             │
                    │  ⚠ NOT included:                            │
                    │    - Driver Costs (DC-*) — workforce cost   │
                    │    - EMI/Loan — capital expenditure          │
                    │    - Payments — revenue side tracking        │
                    └─────────────────────────────────────────────┘
```

### Dimension Matrix

| Dimension | Revenue Source | Expense Source | Status |
|-----------|--------------|----------------|--------|
| **Trip** | `selling_value` (trip price) | TC-* costs for that trip | ✅ Implemented |
| **Vehicle** | Sum of `trip_price` for vehicle's trips | TC-* + MC-* costs for vehicle | ✅ Implemented |
| **Driver** | N/A (cost-only analysis) | DC-* earnings vs deductions | ✅ Via Driver Cost Screen |
| **Customer** | Sum of `trip_price` for customer's trips | TC-* costs for customer's trips | ✅ Via Customer Detail |
| **Fleet (Consolidated)** | Total `trip_price` fleet-wide | Total TC-* + MC-* fleet-wide | ✅ Implemented |
| **Cost Type** | N/A (cost drill-down) | Specific TC-* or MC-* types | ✅ Implemented |

---

## 2. Trip P&L

### What Is It?

Analyzes profitability of individual trips: **Did this trip make money?**

### Revenue & Expense Formula

```
Trip Revenue  = selling_value (or purchase_price — the trip's selling price)
Trip Expenses = total_trip_costs (sum of ALL TC-* costs for this trip)
Gross Profit  = selling_value − total_trip_costs
Profit Margin = (gross_profit / selling_value) × 100
Is Profitable = gross_profit > 0
```

### What Counts as Trip Expense

| Group | ID | Items Included |
|-------|-----|----------------|
| Fuel & Energy | TC-G-001 | Diesel, Petrol, CNG, AdBlue, Electric |
| Road & Travel | TC-G-002 | Toll Charges, Parking, Weigh Bridge |
| Cargo Handling | TC-G-003 | Loading Charges, Unloading, Crane/JCB, Hamali, Packaging |
| Driver Expenses | TC-G-004 | Driver Allowance, Food & Meals, Accommodation |
| Permits & Legal | TC-G-005 | Permit, Overload Challan, Traffic Fine, Insurance |
| Misc Expenses | TC-G-006 | Commission/Brokerage, Communication, Miscellaneous |

### What's NOT Included

- ❌ Maintenance costs (MC-*) — these are vehicle-level, not trip-level
- ❌ Driver costs (DC-*) — TC-G-004 already covers trip-level driver expenses
- ❌ EMI/Loan payments — capital expenditure
- ❌ Trip payments — revenue side (collection tracking)

### Trip P&L Entity

```kotlin
data class TripProfitLoss(
    val tripId: Int,
    val vehicleId: Int?,
    val vehicleNumber: String?,
    val driverId: Int?,
    val driverName: String?,
    val startLocation: String?,
    val endLocation: String?,
    val scheduledDate: String?,       // Trip date
    val state: String?,               // Trip state (completed, on_route, etc.)
    val purchasePrice: Double,        // ≡ selling_value (revenue)
    val sellingValue: Double,         // Same as purchasePrice
    val totalTripCosts: Double,       // Sum of TC-* costs
    val totalExpenses: Double,        // Same as totalTripCosts for trips
    val grossProfit: Double,          // sellingValue − totalExpenses
    val netProfit: Double,            // Same as grossProfit for trips
    val profitMargin: Double,         // (grossProfit / sellingValue) × 100
    val isProfitable: Boolean,        // grossProfit > 0
    val costBreakdown: List<CostBreakdownItem>  // TC-* cost breakdown
)
```

### Filter Options

| Filter | Type | Values | Description |
|--------|------|--------|-------------|
| **Date Range** | Date picker | `startDate` / `endDate` (DD-MM-YYYY) | Required — loads trips within range |
| **Trip Search** | Text | Free text query | Search by trip ID, location, vehicle reg, customer name |
| **Trip Selection** | Multi-select | Checkboxes on trip list | Select specific trips for analysis |
| **P&L Status** | Enum | `ALL` / `PROFITABLE` / `LOSS_MAKING` | Filter results by profitability |

### Sort Options

| Sort | Enum | Logic |
|------|------|-------|
| Profit (High to Low) | `PROFIT_HIGH_LOW` | `sortedByDescending { netProfit }` |
| Profit (Low to High) | `PROFIT_LOW_HIGH` | `sortedBy { netProfit }` |
| Loss (High to Low) | `LOSS_HIGH_LOW` | `sortedBy { netProfit }` (most negative first) |
| Date (Newest) | `DATE_NEWEST` | `sortedByDescending { scheduledDate }` |
| Date (Oldest) | `DATE_OLDEST` | `sortedBy { scheduledDate }` |
| Revenue (High to Low) | `REVENUE_HIGH_LOW` | `sortedByDescending { sellingValue }` |

### Summary Stats (Computed Client-Side)

| Stat | Computation |
|------|-------------|
| `totalProfitableTrips` | `results.count { isProfitable }` |
| `totalLossMakingTrips` | `results.count { !isProfitable }` |
| `totalRevenue` | `results.sumOf { sellingValue }` |
| `totalExpenses` | `results.sumOf { totalExpenses }` |
| `totalNetProfit` | `results.sumOf { netProfit }` |
| `averageMargin` | `results.map { profitMargin }.average()` |

### User Flow

```
1. User opens Trip P&L screen
2. Selects date range (start/end date)
3. Taps "Load Trips" → trips for date range fetched
4. Selects trips (checkboxes, or "Select All")
5. Taps "Generate Report"
6. API called: POST /reports/profit-loss/trips
7. Results displayed with sort/filter controls
```

---

## 3. Vehicle P&L

### What Is It?

Analyzes profitability of vehicles over a time period: **Is this vehicle earning its keep?**

### Revenue & Expense Formula

```
Vehicle Revenue    = Sum of trip_price for ALL completed trips (in period)
Trip Costs         = Sum of ALL TC-* costs across all vehicle's trips
Maintenance Costs  = Sum of ALL MC-* costs for the vehicle
Total Expenses     = Trip Costs + Maintenance Costs
Gross Profit       = Vehicle Revenue − Total Expenses
Profit Margin      = (Gross Profit / Vehicle Revenue) × 100
Is Profitable      = Gross Profit > 0
```

### What Counts as Vehicle Expense

| Category | Cost Types | Scope |
|----------|-----------|-------|
| **Trip Costs (TC-*)** | All 6 groups (22 items) | All trips in period |
| **Maintenance Costs (MC-*)** | All 6 groups (23 items) | Direct vehicle costs in period |

> MC-* includes: Tyres, Battery, Engine, Electrical, Body Work, Servicing & Fluids, Accessories

### Vehicle P&L Entity

```kotlin
data class VehicleProfitLoss(
    val vehicleId: Int,
    val vehicleNumber: String?,
    val make: String?,
    val model: String?,
    val period: String?,               // "monthly", "weekly", etc.
    val startDate: String?,
    val endDate: String?,
    // Trip stats
    val totalTrips: Int,
    val completedTrips: Int,
    // Financials
    val totalRevenue: Double,          // Sum of trip_price
    val totalTripCosts: Double,        // Sum of TC-* costs
    val totalMaintenanceCosts: Double, // Sum of MC-* costs
    val totalExpenses: Double,         // tripCosts + maintenanceCosts
    val grossProfit: Double,
    val netProfit: Double,
    val profitMargin: Double,          // %
    val isProfitable: Boolean,
    // Breakdowns
    val costBreakdown: List<CostBreakdownItem>,  // By cost type
    val tripSummary: List<TripSummaryItem>       // Per-trip breakdown
)
```

### Filter Options

| Filter | Type | Values | Description |
|--------|------|--------|-------------|
| **Period** | Preset | `today` / `weekly` / `monthly` / `yearly` / `custom` | Time window |
| **Custom Date Range** | Date picker | `startDate` / `endDate` | When period = "custom" |
| **Vehicle Selection** | Multi-select | Vehicle filter sheet | Select specific vehicles |
| **Vehicle Search** | Text | Free text | Search by reg number, make, model, driver name |
| **P&L Status** | Enum | `ALL` / `PROFITABLE` / `LOSS_MAKING` | Filter by profitability |
| **Fleet Overview** | Toggle | `isFleetOverviewMode` | Show all vehicles (default) vs single vehicle |

### Sort Options

| Sort | Enum | Logic |
|------|------|-------|
| Profit (High to Low) | `PROFIT_HIGH_LOW` | `sortedByDescending { netProfit }` |
| Profit (Low to High) | `PROFIT_LOW_HIGH` | `sortedBy { netProfit }` |
| Loss (High to Low) | `LOSS_HIGH_LOW` | `sortedBy { netProfit }` (most negative first) |
| Revenue (High to Low) | `REVENUE_HIGH_LOW` | `sortedByDescending { totalRevenue }` |
| Expense (High to Low) | `EXPENSE_HIGH_LOW` | `sortedByDescending { totalExpenses }` |
| Trips (Most to Least) | `TRIPS_HIGH_LOW` | `sortedByDescending { totalTrips }` |

### View Modes

| Mode | Enum | Description |
|------|------|-------------|
| Summary | `SUMMARY` | Cards with key metrics, fleet overview |
| List | `LIST` | Tabular list of all vehicle P&L results |
| Chart | `CHART` | Visual charts (Bar or Pie) |

### Chart Types (in Chart mode)

| Type | Description |
|------|-------------|
| `BAR` | Bar chart — revenue vs expenses per vehicle |
| `PIE` | Pie chart — expense breakdown |

### Fleet Overview Stats (Computed Client-Side)

| Stat | Computation |
|------|-------------|
| `totalProfitableCount` | `multiResults.count { isProfitable }` |
| `totalLossMakingCount` | `multiResults.count { !isProfitable }` |
| `totalRevenue` | `multiResults.sumOf { totalRevenue }` |
| `totalExpenses` | `multiResults.sumOf { totalExpenses }` |
| `totalNetProfit` | `multiResults.sumOf { netProfit }` |
| `fleetProfitMargin` | `(totalNetProfit / totalRevenue) × 100` |
| `averageProfitPerVehicle` | `totalNetProfit / vehicleCount` |
| `topPerformer` | `multiResults.maxByOrNull { netProfit }` |
| `worstPerformer` | `multiResults.minByOrNull { netProfit }` |

### Export Options

| Format | Enum | Extension | Description |
|--------|------|-----------|-------------|
| PDF Report | `PDF` | `.pdf` | Formatted report |
| CSV Data | `CSV` | `.csv` | Raw data export |
| Excel Sheet | `EXCEL` | `.xlsx` | Spreadsheet export |

### User Flow

```
1. User opens Vehicle P&L → Fleet Overview mode (default)
2. All vehicles loaded, period = "monthly" by default
3. P&L generated for ALL vehicles automatically
4. User can:
   a. Change period (today/weekly/monthly/yearly/custom)
   b. Filter by specific vehicles (filter sheet)
   c. Filter by profitability status
   d. Sort by profit/revenue/expense/trips
   e. Switch view mode (summary/list/chart)
   f. Select single vehicle for detailed view
   g. Export report (PDF/CSV/Excel)
```

---

## 4. Driver P&L

### What Is It?

Analyzes **driver costs** — earnings paid to drivers vs deductions. This is NOT a traditional P&L (drivers don't generate revenue directly), but a **cost analysis per driver**.

### Earnings vs Deductions Formula

```
Total Earnings   = Sum of (DC-G-001 + DC-G-002 + DC-G-004) costs
                   = Salary + Bonuses + Other allowances
                   
Total Deductions = Sum of DC-G-003 costs
                   = Advance Recovery + Damage + Fines + Loan EMI + Insurance
                   
Net Amount       = Total Earnings − Total Deductions

Detection: isDeduction = (groupId == "DC-G-003") || (isDeduction flag)
```

### Earnings Groups (Positive for Driver)

| Group | ID | Items |
|-------|-----|-------|
| **Salary & Wages** | DC-G-001 | Monthly Salary, Daily Wages, Overtime, Holiday Pay |
| **Incentives & Bonuses** | DC-G-002 | Trip Bonus, Performance Bonus, Fuel Savings, On-Time Delivery, Safety Bonus |
| **Other** | DC-G-004 | Training, Uniform, Medical, License Renewal, Other |

### Deduction Groups (Negative for Driver)

| Group | ID | Items |
|-------|-----|-------|
| **Deductions** | DC-G-003 | Advance Recovery, Damage Deduction, Fine, Loan EMI, Insurance |

### Driver Cost Summary Entity (from API)

```kotlin
data class DriverCostsSummaryDto(
    val totalEarnings: Double,     // Sum of non-deduction costs
    val totalDeductions: Double,   // Sum of DC-G-003 costs
    val netAmount: Double,         // earnings − deductions
    val costCount: Int             // Total entries
)
```

### Profit/Loss Interpretation for Drivers

| Scenario | Meaning | Action |
|----------|---------|--------|
| **High Earnings, Low Deductions** | Driver is well-compensated, few issues | Healthy — retain driver |
| **High Deductions** | Many fines, damages, advance recovery | Investigate — driver performance issue? |
| **Net Amount growing** | Driver cost to company increasing | Review — salary vs revenue generated |
| **Trip-linked costs (via TC→DC sync)** | Trip allowances flowing to driver ledger | Normal — auto-synced from trip costs |

### Filter Options

| Filter | Type | Description |
|--------|------|-------------|
| **Driver** | Selection | Select specific driver |
| **Month** | Text (YYYY-MM) | Filter by month (salary period) |
| **Date Range** | Date picker | Start/end date |
| **Cost Type** | Selection | Specific DC-* item |

### Sort Options (Driver Cost List)

| Sort | Description |
|------|-------------|
| Date (Newest) | Most recent costs first |
| Date (Oldest) | Oldest costs first |
| Amount (High to Low) | Highest amounts first |
| Amount (Low to High) | Lowest amounts first |

### Implementation Status

- ✅ **Driver cost entry** — `screen-driver` (Create/View driver costs)
- ✅ **Driver cost summary** — API returns earnings/deductions/net
- ✅ **Trip → Driver sync** — TC-G-004 auto-creates DC-G-004 entries
- ⏳ **Driver P&L Report** — Navigation intent exists (`NavigateToDriverCostReport`) but dedicated report screen pending
- ⏳ **Multi-driver comparison** — Not yet implemented

---

## 5. Customer P&L

### What Is It?

Analyzes profitability per customer: **Is this customer relationship profitable?**

### Revenue & Expense Formula

```
Customer Revenue    = Sum of trip_price for ALL customer's trips
Customer Costs      = Sum of TC-* costs for ALL customer's trips  
Net Profit          = Revenue − Costs
Profit Margin       = (Net Profit / Revenue) × 100

Additionally tracked:
  Payment Received  = Sum of collected payments
  Payment Pending   = Revenue − Payment Received
```

### Customer Financial Report Entity

```kotlin
data class CustomerFinancialReport(
    val customerId: String,
    val customerName: String?,
    val period: FinancialPeriod,      // monthly/quarterly/yearly/custom
    val startDate: String?,
    val endDate: String?,
    // P&L
    val totalRevenue: Double,         // Sum of trip prices
    val totalCosts: Double,           // Sum of trip costs
    val netProfit: Double,            // Revenue − Costs
    val profitMargin: Double,         // %
    // Trip Summary
    val tripSummary: FinancialTripSummary?,
    // Time-series
    val periodBreakdown: List<PeriodBreakdown>,
    // Vehicle performance for this customer
    val topVehicles: List<TopVehicle>,
    // Collection tracking
    val paymentReceived: Double,      // What's been collected
    val paymentPending: Double        // What's still outstanding
)
```

### Customer Statistics Entity

```kotlin
data class CustomerStatistics(
    val customerId: String,
    val totalTrips: Int,
    val completedTrips: Int,
    val activeTrips: Int,
    val totalRevenue: Double,          // Sum of trip prices
    val totalPendingPayment: Double,   // Outstanding
    val totalReceivedPayment: Double,  // Collected
    val averageTripValue: Double,      // Revenue / trips
    val lastTripDate: String?
)
```

### Customer Pending Payments

```kotlin
data class CustomerPendingPayment(
    val tripId: String,
    val vehicleRegistration: String?,
    val startLocation: String?, 
    val endLocation: String?,
    val tripDate: String?,
    val tripPrice: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val daysOverdue: Int,              // Days past expected collection
    val state: String?,
    val paymentStatus: String?
)
```

**Overdue severity:**
- `isOverdue` = `daysOverdue > 0`
- `isCriticalOverdue` = `daysOverdue > 7`

### Customer Payment Summary

```kotlin
data class CustomerPaymentSummary(
    val byMode: List<PaymentByMode>,    // Cash: 60%, UPI: 30%, etc.
    val byMonth: List<MonthlyPayment>,  // Jan: ₹5L, Feb: ₹3L, etc.
    val totalTds: Double,               // Total TDS collected
    val totalAmount: Double,            // Grand total payments
    val totalPayments: Int              // Count of payment records
)
```

### Filter Options

| Filter | Type | Description |
|--------|------|-------------|
| **Customer** | Selection | Select from customer list |
| **Period** | Enum | `MONTHLY` / `QUARTERLY` / `YEARLY` / `CUSTOM` |
| **Date Range** | Date picker | For custom period |

### Sort Options (Customer Trip List)

| Sort | Description |
|------|-------------|
| Trip Date (Newest) | Most recent trips first |
| Trip Date (Oldest) | Oldest trips first |
| Revenue (High) | Highest value trips first |
| Pending (High) | Most outstanding first |

### Key Metrics per Customer

| Metric | Meaning | Alert Threshold |
|--------|---------|-----------------|
| **Total Revenue** | Lifetime value of the customer | — |
| **Average Trip Value** | Revenue / total trips | Below fleet average = concern |
| **Pending Payment** | Outstanding collection | > 30 days overdue = critical |
| **Payment Collection Rate** | received / revenue × 100 | < 70% = warning |
| **Trip Frequency** | Trips per month | Declining = risk of churn |

### Customer Profitability View (in Customer Detail)

The Customer Detail screen has a **Financials** section (Owner/GM only) that shows:

```
┌─────────────────────────────────────┐
│ Customer Financial Summary          │
├─────────────────────────────────────┤
│ Total Revenue     ₹12.5L           │
│ Total Costs       ₹8.2L            │
│ Net Profit        ₹4.3L  ✅        │
│ Profit Margin     34.4%            │
├─────────────────────────────────────┤
│ Payment Received  ₹10.0L           │
│ Payment Pending   ₹2.5L ⚠️         │
├─────────────────────────────────────┤
│ Period Breakdown (Monthly chart)    │
│ Top Vehicles for this customer     │
└─────────────────────────────────────┘
```

### Implementation Status

- ✅ **Customer statistics** — `CustomerStatistics` on Customer Detail
- ✅ **Customer trip history** — `CustomerTrip` with payment status
- ✅ **Pending payments** — `CustomerPendingPayment` with overdue tracking
- ✅ **Payment summary** — `CustomerPaymentSummary` with mode/month breakdown
- ✅ **Financial report** — `CustomerFinancialReport` with period breakdown
- ⏳ **Multi-customer comparison report** — Not yet in `screen-report`

---

## 6. Consolidated P&L (Fleet-wide)

### What Is It?

A **fleet-wide aggregate** P&L report with breakdowns by vehicle, trip, cost type, and time period.

### Formula

```
Fleet Revenue    = Sum of trip_price across ALL vehicles and trips
Fleet Expenses   = Total TC-* costs + Total MC-* costs (fleet-wide)
Net Profit       = Fleet Revenue − Fleet Expenses
Profit Margin    = (Net Profit / Fleet Revenue) × 100
```

### Consolidated P&L Entity

```kotlin
data class ConsolidatedPL(
    val startDate: String?,
    val endDate: String?,
    val groupBy: String?,         // "day", "week", "month"
    // Aggregates
    val totalRevenue: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val profitMargin: Double,
    val isProfitable: Boolean,
    // Counts
    val totalVehicles: Int,
    val totalTrips: Int,
    val completedTrips: Int,
    // Breakdowns
    val vehicleSummary: List<VehiclePLSummary>,   // Per-vehicle
    val tripSummary: List<TripPLSummary>,          // Per-trip
    val costBreakdown: List<CostBreakdownItem>,    // By cost type
    val periodBreakdown: List<PeriodBreakdown>     // By time period
)
```

### Filter Options

| Filter | Type | Values | Description |
|--------|------|--------|-------------|
| **Vehicle Selection** | Multi-select | Checkboxes | Select specific vehicles |
| **Cost Types** | Multi-select | TC-*/MC-* types | Which costs to include |
| **Date Range** | Date picker | Start/end date | Report time window |
| **Group By** | Selection | `day` / `week` / `month` | Time grouping for period breakdown |

### Predefined Cost Types in Consolidated View

```
fuel, toll, driver_allowance, parking, loading_charges,
unloading_charges, chalan, permit, insurance, maintenance, other
```

### Period Breakdown Entry

```kotlin
data class PeriodBreakdown(
    val period: String,       // "2025-12", "2025-W48", "2025-12-20"
    val label: String?,       // "December 2025", "Week 48", "Dec 20"
    val revenue: Double,
    val expenses: Double,
    val profit: Double,
    val tripCount: Int,
    val isProfitable: Boolean
)
```

---

## 7. Cost Type Analysis

### What Is It?

A deep-dive into specific cost types across vehicles and time. Answers: **"How much are we spending on fuel? Which vehicle burns the most?"**

### Entity

```kotlin
data class CostTypeAnalysis(
    val costType: String,
    val startDate: String?,
    val endDate: String?,
    val totalAmount: Double,
    val totalCount: Int,
    val averagePerEntry: Double,
    val vehicleBreakdown: List<VehicleCostBreakdown>,  // Per-vehicle spend
    val monthlyTrend: List<MonthlyTrend>                // Monthly trend
)
```

### Filter Options

| Filter | Type | Values | Description |
|--------|------|--------|-------------|
| **Cost Types** | Multi-select | All TC-* and MC-* items | Select cost types to analyze |
| **Date Range** | Date picker | Start/end date | Analysis time window |

The cost type list is **dynamically fetched** from the API and cached locally. Falls back to hardcoded values from `TripCostTypes.types` + `MaintenanceCostTypes.types` if API unavailable.

### Summary Stats

| Stat | Computation |
|------|-------------|
| `totalAmount` | `results.sumOf { totalAmount }` |
| `totalCount` | `results.sumOf { totalCount }` |
| **Vehicle Breakdown** | Which vehicle spent how much on this cost type |
| **Monthly Trend** | How spending on this cost type changes over months |

---

## 8. Reports Hub (Summary)

The Reports Hub screen is the entry point showing a high-level P&L summary with navigation to detailed reports.

### PLSummary Entity (from API)

```kotlin
data class PLSummary(
    // Period
    val startDate: String?,
    val endDate: String?,
    // Overview
    val totalRevenue: Double,
    val totalExpenses: Double,
    val grossProfit: Double,
    val profitMarginPercentage: Double,
    val status: String,                // "profit" or "loss"
    val isProfitable: Boolean,
    // Fleet stats
    val totalVehicles: Int,
    val activeVehicles: Int,
    val profitableVehicles: Int,
    val lossMakingVehicles: Int,
    // Trip stats
    val totalTrips: Int,
    val completedTrips: Int,
    val profitableTrips: Int,
    val lossMakingTrips: Int,
    // Breakdowns
    val expenseBreakdown: List<ExpenseBreakdownItem>,
    // Performers
    val topPerformingVehicle: VehiclePerformer?,
    val lossMakingVehiclesList: List<VehiclePerformer>
)
```

### Period Selection

| Period | Enum | API Value | Description |
|--------|------|-----------|-------------|
| Today | `TODAY` | `today` | Current day |
| This Week | `WEEKLY` | `weekly` | Current week |
| 15 Days | `FIFTEEN_DAYS` | `fifteen_days` | Last 15 days |
| This Month | `MONTHLY` | `monthly` | Current month (default) |
| Quarterly | `QUARTERLY` | `quarterly` | Last 3 months |
| Half Year | `HALF_YEARLY` | `half_yearly` | Last 6 months |
| This Year | `YEARLY` | `yearly` | Current year |
| Custom | `CUSTOM` | `custom` | User-defined date range |

### Navigation from Hub

| Destination | Intent | Screen |
|-------------|--------|--------|
| Vehicle P&L | `NavigateToVehiclePL` | Vehicle Profit/Loss screen |
| Trip P&L | `NavigateToTripPL` | Trip Profit/Loss screen |
| Consolidated P&L | `NavigateToConsolidatedPL` | Consolidated report |
| Cost Analysis | `NavigateToCostAnalysis` | Cost type drill-down |
| Maintenance Cost Report | `NavigateToMaintenanceCostReport` | MC-* analysis |
| Trip Cost Report | `NavigateToTripCostReport` | TC-* analysis |
| Driver Cost Report | `NavigateToDriverCostReport` | DC-* analysis |
| Combined Report | `NavigateToCombinedReport` | Multi-dimension report |

---

## 9. Profitability Thresholds

The system classifies profitability into 5 tiers:

| Status | Profit Margin | Color | Icon | Hex |
|--------|--------------|-------|------|-----|
| **Highly Profitable** | > 20% | Dark Green | 📈 | `#2E7D32` |
| **Profitable** | 10% – 20% | Green | 📈 | `#4CAF50` |
| **Break-even** | 0% – 10% | Amber | 📊 | `#FFC107` |
| **Loss** | 0% to -20% | Red | 📉 | `#F44336` |
| **Severe Loss** | < -20% | Dark Red | 📉 | `#B71C1C` |

**Source:** `ReportsContract.kt → ProfitStatus.fromMargin(margin: Double)`

---

## 10. Master Filter & Sort Reference

### All Filters by Report Type

| Filter | Trip P&L | Vehicle P&L | Consolidated | Cost Analysis | Driver | Customer |
|--------|:--------:|:-----------:|:------------:|:-------------:|:------:|:--------:|
| Date Range | ✅ Required | ✅ Period | ✅ Required | ✅ Required | ✅ | ✅ |
| Period Presets | ❌ | ✅ 5 options | ❌ | ❌ | ❌ | ✅ 4 options |
| Vehicle Select | ❌ | ✅ Multi | ✅ Multi | ❌ | ❌ | ❌ |
| Trip Select | ✅ Multi | ❌ | ❌ | ❌ | ❌ | ❌ |
| Trip Search | ✅ Text | ❌ | ❌ | ❌ | ❌ | ❌ |
| Vehicle Search | ❌ | ✅ Text | ❌ | ❌ | ❌ | ❌ |
| Cost Types | ❌ | ❌ | ✅ Multi | ✅ Multi | ✅ DC-* | ❌ |
| P&L Status | ✅ 3 options | ✅ 3 options | ❌ | ❌ | ❌ | ❌ |
| Group By | ❌ | ❌ | ✅ day/week/month | ❌ | ✅ month | ❌ |
| Driver Select | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| Customer Select | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |

### All Sorts by Report Type

| Sort Option | Trip P&L | Vehicle P&L | Consolidated | Cost Analysis | Driver | Customer |
|-------------|:--------:|:-----------:|:------------:|:-------------:|:------:|:--------:|
| Profit ↑ | ✅ | ✅ | — | — | — | — |
| Profit ↓ | ✅ | ✅ | — | — | — | — |
| Loss ↑ | ✅ | ✅ | — | — | — | — |
| Revenue ↑ | ✅ | ✅ | — | — | — | ✅ |
| Expense ↑ | — | ✅ | — | — | — | — |
| Trips ↑ | — | ✅ | — | — | — | — |
| Date ↑↓ | ✅ | — | — | — | ✅ | ✅ |
| Amount ↑↓ | — | — | — | ✅ | ✅ | — |
| Pending ↑ | — | — | — | — | — | ✅ |

---

## 11. What's Included vs Excluded in P&L

### Included in P&L Calculations

| Item | Trip P&L | Vehicle P&L | Consolidated |
|------|:--------:|:-----------:|:------------:|
| **Revenue:** trip_price (selling_value) | ✅ | ✅ | ✅ |
| **TC-G-001:** Fuel & Energy | ✅ | ✅ | ✅ |
| **TC-G-002:** Road & Travel | ✅ | ✅ | ✅ |
| **TC-G-003:** Cargo Handling | ✅ | ✅ | ✅ |
| **TC-G-004:** Driver Expenses | ✅ | ✅ | ✅ |
| **TC-G-005:** Permits & Legal | ✅ | ✅ | ✅ |
| **TC-G-006:** Misc Expenses | ✅ | ✅ | ✅ |
| **MC-G-001:** Tyres & Wheels | ❌ | ✅ | ✅ |
| **MC-G-002:** Battery & Electrical | ❌ | ✅ | ✅ |
| **MC-G-003:** Engine & Transmission | ❌ | ✅ | ✅ |
| **MC-G-004:** Body & Exterior | ❌ | ✅ | ✅ |
| **MC-G-005:** Servicing & Fluids | ❌ | ✅ | ✅ |
| **MC-G-006:** Accessories & Others | ❌ | ✅ | ✅ |

### Excluded from P&L (with Reasons)

| Item | Reason | Where Tracked Instead |
|------|--------|----------------------|
| **Driver Costs (DC-*)** | Workforce cost, not operational. TC-G-004 already counts trip-level driver expenses in Trip P&L. DC-* sync is for driver's personal ledger only. | `screen-driver` |
| **EMI / Loan Payments** | Capital expenditure, not operational cost. Asset acquisition is separate from operations. | `screen-finance` |
| **Trip Payments** | Payments are on the **revenue** side — they track collection, not expense. P&L uses `trip_price` as revenue regardless of payment status. | `screen-payment` |
| **Depreciation** | Not implemented. Theoretical allocation exists in docs but not in calculations. | Planned (future) |

---

## 12. API Reference

### Report APIs

| Method | Endpoint | Body | Description |
|--------|----------|------|-------------|
| `GET` | `/trips/{tripId}/profit-loss` | — | Single trip P&L |
| `POST` | `/reports/profit-loss/trips` | `MultiTripPLRequest` | Multi-trip P&L |
| `GET` | `/vehicles/{vehicleId}/profit-loss?period=` | — | Single vehicle P&L |
| `POST` | `/reports/profit-loss/vehicles` | `MultiVehiclePLRequest` | Multi-vehicle P&L |
| `GET` | `/reports/profit-loss?period=` | — | Fleet P&L |
| `GET` | `/reports/profit-loss/summary?start_date=&end_date=` | — | P&L summary (Reports Hub) |
| `GET` | `/reports/profit-loss/cost-type/{type}?start_date=&end_date=` | — | Single cost type analysis |
| `POST` | `/reports/profit-loss/cost-types` | `MultiCostTypePLRequest` | Multi cost type analysis |
| `POST` | `/reports/profit-loss/consolidated` | `ConsolidatedPLRequest` | Consolidated P&L |

### Request Bodies

**MultiTripPLRequest:**
```json
{
  "trip_ids": [1, 2, 3],
  "vehicle_id": 5,         // optional alternative to trip_ids
  "start_date": "01-01-2026",
  "end_date": "31-03-2026"
}
```

**MultiVehiclePLRequest:**
```json
{
  "vehicle_ids": [1, 2, 3],
  "start_date": "01-01-2026",
  "end_date": "31-03-2026"
}
```

**MultiCostTypePLRequest:**
```json
{
  "cost_types": ["TC-001-002", "TC-002-001", "MC-001-001"],
  "start_date": "01-01-2026",
  "end_date": "31-03-2026",
  "vehicle_ids": [1, 2]    // optional filter
}
```

**ConsolidatedPLRequest:**
```json
{
  "vehicle_ids": [1, 2],
  "trip_ids": [10, 20],
  "cost_types": ["fuel", "toll"],
  "start_date": "01-01-2026",
  "end_date": "31-03-2026",
  "group_by": "month"
}
```

### Common Response Wrapper

```json
{
  "success": true,
  "message": "Profit loss data retrieved successfully",
  "data": { ... }
}
```

---

## 13. Source Files

### screen-report Module

| File | Purpose |
|------|---------|
| `domain/entity/ProfitLossEntities.kt` | All P&L domain entities |
| `data/model/ProfitLossDto.kt` | All P&L API DTOs (584 lines) |
| `data/model/ProfitLossRequest.kt` | All P&L API request bodies |
| `data/datasource/ReportsRemoteDataSource.kt` | API calls (10 endpoints) |
| `data/mapper/ProfitLossMapper.kt` | DTO → Entity mapping |
| `data/repository/ReportsRepositoryImpl.kt` | Repository implementation |
| `presentation/ReportsContract.kt` | Reports Hub MVI contract |
| `presentation/ReportsViewModel.kt` | Reports Hub ViewModel |
| `presentation/ReportsScreen.kt` | Reports Hub screen |
| `presentation/vehicle/VehiclePLContract.kt` | Vehicle P&L contract (250 lines) |
| `presentation/vehicle/VehiclePLViewModel.kt` | Vehicle P&L ViewModel |
| `presentation/vehicle/VehicleProfitLossScreen.kt` | Vehicle P&L screen |
| `presentation/trip/TripPLContract.kt` | Trip P&L contract |
| `presentation/trip/TripPLViewModel.kt` | Trip P&L ViewModel |
| `presentation/trip/TripProfitLossScreen.kt` | Trip P&L screen |
| `presentation/cost/CostAnalysisContract.kt` | Cost analysis contract |
| `presentation/cost/CostAnalysisViewModel.kt` | Cost analysis ViewModel |
| `presentation/cost/CostAnalysisScreen.kt` | Cost analysis screen |
| `presentation/consolidated/ConsolidatedPLContract.kt` | Consolidated contract |
| `presentation/consolidated/ConsolidatedPLViewModel.kt` | Consolidated ViewModel |
| `presentation/consolidated/ConsolidatedPLScreen.kt` | Consolidated screen |
| `presentation/ReportsFeatureFacade.kt` | Compose entry points |

### screen-customer Module (Customer P&L)

| File | Purpose |
|------|---------|
| `domain/entity/Customer.kt` | CustomerFinancialReport, CustomerStatistics, CustomerPaymentSummary, etc. |
| `presentation/detail/CustomerDetailScreen.kt` | Financials section (Owner/GM only) |

### screen-driver Module (Driver P&L)

| File | Purpose |
|------|---------|
| `ijs-core-lib/.../DriverCostModels.kt` | DriverCostsSummaryDto (earnings/deductions/net) |
| `screen-driver/.../presentation/cost/DriverCostScreen.kt` | Driver cost entry + summary |

### Cost Type Definitions

| File | Module | Purpose |
|------|--------|---------|
| `TripCostTypes` | `ijs-core-lib` | TC-* fallback cost types (22 items) |
| `MaintenanceCostTypes` | `ijs-core-lib` | MC-* fallback cost types (23 items) |
| `DriverCostTypes` | `ijs-core-lib` | DC-* fallback cost types (19 items) |

