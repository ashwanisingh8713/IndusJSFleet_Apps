# Vehicles Module

## Overview

The Vehicles module provides complete vehicle lifecycle management including registration, document tracking, maintenance cost recording, and status management.

---

## Features

- Vehicle CRUD operations
- Document management (upload, view, expiry tracking)
- Maintenance cost tracking
- Vehicle location tracking
- Driver assignment
- Caretaker assignment (Manager/Supervisor)
- Status management
- Trip history per vehicle
- Profit/Loss analysis

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| Vehicles List | `FleetRoute.Vehicles` | List all vehicles with filters |
| Vehicle Detail | `FleetRoute.VehicleDetail` | Vehicle details with tabs |
| Add Vehicle | `FleetRoute.AddVehicle` | Register new vehicle |

---

## Vehicle Entity

### Basic Information

| Field | Description | Required |
|-------|-------------|:--------:|
| Registration Number | Unique vehicle identifier | ✅ |
| Make | Manufacturer (e.g., Tata, Ashok Leyland) | ✅ |
| Model | Vehicle model | ✅ |
| Year | Manufacturing year | ✅ |
| Vehicle Type | Truck, Trailer, Tanker, etc. | ✅ |
| Fuel Type | Diesel, Petrol, CNG, Electric | ✅ |
| Capacity | Load capacity in tons | ✅ |
| Color | Vehicle color | ✅ |

### Status

| Status | Description | Can Assign |
|--------|-------------|:----------:|
| Active | Ready for trips | ✅ |
| On Route | Currently on a trip | ❌ |
| Maintenance | Under repair/service | ❌ |
| Inactive | Not in use | ❌ |
| Damaged | Requires repair | ❌ |
| Decommissioned | Retired from fleet | ❌ |

### Assignments

| Field | Description |
|-------|-------------|
| Assigned Driver | Driver currently assigned |
| Caretaker | Manager/Supervisor responsible |

---

## Vehicle Detail Tabs

### 1. Overview Tab

Displays basic vehicle information:
- Registration details
- Make, Model, Year
- Type and Fuel
- Current status
- Assigned driver
- Caretaker information

### 2. Trips Tab

Shows trip history for this vehicle:
- Trip list with pagination
- Filter by status
- Click to view trip details

### 3. Documents Tab

Manages vehicle documents:

| Document Type | Description |
|---------------|-------------|
| Registration Certificate (RC) | Vehicle registration |
| Insurance | Vehicle insurance |
| PUC Certificate | Pollution control |
| Fitness Certificate | Vehicle fitness |
| Road Tax | Tax payment proof |
| Permit | Route/cargo permit |

**Document Fields:**
- Document Type
- Document Number
- Expiry Date
- Uploaded File (optional)

### 4. Costs Tab

Shows maintenance cost history:
- Cost list with date filter
- Filter by cost type
- Total cost summary
- Add new maintenance cost

---

## Add Vehicle Form

### Sections

1. **Basic Details**
   - Registration Number
   - Make
   - Model
   - Year
   - Vehicle Type (dropdown)
   - Fuel Type (dropdown)
   - Capacity
   - Color

2. **Documents** (Optional)
   - Insurance details with expiry
   - PUC details with expiry
   - Fitness details with expiry
   - Road Tax details with expiry
   - Permit details with expiry

3. **Caretaker Assignment**
   - Select Manager or Supervisor
   - Option to add new team member

---

## Vehicle Status State Machine

### State Transitions

```
            ┌─────────────┐
            │   ACTIVE    │◄────────────────┐
            └──────┬──────┘                 │
                   │                        │
        Start Trip │        ┌───────────────┤
                   ▼        │               │
            ┌─────────────┐ │               │
            │  ON_ROUTE   │─┘               │
            └──────┬──────┘ End Trip        │
                   │                        │
                   │        ┌───────────────┤
                   │        │               │
            ┌──────▼──────┐ │               │
            │ MAINTENANCE │─┘               │
            └──────┬──────┘ Complete        │
                   │        Service         │
                   └────────────────────────┘
```

---

## Role-Based Permissions

| Action | Owner | GM | Manager | Supervisor |
|--------|:-----:|:--:|:-------:|:----------:|
| View Vehicles | ✅ | ✅ | ✅ | ✅ |
| Add Vehicle | ✅ | ✅ | ✅ | ❌ |
| Edit Vehicle | ✅ | ✅ | ✅ | ❌ |
| Delete Vehicle | ✅ | ✅ | ❌ | ❌ |
| Assign Driver | ✅ | ✅ | ✅ | ❌ |
| Assign Caretaker | ✅ | ✅ | ❌ | ❌ |
| Add Documents | ✅ | ✅ | ✅ | ❌ |
| Add Maintenance Cost | ✅ | ✅ | ✅ | ✅ |
| View P&L | ✅ | ✅ | ❌ | ❌ |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/vehicles` | GET | List vehicles with pagination |
| `/vehicles` | POST | Create vehicle |
| `/vehicles/{id}` | GET | Get vehicle details |
| `/vehicles/{id}` | PUT | Update vehicle |
| `/vehicles/{id}` | DELETE | Delete vehicle |
| `/vehicles/{id}/documents` | GET | Get vehicle documents |
| `/vehicles/{id}/documents` | POST | Add document |
| `/vehicles/{id}/maintenance-costs` | GET | Get maintenance costs |
| `/vehicles/{id}/maintenance-costs` | POST | Add maintenance cost |
| `/vehicles/{id}/trip-costs` | GET | Get trip costs for vehicle |
| `/vehicles/{id}/trips` | GET | Get vehicle trip history |
| `/vehicles/{id}/profit-loss` | GET | Get vehicle P&L |

---

## Maintenance Cost Types

| Cost ID | Label | Group |
|---------|-------|-------|
| VMC-001-001 | Engine Oil Change | Regular Service |
| VMC-001-002 | Oil Filter Replacement | Regular Service |
| VMC-002-001 | Front Tyre Replacement | Tyres |
| VMC-002-002 | Rear Tyre Replacement | Tyres |
| VMC-003-001 | Battery Replacement | Electrical |
| VMC-003-002 | Alternator Repair | Electrical |
| VMC-004-001 | Brake Pad Replacement | Brakes |
| VMC-004-002 | Brake Disc Replacement | Brakes |

---

## Related Modules

- [Drivers](../drivers/) - Driver assignment
- [Trips](../trips/) - Trip history
- [Costs](../costs/) - Maintenance costs
- [Reports](../reports/) - Vehicle P&L

---

## Related Documentation

- [Modules Overview](../README.md)
- [User Roles](../../user-roles/README.md)
