# Appendix A: Mosquitto Configuration (Recommended)

Mosquitto is the recommended broker for most deployments due to its low memory footprint (~30MB) and built-in persistence for zero data loss.

---

## A.1 Basic Configuration

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

---

## A.2 ACL Configuration

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

# Admin - full access
user admin
topic readwrite #
```

---

## A.3 Docker Compose Setup

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
    healthcheck:
      test: ["CMD", "mosquitto_sub", "-t", "$$SYS/#", "-C", "1", "-i", "healthcheck", "-W", "3"]
      interval: 30s
      timeout: 10s
      retries: 3
    
  # Mosquitto Exporter for Prometheus
  mosquitto-exporter:
    image: sapcc/mosquitto-exporter:latest
    container_name: mosquitto-exporter
    ports:
      - "9234:9234"
    environment:
      - BROKER_ENDPOINT=tcp://mosquitto:1883
    depends_on:
      - mosquitto
    restart: unless-stopped

volumes:
  mosquitto-data:
  mosquitto-log:
```

---

## A.4 Installation Commands

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mosquitto mosquitto-clients

# Create directory structure
sudo mkdir -p /etc/mosquitto/certs
sudo mkdir -p /var/lib/mosquitto
sudo mkdir -p /var/log/mosquitto

# Create password file
sudo mosquitto_passwd -c /etc/mosquitto/passwd device-001
sudo mosquitto_passwd -b /etc/mosquitto/passwd backend-service your_secure_password
sudo mosquitto_passwd -b /etc/mosquitto/passwd admin admin_password

# Generate certificates (for TLS)
cd /etc/mosquitto/certs

# Generate CA
openssl genrsa -out ca.key 4096
openssl req -new -x509 -days 3650 -key ca.key -out ca.crt \
    -subj "/CN=IndusJS Fleet CA/O=IndusJS/C=IN"

# Generate Server Certificate
openssl genrsa -out server.key 2048
openssl req -new -key server.key -out server.csr \
    -subj "/CN=mqtt.indusjs.com/O=IndusJS/C=IN"
openssl x509 -req -days 365 -in server.csr \
    -CA ca.crt -CAkey ca.key -set_serial 01 -out server.crt

# Set permissions
sudo chown -R mosquitto:mosquitto /etc/mosquitto
sudo chown -R mosquitto:mosquitto /var/lib/mosquitto
sudo chown -R mosquitto:mosquitto /var/log/mosquitto
sudo chmod 600 /etc/mosquitto/certs/*.key

# Restart service
sudo systemctl restart mosquitto
sudo systemctl enable mosquitto

# Check status
sudo systemctl status mosquitto

# Test connection
mosquitto_sub -h localhost -t "test/#" -u backend-service -P your_secure_password &
mosquitto_pub -h localhost -t "test/hello" -m "world" -u backend-service -P your_secure_password
```

---

## A.5 Systemd Service

```ini
# /etc/systemd/system/mosquitto.service
[Unit]
Description=Mosquitto MQTT Broker
Documentation=man:mosquitto.conf(5) man:mosquitto(8)
After=network-online.target
Wants=network-online.target

[Service]
Type=notify
NotifyAccess=main
ExecStart=/usr/sbin/mosquitto -c /etc/mosquitto/mosquitto.conf
ExecReload=/bin/kill -HUP $MAINPID
Restart=on-failure
RestartSec=5
User=mosquitto
Group=mosquitto

# Hardening
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/var/lib/mosquitto /var/log/mosquitto /run/mosquitto

[Install]
WantedBy=multi-user.target
```

---

## A.6 Logrotate Configuration

```conf
# /etc/logrotate.d/mosquitto
/var/log/mosquitto/mosquitto.log {
    daily
    rotate 14
    compress
    delaycompress
    missingok
    notifempty
    create 640 mosquitto mosquitto
    postrotate
        /bin/kill -HUP $(cat /run/mosquitto/mosquitto.pid 2>/dev/null) 2>/dev/null || true
    endscript
}
```

---

[← Implementation Roadmap](./13-implementation-roadmap.md) | [Next: EMQX Config →](./appendix-b-emqx-config.md)

