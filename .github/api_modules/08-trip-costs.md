# 08. Trip Costs

Trip cost management for expenses incurred during trips.

## Role Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| Add Trip Cost | ✅ | ✅ | ✅ | ✅ |
| Edit Trip Cost | ✅ | ✅ | ✅ | ✅ |
| Delete Trip Cost | ✅ | ✅ | ✅ | ❌ |
| View Trip Costs | ✅ | ✅ | ✅ | ✅ |

## Cost Types

| Type | Description |
|------|-------------|
| `fuel` | Fuel expenses |
| `toll` | Toll charges |
| `driver_allowance` | Driver allowance |
| `parking` | Parking fees |
| `loading_charges` | Loading charges |
| `unloading_charges` | Unloading charges |
| `chalan` | Chalan/Fines |
| `permit` | Permit fees |
| `state_permit` | State permit |
| `national_permit` | National permit |
| `insurance` | Insurance |
| `other` | Other expenses |

---

## Endpoints

### Get Trip Cost Types
```http
GET {{base_url}}/trip-costs/types
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": [
        {"type": "fuel", "label": "Fuel"},
        {"type": "toll", "label": "Toll"},
        {"type": "driver_allowance", "label": "Driver Allowance"},
        {"type": "parking", "label": "Parking"},
        {"type": "loading_charges", "label": "Loading Charges"},
        {"type": "unloading_charges", "label": "Unloading Charges"},
        {"type": "chalan", "label": "Chalan/Fine"},
        {"type": "permit", "label": "Permit"},
        {"type": "other", "label": "Other"}
    ]
}
```

---

### Create Trip Cost
```http
POST {{base_url}}/trip-costs
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "trip_id": 1,
    "vehicle_id": 1,
    "cost_type": "fuel",
    "amount": 2500.00,
    "date": "2025-01-15T10:00:00Z",
    "description": "Diesel refill at HP Pump",
    "notes": "Full tank",
    "vendor_name": "HP Petrol Pump",
    "invoice_no": "INV123456"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Trip cost created successfully",
    "data": {
        "id": 1,
        "trip_id": 1,
        "vehicle_id": 1,
        "cost_type": "fuel",
        "amount": 2500.00,
        "date": "2025-01-15T10:00:00Z",
        "description": "Diesel refill at HP Pump",
        "created_by_id": 1,
        "created_by_user": {
            "id": 1,
            "first_name": "John",
            "last_name": "Doe",
            "role": "owner"
        }
    }
}
```

---

### Get Trip Costs
```http
GET {{base_url}}/trips/:id/costs
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Example |
|-----------|-------------|---------|
| cost_types | Filter by types (comma-separated) | `?cost_types=fuel,toll` |
| start_date | Start date | `?start_date=2025-01-01T00:00:00Z` |
| end_date | End date | `?end_date=2025-01-31T23:59:59Z` |

**Response:**
```json
{
    "success": true,
    "data": {
        "costs": [
            {
                "id": 1,
                "trip_id": 1,
                "cost_type": "fuel",
                "amount": 2500.00,
                "date": "2025-01-15T10:00:00Z",
                "description": "Diesel refill",
                "created_by_user": {
                    "id": 1,
                    "first_name": "John",
                    "last_name": "Doe",
                    "role": "owner"
                }
            }
        ],
        "total": 2500.00,
        "count": 1
    }
}
```

---

### Get Trip Cost
```http
GET {{base_url}}/trip-costs/:id
Authorization: Bearer {{token}}
```

---

### Update Trip Cost
```http
PUT {{base_url}}/trip-costs/:id
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "cost_type": "fuel",
    "amount": 2800.00,
    "description": "Diesel refill - updated",
    "notes": "Full tank plus extra",
    "vendor_name": "HP Petrol Pump",
    "invoice_no": "INV123457"
}
```

---

### Delete Trip Cost
```http
DELETE {{base_url}}/trip-costs/:id
Authorization: Bearer {{token}}
```

**Note:** Supervisors cannot delete costs

---

### Bulk Create Trip Costs
```http
POST {{base_url}}/trips/:id/costs/bulk
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "costs": [
        {
            "cost_type": "fuel",
            "amount": 2500.00,
            "date": "2025-01-15T10:00:00Z",
            "description": "Diesel refill"
        },
        {
            "cost_type": "toll",
            "amount": 350.00,
            "date": "2025-01-15T11:00:00Z",
            "description": "Mumbai-Pune Expressway"
        }
    ]
}
```

---

### Get Trip Cost History
```http
GET {{base_url}}/trips/:id/costs/history
Authorization: Bearer {{token}}
```

Returns cost history with audit trail.

---

### Get Trip Cost Summary
```http
GET {{base_url}}/trips/:id/costs/summary
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "total_cost": 5500.00,
        "by_type": {
            "fuel": 2500.00,
            "toll": 350.00,
            "driver_allowance": 500.00,
            "other": 2150.00
        },
        "cost_count": 5
    }
}
```

---

### Get Trip Cost Details
```http
GET {{base_url}}/trips/:id/costs/details
Authorization: Bearer {{token}}
```

Returns detailed breakdown with all costs and metadata.

---

## Vehicle Trip Costs

### Get Vehicle Trip Costs
```http
GET {{base_url}}/vehicles/:id/trip-costs
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description |
|-----------|-------------|
| cost_types | Filter by types |
| start_date | Start date |
| end_date | End date |
| page | Page number |
| per_page | Items per page |

---

## Error Responses

### Trip Not Found
```json
{
    "success": false,
    "message": "Trip not found",
    "error": "record not found"
}
```

### Invalid Cost Type
```json
{
    "success": false,
    "message": "Invalid cost type",
    "error": "invalid_type is not a valid cost type"
}
```

### Delete Not Allowed
```json
{
    "success": false,
    "message": "Access denied",
    "error": "Supervisors cannot delete costs"
}
```

