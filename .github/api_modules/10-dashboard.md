# 10. Dashboard

Dashboard and overview endpoints.

## Role Permissions

| Feature | Owner | General Manager | Manager | Supervisor |
|---------|-------|-----------------|---------|------------|
| View Full Dashboard | ✅ | ✅ | ✅ | ✅ |
| View Cost Overview | ✅ | ✅ | ❌ | ❌ |
| View Pending Payments | ✅ | ✅ | ❌ | ❌ |
| View Team Stats | ✅ | ✅ | ❌ | ❌ |
| View Document Stats | ✅ | ✅ | ✅ | ❌ |

---

## Endpoints

### Get Unified Dashboard
```http
GET {{base_url}}/dashboard
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Dashboard retrieved successfully",
    "data": {
        "user_info": {
            "id": 1,
            "first_name": "John",
            "last_name": "Doe",
            "role": "owner",
            "email": "john@example.com"
        },
        "fleet_overview": {
            "total_vehicles": 50,
            "active_vehicles": 45,
            "maintenance_vehicles": 3,
            "inactive_vehicles": 2,
            "total_drivers": 40,
            "active_drivers": 35,
            "drivers_on_trip": 10,
            "drivers_on_leave": 5,
            "total_trips": 500,
            "ongoing_trips": 15,
            "planned_trips": 20,
            "completed_trips": 450
        },
        "today_summary": {
            "completed_trips_today": 5,
            "total_distance_today": 750.5,
            "total_fuel_filled": 200,
            "total_fuel_used": 180,
            "total_fuel_cost": 18000,
            "active_vehicles_now": 10,
            "new_trips_today": 8,
            "alerts_count": 3
        },
        "alerts": [
            {
                "id": "doc_exp_1",
                "type": "DOCUMENT_EXPIRY",
                "priority": "warning",
                "title": "Insurance Expiring",
                "message": "Vehicle MH12AB1234 insurance expires in 7 days",
                "entity_type": "vehicle",
                "entity_id": 1,
                "vehicle_registration_number": "MH12AB1234",
                "days_until_expiry": 7
            }
        ],
        "total_alerts": 3,
        "quick_actions": {
            "vehicles_count": 50,
            "drivers_count": 40,
            "trips_count": 15,
            "alerts_count": 3
        },
        "live_status": {
            "active_vehicles": 10,
            "idle_vehicles": 5,
            "offline_vehicles": 35
        },
        "team_stats": {
            "total_managers": 3,
            "total_supervisors": 5,
            "total_members": 8
        },
        "document_stats": {
            "total_documents": 150,
            "expiring_documents": 5,
            "expired_documents": 2
        },
        "last_updated": "2025-01-15T10:30:00Z"
    }
}
```

**Note:** `team_stats` is only returned for Owner and General Manager. `document_stats` is returned for Owner, General Manager, and Manager.

---

### Get Cost Overview
```http
GET {{base_url}}/dashboard/cost-overview
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Options |
|-----------|-------------|---------|
| filter | Time filter | `today`, `weekly`, `monthly` |

**Response:**
```json
{
    "success": true,
    "data": {
        "filter": "monthly",
        "total_trip_cost": 150000.00,
        "total_maintenance_cost": 50000.00,
        "grand_total": 200000.00,
        "trip_cost_breakdown": [
            {"cost_type": "fuel", "amount": 80000, "count": 50},
            {"cost_type": "toll", "amount": 25000, "count": 100},
            {"cost_type": "driver_allowance", "amount": 30000, "count": 40}
        ],
        "maintenance_cost_breakdown": [
            {"cost_type": "service", "amount": 30000, "count": 10},
            {"cost_type": "tyre", "amount": 15000, "count": 5}
        ],
        "completed_trips": 45,
        "total_revenue": 500000.00,
        "fuel_expenses": 80000.00,
        "toll_expenses": 25000.00,
        "driver_allowance_expenses": 30000.00
    }
}
```

**Access:** Owner and General Manager only

---

### Get Pending Payments
```http
GET {{base_url}}/dashboard/pending-payments
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description |
|-----------|-------------|
| page | Page number |
| per_page | Items per page |

**Response:**
```json
{
    "success": true,
    "data": {
        "payments": [
            {
                "trip_id": 1,
                "vehicle_id": 1,
                "vehicle_registration": "MH12AB1234",
                "customer_name": "ABC Corp",
                "customer_contact": "+919876543210",
                "trip_price": 15000,
                "partial_payment_amount": 5000,
                "pending_amount": 10000,
                "payment_status": "partial",
                "scheduled_date": "2025-01-10"
            }
        ],
        "total_pending": 50000.00,
        "count": 5,
        "page": 1,
        "per_page": 20
    }
}
```

**Access:** Owner and General Manager only

---

### Get Alerts Status
```http
GET {{base_url}}/dashboard/alerts-status
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "document_expiring_count": 5,
        "document_expired_count": 2,
        "license_expiring_count": 3,
        "license_expired_count": 1,
        "maintenance_due_count": 4,
        "total_alerts": 15,
        "critical_alerts": 3,
        "warning_alerts": 7,
        "info_alerts": 5
    }
}
```

---

### Get Fleet Stats
```http
GET {{base_url}}/dashboard/fleet-stats
Authorization: Bearer {{token}}
```

Returns detailed fleet statistics.

---

### Get Fleet Summary
```http
GET {{base_url}}/dashboard/summary
Authorization: Bearer {{token}}
```

Returns quick summary for mobile app home screen.

---

## Error Responses

### Access Denied (Financial APIs)
```json
{
    "success": false,
    "message": "Access denied",
    "error": "Cost overview is only available to Owner and General Manager"
}
```

### Invalid Filter
```json
{
    "success": false,
    "message": "Invalid filter",
    "error": "filter must be one of: today, weekly, monthly"
}
```

