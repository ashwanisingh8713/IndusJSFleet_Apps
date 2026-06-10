# 03b. Caretaker Management

Caretaker assignment for vehicles and drivers.

## Overview
- Caretaker must be a **Manager** or **Supervisor**
- Caretaker is **optional** (can be null)
- One caretaker can manage **multiple** vehicles and drivers
- Only **Owner** and **General Manager** can assign caretakers

---

## Endpoints

### Assign Caretaker to Vehicle
```http
POST {{base_url}}/vehicles/:id/assign-caretaker
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "caretaker_id": 5
}
```

**Response:**
```json
{
    "success": true,
    "message": "Caretaker assigned successfully",
    "data": {
        "id": 1,
        "registration_number": "MH12AB1234",
        "caretaker_id": 5,
        "caretaker": {
            "id": 5,
            "first_name": "John",
            "last_name": "Manager",
            "role": "manager"
        }
    }
}
```

---

### Remove Caretaker from Vehicle
```http
DELETE {{base_url}}/vehicles/:id/remove-caretaker
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Caretaker removed successfully"
}
```

---

### Assign Caretaker to Driver
```http
POST {{base_url}}/drivers/:id/assign-caretaker
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "caretaker_id": 5
}
```

**Response:**
```json
{
    "success": true,
    "message": "Caretaker assigned successfully",
    "data": {
        "id": 1,
        "first_name": "Rajesh",
        "last_name": "Kumar",
        "caretaker_id": 5
    }
}
```

---

### Remove Caretaker from Driver
```http
DELETE {{base_url}}/drivers/:id/remove-caretaker
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Caretaker removed successfully"
}
```

---

### Bulk Assign Caretaker
```http
POST {{base_url}}/caretakers/assign-bulk
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "caretaker_id": 5,
    "vehicle_ids": [1, 2, 3],
    "driver_ids": [1, 2]
}
```

**Response:**
```json
{
    "success": true,
    "message": "Bulk assignment completed",
    "data": {
        "caretaker_id": 5,
        "caretaker_name": "John Manager",
        "vehicles_assigned": 3,
        "drivers_assigned": 2
    }
}
```

---

### Reassign Caretaker Bulk
```http
POST {{base_url}}/caretakers/reassign-bulk
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "from_caretaker_id": 5,
    "to_caretaker_id": 7
}
```

**Response:**
```json
{
    "success": true,
    "message": "Reassignment completed",
    "data": {
        "from_caretaker_id": 5,
        "to_caretaker_id": 7,
        "to_caretaker_name": "Jane Supervisor",
        "vehicles_reassigned": 3,
        "drivers_reassigned": 2
    }
}
```

**Use Case:** When a caretaker is being disabled or leaving

---

### Get Caretaker Assignments
```http
GET {{base_url}}/caretakers/:id/assignments
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Caretaker assignments retrieved",
    "data": {
        "caretaker": {
            "id": 5,
            "name": "John Manager",
            "role": "manager"
        },
        "vehicles": [
            {
                "id": 1,
                "registration_number": "MH12AB1234",
                "make": "Tata",
                "model": "Ace",
                "state": "active"
            }
        ],
        "vehicle_count": 1,
        "drivers": [
            {
                "id": 1,
                "name": "Rajesh Kumar",
                "mobile": "+919876543210",
                "status": "active",
                "is_active": true
            }
        ],
        "driver_count": 1
    }
}
```

---

### Get Orphaned Assignments
```http
GET {{base_url}}/caretakers/orphaned-assignments
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Orphaned assignments retrieved",
    "data": {
        "vehicles_no_caretaker": [],
        "vehicles_no_caretaker_count": 0,
        "vehicles_inactive_caretaker": [],
        "vehicles_inactive_caretaker_count": 0,
        "drivers_no_caretaker": [],
        "drivers_no_caretaker_count": 0,
        "drivers_inactive_caretaker": [],
        "drivers_inactive_caretaker_count": 0,
        "total_orphaned": 0
    }
}
```

**Use Case:** Identify vehicles/drivers needing caretaker assignment

---

## Error Responses

### Invalid Caretaker Role
```json
{
    "success": false,
    "message": "Invalid caretaker role",
    "error": "Caretaker must be a Manager or Supervisor. Current role: owner"
}
```

### Caretaker Not Found
```json
{
    "success": false,
    "message": "Caretaker not found",
    "error": "Caretaker must be an active team member"
}
```

