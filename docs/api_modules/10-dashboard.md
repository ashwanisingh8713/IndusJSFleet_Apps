# 10. Dashboard

Dashboard and overview endpoints.

## Role Permissions

| Feature | Owner | General Manager | Manager | Supervisor |
|---------|-------|-----------------|---------|------------|
| View Full Dashboard | ✅ | ✅ | ✅ | ✅ |
| View Financial Summary | ✅ | ✅ | ❌ | ❌ |
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
        "user_info": {...},
        "fleet_overview": {...},
        "today_summary": {...},
        "alerts": [...],
        "total_alerts": 3,
        "quick_actions": {...},
        "live_status": {...},
        "team_stats": {...},
        "document_stats": {...},
        "last_updated": "2026-01-15T10:30:00Z"
    }
}
```

---

### Get Financial Summary (Owner/GM Only)
```http
GET {{base_url}}/dashboard/financial-summary?period=monthly
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Options |
|-----------|-------------|---------|
| period | Time period | `today`, `weekly`, `monthly`, `yearly` |

**Response:**
```json
{
    "success": true,
    "data": {
        "period": "monthly",
        "start_date": "2025-12-11",
        "end_date": "2026-01-11",
        "total_revenue": 500000.00,
        "total_expenses": 200000.00,
        "trip_costs": 150000.00,
        "maintenance_costs": 50000.00,
        "net_profit": 300000.00,
        "profit_margin": 60.0,
        "profit_status": "profit",
        "pending_payments": 50000.00,
        "received_payments": 450000.00,
        "completed_trips": 45,
        "total_trips": 50,
        "avg_trip_revenue": 11111.11,
        "avg_trip_cost": 4444.44,
        "avg_trip_profit": 6666.67
    }
}
```

**Notes:**
- Revenue is calculated from `trip_price` of completed trips
- Expenses include both trip costs and maintenance costs
- `profit_status`: `profit`, `loss`, or `break_even`

---

### Get Cost Overview (Owner/GM Only)
```http
GET {{base_url}}/dashboard/cost-overview?filter=monthly
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
        "period": "monthly",
        "total_expenses": 200000.00,
        "total_revenue": 500000.00,
        "total_profit": 300000.00,
        "total_loss": 0,
        "net_profit_loss": 300000.00,
        "completed_trips": 45,
        "fuel_expenses": 80000.00,
        "toll_expenses": 25000.00,
        "maintenance_expenses": 50000.00,
        "other_expenses": 15000.00,
        "pending_payments": 50000.00,
        "received_payments": 450000.00,
        "trip_cost_breakdown": [
            {"cost_id": "TC-001-002", "cost_label": "Diesel", "group_id": "TC-G-001", "amount": 80000, "count": 50},
            {"cost_id": "TC-002-001", "cost_label": "Toll Charges", "group_id": "TC-G-002", "amount": 25000, "count": 100}
        ],
        "maintenance_cost_breakdown": [
            {"cost_id": "VMC-001-001", "cost_label": "Engine Oil Change", "group_id": "VMC-G-001", "amount": 30000, "count": 10},
            {"cost_id": "VMC-002-003", "cost_label": "Tyres Replacement", "group_id": "VMC-G-002", "amount": 15000, "count": 5}
        ],
        "driver_allowance_expenses": 30000.00,
        "parking_expenses": 5000.00,
        "loading_charges": 8000.00,
        "unloading_charges": 7000.00,
        "chalan_expenses": 2000.00,
        "permit_expenses": 3000.00
    }
}
```

---

### Get Pending Payments (Owner/GM Only)
```http
GET {{base_url}}/dashboard/pending-payments?page=1&per_page=20
Authorization: Bearer {{token}}
```

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
                "scheduled_date": "2026-01-10"
            }
        ],
        "total_pending": 50000.00,
        "count": 5,
        "page": 1,
        "per_page": 20
    }
}
```

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

## Revenue Calculation

All financial APIs use `trip_price` as the revenue source:

```
Revenue = SUM(trip_price) for completed trips
Expenses = SUM(trip_costs) + SUM(maintenance_costs)
Net Profit = Revenue - Expenses
Profit Margin = (Net Profit / Revenue) * 100
```

---

## Error Responses

### Access Denied (Financial APIs)
```json
{
    "success": false,
    "message": "Access denied",
    "error": "Financial summary is only available to Owner and General Manager"
}
```

