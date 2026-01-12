# 09. Maintenance Costs

Vehicle maintenance cost tracking for repairs, services, and replacements.

## Role Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| Add Maintenance Cost | ✅ | ✅ | ✅ | ✅ |
| Edit Maintenance Cost | ✅ | ✅ | ✅ | ✅ |
| Delete Maintenance Cost | ✅ | ✅ | ✅ | ❌ |
| View Maintenance Costs | ✅ | ✅ | ✅ | ✅ |

## Cost Structure (NEW)

All maintenance costs use a structured ID system:

| Field | Description | Example |
|-------|-------------|---------|
| `cost_id` | Unique cost identifier | `VMC-002-003` |
| `cost_label` | Human-readable label | `Tyres Replacement` |
| `group_id` | Group identifier | `VMC-G-002` |

## Cost Groups

| Group ID | Group Name | Cost IDs |
|----------|------------|----------|
| `VMC-G-001` | Regular Maintenance | VMC-001-001 (Engine Oil Change), VMC-001-002 (Oil Filter), VMC-001-003 (Air Filter), VMC-001-004 (Wheel Alignment), VMC-001-005 (General Servicing) |
| `VMC-G-002` | Repairs & Replacements | VMC-002-001 (Brake Pads/Discs), VMC-002-002 (Battery), VMC-002-003 (Tyres), VMC-002-004 (Clutch), VMC-002-005 (Suspension) |
| `VMC-G-003` | Electrical & AC | VMC-003-001 (AC Service), VMC-003-002 (Electrical Wiring), VMC-003-003 (Alternator/Starter), VMC-003-004 (Lights) |
| `VMC-G-004` | Body & Exterior | VMC-004-001 (Denting & Painting), VMC-004-002 (Windshield/Glass), VMC-004-003 (Body Parts), VMC-004-004 (Washing & Cleaning) |
| `VMC-G-005` | Engine & Transmission | VMC-005-001 (Engine Overhaul), VMC-005-002 (Gearbox), VMC-005-003 (Radiator), VMC-005-004 (Fuel System) |
| `VMC-G-006` | Miscellaneous | VMC-006-001 (Accessories), VMC-006-002 (GPS/Tracking Device), VMC-006-003 (Other) |
| `VMC-G-007` | Wheels & Tires | VMC-007-001 (Tire Rotation), VMC-007-002 (Wheel Alignment), VMC-007-003 (Tire Replacement) |

## Special Fields

### Other Type (VMC-006-003)
- `custom_cost_label`: Custom description for "Other" expenses

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
    "message": "Maintenance cost types retrieved successfully",
    "data": {
        "category_id": "VMC-001",
        "category_name": "Vehicle Maintenance Costs",
        "groups": [
            {
                "group_id": "VMC-G-001",
                "group_name": "Regular Maintenance",
                "items": [
                    {"id": "VMC-001-001", "value": "VMC-001-001", "label": "Engine Oil Change"},
                    {"id": "VMC-001-002", "value": "VMC-001-002", "label": "Oil Filter Replacement"},
                    {"id": "VMC-001-003", "value": "VMC-001-003", "label": "Air Filter Replacement"},
                    {"id": "VMC-001-004", "value": "VMC-001-004", "label": "Wheel Alignment & Balancing"},
                    {"id": "VMC-001-005", "value": "VMC-001-005", "label": "General Servicing Labor"}
                ]
            },
            {
                "group_id": "VMC-G-002",
                "group_name": "Repairs & Replacements",
                "items": [
                    {"id": "VMC-002-001", "value": "VMC-002-001", "label": "Brake Pads / Discs"},
                    {"id": "VMC-002-002", "value": "VMC-002-002", "label": "Battery Replacement"},
                    {"id": "VMC-002-003", "value": "VMC-002-003", "label": "Tyres Replacement"},
                    {"id": "VMC-002-004", "value": "VMC-002-004", "label": "Clutch Repair"},
                    {"id": "VMC-002-005", "value": "VMC-002-005", "label": "Suspension Repair"}
                ]
            }
        ]
    }
}
```

---

### Create Maintenance Cost

```http
POST {{base_url}}/maintenance-costs
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body (Tyres Replacement):**
```json
{
    "vehicle_id": 1,
    "cost_id": "VMC-002-003",
    "cost_label": "Tyres Replacement",
    "group_id": "VMC-G-002",
    "amount": 25000.00,
    "date": "20-12-2025",
    "time": "14:00",
    "description": "Replaced 2 front tyres",
    "notes": "MRF brand",
    "vendor_name": "Sharma Tyre Works",
    "invoice_no": "INV-2025-001"
}
```

**Request Body (Other with Custom Label):**
```json
{
    "vehicle_id": 1,
    "cost_id": "VMC-006-003",
    "cost_label": "Other",
    "group_id": "VMC-G-006",
    "amount": 1500.00,
    "date": "20-12-2025",
    "time": "16:00",
    "custom_cost_label": "Insurance Premium",
    "description": "Quarterly insurance payment",
    "vendor_name": "ICICI Lombard",
    "invoice_no": "INS-2025-Q4"
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
        "cost_id": "VMC-002-003",
        "cost_label": "Tyres Replacement",
        "group_id": "VMC-G-002",
        "amount": 25000.00,
        "date": "2025-12-20T00:00:00Z",
        "time": "14:00",
        "description": "Replaced 2 front tyres",
        "notes": "MRF brand",
        "vendor_name": "Sharma Tyre Works",
        "invoice_no": "INV-2025-001",
        "created_by_id": 1,
        "created_by_user": {
            "id": 1,
            "first_name": "John",
            "last_name": "Doe",
            "role": "owner"
        },
        "created_at": "2025-12-20T14:00:00Z"
    }
}
```

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
            "cost_id": "VMC-001-001",
            "cost_label": "Engine Oil Change",
            "group_id": "VMC-G-001",
            "amount": 5000.00,
            "date": "2026-12-20T10:00:00Z",
            "description": "Regular oil change",
            "notes": "Castrol 5W30",
            "vendor_name": "ABC Service Center",
            "invoice_no": "INV-001"
        },
        {
            "cost_id": "VMC-004-004",
            "cost_label": "Washing & Cleaning",
            "group_id": "VMC-G-004",
            "amount": 500.00,
            "date": "2026-12-20T11:00:00Z",
            "description": "Full wash",
            "notes": "Interior + Exterior"
        }
    ]
}
```

**Response (Success):**
```json
{
    "success": true,
    "message": "Maintenance costs processed",
    "created_count": 2,
    "error_count": 0,
    "costs": [
        {
            "id": 1,
            "vehicle_id": 1,
            "cost_id": "VMC-001-001",
            "cost_label": "Engine Oil Change",
            "group_id": "VMC-G-001",
            "amount": 5000.00
        }
    ]
}
```

**Response (Validation Error - 400):**
```json
{
    "success": false,
    "message": "All entries failed validation",
    "data": {
        "error_count": 1,
        "errors": ["Entry 1: invalid cost_id or group_id"]
    }
}
```

---

### List All Maintenance Costs

```http
GET {{base_url}}/maintenance-costs
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Example |
|-----------|-------------|---------|
| `vehicle_id` | Filter by vehicle | `?vehicle_id=1` |
| `cost_id` | Filter by cost_id (comma-separated) | `?cost_id=VMC-002-003,VMC-001-001` |
| `start_date` | Start date (DD-MM-YYYY) | `?start_date=01-01-2025` |
| `end_date` | End date (DD-MM-YYYY) | `?end_date=31-12-2025` |
| `page` | Page number | `?page=1` |
| `per_page` | Items per page | `?per_page=20` |

**Response:**
```json
{
    "success": true,
    "data": {
        "costs": [
            {
                "id": 1,
                "vehicle_id": 1,
                "cost_id": "VMC-002-003",
                "cost_label": "Tyres Replacement",
                "group_id": "VMC-G-002",
                "amount": 25000.00,
                "date": "2025-12-20T00:00:00Z",
                "vendor_name": "Sharma Tyre Works",
                "created_by_user": {...}
            }
        ],
        "total_cost": 25000.00,
        "count": 1,
        "page": 1,
        "per_page": 20,
        "total_pages": 1
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
| `cost_id` | Filter by cost_id (comma-separated) | `?cost_id=VMC-002-003,VMC-001-001` |
| `start_date` | Start date (DD-MM-YYYY) | `?start_date=01-01-2025` |
| `end_date` | End date (DD-MM-YYYY) | `?end_date=31-12-2025` |
| `sort_by` | Sort field (date, amount, cost_id) | `?sort_by=date` |
| `sort_order` | Sort direction (asc, desc) | `?sort_order=desc` |
| `page` | Page number | `?page=1` |
| `per_page` | Items per page | `?per_page=20` |

**Response:**
```json
{
    "success": true,
    "data": {
        "costs": [...],
        "total_cost": 30500.00,
        "filtered_total": 25000.00,
        "cost_by_group": {
            "VMC-G-001": 5500.00,
            "VMC-G-002": 25000.00
        },
        "count": 3,
        "page": 1,
        "per_page": 20,
        "total_pages": 1,
        "has_more": false,
        "applied_filters": {
            "cost_ids": ["VMC-002-003"]
        }
    }
}
```

---

### Get Maintenance Cost

```http
GET {{base_url}}/maintenance-costs/:id
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "id": 1,
        "vehicle_id": 1,
        "cost_id": "VMC-002-003",
        "cost_label": "Tyres Replacement",
        "group_id": "VMC-G-002",
        "amount": 25000.00,
        "date": "2025-12-20T00:00:00Z",
        "time": "14:00",
        "description": "Replaced 2 front tyres",
        "vendor_name": "Sharma Tyre Works",
        "invoice_no": "INV-2025-001",
        "vehicle": {
            "id": 1,
            "registration_number": "MH01AB1234"
        },
        "created_by_user": {...}
    }
}
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
    "cost_id": "VMC-002-003",
    "cost_label": "Tyres Replacement",
    "group_id": "VMC-G-002",
    "amount": 26000.00,
    "notes": "Updated with GST"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Maintenance cost updated successfully",
    "data": {...}
}
```

---

### Delete Maintenance Cost

```http
DELETE {{base_url}}/maintenance-costs/:id
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Maintenance cost deleted successfully"
}
```

---

### Get Maintenance Summary

```http
GET {{base_url}}/vehicles/:id/maintenance-costs/summary
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "summary": {
            "vehicle_id": 1,
            "total_cost": 30500.00,
            "maintenance_count": 3,
            "last_maintenance_at": "20-12-2025"
        },
        "breakdown": [
            {
                "group_id": "VMC-G-001",
                "group_name": "Regular Maintenance",
                "total_cost": 5500.00,
                "entry_count": 1
            },
            {
                "group_id": "VMC-G-002",
                "group_name": "Repairs & Replacements",
                "total_cost": 25000.00,
                "entry_count": 2
            }
        ]
    }
}
```

---

### Get Maintenance History

```http
GET {{base_url}}/vehicles/:id/maintenance-costs/history
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description |
|-----------|-------------|
| `page` | Page number |
| `per_page` | Items per page |

**Response:**
```json
{
    "success": true,
    "data": {
        "costs": [...],
        "count": 10,
        "page": 1,
        "per_page": 20,
        "total_pages": 1
    }
}
```

