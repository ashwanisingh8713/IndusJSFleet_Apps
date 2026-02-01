# 14. Customers

Customer management endpoints for managing client/customer details.

## Overview
Each owner maintains their own customer list. Customers can be associated with trips for better tracking and payment management.

## Role Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| Create Customer | ✅ | ✅ | ✅ | ❌ |
| View Customer | ✅ | ✅ | ✅ | ✅ |
| View Statistics | ✅ | ✅ | ❌ | ❌ |
| Edit Customer | ✅ | ✅ | ✅ | ❌ |
| Toggle Active | ✅ | ✅ | ✅ | ❌ |

---

## Endpoints

### Create Customer
```http
POST {{base_url}}/customers
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "company_name": "ABC Logistics Pvt Ltd",
    "person_name": "Rajesh Sharma",
    "primary_contact": "+919876543210",
    "secondary_contact": "+919876543211",
    "company_address": "123, Industrial Area, Mumbai 400001",
    "email": "rajesh@abclogistics.com",
    "gst_number": "27AABCU9603R1ZM",
    "notes": "Premium customer"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Customer created successfully",
    "data": {
        "id": 1,
        "company_name": "ABC Logistics Pvt Ltd",
        "person_name": "Rajesh Sharma",
        "primary_contact": "+919876543210",
        "secondary_contact": "+919876543211",
        "company_address": "123, Industrial Area, Mumbai 400001",
        "email": "rajesh@abclogistics.com",
        "gst_number": "27AABCU9603R1ZM",
        "notes": "Premium customer",
        "owner_id": 1,
        "is_active": true,
        "created_by_id": 1,
        "created_by": {
            "id": 1,
            "first_name": "John",
            "last_name": "Doe",
            "role": "owner"
        },
        "created_at": "2026-01-15T10:00:00Z",
        "updated_at": "2026-01-15T10:00:00Z"
    }
}
```

---

### List Customers
```http
GET {{base_url}}/customers
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Example |
|-----------|-------------|---------|
| search | Search by company/person name or contact | `?search=ABC` |
| is_active | Filter by active status | `?is_active=true` |
| page | Page number | `?page=1` |
| per_page | Items per page | `?per_page=20` |

**Response (Owner/GM - includes statistics):**
```json
{
    "success": true,
    "data": {
        "items": [
            {
                "id": 1,
                "company_name": "ABC Logistics Pvt Ltd",
                "person_name": "Rajesh Sharma",
                "primary_contact": "+919876543210",
                "is_active": true,
                "total_trips": 15,
                "total_revenue": 450000,
                "pending_payment": 50000,
                "created_at": "2026-01-15T10:00:00Z"
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

**Note:** `total_trips`, `total_revenue`, and `pending_payment` are only shown to Owner and General Manager.

---

### Get Customer
```http
GET {{base_url}}/customers/:id
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "id": 1,
        "company_name": "ABC Logistics Pvt Ltd",
        "person_name": "Rajesh Sharma",
        "primary_contact": "+919876543210",
        "secondary_contact": "+919876543211",
        "company_address": "123, Industrial Area, Mumbai 400001",
        "email": "rajesh@abclogistics.com",
        "gst_number": "27AABCU9603R1ZM",
        "is_active": true,
        "total_trips": 15,
        "total_revenue": 450000,
        "pending_payment": 50000,
        "created_by": {
            "id": 1,
            "first_name": "John",
            "last_name": "Doe",
            "role": "owner"
        },
        "created_at": "2026-01-15T10:00:00Z",
        "updated_at": "2026-01-15T10:00:00Z"
    }
}
```

---

### Update Customer
```http
PUT {{base_url}}/customers/:id
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "company_name": "ABC Logistics India Pvt Ltd",
    "person_name": "Rajesh Kumar Sharma",
    "primary_contact": "+919876543212",
    "company_address": "456, New Industrial Area, Mumbai 400002"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Customer updated successfully",
    "data": {
        "id": 1,
        "company_name": "ABC Logistics India Pvt Ltd",
        "person_name": "Rajesh Kumar Sharma",
        "primary_contact": "+919876543212",
        "is_active": true,
        "updated_at": "2026-01-15T11:00:00Z"
    }
}
```

---

### Toggle Customer Active
```http
PATCH {{base_url}}/customers/:id/toggle-active
Authorization: Bearer {{token}}
```

**Response (Disable):**
```json
{
    "success": true,
    "message": "Customer disabled successfully",
    "data": {
        "id": 1,
        "is_active": false
    }
}
```

**Note:** Cannot disable a customer with active trips (planned, assigned, or on_route).

---

### Get Customer Trips
```http
GET {{base_url}}/customers/:id/trips
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Example |
|-----------|-------------|---------|
| state | Filter by trip state | `?state=completed` |
| page | Page number | `?page=1` |
| per_page | Items per page | `?per_page=20` |

**Response:**
```json
{
    "success": true,
    "data": {
        "customer": {
            "id": 1,
            "company_name": "ABC Logistics Pvt Ltd",
            "person_name": "Rajesh Sharma",
            "primary_contact": "+919876543210"
        },
        "items": [
            {
                "id": 5,
                "start_location": "Mumbai",
                "end_location": "Pune",
                "state": "completed",
                "expected_trip_price": 15000,
                "paid_trip_price": 10000,
                "is_full_payment_done": false,
                "is_payment_pending": true,
                "vehicle": {
                    "id": 1,
                    "registration_number": "MH12AB1234"
                },
                "driver": {
                    "id": 1,
                    "first_name": "Rajesh",
                    "last_name": "Kumar"
                }
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

**Note:** Financial fields (`expected_trip_price`, `paid_trip_price`, etc.) are hidden for Manager and Supervisor.

---

## Error Responses

### Duplicate Contact
```json
{
    "success": false,
    "message": "Duplicate customer",
    "error": "Customer with contact +919876543210 already exists"
}
```

### Cannot Disable (Active Trips)
```json
{
    "success": false,
    "message": "Cannot disable customer",
    "error": "Customer has 2 active trip(s). Complete or cancel them first."
}
```

### Supervisor Access Denied
```json
{
    "success": false,
    "message": "Access denied",
    "error": "Supervisors cannot create customers"
}
```

