# 3. Entity Relationships

## 3.1 Entity Model

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────────┐
│      USER       │       │     VEHICLE     │       │   TRACKER_DEVICE    │
├─────────────────┤       ├─────────────────┤       ├─────────────────────┤
│ id (PK)         │       │ id (PK)         │       │ id (PK)             │
│ name            │──1:N──│ user_id (FK)    │──1:1──│ vehicle_id (FK)     │
│ email           │       │ registration_no │       │ device_serial       │
│ phone           │       │ make            │       │ imei                │
│ created_at      │       │ model           │       │ firmware_version    │
└─────────────────┘       │ year            │       │ status              │
                          │ current_device  │───────│ assigned_at         │
                          │ status          │       │ last_seen           │
                          └─────────────────┘       │ mqtt_client_id      │
                                  │                 └─────────────────────┘
                                  │
                          ┌───────▼───────┐
                          │ DEVICE_HISTORY │
                          ├───────────────┤
                          │ id (PK)       │
                          │ vehicle_id    │
                          │ device_id     │
                          │ assigned_at   │
                          │ removed_at    │
                          │ reason        │
                          └───────────────┘
```

---

## 3.2 Database Schema

```sql
-- Users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'owner',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Vehicles table
CREATE TABLE vehicles (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    registration_number VARCHAR(50) UNIQUE NOT NULL,
    make VARCHAR(100),
    model VARCHAR(100),
    year INTEGER,
    vehicle_type VARCHAR(50),
    current_device_id INTEGER,
    status VARCHAR(20) DEFAULT 'active', -- active, inactive, maintenance
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tracker Devices table
CREATE TABLE tracker_devices (
    id SERIAL PRIMARY KEY,
    device_serial VARCHAR(100) UNIQUE NOT NULL,
    imei VARCHAR(20) UNIQUE NOT NULL,
    vehicle_id INTEGER REFERENCES vehicles(id) ON DELETE SET NULL,
    firmware_version VARCHAR(50),
    hardware_version VARCHAR(50),
    sim_iccid VARCHAR(30),
    status VARCHAR(20) DEFAULT 'unassigned', -- unassigned, active, inactive, faulty
    mqtt_client_id VARCHAR(100) UNIQUE,
    mqtt_username VARCHAR(100),
    last_seen TIMESTAMP,
    assigned_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add FK to vehicles
ALTER TABLE vehicles 
ADD CONSTRAINT fk_current_device 
FOREIGN KEY (current_device_id) REFERENCES tracker_devices(id);

-- Device assignment history
CREATE TABLE device_history (
    id SERIAL PRIMARY KEY,
    vehicle_id INTEGER REFERENCES vehicles(id),
    device_id INTEGER REFERENCES tracker_devices(id),
    assigned_at TIMESTAMP NOT NULL,
    removed_at TIMESTAMP,
    removal_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Telemetry data (TimescaleDB hypertable)
CREATE TABLE telemetry (
    time TIMESTAMPTZ NOT NULL,
    vehicle_id INTEGER NOT NULL,
    device_id INTEGER NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    altitude DOUBLE PRECISION,
    speed DOUBLE PRECISION,
    heading DOUBLE PRECISION,
    accuracy DOUBLE PRECISION,
    satellites INTEGER,
    ignition BOOLEAN,
    battery_voltage DOUBLE PRECISION,
    gsm_signal INTEGER,
    odometer DOUBLE PRECISION,
    raw_data JSONB
);

-- Convert to TimescaleDB hypertable
SELECT create_hypertable('telemetry', 'time');

-- Create indexes
CREATE INDEX idx_telemetry_vehicle_time ON telemetry (vehicle_id, time DESC);
CREATE INDEX idx_telemetry_device_time ON telemetry (device_id, time DESC);
```

---

## 3.3 Key Relationships

| Relationship | Cardinality | Description |
|--------------|-------------|-------------|
| User → Vehicle | 1:N | One user owns multiple vehicles |
| Vehicle → Device | 1:1 | One vehicle has one active tracker at a time |
| Vehicle → DeviceHistory | 1:N | Vehicle can have history of multiple devices |
| Device → Telemetry | 1:N | Device generates time-series telemetry |

---

## 3.4 Device Replacement Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                    DEVICE REPLACEMENT FLOW                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  1. Mark Old Device as Faulty/Replaced                              │
│     ┌──────────────┐                                                │
│     │ Old Device   │ status: active → faulty                        │
│     │ DEV-001      │ vehicle_id: NULL                               │
│     └──────────────┘                                                │
│                                                                     │
│  2. Update Device History                                           │
│     ┌────────────────────────────────────────────────────┐          │
│     │ device_history                                     │          │
│     │ vehicle_id: VEH-001, device_id: DEV-001            │          │
│     │ removed_at: 2026-02-10, reason: "Hardware Failure" │          │
│     └────────────────────────────────────────────────────┘          │
│                                                                     │
│  3. Assign New Device                                               │
│     ┌──────────────┐                                                │
│     │ New Device   │ status: unassigned → active                    │
│     │ DEV-002      │ vehicle_id: VEH-001                            │
│     └──────────────┘                                                │
│                                                                     │
│  4. Update Vehicle                                                  │
│     ┌──────────────┐                                                │
│     │ Vehicle      │ current_device_id: DEV-001 → DEV-002           │
│     │ VEH-001      │                                                │
│     └──────────────┘                                                │
│                                                                     │
│  5. Create New History Entry                                        │
│     ┌────────────────────────────────────────────────────┐          │
│     │ device_history                                     │          │
│     │ vehicle_id: VEH-001, device_id: DEV-002            │          │
│     │ assigned_at: 2026-02-10                            │          │
│     └────────────────────────────────────────────────────┘          │
│                                                                     │
│  ✓ All historical telemetry remains linked to vehicle_id           │
│  ✓ New telemetry flows through new device to same vehicle          │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

[← Previous: System Architecture](./02-system-architecture.md) | [Next: MQTT Topic Design →](./04-mqtt-topic-design.md)

