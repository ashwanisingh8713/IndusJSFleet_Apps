# 04. Drivers

Driver management endpoints.

## Role Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| Create Driver | ✅ | ✅ | ✅ | ❌ |
| View Driver | ✅ | ✅ | ✅ | ✅ |
| Edit Driver | ✅ | ✅ | ✅ | ❌ |
| Disable Driver | ✅ | ✅ | ❌ | ❌ |
| Delete Driver | ✅ | ✅ | ❌ | ❌ |

---

## Endpoints

### Create Driver
```http
POST {{base_url}}/drivers
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "first_name": "Rajesh",
    "last_name": "Kumar",
    "mobile": "+919876543215",
    "email": "rajesh@example.com",
    "license_number": "DL12345678901234",
    "license_expiry": "31-12-2026",
    "address": "123 Main St, Mumbai"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Driver created successfully",
    "data": {
        "id": 1,
        "first_name": "Rajesh",
        "last_name": "Kumar",
        "mobile": "+919876543215",
        "email": "rajesh@example.com",
        "license_number": "DL12345678901234",
        "license_expiry": "2026-12-31T00:00:00Z",
        "status": "active",
        "is_active": true,
        "owner_id": 1
    }
}
```

---

### List Drivers
```http
GET {{base_url}}/drivers
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Example |
|-----------|-------------|---------|
| status | Filter by status | `?status=active` |
| is_active | Filter by active | `?is_active=true` |
| page | Page number | `?page=1` |
| per_page | Items per page | `?per_page=20` |

**Status Options:** `active`, `on_trip`, `on_leave`, `suspended`, `inactive`

**Response:**
```json
{
    "success": true,
    "message": "Drivers retrieved successfully",
    "data": [
        {
            "id": 1,
            "first_name": "Rajesh",
            "last_name": "Kumar",
            "mobile": "+919876543215",
            "license_number": "DL12345678901234",
            "status": "active",
            "is_active": true
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

### Get Available Drivers
```http
GET {{base_url}}/drivers/available
Authorization: Bearer {{token}}
```

Returns drivers who are active and not currently on a trip.

**Response:**
```json
{
    "success": true,
    "message": "Available drivers retrieved successfully",
    "data": [
        {
            "id": 1,
            "first_name": "Rajesh",
            "last_name": "Kumar",
            "status": "active"
        }
    ]
}
```

---

### Get Driver
```http
GET {{base_url}}/drivers/:id
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Driver retrieved successfully",
    "data": {
        "id": 1,
        "first_name": "Rajesh",
        "last_name": "Kumar",
        "mobile": "+919876543215",
        "email": "rajesh@example.com",
        "license_number": "DL12345678901234",
        "license_expiry": "2026-12-31T00:00:00Z",
        "address": "123 Main St, Mumbai",
        "status": "active",
        "is_active": true,
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

### Update Driver
```http
PUT {{base_url}}/drivers/:id
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "first_name": "Rajesh",
    "last_name": "Kumar Updated",
    "mobile": "+919876543216",
    "email": "rajesh.updated@example.com",
    "license_number": "DL12345678901234",
    "license_expiry": "31-12-2027",
    "address": "456 New St, Mumbai"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Driver updated successfully",
    "data": {
        "id": 1,
        "first_name": "Rajesh",
        "last_name": "Kumar Updated"
    }
}
```

---

### Update Driver Status
```http
PATCH {{base_url}}/drivers/:id/status
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "status": "on_leave"
}
```

**Status Options:** `active`, `on_trip`, `on_leave`, `suspended`, `inactive`

**Response:**
```json
{
    "success": true,
    "message": "Driver status updated successfully",
    "data": {
        "id": 1,
        "status": "on_leave"
    }
}
```

---

### Toggle Driver Active
```http
PATCH {{base_url}}/drivers/:id/toggle-active
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Driver disabled successfully",
    "data": {
        "id": 1,
        "is_active": false
    }
}
```

---

### Delete Driver
```http
DELETE {{base_url}}/drivers/:id
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Driver deleted successfully"
}
```

---

## Error Responses

### Driver Not Found
```json
{
    "success": false,
    "message": "Driver not found",
    "error": "record not found"
}
```

### Driver Already Exists
```json
{
    "success": false,
    "message": "Driver already exists",
    "error": "mobile number already registered"
}
```

### Driver on Trip
```json
{
    "success": false,
    "message": "Cannot delete driver",
    "error": "driver is currently assigned to an active trip"
}
```

