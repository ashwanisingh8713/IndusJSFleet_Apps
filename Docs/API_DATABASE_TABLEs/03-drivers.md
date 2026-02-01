# Drivers Table

## Table: `drivers`

Stores all driver profiles in the fleet management system.

---

## Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `first_name` | `varchar(100)` | NOT NULL | Driver's first name |
| `last_name` | `varchar(100)` | NOT NULL | Driver's last name |
| `email` | `varchar(255)` | UNIQUE | Email address (optional) |
| `mobile` | `varchar(20)` | UNIQUE, NOT NULL | Mobile phone number |
| `license_number` | `varchar(50)` | UNIQUE, NOT NULL | Driving license number |
| `license_expiry` | `timestamp` | NOT NULL | License expiry date |
| `license_type` | `varchar(50)` | | License type (LMV, HMV, etc.) |
| `date_of_birth` | `timestamp` | | Date of birth |
| `address` | `varchar(500)` | | Residential address |
| `emergency_contact` | `varchar(20)` | | Emergency contact number |
| `blood_group` | `varchar(10)` | | Blood group |
| `profile_photo` | `varchar(255)` | | Profile photo URL/path |
| `status` | `varchar(50)` | DEFAULT 'active' | Driver status |
| `owner_id` | `bigint` | INDEX, NOT NULL | Fleet owner |
| `created_by_id` | `bigint` | INDEX, NOT NULL | User who added driver |
| `caretaker_id` | `bigint` | INDEX | Manager/Supervisor responsible |
| `updated_by_id` | `bigint` | INDEX | User who last updated |
| `joining_date` | `timestamp` | | Employment start date |
| `is_active` | `boolean` | DEFAULT true | Active status |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Driver Statuses

```go
const (
    DriverStatusInactive   DriverStatus = "inactive"   // Disabled/not working
    DriverStatusActive     DriverStatus = "active"     // Available for assignment
    DriverStatusOnRoute    DriverStatus = "on_route"   // Currently on trip
    DriverStatusOnLeave    DriverStatus = "on_leave"   // On approved leave
    DriverStatusSuspended  DriverStatus = "suspended"  // Privileges suspended
    DriverStatusTerminated DriverStatus = "terminated" // Employment ended
)
```

---

## Status Transitions

```
┌──────────┐
│ inactive │◄──────────────────────────────────────┐
└────┬─────┘                                       │
     │                                             │
     ▼                                             │
┌──────────┐     ┌──────────┐                     │
│  active  │────►│ on_route │─────────────────────┤
└────┬─────┘     └──────────┘                     │
     │                                             │
     ├───────────────────────────┐                │
     │                           │                │
     ▼                           ▼                │
┌──────────┐               ┌───────────┐         │
│ on_leave │               │ suspended │─────────┤
└────┬─────┘               └─────┬─────┘         │
     │                           │                │
     └───────────────────────────┴────────────────┘
                                 │
                                 ▼
                          ┌────────────┐
                          │ terminated │ (Terminal)
                          └────────────┘
```

---

## Indexes

```sql
CREATE UNIQUE INDEX idx_drivers_email ON drivers(email) WHERE email IS NOT NULL;
CREATE UNIQUE INDEX idx_drivers_mobile ON drivers(mobile);
CREATE UNIQUE INDEX idx_drivers_license ON drivers(license_number);
CREATE INDEX idx_drivers_owner_id ON drivers(owner_id);
CREATE INDEX idx_drivers_caretaker_id ON drivers(caretaker_id);
CREATE INDEX idx_drivers_status ON drivers(status);
CREATE INDEX idx_drivers_deleted_at ON drivers(deleted_at);
```

---

## Relationships

| Relation | Type | Target Table | Foreign Key |
|----------|------|--------------|-------------|
| Owner | belongs_to | users | owner_id |
| CreatedBy | belongs_to | users | created_by_id |
| Caretaker | belongs_to | users | caretaker_id |
| UpdatedBy | belongs_to | users | updated_by_id |
| Trips | has_many | trips | driver_id |
| DriverCosts | has_many | driver_costs | driver_id |
| AssignedVehicle | has_one | vehicles | assigned_driver_id |

---

## Sample Data

```json
{
  "id": 1,
  "first_name": "Ramesh",
  "last_name": "Singh",
  "email": "ramesh.singh@email.com",
  "mobile": "9876543210",
  "license_number": "MH0220210012345",
  "license_expiry": "2028-05-15T00:00:00Z",
  "license_type": "HMV",
  "date_of_birth": "1985-03-20T00:00:00Z",
  "address": "123, Main Street, Pune",
  "emergency_contact": "9876543211",
  "blood_group": "O+",
  "status": "active",
  "owner_id": 1,
  "created_by_id": 1,
  "caretaker_id": 2,
  "joining_date": "2023-01-15T00:00:00Z",
  "is_active": true
}
```

---

## License Types

| Value | Label | Description |
|-------|-------|-------------|
| `LMV` | Light Motor Vehicle | Cars, vans |
| `HMV` | Heavy Motor Vehicle | Trucks, buses |
| `HGMV` | Heavy Goods Motor Vehicle | Heavy trucks |
| `PSV` | Public Service Vehicle | Passenger transport |
| `TRANS` | Transport | Commercial transport |

---

## Blood Groups

| Value | Label |
|-------|-------|
| `A+` | A Positive |
| `A-` | A Negative |
| `B+` | B Positive |
| `B-` | B Negative |
| `AB+` | AB Positive |
| `AB-` | AB Negative |
| `O+` | O Positive |
| `O-` | O Negative |

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v2/drivers` | Add driver | Owner, GM, Manager |
| GET | `/api/v2/drivers` | List drivers | All |
| GET | `/api/v2/drivers/:id` | Get driver details | All |
| PUT | `/api/v2/drivers/:id` | Update driver | Owner, GM, Manager |
| PATCH | `/api/v2/drivers/:id/status` | Update status | Owner, GM, Manager |
| DELETE | `/api/v2/drivers/:id` | Disable driver | Owner only |
| GET | `/api/v2/drivers/:id/trips` | Get driver trips | All |
| GET | `/api/v2/drivers/:id/costs` | Get driver costs | Owner, GM |
| GET | `/api/v2/drivers/:id/earnings` | Get earnings summary | Owner, GM |

---

## Computed Fields (Not Stored)

These fields are calculated at query time:

| Field | Description |
|-------|-------------|
| `full_name` | `first_name + " " + last_name` |
| `total_trips` | Count of completed trips |
| `total_distance` | Sum of trip distances |
| `total_earnings` | Sum of salary + incentives - deductions |

---

## Notes

1. **License Validation**: System alerts when license is expiring (30, 15, 7 days)
2. **Caretaker**: Manager/Supervisor responsible for this driver
3. **Status Tracking**: All status changes recorded in audit logs
4. **Soft Delete**: Drivers are terminated/disabled, not deleted
5. **Availability**: Status must be "active" to be assigned to trips

---

*Last Updated: January 2026*

