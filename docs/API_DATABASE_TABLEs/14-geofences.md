# Geofences & Geofence Events Tables

## Table: `geofences`

Stores geofence zone definitions for monitoring vehicle entry/exit in specific areas.

---

## Schema - Geofences

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| `owner_id` | `bigint` | NOT NULL, INDEX | Fleet owner |
| `name` | `varchar(255)` | NOT NULL | Geofence name |
| `description` | `text` | | Description |
| `type` | `varchar(20)` | NOT NULL, DEFAULT 'circle' | Geofence type |
| **Circle Geofence** |
| `center_lat` | `float` | | Center latitude |
| `center_lng` | `float` | | Center longitude |
| `radius` | `float` | | Radius in meters |
| **Polygon Geofence** |
| `polygon` | `jsonb` | | Array of [lat, lng] points |
| **Settings** |
| `color` | `varchar(20)` | DEFAULT '#FF0000' | Display color |
| `is_active` | `boolean` | DEFAULT true | Active status |
| `alert_on_entry` | `boolean` | DEFAULT true | Alert on vehicle entry |
| `alert_on_exit` | `boolean` | DEFAULT true | Alert on vehicle exit |
| `speed_limit` | `float` | | Speed limit (km/h), 0 = none |
| **Ownership** |
| `created_by` | `bigint` | INDEX | User who created |
| **Timestamps** |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Geofence Types

```go
const (
    GeofenceTypeCircle  GeofenceType = "circle"
    GeofenceTypePolygon GeofenceType = "polygon"
)
```

---

## Table: `geofence_events`

Stores entry/exit events when vehicles cross geofence boundaries.

### Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| `geofence_id` | `bigint` | NOT NULL, INDEX, FK | Geofence reference |
| `vehicle_id` | `bigint` | NOT NULL, INDEX, FK | Vehicle |
| `driver_id` | `bigint` | INDEX, FK | Driver (optional) |
| `trip_id` | `bigint` | INDEX, FK | Trip (optional) |
| `event_type` | `varchar(20)` | NOT NULL | Event type |
| `latitude` | `float` | | Event location lat |
| `longitude` | `float` | | Event location lng |
| `speed` | `float` | | Speed at event (km/h) |
| `event_time` | `timestamp` | NOT NULL | Event timestamp |
| `dwell_time` | `int` | | Time spent in zone (seconds) |
| `owner_id` | `bigint` | NOT NULL, INDEX | Fleet owner |
| `created_at` | `timestamp` | | Record creation |

---

## Event Types

```go
const (
    GeofenceEventEntry GeofenceEventType = "entry"
    GeofenceEventExit  GeofenceEventType = "exit"
    GeofenceEventDwell GeofenceEventType = "dwell"
)
```

---

## Indexes

```sql
-- Geofences
CREATE INDEX idx_geofences_owner_id ON geofences(owner_id);
CREATE INDEX idx_geofences_is_active ON geofences(is_active);
CREATE INDEX idx_geofences_deleted_at ON geofences(deleted_at);

-- Geofence Events
CREATE INDEX idx_geofence_events_geofence_id ON geofence_events(geofence_id);
CREATE INDEX idx_geofence_events_vehicle_id ON geofence_events(vehicle_id);
CREATE INDEX idx_geofence_events_event_time ON geofence_events(event_time);
CREATE INDEX idx_geofence_events_owner_id ON geofence_events(owner_id);
```

---

## Sample Data

### Circle Geofence
```json
{
  "id": 1,
  "owner_id": 1,
  "name": "Warehouse A",
  "description": "Main loading/unloading warehouse",
  "type": "circle",
  "center_lat": 19.0760,
  "center_lng": 72.8777,
  "radius": 500,
  "color": "#4CAF50",
  "is_active": true,
  "alert_on_entry": true,
  "alert_on_exit": true,
  "speed_limit": 20,
  "created_by": 1
}
```

### Polygon Geofence
```json
{
  "id": 2,
  "owner_id": 1,
  "name": "Restricted Zone - City Center",
  "description": "Heavy vehicle restricted during peak hours",
  "type": "polygon",
  "polygon": [
    [19.0750, 72.8750],
    [19.0750, 72.8850],
    [19.0850, 72.8850],
    [19.0850, 72.8750]
  ],
  "color": "#F44336",
  "is_active": true,
  "alert_on_entry": true,
  "alert_on_exit": false,
  "created_by": 1
}
```

### Geofence Event
```json
{
  "id": 1,
  "geofence_id": 1,
  "vehicle_id": 1,
  "driver_id": 1,
  "trip_id": 5,
  "event_type": "entry",
  "latitude": 19.0762,
  "longitude": 72.8779,
  "speed": 15.5,
  "event_time": "2026-01-15T10:30:00Z",
  "owner_id": 1
}
```

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v2/geofences` | Create geofence | Owner, GM, Manager |
| GET | `/api/v2/geofences` | List geofences | All |
| GET | `/api/v2/geofences/:id` | Get geofence details | All |
| PUT | `/api/v2/geofences/:id` | Update geofence | Owner, GM, Manager |
| DELETE | `/api/v2/geofences/:id` | Delete geofence | Owner, GM |
| **Events** |
| GET | `/api/v2/geofences/:id/events` | Get geofence events | All |
| GET | `/api/v2/geofence-events` | List all events | All |
| GET | `/api/v2/vehicles/:id/geofence-events` | Vehicle's events | All |

---

## Geofence Detection Algorithm

```go
// Check if point is inside circle geofence
func (g *Geofence) ContainsCircle(lat, lng float64) bool {
    distance := haversineDistance(g.CenterLat, g.CenterLng, lat, lng)
    return distance <= g.Radius
}

// Check if point is inside polygon geofence
func (g *Geofence) ContainsPolygon(lat, lng float64) bool {
    // Ray casting algorithm
    points := parsePolygon(g.Polygon)
    return pointInPolygon(lat, lng, points)
}

// Haversine distance in meters
func haversineDistance(lat1, lng1, lat2, lng2 float64) float64 {
    R := 6371000.0 // Earth radius in meters
    φ1 := lat1 * math.Pi / 180
    φ2 := lat2 * math.Pi / 180
    Δφ := (lat2 - lat1) * math.Pi / 180
    Δλ := (lng2 - lng1) * math.Pi / 180
    
    a := math.Sin(Δφ/2)*math.Sin(Δφ/2) +
         math.Cos(φ1)*math.Cos(φ2)*
         math.Sin(Δλ/2)*math.Sin(Δλ/2)
    c := 2 * math.Atan2(math.Sqrt(a), math.Sqrt(1-a))
    
    return R * c
}
```

---

## Real-time Geofence Monitoring

On each location update:

```go
func CheckGeofencesOnLocationUpdate(vehicleID uint, lat, lng float64, tripID *uint) {
    geofences := GetActiveGeofences(ownerID)
    
    for _, geofence := range geofences {
        isInside := geofence.Contains(lat, lng)
        wasInside := GetVehicleGeofenceStatus(vehicleID, geofence.ID)
        
        if isInside && !wasInside {
            // Entry event
            RecordGeofenceEvent(GeofenceEventEntry, vehicleID, geofence.ID, lat, lng)
            if geofence.AlertOnEntry {
                SendNotification(GeofenceEntry, vehicleID, geofence)
            }
        } else if !isInside && wasInside {
            // Exit event
            RecordGeofenceEvent(GeofenceEventExit, vehicleID, geofence.ID, lat, lng)
            if geofence.AlertOnExit {
                SendNotification(GeofenceExit, vehicleID, geofence)
            }
        }
        
        UpdateVehicleGeofenceStatus(vehicleID, geofence.ID, isInside)
    }
}
```

---

## Vehicle Geofence Status

Track current status of vehicles in geofences:

```go
type GeofenceVehicleStatus struct {
    VehicleID   uint      `json:"vehicle_id"`
    GeofenceID  uint      `json:"geofence_id"`
    IsInside    bool      `json:"is_inside"`
    EnteredAt   time.Time `json:"entered_at,omitempty"`
    DwellTime   int       `json:"dwell_time"` // seconds
}
```

---

## Use Cases

### 1. Warehouse Monitoring
- Track when vehicles arrive/depart warehouses
- Calculate loading/unloading time

### 2. Customer Site Geofencing
- Auto-detect arrival at customer locations
- Trigger delivery confirmation

### 3. Restricted Zone Alerts
- Alert when vehicle enters restricted area
- Speed violation in school zones

### 4. Route Deviation
- Create corridor geofence along planned route
- Alert on deviation

---

## Notes

1. **Real-time Processing**: Events detected during location updates
2. **Dwell Time**: Calculated from entry to exit
3. **Speed Limit**: Alert if vehicle exceeds limit within geofence
4. **Multiple Geofences**: A vehicle can be in multiple geofences
5. **Historical Analysis**: Events stored for later analysis

---

*Last Updated: January 2026*

