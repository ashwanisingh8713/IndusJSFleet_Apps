# 12. Location Tracking

Real-time vehicle location tracking endpoints.

---

## Endpoints

### Update Vehicle Location
```http
POST {{base_url}}/locations
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "vehicle_id": 1,
    "latitude": 19.0760,
    "longitude": 72.8777,
    "speed": 45.5,
    "heading": 180,
    "accuracy": 10,
    "altitude": 50,
    "timestamp": "2025-01-15T10:30:00Z"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Location updated successfully",
    "data": {
        "id": 1,
        "vehicle_id": 1,
        "latitude": 19.0760,
        "longitude": 72.8777,
        "speed": 45.5,
        "heading": 180,
        "timestamp": "2025-01-15T10:30:00Z"
    }
}
```

---

### Bulk Update Locations
```http
POST {{base_url}}/locations/bulk
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "locations": [
        {
            "vehicle_id": 1,
            "latitude": 19.0760,
            "longitude": 72.8777,
            "speed": 45.5,
            "heading": 180,
            "timestamp": "2025-01-15T10:30:00Z"
        },
        {
            "vehicle_id": 2,
            "latitude": 18.5204,
            "longitude": 73.8567,
            "speed": 50.0,
            "heading": 90,
            "timestamp": "2025-01-15T10:30:00Z"
        }
    ]
}
```

---

### Get Vehicle Current Location
```http
GET {{base_url}}/vehicles/:id/location
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "vehicle_id": 1,
        "registration_number": "MH12AB1234",
        "latitude": 19.0760,
        "longitude": 72.8777,
        "speed": 45.5,
        "heading": 180,
        "accuracy": 10,
        "timestamp": "2025-01-15T10:30:00Z",
        "is_moving": true,
        "last_updated": "2025-01-15T10:30:00Z"
    }
}
```

---

### Get Vehicle Location History
```http
GET {{base_url}}/vehicles/:id/location/history
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Example |
|-----------|-------------|---------|
| start_date | Start datetime | `2025-01-15T00:00:00Z` |
| end_date | End datetime | `2025-01-15T23:59:59Z` |
| interval | Sample interval (minutes) | `5` |

**Response:**
```json
{
    "success": true,
    "data": {
        "vehicle_id": 1,
        "period": {
            "start": "2025-01-15T00:00:00Z",
            "end": "2025-01-15T23:59:59Z"
        },
        "locations": [
            {
                "latitude": 19.0760,
                "longitude": 72.8777,
                "speed": 45.5,
                "timestamp": "2025-01-15T10:00:00Z"
            },
            {
                "latitude": 19.0800,
                "longitude": 72.8800,
                "speed": 50.0,
                "timestamp": "2025-01-15T10:05:00Z"
            }
        ],
        "total_points": 288,
        "total_distance": 150.5
    }
}
```

---

### Get All Vehicle Locations
```http
GET {{base_url}}/locations/vehicles
Authorization: Bearer {{token}}
```

Returns current location of all vehicles.

**Response:**
```json
{
    "success": true,
    "data": [
        {
            "vehicle_id": 1,
            "registration_number": "MH12AB1234",
            "latitude": 19.0760,
            "longitude": 72.8777,
            "speed": 45.5,
            "is_moving": true,
            "last_updated": "2025-01-15T10:30:00Z"
        },
        {
            "vehicle_id": 2,
            "registration_number": "MH12CD5678",
            "latitude": 18.5204,
            "longitude": 73.8567,
            "speed": 0,
            "is_moving": false,
            "last_updated": "2025-01-15T10:25:00Z"
        }
    ]
}
```

---

### Get Live Tracking Status
```http
GET {{base_url}}/locations/live-status
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "total_vehicles": 50,
        "online_vehicles": 35,
        "moving_vehicles": 20,
        "idle_vehicles": 15,
        "offline_vehicles": 15,
        "last_checked": "2025-01-15T10:30:00Z"
    }
}
```

---

## Real-time Integration

### WebSocket Connection
```
ws://your-server/ws/locations
```

**Authentication:**
Connect with JWT token as query parameter:
```
ws://your-server/ws/locations?token=your_jwt_token
```

**Subscribe to Vehicle:**
```json
{
    "action": "subscribe",
    "vehicle_ids": [1, 2, 3]
}
```

**Receive Updates:**
```json
{
    "type": "location_update",
    "data": {
        "vehicle_id": 1,
        "latitude": 19.0760,
        "longitude": 72.8777,
        "speed": 45.5,
        "timestamp": "2025-01-15T10:30:00Z"
    }
}
```

---

### MQTT Topics

**Publish Location:**
```
fleet/{owner_id}/vehicles/{vehicle_id}/location
```

**Payload:**
```json
{
    "lat": 19.0760,
    "lng": 72.8777,
    "speed": 45.5,
    "heading": 180,
    "ts": "2025-01-15T10:30:00Z"
}
```

---

## Error Responses

### Vehicle Not Found
```json
{
    "success": false,
    "message": "Vehicle not found",
    "error": "record not found"
}
```

### No Location Data
```json
{
    "success": false,
    "message": "No location data",
    "error": "no location data available for this vehicle"
}
```

