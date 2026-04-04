# 05. Vehicles

Vehicle management endpoints.

## Role Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| Create Vehicle | ✅ | ✅ | ✅ | ❌ |
| View Vehicle | ✅ | ✅ | ✅ | ✅ |
| Edit Vehicle | ✅ | ✅ | ✅ | ❌ |
| Disable Vehicle | ✅ | ✅ | ❌ | ❌ |
| Delete Vehicle | ✅ | ✅ | ❌ | ❌ |

---

## Endpoints

### Register Vehicle
```http
POST {{base_url}}/vehicles
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "registration_number": "MH12AB1234",
    "make": "Tata",
    "model": "Ace",
    "year": 2023,
    "vehicle_type": "truck",
    "fuel_type": "diesel",
    "capacity": 1000,
    "color": "white",
    "mileage": 15.5
}
```

**Vehicle Types:** `car`, `truck`, `bus`, `van`, `motorcycle`, `auto`
**Fuel Types:** `petrol`, `diesel`, `cng`, `electric`, `hybrid`

**Response:**
```json
{
    "success": true,
    "message": "Vehicle registered successfully",
    "data": {
        "id": 1,
        "registration_number": "MH12AB1234",
        "make": "Tata",
        "model": "Ace",
        "year": 2023,
        "vehicle_type": "truck",
        "fuel_type": "diesel",
        "capacity": 1000,
        "color": "white",
        "mileage": 15.5,
        "state": "active",
        "owner_id": 1
    }
}
```

---

### Register Vehicle with Documents
```http
POST {{base_url}}/vehicles/with-documents
Authorization: Bearer {{token}}
Content-Type: multipart/form-data
```

**Form Data:**
| Field | Type | Description |
|-------|------|-------------|
| registration_number | text | Vehicle registration |
| make | text | Manufacturer |
| model | text | Model name |
| year | text | Year |
| vehicle_type | text | Type |
| fuel_type | text | Fuel type |
| rc_document | file | RC document file |
| insurance_document | file | Insurance file |
| puc_document | file | PUC file |

---

### List Vehicles
```http
GET {{base_url}}/vehicles
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Example |
|-----------|-------------|---------|
| state | Filter by state | `?state=active` |
| vehicle_type | Filter by type | `?vehicle_type=truck` |
| page | Page number | `?page=1` |
| per_page | Items per page | `?per_page=20` |

**State Options:** `active`, `inactive`, `maintenance`, `retired`

**Response:**
```json
{
    "success": true,
    "message": "Vehicles retrieved successfully",
    "data": [
        {
            "id": 1,
            "registration_number": "MH12AB1234",
            "make": "Tata",
            "model": "Ace",
            "state": "active"
        }
    ],
    "pagination": {
        "page": 1,
        "per_page": 20,
        "total": 1
    }
}
```

---

### Get Vehicle
```http
GET {{base_url}}/vehicles/:id
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Vehicle retrieved successfully",
    "data": {
        "id": 1,
        "registration_number": "MH12AB1234",
        "make": "Tata",
        "model": "Ace",
        "year": 2023,
        "vehicle_type": "truck",
        "fuel_type": "diesel",
        "capacity": 1000,
        "color": "white",
        "mileage": 15.5,
        "state": "active",
        "owner_id": 1,
        "caretaker_id": 5,
        "caretaker": {
            "id": 5,
            "first_name": "John",
            "last_name": "Manager"
        }
    }
}
```

---

### Update Vehicle
```http
PUT {{base_url}}/vehicles/:id
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "make": "Tata",
    "model": "Ace Gold",
    "year": 2024,
    "vehicle_type": "truck",
    "fuel_type": "diesel",
    "capacity": 1200,
    "color": "blue",
    "mileage": 16.0
}
```

**Response:**
```json
{
    "success": true,
    "message": "Vehicle updated successfully",
    "data": {
        "id": 1,
        "registration_number": "MH12AB1234",
        "make": "Tata",
        "model": "Ace Gold",
        "mileage": 16.0
    }
}
```

---

### Update Vehicle State
```http
PATCH {{base_url}}/vehicles/:id/state
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "state": "maintenance"
}
```

**State Options:** `active`, `inactive`, `maintenance`, `retired`

**Response:**
```json
{
    "success": true,
    "message": "Vehicle state updated successfully",
    "data": {
        "id": 1,
        "state": "maintenance"
    }
}
```

---

### Delete Vehicle
```http
DELETE {{base_url}}/vehicles/:id
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Vehicle deleted successfully"
}
```

---

## Vehicle Detail APIs (Mobile App)

### Get Vehicle Overview
```http
GET {{base_url}}/vehicles/:id/overview
Authorization: Bearer {{token}}
```

Returns vehicle summary with current trip, driver, and recent activity.

---

### Get Vehicle Trips
```http
GET {{base_url}}/vehicles/:id/trips
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description |
|-----------|-------------|
| state | Filter by trip state |
| page | Page number |
| per_page | Items per page |

---

### Get Vehicle Route
```http
GET {{base_url}}/vehicles/:id/route
Authorization: Bearer {{token}}
```

Returns current route information for active trip.

---

### Get Vehicle Documents Detail
```http
GET {{base_url}}/vehicles/:id/documents/detail
Authorization: Bearer {{token}}
```

Returns all documents with expiry status.

---

### Get Vehicle Full Detail
```http
GET {{base_url}}/vehicles/:id/detail
Authorization: Bearer {{token}}
```

Returns complete vehicle information including documents, trips, and costs.

---

## Vehicle Location APIs

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
        "latitude": 19.0760,
        "longitude": 72.8777,
        "speed": 45.5,
        "heading": 180,
        "timestamp": "2025-01-01T10:00:00Z"
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
| start_date | Start date | `?start_date=01-01-2025` |
| end_date | End date | `?end_date=31-01-2025` |

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

### Registration Already Exists
```json
{
    "success": false,
    "message": "Vehicle already exists",
    "error": "registration number already registered"
}
```

