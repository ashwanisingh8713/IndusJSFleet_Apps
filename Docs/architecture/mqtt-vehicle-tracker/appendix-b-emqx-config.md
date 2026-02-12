# Appendix B: EMQX Configuration (Large Scale 50K+)

Use EMQX only if you need to scale beyond 25K devices or require advanced features like rule engine.

---

## B.1 When to Use EMQX vs Mosquitto

| Factor | Use Mosquitto | Use EMQX |
|--------|---------------|----------|
| Device count | < 25,000 | > 25,000 |
| Memory available | < 512MB | > 2GB |
| Need clustering | No | Yes |
| Need rule engine | No | Yes |
| Budget | Minimal | Higher |
| Team expertise | Basic | Advanced |

---

## B.2 EMQX Basic Configuration

```yaml
# emqx.conf
node:
  name: "emqx@node1.indusjs.com"
  cookie: "secret-cluster-cookie"
  data_dir: "/opt/emqx/data"

cluster:
  name: indusjs-fleet
  discovery_strategy: static
  static:
    seeds:
      - "emqx@node1.indusjs.com"
      - "emqx@node2.indusjs.com"
      - "emqx@node3.indusjs.com"

listeners:
  tcp:
    default:
      bind: "0.0.0.0:1883"
      max_connections: 1000000
  ssl:
    default:
      bind: "0.0.0.0:8883"
      max_connections: 1000000
      ssl_options:
        cacertfile: "/etc/emqx/certs/ca.pem"
        certfile: "/etc/emqx/certs/server.pem"
        keyfile: "/etc/emqx/certs/server.key"
        verify: verify_peer
        fail_if_no_peer_cert: false

authentication:
  - mechanism: password_based
    backend: built_in_database
    
authorization:
  no_match: deny
  sources:
    - type: built_in_database
```

---

## B.3 Docker Compose (3-Node Cluster)

```yaml
# docker-compose-emqx.yml
version: '3.8'

services:
  emqx1:
    image: emqx/emqx:5.5.0
    container_name: emqx1
    hostname: node1.emqx.io
    environment:
      - EMQX_NODE_NAME=emqx@node1.emqx.io
      - EMQX_CLUSTER__DISCOVERY_STRATEGY=static
      - EMQX_CLUSTER__STATIC__SEEDS=[emqx@node1.emqx.io,emqx@node2.emqx.io,emqx@node3.emqx.io]
    ports:
      - "1883:1883"
      - "8883:8883"
      - "18083:18083"
    volumes:
      - emqx1-data:/opt/emqx/data
      - emqx1-log:/opt/emqx/log
      - ./certs:/etc/emqx/certs
    networks:
      - emqx-cluster
    restart: unless-stopped

  emqx2:
    image: emqx/emqx:5.5.0
    container_name: emqx2
    hostname: node2.emqx.io
    environment:
      - EMQX_NODE_NAME=emqx@node2.emqx.io
      - EMQX_CLUSTER__DISCOVERY_STRATEGY=static
      - EMQX_CLUSTER__STATIC__SEEDS=[emqx@node1.emqx.io,emqx@node2.emqx.io,emqx@node3.emqx.io]
    volumes:
      - emqx2-data:/opt/emqx/data
      - emqx2-log:/opt/emqx/log
      - ./certs:/etc/emqx/certs
    networks:
      - emqx-cluster
    restart: unless-stopped

  emqx3:
    image: emqx/emqx:5.5.0
    container_name: emqx3
    hostname: node3.emqx.io
    environment:
      - EMQX_NODE_NAME=emqx@node3.emqx.io
      - EMQX_CLUSTER__DISCOVERY_STRATEGY=static
      - EMQX_CLUSTER__STATIC__SEEDS=[emqx@node1.emqx.io,emqx@node2.emqx.io,emqx@node3.emqx.io]
    volumes:
      - emqx3-data:/opt/emqx/data
      - emqx3-log:/opt/emqx/log
      - ./certs:/etc/emqx/certs
    networks:
      - emqx-cluster
    restart: unless-stopped

  haproxy:
    image: haproxy:2.8
    container_name: haproxy
    ports:
      - "1884:1883"
      - "8884:8883"
    volumes:
      - ./haproxy.cfg:/usr/local/etc/haproxy/haproxy.cfg:ro
    networks:
      - emqx-cluster
    depends_on:
      - emqx1
      - emqx2
      - emqx3
    restart: unless-stopped

networks:
  emqx-cluster:
    driver: bridge

volumes:
  emqx1-data:
  emqx1-log:
  emqx2-data:
  emqx2-log:
  emqx3-data:
  emqx3-log:
```

---

## B.4 HAProxy Configuration

```conf
# haproxy.cfg
global
    log stdout format raw local0
    maxconn 50000

defaults
    log global
    mode tcp
    option tcplog
    timeout connect 10s
    timeout client 30m
    timeout server 30m

frontend mqtt_frontend
    bind *:1883
    default_backend mqtt_backend

frontend mqtts_frontend
    bind *:8883
    default_backend mqtt_backend

backend mqtt_backend
    balance roundrobin
    option tcp-check
    server emqx1 node1.emqx.io:1883 check inter 5s
    server emqx2 node2.emqx.io:1883 check inter 5s
    server emqx3 node3.emqx.io:1883 check inter 5s
```

---

## B.5 Session Persistence with Redis

```yaml
# emqx.conf - Redis session persistence
session:
  persistence:
    backend: redis
    redis:
      servers:
        - host: redis-master
          port: 6379
      pool_size: 8
      database: 0
      password: "${REDIS_PASSWORD}"
```

---

## B.6 Rule Engine Example

```sql
-- Forward telemetry to Kafka
SELECT
  clientid as device_id,
  payload.lat as latitude,
  payload.lng as longitude,
  payload.spd as speed,
  payload.ign as ignition,
  timestamp
FROM
  "indusjs/fleet/v1/devices/+/telemetry"
```

---

## B.7 EMQX CLI Commands

```bash
# Check cluster status
docker exec emqx1 emqx_ctl cluster status

# List connected clients
docker exec emqx1 emqx_ctl clients list

# Show subscriptions
docker exec emqx1 emqx_ctl subscriptions list

# Add user
docker exec emqx1 emqx_ctl admins add admin password

# Check broker stats
docker exec emqx1 emqx_ctl broker stats
```

---

[← Mosquitto Config](./appendix-a-mosquitto-config.md) | [Next: Monitoring →](./appendix-c-monitoring.md)

