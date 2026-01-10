# 07. Trips

Trip management endpoints.

## Date & Time Formats (IMPORTANT)
- **Date**: `DD-MM-YYYY` format ONLY (e.g., 31-12-2025)
- **Time**: `HH:MM` 24-hour format (e.g., 14:30)
- **DateTime (ISO)**: `2025-12-31T14:30:00Z` for timestamp fields

## Role Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| Create Trip | ✅ | ✅ | ✅ | ❌ |
| View Trip | ✅ | ✅ | ✅ | ✅ |
| View trip_price | ✅ | ✅ | ❌ | ❌ |
| Edit Trip (Planned) | ✅ | ✅ | ✅ | ❌ |
| Edit Trip (In Progress) | ✅ | ✅ | ❌ | ❌ |
| Edit Trip (Completed) | ✅ | ✅ | ❌ | ❌ |
| Change Trip State | ✅ | ✅ | ✅ | ❌ |
| Delete Trip | ✅ | ✅ | ❌ | ❌ |

## Trip States
- `planned` - Trip is scheduled
- `in_progress` - Trip is currently running
- `completed` - Trip has finished
- `cancelled` - Trip was cancelled

---

## Endpoints

### Create Trip
```http
POST {{base_url}}/trips
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "vehicle_id": 1,
    "driver_id": 1,
    "scheduled_date": "15-01-2025",
    "start_time": "2025-01-15T08:00:00Z",
    "delivery_date": "15-01-2025",
    "delivery_time": "2025-01-15T18:00:00Z",
    "start_location": "Mumbai",
    "start_lat": 19.0760,
    "start_lng": 72.8777,
    "end_location": "Pune",
    "end_lat": 18.5204,
    "end_lng": 73.8567,
    "estimated_distance": 150.5,
    "cargo_type": "general",
    "cargo_description": "Electronics",
    "cargo_loading_weight": 500,
    "weight_unit": "kg",
    "customer_name": "ABC Corp",
    "customer_contact": "+919876543210",
    "priority": "normal",
    "trip_price": 15000,
    "notes": "Handle with care"
}
```

**Priority Options:** `low`, `normal`, `high`, `urgent`
**Cargo Types:** `general`, `fragile`, `perishable`, `hazardous`, `livestock`

**Response:**
```json
{
    "success": true,
    "message": "Trip created successfully",
    "data": {
        "id": 1,
        "vehicle_id": 1,
        "driver_id": 1,
        "state": "planned",
        "start_location": "Mumbai",
        "end_location": "Pune",
        "estimated_distance": 150.5,
        "trip_price": 15000,
        "display_info": {
            "state_label": "Planned",
            "distance_info": {
                "display_value": "150.5 km",
                "display_label": "Estimated"
            }
        }
    }
}
```

---

### List Trips
```http
GET {{base_url}}/trips
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Example |
|-----------|-------------|---------|
| state | Filter by state | `?state=in_progress` |
| vehicle_id | Filter by vehicle | `?vehicle_id=1` |
| driver_id | Filter by driver | `?driver_id=1` |
| priority | Filter by priority | `?priority=high` |
| cargo_type | Filter by cargo | `?cargo_type=fragile` |
| page | Page number | `?page=1` |
| per_page | Items per page | `?per_page=20` |

**Response:**
```json
{
    "success": true,
    "data": [
        {
            "id": 1,
            "vehicle_id": 1,
            "driver_id": 1,
            "state": "in_progress",
            "state_label": "In Progress",
            "start_location": "Mumbai",
            "end_location": "Pune",
            "scheduled_date": "15-01-2025",
            "vehicle_number": "MH12AB1234",
            "driver_name": "Rajesh Kumar",
            "estimated_distance": 150.5,
            "estimated_distance_label": "150.5 km",
            "distance_display": "75.2 km covered",
            "trip_price": 15000
        }
    ]
}
```

**Note:** `trip_price` is hidden (returns 0) for Manager and Supervisor roles.

---

### Get Trip
```http
GET {{base_url}}/trips/:id
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "id": 1,
        "vehicle_id": 1,
        "driver_id": 1,
        "state": "in_progress",
        "start_location": "Mumbai",
        "end_location": "Pune",
        "estimated_distance": 150.5,
        "covered_distance": 75.2,
        "trip_price": 15000,
        "vehicle": {
            "id": 1,
            "registration_number": "MH12AB1234"
        },
        "driver": {
            "id": 1,
            "first_name": "Rajesh",
            "last_name": "Kumar"
        },
        "stops": [],
        "fuel_info": {
            "filled_fuel": 50,
            "used_fuel": 25,
            "remaining_fuel": 25,
            "fuel_cost": 2500
        },
        "display_info": {
            "state_label": "In Progress",
            "distance_info": {
                "display_value": "75.2 km",
                "display_label": "Covered"
            }
        }
    }
}
```

---

### Update Trip
```http
PUT {{base_url}}/trips/:id
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "vehicle_id": 2,
    "driver_id": 2,
    "start_location": "Mumbai Updated",
    "end_location": "Pune Updated",
    "scheduled_date": "16-01-2025",
    "cargo_description": "Updated cargo",
    "customer_name": "XYZ Corp",
    "priority": "high",
    "trip_price": 18000
}
```

**Notes:**
- Only planned trips can be updated by Manager
- Owner and General Manager can update trips in any state

---

### Update Trip State
```http
PATCH {{base_url}}/trips/:id/state
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "state": "in_progress"
}
```

**Valid Transitions:**
- `planned` → `in_progress` or `cancelled`
- `in_progress` → `completed` or `cancelled`
- `completed` → (no transitions)
- `cancelled` → (no transitions)

**Response:**
```json
{
    "success": true,
    "message": "Trip state updated successfully",
    "data": {
        "id": 1,
        "state": "in_progress",
        "actual_start": "2025-01-15T08:15:00Z"
    }
}
```

---

### Update Trip Location
```http
PATCH {{base_url}}/trips/:id/location
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "current_lat": 18.9500,
    "current_lng": 73.2000
}
```

**Note:** Only for in_progress trips

---

### Update Trip Progress
```http
PATCH {{base_url}}/trips/:id/progress
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "covered_distance": 100.5,
    "covered_duration_minutes": 120,
    "current_lat": 18.7500,
    "current_lng": 73.5000
}
```

---

### Delete Trip
```http
DELETE {{base_url}}/trips/:id
Authorization: Bearer {{token}}
```

**Note:** Only planned or cancelled trips can be deleted

---

## Trip Stops

### Get Trip Stops
```http
GET {{base_url}}/trips/:id/stops
Authorization: Bearer {{token}}
```

### Add Trip Stop
```http
POST {{base_url}}/trips/:id/stops
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "stop_order": 1,
    "location": "Lonavala",
    "latitude": 18.7557,
    "longitude": 73.4091,
    "arrival_time": "2025-01-15T10:00:00Z",
    "stop_duration": 30,
    "notes": "Rest stop"
}
```

### Update Trip Stop
```http
PUT {{base_url}}/trips/:id/stops/:stop_id
Authorization: Bearer {{token}}
Content-Type: application/json
```

### Mark Stop Completed
```http
PATCH {{base_url}}/trips/:id/stops/:stop_id/complete
Authorization: Bearer {{token}}
```

### Delete Trip Stop
```http
DELETE {{base_url}}/trips/:id/stops/:stop_id
Authorization: Bearer {{token}}
```

---

## Trip Fuel APIs

### Get Trip Fuel Info
```http
GET {{base_url}}/trips/:id/fuel
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "filled_fuel_quantity": 50,
        "used_fuel_quantity": 25,
        "remaining_fuel": 25,
        "fuel_rate": 100,
        "total_fuel_cost": 2500,
        "remaining_fuel_value": 2500,
        "km_per_liter": 15
    }
}
```

### Update Trip Fuel Usage
```http
PATCH {{base_url}}/trips/:id/fuel
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "used_fuel_quantity": 35
}
```

---

## Error Responses

### Vehicle Not Available
```json
{
    "success": false,
    "message": "Vehicle not available",
    "error": "Vehicle is already assigned to trip #5 (status: in_progress)"
}
```

### Driver Not Available
```json
{
    "success": false,
    "message": "Driver not available",
    "error": "Driver is already assigned to trip #3"
}
```

### Invalid State Transition
```json
{
    "success": false,
    "message": "Invalid state transition",
    "error": "cannot transition from completed to in_progress"
}
```

