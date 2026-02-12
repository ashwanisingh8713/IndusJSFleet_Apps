# Appendix C: Monitoring & Alerting

## C.1 Prometheus Metrics

### Mosquitto Metrics (via mosquitto-exporter)

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'mosquitto'
    static_configs:
      - targets: ['mosquitto-exporter:9234']
    metrics_path: /metrics
    scrape_interval: 15s
```

### Key Mosquitto Metrics

| Metric | Description |
|--------|-------------|
| `mosquitto_clients_connected` | Current connected clients |
| `mosquitto_clients_total` | Total clients seen |
| `mosquitto_messages_received_total` | Messages received |
| `mosquitto_messages_sent_total` | Messages sent |
| `mosquitto_messages_stored` | Queued messages |
| `mosquitto_subscriptions_total` | Active subscriptions |
| `mosquitto_publish_messages_dropped_total` | Dropped messages (CRITICAL) |
| `mosquitto_bytes_received_total` | Bytes received |
| `mosquitto_bytes_sent_total` | Bytes sent |

### EMQX Metrics

| Metric | Description |
|--------|-------------|
| `emqx_connections_count` | Current connections |
| `emqx_messages_received` | Messages received |
| `emqx_messages_sent` | Messages sent |
| `emqx_messages_dropped` | Messages dropped |
| `emqx_subscriptions_count` | Active subscriptions |

---

## C.2 Tracking Service Metrics

```yaml
# Custom application metrics
metrics:
  - tracking_telemetry_received_total
  - tracking_telemetry_processing_duration_seconds
  - tracking_commands_sent_total
  - tracking_commands_acked_total
  - tracking_commands_failed_total
  - tracking_devices_online_count
  - tracking_devices_offline_count
  - tracking_geofence_events_total
```

---

## C.3 Database Metrics

```yaml
# TimescaleDB / PostgreSQL
metrics:
  - pg_stat_database_tup_inserted
  - pg_stat_database_tup_updated
  - pg_stat_database_numbackends
  - pg_database_size_bytes
  - timescaledb_chunks_created_total
  - timescaledb_data_node_bytes
```

---

## C.4 Redis Metrics

```yaml
# Redis
metrics:
  - redis_connected_clients
  - redis_used_memory_bytes
  - redis_commands_processed_total
  - redis_keyspace_hits_total
  - redis_keyspace_misses_total
```

---

## C.5 Prometheus Alert Rules

```yaml
# prometheus-alerts.yml
groups:
  - name: mqtt_alerts
    rules:
      # CRITICAL: Messages being dropped
      - alert: MQTTMessagesDropped
        expr: increase(mosquitto_publish_messages_dropped_total[5m]) > 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "MQTT messages being dropped - potential data loss!"
          description: "{{ $value }} messages dropped in last 5 minutes"
          
      # WARNING: High queued messages
      - alert: MQTTHighQueuedMessages
        expr: mosquitto_messages_stored > 5000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High number of queued messages - backend may be slow"
          description: "{{ $value }} messages currently queued"
          
      # CRITICAL: Broker down
      - alert: MQTTBrokerDown
        expr: up{job="mosquitto"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "MQTT broker is down!"
          
      # WARNING: High connection count
      - alert: MQTTHighConnections
        expr: mosquitto_clients_connected > 20000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "MQTT connections approaching limit"
          description: "{{ $value }} clients connected"

  - name: device_alerts
    rules:
      # WARNING: Device offline
      - alert: DeviceOfflineTooLong
        expr: (time() - device_last_seen_timestamp) > 3600
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Device offline for more than 1 hour"
          
      # CRITICAL: Many devices offline
      - alert: ManyDevicesOffline
        expr: tracking_devices_offline_count > 100
        for: 10m
        labels:
          severity: critical
        annotations:
          summary: "{{ $value }} devices are offline"

  - name: database_alerts
    rules:
      # WARNING: Database connection pool exhausted
      - alert: DatabaseConnectionPoolExhausted
        expr: pg_stat_database_numbackends > 90
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Database connection pool nearly exhausted"
          
      # WARNING: Database size growing fast
      - alert: DatabaseSizeGrowth
        expr: increase(pg_database_size_bytes[1h]) > 1073741824
        for: 1h
        labels:
          severity: warning
        annotations:
          summary: "Database grew by more than 1GB in last hour"
```

---

## C.6 Grafana Dashboard

### Main Dashboard Panels

| Panel | Metric | Visualization |
|-------|--------|---------------|
| Connected Devices | `mosquitto_clients_connected` | Gauge |
| Messages/sec | `rate(mosquitto_messages_received_total[1m])` | Graph |
| Dropped Messages | `mosquitto_publish_messages_dropped_total` | Counter (Alert) |
| Queued Messages | `mosquitto_messages_stored` | Graph |
| Online vs Offline | `tracking_devices_online_count / offline` | Pie |
| Telemetry Latency | `tracking_telemetry_processing_duration_seconds` | Histogram |

### Grafana Dashboard JSON (Import)

```json
{
  "title": "MQTT Vehicle Tracker",
  "panels": [
    {
      "title": "Connected Devices",
      "type": "gauge",
      "targets": [
        { "expr": "mosquitto_clients_connected" }
      ],
      "thresholds": {
        "steps": [
          { "value": 0, "color": "green" },
          { "value": 15000, "color": "yellow" },
          { "value": 22000, "color": "red" }
        ]
      }
    },
    {
      "title": "Messages Per Second",
      "type": "graph",
      "targets": [
        { "expr": "rate(mosquitto_messages_received_total[1m])", "legendFormat": "Received" },
        { "expr": "rate(mosquitto_messages_sent_total[1m])", "legendFormat": "Sent" }
      ]
    },
    {
      "title": "Dropped Messages (CRITICAL)",
      "type": "stat",
      "targets": [
        { "expr": "increase(mosquitto_publish_messages_dropped_total[24h])" }
      ],
      "thresholds": {
        "steps": [
          { "value": 0, "color": "green" },
          { "value": 1, "color": "red" }
        ]
      }
    }
  ]
}
```

---

## C.7 Log Aggregation (Loki)

```yaml
# loki-config.yml
scrape_configs:
  - job_name: mosquitto
    static_configs:
      - targets:
          - localhost
        labels:
          job: mosquitto
          __path__: /var/log/mosquitto/*.log
          
  - job_name: tracking-service
    static_configs:
      - targets:
          - localhost
        labels:
          job: tracking-service
          __path__: /var/log/tracking/*.log
```

---

## C.8 PagerDuty Integration

```yaml
# alertmanager.yml
route:
  receiver: 'pagerduty-critical'
  routes:
    - match:
        severity: critical
      receiver: 'pagerduty-critical'
    - match:
        severity: warning
      receiver: 'slack-warnings'

receivers:
  - name: 'pagerduty-critical'
    pagerduty_configs:
      - service_key: 'your-pagerduty-key'
        severity: critical
        
  - name: 'slack-warnings'
    slack_configs:
      - api_url: 'https://hooks.slack.com/services/xxx'
        channel: '#fleet-alerts'
```

---

[← EMQX Config](./appendix-b-emqx-config.md) | [Next: Error Codes →](./appendix-d-error-codes.md)

