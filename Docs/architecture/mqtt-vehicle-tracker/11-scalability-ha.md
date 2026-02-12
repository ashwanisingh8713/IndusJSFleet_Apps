# 11. Scalability & High Availability

## 11.1 Scaling Strategy by Device Count

| Device Count | Broker | Architecture | Est. Cost/Month |
|--------------|--------|--------------|-----------------|
| 1 - 5,000 | Mosquitto (single) | Single server | $20-50 |
| 5,000 - 25,000 | Mosquitto (HA) | 2 servers + Redis | $100-200 |
| 25,000 - 100,000 | EMQX (cluster) | 3 nodes + Redis | $500-1000 |
| 100,000+ | EMQX (enterprise) | 5+ nodes + HA infra | $2000+ |

---

## 11.2 Mosquitto High Availability Setup

For up to 25,000 devices, use Mosquitto with bridging:

```
┌─────────────────────────────────────────────────────────────────┐
│                    MOSQUITTO HA SETUP                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│                    ┌─────────────────┐                           │
│                    │  Load Balancer  │                           │
│                    │   (HAProxy/NLB) │                           │
│                    └────────┬────────┘                           │
│                             │                                    │
│              ┌──────────────┼──────────────┐                    │
│              │              │              │                    │
│       ┌──────▼──────┐      │       ┌──────▼──────┐              │
│       │  Mosquitto  │◄─────┴──────►│  Mosquitto  │              │
│       │   Primary   │   Bridge     │   Standby   │              │
│       └──────┬──────┘              └──────┬──────┘              │
│              │                            │                      │
│              └────────────┬───────────────┘                      │
│                           │                                      │
│                    ┌──────▼──────┐                               │
│                    │   Shared    │                               │
│                    │   Storage   │ (Persistence)                 │
│                    └─────────────┘                               │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Bridge Configuration

```conf
# mosquitto-primary.conf
connection standby-bridge
address standby.internal:1883
topic # both 2
bridge_protocol_version mqttv50
cleansession false
```

---

## 11.3 EMQX Cluster Setup (50K+ devices)

```
┌─────────────────────────────────────────────────────────────────┐
│                    EMQX CLUSTER                                  │
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
└─────────────────────────────────────────────────────────────────┘
```

---

## 11.4 Scaling Metrics

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Concurrent Connections | Varies by tier | > 80% capacity |
| Messages/second | 100,000+ | > 90% capacity |
| Connection Latency | < 100ms | > 200ms |
| Message Latency | < 50ms | > 100ms |
| CPU Usage | < 70% | > 85% |
| Memory Usage | < 80% | > 90% |
| Message Queue Length | < 1000/client | > 5000/client |

---

## 11.5 Auto-Scaling Configuration (Kubernetes)

```yaml
# tracking-service-hpa.yaml
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

## 11.6 Database Scaling (TimescaleDB)

```sql
-- Partition telemetry by time (1 week chunks)
SELECT set_chunk_time_interval('telemetry', INTERVAL '1 week');

-- Create compression policy (compress data older than 7 days)
ALTER TABLE telemetry SET (
  timescaledb.compress,
  timescaledb.compress_segmentby = 'vehicle_id'
);

SELECT add_compression_policy('telemetry', INTERVAL '7 days');

-- Retention policy (delete data older than 1 year)
SELECT add_retention_policy('telemetry', INTERVAL '1 year');

-- Create continuous aggregates for dashboards
CREATE MATERIALIZED VIEW telemetry_hourly
WITH (timescaledb.continuous) AS
SELECT
  time_bucket('1 hour', time) AS bucket,
  vehicle_id,
  AVG(speed) AS avg_speed,
  MAX(speed) AS max_speed,
  SUM(CASE WHEN ignition THEN 1 ELSE 0 END) AS ignition_on_count
FROM telemetry
GROUP BY bucket, vehicle_id;

-- Refresh policy
SELECT add_continuous_aggregate_policy('telemetry_hourly',
  start_offset => INTERVAL '3 hours',
  end_offset => INTERVAL '1 hour',
  schedule_interval => INTERVAL '1 hour');
```

---

## 11.7 Redis Cluster for Caching

```yaml
# redis-cluster.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: redis-cluster-config
data:
  redis.conf: |
    cluster-enabled yes
    cluster-node-timeout 5000
    appendonly yes
    maxmemory 1gb
    maxmemory-policy allkeys-lru
```

---

## 11.8 Load Testing

```bash
# Using mqttx-cli for load testing
mqttx bench conn \
  --hostname mqtt.indusjs.com \
  --port 8883 \
  --ssl \
  --count 10000 \
  --interval 10

# Publish test
mqttx bench pub \
  --hostname mqtt.indusjs.com \
  --port 8883 \
  --ssl \
  --topic "test/telemetry" \
  --count 10000 \
  --interval 100 \
  --message '{"lat":28.6,"lng":77.2,"spd":45}'
```

---

[← Previous: Device Lifecycle](./10-device-lifecycle.md) | [Next: Offline Handling →](./12-offline-handling.md)

