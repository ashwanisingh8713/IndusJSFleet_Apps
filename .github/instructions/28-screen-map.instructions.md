# screen-map — IndusJS Fleet

## Purpose

Real-time vehicle tracking on map. MQTT subscribe for GPS location data.
Currently mock data — real integration via locationTracker APK.

## Package: `com.ijs.map`

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| MapsScreen | `Maps` | Real-time vehicle positions on map |

## Key Files

| File | Purpose |
|------|---------|
| `presentation/MapFeatureFacade.kt` | Facade — 1 entry point |
| `presentation/MapsContract.kt` | State/Intent/Effect |
| `presentation/MapsViewModel.kt` | ViewModel |
| `presentation/MapsScreen.kt` | UI |

## MQTT Integration

- **locationTracker** APK publishes GPS data via HiveMQ MQTT
- Fleet app subscribes to topic for real-time vehicle positions
- Topic format: `fleet/{ownerId}/vehicle/{vehicleId}/location`

## Module Path

`screen-map/src/commonMain/kotlin/com/ijs/map/`

## Depends On: `ijs-network-lib`

