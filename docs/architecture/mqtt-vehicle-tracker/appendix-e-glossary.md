# Appendix E: Glossary

## General Terms

| Term | Definition |
|------|------------|
| **Fleet** | A group of vehicles managed by a single organization |
| **Tracker** | GPS device installed in a vehicle for location tracking |
| **Telemetry** | Data transmitted from device to server (location, speed, etc.) |
| **Geofence** | Virtual boundary around a geographic area |

---

## MQTT Terms

| Term | Definition |
|------|------------|
| **MQTT** | Message Queuing Telemetry Transport - lightweight pub/sub protocol |
| **Broker** | Server that routes messages between publishers and subscribers |
| **Topic** | Hierarchical string that categorizes messages (e.g., `devices/123/telemetry`) |
| **QoS** | Quality of Service - message delivery guarantee level (0, 1, or 2) |
| **QoS 0** | At most once - fire and forget, no guarantee |
| **QoS 1** | At least once - guaranteed delivery, may have duplicates |
| **QoS 2** | Exactly once - guaranteed delivery, no duplicates |
| **Publish** | Send a message to a topic |
| **Subscribe** | Register to receive messages from a topic |
| **Retain** | Keep last message on broker for new subscribers |
| **Clean Session** | If false, broker stores session state for reconnection |
| **Keep Alive** | Heartbeat interval to detect connection loss |
| **Last Will** | Message sent by broker if client disconnects unexpectedly |
| **ACL** | Access Control List - defines topic permissions |

---

## Security Terms

| Term | Definition |
|------|------------|
| **TLS** | Transport Layer Security - encryption for network communication |
| **mTLS** | Mutual TLS - two-way certificate authentication |
| **CA** | Certificate Authority - issues digital certificates |
| **CSR** | Certificate Signing Request - request for a certificate |
| **JWT** | JSON Web Token - compact authentication token |
| **HMAC** | Hash-based Message Authentication Code - message signing |

---

## Hardware Terms

| Term | Definition |
|------|------------|
| **MCU** | Microcontroller Unit - embedded processor |
| **GPS** | Global Positioning System - satellite navigation |
| **GNSS** | Global Navigation Satellite System - includes GPS, GLONASS, etc. |
| **GSM** | Global System for Mobile communications - 2G cellular |
| **LTE** | Long Term Evolution - 4G cellular |
| **IMEI** | International Mobile Equipment Identity - unique device identifier |
| **ICCID** | Integrated Circuit Card Identifier - SIM card identifier |
| **NMEA** | National Marine Electronics Association - GPS data format |
| **HDOP** | Horizontal Dilution of Precision - GPS accuracy indicator |
| **ADC** | Analog to Digital Converter - reads analog sensors |
| **UART** | Universal Asynchronous Receiver-Transmitter - serial communication |
| **SPI** | Serial Peripheral Interface - high-speed serial bus |
| **I2C** | Inter-Integrated Circuit - multi-device serial bus |
| **GPIO** | General Purpose Input/Output - digital pins |

---

## Software Terms

| Term | Definition |
|------|------------|
| **TinyGo** | Go compiler for microcontrollers |
| **Goroutine** | Lightweight thread in Go |
| **Ring Buffer** | Circular fixed-size queue |
| **OTA** | Over-The-Air - remote firmware updates |
| **Exponential Backoff** | Retry delay that doubles each attempt |

---

## Database Terms

| Term | Definition |
|------|------------|
| **TimescaleDB** | Time-series database extension for PostgreSQL |
| **Hypertable** | TimescaleDB partitioned table for time-series data |
| **Chunk** | Partition of a hypertable by time range |
| **Continuous Aggregate** | Pre-computed materialized view |
| **Redis** | In-memory key-value store for caching |

---

## Metrics Terms

| Term | Definition |
|------|------------|
| **Prometheus** | Open-source monitoring and alerting system |
| **Grafana** | Visualization and dashboarding platform |
| **Metric** | Numeric value tracked over time |
| **Counter** | Metric that only increases |
| **Gauge** | Metric that can increase or decrease |
| **Histogram** | Distribution of values |

---

## Architecture Terms

| Term | Definition |
|------|------------|
| **HA** | High Availability - system designed to minimize downtime |
| **Clustering** | Multiple servers working together |
| **Load Balancer** | Distributes traffic across servers |
| **Failover** | Automatic switch to backup system |
| **Persistence** | Storing data to survive restarts |

---

## Abbreviations

| Abbreviation | Full Form |
|--------------|-----------|
| API | Application Programming Interface |
| CRUD | Create, Read, Update, Delete |
| DB | Database |
| FK | Foreign Key |
| HA | High Availability |
| PK | Primary Key |
| SSL | Secure Sockets Layer |
| UUID | Universally Unique Identifier |
| WS | WebSocket |

---

[← Error Codes](./appendix-d-error-codes.md) | [Back to README →](./README.md)

