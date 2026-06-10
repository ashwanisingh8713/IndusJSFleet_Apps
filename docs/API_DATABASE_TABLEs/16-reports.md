# Reports & Profit Loss Summaries Tables

## Table: `reports`

Stores generated reports for trip summaries, vehicle performance, profit/loss analysis, and other business metrics.

---

## Schema - Reports

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| `owner_id` | `bigint` | NOT NULL, INDEX | Fleet owner |
| **Report Details** |
| `type` | `varchar(50)` | NOT NULL | Report type |
| `period` | `varchar(20)` | NOT NULL | Report period |
| `title` | `varchar(255)` | NOT NULL | Report title |
| `description` | `text` | | Report description |
| **Date Range** |
| `start_date` | `timestamp` | NOT NULL | Period start |
| `end_date` | `timestamp` | NOT NULL | Period end |
| **Report Data** |
| `data` | `jsonb` | | Full report data (JSON) |
| `summary` | `jsonb` | | Summary data (JSON) |
| **Filters** |
| `vehicle_id` | `bigint` | INDEX | Optional: specific vehicle |
| `driver_id` | `bigint` | INDEX | Optional: specific driver |
| **Generation** |
| `generated_by` | `bigint` | INDEX | User who generated |
| `generated_at` | `timestamp` | | Generation timestamp |
| `expires_at` | `timestamp` | | Report expiry |
| **Timestamps** |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Report Types

```go
const (
    ReportTypeTripSummary       ReportType = "trip_summary"
    ReportTypeVehicleSummary    ReportType = "vehicle_summary"
    ReportTypeDriverPerformance ReportType = "driver_performance"
    ReportTypeProfitLoss        ReportType = "profit_loss"
    ReportTypeFuelConsumption   ReportType = "fuel_consumption"
    ReportTypeMaintenanceCost   ReportType = "maintenance_cost"
    ReportTypeDocumentExpiry    ReportType = "document_expiry"
    ReportTypeFleetUtilization  ReportType = "fleet_utilization"
)
```

---

## Report Periods

```go
const (
    ReportPeriodDaily   ReportPeriod = "daily"
    ReportPeriodWeekly  ReportPeriod = "weekly"
    ReportPeriodMonthly ReportPeriod = "monthly"
    ReportPeriodYearly  ReportPeriod = "yearly"
    ReportPeriodCustom  ReportPeriod = "custom"
)
```

---

## Table: `profit_loss_summaries`

Stores aggregated profit/loss summaries for trips, vehicles, and drivers.

### Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| `owner_id` | `bigint` | NOT NULL, INDEX | Fleet owner |
| **References** |
| `trip_id` | `bigint` | INDEX | Optional: specific trip |
| `vehicle_id` | `bigint` | INDEX | Optional: specific vehicle |
| `driver_id` | `bigint` | INDEX | Optional: specific driver |
| **Period** |
| `period` | `varchar(20)` | | Period type |
| `period_start` | `timestamp` | NOT NULL | Period start date |
| `period_end` | `timestamp` | NOT NULL | Period end date |
| **Revenue** |
| `total_revenue` | `decimal(12,2)` | | Total revenue (₹) |
| `selling_value` | `decimal(12,2)` | | Cargo selling value (₹) |
| `other_income` | `decimal(12,2)` | | Other income (₹) |
| **Costs** |
| `total_cost` | `decimal(12,2)` | | Total costs (₹) |
| `purchase_price` | `decimal(12,2)` | | Cargo purchase cost (₹) |
| `fuel_cost` | `decimal(12,2)` | | Fuel expenses (₹) |
| `toll_cost` | `decimal(12,2)` | | Toll expenses (₹) |
| `driver_cost` | `decimal(12,2)` | | Driver expenses (₹) |
| `maintenance_cost` | `decimal(12,2)` | | Maintenance expenses (₹) |
| `other_cost` | `decimal(12,2)` | | Other expenses (₹) |
| **Profit/Loss** |
| `gross_profit` | `decimal(12,2)` | | Gross profit (₹) |
| `net_profit` | `decimal(12,2)` | | Net profit (₹) |
| `profit_margin` | `decimal(5,2)` | | Profit margin (%) |
| **Metrics** |
| `total_trips` | `int` | | Number of trips |
| `total_distance` | `decimal(12,2)` | | Total distance (km) |
| `avg_profit_per_trip` | `decimal(12,2)` | | Avg profit per trip (₹) |
| `avg_profit_per_km` | `decimal(10,4)` | | Avg profit per km (₹) |
| **Timestamps** |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |

---

## Indexes

```sql
-- Reports
CREATE INDEX idx_reports_owner_id ON reports(owner_id);
CREATE INDEX idx_reports_type ON reports(type);
CREATE INDEX idx_reports_period ON reports(period);
CREATE INDEX idx_reports_generated_at ON reports(generated_at);
CREATE INDEX idx_reports_deleted_at ON reports(deleted_at);

-- Profit Loss Summaries
CREATE INDEX idx_pl_summaries_owner_id ON profit_loss_summaries(owner_id);
CREATE INDEX idx_pl_summaries_trip_id ON profit_loss_summaries(trip_id);
CREATE INDEX idx_pl_summaries_vehicle_id ON profit_loss_summaries(vehicle_id);
CREATE INDEX idx_pl_summaries_driver_id ON profit_loss_summaries(driver_id);
CREATE INDEX idx_pl_summaries_period ON profit_loss_summaries(period_start, period_end);
```

---

## Sample Data

### Profit/Loss Report
```json
{
  "id": 1,
  "owner_id": 1,
  "type": "profit_loss",
  "period": "monthly",
  "title": "Profit/Loss Report - January 2026",
  "start_date": "2026-01-01T00:00:00Z",
  "end_date": "2026-01-31T23:59:59Z",
  "summary": {
    "total_revenue": 4500000.00,
    "total_expenses": 3200000.00,
    "gross_profit": 1300000.00,
    "net_profit": 1150000.00,
    "profit_margin": 25.56,
    "total_trips": 150,
    "completed_trips": 145,
    "avg_revenue_per_trip": 30000.00
  },
  "data": {
    "revenue_breakdown": {
      "trip_revenue": 4350000.00,
      "other_income": 150000.00
    },
    "expense_breakdown": {
      "fuel": 1200000.00,
      "toll": 300000.00,
      "driver": 750000.00,
      "maintenance": 450000.00,
      "other": 500000.00
    },
    "top_vehicles": [...],
    "top_drivers": [...]
  },
  "generated_by": 1,
  "generated_at": "2026-02-01T08:00:00Z"
}
```

### Trip P&L Summary
```json
{
  "id": 1,
  "owner_id": 1,
  "trip_id": 5,
  "vehicle_id": 1,
  "driver_id": 1,
  "period_start": "2026-01-15T00:00:00Z",
  "period_end": "2026-01-15T23:59:59Z",
  "total_revenue": 90000.00,
  "selling_value": 75000.00,
  "other_income": 15000.00,
  "total_cost": 65000.00,
  "purchase_price": 50000.00,
  "fuel_cost": 8950.00,
  "toll_cost": 1500.00,
  "driver_cost": 2000.00,
  "maintenance_cost": 0,
  "other_cost": 2550.00,
  "gross_profit": 25000.00,
  "net_profit": 23000.00,
  "profit_margin": 27.78,
  "total_trips": 1,
  "total_distance": 150.5,
  "avg_profit_per_trip": 23000.00,
  "avg_profit_per_km": 152.82
}
```

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v2/reports/generate` | Generate report | Owner, GM |
| GET | `/api/v2/reports` | List reports | Owner, GM |
| GET | `/api/v2/reports/:id` | Get report | Owner, GM |
| DELETE | `/api/v2/reports/:id` | Delete report | Owner |
| **Financial** |
| GET | `/api/v2/financial/overview` | Financial overview | Owner, GM |
| GET | `/api/v2/financial/profit-loss` | P&L summary | Owner, GM |
| GET | `/api/v2/financial/trips/:id` | Trip P&L | Owner, GM |
| GET | `/api/v2/financial/vehicles/:id` | Vehicle P&L | Owner, GM |
| GET | `/api/v2/financial/cost-breakdown` | Cost breakdown | Owner, GM |

---

## Report Generation

```go
type ReportInput struct {
    Type        ReportType   `json:"type" binding:"required"`
    Period      ReportPeriod `json:"period" binding:"required"`
    StartDate   string       `json:"start_date"` // YYYY-MM-DD
    EndDate     string       `json:"end_date"`   // YYYY-MM-DD
    VehicleID   *uint        `json:"vehicle_id"`
    DriverID    *uint        `json:"driver_id"`
    Title       string       `json:"title"`
    Description string       `json:"description"`
}
```

---

## Financial Overview Response

```go
type FinancialOverview struct {
    Period           string  `json:"period"`
    TotalRevenue     float64 `json:"total_revenue"`
    TotalExpenses    float64 `json:"total_expenses"`
    GrossProfit      float64 `json:"gross_profit"`
    NetProfit        float64 `json:"net_profit"`
    ProfitMargin     float64 `json:"profit_margin"`
    CompletedTrips   int     `json:"completed_trips"`
    PendingPayments  float64 `json:"pending_payments"`
    CollectedPayments float64 `json:"collected_payments"`
    
    // Trends (vs previous period)
    RevenueChange    float64 `json:"revenue_change_percent"`
    ExpenseChange    float64 `json:"expense_change_percent"`
    ProfitChange     float64 `json:"profit_change_percent"`
}
```

---

## Cost Breakdown Response

```go
type CostBreakdown struct {
    Period      string          `json:"period"`
    TotalCost   float64         `json:"total_cost"`
    ByGroup     []GroupCost     `json:"by_group"`
    ByType      []TypeCost      `json:"by_type"`
    ByVehicle   []VehicleCost   `json:"by_vehicle"`
    Trends      []MonthlyTrend  `json:"trends"`
}

type GroupCost struct {
    GroupID    string  `json:"group_id"`
    GroupLabel string  `json:"group_label"`
    Amount     float64 `json:"amount"`
    Percentage float64 `json:"percentage"`
    Count      int     `json:"count"`
}
```

---

## P&L Calculation Logic

```go
// Trip P&L
Revenue = ExpectedTripPrice (or SellingValue if no trip price)
Expenses = SUM(TripCosts) + (AllocatedMaintenanceCost)
GrossProfit = Revenue - PurchasePrice
NetProfit = GrossProfit - Expenses
ProfitMargin = (NetProfit / Revenue) * 100

// Vehicle P&L (Period)
Revenue = SUM(trip.ExpectedTripPrice) for vehicle's trips
TripExpenses = SUM(TripCosts) for vehicle's trips
MaintenanceExpenses = SUM(MaintenanceCosts) for vehicle
TotalExpenses = TripExpenses + MaintenanceExpenses
NetProfit = Revenue - TotalExpenses

// Fleet P&L (Period)
Revenue = SUM(all completed trips' ExpectedTripPrice)
Expenses = SUM(all TripCosts) + SUM(all MaintenanceCosts) + SUM(all DriverCosts)
NetProfit = Revenue - Expenses
```

---

## Dashboard Financial Widget

```json
{
  "financial_summary": {
    "today": {
      "revenue": 250000,
      "expenses": 175000,
      "profit": 75000
    },
    "this_week": {
      "revenue": 1500000,
      "expenses": 1050000,
      "profit": 450000
    },
    "this_month": {
      "revenue": 4500000,
      "expenses": 3200000,
      "profit": 1300000
    }
  },
  "expense_breakdown": [
    {"label": "Fuel", "amount": 1200000, "percentage": 37.5},
    {"label": "Driver", "amount": 750000, "percentage": 23.4},
    {"label": "Maintenance", "amount": 450000, "percentage": 14.1},
    {"label": "Toll", "amount": 300000, "percentage": 9.4},
    {"label": "Other", "amount": 500000, "percentage": 15.6}
  ]
}
```

---

## Notes

1. **Real-time vs Cached**: Dashboard shows real-time; detailed reports can be cached
2. **Role Restriction**: Financial reports restricted to Owner and General Manager
3. **Date Range**: Custom date ranges supported for flexible analysis
4. **Export**: Reports can be exported to PDF/Excel
5. **Scheduled Reports**: Support for automated weekly/monthly report generation

---

*Last Updated: January 2026*

