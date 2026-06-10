# Driver Costs Table

## Table: `driver_costs`

Stores cost entries for drivers including salary, incentives, bonuses, and deductions.

---

## Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| `driver_id` | `bigint` | NOT NULL, INDEX, FK | Associated driver |
| `trip_id` | `bigint` | INDEX, FK | Optional: linked trip |
| `vehicle_id` | `bigint` | INDEX, FK | Optional: linked vehicle |
| **Cost Identification** |
| `cost_id` | `varchar(20)` | NOT NULL, INDEX | Cost type ID (e.g., "DC-001-001") |
| `cost_label` | `varchar(100)` | NOT NULL | Human-readable label |
| `group_id` | `varchar(20)` | NOT NULL, INDEX | Group ID (e.g., "DC-G-001") |
| **Amount & Details** |
| `amount` | `decimal(12,2)` | NOT NULL | Cost amount in INR |
| `date` | `timestamp` | NOT NULL | Date of transaction |
| `month` | `varchar(7)` | INDEX | Period (YYYY-MM) for monthly costs |
| `description` | `text` | | Description |
| `notes` | `text` | | Additional notes |
| **Custom Cost** |
| `custom_cost_label` | `varchar(100)` | | Custom label (when cost_id = "DC-004-005") |
| **Deduction Flag** |
| `is_deduction` | `boolean` | DEFAULT false | True if this is a deduction |
| **Ownership** |
| `owner_id` | `bigint` | NOT NULL, INDEX | Fleet owner |
| `created_by` | `bigint` | INDEX | User who created |
| **Timestamps** |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Cost Groups & Types

### Group: DC-G-001 - Salary & Wages

| Cost ID | Label | Description |
|---------|-------|-------------|
| `DC-001-001` | Monthly Salary | Regular monthly salary |
| `DC-001-002` | Daily Wages | Per-day wages |
| `DC-001-003` | Overtime Pay | Overtime compensation |
| `DC-001-004` | Night Shift Allowance | Night driving allowance |

### Group: DC-G-002 - Incentives & Bonuses

| Cost ID | Label | Description |
|---------|-------|-------------|
| `DC-002-001` | Trip Bonus | Per-trip completion bonus |
| `DC-002-002` | Performance Bonus | Performance-based bonus |
| `DC-002-003` | Safety Bonus | Safe driving bonus |
| `DC-002-004` | Fuel Saving Bonus | Fuel efficiency bonus |
| `DC-002-005` | Festival Bonus | Festival/Diwali bonus |
| `DC-002-006` | Yearly Bonus | Annual bonus |

### Group: DC-G-003 - Deductions

| Cost ID | Label | Is Deduction | Description |
|---------|-------|--------------|-------------|
| `DC-003-001` | Advance Recovery | ✅ | Salary advance recovery |
| `DC-003-002` | Damage Deduction | ✅ | Vehicle damage recovery |
| `DC-003-003` | Loan EMI | ✅ | Loan installment |
| `DC-003-004` | Penalty | ✅ | Fine/penalty deduction |
| `DC-003-005` | Late Arrival | ✅ | Late arrival penalty |
| `DC-003-006` | Absent Deduction | ✅ | Absence deduction |

### Group: DC-G-004 - Other

| Cost ID | Label | Description |
|---------|-------|-------------|
| `DC-004-001` | Expense Reimbursement | Expense reimbursement |
| `DC-004-002` | Travel Allowance | Travel allowance |
| `DC-004-003` | Mobile Allowance | Phone allowance |
| `DC-004-004` | Uniform Allowance | Uniform/clothing allowance |
| `DC-004-005` | Other | Custom (requires `custom_cost_label`) |

---

## Net Earnings Calculation

```go
// Net Earnings = Salary + Incentives - Deductions + Other
type DriverEarnings struct {
    DriverID      uint    `json:"driver_id"`
    Period        string  `json:"period"`          // YYYY-MM
    TotalSalary   float64 `json:"total_salary"`    // Group DC-G-001
    TotalIncentive float64 `json:"total_incentive"` // Group DC-G-002
    TotalDeduction float64 `json:"total_deduction"` // Group DC-G-003
    TotalOther    float64 `json:"total_other"`     // Group DC-G-004
    NetEarnings   float64 `json:"net_earnings"`    // Calculated
    TripCount     int     `json:"trip_count"`
}

// Calculation
NetEarnings = TotalSalary + TotalIncentive - TotalDeduction + TotalOther
```

---

## Indexes

```sql
CREATE INDEX idx_driver_costs_driver_id ON driver_costs(driver_id);
CREATE INDEX idx_driver_costs_trip_id ON driver_costs(trip_id);
CREATE INDEX idx_driver_costs_cost_id ON driver_costs(cost_id);
CREATE INDEX idx_driver_costs_group_id ON driver_costs(group_id);
CREATE INDEX idx_driver_costs_month ON driver_costs(month);
CREATE INDEX idx_driver_costs_owner_id ON driver_costs(owner_id);
CREATE INDEX idx_driver_costs_deleted_at ON driver_costs(deleted_at);
```

---

## Sample Data

### Monthly Salary
```json
{
  "id": 1,
  "driver_id": 1,
  "cost_id": "DC-001-001",
  "cost_label": "Monthly Salary",
  "group_id": "DC-G-001",
  "amount": 25000.00,
  "date": "2026-01-01T00:00:00Z",
  "month": "2026-01",
  "description": "January 2026 Salary",
  "is_deduction": false,
  "owner_id": 1,
  "created_by": 1
}
```

### Trip Bonus (Linked to Trip)
```json
{
  "id": 2,
  "driver_id": 1,
  "trip_id": 5,
  "vehicle_id": 1,
  "cost_id": "DC-002-001",
  "cost_label": "Trip Bonus",
  "group_id": "DC-G-002",
  "amount": 500.00,
  "date": "2026-01-15T00:00:00Z",
  "description": "Bonus for Trip #5 completion",
  "is_deduction": false,
  "owner_id": 1,
  "created_by": 1
}
```

### Advance Recovery (Deduction)
```json
{
  "id": 3,
  "driver_id": 1,
  "cost_id": "DC-003-001",
  "cost_label": "Advance Recovery",
  "group_id": "DC-G-003",
  "amount": 5000.00,
  "date": "2026-01-01T00:00:00Z",
  "month": "2026-01",
  "description": "Salary advance recovery (₹25,000 loan - EMI 5/5)",
  "is_deduction": true,
  "owner_id": 1,
  "created_by": 1
}
```

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v2/driver-costs` | Create driver cost | Owner, GM |
| GET | `/api/v2/driver-costs` | List all driver costs | Owner, GM |
| GET | `/api/v2/driver-costs/:id` | Get cost details | Owner, GM |
| PUT | `/api/v2/driver-costs/:id` | Update cost | Owner, GM |
| DELETE | `/api/v2/driver-costs/:id` | Delete cost | Owner, GM |
| **Driver-specific** |
| GET | `/api/v2/drivers/:id/costs` | Get driver's costs | Owner, GM |
| GET | `/api/v2/drivers/:id/earnings` | Get earnings summary | Owner, GM |
| POST | `/api/v2/drivers/:id/costs/bulk` | Bulk add costs | Owner, GM |
| **Cost Types** |
| GET | `/api/v2/cost-types/driver` | Get all driver cost types | Owner, GM |

---

## Permission Restrictions

Driver costs are **sensitive financial data** and restricted:

| Action | Owner | GM | Manager | Supervisor |
|--------|-------|-----|---------|------------|
| View | ✅ | ✅ | ❌ | ❌ |
| Create | ✅ | ✅ | ❌ | ❌ |
| Update | ✅ | ✅ | ❌ | ❌ |
| Delete | ✅ | ✅ | ❌ | ❌ |

---

## Monthly Earnings Report

```go
type MonthlyDriverReport struct {
    DriverID      uint           `json:"driver_id"`
    DriverName    string         `json:"driver_name"`
    Month         string         `json:"month"`
    Breakdown     []CostBreakdown `json:"breakdown"`
    Summary       EarningsSummary `json:"summary"`
}

type CostBreakdown struct {
    GroupID    string  `json:"group_id"`
    GroupLabel string  `json:"group_label"`
    Total      float64 `json:"total"`
    Items      []CostItem `json:"items"`
}

type EarningsSummary struct {
    GrossEarnings  float64 `json:"gross_earnings"`   // Salary + Incentives + Other
    TotalDeductions float64 `json:"total_deductions"` // All deductions
    NetEarnings    float64 `json:"net_earnings"`     // Gross - Deductions
    TripCount      int     `json:"trip_count"`
}
```

---

## Trip-wise Driver Cost

When a driver cost is linked to a trip (`trip_id` is set), it:
- Appears in trip cost breakdown
- Affects trip P&L calculations
- Shows in driver's trip history

```sql
-- Get all trip-related costs for a driver
SELECT dc.*, t.start_location, t.end_location
FROM driver_costs dc
JOIN trips t ON dc.trip_id = t.id
WHERE dc.driver_id = ?
  AND dc.trip_id IS NOT NULL
  AND dc.deleted_at IS NULL
ORDER BY dc.date DESC;
```

---

## Notes

1. **Cost ID Structure**: `DC-{group_number}-{item_number}` (e.g., DC-001-001)
2. **Deductions**: Amounts in DC-G-003 are subtracted from earnings
3. **Trip Linking**: Optional - link costs to specific trips for detailed tracking
4. **Month Format**: Use YYYY-MM for monthly aggregation
5. **Restricted Access**: Only Owner and General Manager can access driver costs

---

*Last Updated: January 2026*

