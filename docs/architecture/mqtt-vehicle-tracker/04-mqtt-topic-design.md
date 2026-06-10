# 4. MQTT Topic Design

## 4.1 Topic Hierarchy

```
indusjs/fleet/v1/
├── devices/{device_id}/
│   ├── telemetry          # Device → Cloud (QoS 1)
│   ├── status             # Device → Cloud (QoS 1, Retained)
│   ├── events             # Device → Cloud (QoS 1)
│   ├── commands           # Cloud → Device (QoS 2)
│   ├── config             # Cloud → Device (QoS 2, Retained)
│   ├── ota                # Cloud → Device (QoS 2)
│   └── ack                # Device → Cloud (QoS 1)
│
├── vehicles/{vehicle_id}/
│   ├── location           # Processed location (for apps)
│   ├── alerts             # Geofence, speed alerts
│   └── status             # Vehicle status aggregation
│
└── users/{user_id}/
    ├── notifications      # Push notifications
    └── fleet-status       # All vehicles summary
```

---

## 4.2 Topic Details

| Topic | Direction | QoS | Retained | Description |
|-------|-----------|-----|----------|-------------|
| `devices/{id}/telemetry` | Device → Cloud | 1 | No | GPS, speed, ignition data |
| `devices/{id}/status` | Device → Cloud | 1 | Yes | Device health, battery, signal |
| `devices/{id}/events` | Device → Cloud | 1 | No | Ignition on/off, geofence, alerts |
| `devices/{id}/commands` | Cloud → Device | 2 | No | Reboot, buzzer, lock commands |
| `devices/{id}/config` | Cloud → Device | 2 | Yes | Reporting interval, geofences |
| `devices/{id}/ota` | Cloud → Device | 2 | No | Firmware update commands |
| `devices/{id}/ack` | Device → Cloud | 1 | No | Command acknowledgments |

---

## 4.3 QoS Levels Explained

```
┌─────────────────────────────────────────────────────────────────┐
│                    MQTT QoS LEVELS                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  QoS 0: AT MOST ONCE (Fire and Forget)                          │
│  ┌─────────┐         ┌─────────┐                                │
│  │ Device  │────────►│ Broker  │  No acknowledgment             │
│  └─────────┘         └─────────┘  Message may be lost           │
│                                   ❌ NOT for critical data       │
│                                                                  │
│  QoS 1: AT LEAST ONCE                                           │
│  ┌─────────┐         ┌─────────┐                                │
│  │ Device  │────────►│ Broker  │                                │
│  └─────────┘         └─────────┘                                │
│       │                   │                                      │
│       │◄─────PUBACK───────│  Message delivered at least once    │
│                              ⚠️ May have duplicates              │
│                                                                  │
│  QoS 2: EXACTLY ONCE ⭐ FOR COMMANDS                            │
│  ┌─────────┐         ┌─────────┐                                │
│  │ Device  │────────►│ Broker  │                                │
│  └─────────┘         └─────────┘                                │
│       │                   │                                      │
│       │◄─────PUBREC───────│  Step 1: Received                   │
│       │──────PUBREL──────►│  Step 2: Release                    │
│       │◄─────PUBCOMP──────│  Step 3: Complete                   │
│                              ✅ Guaranteed exactly once          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4.4 Topic Access Control (ACL)

### 4.4.1 Device ACL Rules

```yaml
device_acl:
  - client_id_pattern: "device-{device_id}"
    rules:
      # Can publish to own topics
      - action: publish
        topic: "indusjs/fleet/v1/devices/{device_id}/telemetry"
        allow: true
      - action: publish
        topic: "indusjs/fleet/v1/devices/{device_id}/status"
        allow: true
      - action: publish
        topic: "indusjs/fleet/v1/devices/{device_id}/events"
        allow: true
      - action: publish
        topic: "indusjs/fleet/v1/devices/{device_id}/ack"
        allow: true
      
      # Can subscribe to own command topics
      - action: subscribe
        topic: "indusjs/fleet/v1/devices/{device_id}/commands"
        allow: true
      - action: subscribe
        topic: "indusjs/fleet/v1/devices/{device_id}/config"
        allow: true
      - action: subscribe
        topic: "indusjs/fleet/v1/devices/{device_id}/ota"
        allow: true
      
      # Deny all other topics
      - action: all
        topic: "#"
        allow: false
```

### 4.4.2 Backend Service ACL Rules

```yaml
backend_acl:
  - client_id_pattern: "backend-service-*"
    rules:
      # Can subscribe to all device topics
      - action: subscribe
        topic: "indusjs/fleet/v1/devices/+/telemetry"
        allow: true
      - action: subscribe
        topic: "indusjs/fleet/v1/devices/+/status"
        allow: true
      - action: subscribe
        topic: "indusjs/fleet/v1/devices/+/events"
        allow: true
      - action: subscribe
        topic: "indusjs/fleet/v1/devices/+/ack"
        allow: true
      
      # Can publish commands to all devices
      - action: publish
        topic: "indusjs/fleet/v1/devices/+/commands"
        allow: true
      - action: publish
        topic: "indusjs/fleet/v1/devices/+/config"
        allow: true
      - action: publish
        topic: "indusjs/fleet/v1/devices/+/ota"
        allow: true
```

### 4.4.3 Mosquitto ACL File Format

```conf
# /etc/mosquitto/acl

# Device pattern - can only access own topics
pattern write indusjs/fleet/v1/devices/%c/telemetry
pattern write indusjs/fleet/v1/devices/%c/status
pattern write indusjs/fleet/v1/devices/%c/events
pattern write indusjs/fleet/v1/devices/%c/ack
pattern read indusjs/fleet/v1/devices/%c/commands
pattern read indusjs/fleet/v1/devices/%c/config
pattern read indusjs/fleet/v1/devices/%c/ota

# Backend service - full access
user backend-service
topic readwrite indusjs/fleet/v1/#
```

---

## 4.5 Topic Naming Best Practices

| Practice | Example | Reason |
|----------|---------|--------|
| Use version prefix | `v1/devices/...` | API versioning |
| Use lowercase | `devices` not `Devices` | Consistency |
| Use plural nouns | `devices` not `device` | RESTful convention |
| Use forward slash | `devices/123/telemetry` | Standard hierarchy |
| Avoid spaces | Use `-` or `_` | URL compatibility |
| Keep short | Limit depth to 5-6 levels | Memory on devices |

---

[← Previous: Entity Relationships](./03-entity-relationships.md) | [Next: TinyGo Firmware →](./05-tinygo-firmware.md)

