# Profit & Loss Calculation Framework

## Overview

This document provides a comprehensive analysis of all cost components, revenue sources, and calculation formulas for generating accurate Loss & Profit reports across various dimensions in the IndusJS Fleet application.

---

## Table of Contents

1. [Revenue Sources](#1-revenue-sources)
2. [Expense Categories](#2-expense-categories)
3. [Vehicle Purchase & Loan Tracking](#3-vehicle-purchase--loan-tracking)
4. [P&L Calculation Formulas](#4-pl-calculation-formulas)
5. [Date Range Periods](#5-date-range-periods)
6. [Cost Allocation Strategies](#6-cost-allocation-strategies)
7. [Key Metrics](#7-key-metrics)
8. [Profitability Indicators](#8-profitability-indicators)
9. [Report Types](#9-report-types)
10. [Comparative & Trend Analysis](#10-comparative--trend-analysis)
11. [Break-even & Lifecycle Analysis](#11-break-even--lifecycle-analysis)
12. [Customer & Route Profitability](#12-customer--route-profitability)
13. [Report Export & PDF Generation](#13-report-export--pdf-generation)
14. [Role-Based Access Control](#14-role-based-access-control)
15. [Data Gaps & Recommendations](#15-data-gaps--recommendations)
16. [API Endpoints](#16-api-endpoints)

---

## 1. Revenue Sources

### Trip Revenue

| Source | Table | Field | Description |
|--------|-------|-------|-------------|
| Trip Payment | `trip_payments` | `net_amount` | Actual payments received (after TDS & discounts) |
| Trip Price | `trips` | `paid_trip_price` | Sum of received payments (auto-calculated) |
| Expected Price | `trips` | `expected_trip_price` | Quoted/agreed price with customer |
| Cargo Margin | `trips` | `selling_value - purchase_price` | Goods trading margin (if applicable) |

### Revenue Calculation Formula

```
Actual Trip Revenue = SUM(trip_payments.net_amount WHERE payment_status = 'received')

Expected Trip Revenue = trips.expected_trip_price (for budgeting)

Cargo Margin = selling_value - purchase_price (when applicable)
```

**Recommendation**: Use `paid_trip_price` (sum of received payments) for actual P&L reporting.

---

## 2. Expense Categories

### 2.1 Trip Costs (Table: `trip_costs`)

Costs incurred during trip execution.

| Group ID | Group Name | Cost IDs | Description |
|----------|------------|----------|-------------|
| TC-G-001 | Fuel & Energy | TC-001-001 to TC-001-005 | Petrol, Diesel, CNG, Electric, AdBlue |
| TC-G-002 | Toll & Parking | TC-002-001 to TC-002-004 | Highway Toll, Bridge Toll, Parking, Entry Tax |
| TC-G-003 | Loading/Unloading | TC-003-001 to TC-003-004 | Loading, Unloading, Crane, Weighbridge |
| TC-G-004 | Driver Expenses | TC-004-001 to TC-004-003 | Food, Accommodation, Allowance |
| TC-G-005 | Permits & Compliance | TC-005-001 to TC-005-004 | State Permit, Border Tax, RTO, Fines |
| TC-G-006 | Miscellaneous | TC-006-001 to TC-006-005 | Commission, Communication, Documentation, Repairs, Other |

**Trip Cost Formula**:
```sql
Total Trip Cost = SUM(trip_costs.amount 
                      WHERE trip_id = X 
                      AND deleted_at IS NULL)
```

### 2.2 Vehicle Maintenance Costs (Table: `vehicle_maintenance_costs`)

Costs for vehicle upkeep and repairs.

| Group ID | Group Name | Cost IDs | Description |
|----------|------------|----------|-------------|
| VMC-G-001 | Regular Maintenance | VMC-001-001 to VMC-001-008 | Oil Change, Filters, Brakes, Service, Greasing |
| VMC-G-002 | Repairs & Replacements | VMC-002-001 to VMC-002-007 | Clutch, Radiator, Pumps, Motor, Alternator |
| VMC-G-003 | Electrical & AC | VMC-003-001 to VMC-003-006 | Battery, Wiring, Lights, AC Service, Compressor |
| VMC-G-004 | Body & Exterior | VMC-004-001 to VMC-004-005 | Denting, Painting, Windshield, Mirror, Door |
| VMC-G-005 | Engine & Transmission | VMC-005-001 to VMC-005-007 | Engine Overhaul, Gearbox, Differential, Axle |
| VMC-G-006 | Miscellaneous | VMC-006-001 to VMC-006-003 | Washing, Interior Cleaning, Other |
| VMC-G-007 | Wheels & Tyres | VMC-007-001 to VMC-007-006 | Tyre, Puncture, Alignment, Balancing, Rim |

**Maintenance Cost Formula**:
```sql
Total Maintenance Cost = SUM(vehicle_maintenance_costs.amount 
                             WHERE vehicle_id = X 
                             AND date BETWEEN start_date AND end_date
                             AND deleted_at IS NULL)
```

### 2.3 Vehicle EMI/Finance Costs (Table: `vehicle_loan_payments`)

Monthly EMI payments for financed vehicles.

| Cost Type | Field | Description |
|-----------|-------|-------------|
| EMI Payment | `amount` | Total monthly payment |
| Late Fees | `late_fee` | Penalty for late payment |
| Interest Component | `interest_amount` | Interest portion of EMI |
| Principal Component | `principal_amount` | Principal portion of EMI |
| Prepayment | `prepayment_amount` | Extra payment towards principal |

**EMI Cost Formula**:
```sql
Total EMI Cost = SUM(vehicle_loan_payments.amount + COALESCE(late_fee, 0)
                     WHERE vehicle_id = X 
                     AND payment_status = 'paid'
                     AND payment_date BETWEEN start_date AND end_date
                     AND deleted_at IS NULL)
```

### 2.4 Vehicle Document/Paper Costs

Costs associated with vehicle documentation renewals.

| Document Type | Tag | Renewal Frequency | Typical Cost Range |
|---------------|-----|-------------------|-------------------|
| Registration Certificate | RC | 15 years | One-time |
| Insurance | INS | Yearly | ₹15,000 - ₹50,000 |
| PUC Certificate | PUC | 6 months | ₹100 - ₹500 |
| Fitness Certificate | FC | 2 years | ₹1,000 - ₹3,000 |
| Road Tax | RT | Yearly/Lifetime | ₹5,000 - ₹50,000 |
| Permits | PERMIT | Yearly | ₹5,000 - ₹25,000 |

> **⚠️ IMPLEMENTATION REQUIRED**: Document costs tracking needs to be added.
> See [Data Gaps section](#15-data-gaps--recommendations) for proposed schema.

#### Proposed Document Cost Schema

**Option 1: Add columns to existing `documents` table**
```sql
ALTER TABLE documents ADD COLUMN cost DECIMAL(12,2) DEFAULT 0;
ALTER TABLE documents ADD COLUMN payment_date TIMESTAMP;
ALTER TABLE documents ADD COLUMN payment_mode VARCHAR(20);
ALTER TABLE documents ADD COLUMN vendor_name VARCHAR(255);
ALTER TABLE documents ADD COLUMN invoice_number VARCHAR(100);
```

**Option 2: Separate `document_costs` table (Recommended)**
```sql
CREATE TABLE document_costs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT REFERENCES documents(id),
    vehicle_id BIGINT NOT NULL REFERENCES vehicles(id),
    document_type VARCHAR(50) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    payment_mode VARCHAR(20),
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

**Document Cost Formula**:
```sql
Total Document Cost = SUM(document_costs.amount 
                          WHERE vehicle_id = X 
                          AND payment_date BETWEEN start_date AND end_date
                          AND deleted_at IS NULL)

-- OR with time-based allocation for annual costs
Allocated Document Cost = (Annual Cost / 12) × Months in Period
```

### 2.5 Driver Costs (Table: `driver_costs`)

Driver-related expenses including salaries, bonuses, and deductions.

| Group ID | Group Name | Is Deduction | Cost Types |
|----------|------------|--------------|------------|
| DC-G-001 | Salary & Wages | No | Monthly Salary, Daily Wages, Overtime, Night Shift |
| DC-G-002 | Incentives & Bonuses | No | Trip Bonus, Performance, Safety, Fuel Saving, Festival |
| DC-G-003 | Deductions | Yes | Advance Recovery, Damage, Loan EMI, Penalties |
| DC-G-004 | Other Allowances | No | Reimbursement, Travel, Mobile, Uniform |

**Driver Cost Formula**:
```sql
Net Driver Cost = (Salary + Incentives + Other) - Deductions

WHERE:
  Salary     = SUM(amount WHERE group_id = 'DC-G-001')
  Incentives = SUM(amount WHERE group_id = 'DC-G-002')
  Other      = SUM(amount WHERE group_id = 'DC-G-004')
  Deductions = SUM(amount WHERE group_id = 'DC-G-003')
```

---

## 3. Vehicle Purchase & Loan Tracking

### 3.1 Purchase Information (Table: `vehicle_purchases`)

| Field | Type | Description |
|-------|------|-------------|
| `purchase_date` | Timestamp | Date of vehicle purchase |
| `purchase_price` | Decimal | Total purchase price (₹) |
| `vendor_name` | String | Dealer/Vendor name |
| `invoice_number` | String | Purchase invoice number |
| `payment_type` | Enum | `cash` or `loan` |

### 3.2 Loan Details (for Financed Vehicles)

| Field | Type | Description |
|-------|------|-------------|
| `down_payment` | Decimal | Initial down payment (₹) |
| `loan_amount` | Decimal | Total loan principal (₹) |
| `interest_rate` | Decimal | Annual interest rate (%) |
| `tenure_months` | Integer | Loan duration in months |
| `emi_amount` | Decimal | Monthly EMI amount (₹) |
| `loan_start_date` | Timestamp | First EMI due date |
| `loan_end_date` | Timestamp | Last EMI due date |
| `financier_name` | String | Bank/NBFC name |
| `loan_account_number` | String | Loan account number |
| `bank_name` | String | Bank name for payments |
| `auto_debit_enabled` | Boolean | Auto-debit EMI enabled |

### 3.3 Loan Summary (Auto-Calculated Fields)

| Field | Formula | Description |
|-------|---------|-------------|
| `total_paid` | `down_payment + SUM(paid EMIs)` | Total amount paid so far |
| `outstanding_balance` | `loan_amount - SUM(principal_paid)` | Remaining loan amount |
| `emis_paid` | `COUNT(payments WHERE status='paid')` | EMIs completed |
| `emis_remaining` | `tenure_months - emis_paid` | Pending EMIs |
| `next_emi_due_date` | Calculated | Next payment due date |

### 3.4 Loan Status

| Status | Description |
|--------|-------------|
| `not_applicable` | Cash purchase, no loan |
| `active` | Loan in progress, EMIs being paid |
| `closed` | Loan fully paid off |
| `defaulted` | Loan defaulted |

### 3.5 EMI Payment Entry Types

| Entry Type | Description |
|------------|-------------|
| `scheduled` | Auto-generated from loan tenure |
| `manual` | User recorded after paying via netbanking/bank |
| `app_payment` | Paid through app (future integration) |
| `auto_debit` | Bank auto-debited, user recording it |

### 3.6 Payment Status

| Status | Description |
|--------|-------------|
| `pending` | Scheduled EMI, not yet paid |
| `paid` | Payment completed |
| `overdue` | Past due date, not paid |
| `failed` | Payment attempt failed |
| `cancelled` | Payment cancelled |

### 3.7 Total Cost of Purchase

```
Total Purchase Cost (Cash) = Purchase Price

Total Purchase Cost (Loan) = Purchase Price + Total Interest Paid + Late Fees

Where:
  Total Interest = SUM(interest_amount from all paid EMIs)
  Late Fees = SUM(late_fee from all EMIs)
  
For Active Loans (Projected):
  Projected Total = Down Payment + (EMI Amount × Tenure Months)
```

**EMI Cost for P&L**:
```sql
EMI Cost in Period = SUM(vehicle_loan_payments.amount + COALESCE(late_fee, 0)
                         WHERE vehicle_id = X 
                         AND payment_status = 'paid'
                         AND payment_date BETWEEN start_date AND end_date)
```

---

## 4. P&L Calculation Formulas

### 4.1 Trip-Level P&L

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
│ EXPENSES                                                         │
│   - Fuel Cost (TC-G-001)                                         │
│   - Toll & Parking (TC-G-002)                                    │
│   - Loading/Unloading (TC-G-003)                                 │
│   - Driver On-Trip Expenses (TC-G-004)                           │
│   - Permits & Compliance (TC-G-005)                              │
│   - Miscellaneous (TC-G-006)                                     │
│   ─────────────────────────────────────────────────              │
│   = TOTAL TRIP EXPENSES                                          │
├──────────────────────────────────────────────────────────────────┤
│ TRIP PROFIT/LOSS = TOTAL REVENUE - TOTAL EXPENSES                │
│ PROFIT MARGIN = (PROFIT / REVENUE) × 100                         │
│ PROFIT PER KM = PROFIT / actual_distance                         │
└──────────────────────────────────────────────────────────────────┘
```

### 4.2 Vehicle-Level P&L

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
│ FIXED/OWNERSHIP EXPENSES                                         │
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

### 4.3 Fleet-Level P&L (Consolidated)

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
│   - Sum of all Driver Costs                                      │
├──────────────────────────────────────────────────────────────────┤
│ FLEET PROFIT/LOSS = TOTAL REVENUE - TOTAL EXPENSES               │
│ AVERAGE MARGIN = (TOTAL PROFIT / TOTAL REVENUE) × 100            │
│ PROFITABLE VEHICLES = Count WHERE vehicle_profit > 0             │
│ LOSS-MAKING VEHICLES = Count WHERE vehicle_profit < 0            │
│ BREAK-EVEN VEHICLES = Count WHERE vehicle_profit = 0             │
│ FLEET UTILIZATION = Active Vehicles / Total Vehicles × 100       │
└──────────────────────────────────────────────────────────────────┘
```

### 4.4 Driver Cost Analysis

```
┌──────────────────────────────────────────────────────────────────┐
│                     DRIVER COST ANALYSIS                         │
├──────────────────────────────────────────────────────────────────┤
│ GROSS EARNINGS                                                   │
│   + Salary & Wages (DC-G-001)                                    │
│   + Incentives & Bonuses (DC-G-002)                              │
│   + Other Allowances (DC-G-004)                                  │
│   ─────────────────────────────────────────────────              │
│   = TOTAL GROSS EARNINGS                                         │
├──────────────────────────────────────────────────────────────────┤
│ DEDUCTIONS                                                       │
│   - Advance Recovery (DC-003-001)                                │
│   - Damage Deduction (DC-003-002)                                │
│   - Loan EMI (DC-003-003)                                        │
│   - Penalties (DC-003-004 to DC-003-006)                         │
│   ─────────────────────────────────────────────────              │
│   = TOTAL DEDUCTIONS                                             │
├──────────────────────────────────────────────────────────────────┤
│ NET DRIVER COST = GROSS EARNINGS - TOTAL DEDUCTIONS              │
│ COST PER TRIP = NET COST / TRIPS COMPLETED                       │
│ COST PER KM = NET COST / TOTAL KM DRIVEN                         │
└──────────────────────────────────────────────────────────────────┘
```

---

## 5. Date Range Periods

| Period | Calculation | Description |
|--------|-------------|-------------|
| **Daily** | Current date | Today's data only |
| **Weekly** | Last 7 days | Rolling week from today |
| **15 Days** | Last 15 days | Bi-weekly rolling |
| **Monthly** | Current calendar month | 1st to last day of month |
| **Quarterly** | Current quarter | Q1: Apr-Jun, Q2: Jul-Sep, Q3: Oct-Dec, Q4: Jan-Mar |
| **Half Yearly** | Last 6 months | Rolling 6 months |
| **Yearly** | Financial year | April 1 to March 31 |
| **Custom** | User-defined | Any start_date to end_date |

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

fun getFinancialMonth(date: LocalDate): String {
    return "${date.year}-${date.monthNumber.toString().padStart(2, '0')}"
}
```

---

## 6. Cost Allocation Strategies

### 6.1 Time-Based Allocation (for Fixed Costs)

EMI, Insurance, Road Tax, and other fixed costs should be allocated proportionally across reporting periods.

```
Daily Allocation = Monthly Cost / Days in Month
Weekly Allocation = Monthly Cost / 4.33
Period Allocation = Daily Allocation × Days in Period
```

**Example**: Vehicle Insurance = ₹60,000/year

| Period | Allocation |
|--------|------------|
| Yearly | ₹60,000 |
| Half-Yearly | ₹30,000 |
| Quarterly | ₹15,000 |
| Monthly | ₹5,000 |
| Weekly | ₹1,154 |
| Daily | ₹164 |

### 6.2 Usage-Based Allocation (for Variable Costs)

Fuel, maintenance, and trip costs are directly allocated to the period in which they occurred.

```sql
-- Variable costs for period
SELECT SUM(amount) 
FROM trip_costs 
WHERE date BETWEEN start_date AND end_date
```

### 6.3 Trip-Based Allocation

Driver costs with `trip_id` linked are allocated to specific trips.

```sql
-- Driver costs per trip
SELECT SUM(amount)
FROM driver_costs
WHERE trip_id = X AND is_deduction = false
```

### 6.4 Distance-Based Allocation

For multi-trip analysis, allocate shared costs by distance proportion.

```
Vehicle Share = Trip Distance / Total Vehicle Distance × Fixed Costs
```

---

## 7. Key Metrics

### 7.1 Financial Metrics

| Metric | Formula | Unit | Description |
|--------|---------|------|-------------|
| Gross Profit | Revenue - Trip Costs | ₹ | Before fixed costs |
| Net Profit | Revenue - All Costs | ₹ | After all costs |
| Profit Margin | (Net Profit / Revenue) × 100 | % | Profitability ratio |
| ROI | (Net Profit / Total Investment) × 100 | % | Return on investment |

### 7.2 Efficiency Metrics

| Metric | Formula | Unit | Description |
|--------|---------|------|-------------|
| Cost per KM | Total Costs / Distance | ₹/km | Operating efficiency |
| Revenue per KM | Revenue / Distance | ₹/km | Earning efficiency |
| Profit per KM | Net Profit / Distance | ₹/km | Net earning per km |
| Cost per Trip | Total Costs / Trip Count | ₹/trip | Per-trip cost |
| Fuel Efficiency | Distance / Fuel Quantity | km/L | Fuel economy |
| Fuel Cost per KM | Fuel Cost / Distance | ₹/km | Fuel spending |

### 7.3 Ratio Metrics

| Metric | Formula | Description |
|--------|---------|-------------|
| Maintenance Ratio | (Maintenance / Revenue) × 100 | % of revenue on maintenance |
| EMI Burden | (EMI / Revenue) × 100 | % of revenue on EMI |
| Fuel Ratio | (Fuel Cost / Total Costs) × 100 | Fuel as % of total costs |
| Driver Cost Ratio | (Driver Costs / Revenue) × 100 | Driver expense burden |
| Fixed Cost Ratio | (Fixed Costs / Total Costs) × 100 | Fixed vs variable split |
| Operating Leverage | Variable Costs / Fixed Costs | Cost structure indicator |

### 7.4 Utilization Metrics

| Metric | Formula | Description |
|--------|---------|-------------|
| Vehicle Utilization | Days on Trip / Total Days × 100 | Vehicle usage rate |
| Driver Utilization | Active Days / Working Days × 100 | Driver productivity |
| Load Factor | Actual Load / Max Capacity × 100 | Capacity utilization |
| Trip Frequency | Total Trips / Period Days | Trips per day |

---

## 8. Profitability Indicators

| Status | Profit Margin | Color Code | Action |
|--------|---------------|------------|--------|
| 🟢 Highly Profitable | > 20% | `#2E7D32` (Dark Green) | Maintain strategy |
| 🟢 Profitable | 10% - 20% | `#4CAF50` (Green) | Optimize costs |
| 🟡 Break-even | 0% - 10% | `#FFC107` (Yellow) | Review expenses |
| 🔴 Loss | 0% to -20% | `#F44336` (Red) | Immediate review needed |
| 🔴 Severe Loss | < -20% | `#B71C1C` (Dark Red) | Critical intervention required |

### Visual Indicators in UI

```kotlin
fun getProfitStatusColor(margin: Double): Color {
    return when {
        margin > 20 -> Color(0xFF2E7D32)  // Dark Green
        margin > 10 -> Color(0xFF4CAF50)  // Green
        margin > 0 -> Color(0xFFFFC107)   // Yellow/Amber
        margin > -20 -> Color(0xFFF44336) // Red
        else -> Color(0xFFB71C1C)         // Dark Red
    }
}

fun getProfitStatusLabel(margin: Double): String {
    return when {
        margin > 20 -> "Highly Profitable"
        margin > 10 -> "Profitable"
        margin > 0 -> "Break-even"
        margin > -20 -> "Loss"
        else -> "Severe Loss"
    }
}

fun getProfitStatusIcon(margin: Double): String {
    return when {
        margin > 10 -> "📈"
        margin > 0 -> "📊"
        else -> "📉"
    }
}
```

---

## 9. Report Types

### 9.1 Vehicle Base Report

**Includes**:
- EMI Payments (from `vehicle_loan_payments`)
- Maintenance Costs (from `vehicle_maintenance_costs`)
- Document/Paper Costs (from `document_costs`)

**Use Case**: Track fixed vehicle ownership costs

**Formula**:
```
Vehicle Fixed Cost = EMI + Maintenance + Document Costs
```

**Features**:
- Multi-vehicle selection via bottom sheet
- Pre-selected vehicles shown on top when reopening
- Search by registration number
- Date range filter
- Sorting by cost, date, vehicle
- Cost breakdown by type
- PDF export

### 9.2 Trip Base Report

**Includes**:
- All trip costs by group
- Driver on-trip allowances (TC-G-004)
- Permit costs (TC-G-005)
- Revenue vs expenses

**Use Case**: Track operating costs and trip profitability

**Formula**:
```
Trip Operating Cost = Fuel + Toll + Loading + Driver + Permit + Misc
Trip Profit = Trip Revenue - Trip Operating Cost
```

**Features**:
- Date range filter
- Search by trip ID or route
- Cost breakdown by category
- Sorting by profit, revenue, date
- PDF export

### 9.3 Combined Vehicle + Trip Report

**Includes**:
- All Trip Costs (fuel, toll, loading, driver, permits, misc)
- All Vehicle Fixed Costs (maintenance, EMI, documents)

**Use Case**: Complete profitability analysis for selected period

**Formula**:
```
Total Revenue = SUM(trip_payments.net_amount) for period

Total Expenses = Trip Costs + Maintenance Costs + EMI Payments + Document Costs

WHERE all costs are filtered by:
  date/payment_date BETWEEN start_date AND end_date

Net Profit = Total Revenue - Total Expenses
```

**Features**:
- Combined view of variable and fixed costs
- Date range filter
- Search by trip or vehicle
- Comprehensive breakdown
- PDF export

### 9.4 Individual Maintenance Cost Type Report

**Use Case**: Drill-down analysis of specific maintenance expenses

**Example**: "How much did I spend on Tyres across all vehicles?"

```sql
SELECT 
    v.registration_number,
    SUM(vmc.amount) as total,
    COUNT(*) as count
FROM vehicle_maintenance_costs vmc
JOIN vehicles v ON vmc.vehicle_id = v.id
WHERE vmc.cost_id LIKE 'VMC-007-%'  -- Wheels & Tyres group
AND vmc.date BETWEEN start AND end
GROUP BY v.id
ORDER BY total DESC
```

### 9.5 Individual Trip Cost Type Report

**Use Case**: Drill-down analysis of specific trip expenses

**Example**: "How much did I spend on Diesel across all trips?"

```sql
SELECT 
    t.id as trip_id,
    v.registration_number,
    SUM(tc.amount) as total,
    SUM(tc.fuel_quantity) as liters
FROM trip_costs tc
JOIN trips t ON tc.trip_id = t.id
JOIN vehicles v ON tc.vehicle_id = v.id
WHERE tc.cost_id = 'TC-001-002'  -- Diesel
AND tc.date BETWEEN start AND end
GROUP BY t.id
ORDER BY total DESC
```

### 9.6 Driver Cost Type Report

**Use Case**: Analyze driver-related expenses

```sql
SELECT 
    d.first_name || ' ' || d.last_name as driver_name,
    SUM(CASE WHEN dc.group_id = 'DC-G-001' THEN amount ELSE 0 END) as salary,
    SUM(CASE WHEN dc.group_id = 'DC-G-002' THEN amount ELSE 0 END) as incentives,
    SUM(CASE WHEN dc.group_id = 'DC-G-003' THEN amount ELSE 0 END) as deductions,
    SUM(CASE WHEN dc.group_id = 'DC-G-004' THEN amount ELSE 0 END) as other,
    SUM(CASE WHEN NOT is_deduction THEN amount ELSE -amount END) as net
FROM driver_costs dc
JOIN drivers d ON dc.driver_id = d.id
WHERE dc.date BETWEEN start AND end
GROUP BY d.id
ORDER BY net DESC
```

---

## 10. Comparative & Trend Analysis

### 10.1 Period-over-Period Comparison

Compare current period with previous periods.

| Comparison Type | Current Period | Comparison Period |
|-----------------|----------------|-------------------|
| Week-over-Week (WoW) | This Week | Last Week |
| Month-over-Month (MoM) | This Month | Last Month |
| Quarter-over-Quarter (QoQ) | This Quarter | Last Quarter |
| Year-over-Year (YoY) | This FY | Last FY |

**Variance Calculation**:
```
Variance = Current Value - Previous Value
Variance % = ((Current - Previous) / Previous) × 100

Growth Indicator:
  ↑ Positive growth (green) - Revenue increase or Cost decrease
  ↓ Negative growth (red) - Revenue decrease or Cost increase
  → No change (grey)
```

### 10.2 Trend Analysis

Track metrics over time to identify patterns.

**Monthly Trend Example**:
```kotlin
data class TrendData(
    val period: String,        // "Jan 2026"
    val revenue: Double,
    val expenses: Double,
    val profit: Double,
    val margin: Double,
    val tripCount: Int,
    val distance: Double
)

// Calculate 6-month trend
fun getSixMonthTrend(vehicleId: Int): List<TrendData> {
    return (0..5).map { monthsAgo ->
        val period = today.minus(monthsAgo, DateTimeUnit.MONTH)
        calculateMonthData(vehicleId, period)
    }.reversed()
}
```

### 10.3 Budget vs Actual Analysis

Compare planned/expected vs actual performance.

```
Budget Variance = Actual - Budget
Variance % = (Variance / Budget) × 100

Favorable: Actual Revenue > Budget OR Actual Cost < Budget
Unfavorable: Actual Revenue < Budget OR Actual Cost > Budget
```

| Metric | Budget | Actual | Variance | Status |
|--------|--------|--------|----------|--------|
| Revenue | ₹500,000 | ₹480,000 | -₹20,000 (-4%) | 🔴 Unfavorable |
| Fuel Cost | ₹150,000 | ₹140,000 | -₹10,000 (-7%) | 🟢 Favorable |
| Maintenance | ₹50,000 | ₹65,000 | +₹15,000 (+30%) | 🔴 Unfavorable |

---

## 11. Break-even & Lifecycle Analysis

### 11.1 Break-even Analysis

Calculate when a vehicle recovers its investment.

**Break-even Point (Trips)**:
```
Break-even Trips = Total Fixed Costs / Average Profit per Trip

Example:
  Vehicle Purchase: ₹25,00,000
  Annual Fixed Costs: ₹5,00,000 (EMI + Insurance + Maintenance)
  Average Profit per Trip: ₹15,000
  
  Break-even = ₹30,00,000 / ₹15,000 = 200 trips
  At 12 trips/month = ~17 months to break-even
```

**Break-even Point (Time)**:
```
Monthly Fixed Costs = EMI + (Insurance/12) + (Avg Maintenance/12)
Monthly Revenue Required = Monthly Fixed Costs / Target Margin

Example:
  Monthly Fixed: ₹75,000
  Target Margin: 20%
  Required Monthly Revenue: ₹75,000 / 0.20 = ₹3,75,000
```

### 11.2 Vehicle Lifecycle Cost (Total Cost of Ownership)

Calculate complete cost of owning and operating a vehicle over its lifetime.

```
┌─────────────────────────────────────────────────────────────────┐
│              VEHICLE LIFECYCLE COST (TCO)                       │
├─────────────────────────────────────────────────────────────────┤
│ ACQUISITION COST                                                │
│   + Purchase Price                                              │
│   + Registration & Initial Documentation                        │
│   + Interest on Loan (Total over tenure)                        │
│   + Late Fees (if any)                                          │
│   ─────────────────────────────────────────────                 │
│   = TOTAL ACQUISITION COST                                      │
├─────────────────────────────────────────────────────────────────┤
│ OPERATING COST (Over Lifecycle)                                 │
│   + Total Fuel Costs                                            │
│   + Total Toll Costs                                            │
│   + Total Driver Costs (trip-related)                           │
│   + Total Other Trip Costs                                      │
│   ─────────────────────────────────────────────────             │
│   = TOTAL OPERATING COST                                        │
├─────────────────────────────────────────────────────────────────┤
│ MAINTENANCE COST (Over Lifecycle)                               │
│   + Regular Maintenance                                         │
│   + Repairs & Replacements                                      │
│   + Tyres & Consumables                                         │
│   ─────────────────────────────────────────────────             │
│   = TOTAL MAINTENANCE COST                                      │
├─────────────────────────────────────────────────────────────────┤
│ COMPLIANCE COST (Over Lifecycle)                                │
│   + Insurance (yearly × years)                                  │
│   + Permits (yearly × years)                                    │
│   + Road Tax                                                    │
│   + Fitness & PUC                                               │
│   ─────────────────────────────────────────────────             │
│   = TOTAL COMPLIANCE COST                                       │
├─────────────────────────────────────────────────────────────────┤
│ RESIDUAL VALUE                                                  │
│   - Estimated Resale Value at End of Life                       │
├─────────────────────────────────────────────────────────────────┤
│ TOTAL COST OF OWNERSHIP = Acquisition + Operating +             │
│                           Maintenance + Compliance - Residual   │
│                                                                 │
│ COST PER KM (Lifecycle) = TCO / Total Expected KMs              │
│ COST PER YEAR = TCO / Expected Years                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 12. Customer & Route Profitability

### 12.1 Customer Profitability Analysis

Identify most and least profitable customers.

```sql
SELECT 
    c.company_name,
    COUNT(t.id) as total_trips,
    SUM(t.paid_trip_price) as total_revenue,
    SUM(tc.total_cost) as total_cost,
    SUM(t.paid_trip_price) - SUM(tc.total_cost) as profit,
    (SUM(t.paid_trip_price) - SUM(tc.total_cost)) / 
        NULLIF(SUM(t.paid_trip_price), 0) * 100 as margin
FROM customers c
JOIN trips t ON c.id = t.customer_id
LEFT JOIN (
    SELECT trip_id, SUM(amount) as total_cost
    FROM trip_costs
    GROUP BY trip_id
) tc ON t.id = tc.trip_id
WHERE t.state = 'completed'
GROUP BY c.id
ORDER BY profit DESC
```

**Customer Metrics**:
| Metric | Description |
|--------|-------------|
| Total Revenue | Sum of all trip payments from customer |
| Total Trips | Number of completed trips |
| Average Trip Value | Revenue / Trips |
| Payment Behavior | % of on-time payments |
| Profitability Rank | Ranking by profit margin |

### 12.2 Route Profitability Analysis

Identify profitable and unprofitable routes.

```sql
SELECT 
    CONCAT(start_location, ' → ', end_location) as route,
    COUNT(*) as trip_count,
    AVG(actual_distance) as avg_distance,
    SUM(paid_trip_price) as total_revenue,
    SUM(cost.total) as total_cost,
    SUM(paid_trip_price - cost.total) as total_profit,
    AVG(paid_trip_price - cost.total) as avg_profit_per_trip
FROM trips t
LEFT JOIN (
    SELECT trip_id, SUM(amount) as total
    FROM trip_costs
    GROUP BY trip_id
) cost ON t.id = cost.trip_id
WHERE t.state = 'completed'
GROUP BY start_location, end_location
ORDER BY total_profit DESC
```

**Route Metrics**:
| Metric | Description |
|--------|-------------|
| Frequency | How often this route is traveled |
| Average Distance | Typical distance for route |
| Revenue per KM | Route earning efficiency |
| Profit per KM | Net profit per km on this route |
| Best Performing Vehicle | Which vehicle is most efficient on this route |

---

## 13. Report Export & PDF Generation

### 13.1 PDF Report Structure

```
┌─────────────────────────────────────────────────────────────────┐
│                        REPORT HEADER                            │
│  Company: IndusJS Fleet                                         │
│  Report: Vehicle Profit & Loss                                  │
│  Period: 01-Jan-2026 to 31-Jan-2026                            │
│  Generated: 02-Feb-2026 10:30 AM                               │
├─────────────────────────────────────────────────────────────────┤
│                      EXECUTIVE SUMMARY                          │
│  Total Revenue: ₹5,00,000                                      │
│  Total Expenses: ₹3,80,000                                     │
│  Net Profit: ₹1,20,000                                         │
│  Profit Margin: 24%  [🟢 Highly Profitable]                    │
├─────────────────────────────────────────────────────────────────┤
│                    REVENUE BREAKDOWN                            │
│  ┌───────────────────────────────────────────┐                 │
│  │ Trip Payments      ₹4,80,000    96%       │                 │
│  │ Cargo Margin       ₹20,000      4%        │                 │
│  └───────────────────────────────────────────┘                 │
├─────────────────────────────────────────────────────────────────┤
│                    EXPENSE BREAKDOWN                            │
│  Category           Amount      % of Total                     │
│  ─────────────────────────────────────────────                 │
│  Fuel               ₹1,50,000   39.5%                          │
│  EMI                ₹84,000     22.1%                          │
│  Maintenance        ₹45,000     11.8%                          │
│  Toll               ₹30,000     7.9%                           │
│  Loading/Unloading  ₹25,000     6.6%                           │
│  Documents          ₹16,000     4.2%                           │
│  Driver Expenses    ₹15,000     3.9%                           │
│  Permits            ₹10,000     2.6%                           │
│  Miscellaneous      ₹5,000      1.3%                           │
│  ─────────────────────────────────────────────                 │
│  TOTAL              ₹3,80,000   100%                           │
├─────────────────────────────────────────────────────────────────┤
│                    KEY METRICS                                  │
│  Total Trips: 25                                               │
│  Total Distance: 12,500 km                                     │
│  Profit per Trip: ₹4,800                                       │
│  Profit per KM: ₹9.60                                          │
│  Cost per KM: ₹30.40                                           │
├─────────────────────────────────────────────────────────────────┤
│                 VEHICLE-WISE SUMMARY                            │
│  Vehicle          Revenue     Expense    Profit    Margin      │
│  ─────────────────────────────────────────────────────────     │
│  MH12AB1234      ₹2,00,000   ₹1,50,000  ₹50,000   25% 🟢      │
│  MH12CD5678      ₹1,80,000   ₹1,40,000  ₹40,000   22% 🟢      │
│  MH12EF9012      ₹1,20,000   ₹90,000    ₹30,000   25% 🟢      │
├─────────────────────────────────────────────────────────────────┤
│                        FOOTER                                   │
│  This report is auto-generated by IndusJS Fleet                │
│  For queries, contact: support@indusjs.com                     │
└─────────────────────────────────────────────────────────────────┘
```

### 13.2 Export Formats

| Format | Description | Use Case |
|--------|-------------|----------|
| PDF | Printable document | Official records, accounting |
| Share | Native device sharing | Quick sharing via apps |

### 13.3 PDF Generation Code

```kotlin
fun generateProfitLossReport(
    data: ProfitLossData,
    period: DateRange
): ByteArray {
    val html = buildReportHtml(data, period)
    return convertHtmlToPdf(html)
}

fun buildReportHtml(data: ProfitLossData, period: DateRange): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; margin: 20px; }
                .header { text-align: center; border-bottom: 2px solid #333; padding-bottom: 10px; }
                .summary { background: #f5f5f5; padding: 16px; margin: 16px 0; border-radius: 8px; }
                table { width: 100%; border-collapse: collapse; margin: 16px 0; }
                th, td { padding: 8px; border: 1px solid #ddd; text-align: left; }
                th { background: #f0f0f0; }
                .profit { color: #2E7D32; font-weight: bold; }
                .loss { color: #F44336; font-weight: bold; }
                .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
            </style>
        </head>
        <body>
            <!-- Report content dynamically inserted -->
        </body>
        </html>
    """.trimIndent()
}
```

---

## 14. Role-Based Access Control

### 14.1 Report Access Matrix

| Report Type | Owner | General Manager | Manager | Supervisor |
|-------------|:-----:|:---------------:|:-------:|:----------:|
| Fleet P&L | ✅ | ✅ | ❌ | ❌ |
| Vehicle P&L | ✅ | ✅ | ❌ | ❌ |
| Trip P&L | ✅ | ✅ | ❌ | ❌ |
| Cost Analysis | ✅ | ✅ | ❌ | ❌ |
| Driver Costs | ✅ | ✅ | ❌ | ❌ |
| Customer Profitability | ✅ | ✅ | ❌ | ❌ |
| Route Analysis | ✅ | ✅ | ❌ | ❌ |
| Export PDF | ✅ | ✅ | ❌ | ❌ |
| Custom Date Range | ✅ | ✅ | ❌ | ❌ |

### 14.2 Data Visibility

| Data Element | Owner | GM | Manager | Supervisor |
|--------------|:-----:|:--:|:-------:|:----------:|
| Trip Revenue | ✅ | ✅ | ✅ | ✅ |
| Trip Costs | ✅ | ✅ | ✅ | ✅ |
| Vehicle Purchase Price | ✅ | ✅ | ❌ | ❌ |
| EMI Details | ✅ | ✅ | ❌ | ❌ |
| Loan Information | ✅ | ✅ | ❌ | ❌ |
| Driver Salary | ✅ | ✅ | ❌ | ❌ |
| Profit Margins | ✅ | ✅ | ❌ | ❌ |
| Customer Payments | ✅ | ✅ | ✅ | ❌ |

### 14.3 Access Control Implementation

```kotlin
fun canAccessReport(user: User, reportType: ReportType): Boolean {
    return when (user.role) {
        UserRole.OWNER, UserRole.GENERAL_MANAGER -> true
        UserRole.MANAGER, UserRole.SUPERVISOR -> false
    }
}

fun filterDataByRole(user: User, data: ReportData): ReportData {
    return when (user.role) {
        UserRole.OWNER, UserRole.GENERAL_MANAGER -> data
        else -> data.copy(
            purchaseInfo = null,
            emiDetails = null,
            driverSalary = null,
            profitMargins = null
        )
    }
}
```

---

## 15. Data Gaps & Recommendations

### 15.1 Document Costs (Implementation Required)

**Current Issue**: Document costs are NOT tracked in the `documents` table.

**Impact**: Cannot include insurance, permit, and other document-related costs in P&L calculations.

**Recommendation**: Create separate `document_costs` table (preferred for audit trail):

```sql
CREATE TABLE document_costs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT REFERENCES documents(id),
    vehicle_id BIGINT NOT NULL REFERENCES vehicles(id),
    document_type VARCHAR(50) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    payment_mode VARCHAR(20),
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

**OR** Add fields to existing `documents` table:

```sql
ALTER TABLE documents ADD COLUMN cost DECIMAL(12,2) DEFAULT 0;
ALTER TABLE documents ADD COLUMN payment_date TIMESTAMP;
ALTER TABLE documents ADD COLUMN payment_mode VARCHAR(20);
ALTER TABLE documents ADD COLUMN vendor_name VARCHAR(255);
ALTER TABLE documents ADD COLUMN invoice_number VARCHAR(100);
```

### 15.2 Driver-Trip Linkage (Improvement)

**Current Issue**: Driver costs not always linked to specific trips.

**Impact**: Cannot accurately attribute driver costs to trips.

**Recommendation**: Ensure `trip_id` is populated in `driver_costs` when cost is trip-related.

### 15.3 Route Data Normalization (Future Enhancement)

**Current Issue**: Routes are stored as free-text in `start_location` and `end_location`.

**Impact**: Difficult to aggregate route-based analytics.

**Recommendation**: Consider creating normalized route table or use location IDs for better aggregation.

---

## 16. API Endpoints

### 16.1 Existing Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/reports/profit-loss` | GET | Fleet P&L by period |
| `/reports/profit-loss/summary` | GET | P&L summary with date range |
| `/vehicles/{id}/profit-loss` | GET | Single vehicle P&L |
| `/reports/profit-loss/vehicles` | POST | Multi-vehicle P&L |
| `/trips/{id}/profit-loss` | GET | Single trip P&L |
| `/reports/profit-loss/trips` | POST | Multi-trip P&L |
| `/reports/profit-loss/cost-type/{id}` | GET | Single cost type analysis |
| `/reports/profit-loss/cost-types` | POST | Multi cost type analysis |
| `/reports/profit-loss/consolidated` | POST | Combined comprehensive report |

### 16.2 Request Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `vehicle_ids` | Array<Int> | List of vehicle IDs |
| `trip_ids` | Array<Int> | List of trip IDs |
| `cost_ids` | Array<String> | List of cost type IDs |
| `start_date` | String | Start date (DD-MM-YYYY) |
| `end_date` | String | End date (DD-MM-YYYY) |
| `period` | String | daily/weekly/monthly/quarterly/yearly |
| `include_comparison` | Boolean | Include previous period comparison |
| `include_trend` | Boolean | Include historical trend data |

### 16.3 Sample Response Structure

```json
{
  "success": true,
  "message": "Report generated successfully",
  "data": {
    "period": {
      "start_date": "01-01-2026",
      "end_date": "31-01-2026",
      "period_type": "monthly"
    },
    "summary": {
      "total_revenue": 500000,
      "total_expenses": 380000,
      "net_profit": 120000,
      "profit_margin": 24.0,
      "profit_status": "highly_profitable"
    },
    "revenue_breakdown": {
      "trip_payments": 480000,
      "cargo_margin": 20000
    },
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
    "metrics": {
      "total_trips": 25,
      "total_distance_km": 12500,
      "profit_per_km": 9.60,
      "profit_per_trip": 4800,
      "cost_per_km": 30.40,
      "fuel_efficiency_kmpl": 4.5
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
    },
    "cost_breakdown": [
      {
        "cost_id": "TC-001-002",
        "cost_label": "Diesel",
        "group_id": "TC-G-001",
        "amount": 150000,
        "count": 45,
        "percentage": 39.47
      }
    ]
  }
}
```

### 16.4 Proposed Document Costs Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/vehicles/{id}/document-costs` | GET | Get document costs for vehicle |
| `/vehicles/{id}/document-costs` | POST | Add document cost entry |
| `/document-costs/{id}` | GET | Get document cost by ID |
| `/document-costs/{id}` | PUT | Update document cost |
| `/document-costs/{id}` | DELETE | Delete document cost |

---

## Related Documentation

- [Costs Module](../costs/costs-README.md)
- [Reports Module](./reports-README.md)
- [Vehicles Table](../../API_DATABASE_TABLEs/02-vehicles.md)
- [Trip Payments Table](../../API_DATABASE_TABLEs/12-trip-payments.md)
- [Vehicle Purchases Table](../../API_DATABASE_TABLEs/17-vehicle-purchases.md)
- [Vehicle Loan Payments Table](../../API_DATABASE_TABLEs/18-vehicle-loan-payments.md)

---

*Last Updated: 02 February 2026*
