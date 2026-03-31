# locationTracker — Standalone GPS Tracker

## Overview

**Separate Android APK** (not KMP). Publishes vehicle GPS locations to MQTT broker. Fleet app subscribes to display real-time tracking. Has **no dependency on sharedUI**.

## File Tree

```
locationTracker/
├── build.gradle.kts
├── AGENTS.md
├── README.md
└── src/main/
    ├── AndroidManifest.xml
    ├── kotlin/com/indusjs/fleet/locationtracker/
    │   ├── LocationTrackerApp.kt              # Application class
    │   ├── data/
    │   │   ├── Models.kt                      # Location data models
    │   │   └── TrackerPreferencesRepository.kt # DataStore preferences
    │   ├── mqtt/
    │   │   ├── MqttClientManager.kt           # HiveMQ MQTT client
    │   │   └── MqttLocationClient.kt          # Location → MQTT publish
    │   ├── receiver/
    │   │   ├── BootReceiver.kt                # Restart on boot
    │   │   └── RestartReceiver.kt             # Restart on connectivity
    │   ├── service/
    │   │   └── LocationTrackingService.kt     # Foreground service
    │   └── ui/
    │       ├── MainActivity.kt                # Compose UI
    │       ├── MainViewModel.kt               # Config + status
    │       └── theme/Theme.kt                 # Local Material theme
    └── res/
        ├── mipmap-anydpi-v26/ic_launcher.xml
        ├── values/strings.xml, styles.xml
        └── xml/network_security_config.xml
```

## How It Works

```
LocationTrackingService (foreground service)
  → Google Play Services FusedLocationProvider
    → Gets GPS coordinates at configured interval
      → MqttLocationClient serializes to JSON
        → MqttClientManager publishes to MQTT broker (HiveMQ)
          → Fleet app subscribes on MapsScreen
```

## Key Components

### LocationTrackingService
- Android foreground service with persistent notification
- Uses `FusedLocationProviderClient` from Google Play Services
- Configurable interval and distance filter
- Auto-restarts on boot and connectivity changes

### MqttClientManager
- HiveMQ MQTT client library (v1.3.3)
- Handles connection, reconnection, publish
- TLS support for secure broker communication

### TrackerPreferencesRepository
- DataStore-based preferences
- Stores: broker URL, port, topic, vehicle ID, update interval

### Receivers
- `BootReceiver` — restarts tracking service on device boot
- `RestartReceiver` — restarts on connectivity changes

## Configuration

| Setting | Value |
|---------|-------|
| `applicationId` | `com.indusjs.fleet.locationtracker` |
| `minSdk` | 24 (overrides fleet convention of 23) |
| `compileSdk` | 36 |

## Manifest Permissions

- `INTERNET`, `ACCESS_NETWORK_STATE`
- `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`
- `FOREGROUND_SERVICE` (type: location)
- `POST_NOTIFICATIONS`, `WAKE_LOCK`
- `RECEIVE_BOOT_COMPLETED`, `SCHEDULE_EXACT_ALARM`

## Integration with Fleet App

The fleet app's `screen-map` module subscribes to the same MQTT topics to display real-time vehicle positions. Currently uses mock data in the fleet app.
