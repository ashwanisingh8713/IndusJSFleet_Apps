# Vehicle Tracker System - MQTT Architecture with TinyGo

## Document Version
- **Version**: 1.0.0
- **Date**: February 10, 2026
- **Author**: IndusJS Fleet Team

---

## Table of Contents
1. [Overview](#1-overview)
2. [System Architecture](#2-system-architecture)
3. [Entity Relationships](#3-entity-relationships)
4. [MQTT Topic Design](#4-mqtt-topic-design)
5. [TinyGo Device Firmware](#5-tinygo-device-firmware)
6. [Security Architecture](#6-security-architecture)
7. [Message Protocols](#7-message-protocols)
8. [Backend Integration](#8-backend-integration)
9. [Device Lifecycle Management](#9-device-lifecycle-management)
10. [Scalability & High Availability](#10-scalability--high-availability)
11. [Offline Handling & Retry](#11-offline-handling--retry)
12. [Implementation Roadmap](#12-implementation-roadmap)

---

## 1. Overview

### 1.1 Purpose
This document describes the MQTT-based architecture for the **IndusJS Fleet Vehicle Tracker System**. The system enables real-time tracking of fleet vehicles using GPS-enabled tracker devices communicating over MQTT protocol.

### 1.2 Requirements

#### 1.2.1 Functional Requirements
- A user can own multiple vehicles
- Each vehicle has one active tracker device
- A tracker device can be updated or replaced if hardware fails
- Vehicle identity must remain stable even if device changes
- Real-time telemetry support:
  - GPS location
  - Speed
  - Ignition state
  - Device health
- Support command & control:
  - Reboot
  - Configuration update
  - Firmware update (OTA)

#### 1.2.2 Non-Functional Requirements
- Strong authentication and authorization
- Device-level isolation
- High availability and scalability
- Offline tolerance and retry mechanism
- Secure communication channel

### 1.3 Key Features

| Feature | Description |
|---------|-------------|
| **Multi-Vehicle Support** | One user can own and track multiple vehicles |
| **Device Abstraction** | Vehicle identity remains stable even if tracker device is replaced |
| **Real-Time Telemetry** | GPS location, speed, ignition state, device health |
| **Command & Control** | Remote reboot, configuration update, OTA firmware updates |
| **Offline Tolerance** | Store-and-forward mechanism for network interruptions |
| **Production Scale** | Designed for 20,000+ concurrent devices |

### 1.4 Technology Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| Device Firmware | **TinyGo** | Lightweight Go for embedded systems |
| Device MQTT Client | **Paho/lwmqtt** | Lightweight client (~15-20KB RAM) |
| Protocol | **MQTT 5.0** | Lightweight pub/sub messaging |
| Broker (Recommended) | **Mosquitto** | Lightweight broker (~30MB RAM) |
| Broker (Large Scale) | **EMQX** | For 50K+ devices (higher memory) |
| Backend | Kotlin/Spring Boot | API and business logic |
| Database | PostgreSQL + TimescaleDB | Relational + Time-series data |
| Cache | Redis | Session, device state, pub/sub |
| Message Queue | Apache Kafka | Event streaming for analytics |

### 1.5 Memory Requirements

#### 1.5.1 Device Memory Budget (Tracker Hardware)

| Component | RAM Usage | Notes |
|-----------|-----------|-------|
| TinyGo Runtime | ~30KB | Minimal runtime overhead |
| MQTT Client | ~15-20KB | Paho or lwmqtt library |
| GPS Driver | ~5KB | NMEA parser |
| GSM/LTE Driver | ~10KB | AT command handler |
| Offline Buffer | ~10-15KB | 50-100 messages |
| Application Logic | ~20KB | Business logic |
| Stack & Heap | ~50KB | Runtime allocation |
| **TOTAL** | **~150KB** | ✅ Fits in 256KB device |

#### 1.5.2 Server Memory (MQTT Broker Comparison)

| Broker | RAM Usage | Max Connections | Cost | Recommended For |
|--------|-----------|-----------------|------|-----------------|
| **Mosquitto** ⭐ | ~30-50MB | 25,000 | FREE | ✅ <25K devices, low cost |
| **NanoMQ** | ~5-10MB | 10,000 | FREE | ✅ Edge gateway |
| **VerneMQ** | ~200MB+ | 1,000,000 | FREE | Medium-large scale |
| **EMQX** | ~500MB+ | 5,000,000 | FREE | Large scale 50K+ |

**Recommendation:** Use **Mosquitto** for up to 25K devices - it's lightweight, FREE, and supports disk persistence for zero data loss.

---

## 2. System Architecture

### 2.1 High-Level Architecture

```
                              CLOUD INFRASTRUCTURE
  ┌────────────────────────────────────────────────────────────────────┐
  │                                                                    │
  │  ┌──────────┐    ┌──────────┐    ┌──────────┐                     │
  │  │  Mobile  │    │   Web    │    │  Admin   │                     │
  │  │   App    │    │Dashboard │    │  Portal  │                     │
  │  │  (KMP)   │    │          │    │          │                     │
  │  └────┬─────┘    └────┬─────┘    └────┬─────┘                     │
  │       └───────────────┼───────────────┘                           │
  │                       │                                            │
  │               ┌───────▼───────┐                                    │
  │               │  API Gateway  │                                    │
  │               │  (Kong/Nginx) │                                    │
  │               └───────┬───────┘                                    │
  │                       │                                            │
  │       ┌───────────────┼───────────────┐                           │
  │       │               │               │                           │
  │  ┌────▼────┐    ┌────▼────┐    ┌────▼────┐                       │
  │  │Fleet API│    │Tracking │    │Analytics│                       │
  │  │ Service │    │ Service │    │ Service │                       │
  │  └────┬────┘    └────┬────┘    └────┬────┘                       │
  │       └───────────────┼───────────────┘                           │
  │                       │                                            │
│               ┌───────▼───────┐                                    │
│               │  MQTT Broker  │◄────────────────────┐             │
│               │  (Mosquitto)  │                     │             │
│               └───────┬───────┘                     │             │
  │                       │                             │             │
  │       ┌───────────────┼───────────────┐            │             │
  │       │               │               │            │             │
  │  ┌────▼────┐    ┌────▼────┐    ┌────▼────┐       │             │
  │  │PostgreSQL│   │  Redis  │    │  Kafka  │       │             │
  │  │+Timescale│   │  Cache  │    │ Stream  │       │             │
  │  └──────────┘   └─────────┘    └─────────┘       │             │
  │                                                   │             │
  └───────────────────────────────────────────────────┼─────────────┘
                                                      │
                      MQTT over TLS (Port 8883)       │
                                                      │
        ┌─────────────────────────────────────────────┘
        │                    │                    │
  ┌─────▼─────┐        ┌─────▼─────┐        ┌─────▼─────┐
  │  Tracker  │        │  Tracker  │        │  Tracker  │
  │  Device 1 │        │  Device 2 │        │  Device N │
  │  (TinyGo) │        │  (TinyGo) │        │  (TinyGo) │
  └─────┬─────┘        └─────┬─────┘        └─────┬─────┘
        │                    │                    │
  ┌─────▼─────┐        ┌─────▼─────┐        ┌─────▼─────┐
  │ Vehicle A │        │ Vehicle B │        │ Vehicle N │
  └───────────┘        └───────────┘        └───────────┘
```

### 2.2 Component Description

| Component | Responsibility |
|-----------|----------------|
| **Tracker Device** | GPS acquisition, telemetry collection, MQTT communication |
| **MQTT Broker** | Message routing, authentication, QoS management |
| **Tracking Service** | Real-time location processing, geofencing, alerts |
| **Fleet API** | Vehicle/device management, user operations |
| **Analytics Service** | Historical data analysis, reports, ML insights |
| **Redis Cache** | Device state, session management, rate limiting |
| **TimescaleDB** | Time-series telemetry storage |
| **Kafka** | Event streaming for decoupled processing |

---

## 3. Entity Relationships

### 3.1 Entity Model

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

### 3.2 Database Schema

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

### 3.3 Key Relationships

| Relationship | Cardinality | Description |
|--------------|-------------|-------------|
| User → Vehicle | 1:N | One user owns multiple vehicles |
| Vehicle → Device | 1:1 | One vehicle has one active tracker at a time |
| Vehicle → DeviceHistory | 1:N | Vehicle can have history of multiple devices |
| Device → Telemetry | 1:N | Device generates time-series telemetry |

### 3.4 Device Replacement Flow

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

## 4. MQTT Topic Design

### 4.1 Topic Hierarchy

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

### 4.2 Topic Details

| Topic | Direction | QoS | Retained | Description |
|-------|-----------|-----|----------|-------------|
| `devices/{id}/telemetry` | Device → Cloud | 1 | No | GPS, speed, ignition data |
| `devices/{id}/status` | Device → Cloud | 1 | Yes | Device health, battery, signal |
| `devices/{id}/events` | Device → Cloud | 1 | No | Ignition on/off, geofence, alerts |
| `devices/{id}/commands` | Cloud → Device | 2 | No | Reboot, buzzer, lock commands |
| `devices/{id}/config` | Cloud → Device | 2 | Yes | Reporting interval, geofences |
| `devices/{id}/ota` | Cloud → Device | 2 | No | Firmware update commands |
| `devices/{id}/ack` | Device → Cloud | 1 | No | Command acknowledgments |

### 4.3 Topic Access Control (ACL)

```yaml
# Device ACL Rules
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

# Backend Service ACL
backend_acl:
  - client_id_pattern: "backend-service-*"
    rules:
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

---

## 5. TinyGo Device Firmware

### 5.1 Why TinyGo?

| Advantage | Description |
|-----------|-------------|
| **Small Binary Size** | ~50KB vs ~2MB for standard Go |
| **Low Memory** | Runs on devices with 256KB RAM |
| **Go Ecosystem** | Use familiar Go syntax and patterns |
| **Cross-Platform** | Supports ESP32, STM32, nRF52, etc. |
| **Concurrency** | Goroutines for concurrent operations |
| **Type Safety** | Compile-time error detection |

### 5.2 Hardware Requirements

| Component | Specification |
|-----------|---------------|
| MCU | ESP32-S3 or STM32L4 |
| RAM | Minimum 256KB |
| Flash | Minimum 2MB |
| GPS | u-blox NEO-M8N or similar |
| GSM/LTE | SIM7600 or Quectel EC25 |
| Power | 12V-24V automotive input |

### 5.3 Firmware Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    TINYGO FIRMWARE ARCHITECTURE                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                      MAIN LOOP                               ││
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐         ││
│  │  │  GPS    │  │ Sensor  │  │  MQTT   │  │ Command │         ││
│  │  │ Task    │  │ Task    │  │ Task    │  │ Handler │         ││
│  │  └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘         ││
│  │       │            │            │            │               ││
│  └───────┼────────────┼────────────┼────────────┼───────────────┘│
│          │            │            │            │                │
│  ┌───────▼────────────▼────────────▼────────────▼───────────────┐│
│  │                    MESSAGE QUEUE (Ring Buffer)               ││
│  └──────────────────────────────────────────────────────────────┘│
│                              │                                   │
│  ┌───────────────────────────▼──────────────────────────────────┐│
│  │                    DRIVERS LAYER                             ││
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐         ││
│  │  │  GPS    │  │  GSM    │  │ Storage │  │  ADC    │         ││
│  │  │ Driver  │  │ Driver  │  │ Driver  │  │ Driver  │         ││
│  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘         ││
│  └──────────────────────────────────────────────────────────────┘│
│                              │                                   │
│  ┌───────────────────────────▼──────────────────────────────────┐│
│  │                    HARDWARE ABSTRACTION                      ││
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐         ││
│  │  │  UART   │  │  SPI    │  │  I2C    │  │  GPIO   │         ││
│  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘         ││
│  └──────────────────────────────────────────────────────────────┘│
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 5.4 TinyGo Code Structure

```
tracker-firmware/
├── main.go
├── go.mod
├── config/
│   └── config.go
├── drivers/
│   ├── gps/
│   │   └── neo_m8n.go
│   ├── gsm/
│   │   └── sim7600.go
│   └── storage/
│       └── flash.go
├── mqtt/
│   ├── client.go
│   ├── messages.go
│   └── topics.go
├── telemetry/
│   ├── collector.go
│   └── types.go
├── commands/
│   └── handler.go
└── utils/
    ├── buffer.go
    └── crc.go
```

### 5.5 Core Firmware Code

```go
// main.go
package main

import (
    "time"
    "machine"
    "tracker/config"
    "tracker/drivers/gps"
    "tracker/drivers/gsm"
    "tracker/mqtt"
    "tracker/telemetry"
    "tracker/commands"
)

var (
    cfg          *config.Config
    gpsDriver    *gps.Driver
    gsmDriver    *gsm.Driver
    mqttClient   *mqtt.Client
    cmdHandler   *commands.Handler
)

func main() {
    // Initialize configuration
    cfg = config.Load()
    
    // Initialize hardware drivers
    initDrivers()
    
    // Initialize MQTT client
    initMQTT()
    
    // Start main loop
    mainLoop()
}

func initDrivers() {
    // GPS initialization
    gpsDriver = gps.New(machine.UART1, 9600)
    gpsDriver.Start()
    
    // GSM/LTE initialization
    gsmDriver = gsm.New(machine.UART2, 115200)
    gsmDriver.Init(cfg.APN, cfg.APNUser, cfg.APNPass)
    
    // Wait for network registration
    for !gsmDriver.IsRegistered() {
        time.Sleep(time.Second)
    }
}

func initMQTT() {
    mqttClient = mqtt.NewClient(mqtt.Config{
        Broker:       cfg.MQTTBroker,
        Port:         cfg.MQTTPort,
        ClientID:     cfg.DeviceID,
        Username:     cfg.MQTTUsername,
        Password:     cfg.MQTTPassword,
        UseTLS:       true,
        CleanSession: false,
    })
    
    // Connect with retry
    for {
        if err := mqttClient.Connect(); err == nil {
            break
        }
        time.Sleep(5 * time.Second)
    }
    
    // Subscribe to command topics
    mqttClient.Subscribe(cfg.CommandTopic, mqtt.QoS2, handleCommand)
    mqttClient.Subscribe(cfg.ConfigTopic, mqtt.QoS2, handleConfig)
    mqttClient.Subscribe(cfg.OTATopic, mqtt.QoS2, handleOTA)
    
    // Initialize command handler
    cmdHandler = commands.NewHandler(mqttClient, cfg)
}

func mainLoop() {
    telemetryTicker := time.NewTicker(time.Duration(cfg.ReportingInterval) * time.Second)
    statusTicker := time.NewTicker(5 * time.Minute)
    
    for {
        select {
        case <-telemetryTicker.C:
            sendTelemetry()
            
        case <-statusTicker.C:
            sendStatus()
        }
    }
}

func sendTelemetry() {
    loc := gpsDriver.GetLocation()
    if loc == nil {
        return // No valid GPS fix
    }
    
    msg := telemetry.Message{
        Timestamp:  time.Now().Unix(),
        Latitude:   loc.Latitude,
        Longitude:  loc.Longitude,
        Altitude:   loc.Altitude,
        Speed:      loc.Speed,
        Heading:    loc.Heading,
        Accuracy:   loc.HDOP,
        Satellites: loc.Satellites,
        Ignition:   readIgnitionState(),
        Battery:    readBatteryVoltage(),
    }
    
    data, _ := msg.Marshal()
    mqttClient.Publish(cfg.TelemetryTopic, mqtt.QoS1, false, data)
}

func sendStatus() {
    status := telemetry.DeviceStatus{
        Timestamp:       time.Now().Unix(),
        FirmwareVersion: cfg.FirmwareVersion,
        Uptime:          getUptime(),
        FreeMemory:      getFreeMemory(),
        GSMSignal:       gsmDriver.GetSignalStrength(),
        GPSStatus:       gpsDriver.GetStatus(),
        BatteryVoltage:  readBatteryVoltage(),
        Temperature:     readTemperature(),
    }
    
    data, _ := status.Marshal()
    mqttClient.Publish(cfg.StatusTopic, mqtt.QoS1, true, data)
}

func handleCommand(topic string, payload []byte) {
    cmd, err := commands.Parse(payload)
    if err != nil {
        return
    }
    
    result := cmdHandler.Execute(cmd)
    
    // Send acknowledgment
    ack := commands.Ack{
        CommandID: cmd.ID,
        Status:    result.Status,
        Message:   result.Message,
        Timestamp: time.Now().Unix(),
    }
    
    data, _ := ack.Marshal()
    mqttClient.Publish(cfg.AckTopic, mqtt.QoS1, false, data)
}

func handleConfig(topic string, payload []byte) {
    newCfg, err := config.ParseUpdate(payload)
    if err != nil {
        return
    }
    
    // Apply configuration
    cfg.Apply(newCfg)
    cfg.Save()
}

func handleOTA(topic string, payload []byte) {
    ota, err := commands.ParseOTA(payload)
    if err != nil {
        return
    }
    
    // Download and verify firmware
    cmdHandler.ExecuteOTA(ota)
}

// Hardware reading functions
func readIgnitionState() bool {
    return machine.D5.Get() // GPIO pin for ignition
}

func readBatteryVoltage() float32 {
    // ADC reading with voltage divider calculation
    adc := machine.ADC{Pin: machine.ADC0}
    adc.Configure(machine.ADCConfig{})
    raw := adc.Get()
    return float32(raw) * 3.3 / 65535 * 11 // Voltage divider ratio
}
```

### 5.6 Telemetry Message Types

```go
// telemetry/types.go
package telemetry

import "encoding/json"

// Message represents GPS telemetry data
type Message struct {
    Timestamp  int64   `json:"ts"`
    Latitude   float64 `json:"lat"`
    Longitude  float64 `json:"lng"`
    Altitude   float64 `json:"alt,omitempty"`
    Speed      float64 `json:"spd"`
    Heading    float64 `json:"hdg"`
    Accuracy   float64 `json:"acc,omitempty"`
    Satellites int     `json:"sat,omitempty"`
    Ignition   bool    `json:"ign"`
    Battery    float32 `json:"bat"`
}

// Marshal serializes message to compact JSON
func (m *Message) Marshal() ([]byte, error) {
    return json.Marshal(m)
}

// DeviceStatus represents device health status
type DeviceStatus struct {
    Timestamp       int64   `json:"ts"`
    FirmwareVersion string  `json:"fw"`
    Uptime          int64   `json:"up"`
    FreeMemory      uint32  `json:"mem"`
    GSMSignal       int     `json:"gsm"`
    GPSStatus       string  `json:"gps"` // "fixed", "searching", "error"
    BatteryVoltage  float32 `json:"bat"`
    Temperature     float32 `json:"tmp,omitempty"`
}
```

### 5.7 Memory-Optimized Implementation (For Low Memory Devices)

For devices with limited RAM (256KB or less), use this memory-optimized approach:

#### 5.7.1 Compact Telemetry Format (Binary)

```go
// telemetry/compact.go
// Memory-optimized telemetry - uses 20 bytes vs 80+ bytes JSON

package telemetry

// TelemetryCompact uses fixed-point integers to save memory
// Total size: 20 bytes per message
type TelemetryCompact struct {
    Timestamp  uint32  // Unix timestamp (4 bytes)
    LatInt     int32   // Latitude * 1000000 (4 bytes)
    LngInt     int32   // Longitude * 1000000 (4 bytes)
    SpeedX10   uint16  // Speed * 10 in km/h (2 bytes)
    HeadingX10 uint16  // Heading * 10 in degrees (2 bytes)
    BatteryMv  uint16  // Battery in millivolts (2 bytes)
    Flags      uint8   // Bit flags: ignition, GPS fix, etc. (1 byte)
    Satellites uint8   // Satellite count (1 byte)
}

// Flag bit positions
const (
    FlagIgnition = 1 << 0  // Bit 0: Ignition on/off
    FlagGPSFix   = 1 << 1  // Bit 1: GPS has fix
    FlagMoving   = 1 << 2  // Bit 2: Vehicle moving
    FlagCharging = 1 << 3  // Bit 3: Battery charging
)

// Pack serializes to 20-byte binary format
func (t *TelemetryCompact) Pack(buf []byte) int {
    // Little-endian binary packing - no heap allocation
    buf[0] = byte(t.Timestamp)
    buf[1] = byte(t.Timestamp >> 8)
    buf[2] = byte(t.Timestamp >> 16)
    buf[3] = byte(t.Timestamp >> 24)
    buf[4] = byte(t.LatInt)
    buf[5] = byte(t.LatInt >> 8)
    buf[6] = byte(t.LatInt >> 16)
    buf[7] = byte(t.LatInt >> 24)
    buf[8] = byte(t.LngInt)
    buf[9] = byte(t.LngInt >> 8)
    buf[10] = byte(t.LngInt >> 16)
    buf[11] = byte(t.LngInt >> 24)
    buf[12] = byte(t.SpeedX10)
    buf[13] = byte(t.SpeedX10 >> 8)
    buf[14] = byte(t.HeadingX10)
    buf[15] = byte(t.HeadingX10 >> 8)
    buf[16] = byte(t.BatteryMv)
    buf[17] = byte(t.BatteryMv >> 8)
    buf[18] = t.Flags
    buf[19] = t.Satellites
    return 20
}

// NewCompact creates compact telemetry from GPS data
func NewCompact(lat, lng float64, speed, heading float32, 
                batteryMv uint16, ignition bool, sats uint8) TelemetryCompact {
    var flags uint8 = 0
    if ignition {
        flags |= FlagIgnition
    }
    if sats >= 4 {
        flags |= FlagGPSFix
    }
    if speed > 5.0 {
        flags |= FlagMoving
    }
    
    return TelemetryCompact{
        Timestamp:  uint32(time.Now().Unix()),
        LatInt:     int32(lat * 1000000),
        LngInt:     int32(lng * 1000000),
        SpeedX10:   uint16(speed * 10),
        HeadingX10: uint16(heading * 10),
        BatteryMv:  batteryMv,
        Flags:      flags,
        Satellites: sats,
    }
}
```

#### 5.7.2 Fixed-Size Ring Buffer (No Dynamic Allocation)

```go
// buffer/ring.go
// Zero-allocation ring buffer for offline message storage

package buffer

const (
    MessageSize = 24    // 20 bytes data + 4 bytes header
    BufferCount = 50    // Store 50 messages when offline
)

// RingBuffer uses fixed arrays - no malloc/free
type RingBuffer struct {
    data  [BufferCount][MessageSize]byte
    head  uint8
    tail  uint8
    count uint8
}

// Push adds message to buffer, returns false if dropped
func (rb *RingBuffer) Push(msg []byte) bool {
    if len(msg) > MessageSize {
        return false
    }
    
    if rb.count >= BufferCount {
        // Buffer full - drop oldest
        rb.head = (rb.head + 1) % BufferCount
        rb.count--
    }
    
    // Copy message to fixed buffer
    copy(rb.data[rb.tail][:], msg)
    rb.tail = (rb.tail + 1) % BufferCount
    rb.count++
    return true
}

// Pop retrieves oldest message
func (rb *RingBuffer) Pop(buf []byte) bool {
    if rb.count == 0 {
        return false
    }
    
    copy(buf, rb.data[rb.head][:])
    rb.head = (rb.head + 1) % BufferCount
    rb.count--
    return true
}

// Count returns number of buffered messages
func (rb *RingBuffer) Count() uint8 {
    return rb.count
}

// IsFull returns true if buffer is at capacity
func (rb *RingBuffer) IsFull() bool {
    return rb.count >= BufferCount
}
```

#### 5.7.3 Memory Usage Summary

| Component | Standard | Optimized | Savings |
|-----------|----------|-----------|---------|
| Telemetry struct | ~80 bytes | 20 bytes | **75%** |
| JSON encoding | ~150 bytes | 0 bytes | **100%** |
| Message buffer (50 msgs) | ~12KB | ~1.2KB | **90%** |
| String allocations | Variable | 0 bytes | **100%** |
| **Total per message** | ~250 bytes | ~24 bytes | **90%** |
```

---

## 6. Security Architecture

### 6.1 Security Layers

```
┌─────────────────────────────────────────────────────────────────┐
│                      SECURITY ARCHITECTURE                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  LAYER 1: TRANSPORT SECURITY                                     │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  • TLS 1.3 encryption for all MQTT connections            │  │
│  │  • Certificate pinning on device                          │  │
│  │  • Mutual TLS (mTLS) for device authentication            │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
│  LAYER 2: AUTHENTICATION                                         │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  • Unique client certificate per device                    │  │
│  │  • JWT tokens for backend services                         │  │
│  │  • Device provisioning with secure enrollment              │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
│  LAYER 3: AUTHORIZATION (ACL)                                    │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  • Topic-level access control                              │  │
│  │  • Device can only access own topics                       │  │
│  │  • Backend has controlled wildcard access                  │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
│  LAYER 4: MESSAGE SECURITY                                       │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  • Message signing (HMAC-SHA256)                           │  │
│  │  • Replay attack prevention (timestamp + nonce)            │  │
│  │  • Payload encryption for sensitive commands               │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
│  LAYER 5: DEVICE SECURITY                                        │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  • Secure boot                                             │  │
│  │  • Encrypted credential storage                            │  │
│  │  • Firmware signature verification                         │  │
│  │  • Watchdog timer                                          │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 6.2 Device Provisioning Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                   DEVICE PROVISIONING FLOW                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  FACTORY PROVISIONING                                            │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 1. Generate unique device key pair on secure element        │ │
│  │ 2. Create CSR (Certificate Signing Request)                 │ │
│  │ 3. Sign CSR with IndusJS Fleet CA                           │ │
│  │ 4. Flash certificate to device secure storage               │ │
│  │ 5. Register device in provisioning database                 │ │
│  └────────────────────────────────────────────────────────────┘ │
│                              │                                   │
│                              ▼                                   │
│  FIELD ACTIVATION                                                │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 1. Installer scans device QR code (contains device_serial)  │ │
│  │ 2. App sends activation request to backend                  │ │
│  │ 3. Backend validates device exists and is unassigned        │ │
│  │ 4. Generate MQTT credentials (username/password)            │ │
│  │ 5. Push credentials to device via bootstrap topic           │ │
│  │ 6. Device connects with new credentials                     │ │
│  │ 7. Backend assigns device to vehicle                        │ │
│  └────────────────────────────────────────────────────────────┘ │
│                              │                                   │
│                              ▼                                   │
│  OPERATIONAL                                                     │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ • Device uses mTLS + username/password for MQTT             │ │
│  │ • Credentials rotated every 30 days                         │ │
│  │ • Certificate renewed annually                               │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 6.3 Credential Management

```go
// Device credentials structure
type DeviceCredentials struct {
    DeviceID      string    `json:"device_id"`
    MQTTUsername  string    `json:"mqtt_username"`
    MQTTPassword  string    `json:"mqtt_password"` // Hashed with bcrypt
    Certificate   []byte    `json:"-"`             // X.509 certificate
    PrivateKey    []byte    `json:"-"`             // Stored in secure element
    ExpiresAt     time.Time `json:"expires_at"`
    CreatedAt     time.Time `json:"created_at"`
}
```

---

## 7. Message Protocols

### 7.1 Telemetry Message (Device → Cloud)

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

**Field Specifications:**

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

### 7.2 Command Message (Cloud → Device)

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

**Command Types:**

| Type | Parameters | Description |
|------|------------|-------------|
| `reboot` | none | Restart device |
| `buzzer` | `{ "duration": 5, "pattern": "sos" }` | Activate buzzer |
| `config_update` | `{ "interval": 30 }` | Update settings |
| `locate` | none | Force immediate location report |
| `ota_start` | `{ "url": "...", "hash": "..." }` | Start firmware update |

### 7.3 Acknowledgment Message (Device → Cloud)

```json
{
  "cmd_id": "cmd-uuid-12345",
  "status": "success",
  "message": "Command executed successfully",
  "ts": 1707580805,
  "data": {}
}
```

**Status Codes:**

| Status | Description |
|--------|-------------|
| `success` | Command executed successfully |
| `failed` | Command execution failed |
| `rejected` | Command rejected (invalid params) |
| `timeout` | Command timed out |
| `pending` | Command queued for execution |

### 7.4 Event Message (Device → Cloud)

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

**Event Types:**

| Event | Trigger | Data |
|-------|---------|------|
| `ignition_on` | Ignition turned on | Location |
| `ignition_off` | Ignition turned off | Location, trip summary |
| `geofence_enter` | Entered geofence | Geofence ID, location |
| `geofence_exit` | Exited geofence | Geofence ID, location |
| `overspeed` | Speed threshold exceeded | Speed, limit, location |
| `harsh_brake` | Harsh braking detected | Deceleration, location |
| `device_tamper` | Tampering detected | Alert type |
| `low_battery` | Battery below threshold | Voltage |

---

## 8. Backend Integration

### 8.1 Tracking Service Architecture

```kotlin
// TrackingService.kt
@Service
class TrackingService(
    private val mqttClient: MqttClient,
    private val telemetryRepository: TelemetryRepository,
    private val vehicleRepository: VehicleRepository,
    private val deviceRepository: DeviceRepository,
    private val kafkaProducer: KafkaProducer<String, TelemetryEvent>,
    private val redisTemplate: RedisTemplate<String, String>,
    private val alertService: AlertService
) {
    
    private val logger = LoggerFactory.getLogger(TrackingService::class.java)
    
    @PostConstruct
    fun init() {
        // Subscribe to all device telemetry
        mqttClient.subscribe("indusjs/fleet/v1/devices/+/telemetry") { topic, message ->
            handleTelemetry(topic, message)
        }
        
        mqttClient.subscribe("indusjs/fleet/v1/devices/+/status") { topic, message ->
            handleDeviceStatus(topic, message)
        }
        
        mqttClient.subscribe("indusjs/fleet/v1/devices/+/events") { topic, message ->
            handleDeviceEvent(topic, message)
        }
    }
    
    private fun handleTelemetry(topic: String, message: MqttMessage) {
        val deviceId = extractDeviceId(topic)
        val telemetry = parseTelemetry(message.payload)
        
        // Get vehicle for this device
        val device = deviceRepository.findByMqttClientId(deviceId)
            ?: run {
                logger.warn("Unknown device: $deviceId")
                return
            }
        
        val vehicleId = device.vehicleId
            ?: run {
                logger.warn("Device not assigned to vehicle: $deviceId")
                return
            }
        
        // Store in TimescaleDB
        telemetryRepository.save(
            TelemetryEntity(
                time = Instant.ofEpochSecond(telemetry.timestamp),
                vehicleId = vehicleId,
                deviceId = device.id,
                latitude = telemetry.latitude,
                longitude = telemetry.longitude,
                altitude = telemetry.altitude,
                speed = telemetry.speed,
                heading = telemetry.heading,
                accuracy = telemetry.accuracy,
                satellites = telemetry.satellites,
                ignition = telemetry.ignition,
                batteryVoltage = telemetry.battery
            )
        )
        
        // Update Redis cache (latest location)
        val locationJson = objectMapper.writeValueAsString(
            VehicleLocation(
                vehicleId = vehicleId,
                latitude = telemetry.latitude,
                longitude = telemetry.longitude,
                speed = telemetry.speed,
                heading = telemetry.heading,
                ignition = telemetry.ignition,
                timestamp = telemetry.timestamp
            )
        )
        redisTemplate.opsForValue().set(
            "vehicle:location:$vehicleId",
            locationJson,
            Duration.ofMinutes(5)
        )
        
        // Publish to Kafka for analytics
        kafkaProducer.send(
            "telemetry-events",
            vehicleId.toString(),
            TelemetryEvent(vehicleId, device.id, telemetry)
        )
        
        // Check geofences and alerts
        alertService.checkGeofences(vehicleId, telemetry.latitude, telemetry.longitude)
        alertService.checkSpeedLimit(vehicleId, telemetry.speed)
        
        // Update device last_seen
        deviceRepository.updateLastSeen(device.id, Instant.now())
    }
    
    fun sendCommand(deviceId: String, command: DeviceCommand): CommandResult {
        val commandId = UUID.randomUUID().toString()
        val commandMessage = CommandMessage(
            id = commandId,
            type = command.type,
            params = command.params,
            timestamp = Instant.now().epochSecond,
            ttl = command.ttl ?: 300
        )
        
        // Store pending command
        redisTemplate.opsForValue().set(
            "command:pending:$commandId",
            objectMapper.writeValueAsString(commandMessage),
            Duration.ofSeconds(commandMessage.ttl.toLong())
        )
        
        // Publish command
        val topic = "indusjs/fleet/v1/devices/$deviceId/commands"
        mqttClient.publish(topic, commandMessage.toJson(), MqttQos.EXACTLY_ONCE)
        
        // Wait for acknowledgment (with timeout)
        return waitForAck(commandId, command.ttl ?: 300)
    }
}
```

### 8.2 Real-Time Location API

```kotlin
// LocationController.kt
@RestController
@RequestMapping("/api/v2/vehicles")
class LocationController(
    private val redisTemplate: RedisTemplate<String, String>,
    private val telemetryRepository: TelemetryRepository
) {
    
    @GetMapping("/{vehicleId}/location")
    fun getCurrentLocation(@PathVariable vehicleId: Int): ResponseEntity<VehicleLocation> {
        // Try Redis cache first
        val cached = redisTemplate.opsForValue().get("vehicle:location:$vehicleId")
        if (cached != null) {
            return ResponseEntity.ok(objectMapper.readValue(cached, VehicleLocation::class.java))
        }
        
        // Fallback to database
        val latest = telemetryRepository.findLatestByVehicleId(vehicleId)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(
            VehicleLocation(
                vehicleId = vehicleId,
                latitude = latest.latitude,
                longitude = latest.longitude,
                speed = latest.speed,
                heading = latest.heading,
                ignition = latest.ignition,
                timestamp = latest.time.epochSecond
            )
        )
    }
    
    @GetMapping("/{vehicleId}/location/history")
    fun getLocationHistory(
        @PathVariable vehicleId: Int,
        @RequestParam startDate: String,
        @RequestParam endDate: String
    ): ResponseEntity<List<TelemetryPoint>> {
        val start = LocalDate.parse(startDate).atStartOfDay(ZoneOffset.UTC).toInstant()
        val end = LocalDate.parse(endDate).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
        
        val history = telemetryRepository.findByVehicleIdAndTimeBetween(vehicleId, start, end)
        
        return ResponseEntity.ok(history.map { 
            TelemetryPoint(
                timestamp = it.time.epochSecond,
                latitude = it.latitude,
                longitude = it.longitude,
                speed = it.speed,
                ignition = it.ignition
            )
        })
    }
}
```

---

## 9. Device Lifecycle Management

### 9.1 Device States

```
┌─────────────────────────────────────────────────────────────────┐
│                    DEVICE STATE MACHINE                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│     ┌──────────────┐                                             │
│     │  MANUFACTURED │ Initial state after factory                │
│     └───────┬──────┘                                             │
│             │ provision()                                        │
│             ▼                                                    │
│     ┌──────────────┐                                             │
│     │  PROVISIONED │ Credentials loaded, ready for deployment    │
│     └───────┬──────┘                                             │
│             │ activate()                                         │
│             ▼                                                    │
│     ┌──────────────┐         deactivate()      ┌──────────────┐ │
│     │    ACTIVE    │◄─────────────────────────►│   INACTIVE   │ │
│     └───────┬──────┘         activate()        └──────────────┘ │
│             │                                                    │
│             │ reportFault() / replace()                          │
│             ▼                                                    │
│     ┌──────────────┐                                             │
│     │    FAULTY    │ Needs replacement                           │
│     └───────┬──────┘                                             │
│             │ dispose()                                          │
│             ▼                                                    │
│     ┌──────────────┐                                             │
│     │   DISPOSED   │ End of life, credentials revoked            │
│     └──────────────┘                                             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 9.2 Device Assignment API

```kotlin
// DeviceManagementService.kt
@Service
class DeviceManagementService(
    private val deviceRepository: DeviceRepository,
    private val vehicleRepository: VehicleRepository,
    private val deviceHistoryRepository: DeviceHistoryRepository,
    private val mqttCredentialService: MqttCredentialService
) {
    
    @Transactional
    fun assignDeviceToVehicle(
        deviceSerial: String,
        vehicleId: Int,
        assignedBy: Int
    ): DeviceAssignmentResult {
        // 1. Validate device exists and is available
        val device = deviceRepository.findByDeviceSerial(deviceSerial)
            ?: throw NotFoundException("Device not found: $deviceSerial")
        
        if (device.status != DeviceStatus.PROVISIONED && device.status != DeviceStatus.INACTIVE) {
            throw BadRequestException("Device not available. Status: ${device.status}")
        }
        
        // 2. If vehicle has existing device, unassign it first
        vehicleRepository.findById(vehicleId)?.currentDeviceId?.let { existingDeviceId ->
            unassignDeviceFromVehicle(existingDeviceId, "Replaced with new device")
        }
        
        // 3. Generate MQTT credentials
        val credentials = mqttCredentialService.generateCredentials(device.id)
        
        // 4. Assign device to vehicle
        device.apply {
            this.vehicleId = vehicleId
            this.status = DeviceStatus.ACTIVE
            this.mqttClientId = "device-${device.deviceSerial}"
            this.mqttUsername = credentials.username
            this.assignedAt = Instant.now()
        }
        deviceRepository.save(device)
        
        // 5. Update vehicle
        val vehicle = vehicleRepository.findById(vehicleId)!!
        vehicle.currentDeviceId = device.id
        vehicleRepository.save(vehicle)
        
        // 6. Create history entry
        deviceHistoryRepository.save(
            DeviceHistory(
                vehicleId = vehicleId,
                deviceId = device.id,
                assignedAt = Instant.now(),
                assignedBy = assignedBy
            )
        )
        
        return DeviceAssignmentResult(
            deviceId = device.id,
            vehicleId = vehicleId,
            mqttClientId = device.mqttClientId!!,
            status = "assigned"
        )
    }
    
    @Transactional
    fun replaceDevice(
        vehicleId: Int,
        oldDeviceSerial: String,
        newDeviceSerial: String,
        reason: String
    ): DeviceReplacementResult {
        // 1. Unassign old device
        val oldDevice = deviceRepository.findByDeviceSerial(oldDeviceSerial)
            ?: throw NotFoundException("Old device not found: $oldDeviceSerial")
        
        unassignDeviceFromVehicle(oldDevice.id, reason)
        
        // Mark old device as faulty if hardware issue
        if (reason.contains("hardware", ignoreCase = true) || 
            reason.contains("faulty", ignoreCase = true)) {
            oldDevice.status = DeviceStatus.FAULTY
            deviceRepository.save(oldDevice)
        }
        
        // 2. Assign new device
        val result = assignDeviceToVehicle(newDeviceSerial, vehicleId, getCurrentUserId())
        
        return DeviceReplacementResult(
            vehicleId = vehicleId,
            oldDeviceId = oldDevice.id,
            newDeviceId = result.deviceId,
            status = "replaced"
        )
    }
}
```

---

## 10. Scalability & High Availability

### 10.1 MQTT Broker Cluster

```
┌─────────────────────────────────────────────────────────────────┐
│                    MQTT BROKER CLUSTER                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│                    ┌─────────────────┐                           │
│                    │  Load Balancer  │                           │
│                    │   (HAProxy/NLB) │                           │
│                    └────────┬────────┘                           │
│                             │                                    │
│         ┌───────────────────┼───────────────────┐                │
│         │                   │                   │                │
│  ┌──────▼──────┐    ┌──────▼──────┐    ┌──────▼──────┐          │
│  │  EMQX Node  │    │  EMQX Node  │    │  EMQX Node  │          │
│  │  (Primary)  │◄──►│  (Replica)  │◄──►│  (Replica)  │          │
│  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘          │
│         │                   │                   │                │
│         └───────────────────┼───────────────────┘                │
│                             │                                    │
│                    ┌────────▼────────┐                           │
│                    │  Redis Cluster  │ (Session storage)         │
│                    └─────────────────┘                           │
│                                                                  │
│  CLUSTER CONFIGURATION:                                          │
│  • 3 EMQX nodes minimum                                          │
│  • Ekka cluster for auto-discovery                               │
│  • Session data shared via Redis                                 │
│  • Route tables replicated across nodes                          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 10.2 Scaling Metrics

| Metric | Target | Monitoring |
|--------|--------|------------|
| Concurrent Connections | 20,000+ per node | EMQX Dashboard |
| Messages/second | 100,000+ | Prometheus |
| Connection Latency | < 100ms | Grafana |
| Message Latency | < 50ms | Jaeger |
| CPU Usage | < 70% | CloudWatch |
| Memory Usage | < 80% | CloudWatch |

### 10.3 Auto-Scaling Configuration

```yaml
# Kubernetes HPA for tracking service
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: tracking-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: tracking-service
  minReplicas: 3
  maxReplicas: 20
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Pods
      pods:
        metric:
          name: mqtt_messages_per_second
        target:
          type: AverageValue
          averageValue: "5000"
```

---

## 11. Offline Handling & Retry

### 11.1 Device Offline Buffer

```go
// buffer/ring_buffer.go
package buffer

import (
    "sync"
)

const MaxBufferSize = 1000 // Store up to 1000 messages

type RingBuffer struct {
    data  [][]byte
    head  int
    tail  int
    count int
    mu    sync.Mutex
}

func NewRingBuffer() *RingBuffer {
    return &RingBuffer{
        data: make([][]byte, MaxBufferSize),
    }
}

func (rb *RingBuffer) Push(msg []byte) bool {
    rb.mu.Lock()
    defer rb.mu.Unlock()
    
    if rb.count >= MaxBufferSize {
        // Buffer full, drop oldest
        rb.head = (rb.head + 1) % MaxBufferSize
        rb.count--
    }
    
    rb.data[rb.tail] = msg
    rb.tail = (rb.tail + 1) % MaxBufferSize
    rb.count++
    
    return true
}

func (rb *RingBuffer) Pop() ([]byte, bool) {
    rb.mu.Lock()
    defer rb.mu.Unlock()
    
    if rb.count == 0 {
        return nil, false
    }
    
    msg := rb.data[rb.head]
    rb.head = (rb.head + 1) % MaxBufferSize
    rb.count--
    
    return msg, true
}

func (rb *RingBuffer) Count() int {
    rb.mu.Lock()
    defer rb.mu.Unlock()
    return rb.count
}
```

### 11.2 MQTT Reconnection Strategy

```go
// mqtt/reconnect.go
package mqtt

import (
    "time"
    "math"
)

type ReconnectStrategy struct {
    BaseDelay    time.Duration
    MaxDelay     time.Duration
    MaxRetries   int
    attempts     int
    offlineBuffer *buffer.RingBuffer
}

func NewReconnectStrategy() *ReconnectStrategy {
    return &ReconnectStrategy{
        BaseDelay:    1 * time.Second,
        MaxDelay:     5 * time.Minute,
        MaxRetries:   0, // Infinite retries
        offlineBuffer: buffer.NewRingBuffer(),
    }
}

func (rs *ReconnectStrategy) NextDelay() time.Duration {
    delay := time.Duration(math.Pow(2, float64(rs.attempts))) * rs.BaseDelay
    if delay > rs.MaxDelay {
        delay = rs.MaxDelay
    }
    rs.attempts++
    return delay
}

func (rs *ReconnectStrategy) Reset() {
    rs.attempts = 0
}

func (rs *ReconnectStrategy) OnDisconnect(client *Client) {
    go func() {
        for {
            delay := rs.NextDelay()
            time.Sleep(delay)
            
            if err := client.Connect(); err == nil {
                rs.Reset()
                rs.flushBuffer(client)
                return
            }
        }
    }()
}

func (rs *ReconnectStrategy) flushBuffer(client *Client) {
    for {
        msg, ok := rs.offlineBuffer.Pop()
        if !ok {
            break
        }
        
        // Re-publish buffered message
        client.PublishRaw(msg)
    }
}
```

### 11.3 Server-Side Offline Detection

```kotlin
// OfflineDetectionService.kt
@Service
class OfflineDetectionService(
    private val deviceRepository: DeviceRepository,
    private val notificationService: NotificationService,
    private val redisTemplate: RedisTemplate<String, String>
) {
    
    private val offlineThreshold = Duration.ofMinutes(5)
    
    @Scheduled(fixedRate = 60_000) // Run every minute
    fun checkOfflineDevices() {
        val threshold = Instant.now().minus(offlineThreshold)
        
        val offlineDevices = deviceRepository.findByStatusAndLastSeenBefore(
            DeviceStatus.ACTIVE,
            threshold
        )
        
        offlineDevices.forEach { device ->
            val key = "device:offline:notified:${device.id}"
            
            // Only notify once per offline period
            if (redisTemplate.opsForValue().get(key) == null) {
                notificationService.sendDeviceOfflineAlert(device)
                redisTemplate.opsForValue().set(key, "1", Duration.ofHours(1))
            }
        }
    }
    
    fun markDeviceOnline(deviceId: Int) {
        val key = "device:offline:notified:$deviceId"
        redisTemplate.delete(key)
        
        deviceRepository.updateLastSeen(deviceId, Instant.now())
    }
}
```

---

## 12. Implementation Roadmap

### Phase 1: Foundation (4 weeks)

| Week | Tasks |
|------|-------|
| 1 | Database schema design, MQTT broker setup |
| 2 | Device provisioning system, certificate management |
| 3 | TinyGo firmware skeleton, GPS/GSM drivers |
| 4 | Basic telemetry flow (device → broker → backend) |

### Phase 2: Core Features (4 weeks)

| Week | Tasks |
|------|-------|
| 5 | Real-time location API, Redis caching |
| 6 | Command & control implementation |
| 7 | Device lifecycle management APIs |
| 8 | Mobile app integration (KMP) |

### Phase 3: Advanced Features (4 weeks)

| Week | Tasks |
|------|-------|
| 9 | Geofencing engine, alerts |
| 10 | OTA firmware update system |
| 11 | Analytics pipeline (Kafka + TimescaleDB) |
| 12 | Offline handling, buffer management |

### Phase 4: Production Readiness (4 weeks)

| Week | Tasks |
|------|-------|
| 13 | Security hardening, penetration testing |
| 14 | Load testing (20k+ devices) |
| 15 | Monitoring, alerting (Prometheus + Grafana) |
| 16 | Documentation, deployment automation |

---

## Appendix A: Mosquitto Configuration (Recommended - Low Memory)

Mosquitto is the recommended broker for most deployments due to its low memory footprint (~30MB) and built-in persistence for zero data loss.

### A.1 Basic Configuration

```conf
# /etc/mosquitto/mosquitto.conf
# Lightweight configuration for up to 25K devices

# =============================================================================
# GENERAL SETTINGS
# =============================================================================
pid_file /run/mosquitto/mosquitto.pid
user mosquitto

# =============================================================================
# PERSISTENCE (Zero Data Loss)
# =============================================================================
persistence true
persistence_location /var/lib/mosquitto/
persistence_file mosquitto.db
autosave_interval 60
autosave_on_changes true

# =============================================================================
# LISTENERS
# =============================================================================
# Plain MQTT (internal only)
listener 1883 127.0.0.1

# MQTT over TLS (external)
listener 8883
certfile /etc/mosquitto/certs/server.crt
keyfile /etc/mosquitto/certs/server.key
cafile /etc/mosquitto/certs/ca.crt
require_certificate false

# WebSocket over TLS (for web dashboard)
listener 8084
protocol websockets
certfile /etc/mosquitto/certs/server.crt
keyfile /etc/mosquitto/certs/server.key

# =============================================================================
# AUTHENTICATION
# =============================================================================
allow_anonymous false
password_file /etc/mosquitto/passwd
acl_file /etc/mosquitto/acl

# =============================================================================
# PERFORMANCE TUNING
# =============================================================================
max_connections 25000
max_inflight_messages 100
max_queued_messages 10000
message_size_limit 1024
max_keepalive 120

# =============================================================================
# LOGGING
# =============================================================================
log_dest file /var/log/mosquitto/mosquitto.log
log_type error
log_type warning
log_type notice
log_timestamp true
log_timestamp_format %Y-%m-%dT%H:%M:%S
```

### A.2 ACL Configuration

```conf
# /etc/mosquitto/acl
# Access Control List for devices and backend

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

### A.3 Docker Compose Setup

```yaml
# docker-compose.yml
version: '3.8'

services:
  mosquitto:
    image: eclipse-mosquitto:2.0
    container_name: mosquitto
    ports:
      - "1883:1883"
      - "8883:8883"
      - "8084:8084"
    volumes:
      - ./mosquitto/config:/mosquitto/config
      - ./mosquitto/data:/mosquitto/data
      - ./mosquitto/log:/mosquitto/log
      - ./mosquitto/certs:/mosquitto/certs
    restart: unless-stopped
    
  # Optional: Mosquitto Exporter for Prometheus
  mosquitto-exporter:
    image: sapcc/mosquitto-exporter:latest
    container_name: mosquitto-exporter
    ports:
      - "9234:9234"
    environment:
      - BROKER_ENDPOINT=tcp://mosquitto:1883
    depends_on:
      - mosquitto
```

### A.4 Installation Commands

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mosquitto mosquitto-clients

# Create password file
sudo mosquitto_passwd -c /etc/mosquitto/passwd device-001
sudo mosquitto_passwd -b /etc/mosquitto/passwd backend-service secretpassword

# Generate certificates (for TLS)
cd /etc/mosquitto/certs
openssl genrsa -out ca.key 2048
openssl req -new -x509 -days 3650 -key ca.key -out ca.crt
openssl genrsa -out server.key 2048
openssl req -new -key server.key -out server.csr
openssl x509 -req -days 365 -in server.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out server.crt

# Restart service
sudo systemctl restart mosquitto
sudo systemctl enable mosquitto

# Test connection
mosquitto_sub -h localhost -t "test/#" -u backend-service -P secretpassword
mosquitto_pub -h localhost -t "test/hello" -m "world" -u backend-service -P secretpassword
```

---

## Appendix A2: EMQX Configuration (For Large Scale 50K+ Devices)

Use EMQX only if you need to scale beyond 25K devices or require advanced features like rule engine.

```yaml
# emqx.conf (for 50K+ devices)
node:
  name: "emqx@node1.indusjs.com"
  cookie: "secret-cluster-cookie"

cluster:
  name: indusjs-fleet
  discovery_strategy: static
  static:
    seeds:
      - "emqx@node1.indusjs.com"
      - "emqx@node2.indusjs.com"
      - "emqx@node3.indusjs.com"

listeners:
  ssl:
    default:
      bind: "0.0.0.0:8883"
      ssl_options:
        cacertfile: "/etc/emqx/certs/ca.pem"
        certfile: "/etc/emqx/certs/server.pem"
        keyfile: "/etc/emqx/certs/server.key"
        verify: verify_peer
        fail_if_no_peer_cert: true

authentication:
  - mechanism: password_based
    backend: redis
    redis:
      server: "redis-cluster:6379"
      password_hash_algorithm: bcrypt

authorization:
  sources:
    - type: redis
      redis:
        server: "redis-cluster:6379"
        cmd: "HGET mqtt:acl:${clientid}"
```

---

## Appendix B: Monitoring Metrics

```yaml
# Prometheus metrics to collect

# =============================================================================
# MOSQUITTO BROKER METRICS (via mosquitto-exporter)
# =============================================================================
metrics:
  - mosquitto_clients_connected
  - mosquitto_clients_total
  - mosquitto_messages_received_total
  - mosquitto_messages_sent_total
  - mosquitto_messages_stored
  - mosquitto_subscriptions_total
  - mosquitto_publish_messages_dropped_total
  - mosquitto_bytes_received_total
  - mosquitto_bytes_sent_total

# =============================================================================
# EMQX BROKER METRICS (if using EMQX)
# =============================================================================
  - emqx_connections_count
  - emqx_messages_received
  - emqx_messages_sent
  - emqx_messages_dropped
  - emqx_subscriptions_count
  
# =============================================================================
# TRACKING SERVICE METRICS
# =============================================================================
  - tracking_telemetry_received_total
  - tracking_telemetry_processing_duration_seconds
  - tracking_commands_sent_total
  - tracking_commands_acked_total
  - tracking_devices_online_count
  - tracking_devices_offline_count
  
# =============================================================================
# DATABASE METRICS
# =============================================================================
  - timescaledb_chunks_created_total
  - timescaledb_data_node_bytes
  - pg_stat_database_tup_inserted
  - pg_stat_database_tup_updated
  
# =============================================================================
# REDIS METRICS
# =============================================================================
  - redis_connected_clients
  - redis_used_memory_bytes
  - redis_commands_processed_total
```

### B.1 Prometheus Alerts for Zero Data Loss

```yaml
# prometheus-alerts.yml
groups:
  - name: mqtt_alerts
    rules:
      - alert: MQTTMessagesDropped
        expr: increase(mosquitto_publish_messages_dropped_total[5m]) > 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "MQTT messages being dropped - potential data loss!"
          
      - alert: MQTTHighQueuedMessages
        expr: mosquitto_messages_stored > 5000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High number of queued messages - backend may be slow"
          
      - alert: DeviceOfflineTooLong
        expr: (time() - device_last_seen_timestamp) > 3600
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Device offline for more than 1 hour"
          
      - alert: MQTTBrokerDown
        expr: up{job="mosquitto"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "MQTT broker is down!"
```

---

## Appendix C: Error Codes

| Code | Category | Description |
|------|----------|-------------|
| E1001 | Device | GPS fix not available |
| E1002 | Device | GSM registration failed |
| E1003 | Device | MQTT connection failed |
| E1004 | Device | Low battery critical |
| E1005 | Device | Tamper detected |
| E2001 | Backend | Device not found |
| E2002 | Backend | Device not assigned |
| E2003 | Backend | Invalid command |
| E2004 | Backend | Command timeout |
| E3001 | Security | Authentication failed |
| E3002 | Security | Certificate expired |
| E3003 | Security | ACL denied |

---

## Appendix D: Glossary

| Term | Definition |
|------|------------|
| **ACL** | Access Control List - defines topic permissions |
| **mTLS** | Mutual TLS - two-way certificate authentication |
| **OTA** | Over-The-Air - remote firmware updates |
| **QoS** | Quality of Service - message delivery guarantee |
| **HDOP** | Horizontal Dilution of Precision - GPS accuracy |
| **IMEI** | International Mobile Equipment Identity |
| **ICCID** | Integrated Circuit Card Identifier (SIM card) |

---

## Appendix E: Document Changelog

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | Feb 10, 2026 | Initial document |
| 1.1.0 | Feb 10, 2026 | Added low-memory support: Mosquitto as primary broker, memory-optimized TinyGo client, compact binary telemetry format |

---

*Document End - IndusJS Fleet Team*
*Version 1.1.0 - February 10, 2026*

