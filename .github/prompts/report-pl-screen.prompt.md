# Reports & Profit/Loss Screen Implementation

## Overview

This document provides a comprehensive implementation guide for the **Reports & Profit/Loss** feature in the IndusJS Fleet Management application. It covers all report types, P&L calculation formulas, UI/UX requirements, data flows, and API integrations.

**Target Users:** Owner and General Manager only  
**Last Updated:** February 10, 2026

---

## Table of Contents

1. [Feature Summary](#feature-summary)
2. [Revenue Sources](#revenue-sources)
3. [Expense Categories](#expense-categories)
4. [P&L Calculation Formulas](#pl-calculation-formulas)
5. [Date Range Periods](#date-range-periods)
6. [Cost Allocation Strategies](#cost-allocation-strategies)
7. [Report Types](#report-types)
8. [Profitability Indicators](#profitability-indicators)
9. [Key Metrics](#key-metrics)
10. [Screen Architecture](#screen-architecture)
11. [UI Components & Patterns](#ui-components--patterns)
12. [API Endpoints](#api-endpoints)
13. [Data Models](#data-models)
14. [Implementation Considerations](#implementation-considerations)
15. [File Structure](#file-structure)

---

## Feature Summary

The Reports & P/L feature allows Owner and General Manager to:

- View **Fleet-wide Profit/Loss** consolidated summary
- Analyze **Vehicle-level P&L** with all associated costs
- Track **Trip-level P&L** with direct cost breakdown
- Generate **Combined Vehicle + Trip** comprehensive reports
- Drill-down by **Individual Cost Type** (Maintenance, Trip, Driver)
- Compare performance across **multiple date ranges**
- Export reports to **PDF** for sharing/printing
- Access **Comparative Analysis** (period-over-period)

---

## Revenue Sources

### Primary Revenue

| Source | Table | Field | Description |
|--------|-------|-------|-------------|
| Trip Payment | `trip_payments` | `net_amount` | Actual payments received (after TDS & discounts) |
| Trip Price | `trips` | `paid_trip_price` | Sum of received payments (auto-calculated) |

### Secondary Revenue (Optional)

| Source | Table | Fields | Description |
|--------|-------|--------|-------------|
| Cargo Margin | `trips` | `selling_value - purchase_price` | Goods trading margin (if applicable) |

### Revenue Calculation

```sql
-- Actual Trip Revenue (for completed trips with payments)
Actual Trip Revenue = SUM(trip_payments.net_amount 
                          WHERE payment_status = 'received')

-- Or use the auto-calculated field
Trip Revenue = trips.paid_trip_price

-- Expected Revenue (for budgeting/projection)
Expected Revenue = trips.expected_trip_price
```

**Important:** Always use `paid_trip_price` (sum of received payments) for actual P&L calculations, not `expected_trip_price`.

---

## Expense Categories

### 1. Trip Costs (Table: `trip_costs`)

Direct costs incurred during trip execution.

| Group ID | Group Name | Cost IDs | Description |
|----------|------------|----------|-------------|
| TC-G-001 | Fuel & Energy | TC-001-001 to TC-001-004 | Petrol, Diesel, CNG/LPG, EV Charging |
| TC-G-002 | Toll & Parking | TC-002-001 to TC-002-003 | Toll Charges, Parking Fees, Entry Charges |
| TC-G-003 | Loading & Unloading | TC-003-001 to TC-003-004 | Loading, Unloading, Crane/Forklift, Labor |
| TC-G-004 | Driver Expenses | TC-004-001 to TC-004-003 | Driver Allowance, Food, Accommodation |
| TC-G-005 | Permits & Compliance | TC-005-001 to TC-005-004 | State Permit, National Permit, Special Permit, Chalan/Fine |
| TC-G-006 | Miscellaneous | TC-006-001 to TC-006-005 | Police, RTO, Weighbridge, Commission, Other |

```sql
Total Trip Cost = SUM(trip_costs.amount 
                      WHERE trip_id = X 
                      AND deleted_at IS NULL)
```

### 2. Vehicle Maintenance Costs (Table: `vehicle_maintenance_costs`)

Costs for vehicle upkeep, repairs, and replacements.

| Group ID | Group Name | Cost IDs | Description |
|----------|------------|----------|-------------|
| VMC-G-001 | Regular Maintenance | VMC-001-001 to VMC-001-005 | Oil Change, Filters, Alignment, Servicing |
| VMC-G-002 | Repairs & Replacements | VMC-002-001 to VMC-002-005 | Brakes, Battery, Tyres, Clutch, Suspension |
| VMC-G-003 | Electrical & AC | VMC-003-001 to VMC-003-004 | AC Service, Wiring, Alternator, Lights |
| VMC-G-004 | Body & Exterior | VMC-004-001 to VMC-004-004 | Denting, Windshield, Body Parts, Washing |
| VMC-G-005 | Engine & Transmission | VMC-005-001 to VMC-005-004 | Engine Overhaul, Gearbox, Radiator, Fuel System |
| VMC-G-006 | Miscellaneous | VMC-006-001 to VMC-006-003 | Accessories, GPS Device, Other |
| VMC-G-007 | Wheels & Tyres | VMC-007-001 to VMC-007-003 | Tyre Rotation, Alignment, Replacement |

```sql
Total Maintenance Cost = SUM(vehicle_maintenance_costs.amount 
                             WHERE vehicle_id = X 
                             AND date BETWEEN start_date AND end_date
                             AND deleted_at IS NULL)
```

### 3. Vehicle EMI/Finance Costs (Table: `vehicle_loan_payments`)

Monthly EMI payments for financed vehicles.

| Cost Component | Field | Description |
|----------------|-------|-------------|
| EMI Payment | `amount` | Total monthly payment |
| Late Fees | `late_fee` | Penalty for late payment |
| Interest | `interest_amount` | Interest portion (for analytics) |
| Principal | `principal_amount` | Principal portion (for analytics) |

```sql
Total EMI Cost = SUM(vehicle_loan_payments.amount + COALESCE(late_fee, 0)
                     WHERE vehicle_id = X 
                     AND payment_status = 'paid'
                     AND payment_date BETWEEN start_date AND end_date
                     AND deleted_at IS NULL)
```

### 4. Document/Paper Costs (⚠️ IMPLEMENTATION REQUIRED)

Costs for vehicle documentation and compliance renewals.

| Document Type | Tag | Typical Frequency | Example Cost Range |
|---------------|-----|-------------------|-------------------|
| Insurance | INS | Yearly | ₹15,000 - ₹50,000 |
| PUC Certificate | PUC | 6 months | ₹100 - ₹500 |
| Fitness Certificate | FC | 2 years | ₹1,000 - ₹3,000 |
| Road Tax | RT | Yearly/Lifetime | ₹5,000 - ₹50,000 |
| Permits | PERMIT | Yearly | ₹5,000 - ₹25,000 |
| Registration | RC | 15 years | One-time |

> **⚠️ DATA GAP:** Document costs are NOT currently tracked in the system.
> See [Implementation Considerations](#implementation-considerations) for proposed solution.

### 5. Driver Costs (Table: `driver_costs`)

Driver-related expenses including salaries, bonuses, and deductions.

| Group ID | Group Name | Is Deduction | Cost Types |
|----------|------------|--------------|------------|
| DC-G-001 | Salary & Wages | No | Monthly Salary, Daily Wages, Overtime, Holiday Pay |
| DC-G-002 | Incentives & Bonuses | No | Trip Bonus, Performance, Fuel Saving, On-Time, Safety |
| DC-G-003 | Deductions | Yes | Advance Recovery, Damage, Fine, Loan EMI, Insurance |
| DC-G-004 | Other | No | Training, Uniform, Medical, License Renewal, Other |

```sql
Net Driver Cost = (Salary + Incentives + Other) - Deductions

WHERE:
  Salary     = SUM(amount WHERE group_id = 'DC-G-001' AND NOT is_deduction)
  Incentives = SUM(amount WHERE group_id = 'DC-G-002' AND NOT is_deduction)
  Other      = SUM(amount WHERE group_id = 'DC-G-004' AND NOT is_deduction)
  Deductions = SUM(amount WHERE group_id = 'DC-G-003' AND is_deduction)
```

---

## P&L Calculation Formulas

### Trip-Level P&L

Direct profitability of a single trip.

```
┌──────────────────────────────────────────────────────────────────┐
│                        TRIP PROFIT/LOSS                          │
├──────────────────────────────────────────────────────────────────┤
│ REVENUE                                                          │
│   + Trip Price Received (paid_trip_price)                        │
│   + Cargo Margin (selling_value - purchase_price)  [if applicable]│
│   ─────────────────────────────────────────────────              │
│   = TOTAL TRIP REVENUE                                           │
├──────────────────────────────────────────────────────────────────┤
│ DIRECT EXPENSES                                                  │
│   - Fuel Cost (TC-G-001)                                         │
│   - Toll & Parking (TC-G-002)                                    │
│   - Loading/Unloading (TC-G-003)                                 │
│   - Driver On-Trip Expenses (TC-G-004)                           │
│   - Permits & Compliance (TC-G-005)                              │
│   - Miscellaneous (TC-G-006)                                     │
│   - Trip-Linked Driver Costs (driver_costs WHERE trip_id = X)    │
│   ─────────────────────────────────────────────────              │
│   = TOTAL TRIP EXPENSES                                          │
├──────────────────────────────────────────────────────────────────┤
│ TRIP PROFIT/LOSS = TOTAL REVENUE - TOTAL EXPENSES                │
│ PROFIT MARGIN = (PROFIT / REVENUE) × 100                         │
│ PROFIT PER KM = PROFIT / actual_distance                         │
└──────────────────────────────────────────────────────────────────┘
```

**Driver Cost Attribution for Trip P&L:**
- Include ONLY driver costs that have `trip_id` linked (e.g., Trip Bonus, Driver Allowance)
- Do NOT include general salary or monthly costs

### Vehicle-Level P&L

Complete profitability of a vehicle over a period.

```
┌──────────────────────────────────────────────────────────────────┐
│                      VEHICLE PROFIT/LOSS                         │
├──────────────────────────────────────────────────────────────────┤
│ REVENUE                                                          │
│   + Sum of all Trip Revenues (WHERE vehicle_id = X)              │
│   ─────────────────────────────────────────────────              │
│   = TOTAL VEHICLE REVENUE                                        │
├──────────────────────────────────────────────────────────────────┤
│ OPERATING EXPENSES (Variable)                                    │
│   - Sum of all Trip Costs (fuel, toll, loading, etc.)            │
│   ─────────────────────────────────────────────────              │
│   = TOTAL OPERATING EXPENSES                                     │
├──────────────────────────────────────────────────────────────────┤
│ FIXED/OWNERSHIP EXPENSES (Time-Allocated)                        │
│   - Maintenance Costs (VMC-G-001 to VMC-G-007)                   │
│   - EMI Payments (principal + interest + late fees)              │
│   - Document Costs (Insurance, Permit, PUC, Road Tax, Fitness)   │
│   ─────────────────────────────────────────────────              │
│   = TOTAL OWNERSHIP EXPENSES                                     │
├──────────────────────────────────────────────────────────────────┤
│ TOTAL EXPENSES = OPERATING + OWNERSHIP                           │
│ VEHICLE PROFIT/LOSS = REVENUE - TOTAL EXPENSES                   │
│ PROFIT MARGIN = (PROFIT / REVENUE) × 100                         │
│ PROFIT PER KM = PROFIT / TOTAL DISTANCE                          │
│ PROFIT PER TRIP = PROFIT / TOTAL TRIPS COMPLETED                 │
└──────────────────────────────────────────────────────────────────┘
```

### Fleet-Level P&L (Consolidated)

Overall fleet profitability including all costs.

```
┌──────────────────────────────────────────────────────────────────┐
│                       FLEET PROFIT/LOSS                          │
├──────────────────────────────────────────────────────────────────┤
│ TOTAL FLEET REVENUE                                              │
│   + Sum of all Vehicle Revenues                                  │
├──────────────────────────────────────────────────────────────────┤
│ TOTAL FLEET EXPENSES                                             │
│   - Sum of all Trip Costs (all vehicles)                         │
│   - Sum of all Maintenance Costs (all vehicles)                  │
│   - Sum of all EMI Payments (financed vehicles)                  │
│   - Sum of all Document Costs (all vehicles)                     │
│   - Sum of ALL Driver Costs (including salaries)                 │
├──────────────────────────────────────────────────────────────────┤
│ FLEET PROFIT/LOSS = TOTAL REVENUE - TOTAL EXPENSES               │
│ AVERAGE MARGIN = (TOTAL PROFIT / TOTAL REVENUE) × 100            │
│ PROFITABLE VEHICLES = Count WHERE vehicle_profit > 0             │
│ LOSS-MAKING VEHICLES = Count WHERE vehicle_profit < 0            │
│ BREAK-EVEN VEHICLES = Count WHERE vehicle_profit ≈ 0             │
│ FLEET UTILIZATION = Active Vehicles / Total Vehicles × 100       │
└──────────────────────────────────────────────────────────────────┘
```

**Driver Cost Attribution for Fleet P&L:**
- Include ALL driver costs (salaries, incentives, deductions, other)
- This gives the true fleet-wide profitability picture

---

## Date Range Periods

| Period | Calculation | Use Case |
|--------|-------------|----------|
| **Daily** | Current date only | Quick daily check |
| **Weekly** | Last 7 days (rolling) | Weekly review |
| **15 Days** | Last 15 days (rolling) | Bi-weekly analysis |
| **Monthly** | Current calendar month | Monthly reporting |
| **Quarterly** | Current quarter (Q1: Apr-Jun, Q2: Jul-Sep, Q3: Oct-Dec, Q4: Jan-Mar) | Quarterly review |
| **Half Yearly** | Last 6 months (rolling) | Semi-annual analysis |
| **Yearly** | Financial year (Apr 1 - Mar 31) | Annual reporting |
| **Custom** | User-defined start/end | Specific period analysis |

### Financial Period Calculation (India)

```kotlin
fun getFinancialYear(date: LocalDate): String {
    val year = if (date.monthNumber >= 4) date.year else date.year - 1
    return "${year}-${(year + 1) % 100}"  // e.g., "2025-26"
}

fun getQuarter(date: LocalDate): String {
    val fy = getFinancialYear(date)
    return when (date.monthNumber) {
        in 4..6 -> "Q1-$fy"   // Apr-Jun
        in 7..9 -> "Q2-$fy"   // Jul-Sep
        in 10..12 -> "Q3-$fy" // Oct-Dec
        else -> "Q4-$fy"      // Jan-Mar
    }
}

fun getPeriodDates(period: ReportPeriod): Pair<LocalDate, LocalDate> {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return when (period) {
        ReportPeriod.DAILY -> today to today
        ReportPeriod.WEEKLY -> today.minus(6, DateTimeUnit.DAY) to today
        ReportPeriod.FIFTEEN_DAYS -> today.minus(14, DateTimeUnit.DAY) to today
        ReportPeriod.MONTHLY -> {
            val start = LocalDate(today.year, today.monthNumber, 1)
            val end = start.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
            start to minOf(end, today)
        }
        ReportPeriod.QUARTERLY -> {
            val quarterStart = when (today.monthNumber) {
                in 4..6 -> LocalDate(today.year, 4, 1)
                in 7..9 -> LocalDate(today.year, 7, 1)
                in 10..12 -> LocalDate(today.year, 10, 1)
                else -> LocalDate(today.year - 1, 1, 1)
            }
            quarterStart to today
        }
        ReportPeriod.HALF_YEARLY -> today.minus(6, DateTimeUnit.MONTH) to today
        ReportPeriod.YEARLY -> {
            val fyStart = if (today.monthNumber >= 4) 
                LocalDate(today.year, 4, 1) 
            else 
                LocalDate(today.year - 1, 4, 1)
            fyStart to today
        }
        is ReportPeriod.Custom -> period.startDate to period.endDate
    }
}
```

---

## Cost Allocation Strategies

### Time-Based Allocation for Fixed Costs (RECOMMENDED)

EMI, Insurance, Road Tax, and other fixed costs should be **time-allocated** proportionally across reporting periods for accuracy.

```kotlin
/**
 * Time-based allocation for fixed costs
 * Provides accurate cost distribution across periods
 */
object CostAllocation {
    
    /**
     * Calculate allocated cost for a specific period
     * @param annualCost Total annual cost
     * @param periodDays Number of days in the reporting period
     * @return Allocated cost for the period
     */
    fun allocateAnnualCost(annualCost: Double, periodDays: Int): Double {
        val dailyRate = annualCost / 365.0
        return dailyRate * periodDays
    }
    
    /**
     * Calculate allocated EMI cost for a period
     * @param monthlyEmi Monthly EMI amount
     * @param periodStartDate Start of reporting period
     * @param periodEndDate End of reporting period
     * @return Allocated EMI cost
     */
    fun allocateEmiCost(
        monthlyEmi: Double,
        periodStartDate: LocalDate,
        periodEndDate: LocalDate
    ): Double {
        val dailyRate = monthlyEmi / 30.0  // Approximate
        val days = periodStartDate.daysUntil(periodEndDate) + 1
        return dailyRate * days
    }
}
```

### Allocation Examples

| Cost Type | Annual Amount | Period | Allocation |
|-----------|---------------|--------|------------|
| Insurance | ₹60,000/year | Yearly | ₹60,000 |
| Insurance | ₹60,000/year | Half-Yearly | ₹30,000 |
| Insurance | ₹60,000/year | Quarterly | ₹15,000 |
| Insurance | ₹60,000/year | Monthly | ₹5,000 |
| Insurance | ₹60,000/year | Weekly | ₹1,154 |
| Insurance | ₹60,000/year | Daily | ₹164 |
| EMI | ₹42,000/month | Monthly | ₹42,000 |
| EMI | ₹42,000/month | Weekly | ₹9,692 |
| EMI | ₹42,000/month | Daily | ₹1,400 |

### Usage-Based Allocation (for Variable Costs)

Fuel, maintenance, and trip costs are **directly allocated** to the period in which they occurred - no calculation needed.

```sql
-- Variable costs are filtered by date, not allocated
SELECT SUM(amount) 
FROM trip_costs 
WHERE date BETWEEN start_date AND end_date
AND vehicle_id = X
```

### Driver Cost Allocation Rules

| P&L Level | Driver Costs Included | Rationale |
|-----------|----------------------|-----------|
| **Trip P&L** | Only trip-linked costs (`trip_id IS NOT NULL`) | Direct cost attribution |
| **Vehicle P&L** | Trip-linked costs for vehicle's trips | Costs associated with that vehicle's operations |
| **Fleet P&L** | ALL driver costs (including salaries) | True total fleet profitability |

```sql
-- For Trip P&L: Only trip-specific driver costs
SELECT SUM(CASE WHEN is_deduction THEN -amount ELSE amount END)
FROM driver_costs
WHERE trip_id = :tripId
AND deleted_at IS NULL

-- For Fleet P&L: All driver costs
SELECT SUM(CASE WHEN is_deduction THEN -amount ELSE amount END)
FROM driver_costs
WHERE owner_id = :ownerId
AND date BETWEEN :startDate AND :endDate
AND deleted_at IS NULL
```

---

## Report Types

### 1. Vehicle Base Report

**Purpose:** Track fixed vehicle ownership costs

**Includes:**
- EMI Payments (from `vehicle_loan_payments`)
- Maintenance Costs (from `vehicle_maintenance_costs`)
- Document/Paper Costs (from `document_costs` - when implemented)

**Features:**
| Feature | Description |
|---------|-------------|
| Multi-vehicle selection | Bottom sheet with LazyColumn, multi-select |
| Pre-selected vehicles on top | Previously selected vehicles appear first when reopening |
| Search | Search by registration number |
| Date range filter | All period options |
| Cost breakdown | Detailed breakdown by cost type |
| Sorting | By total cost, vehicle name, date |
| PDF export | Generate downloadable report |

**Formula:**
```
Vehicle Fixed Cost = EMI (time-allocated) + Maintenance + Document Costs
```

### 2. Trip Base Report

**Purpose:** Track operating costs and trip profitability

**Includes:**
- All trip costs by group (Fuel, Toll, Loading, Driver, Permits, Misc)
- Trip-linked driver allowances
- Trip revenue vs expenses

**Features:**
| Feature | Description |
|---------|-------------|
| Date range filter | All period options |
| Search | Search by trip ID, route, customer |
| Cost breakdown | By category (fuel, toll, etc.) |
| Sorting | By profit, revenue, date, distance |
| PDF export | Generate downloadable report |

**Formula:**
```
Trip Operating Cost = Fuel + Toll + Loading + Driver + Permit + Misc
Trip Profit = Trip Revenue - Trip Operating Cost
```

### 3. Combined Vehicle + Trip Report

**Purpose:** Complete profitability analysis

**Includes:**
- All Trip Costs (variable operating costs)
- All Vehicle Fixed Costs (maintenance, EMI, documents)

**Features:**
| Feature | Description |
|---------|-------------|
| Vehicle + Trip search | Combined search functionality |
| Date range filter | All period options |
| Comprehensive breakdown | Variable + Fixed costs |
| Sorting | Multiple sort options |
| PDF export | Generate downloadable report |

**Formula:**
```
Total Revenue = SUM(paid_trip_price) for period
Total Expenses = Trip Costs + Maintenance + EMI + Documents
Net Profit = Total Revenue - Total Expenses
```

### 4. Individual Maintenance Cost Type Report

**Purpose:** Drill-down analysis of specific maintenance expenses

**Example Use Cases:**
- "How much did I spend on Tyres across all vehicles?"
- "Which vehicle has the highest battery replacement cost?"

**Features:**
| Feature | Description |
|---------|-------------|
| Cost type selection | Single or multiple cost types |
| Date range filter | All period options |
| Vehicle grouping | Costs grouped by vehicle |
| Sorting | By amount, vehicle, count |
| PDF export | Generate downloadable report |

```sql
SELECT 
    v.registration_number,
    vmc.cost_id,
    vmc.cost_label,
    SUM(vmc.amount) as total,
    COUNT(*) as count
FROM vehicle_maintenance_costs vmc
JOIN vehicles v ON vmc.vehicle_id = v.id
WHERE vmc.cost_id IN (:costIds)  -- e.g., 'VMC-002-003' for Tyres
AND vmc.date BETWEEN :startDate AND :endDate
AND vmc.deleted_at IS NULL
GROUP BY v.id, vmc.cost_id
ORDER BY total DESC
```

### 5. Individual Trip Cost Type Report

**Purpose:** Drill-down analysis of specific trip expenses

**Example Use Cases:**
- "How much did I spend on Diesel across all trips?"
- "What's my total toll expense this month?"

**Features:**
| Feature | Description |
|---------|-------------|
| Cost type selection | Single or multiple cost types |
| Date range filter | All period options |
| Trip/Vehicle grouping | Costs grouped by trip or vehicle |
| Fuel details | Quantity, rate, efficiency (for fuel types) |
| Sorting | By amount, date, vehicle |
| PDF export | Generate downloadable report |

```sql
SELECT 
    t.id as trip_id,
    v.registration_number,
    tc.cost_id,
    tc.cost_label,
    SUM(tc.amount) as total,
    SUM(tc.fuel_quantity) as liters,  -- For fuel costs
    COUNT(*) as count
FROM trip_costs tc
JOIN trips t ON tc.trip_id = t.id
JOIN vehicles v ON tc.vehicle_id = v.id
WHERE tc.cost_id IN (:costIds)  -- e.g., 'TC-001-002' for Diesel
AND tc.date BETWEEN :startDate AND :endDate
AND tc.deleted_at IS NULL
GROUP BY t.id, tc.cost_id
ORDER BY total DESC
```

### 6. Driver Cost Type Report

**Purpose:** Analyze driver-related expenses

**Example Use Cases:**
- "What's my total salary expense this month?"
- "Which driver has the highest incentive payouts?"

**Features:**
| Feature | Description |
|---------|-------------|
| Cost group/type filter | Filter by group or specific type |
| Date range filter | All period options |
| Driver grouping | Costs grouped by driver |
| Earnings vs Deductions | Separate columns |
| Sorting | By net amount, driver, date |
| PDF export | Generate downloadable report |

```sql
SELECT 
    d.id as driver_id,
    d.first_name || ' ' || d.last_name as driver_name,
    SUM(CASE WHEN dc.group_id = 'DC-G-001' AND NOT dc.is_deduction 
        THEN dc.amount ELSE 0 END) as salary,
    SUM(CASE WHEN dc.group_id = 'DC-G-002' AND NOT dc.is_deduction 
        THEN dc.amount ELSE 0 END) as incentives,
    SUM(CASE WHEN dc.group_id = 'DC-G-003' AND dc.is_deduction 
        THEN dc.amount ELSE 0 END) as deductions,
    SUM(CASE WHEN dc.group_id = 'DC-G-004' AND NOT dc.is_deduction 
        THEN dc.amount ELSE 0 END) as other,
    SUM(CASE WHEN NOT dc.is_deduction THEN dc.amount ELSE -dc.amount END) as net
FROM driver_costs dc
JOIN drivers d ON dc.driver_id = d.id
WHERE dc.date BETWEEN :startDate AND :endDate
AND dc.deleted_at IS NULL
GROUP BY d.id
ORDER BY net DESC
```

---

## Profitability Indicators

### Status Thresholds

| Status | Profit Margin | Color Code | Icon | Action |
|--------|---------------|------------|------|--------|
| 🟢 Highly Profitable | > 20% | `#2E7D32` (Dark Green) | 📈 | Maintain strategy |
| 🟢 Profitable | 10% - 20% | `#4CAF50` (Green) | 📈 | Optimize costs |
| 🟡 Break-even | 0% - 10% | `#FFC107` (Amber) | 📊 | Review expenses |
| 🔴 Loss | 0% to -20% | `#F44336` (Red) | 📉 | Immediate review |
| 🔴 Severe Loss | < -20% | `#B71C1C` (Dark Red) | 📉 | Critical intervention |

### Implementation

```kotlin
enum class ProfitStatus(
    val label: String,
    val colorHex: Long,
    val icon: String
) {
    HIGHLY_PROFITABLE("Highly Profitable", 0xFF2E7D32, "📈"),
    PROFITABLE("Profitable", 0xFF4CAF50, "📈"),
    BREAK_EVEN("Break-even", 0xFFFFC107, "📊"),
    LOSS("Loss", 0xFFF44336, "📉"),
    SEVERE_LOSS("Severe Loss", 0xFFB71C1C, "📉");
    
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

@Composable
fun getProfitStatusColor(margin: Double): Color {
    return Color(ProfitStatus.fromMargin(margin).colorHex)
}
```

---

## Key Metrics

### Financial Metrics

| Metric | Formula | Unit | Description |
|--------|---------|------|-------------|
| Gross Profit | Revenue - Trip Costs | ₹ | Before fixed costs |
| Net Profit | Revenue - All Costs | ₹ | After all costs |
| Profit Margin | (Net Profit / Revenue) × 100 | % | Profitability ratio |
| ROI | (Net Profit / Investment) × 100 | % | Return on investment |

### Efficiency Metrics

| Metric | Formula | Unit | Description |
|--------|---------|------|-------------|
| Cost per KM | Total Costs / Distance | ₹/km | Operating efficiency |
| Revenue per KM | Revenue / Distance | ₹/km | Earning efficiency |
| Profit per KM | Net Profit / Distance | ₹/km | Net earning per km |
| Cost per Trip | Total Costs / Trip Count | ₹/trip | Per-trip cost |
| Fuel Efficiency | Distance / Fuel Quantity | km/L | Fuel economy |
| Fuel Cost per KM | Fuel Cost / Distance | ₹/km | Fuel spending |

### Ratio Metrics

| Metric | Formula | Description |
|--------|---------|-------------|
| Maintenance Ratio | (Maintenance / Revenue) × 100 | % of revenue on maintenance |
| EMI Burden | (EMI / Revenue) × 100 | % of revenue on EMI |
| Fuel Ratio | (Fuel Cost / Total Costs) × 100 | Fuel as % of total costs |
| Driver Cost Ratio | (Driver Costs / Revenue) × 100 | Driver expense burden |
| Fixed Cost Ratio | (Fixed Costs / Total Costs) × 100 | Fixed vs variable split |

---

## Screen Architecture

### Navigation Structure

```
FleetRoute.Reports (Reports Dashboard)
├── FleetRoute.VehicleProfitLoss (Vehicle P&L)
├── FleetRoute.TripProfitLoss (Trip P&L)
├── FleetRoute.ConsolidatedPL (Fleet P&L)
├── FleetRoute.VehicleBaseReport (Vehicle Costs)
├── FleetRoute.TripBaseReport (Trip Costs)
├── FleetRoute.CombinedReport (Vehicle + Trip)
├── FleetRoute.MaintenanceCostTypeReport (Maintenance Drill-down)
├── FleetRoute.TripCostTypeReport (Trip Cost Drill-down)
└── FleetRoute.DriverCostReport (Driver Costs)
```

### Reports Dashboard Layout

```
┌─────────────────────────────────────────────────────────────────┐
│  ← Reports & Analytics                              [PDF] [⋮]  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ 📊 Fleet Overview (Current Month)                       │   │
│  │    Revenue: ₹5,00,000   Expenses: ₹3,80,000             │   │
│  │    Profit: ₹1,20,000    Margin: 24% 🟢                  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ── Profit & Loss Reports ─────────────────────────────────    │
│                                                                 │
│  ┌─────────────────────┐  ┌─────────────────────┐              │
│  │ 🚛 Vehicle P&L      │  │ 🛣️ Trip P&L         │              │
│  │ Per-vehicle profit  │  │ Per-trip profit     │              │
│  └─────────────────────┘  └─────────────────────┘              │
│                                                                 │
│  ┌─────────────────────┐  ┌─────────────────────┐              │
│  │ 📈 Fleet P&L        │  │ 🔀 Combined Report  │              │
│  │ Consolidated view   │  │ Vehicle + Trip      │              │
│  └─────────────────────┘  └─────────────────────┘              │
│                                                                 │
│  ── Cost Analysis ─────────────────────────────────────────    │
│                                                                 │
│  ┌─────────────────────┐  ┌─────────────────────┐              │
│  │ 🔧 Maintenance      │  │ ⛽ Trip Costs       │              │
│  │ By cost type        │  │ By cost type        │              │
│  └─────────────────────┘  └─────────────────────┘              │
│                                                                 │
│  ┌─────────────────────┐                                       │
│  │ 👨‍✈️ Driver Costs     │                                       │
│  │ Salary & expenses   │                                       │
│  └─────────────────────┘                                       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Vehicle Selection Bottom Sheet

```
┌─────────────────────────────────────────────────────────────────┐
│  Select Vehicles                                    ✕ Close    │
├─────────────────────────────────────────────────────────────────┤
│  🔍 Search by registration number...                           │
├─────────────────────────────────────────────────────────────────┤
│  [Select All]  [Clear All]                    3 selected       │
├─────────────────────────────────────────────────────────────────┤
│  ── Previously Selected ───────────────────────────────────    │
│  ☑️ MH12AB1234  │  Tata Prima  │  Active                       │
│  ☑️ MH12CD5678  │  Ashok Leyland  │  Active                    │
│  ☑️ MH12EF9012  │  Mahindra Blazo  │  Active                   │
├─────────────────────────────────────────────────────────────────┤
│  ── All Vehicles ──────────────────────────────────────────    │
│  ☐ MH12GH3456  │  Eicher Pro  │  Active                        │
│  ☐ MH12IJ7890  │  BharatBenz  │  Maintenance                   │
│  ☐ MH12KL2345  │  Tata Signa  │  Active                        │
│  ...                                                            │
├─────────────────────────────────────────────────────────────────┤
│  [Cancel]                                          [Apply]     │
└─────────────────────────────────────────────────────────────────┘
```

### Date Range Selector

```
┌─────────────────────────────────────────────────────────────────┐
│  Select Period                                                  │
├─────────────────────────────────────────────────────────────────┤
│  ○ Daily          ○ Weekly         ○ 15 Days                   │
│  ● Monthly        ○ Quarterly      ○ Half Yearly               │
│  ○ Yearly         ○ Custom                                     │
├─────────────────────────────────────────────────────────────────┤
│  (If Custom selected)                                           │
│  From: [📅 01-02-2026]    To: [📅 10-02-2026]                  │
├─────────────────────────────────────────────────────────────────┤
│  [Cancel]                                          [Apply]     │
└─────────────────────────────────────────────────────────────────┘
```

---

## UI Components & Patterns

### Required Core Components

| Component | Usage |
|-----------|-------|
| `ScreenContent` | Wrapper for loading/error/content states |
| `FleetCard` | Report cards with consistent styling |
| `FleetChip` | Period selection chips |
| `FleetDateField` | Date range inputs |
| `LoadingContent` | Loading spinner |
| `ErrorContent` | Error display with retry |
| `EmptyContent` | Empty state |

### Report-Specific Components

| Component | Description |
|-----------|-------------|
| `ProfitLossCard` | Summary card with revenue, expenses, profit, margin |
| `CostBreakdownChart` | Pie/bar chart for cost distribution |
| `VehicleSelectionBottomSheet` | Multi-select vehicle picker |
| `DateRangeSelector` | Period selection with custom range |
| `SortOptionsMenu` | Dropdown for sorting options |
| `ProfitStatusBadge` | Colored badge with profit status |
| `MetricsGrid` | Grid display for KPIs |
| `CostTypeFilterChips` | Filter by cost type groups |

### Component Examples

```kotlin
@Composable
fun ProfitLossCard(
    title: String,
    revenue: Double,
    expenses: Double,
    modifier: Modifier = Modifier
) {
    val profit = revenue - expenses
    val margin = if (revenue > 0) (profit / revenue) * 100 else 0.0
    val status = ProfitStatus.fromMargin(margin)
    
    FleetCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                MetricItem(label = "Revenue", value = "₹${revenue.formatAmount()}")
                MetricItem(label = "Expenses", value = "₹${expenses.formatAmount()}")
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Net Profit", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "₹${profit.formatAmount()}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = getProfitStatusColor(margin)
                    )
                }
                ProfitStatusBadge(status = status, margin = margin)
            }
        }
    }
}

@Composable
fun ProfitStatusBadge(status: ProfitStatus, margin: Double) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(status.colorHex).copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(status.icon, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "${margin.roundToOneDecimal()}%",
                style = MaterialTheme.typography.labelMedium,
                color = Color(status.colorHex)
            )
        }
    }
}
```

---

## API Endpoints

### Report Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/reports/types` | GET | Get available report types |
| `/reports/periods` | GET | Get available period options |
| `/reports/profit-loss` | GET | Fleet P&L with period |
| `/reports/profit-loss/summary` | GET | P&L summary for dashboard |
| `/vehicles/{id}/profit-loss` | GET | Single vehicle P&L |
| `/reports/profit-loss/vehicles` | POST | Multi-vehicle P&L |
| `/trips/{id}/profit-loss` | GET | Single trip P&L |
| `/reports/profit-loss/trips` | POST | Multi-trip P&L |
| `/reports/profit-loss/cost-type/{id}` | GET | Single cost type analysis |
| `/reports/profit-loss/cost-types` | POST | Multi cost type analysis |
| `/reports/profit-loss/consolidated` | POST | Combined comprehensive report |
| `/reports/profit-loss/drivers` | POST | Driver cost analysis |

### Request Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `vehicle_ids` | Array<Int> | List of vehicle IDs |
| `trip_ids` | Array<Int> | List of trip IDs |
| `cost_ids` | Array<String> | List of cost type IDs |
| `driver_ids` | Array<Int> | List of driver IDs |
| `start_date` | String | Start date (DD-MM-YYYY) |
| `end_date` | String | End date (DD-MM-YYYY) |
| `period` | String | daily/weekly/monthly/quarterly/yearly |
| `include_comparison` | Boolean | Include previous period comparison |
| `include_breakdown` | Boolean | Include detailed cost breakdown |

### Sample Request/Response

**Request: Multi-Vehicle P&L**
```http
POST /api/v2/reports/profit-loss/vehicles
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "vehicle_ids": [1, 2, 3],
    "start_date": "01-01-2026",
    "end_date": "31-01-2026",
    "include_comparison": true,
    "include_breakdown": true
}
```

**Response:**
```json
{
    "success": true,
    "message": "Report generated successfully",
    "data": {
        "period": {
            "start_date": "01-01-2026",
            "end_date": "31-01-2026",
            "period_type": "custom"
        },
        "summary": {
            "total_revenue": 500000,
            "total_expenses": 380000,
            "net_profit": 120000,
            "profit_margin": 24.0,
            "profit_status": "highly_profitable",
            "total_trips": 25,
            "total_distance_km": 12500
        },
        "vehicles": [
            {
                "vehicle_id": 1,
                "registration_number": "MH12AB1234",
                "revenue": 200000,
                "trip_costs": 85000,
                "maintenance_costs": 15000,
                "emi_costs": 42000,
                "document_costs": 5000,
                "total_expenses": 147000,
                "profit": 53000,
                "margin": 26.5,
                "status": "highly_profitable"
            }
        ],
        "expense_breakdown": {
            "trip_costs": {
                "fuel": 150000,
                "toll": 30000,
                "loading_unloading": 25000,
                "driver_expenses": 15000,
                "permits": 10000,
                "miscellaneous": 5000,
                "total": 235000
            },
            "maintenance": 45000,
            "emi": 84000,
            "documents": 16000,
            "total": 380000
        },
        "comparison": {
            "previous_period": {
                "start_date": "01-12-2025",
                "end_date": "31-12-2025"
            },
            "revenue_change": 15000,
            "revenue_change_percent": 3.1,
            "expense_change": -5000,
            "expense_change_percent": -1.3,
            "profit_change": 20000,
            "profit_change_percent": 20.0
        }
    }
}
```

---

## Data Models

### Domain Entities

```kotlin
// Report period enum
enum class ReportPeriod {
    DAILY, WEEKLY, FIFTEEN_DAYS, MONTHLY, QUARTERLY, 
    HALF_YEARLY, YEARLY;
    
    data class Custom(val startDate: LocalDate, val endDate: LocalDate) : ReportPeriod()
}

// P&L Summary
data class ProfitLossSummary(
    val totalRevenue: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val profitMargin: Double,
    val profitStatus: ProfitStatus,
    val totalTrips: Int,
    val totalDistanceKm: Double,
    val profitPerKm: Double,
    val profitPerTrip: Double
)

// Vehicle P&L
data class VehicleProfitLoss(
    val vehicleId: Int,
    val registrationNumber: String,
    val revenue: Double,
    val tripCosts: Double,
    val maintenanceCosts: Double,
    val emiCosts: Double,
    val documentCosts: Double,
    val totalExpenses: Double,
    val profit: Double,
    val margin: Double,
    val status: ProfitStatus,
    val tripCount: Int,
    val distanceKm: Double
)

// Trip P&L
data class TripProfitLoss(
    val tripId: Int,
    val vehicleId: Int,
    val vehicleRegistration: String,
    val driverId: Int,
    val driverName: String,
    val tripDate: String,
    val route: String,
    val distance: Double,
    val tripPrice: Double,
    val totalCost: Double,
    val profit: Double,
    val margin: Double,
    val status: ProfitStatus,
    val costBreakdown: List<CostBreakdownItem>
)

// Cost breakdown item
data class CostBreakdownItem(
    val costId: String,
    val costLabel: String,
    val groupId: String,
    val amount: Double,
    val count: Int,
    val percentage: Double
)

// Expense breakdown
data class ExpenseBreakdown(
    val tripCosts: TripCostBreakdown,
    val maintenanceCosts: Double,
    val emiCosts: Double,
    val documentCosts: Double,
    val driverCosts: Double,
    val total: Double
)

data class TripCostBreakdown(
    val fuel: Double,
    val toll: Double,
    val loadingUnloading: Double,
    val driverExpenses: Double,
    val permits: Double,
    val miscellaneous: Double,
    val total: Double
)

// Period comparison
data class PeriodComparison(
    val previousPeriod: DateRange,
    val revenueChange: Double,
    val revenueChangePercent: Double,
    val expenseChange: Double,
    val expenseChangePercent: Double,
    val profitChange: Double,
    val profitChangePercent: Double
)

// Vehicle selection state
data class VehicleSelectionState(
    val selectedVehicleIds: Set<Int> = emptySet(),
    val previouslySelectedIds: Set<Int> = emptySet(),
    val searchQuery: String = "",
    val allVehicles: List<VehicleListItem> = emptyList()
) {
    val filteredVehicles: List<VehicleListItem>
        get() = allVehicles
            .filter { it.registrationNumber.contains(searchQuery, ignoreCase = true) }
            .sortedByDescending { it.id in previouslySelectedIds }
}
```

### DTOs

```kotlin
@Serializable
data class ProfitLossReportDto(
    @SerialName("period")
    val period: PeriodDto,
    @SerialName("summary")
    val summary: ProfitLossSummaryDto,
    @SerialName("vehicles")
    val vehicles: List<VehicleProfitLossDto>? = null,
    @SerialName("trips")
    val trips: List<TripProfitLossDto>? = null,
    @SerialName("expense_breakdown")
    val expenseBreakdown: ExpenseBreakdownDto? = null,
    @SerialName("comparison")
    val comparison: PeriodComparisonDto? = null
)

@Serializable
data class ProfitLossSummaryDto(
    @SerialName("total_revenue")
    val totalRevenue: Double,
    @SerialName("total_expenses")
    val totalExpenses: Double,
    @SerialName("net_profit")
    val netProfit: Double,
    @SerialName("profit_margin")
    val profitMargin: Double,
    @SerialName("profit_status")
    val profitStatus: String,
    @SerialName("total_trips")
    val totalTrips: Int,
    @SerialName("total_distance_km")
    val totalDistanceKm: Double
)

@Serializable
data class VehicleProfitLossDto(
    @SerialName("vehicle_id")
    val vehicleId: Int,
    @SerialName("registration_number")
    val registrationNumber: String,
    @SerialName("revenue")
    val revenue: Double,
    @SerialName("trip_costs")
    val tripCosts: Double,
    @SerialName("maintenance_costs")
    val maintenanceCosts: Double,
    @SerialName("emi_costs")
    val emiCosts: Double,
    @SerialName("document_costs")
    val documentCosts: Double,
    @SerialName("total_expenses")
    val totalExpenses: Double,
    @SerialName("profit")
    val profit: Double,
    @SerialName("margin")
    val margin: Double,
    @SerialName("status")
    val status: String
)

// Request DTOs
@Serializable
data class MultiVehiclePLRequest(
    @SerialName("vehicle_ids")
    val vehicleIds: List<Int>,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String,
    @SerialName("include_comparison")
    val includeComparison: Boolean = false,
    @SerialName("include_breakdown")
    val includeBreakdown: Boolean = true
)

@Serializable
data class CostTypeReportRequest(
    @SerialName("cost_ids")
    val costIds: List<String>,
    @SerialName("vehicle_ids")
    val vehicleIds: List<Int>? = null,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String
)
```

---

## Implementation Considerations

### 1. Document Costs Gap (⚠️ CRITICAL)

**Current Issue:**  
Document costs (Insurance, Permit, PUC, Road Tax, Fitness) are NOT currently tracked in the database. The `documents` table only stores metadata without cost information.

**Impact:**  
- Vehicle P&L will be incomplete without document costs
- Fixed cost calculations will underestimate true ownership costs
- Inaccurate profitability assessment

**Recommended Solution:**  
Create a separate `document_costs` table (preferred for audit trail):

```sql
CREATE TABLE document_costs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT REFERENCES documents(id),
    vehicle_id BIGINT NOT NULL REFERENCES vehicles(id),
    document_type VARCHAR(50) NOT NULL,  -- INS, PUC, FC, RT, PERMIT
    amount DECIMAL(12,2) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    payment_mode VARCHAR(20),            -- cash, upi, netbanking, cheque
    vendor_name VARCHAR(255),
    invoice_number VARCHAR(100),
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,
    notes TEXT,
    owner_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
```

**Frontend Handling (Before Backend Implementation):**
- Display a notice in reports: "Document costs not yet tracked"
- Show 0 for document costs with a tooltip explaining the gap
- Provide a link to request the feature or add manually

```kotlin
// Handle missing document costs gracefully
val documentCosts = vehiclePL.documentCosts ?: 0.0
val hasDocumentCostsGap = vehiclePL.documentCosts == null

if (hasDocumentCostsGap) {
    Text(
        "* Document costs not tracked",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline
    )
}
```

### 2. Cost Allocation for Fixed Costs

**Decision:** Use **time-based allocation** for accuracy.

**Implementation:**

```kotlin
object FixedCostAllocator {
    
    /**
     * Allocate annual cost to a specific period
     */
    fun allocateAnnualCost(
        annualCost: Double,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): Double {
        val daysInPeriod = periodStart.daysUntil(periodEnd) + 1
        val dailyRate = annualCost / 365.0
        return dailyRate * daysInPeriod
    }
    
    /**
     * Allocate monthly EMI to a period
     */
    fun allocateMonthlyEmi(
        monthlyEmi: Double,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): Double {
        val daysInPeriod = periodStart.daysUntil(periodEnd) + 1
        val dailyRate = monthlyEmi / 30.44  // Average days per month
        return dailyRate * daysInPeriod
    }
    
    /**
     * Get allocated EMI for vehicles with active loans
     * For actual paid EMIs, query the vehicle_loan_payments table instead
     */
    fun getActualEmiCost(
        vehicleId: Int,
        periodStart: LocalDate,
        periodEnd: LocalDate,
        paidEmis: List<LoanPayment>
    ): Double {
        return paidEmis
            .filter { it.paymentDate in periodStart..periodEnd }
            .sumOf { it.amount + (it.lateFee ?: 0.0) }
    }
}
```

**Note:** The backend should handle this allocation. The frontend should display whatever the API returns. If the API doesn't allocate, the frontend can apply allocation client-side as a fallback.

### 3. Driver Costs Attribution

**Decision:**
- **Trip P&L:** Include ONLY trip-linked driver costs (`trip_id IS NOT NULL`)
- **Vehicle P&L:** Include trip-linked costs for that vehicle's trips
- **Fleet P&L:** Include ALL driver costs (salaries, incentives, deductions)

**Implementation:**

```kotlin
enum class DriverCostScope {
    TRIP_ONLY,      // Only costs with trip_id
    VEHICLE_TRIPS,  // Costs linked to a specific vehicle's trips
    ALL             // All driver costs
}

suspend fun getDriverCosts(
    scope: DriverCostScope,
    tripId: Int? = null,
    vehicleId: Int? = null,
    dateRange: DateRange
): Double {
    return when (scope) {
        DriverCostScope.TRIP_ONLY -> {
            requireNotNull(tripId) { "tripId required for TRIP_ONLY scope" }
            repository.getDriverCostsForTrip(tripId)
        }
        DriverCostScope.VEHICLE_TRIPS -> {
            requireNotNull(vehicleId) { "vehicleId required for VEHICLE_TRIPS scope" }
            repository.getDriverCostsForVehicleTrips(vehicleId, dateRange)
        }
        DriverCostScope.ALL -> {
            repository.getAllDriverCosts(dateRange)
        }
    }
}
```

**API Parameter:**
```json
{
    "driver_cost_scope": "trip_only" | "vehicle_trips" | "all"
}
```

### 4. Role-Based Access Control

| Report Feature | Owner | GM | Manager | Supervisor |
|----------------|:-----:|:--:|:-------:|:----------:|
| View Reports Dashboard | ✅ | ✅ | ❌ | ❌ |
| Fleet P&L | ✅ | ✅ | ❌ | ❌ |
| Vehicle P&L | ✅ | ✅ | ❌ | ❌ |
| Trip P&L | ✅ | ✅ | ❌ | ❌ |
| Cost Type Analysis | ✅ | ✅ | ❌ | ❌ |
| Driver Costs | ✅ | ✅ | ❌ | ❌ |
| Export PDF | ✅ | ✅ | ❌ | ❌ |

```kotlin
fun canAccessReports(userRole: UserRole): Boolean {
    return userRole in listOf(UserRole.OWNER, UserRole.GENERAL_MANAGER)
}
```

---

## File Structure

```
sharedUI/src/commonMain/kotlin/com/indusjs/fleet/
├── domain/
│   ├── entity/reports/
│   │   ├── ReportPeriod.kt
│   │   ├── ProfitStatus.kt
│   │   ├── ProfitLossSummary.kt
│   │   ├── VehicleProfitLoss.kt
│   │   ├── TripProfitLoss.kt
│   │   ├── CostBreakdown.kt
│   │   ├── ExpenseBreakdown.kt
│   │   └── PeriodComparison.kt
│   ├── repository/reports/
│   │   └── ReportsRepository.kt
│   └── usecase/reports/
│       ├── GetFleetProfitLossUseCase.kt
│       ├── GetVehicleProfitLossUseCase.kt
│       ├── GetTripProfitLossUseCase.kt
│       ├── GetCostTypeReportUseCase.kt
│       └── GetDriverCostReportUseCase.kt
├── data/
│   ├── model/reports/
│   │   ├── ProfitLossReportDto.kt
│   │   ├── VehicleProfitLossDto.kt
│   │   ├── TripProfitLossDto.kt
│   │   ├── CostBreakdownDto.kt
│   │   └── ReportRequestDto.kt
│   ├── datasource/reports/
│   │   └── ReportsRemoteDataSource.kt
│   ├── mapper/reports/
│   │   └── ReportsMapper.kt
│   └── repository/reports/
│       └── ReportsRepositoryImpl.kt
├── presentation/reports/
│   ├── ReportsContract.kt
│   ├── ReportsViewModel.kt
│   ├── ReportsDashboardScreen.kt
│   ├── VehicleProfitLossScreen.kt
│   ├── TripProfitLossScreen.kt
│   ├── FleetProfitLossScreen.kt
│   ├── CostTypeReportScreen.kt
│   ├── DriverCostReportScreen.kt
│   └── components/
│       ├── ProfitLossCard.kt
│       ├── ProfitStatusBadge.kt
│       ├── CostBreakdownChart.kt
│       ├── DateRangeSelector.kt
│       ├── VehicleSelectionBottomSheet.kt
│       ├── CostTypeFilterChips.kt
│       ├── MetricsGrid.kt
│       └── SortOptionsMenu.kt
├── di/
│   └── ReportsFeatureGraph.kt
└── navigation/
    └── FleetRoute.kt  # Add report routes
```

---

## PDF Report Generation

### Report Structure

```
┌─────────────────────────────────────────────────────────────────┐
│                        REPORT HEADER                            │
│  Company: IndusJS Fleet                                         │
│  Report: Vehicle Profit & Loss                                  │
│  Period: 01-Jan-2026 to 31-Jan-2026                            │
│  Generated: 10-Feb-2026 10:30 AM                               │
├─────────────────────────────────────────────────────────────────┤
│                      EXECUTIVE SUMMARY                          │
│  Total Revenue: ₹5,00,000                                      │
│  Total Expenses: ₹3,80,000                                     │
│  Net Profit: ₹1,20,000                                         │
│  Profit Margin: 24%  [🟢 Highly Profitable]                    │
├─────────────────────────────────────────────────────────────────┤
│                    EXPENSE BREAKDOWN                            │
│  Category           Amount      % of Total                     │
│  ─────────────────────────────────────────────                 │
│  Fuel               ₹1,50,000   39.5%                          │
│  EMI                ₹84,000     22.1%                          │
│  Maintenance        ₹45,000     11.8%                          │
│  Toll               ₹30,000     7.9%                           │
│  ...                                                           │
├─────────────────────────────────────────────────────────────────┤
│                 VEHICLE-WISE SUMMARY                            │
│  Vehicle          Revenue     Expense    Profit    Margin      │
│  ─────────────────────────────────────────────────────────     │
│  MH12AB1234      ₹2,00,000   ₹1,50,000  ₹50,000   25% 🟢      │
│  MH12CD5678      ₹1,80,000   ₹1,40,000  ₹40,000   22% 🟢      │
│  ...                                                           │
├─────────────────────────────────────────────────────────────────┤
│                        FOOTER                                   │
│  Generated by IndusJS Fleet                                    │
│  Report ID: RPT-2026-02-001                                    │
└─────────────────────────────────────────────────────────────────┘
```

Use `ijs-pdf-report` library for PDF generation.

---

## Testing Checklist

### Functional Tests
- [ ] All date range periods calculate correctly
- [ ] Multi-vehicle selection persists when bottom sheet reopens
- [ ] Search filters vehicles correctly
- [ ] Sorting works for all options
- [ ] P&L calculations match expected formulas
- [ ] Profit status colors and labels are correct
- [ ] PDF export generates valid document
- [ ] Empty states display correctly
- [ ] Error states with retry work

### Edge Cases
- [ ] Zero revenue (all costs, no income)
- [ ] Zero expenses (all revenue, no costs)
- [ ] Negative profit (loss scenario)
- [ ] Single vehicle/trip selection
- [ ] Maximum date range (full year)
- [ ] Vehicles with no trips in period
- [ ] Drivers with only deductions

### Role Access
- [ ] Owner can access all reports
- [ ] GM can access all reports
- [ ] Manager cannot access reports
- [ ] Supervisor cannot access reports

---

## Related Documentation

- [Profit Loss Calculations](../../Docs/modules/reports/profit-loss-calculations.md)
- [Reports Module](../../Docs/modules/reports/reports-README.md)
- [Costs Module](../../Docs/modules/costs/costs-README.md)
- [Finance Module](../../Docs/modules/finance/finance-README.md)
- [Cost Types Reference](./cost_types.md)
- [Trip Payments](./trip-payments.prompt.md)

---

*Last Updated: February 10, 2026*

