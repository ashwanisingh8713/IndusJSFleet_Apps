# Vehicle Maintenance Costs Table

## Table: `vehicle_maintenance_costs`

Stores maintenance and repair expense records for vehicles.

---

## Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| `vehicle_id` | `bigint` | NOT NULL, INDEX, FK | Associated vehicle |
| **Cost Identification** |
| `cost_id` | `varchar(20)` | NOT NULL, INDEX | Cost type ID (e.g., "VMC-001-001") |
| `cost_label` | `varchar(100)` | NOT NULL | Human-readable label |
| `group_id` | `varchar(20)` | NOT NULL, INDEX | Group ID (e.g., "VMC-G-001") |
| **Amount & Details** |
| `amount` | `decimal(12,2)` | NOT NULL | Cost amount in INR |
| `date` | `timestamp` | NOT NULL | Date of maintenance |
| `time` | `varchar(10)` | | Time (HH:MM) |
| `description` | `text` | | Work description |
| `notes` | `text` | | Additional notes |
| `vendor_name` | `varchar(255)` | | Service provider name |
| `invoice_no` | `varchar(100)` | | Invoice/bill number |
| **Custom Cost** |
| `custom_cost_label` | `varchar(100)` | | Custom label (when cost_id = "VMC-006-003") |
| **Ownership** |
| `owner_id` | `bigint` | NOT NULL, INDEX | Fleet owner |
| `created_by` | `bigint` | INDEX | User who created |
| **Timestamps** |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Cost Groups & Types

### Group: VMC-G-001 - Regular Maintenance

| Cost ID | Label | Description |
|---------|-------|-------------|
| `VMC-001-001` | Engine Oil Change | Engine oil replacement |
| `VMC-001-002` | Oil Filter | Oil filter replacement |
| `VMC-001-003` | Air Filter | Air filter replacement |
| `VMC-001-004` | Fuel Filter | Fuel filter replacement |
| `VMC-001-005` | Brake Pad | Brake pad replacement |
| `VMC-001-006` | Brake Service | Complete brake service |
| `VMC-001-007` | General Service | Routine vehicle service |
| `VMC-001-008` | Greasing | Chassis greasing |

### Group: VMC-G-002 - Repairs & Replacements

| Cost ID | Label | Description |
|---------|-------|-------------|
| `VMC-002-001` | Clutch Plate | Clutch plate replacement |
| `VMC-002-002` | Clutch Assembly | Complete clutch assembly |
| `VMC-002-003` | Radiator | Radiator repair/replacement |
| `VMC-002-004` | Water Pump | Water pump replacement |
| `VMC-002-005` | Fuel Pump | Fuel pump replacement |
| `VMC-002-006` | Starter Motor | Starter motor repair |
| `VMC-002-007` | Alternator | Alternator repair/replacement |

### Group: VMC-G-003 - Electrical & AC

| Cost ID | Label | Description |
|---------|-------|-------------|
| `VMC-003-001` | Battery | Battery replacement |
| `VMC-003-002` | Wiring | Electrical wiring repair |
| `VMC-003-003` | Lights | Headlight/taillight repair |
| `VMC-003-004` | AC Service | Air conditioning service |
| `VMC-003-005` | AC Gas Refill | AC refrigerant refill |
| `VMC-003-006` | AC Compressor | AC compressor repair |

### Group: VMC-G-004 - Body & Exterior

| Cost ID | Label | Description |
|---------|-------|-------------|
| `VMC-004-001` | Denting | Body denting work |
| `VMC-004-002` | Painting | Body painting |
| `VMC-004-003` | Windshield | Windshield replacement |
| `VMC-004-004` | Mirror | Side mirror replacement |
| `VMC-004-005` | Door Repair | Door repair/replacement |

### Group: VMC-G-005 - Engine & Transmission

| Cost ID | Label | Description |
|---------|-------|-------------|
| `VMC-005-001` | Engine Overhaul | Complete engine overhaul |
| `VMC-005-002` | Cylinder Head | Cylinder head repair |
| `VMC-005-003` | Piston Ring | Piston ring replacement |
| `VMC-005-004` | Gearbox | Gearbox repair |
| `VMC-005-005` | Differential | Differential repair |
| `VMC-005-006` | Propeller Shaft | Propeller shaft repair |
| `VMC-005-007` | Axle Repair | Axle repair/replacement |

### Group: VMC-G-006 - Miscellaneous

| Cost ID | Label | Description |
|---------|-------|-------------|
| `VMC-006-001` | Washing | Vehicle washing/cleaning |
| `VMC-006-002` | Interior Cleaning | Interior deep cleaning |
| `VMC-006-003` | Other | Custom expense (requires `custom_cost_label`) |

### Group: VMC-G-007 - Wheels & Tires

| Cost ID | Label | Description |
|---------|-------|-------------|
| `VMC-007-001` | Tyre Replacement | New tyre purchase |
| `VMC-007-002` | Tyre Repair | Puncture/tyre repair |
| `VMC-007-003` | Wheel Alignment | Wheel alignment service |
| `VMC-007-004` | Wheel Balancing | Wheel balancing |
| `VMC-007-005` | Tube Replacement | Tube replacement |
| `VMC-007-006` | Rim Repair | Wheel rim repair |

---

## Indexes

```sql
CREATE INDEX idx_maintenance_vehicle_id ON vehicle_maintenance_costs(vehicle_id);
CREATE INDEX idx_maintenance_cost_id ON vehicle_maintenance_costs(cost_id);
CREATE INDEX idx_maintenance_group_id ON vehicle_maintenance_costs(group_id);
CREATE INDEX idx_maintenance_owner_id ON vehicle_maintenance_costs(owner_id);
CREATE INDEX idx_maintenance_date ON vehicle_maintenance_costs(date);
CREATE INDEX idx_maintenance_deleted_at ON vehicle_maintenance_costs(deleted_at);
```

---

## Sample Data

### Regular Maintenance
```json
{
  "id": 1,
  "vehicle_id": 1,
  "cost_id": "VMC-001-001",
  "cost_label": "Engine Oil Change",
  "group_id": "VMC-G-001",
  "amount": 4500.00,
  "date": "2026-01-15T10:00:00Z",
  "time": "10:00",
  "description": "Changed engine oil - 15W40 Mobil",
  "vendor_name": "Authorized Service Center",
  "invoice_no": "INV-2026-001234",
  "owner_id": 1,
  "created_by": 1
}
```

### Tyre Replacement
```json
{
  "id": 2,
  "vehicle_id": 1,
  "cost_id": "VMC-007-001",
  "cost_label": "Tyre Replacement",
  "group_id": "VMC-G-007",
  "amount": 12000.00,
  "date": "2026-01-10T14:30:00Z",
  "description": "Replaced 2 front tyres - MRF 295/80R22.5",
  "vendor_name": "MRF Tyre Dealer",
  "invoice_no": "MRF-10012",
  "notes": "Old tyres had 65,000 km",
  "owner_id": 1,
  "created_by": 2
}
```

### Other (Custom) Cost
```json
{
  "id": 3,
  "vehicle_id": 1,
  "cost_id": "VMC-006-003",
  "cost_label": "Other",
  "group_id": "VMC-G-006",
  "amount": 2500.00,
  "date": "2026-01-12T09:00:00Z",
  "custom_cost_label": "Mudguard replacement",
  "description": "Replaced damaged rear mudguard",
  "owner_id": 1,
  "created_by": 1
}
```

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v2/maintenance-costs` | Create maintenance cost | Owner, GM, Manager |
| GET | `/api/v2/maintenance-costs` | List all maintenance costs | Owner, GM |
| GET | `/api/v2/maintenance-costs/:id` | Get cost details | All |
| PUT | `/api/v2/maintenance-costs/:id` | Update cost | Owner, GM, Manager |
| DELETE | `/api/v2/maintenance-costs/:id` | Delete cost | Owner, GM |
| **Vehicle-specific** |
| GET | `/api/v2/vehicles/:id/maintenance-costs` | Get vehicle's costs | All |
| POST | `/api/v2/vehicles/:id/maintenance-costs/bulk` | Bulk add costs | Owner, GM, Manager |
| **Cost Types** |
| GET | `/api/v2/cost-types/maintenance` | Get all maintenance cost types | All |

---

## Maintenance Cost Summary

```go
type MaintenanceCostSummary struct {
    VehicleID         uint    `json:"vehicle_id"`
    TotalCost         float64 `json:"total_cost"`
    MaintenanceCount  int     `json:"maintenance_count"`
    LastMaintenanceAt string  `json:"last_maintenance_at"`
    CostsByGroup      map[string]float64 `json:"costs_by_group"`
}
```

---

## Aggregation Queries

### Total by Group
```sql
SELECT group_id, SUM(amount) as total
FROM vehicle_maintenance_costs
WHERE vehicle_id = ? AND deleted_at IS NULL
GROUP BY group_id;
```

### Monthly Summary
```sql
SELECT 
    DATE_TRUNC('month', date) as month,
    COUNT(*) as maintenance_count,
    SUM(amount) as total_cost
FROM vehicle_maintenance_costs
WHERE vehicle_id = ? 
  AND date >= NOW() - INTERVAL '12 months'
  AND deleted_at IS NULL
GROUP BY DATE_TRUNC('month', date)
ORDER BY month DESC;
```

### Last Maintenance by Type
```sql
SELECT DISTINCT ON (cost_id)
    cost_id, cost_label, date, amount
FROM vehicle_maintenance_costs
WHERE vehicle_id = ?
  AND deleted_at IS NULL
ORDER BY cost_id, date DESC;
```

---

## Integration with Vehicle P&L

Maintenance costs are included in vehicle profit/loss calculations:
- Reduces vehicle profitability
- Tracked separately from trip costs
- Can be allocated to time periods for depreciation analysis

---

## Notes

1. **Cost ID Structure**: `VMC-{group_number}-{item_number}` (e.g., VMC-001-001)
2. **Custom Costs**: When using VMC-006-003, must provide `custom_cost_label`
3. **Vendor Tracking**: Store vendor details for warranty claims
4. **Invoice Storage**: Store invoice number for expense verification
5. **Date Format**: All dates in ISO 8601 format

---

*Last Updated: January 2026*

