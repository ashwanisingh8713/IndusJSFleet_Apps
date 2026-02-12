# 8. Message Protocols

## 8.1 Telemetry Message (Device → Cloud)

### JSON Format (Standard)

```json
{
  "ts": 1707580800,
  "lat": 28.6139,
  "lng": 77.2090,
  "alt": 216.5,
  "spd": 45.5,
  "hdg": 180.0,
  "acc": 2.5,
  "sat": 12,
  "ign": true,
  "bat": 12.6
}
```

### Field Specifications

| Field | Type | Unit | Description |
|-------|------|------|-------------|
| ts | int64 | Unix epoch | Timestamp |
| lat | float64 | degrees | Latitude (-90 to 90) |
| lng | float64 | degrees | Longitude (-180 to 180) |
| alt | float64 | meters | Altitude above sea level |
| spd | float64 | km/h | Speed |
| hdg | float64 | degrees | Heading (0-360) |
| acc | float64 | meters | Horizontal accuracy |
| sat | int | count | Visible satellites |
| ign | bool | - | Ignition state |
| bat | float32 | volts | Battery voltage |

### Binary Format (Memory-Optimized)

See [06-memory-optimized-client.md](./06-memory-optimized-client.md) for the 20-byte compact binary format.

---

## 8.2 Command Message (Cloud → Device)

```json
{
  "id": "cmd-uuid-12345",
  "type": "reboot",
  "params": {},
  "ts": 1707580800,
  "ttl": 300,
  "sign": "hmac-sha256-signature"
}
```

### Command Types

| Type | Parameters | Description |
|------|------------|-------------|
| `reboot` | none | Restart device |
| `buzzer` | `{ "duration": 5, "pattern": "sos" }` | Activate buzzer |
| `config_update` | `{ "interval": 30 }` | Update reporting interval |
| `locate` | none | Force immediate location report |
| `ota_start` | `{ "url": "...", "hash": "..." }` | Start firmware update |
| `engine_lock` | `{ "lock": true }` | Lock/unlock engine (if supported) |
| `geofence_add` | `{ "id": 1, "lat": ..., "lng": ..., "radius": 100 }` | Add geofence |
| `geofence_remove` | `{ "id": 1 }` | Remove geofence |

---

## 8.3 Acknowledgment Message (Device → Cloud)

```json
{
  "cmd_id": "cmd-uuid-12345",
  "status": "success",
  "message": "Command executed successfully",
  "ts": 1707580805,
  "data": {}
}
```

### Status Codes

| Status | Description |
|--------|-------------|
| `success` | Command executed successfully |
| `failed` | Command execution failed |
| `rejected` | Command rejected (invalid params) |
| `timeout` | Command timed out |
| `pending` | Command queued for execution |
| `unsupported` | Command not supported by device |

---

## 8.4 Event Message (Device → Cloud)

```json
{
  "type": "ignition_on",
  "ts": 1707580800,
  "data": {
    "lat": 28.6139,
    "lng": 77.2090
  }
}
```

### Event Types

| Event | Trigger | Data |
|-------|---------|------|
| `ignition_on` | Ignition turned on | Location |
| `ignition_off` | Ignition turned off | Location, trip summary |
| `geofence_enter` | Entered geofence | Geofence ID, location |
| `geofence_exit` | Exited geofence | Geofence ID, location |
| `overspeed` | Speed threshold exceeded | Speed, limit, location |
| `harsh_brake` | Harsh braking detected | Deceleration, location |
| `harsh_accel` | Harsh acceleration | Acceleration, location |
| `device_tamper` | Tampering detected | Alert type |
| `low_battery` | Battery below threshold | Voltage |
| `power_cut` | Main power disconnected | Battery voltage |
| `power_restore` | Main power restored | Voltage |
| `sos` | SOS button pressed | Location |

---

## 8.5 Status Message (Device → Cloud)

```json
{
  "ts": 1707580800,
  "fw": "1.2.3",
  "up": 86400,
  "mem": 102400,
  "gsm": 85,
  "gps": "fixed",
  "bat": 12.6,
  "tmp": 45.2
}
```

### Status Fields

| Field | Type | Description |
|-------|------|-------------|
| ts | int64 | Timestamp |
| fw | string | Firmware version |
| up | int64 | Uptime in seconds |
| mem | uint32 | Free memory in bytes |
| gsm | int | GSM signal strength (0-100) |
| gps | string | GPS status: "fixed", "searching", "error" |
| bat | float32 | Battery voltage |
| tmp | float32 | Device temperature |

---

## 8.6 Configuration Message (Cloud → Device)

```json
{
  "reporting_interval": 30,
  "status_interval": 300,
  "geofences": [
    {
      "id": 1,
      "lat": 28.6139,
      "lng": 77.2090,
      "radius": 100,
      "type": "home"
    }
  ],
  "speed_limit": 80,
  "low_battery_threshold": 11.5
}
```

---

[← Previous: Security Architecture](./07-security-architecture.md) | [Next: Backend Integration →](./09-backend-integration.md)

