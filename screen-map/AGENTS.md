# AGENTS.md — screen-map

## Purpose

**Maps & Real-time Tracking** feature module. Displays vehicle locations on a map, subscribing to real-time position updates via MQTT. Presentation-only module — location data comes from `ijs-network-lib` (map entities + MQTT subscription).

**Package:** `com.ijs.map`
**Targets:** Android, iOS, JS, WasmJS

---

## Source Tree

```
src/commonMain/kotlin/com/ijs/map/
├── LogTags.kt
└── presentation/
    ├── MapFeatureFacade.kt                # DI entry point
    ├── MapsContract.kt                    # MVI contract (State/Intent/Effect)
    ├── MapsScreen.kt                      # Map display with vehicle markers
    └── MapsViewModel.kt                   # Subscribes to vehicle locations, manages map state
```

---

## Module Dependencies

| Kind | Dependency |
|------|------------|
| api | `:ijs-network-lib` |
| libs | kotlinx-coroutines, kotlinx-serialization, kermit |

**Minimal module** — Only 5 source files. Presentation-only with no data/domain layer.

---

## Screen

| Screen | Route | Description |
|--------|-------|-------------|
| MapsScreen | `Maps` | Real-time vehicle tracking map |

---

## Key Patterns

- **MQTT integration** — The `locationTracker` Android APK publishes GPS coordinates via MQTT (HiveMQ). This module subscribes to vehicle location topics and updates markers in real-time.
- **Map entities** — `MapEntities` domain model (defined in `ijs-network-lib/domain/entity/maps/`) contains `VehicleMapStatus` with lat/lng, heading, speed, and last-seen timestamp.
- **Platform-specific maps** — Map rendering uses expect/actual for platform-specific map SDKs (Google Maps on Android, MapKit on iOS, Leaflet/similar on web).
