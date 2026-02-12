# 1. Overview

## 1.1 Purpose

This document describes the MQTT-based architecture for the **IndusJS Fleet Vehicle Tracker System**. The system enables real-time tracking of fleet vehicles using GPS-enabled tracker devices communicating over MQTT protocol.

---

## 1.2 Requirements

### 1.2.1 Functional Requirements

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

### 1.2.2 Non-Functional Requirements

- Strong authentication and authorization
- Device-level isolation
- High availability and scalability
- Offline tolerance and retry mechanism
- Secure communication channel

---

## 1.3 Key Features

| Feature | Description |
|---------|-------------|
| **Multi-Vehicle Support** | One user can own and track multiple vehicles |
| **Device Abstraction** | Vehicle identity remains stable even if tracker device is replaced |
| **Real-Time Telemetry** | GPS location, speed, ignition state, device health |
| **Command & Control** | Remote reboot, configuration update, OTA firmware updates |
| **Offline Tolerance** | Store-and-forward mechanism for network interruptions |
| **Production Scale** | Designed for 20,000+ concurrent devices |

---

## 1.4 Technology Stack

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

---

## 1.5 Memory Requirements

### 1.5.1 Device Memory Budget (Tracker Hardware)

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

### 1.5.2 Server Memory (MQTT Broker Comparison)

| Broker | RAM Usage | Max Connections | Cost | Recommended For |
|--------|-----------|-----------------|------|-----------------|
| **Mosquitto** ⭐ | ~30-50MB | 25,000 | FREE | ✅ <25K devices, low cost |
| **NanoMQ** | ~5-10MB | 10,000 | FREE | ✅ Edge gateway |
| **VerneMQ** | ~200MB+ | 1,000,000 | FREE | Medium-large scale |
| **EMQX** | ~500MB+ | 5,000,000 | FREE | Large scale 50K+ |

**Recommendation:** Use **Mosquitto** for up to 25K devices - it's lightweight, FREE, and supports disk persistence for zero data loss.

---

[Next: System Architecture →](./02-system-architecture.md)

