# Trip Costs Table

## Table: `trip_costs`

Stores expense entries for trips including fuel, toll, loading/unloading, driver expenses, and other costs.

---

## Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| `trip_id` | `bigint` | NOT NULL, INDEX, FK | Parent trip |
| `vehicle_id` | `bigint` | NOT NULL, INDEX, FK | Associated vehicle |
| `driver_id` | `bigint` | INDEX, FK | Associated driver (optional) |
| **Cost Identification** |
| `cost_id` | `varchar(20)` | NOT NULL, INDEX | Cost type ID (e.g., "TC-001-002") |
| `cost_label` | `varchar(100)` | NOT NULL | Human-readable label (e.g., "Diesel") |
| `group_id` | `varchar(20)` | NOT NULL, INDEX | Group ID (e.g., "TC-G-001") |
| **Amount** |
| `amount` | `decimal(12,2)` | NOT NULL | Cost amount in INR |
| `date` | `timestamp` | NOT NULL | Date of expense |
| `time` | `varchar(10)` | | Time of expense (HH:MM) |
| `notes` | `text` | | Additional notes |
| **Custom Cost** |
| `custom_cost_label` | `varchar(100)` | | Custom label (when cost_id = "TC-006-005") |
| **Fuel Details (Group TC-G-001 only)** |
| `fuel_quantity` | `decimal(10,2)` | | Fuel quantity in liters |
| `fuel_rate` | `decimal(10,2)` | | Rate per liter (₹) |
| `km_per_liter` | `decimal(10,2)` | | Fuel efficiency |
| **Ownership** |
| `owner_id` | `bigint` | NOT NULL, INDEX | Fleet owner |
| `created_by` | `bigint` | INDEX | User who created |
| **Timestamps** |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Cost Groups & Types

### Group: TC-G-001 - Fuel & Energy

| Cost ID | Label | Description |
|---------|-------|-------------|
| `TC-001-001` | Petrol | Petrol fuel cost |
| `TC-001-002` | Diesel | Diesel fuel cost |
| `TC-001-003` | CNG/LPG | CNG or LPG fuel cost |
| `TC-001-004` | Electric Charging | EV charging cost |
| `TC-001-005` | AdBlue/DEF | Diesel exhaust fluid |

**Note**: Fuel costs require additional fields: `fuel_quantity`, `fuel_rate`, `km_per_liter`

### Group: TC-G-002 - Toll & Parking

| Cost ID | Label | Description |
|---------|-------|-------------|
| `TC-002-001` | Highway Toll | Highway toll charges |
| `TC-002-002` | Bridge Toll | Bridge crossing charges |
| `TC-002-003` | Parking Fee | Parking charges |
| `TC-002-004` | Entry Tax | City entry tax |

### Group: TC-G-003 - Loading & Unloading

| Cost ID | Label | Description |
|---------|-------|-------------|
| `TC-003-001` | Loading Charges | Loading labor/equipment |
| `TC-003-002` | Unloading Charges | Unloading labor/equipment |
| `TC-003-003` | Crane Charges | Crane/machinery rental |
| `TC-003-004` | Weighbridge | Weighbridge fees |

### Group: TC-G-004 - Driver Expenses

| Cost ID | Label | Description |
|---------|-------|-------------|
| `TC-004-001` | Driver Food | Driver meals |
| `TC-004-002` | Driver Accommodation | Overnight stay |
| `TC-004-003` | Driver Allowance | Daily allowance |

### Group: TC-G-005 - Permits & Compliance

| Cost ID | Label | Description |
|---------|-------|-------------|
| `TC-005-001` | State Permit | Inter-state permit fees |
| `TC-005-002` | Border Tax | State border tax |
| `TC-005-003` | RTO Charges | RTO-related charges |
| `TC-005-004` | Police Fine | Traffic fine/penalty |

### Group: TC-G-006 - Miscellaneous

| Cost ID | Label | Description |
|---------|-------|-------------|
| `TC-006-001` | Commission | Agent commission |
| `TC-006-002` | Communication | Phone/internet charges |
| `TC-006-003` | Documentation | Paperwork charges |
| `TC-006-004` | Emergency Repair | Minor on-road repairs |
| `TC-006-005` | Other | Custom expense (requires `custom_cost_label`) |

---

## Indexes

```sql
CREATE INDEX idx_trip_costs_trip_id ON trip_costs(trip_id);
CREATE INDEX idx_trip_costs_vehicle_id ON trip_costs(vehicle_id);
CREATE INDEX idx_trip_costs_cost_id ON trip_costs(cost_id);
CREATE INDEX idx_trip_costs_group_id ON trip_costs(group_id);
CREATE INDEX idx_trip_costs_owner_id ON trip_costs(owner_id);
CREATE INDEX idx_trip_costs_date ON trip_costs(date);
CREATE INDEX idx_trip_costs_deleted_at ON trip_costs(deleted_at);
```

---

## Sample Data

### Fuel Cost
```json
{
  "id": 1,
  "trip_id": 1,
  "vehicle_id": 1,
  "driver_id": 1,
  "cost_id": "TC-001-002",
  "cost_label": "Diesel",
  "group_id": "TC-G-001",
  "amount": 8950.00,
  "date": "2026-01-20T10:30:00Z",
  "time": "10:30",
  "fuel_quantity": 100.0,
  "fuel_rate": 89.50,
  "km_per_liter": 4.5,
  "notes": "Filled at HP Pump, Lonavala",
  "owner_id": 1,
  "created_by": 1
}
```

### Toll Cost
```json
{
  "id": 2,
  "trip_id": 1,
  "vehicle_id": 1,
  "cost_id": "TC-002-001",
  "cost_label": "Highway Toll",
  "group_id": "TC-G-002",
  "amount": 750.00,
  "date": "2026-01-20T11:00:00Z",
  "time": "11:00",
  "notes": "Mumbai-Pune Expressway toll",
  "owner_id": 1,
  "created_by": 1
}
```

### Other (Custom) Cost
```json
{
  "id": 3,
  "trip_id": 1,
  "vehicle_id": 1,
  "cost_id": "TC-006-005",
  "cost_label": "Other",
  "group_id": "TC-G-006",
  "amount": 500.00,
  "date": "2026-01-20T14:00:00Z",
  "custom_cost_label": "Tire puncture repair",
  "owner_id": 1,
  "created_by": 1
}
```

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v2/trip-costs` | Create trip cost | Owner, GM, Manager |
| GET | `/api/v2/trip-costs` | List all trip costs | Owner, GM |
| GET | `/api/v2/trip-costs/:id` | Get cost details | All |
| PUT | `/api/v2/trip-costs/:id` | Update cost | Owner, GM, Manager |
| DELETE | `/api/v2/trip-costs/:id` | Delete cost | Owner, GM |
| **Trip-specific** |
| GET | `/api/v2/trips/:id/costs` | Get trip's costs | All |
| POST | `/api/v2/trips/:id/costs/bulk` | Bulk add costs | Owner, GM, Manager |
| **Vehicle-specific** |
| GET | `/api/v2/vehicles/:id/trip-costs` | Get vehicle's trip costs | All |
| **Cost Types** |
| GET | `/api/v2/cost-types/trip` | Get all trip cost types | All |

---

## Response with created_by_user

```json
{
  "id": 1,
  "trip_id": 1,
  "vehicle_id": 1,
  "cost_id": "TC-001-002",
  "cost_label": "Diesel",
  "group_id": "TC-G-001",
  "amount": 8950.00,
  "created_by": 2,
  "created_by_user": {
    "id": 2,
    "first_name": "Rajesh",
    "last_name": "Kumar",
    "role": "manager"
  }
}
```

---

## Aggregation Queries

### Total by Group
```sql
SELECT group_id, SUM(amount) as total
FROM trip_costs
WHERE trip_id = ? AND deleted_at IS NULL
GROUP BY group_id;
```

### Total Fuel Consumption
```sql
SELECT 
    SUM(amount) as total_fuel_cost,
    SUM(fuel_quantity) as total_liters,
    AVG(fuel_rate) as avg_rate,
    AVG(km_per_liter) as avg_mileage
FROM trip_costs
WHERE trip_id = ? 
  AND group_id = 'TC-G-001' 
  AND deleted_at IS NULL;
```

---

## Notes

1. **Cost ID Structure**: `TC-{group_number}-{item_number}` (e.g., TC-001-002 = Fuel Group, Diesel)
2. **Fuel Costs**: Always include `fuel_quantity` and `fuel_rate` for Group TC-G-001
3. **Custom Costs**: When using TC-006-005, must provide `custom_cost_label`
4. **Financial Reports**: Costs are aggregated for P&L calculations
5. **Date Format**: All dates in ISO 8601 format

---

*Last Updated: January 2026*

