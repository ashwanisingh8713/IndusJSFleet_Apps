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
| View Financial Fields | ✅ | ✅ | ❌ | ❌ |
| Edit Trip (Planned) | ✅ | ✅ | ✅ | ❌ |
| Edit Trip (In Progress) | ✅ | ✅ | ❌ | ❌ |
| Edit Trip (Completed) | ✅ | ✅ | ❌ | ❌ |
| Record Payment | ✅ | ✅ | ❌ | ❌ |
| Change Trip State | ✅ | ✅ | ✅ | ❌ |
| Delete Trip | ✅ | ✅ | ❌ | ❌ |

## Trip States
- `planned` - Trip is scheduled
- `assigned` - Vehicle and driver confirmed
- `on_route` - Trip is in progress
- `completed` - Trip has finished
- `cancelled` - Trip was cancelled
- `failed` - Trip could not be completed
- `delayed` - Trip is behind schedule

## Payment Status
- `pending` - No payment received
- `partial` - Partial payment received
- `full` - Full payment received

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
    "scheduled_date": "2026-01-15T00:00:00Z",
    "start_time": "2026-01-15T08:00:00Z",
    "delivery_date": "2026-01-15T00:00:00Z",
    "delivery_time": "2026-01-15T18:00:00Z",
    "planned_start": "2026-01-15T08:00:00Z",
    "planned_end": "2026-01-15T18:00:00Z",
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
    "customer_id": 1,
    "expected_trip_price": 15000,
    "priority": "normal",
    "notes": "Handle with care"
}
```

**Payment Fields (Summary - calculated from trip_payments):**
| Field | Type | Description |
|-------|------|-------------|
| `customer_id` | uint | Reference to Customer entity (preferred) |
| `expected_trip_price` | float | Quoted price to customer |
| `paid_trip_price` | float | Total net amount paid (read-only, auto-calculated) |
| `payment_count` | int | Number of payments received (read-only) |
| `payment_status` | string | pending/partial/full (read-only) |
| `trip_comment` | string | Customer/trip comment |
| `trip_review` | string | Trip review/feedback |

**Note:** Individual payments are recorded via `/api/v2/trip-payments` endpoint. See Trip Payments API for details.

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
        "customer_id": 1,
        "customer": {
            "id": 1,
            "company_name": "ABC Corp",
            "person_name": "Rajesh",
            "primary_contact": "+919876543210"
        },
        "expected_trip_price": 15000,
        "paid_trip_price": 0,
        "payment_count": 0,
        "is_full_payment_done": false,
        "is_payment_pending": true,
        "payment_status": "pending",
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
| state | Filter by state | `?state=on_route` |
| vehicle_id | Filter by vehicle | `?vehicle_id=1` |
| driver_id | Filter by driver | `?driver_id=1` |
| customer_id | Filter by customer | `?customer_id=1` |
| priority | Filter by priority | `?priority=high` |
| cargo_type | Filter by cargo | `?cargo_type=fragile` |
| payment_status | Filter by payment | `?payment_status=pending` |
| page | Page number | `?page=1` |
| per_page | Items per page | `?per_page=20` |

**Response:**
```json
{
    "success": true,
    "data": {
        "items": [
            {
                "id": 1,
                "vehicle_id": 1,
                "driver_id": 1,
                "state": "on_route",
                "state_label": "On Route",
                "start_location": "Mumbai",
                "end_location": "Pune",
                "scheduled_date": "15-01-2026",
                "vehicle_number": "MH12AB1234",
                "driver_name": "Rajesh Kumar",
                "estimated_distance": 150.5,
                "estimated_distance_label": "150.5 km",
                "distance_display": "75.2 km covered",
                "customer_id": 1,
                "customer_name": "ABC Corp",
                "expected_trip_price": 15000,
                "paid_trip_price": 5000,
                "is_full_payment_done": false,
                "is_payment_pending": true,
                "payment_status": "partial"
            }
        ],
        "count": 1,
        "page": 1,
        "per_page": 20,
        "total_pages": 1,
        "has_more": false
    }
}
```

**Note:** Financial fields are hidden (returns 0) for Manager and Supervisor roles.

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
        "state": "on_route",
        "start_location": "Mumbai",
        "end_location": "Pune",
        "estimated_distance": 150.5,
        "covered_distance": 75.2,
        "customer_id": 1,
        "customer": {
            "id": 1,
            "company_name": "ABC Corp",
            "person_name": "Rajesh",
            "primary_contact": "+919876543210"
        },
        "expected_trip_price": 15000,
        "paid_trip_price": 5000,
        "paid_amount": 5000,
        "is_full_payment_done": false,
        "is_payment_pending": true,
        "payment_status": "partial",
        "payment_mode": "upi",
        "trip_comment": "Good service",
        "trip_review": "",
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
            "state_label": "On Route",
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
    "customer_id": 2,
    "expected_trip_price": 18000,
    "trip_comment": "Updated comment",
    "priority": "high"
}
```

**Notes:**
- Only planned trips can be updated by Manager
- Owner and General Manager can update trips in any state
- Financial fields can only be updated by Owner/GM

---

### Get Trip Payments
```http
GET {{base_url}}/trips/:id/payments
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Trip payments retrieved",
    "data": {
        "trip_id": 1,
        "expected_amount": 15000,
        "summary": {
            "total_received": 15000,
            "total_tds": 150,
            "total_discount": 0,
            "net_received": 14850,
            "payment_count": 2,
            "payment_status": "full"
        },
        "payments": [
            {
                "id": 1,
                "receipt_number": "RP-202601-001-00001",
                "amount": 5000,
                "tds_amount": 0,
                "discount_amount": 0,
                "net_amount": 5000,
                "payment_type": "advance",
                "payment_mode": "cash",
                "payment_status": "received",
                "payment_date": "2026-01-10T10:00:00Z",
                "received_by": "Driver - Ramesh"
            },
            {
                "id": 2,
                "receipt_number": "RP-202601-001-00002",
                "amount": 10000,
                "tds_amount": 150,
                "discount_amount": 0,
                "net_amount": 9850,
                "payment_type": "final",
                "payment_mode": "bank_transfer",
                "payment_status": "received",
                "payment_date": "2026-01-15T14:00:00Z",
                "transaction_id": "NEFT123456789"
            }
        ]
    }
}
```

**Note:** To record payments, use the **Trip Payments API** (`/api/v2/trip-payments`). See `08-trip-payments.md` for full documentation.

### Record Payment to Trip (Shortcut)
```http
POST {{base_url}}/trips/:id/payments
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "amount": 10000,
    "payment_type": "partial",
    "payment_mode": "bank_transfer",
    "payment_date": "2026-01-15T14:00:00Z",
    "tds_amount": 100,
    "transaction_id": "NEFT123456789",
    "notes": "Partial payment via NEFT"
}
```

**Payment Types:** `advance`, `partial`, `final`, `refund`
**Payment Modes:** `cash`, `upi`, `bank_transfer`, `card`, `credit`, `other`

**Response:**
```json
{
    "success": true,
    "message": "Payment recorded successfully",
    "data": {
        "payment": {
            "id": 2,
            "trip_id": 1,
            "receipt_number": "RP-202601-001-00002",
            "amount": 10000,
            "tds_amount": 100,
            "net_amount": 9900,
            "payment_type": "partial",
            "payment_mode": "bank_transfer",
            "payment_status": "received",
            "payment_date": "2026-01-15T14:00:00Z",
            "financial_year": "2025-26",
            "financial_month": "2026-01"
        },
        "trip_summary": {
            "expected_trip_price": 15000,
            "paid_trip_price": 14900,
            "payment_count": 2,
            "pending_amount": 100,
            "payment_status": "partial"
        }
    }
}
```

**Payment Calculation Logic:**
- `paid_trip_price` = SUM of all `net_amount` where `payment_status = 'received'`
- `payment_count` = COUNT of payments with status 'received'
- `is_full_payment_done = (paid_trip_price >= expected_trip_price)`
- `is_payment_pending = (paid_trip_price < expected_trip_price)`
- `pending_amount = expected_trip_price - paid_trip_price`

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
    "state": "on_route"
}
```

**Valid Transitions:**
- `planned` → `assigned` or `cancelled`
- `assigned` → `on_route` or `cancelled`
- `on_route` → `completed`, `delayed`, `failed`, or `cancelled`
- `delayed` → `on_route`, `completed`, or `failed`
- `completed` → (no transitions - terminal)
- `cancelled` → (no transitions - terminal)
- `failed` → (no transitions - terminal)

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
    "error": "Vehicle is already assigned to trip #5 (status: on_route)"
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
    "error": "cannot transition from completed to on_route"
}
```

### Invalid Payment
```json
{
    "success": false,
    "message": "Invalid payment",
    "error": "Payment amount must be greater than 0"
}
```
