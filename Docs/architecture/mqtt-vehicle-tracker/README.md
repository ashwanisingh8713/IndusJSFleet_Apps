# Vehicle Tracker System - MQTT Architecture with TinyGo

## Document Version
- **Version**: 1.1.0
- **Date**: February 10, 2026
- **Author**: IndusJS Fleet Team

---

## Overview

This documentation describes the MQTT-based architecture for the **IndusJS Fleet Vehicle Tracker System**. The system enables real-time tracking of fleet vehicles using GPS-enabled tracker devices communicating over MQTT protocol.

### Key Features

| Feature | Description |
|---------|-------------|
| **Multi-Vehicle Support** | One user can own and track multiple vehicles |
| **Device Abstraction** | Vehicle identity remains stable even if tracker device is replaced |
| **Real-Time Telemetry** | GPS location, speed, ignition state, device health |
| **Command & Control** | Remote reboot, configuration update, OTA firmware updates |
| **Offline Tolerance** | Store-and-forward mechanism for network interruptions |
| **Production Scale** | Designed for 20,000+ concurrent devices |
| **Low Memory** | Optimized for devices with 256KB RAM |

---

## Documentation Structure

| File | Description |
|------|-------------|
| [01-overview.md](./01-overview.md) | Purpose, requirements, technology stack, memory requirements |
| [02-system-architecture.md](./02-system-architecture.md) | High-level architecture diagram and component descriptions |
| [03-entity-relationships.md](./03-entity-relationships.md) | Data model, database schema, device replacement flow |
| [04-mqtt-topic-design.md](./04-mqtt-topic-design.md) | Topic hierarchy, QoS settings, ACL rules |
| [05-tinygo-firmware.md](./05-tinygo-firmware.md) | TinyGo firmware architecture and implementation |
| [06-memory-optimized-client.md](./06-memory-optimized-client.md) | Low memory implementation for 256KB devices |
| [07-security-architecture.md](./07-security-architecture.md) | Security layers, device provisioning, credentials |
| [08-message-protocols.md](./08-message-protocols.md) | Telemetry, commands, events, acknowledgments |
| [09-backend-integration.md](./09-backend-integration.md) | Tracking service, location API, Kotlin implementation |
| [10-device-lifecycle.md](./10-device-lifecycle.md) | Device states, assignment API, replacement flow |
| [11-scalability-ha.md](./11-scalability-ha.md) | Broker clustering, scaling metrics, auto-scaling |
| [12-offline-handling.md](./12-offline-handling.md) | Offline buffer, reconnection strategy, detection |
| [13-implementation-roadmap.md](./13-implementation-roadmap.md) | 16-week phased implementation plan |
| [appendix-a-mosquitto-config.md](./appendix-a-mosquitto-config.md) | Mosquitto broker configuration (recommended) |
| [appendix-b-emqx-config.md](./appendix-b-emqx-config.md) | EMQX configuration for large scale (50K+) |
| [appendix-c-monitoring.md](./appendix-c-monitoring.md) | Prometheus metrics and alerts |
| [appendix-d-error-codes.md](./appendix-d-error-codes.md) | Error codes reference |
| [appendix-e-glossary.md](./appendix-e-glossary.md) | Terms and definitions |

---

## Quick Start

### For Device Development (TinyGo)
1. Read [01-overview.md](./01-overview.md) for requirements
2. Read [05-tinygo-firmware.md](./05-tinygo-firmware.md) for firmware architecture
3. Read [06-memory-optimized-client.md](./06-memory-optimized-client.md) for low memory optimization

### For Backend Development
1. Read [02-system-architecture.md](./02-system-architecture.md) for overall design
2. Read [09-backend-integration.md](./09-backend-integration.md) for Kotlin implementation
3. Read [appendix-a-mosquitto-config.md](./appendix-a-mosquitto-config.md) for broker setup

### For DevOps/Infrastructure
1. Read [11-scalability-ha.md](./11-scalability-ha.md) for scaling
2. Read [appendix-a-mosquitto-config.md](./appendix-a-mosquitto-config.md) for deployment
3. Read [appendix-c-monitoring.md](./appendix-c-monitoring.md) for monitoring

---

## Technology Stack

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

## Changelog

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | Feb 10, 2026 | Initial document |
| 1.1.0 | Feb 10, 2026 | Added low-memory support, Mosquitto as primary broker, split into multiple files |

---

*IndusJS Fleet Team*

