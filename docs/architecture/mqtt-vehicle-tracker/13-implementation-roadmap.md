# 13. Implementation Roadmap

## 13.1 Overview

Total Duration: **16 weeks** (4 phases)

```
┌─────────────────────────────────────────────────────────────────┐
│                    IMPLEMENTATION TIMELINE                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Phase 1: Foundation        ████████                 (4 weeks)  │
│  Phase 2: Core Features     ████████████████         (4 weeks)  │
│  Phase 3: Advanced          ████████████████████████ (4 weeks)  │
│  Phase 4: Production        ████████████████████████████████    │
│                                                       (4 weeks)  │
│                                                                  │
│  Week: 1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 13.2 Phase 1: Foundation (Weeks 1-4)

### Week 1: Infrastructure Setup

| Task | Owner | Deliverable |
|------|-------|-------------|
| Database schema design | Backend | PostgreSQL + TimescaleDB schema |
| Mosquitto broker setup | DevOps | Configured broker with TLS |
| Development environment | All | Docker Compose stack |
| Project structure | All | Git repos, CI/CD skeleton |

### Week 2: Device Provisioning

| Task | Owner | Deliverable |
|------|-------|-------------|
| Certificate authority setup | Security | CA + certificate generation |
| Device registration API | Backend | POST /devices endpoint |
| Provisioning flow | Backend | Credential generation service |
| MQTT ACL configuration | DevOps | Topic access control |

### Week 3: TinyGo Firmware Skeleton

| Task | Owner | Deliverable |
|------|-------|-------------|
| Project setup | Firmware | TinyGo project structure |
| GPS driver | Firmware | NEO-M8N NMEA parser |
| GSM driver | Firmware | SIM7600 AT command handler |
| MQTT client wrapper | Firmware | Connection management |

### Week 4: Basic Telemetry Flow

| Task | Owner | Deliverable |
|------|-------|-------------|
| Telemetry publishing | Firmware | Device → Broker |
| Telemetry subscription | Backend | Broker → Service |
| Database storage | Backend | TimescaleDB writes |
| End-to-end test | QA | Device to DB flow working |

**Phase 1 Milestone:** Device can connect, send telemetry, and store in database.

---

## 13.3 Phase 2: Core Features (Weeks 5-8)

### Week 5: Real-Time Location API

| Task | Owner | Deliverable |
|------|-------|-------------|
| Redis caching | Backend | Latest location cache |
| Location API | Backend | GET /vehicles/{id}/location |
| Location history API | Backend | Date range queries |
| API documentation | Backend | OpenAPI/Swagger docs |

### Week 6: Command & Control

| Task | Owner | Deliverable |
|------|-------|-------------|
| Command publishing | Backend | Service → Broker → Device |
| Command handling | Firmware | Parse and execute commands |
| Acknowledgment flow | Both | Device → Broker → Service |
| Command status API | Backend | GET /commands/{id}/status |

### Week 7: Device Lifecycle Management

| Task | Owner | Deliverable |
|------|-------|-------------|
| Device assignment API | Backend | Assign/unassign endpoints |
| Device replacement flow | Backend | Replace without data loss |
| Device history | Backend | Track device changes |
| Admin dashboard (basic) | Frontend | Device management UI |

### Week 8: Mobile App Integration

| Task | Owner | Deliverable |
|------|-------|-------------|
| KMP location module | Mobile | Real-time location display |
| Vehicle list with status | Mobile | Online/offline indicators |
| Command sending UI | Mobile | Reboot, locate buttons |
| Push notifications | Mobile | Device offline alerts |

**Phase 2 Milestone:** Full CRUD operations, real-time tracking in mobile app.

---

## 13.4 Phase 3: Advanced Features (Weeks 9-12)

### Week 9: Geofencing Engine

| Task | Owner | Deliverable |
|------|-------|-------------|
| Geofence CRUD API | Backend | Create/update/delete zones |
| Point-in-polygon algorithm | Backend | Efficient boundary check |
| Enter/exit events | Backend | Real-time detection |
| Geofence UI | Mobile | Draw zones on map |

### Week 10: OTA Firmware Updates

| Task | Owner | Deliverable |
|------|-------|-------------|
| Firmware versioning | Firmware | Version management |
| OTA download handler | Firmware | HTTP download + verify |
| Update command | Backend | Trigger OTA via MQTT |
| Rollback mechanism | Firmware | Safe fallback on failure |

### Week 11: Analytics Pipeline

| Task | Owner | Deliverable |
|------|-------|-------------|
| Kafka integration | Backend | Telemetry event stream |
| Trip detection | Analytics | Start/stop trip logic |
| Daily summary | Analytics | Distance, duration stats |
| Dashboard widgets | Frontend | Key metrics display |

### Week 12: Offline Handling

| Task | Owner | Deliverable |
|------|-------|-------------|
| Offline buffer | Firmware | 1000 message ring buffer |
| Flash persistence | Firmware | Critical data backup |
| Reconnection strategy | Firmware | Exponential backoff |
| Buffer flush logic | Firmware | Ordered message replay |

**Phase 3 Milestone:** Complete feature set, ready for testing.

---

## 13.5 Phase 4: Production Readiness (Weeks 13-16)

### Week 13: Security Hardening

| Task | Owner | Deliverable |
|------|-------|-------------|
| Security audit | Security | Vulnerability assessment |
| Penetration testing | Security | External pen test report |
| Credential rotation | Backend | Automated rotation service |
| Audit logging | Backend | All operations logged |

### Week 14: Load Testing

| Task | Owner | Deliverable |
|------|-------|-------------|
| Load test scripts | QA | MQTT load generator |
| 20K device simulation | QA | Performance report |
| Bottleneck identification | All | Optimization backlog |
| Database tuning | Backend | Query optimization |

### Week 15: Monitoring & Alerting

| Task | Owner | Deliverable |
|------|-------|-------------|
| Prometheus setup | DevOps | Metrics collection |
| Grafana dashboards | DevOps | Visual monitoring |
| Alert rules | DevOps | PagerDuty integration |
| Log aggregation | DevOps | ELK/Loki stack |

### Week 16: Documentation & Deployment

| Task | Owner | Deliverable |
|------|-------|-------------|
| Runbook creation | DevOps | Operational procedures |
| Deployment automation | DevOps | Terraform/Ansible scripts |
| User documentation | Tech Writer | Admin guide, API docs |
| Production deployment | All | Live system! |

**Phase 4 Milestone:** Production-ready system deployed and monitored.

---

## 13.6 Resource Requirements

| Role | Phase 1 | Phase 2 | Phase 3 | Phase 4 |
|------|---------|---------|---------|---------|
| **Firmware Engineer** | 1 | 1 | 1 | 0.5 |
| **Backend Engineer** | 1 | 2 | 2 | 1 |
| **Mobile Engineer** | 0 | 1 | 1 | 0.5 |
| **DevOps Engineer** | 0.5 | 0.5 | 0.5 | 1 |
| **QA Engineer** | 0.5 | 1 | 1 | 1 |
| **Total FTE** | 3 | 5.5 | 5.5 | 4 |

---

## 13.7 Risk Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Hardware delays | High | Order dev kits early, use simulators |
| MQTT scalability | Medium | Load test early, have EMQX as fallback |
| Cell coverage gaps | Medium | Aggressive offline buffering |
| Security breach | Critical | Penetration testing, security review |
| Firmware bugs | High | OTA update capability from day 1 |

---

## 13.8 Success Criteria

| Metric | Target |
|--------|--------|
| Device connection success rate | > 99.5% |
| Message delivery rate | > 99.9% |
| End-to-end latency | < 2 seconds |
| System uptime | > 99.9% |
| Concurrent devices supported | > 20,000 |
| Mean time to recovery | < 5 minutes |

---

[← Previous: Offline Handling](./12-offline-handling.md) | [Appendix: Mosquitto Config →](./appendix-a-mosquitto-config.md)

