# 2. System Architecture

## 2.1 High-Level Architecture

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

---

## 2.2 Component Description

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

## 2.3 Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                       DATA FLOW                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. TELEMETRY FLOW (Device → Cloud)                             │
│     Device → MQTT Broker → Tracking Service → TimescaleDB       │
│                         ↓                                        │
│                     Redis Cache (latest location)               │
│                         ↓                                        │
│                     Kafka (for analytics)                       │
│                                                                  │
│  2. COMMAND FLOW (Cloud → Device)                               │
│     Mobile App → API Gateway → Fleet API → MQTT Broker → Device │
│                                                                  │
│  3. REAL-TIME LOCATION (App Request)                            │
│     Mobile App → API Gateway → Redis Cache → Response           │
│                                   ↓ (cache miss)                │
│                              TimescaleDB                        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2.4 Network Topology

| Connection | Protocol | Port | Security |
|------------|----------|------|----------|
| Device ↔ Broker | MQTT | 8883 | TLS 1.3 |
| App ↔ API Gateway | HTTPS | 443 | TLS 1.3 |
| Services ↔ Broker | MQTT | 1883 | Internal network |
| Services ↔ Database | PostgreSQL | 5432 | TLS + Auth |
| Services ↔ Redis | Redis | 6379 | Auth + Internal |
| Services ↔ Kafka | Kafka | 9092 | SASL + Internal |

---

[← Previous: Overview](./01-overview.md) | [Next: Entity Relationships →](./03-entity-relationships.md)

