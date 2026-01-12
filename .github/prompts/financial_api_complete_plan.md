# Complete Financial API & Cost Management Plan

**Date:** January 12, 2026  
**Version:** 2.0  
**Status:** Implemented

---

## Overview

This document outlines the complete financial API system for the Fleet Management Backend.

---

## Core Revenue Formula

```
Revenue = SUM(trip_price) for completed trips
Trip Costs = SUM(trip_costs) for all trips
Maintenance Costs = SUM(vehicle_maintenance_costs)
Driver Costs = SUM(driver_costs)
Total Expenses = Trip Costs + Maintenance Costs + Driver Costs
Net Profit = Revenue - Total Expenses
Profit Margin = (Net Profit / Revenue) × 100
```

---

## Database Schema

### Tables Overview

| Table | Purpose |
|-------|---------|
| `users` | Owner, GM, Manager, Supervisor accounts |
| `vehicles` | Fleet vehicles |
| `drivers` | Fleet drivers |
| `trips` | Trip records with trip_price |
| `trip_costs` | Trip expenses (fuel, toll, etc.) |
| `vehicle_maintenance_costs` | Vehicle maintenance expenses |
| `driver_costs` | Driver salaries, incentives, deductions |
| `financial_summaries` | Cached P&L summaries with staleness tracking |

### New: driver_costs Table

```sql
CREATE TABLE driver_costs (
    id SERIAL PRIMARY KEY,
    driver_id INT NOT NULL REFERENCES drivers(id),
    trip_id INT REFERENCES trips(id),           -- Optional: link to specific trip
    vehicle_id INT REFERENCES vehicles(id),     -- Optional: link to vehicle
    
    cost_id VARCHAR(20) NOT NULL,               -- DC-001-001
    cost_label VARCHAR(100) NOT NULL,           -- Monthly Salary
    group_id VARCHAR(20) NOT NULL,              -- DC-G-001
    
    amount DECIMAL(12,2) NOT NULL,
    date DATE NOT NULL,
    month VARCHAR(7),                           -- YYYY-MM
    description TEXT,
    notes TEXT,
    custom_cost_label VARCHAR(100),             -- For DC-004-005 (Other)
    is_deduction BOOLEAN DEFAULT FALSE,
    
    owner_id INT NOT NULL,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at TIMESTAMP
);
```

### New: financial_summaries Table (Caching)

```sql
CREATE TABLE financial_summaries (
    id SERIAL PRIMARY KEY,
    owner_id INT NOT NULL,
    period_type VARCHAR(20) NOT NULL,           -- daily, weekly, monthly, yearly
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    
    -- Revenue
    total_revenue DECIMAL(14,2) DEFAULT 0,
    completed_trips INT DEFAULT 0,
    
    -- Expenses
    total_trip_costs DECIMAL(14,2) DEFAULT 0,
    total_maintenance_costs DECIMAL(14,2) DEFAULT 0,
    total_driver_costs DECIMAL(14,2) DEFAULT 0,
    total_expenses DECIMAL(14,2) DEFAULT 0,
    
    -- P/L
    net_profit DECIMAL(14,2) DEFAULT 0,
    profit_margin DECIMAL(5,2) DEFAULT 0,
    profit_status VARCHAR(20) DEFAULT 'break_even',
    
    -- Staleness tracking
    computed_at TIMESTAMP NOT NULL,
    is_stale BOOLEAN DEFAULT FALSE,
    stale_after TIMESTAMP NOT NULL,
    
    UNIQUE(owner_id, period_type, period_start)
);
```

---

## Cost Type Structure

### Trip Costs (TC-G-001 to TC-G-006)

| Group ID | Group Name | Cost IDs |
|----------|------------|----------|
| TC-G-001 | Fuel & Energy | TC-001-001 to TC-001-004 |
| TC-G-002 | Toll & Parking | TC-002-001 to TC-002-003 |
| TC-G-003 | Loading & Unloading | TC-003-001 to TC-003-004 |
| TC-G-004 | Driver Expenses | TC-004-001 to TC-004-003 |
| TC-G-005 | Permits & Compliance | TC-005-001 to TC-005-004 |
| TC-G-006 | Miscellaneous | TC-006-001 to TC-006-005 |

### Maintenance Costs (VMC-G-001 to VMC-G-007)

| Group ID | Group Name |
|----------|------------|
| VMC-G-001 | Regular Maintenance |
| VMC-G-002 | Repairs & Replacements |
| VMC-G-003 | Electrical & AC |
| VMC-G-004 | Body & Exterior |
| VMC-G-005 | Engine & Transmission |
| VMC-G-006 | Miscellaneous |
| VMC-G-007 | Wheels & Tires |

### Driver Costs (DC-G-001 to DC-G-004)

| Group ID | Group Name | Cost IDs |
|----------|------------|----------|
| DC-G-001 | Salary & Wages | DC-001-001 to DC-001-004 |
| DC-G-002 | Incentives & Bonuses | DC-002-001 to DC-002-005 |
| DC-G-003 | Deductions | DC-003-001 to DC-003-005 |
| DC-G-004 | Other | DC-004-001 to DC-004-005 |

---

## API Endpoints

### Cost Type APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/cost-types/trip` | Get trip cost types |
| GET | `/cost-types/maintenance` | Get maintenance cost types |
| GET | `/cost-types/driver` | Get driver cost types |

### Driver Cost APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/drivers/:id/costs` | Create driver cost |
| GET | `/drivers/:id/costs` | List driver costs |
| GET | `/drivers/:id/costs/:costId` | Get driver cost |
| PUT | `/drivers/:id/costs/:costId` | Update driver cost |
| DELETE | `/drivers/:id/costs/:costId` | Delete driver cost |
| POST | `/drivers/:id/costs/bulk` | Bulk create costs |
| GET | `/drivers/:id/financial-summary` | Driver P&L summary |
| GET | `/drivers/:id/earnings` | Driver earnings |

### Dashboard APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/dashboard` | Unified dashboard |
| GET | `/dashboard/financial-summary` | Financial KPIs |
| GET | `/dashboard/cost-overview` | Cost breakdown |
| GET | `/dashboard/pending-payments` | Pending payments |

### Report APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/reports/profit-loss` | Fleet P&L |
| GET | `/trips/:id/profit-loss` | Trip P&L |
| GET | `/vehicles/:id/profit-loss` | Vehicle P&L |
| POST | `/reports/profit-loss/vehicles` | Multi-vehicle P&L |
| POST | `/reports/profit-loss/trips` | Multi-trip P&L |
| POST | `/reports/profit-loss/consolidated` | Consolidated P&L |

---

## Role Access Matrix

| Feature | Owner | GM | Manager | Supervisor |
|---------|-------|-----|---------|------------|
| View Dashboard | ✅ | ✅ | ✅ | ✅ |
| Financial Summary | ✅ | ✅ | ❌ | ❌ |
| Cost Overview | ✅ | ✅ | ❌ | ❌ |
| P&L Reports | ✅ | ✅ | ❌ | ❌ |
| Driver Costs | ✅ | ✅ | ❌ | ❌ |
| Trip Costs (View) | ✅ | ✅ | ✅ | ✅ |
| Trip Costs (Add) | ✅ | ✅ | ✅ | ✅ |
| Maintenance Costs | ✅ | ✅ | ✅ | ✅ |

---

## Cache Strategy

### Real-time with Staleness Flag

1. **Cache Duration:** 5 minutes
2. **Staleness Check:** Before returning cached data
3. **Recompute:** When cache is stale OR on-demand

```go
func GetFinancialSummary() {
    cache := GetCachedSummary(ownerID, period)
    
    if cache != nil && cache.IsCacheValid() {
        return cache  // Return cached data
    }
    
    // Recompute
    summary := ComputeFinancialSummary()
    SaveCache(summary)
    return summary
}
```

---

## CLI Commands

### Drop All Tables (Fresh Start)

```bash
go run main.go --drop-tables
```

⚠️ **WARNING:** This deletes ALL data!

---

## Implementation Status

| Component | Status |
|-----------|--------|
| Driver costs model | ✅ Implemented |
| Driver costs controller | ✅ Implemented |
| Driver cost routes | ✅ Implemented |
| Driver cost types in utils | ✅ Implemented |
| Financial summary cache model | ✅ Implemented |
| DropAllTables function | ✅ Implemented |
| Postman collection | ✅ Created |
| API documentation | ✅ Created |

---

## Files Modified/Created

| File | Action |
|------|--------|
| `models/driver_cost.go` | Created |
| `models/report.go` | Modified (added cache model) |
| `models/trip_cost.go` | Modified (added driver_id) |
| `utils/cost_types.go` | Modified (added driver costs) |
| `controllers/driver_cost_controller.go` | Created |
| `migrations/migrations.go` | Modified (added DropAllTables, new models) |
| `routes/routes.go` | Modified (added driver cost routes) |
| `main.go` | Modified (added --drop-tables flag) |
| `helpers/validation_helpers.go` | Modified (added GetPeriodDates) |
| `.github/postman_collections/12-driver-costs.postman_collection.json` | Created |
| `.github/api_modules/12-driver-costs.md` | Created |
| `.github/copilot-instructions.md` | Modified |

---

**Prepared by:** GitHub Copilot  
**Date:** January 12, 2026

