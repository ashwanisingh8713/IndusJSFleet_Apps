# screen-map

## Overview

**Package:** `com.ijs.map`
**Module type:** Presentation-only feature module
**Purpose:** Real-time vehicle tracking on a map. Displays vehicle positions with status indicators, supports live tracking toggle, geofence overlays, and vehicle selection with detail navigation. Currently uses mock data with MQTT integration planned.

---

## Architecture

| Layer | Contents |
|-------|----------|
| **Presentation** | `MapFeatureFacade`, `MapsContract`, `MapsViewModel`, `MapsScreen` |
| **Domain** | None — uses `MapVehicle`, `Geofence` entities from `ijs-network-lib` |
| **Data** | None — mock data currently; MQTT subscription planned |

---

## Dependencies

```
screen-map → ijs-network-lib → ijs-core-lib
```

No cross-feature module dependencies.

---

## Screens

### MapsScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.Maps` |
| ViewModel | `MapsViewModel` |
| Contract | `MapsContract` |

**Features:**
- Map view centered on India (default: lat 20.5937, lng 78.9629, zoom 5)
- Vehicle markers with status-based colors
- Vehicle selection → info popup with registration, driver, speed, last update
- Live tracking toggle (auto-refresh vehicle positions)
- Geofence overlay toggle
- Navigate to vehicle detail from vehicle popup
- Currently renders mock vehicle positions; real MQTT integration via `locationTracker` APK planned

**State highlights:**
- `vehicles: List<MapVehicle>` — vehicle positions with status
- `geofences: List<Geofence>` — geofence boundaries
- `selectedVehicle: MapVehicle?` — tapped vehicle
- `isLiveTrackingEnabled: Boolean` — auto-refresh toggle
- `showGeofences: Boolean` — geofence overlay toggle
- `mapZoom: Float`, `centerLatitude/Longitude` — map camera state

**Key Intents:** `LoadMapData`, `RefreshVehicleLocations`, `SelectVehicle`, `ClearSelection`, `ToggleLiveTracking`, `ToggleGeofences`, `NavigateToVehicleDetail`

**Key Effects:** `ZoomToVehicle(lat, lng)`, `NavigateToVehicleDetail(id)`, `ShowSnackbar`

---

## Facade

```kotlin
object MapFeatureFacade {
    fun MapsEntry(viewModel, onNavigateToVehicleDetail, onNavigateBack)
}
```

---

## Domain Entities (from ijs-network-lib)

| Entity | Description |
|--------|-------------|
| `MapVehicle` | Vehicle with GPS position, speed, heading, status, driver info |
| `Geofence` | Geographic boundary with name, coordinates, type |

---

## Real-Time Tracking Architecture (Planned)

```
locationTracker APK (driver device)
  → Publishes GPS via MQTT to HiveMQ broker
    → Fleet App subscribes to vehicle topics
      → MapsViewModel updates vehicle positions in real-time
```

The `locationTracker` module is a standalone Android APK that runs as a foreground service, collecting GPS coordinates and publishing them via MQTT. The fleet app's map screen will subscribe to these MQTT topics to show live positions.

