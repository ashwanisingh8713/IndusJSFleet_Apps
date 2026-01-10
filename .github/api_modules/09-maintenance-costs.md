# 09. Maintenance Costs

Vehicle maintenance cost management.

## Role Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| Add Maintenance Cost | ✅ | ✅ | ✅ | ✅ |
| Edit Maintenance Cost | ✅ | ✅ | ✅ | ✅ |
| Delete Maintenance Cost | ✅ | ✅ | ✅ | ❌ |
| View Maintenance Costs | ✅ | ✅ | ✅ | ✅ |

## Cost Types

| Type | Description |
|------|-------------|
| `service` | Regular service |
| `repair` | Repairs |
| `tyre` | Tyre replacement/repair |
| `battery` | Battery replacement |
| `oil_change` | Oil change |
| `brake` | Brake service |
| `engine` | Engine repair |
| `body` | Body work |
| `electrical` | Electrical work |
| `ac` | AC service/repair |
| `washing` | Vehicle washing |
| `other` | Other maintenance |

---

## Endpoints

### Get Maintenance Cost Types
```http
GET {{base_url}}/maintenance-costs/types
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": [
        {"type": "service", "label": "Regular Service"},
        {"type": "repair", "label": "Repairs"},
        {"type": "tyre", "label": "Tyre"},
        {"type": "battery", "label": "Battery"},
        {"type": "oil_change", "label": "Oil Change"},
        {"type": "brake", "label": "Brake Service"},
        {"type": "engine", "label": "Engine Repair"},
        {"type": "body", "label": "Body Work"},
        {"type": "electrical", "label": "Electrical"},
        {"type": "ac", "label": "AC Service"},
        {"type": "washing", "label": "Washing"},
        {"type": "other", "label": "Other"}
    ]
}
```

---

### Create Maintenance Cost
```http
POST {{base_url}}/vehicles/:id/maintenance-costs
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "cost_type": "tyre",
    "amount": 5000.00,
    "date": "2025-01-10T12:00:00Z",
    "description": "Front tyre replacement",
    "notes": "MRF tyres",
    "vendor_name": "MRF Tyre Shop",
    "invoice_no": "INV78901"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Maintenance cost created successfully",
    "data": {
        "id": 1,
        "vehicle_id": 1,
        "cost_type": "tyre",
        "amount": 5000.00,
        "date": "2025-01-10T12:00:00Z",
        "description": "Front tyre replacement",
        "vendor_name": "MRF Tyre Shop",
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

### Get Vehicle Maintenance Costs
```http
GET {{base_url}}/vehicles/:id/maintenance-costs
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Example |
|-----------|-------------|---------|
| cost_types | Filter by types | `?cost_types=tyre,service` |
| start_date | Start date | `?start_date=2025-01-01T00:00:00Z` |
| end_date | End date | `?end_date=2025-01-31T23:59:59Z` |
| page | Page number | `?page=1` |
| per_page | Items per page | `?per_page=20` |

**Response:**
```json
{
    "success": true,
    "data": {
        "costs": [
            {
                "id": 1,
                "vehicle_id": 1,
                "cost_type": "tyre",
                "amount": 5000.00,
                "date": "2025-01-10T12:00:00Z",
                "description": "Front tyre replacement",
                "created_by_user": {
                    "id": 1,
                    "first_name": "John",
                    "last_name": "Doe",
                    "role": "owner"
                }
            }
        ],
        "total": 5000.00,
        "count": 1
    }
}
```

---

### Get Maintenance Cost
```http
GET {{base_url}}/maintenance-costs/:id
Authorization: Bearer {{token}}
```

---

### Update Maintenance Cost
```http
PUT {{base_url}}/maintenance-costs/:id
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "cost_type": "tyre",
    "amount": 5500.00,
    "description": "Front tyre replacement - updated",
    "notes": "MRF ZLO tyres",
    "vendor_name": "MRF Authorized Dealer",
    "invoice_no": "INV78902"
}
```

---

### Delete Maintenance Cost
```http
DELETE {{base_url}}/maintenance-costs/:id
Authorization: Bearer {{token}}
```

**Note:** Supervisors cannot delete costs

---

### Bulk Create Maintenance Costs
```http
POST {{base_url}}/vehicles/:id/maintenance-costs/bulk
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "costs": [
        {
            "cost_type": "tyre",
            "amount": 5000.00,
            "date": "2025-01-10T12:00:00Z",
            "description": "Front tyre",
            "notes": "MRF",
            "vendor_name": "MRF Shop",
            "invoice_no": "INV001"
        },
        {
            "cost_type": "oil_change",
            "amount": 1500.00,
            "date": "2025-01-10T12:00:00Z",
            "description": "Engine oil change",
            "vendor_name": "Service Center"
        }
    ]
}
```

---

### Get Maintenance Cost Summary
```http
GET {{base_url}}/vehicles/:id/maintenance-costs/summary
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description |
|-----------|-------------|
| start_date | Start date |
| end_date | End date |

**Response:**
```json
{
    "success": true,
    "data": {
        "vehicle_id": 1,
        "total_cost": 15000.00,
        "by_type": {
            "tyre": 5000.00,
            "service": 8000.00,
            "oil_change": 2000.00
        },
        "cost_count": 5,
        "period": {
            "start_date": "2025-01-01",
            "end_date": "2025-01-31"
        }
    }
}
```

---

### Get All Maintenance Costs (Owner)
```http
GET {{base_url}}/maintenance-costs
Authorization: Bearer {{token}}
```

Returns all maintenance costs across all vehicles for the owner.

**Query Parameters:**
| Parameter | Description |
|-----------|-------------|
| vehicle_id | Filter by vehicle |
| cost_types | Filter by types |
| start_date | Start date |
| end_date | End date |

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

### Invalid Cost Type
```json
{
    "success": false,
    "message": "Invalid cost type",
    "error": "invalid_type is not a valid maintenance cost type"
}
```

