# Locations Table

## Table: `locations`

Stores GPS location tracking data for vehicles. This is a high-volume table optimized for time-series data.

---

## Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| `vehicle_id` | `bigint` | NOT NULL, INDEX, FK | Associated vehicle |
| **Position** |
| `latitude` | `float` | NOT NULL | GPS latitude |
| `longitude` | `float` | NOT NULL | GPS longitude |
| **Motion Data** |
| `speed` | `float` | | Speed in km/h |
| `heading` | `float` | | Direction (0-360°) |
| `altitude` | `float` | | Altitude in meters |
| `accuracy` | `float` | | GPS accuracy in meters |
| **Timestamps** |
| `timestamp` | `timestamp` | NOT NULL, INDEX | GPS fix timestamp |
| `created_at` | `timestamp` | | Record creation time |

---

## Indexes

```sql
CREATE INDEX idx_locations_vehicle_id ON locations(vehicle_id);
CREATE INDEX idx_locations_timestamp ON locations(timestamp);
CREATE INDEX idx_locations_vehicle_timestamp ON locations(vehicle_id, timestamp DESC);
```

---

## Sample Data

```json
{
  "id": 1,
  "vehicle_id": 1,
  "latitude": 19.0760,
  "longitude": 72.8777,
  "speed": 45.5,
  "heading": 180.0,
  "altitude": 12.5,
  "accuracy": 5.0,
  "timestamp": "2026-01-15T10:30:00Z",
  "created_at": "2026-01-15T10:30:01Z"
}
```

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v2/locations` | Record location | All |
| POST | `/api/v2/locations/batch` | Batch record | All |
| GET | `/api/v2/vehicles/:id/locations` | Get location history | All |
| GET | `/api/v2/vehicles/:id/location/latest` | Get latest location | All |
| **WebSocket** |
| WS | `/ws/locations` | Real-time location stream | All |

---

## Location Input

```go
type LocationInput struct {
    VehicleID uint    `json:"vehicle_id" binding:"required"`
    Latitude  float64 `json:"latitude" binding:"required"`
    Longitude float64 `json:"longitude" binding:"required"`
    Speed     float64 `json:"speed"`
    Heading   float64 `json:"heading"`
    Altitude  float64 `json:"altitude"`
    Accuracy  float64 `json:"accuracy"`
}
```

---

## Location History Query

```go
type LocationHistoryQuery struct {
    VehicleID uint       `form:"vehicle_id" binding:"required"`
    StartTime *time.Time `form:"start_time"`
    EndTime   *time.Time `form:"end_time"`
    Limit     int        `form:"limit"`
}
```

### Example Query

```sql
SELECT * FROM locations
WHERE vehicle_id = ?
  AND timestamp >= ?
  AND timestamp <= ?
ORDER BY timestamp DESC
LIMIT ?;
```

---

## High-Volume Considerations

### Expected Volume
- 50,000 vehicles
- 1 location update per 30 seconds per active vehicle
- ~100,000 records per minute during peak
- ~100M+ records per month

### Optimization Strategies

1. **Table Partitioning** (Recommended)
```sql
-- Partition by time range
CREATE TABLE locations (
    id BIGSERIAL,
    vehicle_id BIGINT NOT NULL,
    -- ... other columns
    timestamp TIMESTAMP NOT NULL
) PARTITION BY RANGE (timestamp);

-- Monthly partitions
CREATE TABLE locations_2026_01 PARTITION OF locations
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
```

2. **TimescaleDB Extension** (Highly Recommended)
```sql
-- Convert to hypertable
SELECT create_hypertable('locations', 'timestamp');

-- Auto-compression
ALTER TABLE locations SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'vehicle_id'
);
```

3. **Data Retention Policy**
```sql
-- Keep detailed data for 30 days
-- Archive to cold storage after
SELECT add_retention_policy('locations', INTERVAL '30 days');
```

---

## Real-time Tracking

### WebSocket Message Format

```json
{
  "type": "location_update",
  "data": {
    "vehicle_id": 1,
    "latitude": 19.0760,
    "longitude": 72.8777,
    "speed": 45.5,
    "heading": 180.0,
    "timestamp": "2026-01-15T10:30:00Z"
  }
}
```

### MQTT Topic Structure
```
fleet/{owner_id}/vehicles/{vehicle_id}/location
```

---

## Location-based Features

### Distance Calculation
```sql
-- Calculate total distance traveled
SELECT 
    vehicle_id,
    SUM(
        6371 * acos(
            cos(radians(lat1)) * cos(radians(lat2)) *
            cos(radians(lng2) - radians(lng1)) +
            sin(radians(lat1)) * sin(radians(lat2))
        )
    ) as total_distance_km
FROM (
    SELECT 
        vehicle_id,
        latitude as lat1,
        longitude as lng1,
        LEAD(latitude) OVER (PARTITION BY vehicle_id ORDER BY timestamp) as lat2,
        LEAD(longitude) OVER (PARTITION BY vehicle_id ORDER BY timestamp) as lng2
    FROM locations
    WHERE vehicle_id = ?
      AND timestamp BETWEEN ? AND ?
) subquery
WHERE lat2 IS NOT NULL;
```

### Speed Analysis
```sql
SELECT 
    vehicle_id,
    AVG(speed) as avg_speed,
    MAX(speed) as max_speed,
    COUNT(*) as data_points
FROM locations
WHERE vehicle_id = ?
  AND timestamp >= NOW() - INTERVAL '24 hours'
  AND speed > 0
GROUP BY vehicle_id;
```

---

## Geofence Integration

Location updates trigger geofence checks:

```go
// On each location update
func CheckGeofences(vehicleID uint, lat, lng float64) {
    // Check if vehicle entered/exited any geofence
    geofences := GetActiveGeofences(ownerID)
    for _, geofence := range geofences {
        if geofence.Contains(lat, lng) {
            // Check if this is entry or already inside
            RecordGeofenceEvent(vehicleID, geofence.ID, "entry")
        }
    }
}
```

---

## Scalability Recommendations

| Scale | Strategy |
|-------|----------|
| < 1K vehicles | Standard PostgreSQL |
| 1K - 10K vehicles | PostgreSQL with partitioning |
| 10K - 50K vehicles | TimescaleDB |
| > 50K vehicles | TimescaleDB + Read replicas |

---

## Notes

1. **No Soft Delete**: Location data is not soft-deleted (historical record)
2. **Rate Limiting**: API has higher rate limit (1000 req/min) for location endpoints
3. **Batch Updates**: Use batch endpoint for efficient bulk recording
4. **Compression**: Enable TimescaleDB compression for older data
5. **Cold Storage**: Archive old data to Google Cloud Storage

---

*Last Updated: January 2026*

