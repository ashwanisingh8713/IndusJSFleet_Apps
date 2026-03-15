# AGENTS.md - locationTracker

## Purpose

**Android-only** standalone app for real-time vehicle GPS tracking. Runs as a foreground service, collects GPS coordinates via `FusedLocationProviderClient`, and publishes them to an MQTT broker (HiveMQ). This is a **separate APK** installed on driver devices — completely independent of the main fleet KMP app.

**Package:** `com.indusjs.fleet.locationtracker`  
**Platform:** Android only (not KMP)

---

## Architecture

```
src/main/kotlin/com/indusjs/fleet/locationtracker/
├── LocationTrackerApp.kt              # Application class
├── service/
│   └── LocationTrackingService.kt     # ★ Foreground service (GPS + MQTT publish, 490 lines)
├── mqtt/
│   ├── MqttClientManager.kt          # Singleton MQTT client (HiveMQ)
│   └── MqttLocationClient.kt         # MQTT connection wrapper
├── data/
│   ├── Models.kt                     # LocationMessage, TrackerConfig DTOs
│   └── TrackerPreferencesRepository.kt # DataStore-based config persistence
├── receiver/
│   ├── BootReceiver.kt               # Auto-start service on device boot
│   └── RestartReceiver.kt            # Restart service if killed by system
└── ui/
    ├── MainActivity.kt               # Config UI (broker URL, vehicle ID, intervals)
    ├── MainViewModel.kt              # UI state management
    └── theme/Theme.kt                # Material 3 theme
```

---

## How It Works

1. Driver opens app → enters MQTT broker URL, vehicle registration number, tracking interval
2. Service starts as foreground (persistent notification — cannot be killed)
3. `FusedLocationProviderClient` provides GPS coordinates at configured intervals
4. Publishes `LocationMessage` JSON to MQTT topic: `fleet/vehicle/{registration_number}/location`
5. Service survives app kill via `AlarmManager` + `BootReceiver` + `RestartReceiver`
6. Watchdog checks every 60s; restarts GPS if no location received for 2 minutes
7. WakeLock keeps CPU running even when screen is off

## MQTT Message Format

```json
{
  "registration_number": "MH12AB1234",
  "lat": 19.0760,
  "lng": 72.8777,
  "speed": 45.5,
  "ts": 1703500800000,
  "heading": 180.0,
  "accuracy": 5.0,
  "trip_id": 456,
  "driver_id": 789,
  "bat": 85,
  "provider": "gps"
}
```

**Topic:** `fleet/vehicle/{registration_number}/location`

---

## Key Dependencies

| Library | Usage |
|---------|-------|
| HiveMQ MQTT Client | `com.hivemq.client:hivemq-mqtt-client` — MQTT 3.1.1/5.0 publish |
| Google Play Services Location | `FusedLocationProviderClient` — high-accuracy GPS |
| Jetpack Compose | Configuration UI only |
| Kermit | Cross-platform logging |
| kotlinx-serialization | JSON serialization of `LocationMessage` |

## Build

```bash
./gradlew :locationTracker:assembleDebug
```

---

## Relation to Main Fleet App

The main fleet app's `MapsScreen` subscribes to the **same MQTT broker** to display vehicle positions in real-time on a map. The two apps are fully independent:

```
locationTracker (driver device) ──MQTT publish──→ Broker ──MQTT subscribe──→ fleet app (MapsScreen)
```

They share no code, no modules, no dependencies.
