# Fuel Records Table

## Table: `fuel_records`

Stores detailed fuel consumption records for vehicles.

---

## Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| `vehicle_id` | `bigint` | NOT NULL, INDEX, FK | Associated vehicle |
| `trip_id` | `bigint` | INDEX, FK | Optional: linked trip |
| `driver_id` | `bigint` | INDEX, FK | Optional: driver who filled |
| **Fuel Details** |
| `fuel_type` | `varchar(20)` | NOT NULL | diesel, petrol, cng, electric |
| `quantity` | `decimal(10,2)` | NOT NULL | Fuel quantity (liters/kWh) |
| `price_per_unit` | `decimal(10,2)` | NOT NULL | Rate per unit (₹) |
| `total_cost` | `decimal(12,2)` | NOT NULL | Total amount paid (₹) |
| **Odometer** |
| `odometer_reading` | `decimal(12,2)` | | Current odometer (km) |
| **Location** |
| `fuel_station` | `varchar(255)` | | Station name |
| `location` | `varchar(500)` | | Station address |
| `latitude` | `float` | | Station latitude |
| `longitude` | `float` | | Station longitude |
| **Transaction** |
| `filled_at` | `timestamp` | NOT NULL | Fill timestamp |
| `receipt_no` | `varchar(100)` | | Receipt number |
| `payment_mode` | `varchar(20)` | | cash, card, upi, credit |
| `notes` | `text` | | Additional notes |
| **Ownership** |
| `owner_id` | `bigint` | NOT NULL, INDEX | Fleet owner |
| `created_by` | `bigint` | INDEX | User who recorded |
| **Timestamps** |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Fuel Types

| Value | Label | Unit |
|-------|-------|------|
| `diesel` | Diesel | Liters |
| `petrol` | Petrol | Liters |
| `cng` | CNG | Kg |
| `electric` | Electric | kWh |

---

## Indexes

```sql
CREATE INDEX idx_fuel_records_vehicle_id ON fuel_records(vehicle_id);
CREATE INDEX idx_fuel_records_trip_id ON fuel_records(trip_id);
CREATE INDEX idx_fuel_records_owner_id ON fuel_records(owner_id);
CREATE INDEX idx_fuel_records_filled_at ON fuel_records(filled_at);
CREATE INDEX idx_fuel_records_deleted_at ON fuel_records(deleted_at);
```

---

## Sample Data

```json
{
  "id": 1,
  "vehicle_id": 1,
  "trip_id": 5,
  "driver_id": 1,
  "fuel_type": "diesel",
  "quantity": 100.00,
  "price_per_unit": 89.50,
  "total_cost": 8950.00,
  "odometer_reading": 45000.00,
  "fuel_station": "HP Petrol Pump - Lonavala",
  "location": "Mumbai-Pune Expressway, Lonavala",
  "latitude": 18.7481,
  "longitude": 73.4072,
  "filled_at": "2026-01-15T10:30:00Z",
  "receipt_no": "HP-001234567",
  "payment_mode": "card",
  "notes": "Full tank",
  "owner_id": 1,
  "created_by": 1
}
```

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v2/fuel-records` | Create fuel record | Owner, GM, Manager |
| GET | `/api/v2/fuel-records` | List fuel records | Owner, GM |
| GET | `/api/v2/fuel-records/:id` | Get record details | All |
| PUT | `/api/v2/fuel-records/:id` | Update record | Owner, GM, Manager |
| DELETE | `/api/v2/fuel-records/:id` | Delete record | Owner, GM |
| **Vehicle-specific** |
| GET | `/api/v2/vehicles/:id/fuel-records` | Get vehicle's records | All |
| GET | `/api/v2/vehicles/:id/fuel-summary` | Get fuel summary | All |

---

## Fuel Consumption Summary

```go
type FuelConsumptionSummary struct {
    VehicleID           uint    `json:"vehicle_id"`
    TotalQuantity       float64 `json:"total_quantity"`
    TotalCost           float64 `json:"total_cost"`
    AveragePricePerUnit float64 `json:"average_price_per_unit"`
    TotalDistance       float64 `json:"total_distance"`
    AverageMileage      float64 `json:"average_mileage"`
    RecordCount         int     `json:"record_count"`
    Period              string  `json:"period"`
}
```

### Summary Response

```json
{
  "vehicle_id": 1,
  "total_quantity": 5000.00,
  "total_cost": 447500.00,
  "average_price_per_unit": 89.50,
  "total_distance": 22500.00,
  "average_mileage": 4.50,
  "record_count": 50,
  "period": "monthly"
}
```

---

## Mileage Calculation

```sql
-- Calculate average mileage between fills
WITH fuel_with_prev AS (
    SELECT 
        id,
        vehicle_id,
        quantity,
        odometer_reading,
        LAG(odometer_reading) OVER (
            PARTITION BY vehicle_id 
            ORDER BY filled_at
        ) as prev_odometer
    FROM fuel_records
    WHERE vehicle_id = ?
      AND odometer_reading IS NOT NULL
      AND deleted_at IS NULL
)
SELECT 
    vehicle_id,
    AVG(
        CASE 
            WHEN prev_odometer IS NOT NULL 
            THEN (odometer_reading - prev_odometer) / quantity
            ELSE NULL
        END
    ) as avg_mileage_km_per_liter
FROM fuel_with_prev
GROUP BY vehicle_id;
```

---

## Fuel Cost Analysis

### Monthly Fuel Cost
```sql
SELECT 
    vehicle_id,
    DATE_TRUNC('month', filled_at) as month,
    SUM(quantity) as total_liters,
    SUM(total_cost) as total_cost,
    AVG(price_per_unit) as avg_rate
FROM fuel_records
WHERE owner_id = ?
  AND filled_at >= NOW() - INTERVAL '12 months'
  AND deleted_at IS NULL
GROUP BY vehicle_id, DATE_TRUNC('month', filled_at)
ORDER BY month DESC;
```

### Fuel Type Distribution
```sql
SELECT 
    fuel_type,
    COUNT(*) as fill_count,
    SUM(quantity) as total_quantity,
    SUM(total_cost) as total_cost
FROM fuel_records
WHERE owner_id = ?
  AND deleted_at IS NULL
GROUP BY fuel_type;
```

---

## Difference from Trip Cost - Fuel

| Aspect | Fuel Record | Trip Cost (Fuel) |
|--------|-------------|------------------|
| **Purpose** | Detailed fuel tracking | Trip expense record |
| **Fields** | Full details (station, GPS) | Basic (amount, quantity, rate) |
| **Trip Link** | Optional | Required |
| **Use Case** | Fleet fuel management | Trip P&L calculation |
| **Analytics** | Mileage, efficiency | Cost breakdown |

**Note**: Both can coexist - Fuel Records for detailed tracking, Trip Costs for expense allocation.

---

## Integration with Trip

When filling fuel during a trip:

```json
POST /api/v2/fuel-records
{
  "vehicle_id": 1,
  "trip_id": 5,
  "driver_id": 1,
  "fuel_type": "diesel",
  "quantity": 100.00,
  "price_per_unit": 89.50,
  "total_cost": 8950.00,
  "odometer_reading": 45000.00,
  "fuel_station": "HP Pump",
  "filled_at": "2026-01-15T10:30:00Z"
}
```

This automatically:
1. Creates a fuel record
2. Can optionally create a trip cost entry
3. Updates vehicle mileage

---

## Notes

1. **Odometer Tracking**: Essential for accurate mileage calculation
2. **Trip Linking**: Link to trip for expense attribution
3. **GPS Location**: Helps verify fill location and detect fraud
4. **Receipt Storage**: Receipt number for expense verification
5. **Payment Mode**: Track payment methods for reconciliation

---

*Last Updated: January 2026*

