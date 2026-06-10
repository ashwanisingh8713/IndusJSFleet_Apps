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

### Get Customer Statistics (Owner/GM Only)
```http
GET {{base_url}}/customers/:id/statistics
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Example |
|-----------|-------------|---------|
| `start_date` | Start date (YYYY-MM-DD) | `?start_date=2026-01-01` |
| `end_date` | End date (YYYY-MM-DD) | `?end_date=2026-03-31` |

**Response:**
```json
{
    "success": true,
    "data": {
        "customer": {
            "id": 1,
            "company_name": "ABC Logistics Pvt Ltd",
            "person_name": "Rajesh Sharma"
        },
        "summary": {
            "total_trips": 25,
            "completed_trips": 22,
            "total_revenue": 750000,
            "total_paid": 680000,
            "total_pending": 70000,
            "collection_rate": 90.67,
            "net_profit": 350000
        },
        "trips_by_state": {
            "completed": 22,
            "on_route": 2,
            "planned": 1
        },
        "payment_by_mode": {
            "cash": 200000,
            "upi": 180000,
            "bank_transfer": 300000
        },
        "monthly_revenue": [
            {"month": "2026-01", "revenue": 250000, "trips": 8},
            {"month": "2026-02", "revenue": 280000, "trips": 9},
            {"month": "2026-03", "revenue": 220000, "trips": 8}
        ]
    }
}
```

---

### Get Customer Pending Payments (Owner/GM Only)
```http
GET {{base_url}}/customers/:id/pending-payments
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Example |
|-----------|-------------|---------|
| `page` | Page number | `?page=1` |
| `per_page` | Items per page | `?per_page=20` |
| `start_date` | Start date (YYYY-MM-DD) | `?start_date=2026-01-01` |
| `end_date` | End date (YYYY-MM-DD) | `?end_date=2026-03-31` |

**Response:**
```json
{
    "success": true,
    "data": {
        "items": [
            {
                "trip_id": 15,
                "vehicle_registration": "MH12AB1234",
                "start_location": "Mumbai",
                "end_location": "Pune",
                "expected_trip_price": 25000,
                "paid_trip_price": 15000,
                "pending_amount": 10000,
                "payment_status": "partial",
                "trip_date": "2026-02-20"
            }
        ],
        "total_pending": 70000,
        "count": 3,
        "page": 1,
        "per_page": 20,
        "total_pages": 1,
        "has_more": false
    }
}
```

---

### Get Customer Payments (Owner/GM Only)
```http
GET {{base_url}}/customers/:id/payments
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Example |
|-----------|-------------|---------|
| `page` | Page number | `?page=1` |
| `per_page` | Items per page | `?per_page=20` |
| `start_date` | Start date (YYYY-MM-DD) | `?start_date=2026-01-01` |
| `end_date` | End date (YYYY-MM-DD) | `?end_date=2026-03-31` |
| `status` | Filter by payment status | `?status=received` |
| `mode` | Filter by payment mode | `?mode=upi` |

**Response:**
```json
{
    "success": true,
    "data": {
        "items": [
            {
                "id": 10,
                "trip_id": 15,
                "receipt_number": "RP-202602-001-00010",
                "amount": 15000,
                "tds_amount": 150,
                "discount_amount": 0,
                "net_amount": 14850,
                "payment_type": "advance",
                "payment_mode": "upi",
                "payment_status": "received",
                "payment_date": "2026-02-20T10:00:00Z",
                "transaction_id": "UPI987654321"
            }
        ],
        "count": 5,
        "page": 1,
        "per_page": 20,
        "total_pages": 1,
        "has_more": false
    }
}
```

---

### Get Customer Payment Summary (Owner/GM Only)
```http
GET {{base_url}}/customers/:id/payment-summary
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Example |
|-----------|-------------|---------|
| `start_date` | Start date (YYYY-MM-DD) | `?start_date=2026-01-01` |
| `end_date` | End date (YYYY-MM-DD) | `?end_date=2026-03-31` |

**Response:**
```json
{
    "success": true,
    "data": {
        "customer_id": 1,
        "customer_name": "ABC Logistics Pvt Ltd",
        "total_billed": 750000,
        "total_received": 680000,
        "total_tds": 6800,
        "total_discount": 0,
        "net_received": 673200,
        "total_pending": 70000,
        "collection_rate": 90.67,
        "payment_count": 25,
        "by_mode": {
            "cash": 200000,
            "upi": 180000,
            "bank_transfer": 300000
        },
        "by_type": {
            "advance": 300000,
            "partial": 200000,
            "final": 180000
        }
    }
}
```

---

### Get Customer Financial Report (Owner/GM Only)
```http
GET {{base_url}}/customers/:id/financial-report
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Options | Default |
|-----------|-------------|---------|---------|
| `start_date` | Start date (YYYY-MM-DD) | - | - |
| `end_date` | End date (YYYY-MM-DD) | - | - |
| `period` | Grouping period | `monthly`, `quarterly`, `yearly` | `monthly` |

**Response:**
```json
{
    "success": true,
    "data": {
        "customer": {
            "id": 1,
            "company_name": "ABC Logistics Pvt Ltd",
            "person_name": "Rajesh Sharma"
        },
        "date_range": {
            "start_date": "2026-01-01",
            "end_date": "2026-03-31"
        },
        "revenue_summary": {
            "total_expected": 750000,
            "total_received": 680000,
            "total_pending": 70000
        },
        "cost_summary": {
            "total_trip_costs": 320000,
            "fuel_costs": 180000,
            "toll_costs": 45000,
            "other_costs": 95000
        },
        "profit_loss": {
            "gross_profit": 360000,
            "profit_margin": 52.94,
            "collection_rate": 90.67,
            "is_profitable": true
        },
        "trip_summary": {
            "total_trips": 25,
            "completed": 22,
            "cancelled": 1,
            "in_progress": 2
        },
        "period_breakdown": [
            {
                "period": "2026-01",
                "revenue": 250000,
                "costs": 110000,
                "profit": 140000,
                "trips": 8,
                "received": 230000,
                "pending": 20000
            },
            {
                "period": "2026-02",
                "revenue": 280000,
                "costs": 120000,
                "profit": 160000,
                "trips": 9,
                "received": 250000,
                "pending": 30000
            },
            {
                "period": "2026-03",
                "revenue": 220000,
                "costs": 90000,
                "profit": 130000,
                "trips": 8,
                "received": 200000,
                "pending": 20000
            }
        ],
        "top_vehicles": [
            {
                "vehicle_id": 1,
                "registration_number": "MH12AB1234",
                "trips": 10,
                "revenue": 300000,
                "profit": 150000
            },
            {
                "vehicle_id": 3,
                "registration_number": "MH14CD5678",
                "trips": 8,
                "revenue": 250000,
                "profit": 120000
            }
        ]
    }
}
```

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

### Financial Access Denied (403)
```json
{
    "success": false,
    "message": "Access denied",
    "error": "Customer statistics are only available to Owner and General Manager"
}
```

---

*Last Updated: April 2026*

