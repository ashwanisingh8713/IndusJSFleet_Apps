# Entity State Machines

> Use this prompt when working with entity status transitions, state-based UI, or status updates.

## Source of Truth

`sharedUI/.../core/constants/StatusConstants.kt` (539 lines) — defines ALL states, valid transitions, display labels, icons, and color schemes.

## Vehicle States

```
inactive ←→ active → on_route → active
                   → maintenance ←→ damaged → decommissioned
```

| State | Label | Icon | Color | Available for Trip? |
|-------|-------|------|-------|---------------------|
| `inactive` | Inactive | ⚫ | Neutral | ❌ |
| `active` | Active | 🟢 | Success | ✅ |
| `on_route` | On Route | 🚗 | Info | ❌ |
| `maintenance` | Maintenance | 🔧 | Warning | ❌ |
| `damaged` | Damaged | ⚠️ | Error | ❌ |
| `decommissioned` | Decommissioned | 🚫 | Neutral | ❌ |

**API values:** `"inactive"`, `"active"`, `"on_route"`, `"maintenance"`, `"damaged"`, `"decommissioned"`

```kotlin
StatusConstants.VehicleState.isAvailableForAssignment("active") // true
StatusConstants.VehicleState.getDisplayLabel("on_route")        // "On Route"
StatusConstants.VehicleState.getIcon("maintenance")             // "🔧"
StatusConstants.VehicleState.getColorScheme("active")           // StateColorScheme.SUCCESS
```

## Driver States

```
inactive ←→ active → on_route → active
                   → on_leave → active
                   → suspended → active | terminated
```

| State | Label | Available for Trip? |
|-------|-------|---------------------|
| `inactive` | Inactive | ❌ |
| `active` | Active | ✅ |
| `on_route` | On Route | ❌ |
| `on_leave` | On Leave | ❌ |
| `suspended` | Suspended | ❌ |
| `terminated` | Terminated | ❌ (terminal) |

```kotlin
StatusConstants.DriverState.AVAILABLE_FOR_ASSIGNMENT // listOf("active")
StatusConstants.DriverState.isValid("on_route")       // true
```

## Trip States

```
planned → on_route → completed
planned → cancelled
on_route → delayed → completed | failed
on_route → cancelled | failed
```

| State | Label | Can Cancel? | Can Start? |
|-------|-------|-------------|------------|
| `planned` | Planned | ✅ | ✅ |
| `on_route` | On Route | ✅ | ❌ |
| `delayed` | Delayed | ❌ | ❌ |
| `completed` | Completed | ❌ | ❌ |
| `cancelled` | Cancelled | ❌ | ❌ |
| `failed` | Failed | ❌ | ❌ |

```kotlin
StatusConstants.TripState.canCancel("planned")     // true
StatusConstants.TripState.canStart("planned")      // true
StatusConstants.TripState.getNextStates("on_route") // ["completed", "delayed", "failed", "cancelled"]
```

## Cross-Entity State Sync

When a trip starts (`planned → on_route`):
- Vehicle state → `on_route`
- Driver state → `on_route`

When a trip completes (`on_route → completed`):
- Vehicle state → `active`
- Driver state → `active`

This is handled by the backend API automatically.

## Payment Status

| Status | Label |
|--------|-------|
| `received` | Received |
| `pending` | Pending |
| `cancelled` | Cancelled |

## User Roles

| Role | API Value | Display |
|------|-----------|---------|
| Owner | `owner` | Owner |
| General Manager | `general_manager` | General Manager |
| Manager | `manager` | Manager |
| Supervisor | `supervisor` | Supervisor |

## UI Usage

```kotlin
// Status chip/badge
val label = StatusConstants.VehicleState.getDisplayLabel(vehicle.status)
val color = StatusConstants.VehicleState.getColorScheme(vehicle.status)

// Filter chips
StatusConstants.VehicleState.ALL.forEach { status ->
    FilterChip(
        selected = selectedFilter == status,
        onClick = { viewModel.sendIntent(Intent.FilterByStatus(status)) },
        label = { Text(StatusConstants.VehicleState.getDisplayLabel(status)) }
    )
}

// Valid transition buttons
StatusConstants.TripState.getNextStates(trip.status).forEach { nextState ->
    Button(onClick = { viewModel.sendIntent(Intent.ChangeStatus(nextState)) }) {
        Text(StatusConstants.TripState.getDisplayLabel(nextState))
    }
}
```

