# Trips & Trip Stops Tables

## Table: `trips`

Stores all trip records including route details, cargo information, pricing, and payment status.

---

## Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| **Identity** |
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| **Assignment** |
| `vehicle_id` | `bigint` | NOT NULL, FK | Assigned vehicle |
| `driver_id` | `bigint` | NOT NULL, FK | Assigned driver |
| **Schedule** |
| `scheduled_date` | `timestamp` | NOT NULL | Scheduled start date |
| `start_time` | `timestamp` | NOT NULL | Planned start time |
| `delivery_date` | `timestamp` | | Expected delivery date |
| `delivery_time` | `timestamp` | | Expected delivery time |
| `planned_start` | `timestamp` | NOT NULL | Planned start datetime |
| `planned_end` | `timestamp` | NOT NULL | Planned end datetime |
| `actual_start` | `timestamp` | | Actual start time |
| `actual_end` | `timestamp` | | Actual end time |
| **Source Location** |
| `start_location` | `varchar(500)` | NOT NULL | Source address |
| `start_lat` | `float` | NOT NULL | Source latitude |
| `start_lng` | `float` | NOT NULL | Source longitude |
| **Destination Location** |
| `end_location` | `varchar(500)` | NOT NULL | Destination address |
| `end_lat` | `float` | NOT NULL | Destination latitude |
| `end_lng` | `float` | NOT NULL | Destination longitude |
| **Current Location** |
| `current_lat` | `float` | | Current latitude |
| `current_lng` | `float` | | Current longitude |
| `last_location_update` | `timestamp` | | Last location update time |
| **Route Information** |
| `number_of_stops` | `int` | DEFAULT 0 | Number of intermediate stops |
| `estimated_distance` | `decimal(10,2)` | | Estimated distance (km) |
| `covered_distance` | `decimal(10,2)` | DEFAULT 0 | Distance covered (km) |
| `actual_distance` | `decimal(10,2)` | | Final distance (km) |
| `covered_duration_minutes` | `int` | DEFAULT 0 | Duration in progress (mins) |
| **Cargo Information** |
| `cargo_type` | `varchar(50)` | DEFAULT 'general' | Cargo type |
| `cargo_description` | `text` | | Cargo description |
| `cargo_loading_weight` | `decimal(10,2)` | | Weight at loading (tons/ft³) |
| `cargo_unloading_weight` | `decimal(10,2)` | | Weight at unloading |
| `vehicle_weight` | `decimal(10,2)` | | Empty vehicle weight |
| `weight_unit` | `varchar(10)` | DEFAULT 'ft3' | Weight unit |
| **Fuel Details** |
| `fuel_type` | `varchar(20)` | DEFAULT 'diesel' | Fuel type |
| `filled_fuel_quantity` | `decimal(10,2)` | | Fuel filled at start (liters) |
| `used_fuel_quantity` | `decimal(10,2)` | | Fuel used (liters) |
| `fuel_rate` | `decimal(10,2)` | | Fuel rate (₹/liter) |
| `km_per_liter` | `decimal(10,2)` | | Fuel efficiency |
| **Pricing** |
| `purchase_price` | `decimal(12,2)` | | Cargo purchase price (₹) |
| `selling_value` | `decimal(12,2)` | | Cargo selling price (₹) |
| `estimated_expense` | `decimal(12,2)` | | Estimated expenses (₹) |
| **Payment Summary Fields** |
| `expected_trip_price` | `decimal(12,2)` | | Price quoted to customer (₹) |
| `paid_trip_price` | `decimal(12,2)` | DEFAULT 0 | Total net amount paid (₹) - auto-calculated from `trip_payments` |
| `payment_count` | `int` | DEFAULT 0 | Number of payments received |
| `is_full_payment_done` | `boolean` | DEFAULT false | Full payment received |
| `is_payment_pending` | `boolean` | DEFAULT true | Payment still pending |
| `payment_status` | `varchar(20)` | DEFAULT 'pending' | pending/partial/full |
| **Feedback** |
| `trip_comment` | `text` | | Customer/trip comment |
| `trip_review` | `text` | | Trip review/feedback |
| **Customer** |
| `customer_id` | `bigint` | INDEX, FK | Reference to Customer |
| `customer_name` | `varchar(255)` | | Legacy: Customer name |
| `customer_contact` | `varchar(100)` | | Legacy: Customer contact |
| `customer_email` | `varchar(255)` | | Legacy: Customer email |
| `customer_address` | `varchar(500)` | | Legacy: Customer address |
| **Other** |
| `priority` | `varchar(50)` | DEFAULT 'normal' | Trip priority |
| `special_instructions` | `text` | | Special instructions |
| `notes` | `text` | | General notes |
| **State & Ownership** |
| `state` | `varchar(50)` | DEFAULT 'planned' | Trip state |
| `owner_id` | `bigint` | NOT NULL, INDEX | Fleet owner |
| `created_by_id` | `bigint` | NOT NULL | User who created |
| **Timestamps** |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Trip States

```go
const (
    TripStatePlanned   TripState = "planned"   // Created but not started
    TripStateOnRoute   TripState = "on_route"  // In progress
    TripStateCompleted TripState = "completed" // Finished successfully
    TripStateCancelled TripState = "cancelled" // Cancelled
    TripStateFailed    TripState = "failed"    // Could not complete
    TripStateDelayed   TripState = "delayed"   // Behind schedule
)
```

---

## State Transitions

```
┌──────────┐                       ┌──────────┐
│ planned  │──────────────────────►│ on_route │
└────┬─────┘                       └────┬─────┘
     │                                  │
     │                                  ├───────────────┐
     │                                  │               │
     ▼                                  ▼               ▼
┌────────────────────────────┐   ┌─────────┐   ┌─────────┐
│        cancelled           │   │ delayed │   │ failed  │
└────────────────────────────┘   └────┬────┘   └─────────┘
                                      │
                                      │
                                      ▼
                               ┌───────────┐
                               │ completed │
                               └───────────┘
```

---

## Payment Status Flow (Auto-calculated from `trip_payments`)

```go
// Called after each payment operation (create/update/delete/clear/bounce)
func UpdateTripPaymentStatus(db *gorm.DB, tripID uint) {
    // Sum only received payments (net_amount after TDS and discounts)
    var result struct {
        NetReceived  float64
        PaymentCount int64
    }
    
    db.Model(&TripPayment{}).
        Select(`
            COALESCE(SUM(CASE WHEN payment_status = 'received' THEN net_amount ELSE 0 END), 0) as net_received,
            COUNT(CASE WHEN payment_status = 'received' THEN 1 END) as payment_count
        `).
        Where("trip_id = ? AND deleted_at IS NULL", tripID).
        Scan(&result)
    
    // Update trip fields
    trip.PaidTripPrice = result.NetReceived
    trip.PaymentCount = int(result.PaymentCount)
    
    // Determine status
    if result.NetReceived >= trip.ExpectedTripPrice && trip.ExpectedTripPrice > 0 {
        trip.PaymentStatus = "full"
        trip.IsFullPaymentDone = true
        trip.IsPaymentPending = false
    } else if result.NetReceived > 0 {
        trip.PaymentStatus = "partial"
        trip.IsFullPaymentDone = false
        trip.IsPaymentPending = true
    } else {
        trip.PaymentStatus = "pending"
        trip.IsFullPaymentDone = false
        trip.IsPaymentPending = true
    }
}
```

---

## ⚠️ Removed Fields (Now in `trip_payments` table)

The following fields were **removed** from `trips` table as individual payments are tracked in `trip_payments`:

| Removed Field | Reason | Use Instead |
|---------------|--------|-------------|
| `paid_amount` | Each payment has its own amount | `trip_payments.amount` |
| `payment_received_date` | Each payment has its own date | `trip_payments.payment_date` |
| `payment_mode` | Each payment can have different mode | `trip_payments.payment_mode` |

**Note**: Use `GET /api/v2/trips/:id/payments` to get all payments for a trip.

---

## Payment Tracking Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                          TRIP                                    │
│  id: 1                                                          │
│  expected_trip_price: ₹1,00,000                                 │
│  paid_trip_price: ₹99,300 (auto-calculated)                     │
│  payment_count: 3                                                │
│  payment_status: "full"                                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ has_many
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      TRIP_PAYMENTS                               │
├─────────────────────────────────────────────────────────────────┤
│  #1: ₹30,000 | cash     | advance | received | 2026-01-15      │
│  #2: ₹40,000 | bank     | partial | received | 2026-01-18      │
│      (TDS: ₹400, Net: ₹39,600)                                  │
│  #3: ₹30,000 | card     | final   | received | 2026-01-25      │
│      (TDS: ₹300, Net: ₹29,700)                                  │
│  ─────────────────────────────────────────────────────────────  │
│  TOTAL: ₹1,00,000 | TDS: ₹700 | NET: ₹99,300                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Cargo Types

| Value | Label |
|-------|-------|
| `general` | General Cargo |
| `gitti` | Gravel/Stone |
| `sand` | Sand |
| `cement` | Cement |
| `steel` | Steel/Metal |
| `coal` | Coal |
| `construction` | Construction Material |
| `agricultural` | Agricultural Products |
| `chemicals` | Chemicals |
| `perishable` | Perishable Goods |
| `electronics` | Electronics |
| `furniture` | Furniture |
| `other` | Other |

---

## Indexes

```sql
CREATE INDEX idx_trips_owner_id ON trips(owner_id);
CREATE INDEX idx_trips_vehicle_id ON trips(vehicle_id);
CREATE INDEX idx_trips_driver_id ON trips(driver_id);
CREATE INDEX idx_trips_customer_id ON trips(customer_id);
CREATE INDEX idx_trips_state ON trips(state);
CREATE INDEX idx_trips_scheduled_date ON trips(scheduled_date);
CREATE INDEX idx_trips_payment_status ON trips(payment_status);
CREATE INDEX idx_trips_deleted_at ON trips(deleted_at);
```

---

## Table: `trip_stops`

Stores intermediate stops for trips.

### Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| `trip_id` | `bigint` | NOT NULL, FK, INDEX | Parent trip |
| `stop_order` | `int` | NOT NULL | Stop sequence number |
| `location` | `varchar(500)` | NOT NULL | Stop address |
| `latitude` | `float` | NOT NULL | Stop latitude |
| `longitude` | `float` | NOT NULL | Stop longitude |
| `arrival_time` | `timestamp` | | Expected/actual arrival |
| `departure_time` | `timestamp` | | Actual departure |
| `stop_duration` | `int` | | Duration in minutes |
| `notes` | `text` | | Stop notes |
| `is_completed` | `boolean` | DEFAULT false | Stop completed |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Sample Trip Data

```json
{
  "id": 1,
  "vehicle_id": 1,
  "driver_id": 1,
  "scheduled_date": "2026-01-20T00:00:00Z",
  "start_time": "2026-01-20T09:00:00Z",
  "planned_start": "2026-01-20T09:00:00Z",
  "planned_end": "2026-01-20T17:00:00Z",
  "start_location": "Warehouse A, Mumbai",
  "start_lat": 19.0760,
  "start_lng": 72.8777,
  "end_location": "Customer Site B, Pune",
  "end_lat": 18.5204,
  "end_lng": 73.8567,
  "estimated_distance": 150.5,
  "cargo_type": "gitti",
  "cargo_loading_weight": 16.5,
  "expected_trip_price": 90000.00,
  "paid_trip_price": 0,
  "payment_status": "pending",
  "customer_id": 1,
  "priority": "high",
  "state": "planned",
  "owner_id": 1
}
```

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v2/trips` | Create trip | Owner, GM, Manager |
| GET | `/api/v2/trips` | List trips | All |
| GET | `/api/v2/trips/:id` | Get trip details | All |
| PUT | `/api/v2/trips/:id` | Update trip | Owner, GM, Manager |
| PATCH | `/api/v2/trips/:id/state` | Update state | Owner, GM, Manager |
| PATCH | `/api/v2/trips/:id/location` | Update location | All |
| PATCH | `/api/v2/trips/:id/progress` | Update progress | All |
| DELETE | `/api/v2/trips/:id` | Cancel trip | Owner, GM, Manager |
| **Payments** |
| GET | `/api/v2/trips/:id/payments` | Get trip's payments | Owner, GM |
| POST | `/api/v2/trips/:id/payments` | Add payment to trip | Owner, GM |
| **Stops** |
| GET | `/api/v2/trips/:id/stops` | Get stops | All |
| POST | `/api/v2/trips/:id/stops` | Add stop | Owner, GM, Manager |
| PUT | `/api/v2/trips/:id/stops/:stop_id` | Update stop | Owner, GM, Manager |
| DELETE | `/api/v2/trips/:id/stops/:stop_id` | Remove stop | Owner, GM, Manager |

---

## Financial Fields Visibility

| Field | Owner | GM | Manager | Supervisor |
|-------|-------|-----|---------|------------|
| `expected_trip_price` | ✅ | ✅ | ❌ | ❌ |
| `paid_trip_price` | ✅ | ✅ | ❌ | ❌ |
| `payment_count` | ✅ | ✅ | ❌ | ❌ |
| `purchase_price` | ✅ | ✅ | ❌ | ❌ |
| `selling_value` | ✅ | ✅ | ❌ | ❌ |
| `payment_status` | ✅ | ✅ | ❌ | ❌ |
| `payments (list)` | ✅ | ✅ | ❌ | ❌ |

---

## Further Considerations

### 1. Removed `assigned` State (January 2026)

The `assigned` state was removed from trip workflow. Trips now transition directly from `planned` → `on_route`.

**Rationale:**
- Simplified workflow reduces confusion
- Vehicle/driver validation happens at trip creation, not as separate "assignment" step
- Fewer states = simpler mobile client implementation

**Migration Required:**
If existing trips have `state='assigned'`, run the following SQL migration:
```sql
UPDATE trips SET state='planned' WHERE state='assigned';
```

**State Transition Changes:**
| From | To (Old) | To (New) |
|------|----------|----------|
| `planned` | `assigned`, `cancelled` | `on_route`, `cancelled` |
| `assigned` | `on_route`, `cancelled` | *(removed)* |

### 2. Database Migration for Existing Data

Before deployment, ensure no trips exist with `state='assigned'`:
```sql
-- Check for existing assigned trips
SELECT COUNT(*) FROM trips WHERE state = 'assigned';

-- Migrate assigned to planned
UPDATE trips SET state = 'planned' WHERE state = 'assigned';
```

### 3. Client Impact

- **Mobile App**: Update to remove handling of `assigned` state
- **Dashboard**: Remove `assigned` count display from trips status
- **API Response**: `GET /api/v2/dashboard/trips-status` no longer includes `assigned` field

---

## Notes

1. **Customer Association**: Use `customer_id` (preferred) or legacy inline fields
2. **Distance**: `estimated_distance` provided at creation, `actual_distance` at completion
3. **Partial Payments**: All payments tracked in `trip_payments` table with full details
4. **Payment Summary**: Trip's `paid_trip_price` is auto-calculated from `trip_payments` (sum of `net_amount` where status='received')
5. **TDS Tracking**: TDS deductions tracked per payment in `trip_payments.tds_amount`
6. **State Changes**: Only valid transitions allowed, tracked in audit logs
7. **Financial Data**: Hidden from Manager and Supervisor roles
8. **Receipt Numbers**: Each payment gets unique receipt number (e.g., RP-202601-001-00042)

---

*Last Updated: January 2026*

