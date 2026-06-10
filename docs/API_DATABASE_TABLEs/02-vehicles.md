# Vehicles Table

## Table: `vehicles`

Stores all fleet vehicles registered in the system.

---

## Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `registration_number` | `varchar(50)` | UNIQUE, NOT NULL | Vehicle registration number |
| `make` | `varchar(100)` | | Vehicle manufacturer (e.g., Tata, Mahindra) |
| `model` | `varchar(100)` | | Vehicle model |
| `year` | `int` | | Year of manufacture |
| `vehicle_type` | `varchar(50)` | | Type (truck, trailer, car, etc.) |
| `fuel_type` | `varchar(50)` | | Fuel type (diesel, petrol, cng, electric) |
| `capacity` | `decimal(10,2)` | | Load capacity |
| `color` | `varchar(50)` | | Vehicle color |
| `mileage` | `decimal(10,2)` | DEFAULT 0 | Current odometer reading (km) |
| `assigned_driver_id` | `bigint` | FK → drivers.id | Permanently assigned driver |
| `caretaker_id` | `bigint` | INDEX, FK → users.id | Manager/Supervisor responsible |
| `state` | `varchar(50)` | DEFAULT 'active' | Vehicle state |
| `owner_id` | `bigint` | NOT NULL, INDEX | Fleet owner |
| `registered_by_id` | `bigint` | NOT NULL | User who registered vehicle |
| `updated_by_id` | `bigint` | INDEX | User who last updated |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Vehicle States

```go
const (
    VehicleStateInactive       VehicleState = "inactive"       // Not in use
    VehicleStateActive         VehicleState = "active"         // Available for assignment
    VehicleStateOnRoute        VehicleState = "on_route"       // Currently on trip
    VehicleStateMaintenance    VehicleState = "maintenance"    // Under maintenance
    VehicleStateDamaged        VehicleState = "damaged"        // Needs repair
    VehicleStateDecommissioned VehicleState = "decommissioned" // Permanently out
)
```

---

## State Transitions

```
┌──────────┐
│ inactive │◄──────────────────────────────────────┐
└────┬─────┘                                       │
     │                                             │
     ▼                                             │
┌──────────┐     ┌──────────┐     ┌───────────┐   │
│  active  │────►│ on_route │────►│  damaged  │───┤
└────┬─────┘     └────┬─────┘     └─────┬─────┘   │
     │                │                 │         │
     │                │                 ▼         │
     │                │          ┌─────────────┐  │
     ├────────────────┼─────────►│ maintenance │──┘
     │                │          └─────────────┘
     │                │
     ▼                ▼
┌────────────────────────┐
│    decommissioned     │ (Terminal)
└────────────────────────┘
```

---

## Indexes

```sql
CREATE UNIQUE INDEX idx_vehicles_registration ON vehicles(registration_number);
CREATE INDEX idx_vehicles_owner_id ON vehicles(owner_id);
CREATE INDEX idx_vehicles_caretaker_id ON vehicles(caretaker_id);
CREATE INDEX idx_vehicles_state ON vehicles(state);
CREATE INDEX idx_vehicles_deleted_at ON vehicles(deleted_at);
```

---

## Relationships

| Relation | Type | Target Table | Foreign Key |
|----------|------|--------------|-------------|
| Owner | belongs_to | users | owner_id |
| RegisteredBy | belongs_to | users | registered_by_id |
| Caretaker | belongs_to | users | caretaker_id |
| AssignedDriver | belongs_to | drivers | assigned_driver_id |
| Documents | has_many | documents | vehicle_id |
| Locations | has_many | locations | vehicle_id |
| Trips | has_many | trips | vehicle_id |
| MaintenanceCosts | has_many | vehicle_maintenance_costs | vehicle_id |
| FuelRecords | has_many | fuel_records | vehicle_id |
| Purchase | has_one | vehicle_purchases | vehicle_id |
| LoanPayments | has_many | vehicle_loan_payments | vehicle_id |

---

## Sample Data

```json
{
  "id": 1,
  "registration_number": "MH12AB1234",
  "make": "Tata",
  "model": "Prima",
  "year": 2023,
  "vehicle_type": "truck",
  "fuel_type": "diesel",
  "capacity": 25.0,
  "color": "white",
  "mileage": 45000.50,
  "assigned_driver_id": 1,
  "caretaker_id": 2,
  "state": "active",
  "owner_id": 1,
  "registered_by_id": 1,
  "created_at": "2026-01-01T00:00:00Z"
}
```

---

## Vehicle Types

| Value | Label | Description |
|-------|-------|-------------|
| `truck` | Truck | Heavy goods vehicle |
| `trailer` | Trailer | Semi-trailer truck |
| `tanker` | Tanker | Liquid transport |
| `container` | Container | Container truck |
| `car` | Car | Passenger vehicle |
| `van` | Van | Light commercial |
| `pickup` | Pickup | Pickup truck |
| `bus` | Bus | Passenger bus |

---

## Fuel Types

| Value | Label |
|-------|-------|
| `diesel` | Diesel |
| `petrol` | Petrol |
| `cng` | CNG |
| `electric` | Electric |

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v2/vehicles` | Register vehicle | Owner, GM, Manager |
| GET | `/api/v2/vehicles` | List vehicles | All |
| GET | `/api/v2/vehicles/:id` | Get vehicle details | All |
| PUT | `/api/v2/vehicles/:id` | Update vehicle | Owner, GM, Manager |
| PATCH | `/api/v2/vehicles/:id/state` | Update state | Owner, GM, Manager |
| DELETE | `/api/v2/vehicles/:id` | Disable vehicle | Owner only |
| GET | `/api/v2/vehicles/:id/trips` | Get vehicle trips | All |
| GET | `/api/v2/vehicles/:id/documents` | Get documents | All |
| GET | `/api/v2/vehicles/:id/locations` | Get location history | All |
| GET | `/api/v2/vehicles/:id/maintenance-costs` | Get maintenance costs | All |
| POST | `/api/v2/vehicles/:id/maintenance-costs/bulk` | Bulk add costs | Owner, GM, Manager |
| POST | `/api/v2/vehicles/:id/purchase` | Record purchase details | Owner, GM |
| GET | `/api/v2/vehicles/:id/purchase` | Get purchase details | Owner, GM |
| PUT | `/api/v2/vehicles/:id/purchase` | Update purchase | Owner, GM |
| GET | `/api/v2/vehicles/:id/loan-summary` | Get loan summary | Owner, GM |
| GET | `/api/v2/vehicles/:id/loan-payments` | Get EMI history | Owner, GM |

---

## Notes

1. **Registration Number**: Must be unique across entire system
2. **Caretaker**: Manager/Supervisor responsible for the vehicle
3. **State Management**: State changes are tracked in audit logs
4. **Soft Delete**: Vehicles are disabled, not deleted
5. **Mileage Tracking**: Updated automatically from trips and fuel records

---

*Last Updated: January 2026*

