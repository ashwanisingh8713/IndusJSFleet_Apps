# 08. Trip Costs

Trip cost management for expenses incurred during trips.

## Role Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| Add Trip Cost | ✅ | ✅ | ✅ | ✅ |
| Edit Trip Cost | ✅ | ✅ | ✅ | ✅ |
| Delete Trip Cost | ✅ | ✅ | ✅ | ❌ |
| View Trip Costs | ✅ | ✅ | ✅ | ✅ |

## Cost Structure (NEW)

All trip costs use a structured ID system:

| Field | Description | Example |
|-------|-------------|---------|
| `cost_id` | Unique cost identifier | `TC-001-002` |
| `cost_label` | Human-readable label | `Diesel` |
| `group_id` | Group identifier | `TC-G-001` |

## Cost Groups

| Group ID | Group Name | Cost IDs |
|----------|------------|----------|
| `TC-G-001` | Fuel & Energy | TC-001-001 (Petrol), TC-001-002 (Diesel), TC-001-003 (CNG/LPG), TC-001-004 (EV Charging) |
| `TC-G-002` | Toll & Parking | TC-002-001 (Toll Charges), TC-002-002 (Parking Fees), TC-002-003 (Entry Charges) |
| `TC-G-003` | Loading & Unloading | TC-003-001 (Loading), TC-003-002 (Unloading), TC-003-003 (Crane/Forklift), TC-003-004 (Labor) |
| `TC-G-004` | Driver Expenses | TC-004-001 (Allowance), TC-004-002 (Food), TC-004-003 (Accommodation) |
| `TC-G-005` | Permits & Compliance | TC-005-001 (State Permit), TC-005-002 (National Permit), TC-005-003 (Special Permit), TC-005-004 (Chalan/Fine) |
| `TC-G-006` | Miscellaneous | TC-006-001 (Police), TC-006-002 (RTO), TC-006-003 (Weighbridge), TC-006-004 (Commission), TC-006-005 (Other) |

## Special Fields

### Fuel & Energy Group (TC-G-001)
Additional fields for fuel tracking:
- `fuel_quantity`: Liters/kWh
- `fuel_rate`: Rate per unit (₹/liter)
- `km_per_liter`: Fuel efficiency

### Other Type (TC-006-005)
- `custom_cost_label`: Custom description for "Other" expenses

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
    "message": "Trip cost types retrieved successfully",
    "data": {
        "category_id": "TC-001",
        "category_name": "Trip Costs",
        "groups": [
            {
                "group_id": "TC-G-001",
                "group_name": "Fuel & Energy",
                "items": [
                    {"id": "TC-001-001", "value": "TC-001-001", "label": "Petrol"},
                    {"id": "TC-001-002", "value": "TC-001-002", "label": "Diesel"},
                    {"id": "TC-001-003", "value": "TC-001-003", "label": "CNG / LPG"},
                    {"id": "TC-001-004", "value": "TC-001-004", "label": "EV Charging"}
                ]
            },
            {
                "group_id": "TC-G-002",
                "group_name": "Toll & Parking",
                "items": [
                    {"id": "TC-002-001", "value": "TC-002-001", "label": "Toll Charges"},
                    {"id": "TC-002-002", "value": "TC-002-002", "label": "Parking Fees"},
                    {"id": "TC-002-003", "value": "TC-002-003", "label": "Entry Charges"}
                ]
            }
        ]
    }
}
```

---

### Create Trip Cost

```http
POST {{base_url}}/trip-costs
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body (Fuel - Diesel):**
```json
{
    "trip_id": 1,
    "vehicle_id": 1,
    "cost_id": "TC-001-002",
    "cost_label": "Diesel",
    "group_id": "TC-G-001",
    "amount": 5000.00,
    "date": "20-12-2025",
    "time": "10:30",
    "notes": "Diesel refill at HP Pump",
    "fuel_quantity": 55.5,
    "fuel_rate": 90.09,
    "km_per_liter": 4.5
}
```

**Request Body (Toll):**
```json
{
    "trip_id": 1,
    "vehicle_id": 1,
    "cost_id": "TC-002-001",
    "cost_label": "Toll Charges",
    "group_id": "TC-G-002",
    "amount": 350.00,
    "date": "20-12-2025",
    "time": "11:15",
    "notes": "Mumbai-Pune Expressway Toll"
}
```

**Request Body (Other with Custom Label):**
```json
{
    "trip_id": 1,
    "vehicle_id": 1,
    "cost_id": "TC-006-005",
    "cost_label": "Other",
    "group_id": "TC-G-006",
    "amount": 200.00,
    "date": "20-12-2025",
    "time": "14:00",
    "custom_cost_label": "Driver Meals",
    "notes": "Lunch expense for driver"
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
        "cost_id": "TC-001-002",
        "cost_label": "Diesel",
        "group_id": "TC-G-001",
        "amount": 5000.00,
        "date": "2025-12-20T00:00:00Z",
        "time": "10:30",
        "notes": "Diesel refill at HP Pump",
        "fuel_quantity": 55.5,
        "fuel_rate": 90.09,
        "km_per_liter": 4.5,
        "created_by_id": 1,
        "created_by_user": {
            "id": 1,
            "first_name": "John",
            "last_name": "Doe",
            "role": "owner"
        },
        "created_at": "2025-12-20T10:30:00Z"
    }
}
```

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
            "cost_id": "TC-001-002",
            "cost_label": "Diesel",
            "group_id": "TC-G-001",
            "amount": 5000.00,
            "date": "2026-12-20T10:30:00Z",
            "time": "2026-12-20T10:30:00Z",
            "notes": "Diesel refill at HP Pump",
            "fuel_quantity": 55.5,
            "fuel_rate": 90.09,
            "km_per_liter": 4.5
        },
        {
            "cost_id": "TC-002-001",
            "cost_label": "Toll Charges",
            "group_id": "TC-G-002",
            "amount": 300.00,
            "date": "2026-12-20T11:00:00Z",
            "notes": "Toll plaza fee"
        }
    ]
}
```

**Response (Success):**
```json
{
    "success": true,
    "message": "Trip costs processed",
    "created_count": 2,
    "error_count": 0,
    "costs": [
        {
            "id": 101,
            "trip_id": 1,
            "vehicle_id": 1,
            "cost_id": "TC-001-002",
            "cost_label": "Diesel",
            "group_id": "TC-G-001",
            "amount": 5000.00
        }
    ]
}
```

**Response (Partial Success - 206):**
```json
{
    "success": true,
    "message": "Some trip costs failed validation",
    "created_count": 1,
    "error_count": 1,
    "costs": [...],
    "errors": ["Entry 2: invalid cost_id or group_id"]
}
```

---

### Get Trip Costs

```http
GET {{base_url}}/trips/:id/costs
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "costs": [
            {
                "id": 1,
                "trip_id": 1,
                "cost_id": "TC-001-002",
                "cost_label": "Diesel",
                "group_id": "TC-G-001",
                "amount": 5000.00,
                "date": "2025-12-20T00:00:00Z",
                "fuel_quantity": 55.5,
                "fuel_rate": 90.09,
                "km_per_liter": 4.5,
                "created_by_user": {
                    "id": 1,
                    "first_name": "John",
                    "last_name": "Doe",
                    "role": "owner"
                }
            }
        ],
        "total_cost": 5000.00,
        "cost_by_group": {
            "TC-G-001": 5000.00
        },
        "count": 1
    }
}
```

---

### Get Vehicle Trip Costs

```http
GET {{base_url}}/vehicles/:id/trip-costs
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Example |
|-----------|-------------|---------|
| `cost_id` | Filter by cost_id (comma-separated) | `?cost_id=TC-001-002,TC-002-001` |
| `start_date` | Start date (DD-MM-YYYY) | `?start_date=01-01-2025` |
| `end_date` | End date (DD-MM-YYYY) | `?end_date=31-12-2025` |
| `sort_by` | Sort field | `?sort_by=date` |
| `sort_order` | Sort direction | `?sort_order=desc` |
| `page` | Page number | `?page=1` |
| `per_page` | Items per page | `?per_page=20` |

**Response:**
```json
{
    "success": true,
    "data": {
        "costs": [...],
        "total_cost": 15000.00,
        "filtered_total": 5000.00,
        "cost_by_group": {
            "TC-G-001": 5000.00,
            "TC-G-002": 350.00
        },
        "count": 5,
        "page": 1,
        "per_page": 20,
        "total_pages": 1,
        "has_more": false,
        "applied_filters": {
            "cost_ids": ["TC-001-002"]
        }
    }
}
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
    "cost_id": "TC-001-002",
    "cost_label": "Diesel",
    "group_id": "TC-G-001",
    "amount": 5500.00,
    "notes": "Updated amount",
    "fuel_quantity": 60.0,
    "fuel_rate": 91.67
}
```

---

### Delete Trip Cost

```http
DELETE {{base_url}}/trip-costs/:id
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Trip cost deleted successfully"
}
```

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
        "summary": {
            "trip_id": 1,
            "total_cost": 5850.00,
            "cost_count": 3,
            "total_fuel_quantity": 55.5,
            "avg_fuel_rate": 90.09,
            "avg_km_per_liter": 4.5,
            "last_updated": "2025-12-20T14:30:00Z"
        },
        "breakdown": [
            {
                "group_id": "TC-G-001",
                "group_name": "Fuel & Energy",
                "total_cost": 5000.00,
                "entry_count": 1
            },
            {
                "group_id": "TC-G-002",
                "group_name": "Toll & Parking",
                "total_cost": 350.00,
                "entry_count": 1
            }
        ]
    }
}
```

---

### Get Trip Cost Details (by Category)

```http
GET {{base_url}}/trips/:id/costs/details
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "trip_info": {
            "id": 1,
            "start_location": "Mumbai",
            "end_location": "Pune"
        },
        "grand_total": 5850.00,
        "total_entries": 3,
        "categories": [
            {
                "category": "operating",
                "total_amount": 5350.00,
                "count": 2,
                "costs": [...]
            },
            {
                "category": "misc",
                "total_amount": 500.00,
                "count": 1,
                "costs": [...]
            }
        ]
    }
}
```

