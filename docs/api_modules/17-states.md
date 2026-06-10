# 17. States (Reference Data)

State reference data endpoint for retrieving all valid entity states used across the application.

## Overview

The States endpoint returns all valid states for vehicles, drivers, trips, and payments. Use this to populate dropdowns, validate state transitions, and display state labels in the UI.

## Role Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| View States | ✅ | ✅ | ✅ | ✅ |

---

## Endpoints

### Get All States

```http
GET {{base_url}}/states
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "All states retrieved successfully",
    "data": {
        "vehicle_states": [
            {"value": "inactive", "label": "Inactive", "color": "#9E9E9E"},
            {"value": "active", "label": "Active", "color": "#4CAF50"},
            {"value": "on_route", "label": "On Route", "color": "#2196F3"},
            {"value": "maintenance", "label": "Maintenance", "color": "#FF9800"},
            {"value": "damaged", "label": "Damaged", "color": "#F44336"},
            {"value": "decommissioned", "label": "Decommissioned", "color": "#795548"}
        ],
        "driver_statuses": [
            {"value": "inactive", "label": "Inactive", "color": "#9E9E9E"},
            {"value": "active", "label": "Active", "color": "#4CAF50"},
            {"value": "on_route", "label": "On Route", "color": "#2196F3"},
            {"value": "on_leave", "label": "On Leave", "color": "#FF9800"},
            {"value": "suspended", "label": "Suspended", "color": "#F44336"},
            {"value": "terminated", "label": "Terminated", "color": "#795548"}
        ],
        "trip_states": [
            {"value": "planned", "label": "Planned", "color": "#2196F3"},
            {"value": "on_route", "label": "On Route", "color": "#4CAF50"},
            {"value": "delayed", "label": "Delayed", "color": "#FF9800"},
            {"value": "completed", "label": "Completed", "color": "#8BC34A"},
            {"value": "cancelled", "label": "Cancelled", "color": "#9E9E9E"},
            {"value": "failed", "label": "Failed", "color": "#F44336"}
        ],
        "payment_states": [
            {"value": "received", "label": "Received", "color": "#4CAF50"},
            {"value": "pending", "label": "Pending", "color": "#FF9800"},
            {"value": "cancelled", "label": "Cancelled", "color": "#F44336"},
            {"value": "partial", "label": "Partial", "color": "#2196F3"}
        ]
    }
}
```

---

## State Definitions

### Vehicle States

| Value | Label | Color | Description |
|-------|-------|-------|-------------|
| `inactive` | Inactive | #9E9E9E | Vehicle is not in use, disabled |
| `active` | Active | #4CAF50 | Vehicle is available for assignment |
| `on_route` | On Route | #2196F3 | Vehicle is currently on a trip |
| `maintenance` | Maintenance | #FF9800 | Vehicle is under maintenance |
| `damaged` | Damaged | #F44336 | Vehicle has damage, needs repair |
| `decommissioned` | Decommissioned | #795548 | Vehicle permanently out of service |

### Driver Statuses

| Value | Label | Color | Description |
|-------|-------|-------|-------------|
| `inactive` | Inactive | #9E9E9E | Driver is disabled/not working |
| `active` | Active | #4CAF50 | Driver is available for assignment |
| `on_route` | On Route | #2196F3 | Driver is currently on a trip |
| `on_leave` | On Leave | #FF9800 | Driver is on approved leave |
| `suspended` | Suspended | #F44336 | Driver privileges suspended |
| `terminated` | Terminated | #795548 | Driver employment terminated |

### Trip States

| Value | Label | Color | Description |
|-------|-------|-------|-------------|
| `planned` | Planned | #2196F3 | Trip created but not started |
| `on_route` | On Route | #4CAF50 | Trip is in progress |
| `delayed` | Delayed | #FF9800 | Trip is behind schedule |
| `completed` | Completed | #8BC34A | Trip finished successfully |
| `cancelled` | Cancelled | #9E9E9E | Trip cancelled |
| `failed` | Failed | #F44336 | Trip could not be completed |

### Payment States

| Value | Label | Color | Description |
|-------|-------|-------|-------------|
| `received` | Received | #4CAF50 | Payment confirmed |
| `pending` | Pending | #FF9800 | Awaiting confirmation |
| `cancelled` | Cancelled | #F44336 | Payment cancelled |
| `partial` | Partial | #2196F3 | Partial payment received |

---

## State Machines

### Vehicle States
```
inactive ←→ active → on_route → active
                   → maintenance → active
                   → damaged → maintenance → active
                            → decommissioned (terminal)
```

### Driver Statuses
```
inactive ←→ active → on_route → active
                   → on_leave → active
                   → suspended → active
                              → terminated (terminal)
```

### Trip States
```
planned → on_route → completed (terminal)
        → cancelled (terminal)
on_route → delayed → on_route
                   → completed (terminal)
                   → failed (terminal)
on_route → failed (terminal)
on_route → cancelled (terminal)
```

### Payment States
```
pending → received (terminal)
pending → cancelled (terminal)
partial → received (terminal)
partial → cancelled (terminal)
```

---

## Color Legend

| Color | Hex | Semantic Meaning |
|-------|-----|------------------|
| Green | #4CAF50 | Active/Available/Good |
| Light Green | #8BC34A | Completed/Success |
| Blue | #2196F3 | In Progress/Planned |
| Orange | #FF9800 | Warning/Pending/Leave |
| Red | #F44336 | Error/Suspended/Failed |
| Gray | #9E9E9E | Inactive/Cancelled |
| Brown | #795548 | Terminal/End State |

---

## Usage

Use this endpoint to:
1. **Populate filter dropdowns** on list screens (vehicles, drivers, trips)
2. **Validate state transitions** before calling state-update APIs
3. **Display state labels and colors** consistently in the UI
4. **Cache locally** — states rarely change; safe to cache for the session

---

## Mobile App Integration

The mobile app should use these exact `value` strings when sending API requests:

```kotlin
// Driver Status enum mapping
enum class DriverStatus(val apiValue: String) {
    INACTIVE("inactive"),
    ACTIVE("active"),
    ON_ROUTE("on_route"),
    ON_LEAVE("on_leave"),
    SUSPENDED("suspended"),
    TERMINATED("terminated")
}
```

**Important:** Use `active` (not `available`) and `on_route` (not `on_trip`).

---

*Last Updated: April 2026*

