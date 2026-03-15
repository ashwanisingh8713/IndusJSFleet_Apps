# Alerts & Maps Features

> Use this prompt when working on document/license expiry alerts or real-time vehicle tracking.

## Alerts

### Screen & Route

| Screen | Route | Description |
|--------|-------|-------------|
| `AlertsListScreen` | `AlertsList` | Document expiry, license expiry, maintenance due |

### API Endpoint

```
GET /dashboard/alerts-status → Alert categories with items
```

### Alert Types

1. **Document Expiry** — Vehicle insurance, fitness, permit, PUC nearing expiry
2. **License Expiry** — Driver licenses expiring soon
3. **Maintenance Due** — Scheduled maintenance reminders

### Severity Levels

- **Critical** (red) — expired or expiring within 7 days
- **Warning** (orange) — expiring within 30 days
- **Info** (blue) — expiring within 90 days

### Data Flow

Alerts data comes from the same endpoint used by Dashboard. `GetAlertsStatusUseCase` is shared between `DashboardViewModel` and `AlertsListViewModel`.

### Key Files

| Layer | File |
|-------|------|
| Use Case | `domain/usecase/dashboard/GetAlertsStatusUseCase.kt` |
| Presentation | `presentation/alerts/AlertsListContract.kt` |
| Presentation | `presentation/alerts/AlertsListViewModel.kt` |
| Presentation | `presentation/alerts/AlertsListScreen.kt` |

---

## Maps (Real-Time Vehicle Tracking)

### Screen & Route

| Screen | Route | Description |
|--------|-------|-------------|
| `MapsScreen` | `Maps` | Real-time vehicle positions on map |

### How It Works

```
locationTracker app (driver device)
    → GPS via FusedLocationProviderClient
    → Publishes to MQTT topic: fleet/vehicle/{reg_number}/location
    → HiveMQ Broker
    → Fleet app MapsScreen subscribes to broker
    → Updates vehicle markers on map in real-time
```

### MQTT Message Format

```json
{
    "registration_number": "MH12AB1234",
    "lat": 19.0760,
    "lng": 72.8777,
    "speed": 45.5,
    "ts": 1703500800000,
    "heading": 180.0,
    "accuracy": 5.0,
    "bat": 85,
    "provider": "gps"
}
```

### Topic Pattern

`fleet/vehicle/{registration_number}/location`

### Key Files

| Layer | File |
|-------|------|
| Entity | `domain/entity/maps/` |
| Presentation | `presentation/maps/MapsViewModel.kt` |
| Presentation | `presentation/maps/MapsScreen.kt` |

### Note

The `locationTracker` module is a separate Android-only APK installed on driver devices. It publishes GPS coordinates. The fleet app only subscribes/consumes.

