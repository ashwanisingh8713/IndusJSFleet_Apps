# Entity State Management Changes

**Date:** January 12, 2026  
**Version:** v2.1.0

---

## Overview

This document outlines the comprehensive state management changes for Vehicle, Driver, and Trip entities. All states are managed using Go constants for type safety and consistency.

---

## 1. Vehicle States

### Current States (to be replaced)
VehicleStateActive      = "active"
VehicleStateInactive    = "inactive"
VehicleStateMaintenance = "maintenance"
VehicleStateRetired     = "retired"
```

### New States
| State | Value | Description |
|-------|-------|-------------|
| `VehicleStateInactive` | `inactive` | Vehicle is not in use, disabled |
| `VehicleStateActive` | `active` | Vehicle is available for assignment |
| `VehicleStateOnRoute` | `on_route` | Vehicle is currently on a trip |
| `VehicleStateMaintenance` | `maintenance` | Vehicle is under maintenance |
| `VehicleStateDamaged` | `damaged` | Vehicle has damage, needs repair |
| `VehicleStateDecommissioned` | `decommissioned` | Vehicle permanently out of service (optional) |

### State Transitions
```
Inactive → Active (Enable vehicle)
Active → On Route (Trip started)
Active → Maintenance (Scheduled maintenance)
Active → Damaged (Accident/damage reported)
On Route → Active (Trip completed/cancelled)
Maintenance → Active (Maintenance complete)
Damaged → Maintenance (Repair started)
Damaged → Decommissioned (Beyond repair)
Any → Inactive (Disable vehicle)
```

### Affected APIs
| API | Method | Changes |
|-----|--------|---------|
| `POST /vehicles` | Create | Default state: `active` |
| `PUT /vehicles/:id` | Update | Can update state |
| `PATCH /vehicles/:id/state` | Update State | Validate state transitions |
| `GET /vehicles` | List | Filter by new states |
| `GET /dashboard/vehicles-status` | Dashboard | Return counts for new states |

---

## 2. Driver States (Status)

### Current States (to be replaced)
```go
DriverStatusActive    = "active"
DriverStatusInactive  = "inactive"
DriverStatusOnTrip    = "on_trip"
DriverStatusOnLeave   = "on_leave"
DriverStatusSuspended = "suspended"
```

### New States
| State | Value | Description |
|-------|-------|-------------|
| `DriverStatusInactive` | `inactive` | Driver is disabled/not working |
| `DriverStatusActive` | `active` | Driver is available for assignment |
| `DriverStatusOnRoute` | `on_route` | Driver is currently on a trip |
| `DriverStatusOnLeave` | `on_leave` | Driver is on approved leave |
| `DriverStatusSuspended` | `suspended` | Driver privileges suspended |
| `DriverStatusTerminated` | `terminated` | Driver employment terminated (optional) |

### State Transitions
```
Inactive → Active (Activate driver)
Active → On Route (Trip started)
Active → On Leave (Leave approved)
Active → Suspended (Suspension applied)
On Route → Active (Trip completed)
On Leave → Active (Leave ended)
Suspended → Active (Suspension lifted)
Suspended → Terminated (Termination)
Any → Inactive (Disable driver)
```

### Affected APIs
| API | Method | Changes |
|-----|--------|---------|
| `POST /drivers` | Create | Default status: `active` |
| `PUT /drivers/:id` | Update | Can update status |
| `PATCH /drivers/:id/status` | Update Status | Validate state transitions |
| `GET /drivers` | List | Filter by new states |
| `GET /drivers/available` | Available | Only return `active` status |
| `GET /dashboard/drivers-status` | Dashboard | Return counts for new states |

---

## 3. Trip States

### Current States (to be replaced)
```go
TripStatePlanned    = "planned"
TripStateInProgress = "in_progress"
TripStateCompleted  = "completed"
TripStateCancelled  = "cancelled"
```

### New States
| State | Value | Description |
|-------|-------|-------------|
| `TripStatePlanned` | `planned` | Trip created but not started |
| `TripStateAssigned` | `assigned` | Vehicle and driver confirmed |
| `TripStateOnRoute` | `on_route` | Trip is in progress |
| `TripStateCompleted` | `completed` | Trip finished successfully |
| `TripStateCancelled` | `cancelled` | Trip cancelled before completion |
| `TripStateFailed` | `failed` | Trip could not be completed (optional) |
| `TripStateDelayed` | `delayed` | Trip is behind schedule (optional) |

### State Transitions
```
Planned → Assigned (Vehicle/Driver assigned)
Planned → Cancelled (Cancel before start)
Assigned → On Route (Trip started)
Assigned → Cancelled (Cancel before start)
On Route → Completed (Trip finished)
On Route → Delayed (Behind schedule)
On Route → Failed (Cannot complete)
On Route → Cancelled (Cancel mid-trip)
Delayed → On Route (Back on schedule)
Delayed → Completed (Finished despite delay)
Delayed → Failed (Cannot complete)
```

### Affected APIs
| API | Method | Changes |
|-----|--------|---------|
| `POST /trips` | Create | Default state: `planned` |
| `PUT /trips/:id` | Update | Validate state-based edits |
| `PATCH /trips/:id/state` | Update State | Validate transitions, update vehicle/driver states |
| `GET /trips` | List | Filter by new states |
| `GET /dashboard/trips-status` | Dashboard | Return counts for new states |

---

## 4. Cross-Entity State Synchronization

When a trip state changes, related entities should update:

### Trip Started (`on_route`)
- Vehicle state → `on_route`
- Driver status → `on_route`

### Trip Completed/Cancelled/Failed
- Vehicle state → `active`
- Driver status → `active`

---



## 5. Postman Collection Updates

- Update state filter options in List APIs
- Update state change request examples
- Add state transition documentation

---

## 6. Dashboard API Response Changes

### Vehicle Status Response
```json
{
  "total": 100,
  "inactive": 10,
  "active": 50,
  "on_route": 25,
  "maintenance": 10,
  "damaged": 3,
  "decommissioned": 2
}
```

### Driver Status Response
```json
{
  "total": 80,
  "inactive": 5,
  "active": 40,
  "on_route": 25,
  "on_leave": 5,
  "suspended": 3,
  "terminated": 2
}
```

### Trip Status Response
```json
{
  "total": 200,
  "planned": 20,
  "assigned": 15,
  "on_route": 30,
  "completed": 120,
  "cancelled": 10,
  "failed": 3,
  "delayed": 2
}
```

---

## 7. Implementation Checklist

- [x] Update Vehicle model with new states
- [x] Update Driver model with new states
- [x] Update Trip model with new states
- [x] Add state validation helpers
- [x] Add state transition validators
- [x] Update Vehicle controller
- [x] Update Driver controller
- [x] Update Trip controller with cross-entity sync
- [x] Update Dashboard 
- [x] To update states implement apis for `Vehicle`, `Driver`, `Trip` in respective `Detail` Screens.
- [x] Who has updated the states it must be tracked(time, to-from, user) and provided into the history.

