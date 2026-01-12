# Financial Overview & Profit/Loss API Plan

## Cost Structure (NEW)

All costs now use structured IDs. See `cost_types.md` for full reference.

| Field | Description | Example |
|-------|-------------|---------|
| `cost_id` | Unique identifier | TC-001-002 |
| `cost_label` | Human-readable label | Diesel |
| `group_id` | Group identifier | TC-G-001 |

**Trip Cost Groups:** TC-G-001 to TC-G-006
**Maintenance Cost Groups:** VMC-G-001 to VMC-G-007

---

## Revenue Calculation (IMPORTANT)

All financial APIs now use `trip_price` as the **sole revenue source**:

```
Revenue = SUM(trip_price) for completed trips
Expenses = SUM(trip_costs) + SUM(maintenance_costs)
Net Profit = Revenue - Expenses
Profit Margin = (Net Profit / Revenue) * 100
```

**Note:** Legacy fields `purchase_price` and `selling_value` are no longer used in P&L calculations.

---

## API Structure

### Dashboard Financial APIs (Quick Summary)
| Endpoint | Purpose | Access |
|----------|---------|--------|
| `GET /dashboard/financial-summary` | Simple KPIs for cards | Owner, GM |
| `GET /dashboard/cost-overview` | Detailed cost breakdown | Owner, GM |
| `GET /dashboard/pending-payments` | Pending payment list | Owner, GM |

### Reports P&L APIs (Detailed Analysis)
| Endpoint | Purpose | Access |
|----------|---------|--------|
| `GET /reports/profit-loss` | Fleet P&L | Owner, GM |
| `GET /trips/:id/profit-loss` | Single trip P&L | Owner, GM |
| `GET /vehicles/:id/profit-loss` | Single vehicle P&L | Owner, GM |
| `POST /reports/profit-loss/vehicles` | Multi-vehicle P&L | Owner, GM |
| `POST /reports/profit-loss/trips` | Multi-trip P&L | Owner, GM |
| `GET /reports/profit-loss/cost-type/:type` | Cost type analysis | Owner, GM |
| `POST /reports/profit-loss/cost-types` | Multi cost type | Owner, GM |
| `POST /reports/profit-loss/consolidated` | Consolidated report | Owner, GM |

---

## New Endpoint: Financial Summary

**Purpose:** Provide simple financial KPIs for dashboard cards

```http
GET /api/v2/dashboard/financial-summary?period=monthly
```

**Response:**
```json
{
    "period": "monthly",
    "start_date": "2025-12-11",
    "end_date": "2026-01-11",
    "total_revenue": 500000.00,
    "total_expenses": 200000.00,
    "trip_costs": 150000.00,
    "maintenance_costs": 50000.00,
    "net_profit": 300000.00,
    "profit_margin": 60.0,
    "profit_status": "profit",
    "pending_payments": 50000.00,
    "received_payments": 450000.00,
    "completed_trips": 45,
    "total_trips": 50,
    "avg_trip_revenue": 11111.11,
    "avg_trip_cost": 4444.44,
    "avg_trip_profit": 6666.67
}
```

---

## Changes Made

### 1. Added `FinancialSummary` struct
Location: `models/report.go`

### 2. Added `GetFinancialSummary` controller
Location: `controllers/report_controller.go`

### 3. Updated P&L calculations to use `trip_price`
- `GetCostOverview` - Now uses trip_price for revenue
- `GetTripProfitLoss` - Returns trip_price instead of selling_value
- `GetVehicleProfitLoss` - Revenue = sum of trip_price
- `GetFleetProfitLoss` - Fleet revenue from trip_price

### 4. Added route
Location: `routes/routes.go`
```go
v2Protected.GET("/dashboard/financial-summary", controllers.GetFinancialSummary)
```

---

## Role Access Matrix

| Endpoint | Owner | GM | Manager | Supervisor |
|----------|-------|-----|---------|------------|
| /dashboard/financial-summary | ✅ | ✅ | ❌ | ❌ |
| /dashboard/cost-overview | ✅ | ✅ | ❌ | ❌ |
| /dashboard/pending-payments | ✅ | ✅ | ❌ | ❌ |
| /reports/profit-loss/* | ✅ | ✅ | ❌ | ❌ |
| /trips/:id/profit-loss | ✅ | ✅ | ❌ | ❌ |
| /vehicles/:id/profit-loss | ✅ | ✅ | ❌ | ❌ |

---

## When to Use Each API

### Dashboard Cards (Mobile Home Screen)
Use: `GET /dashboard/financial-summary`
- Quick overview
- Simple numbers for cards
- Periodic refresh

### Detailed Financial Analysis
Use: `GET /dashboard/cost-overview`
- Cost breakdown by type
- Expense details
- Compare periods

### Entity-Level P&L
Use: `GET /trips/:id/profit-loss` or `GET /vehicles/:id/profit-loss`
- Individual trip/vehicle analysis
- Drill-down from summary

### Reports & Analytics
Use: `POST /reports/profit-loss/consolidated`
- Custom filters
- Multi-entity analysis
- Export/download

---

## Implementation Status

- [x] Add FinancialSummary struct to models/report.go
- [x] Add GetFinancialSummary controller
- [x] Update GetCostOverview to use trip_price
- [x] Update GetTripProfitLoss to use trip_price
- [x] Update GetVehicleProfitLoss to use trip_price
- [x] Update GetFleetProfitLoss to use trip_price
- [x] Add route for /dashboard/financial-summary
- [x] Update Postman collections
- [x] Update API documentation
