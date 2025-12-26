# Fleet Location Tracker - Android App

This is a standalone Android application for tracking vehicle location data and sending it to the Fleet Management server via MQTT protocol.

## Features

- **Real-time GPS Tracking**: Tracks vehicle location at configurable intervals (default: 2 seconds)
- **MQTT Publishing**: Publishes location data to MQTT broker using HiveMQ client
- **Background Service**: Runs as a foreground service with persistent notification
- **Auto-restart on Boot**: Automatically resumes tracking after device restart
- **Offline Buffering**: Buffers messages when MQTT connection is lost

## MQTT Configuration

### Topic Format

**Vehicle Location (using registration number):**
```
fleet/vehicle/{registration_number}/location
```
Example: `fleet/vehicle/MH12AB1234/location`

**Trip Location (if active trip):**
```
fleet/trip/{trip_id}/location
```

### Message Payload (JSON)

```json
{
  "registration_number": "MH12AB1234",
  "lat": 19.0760,
  "lng": 72.8777,
  "speed": 45.5,
  "ts": 1703500800000,
  "heading": 180.0,
  "altitude": 50.0,
  "accuracy": 5.0,
  "trip_id": 456,
  "driver_id": 789,
  "bat": 85,
  "provider": "gps"
}
```

### Field Descriptions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| registration_number | String | ✅ | Vehicle registration number (e.g., 'MH12AB1234') |
| lat | Double | ✅ | Latitude (-90 to 90) |
| lng | Double | ✅ | Longitude (-180 to 180) |
| speed | Double | ✅ | Speed in km/h |
| ts | Long | ✅ | Unix timestamp (milliseconds) |
| heading | Double | ❌ | Direction (0-360°) |
| altitude | Double | ❌ | Altitude in meters |
| accuracy | Double | ❌ | GPS accuracy in meters |
| trip_id | Long | ❌ | Active trip ID |
| driver_id | Long | ❌ | Driver ID |
| bat | Int | ❌ | Battery level (0-100) |
| provider | String | ❌ | Location provider (gps/network) |

**Note:** `vehicle_id` and `owner_id` are automatically resolved from `registration_number` on the server.

## Required Permissions

The app requires the following permissions:

- `INTERNET` - For MQTT connection
- `ACCESS_FINE_LOCATION` - For GPS tracking
- `ACCESS_COARSE_LOCATION` - For network-based location
- `ACCESS_BACKGROUND_LOCATION` - For tracking when app is in background (Android 10+)
- `FOREGROUND_SERVICE` - For running as foreground service
- `FOREGROUND_SERVICE_LOCATION` - For location foreground service type (Android 14+)
- `POST_NOTIFICATIONS` - For showing tracking notification (Android 13+)
- `RECEIVE_BOOT_COMPLETED` - For auto-start after device boot

## Configuration

The app requires the following configuration:

1. **Registration Number** (Required): The vehicle registration number (e.g., 'MH12AB1234')
2. **Driver ID** (Optional): The driver ID
3. **Trip ID** (Optional): The active trip ID
4. **MQTT Broker URL** (Required): e.g., `tcp://your-broker:1883` or `ssl://your-broker:8883`
5. **MQTT Client ID** (Optional): Auto-generated if not provided
6. **MQTT Username/Password** (Optional): For authenticated connections
7. **Update Interval** (Default: 2 seconds): How often to send location updates

## Building

```bash
# Build debug APK
./gradlew :locationTracker:assembleDebug

# Build release APK
./gradlew :locationTracker:assembleRelease
```

The APK will be generated at:
- Debug: `locationTracker/build/outputs/apk/debug/locationTracker-debug.apk`
- Release: `locationTracker/build/outputs/apk/release/locationTracker-release.apk`

## MQTT Broker Setup

### Using Docker (EMQX)

```bash
docker run -d --name emqx \
  -p 1883:1883 \
  -p 8083:8083 \
  -p 8084:8084 \
  -p 8883:8883 \
  -p 18083:18083 \
  emqx/emqx:5.3.0
```

Dashboard: http://localhost:18083 (admin/public)

### Using Docker (Mosquitto)

```bash
docker run -d --name mosquitto \
  -p 1883:1883 \
  -p 9001:9001 \
  eclipse-mosquitto
```

## Architecture

```
locationTracker/
├── data/
│   ├── Models.kt              # Data models (LocationMessage, TrackerConfig)
│   └── TrackerPreferencesRepository.kt  # DataStore for settings
├── mqtt/
│   └── MqttLocationClient.kt  # HiveMQ MQTT client wrapper
├── receiver/
│   └── BootReceiver.kt        # Auto-start on device boot
├── service/
│   └── LocationTrackingService.kt  # Foreground service for tracking
├── ui/
│   ├── MainActivity.kt        # Main UI with Compose
│   ├── MainViewModel.kt       # ViewModel for state management
│   └── theme/
│       └── Theme.kt           # Material 3 theme
└── LocationTrackerApp.kt      # Application class
```

## Dependencies

- **HiveMQ MQTT Client**: Modern, actively maintained MQTT client
- **Google Play Services Location**: Fused Location Provider for accurate GPS
- **Jetpack Compose**: Modern declarative UI
- **Kotlin Coroutines**: Asynchronous programming
- **DataStore Preferences**: Local storage for configuration
- **Kermit**: Multiplatform logging

