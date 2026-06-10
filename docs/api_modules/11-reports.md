# 11. Reports & Profit/Loss

Financial reports and profit/loss analysis.

## Revenue Calculation (IMPORTANT)

All P&L APIs use `trip_price` as the revenue source:

```
Revenue = SUM(trip_price) for completed trips
Expenses = SUM(trip_costs) + SUM(maintenance_costs)
Net Profit = Revenue - Expenses
Profit Margin = (Net Profit / Revenue) * 100
```

## Role Permissions

| Feature | Owner | General Manager | Manager | Supervisor |
|---------|-------|-----------------|---------|------------|
| View P&L Reports | ✅ | ✅ | ❌ | ❌ |
| Trip P&L | ✅ | ✅ | ❌ | ❌ |
| Vehicle P&L | ✅ | ✅ | ❌ | ❌ |
| Cost Type Analysis | ✅ | ✅ | ❌ | ❌ |
| Consolidated P&L | ✅ | ✅ | ❌ | ❌ |

---

## Endpoints

### Get Report Types
```http
GET {{base_url}}/reports/types
Authorization: Bearer {{token}}
```

### Get Report Periods
```http
GET {{base_url}}/reports/periods
Authorization: Bearer {{token}}
```

---

## Fleet P&L

### Get Fleet Profit/Loss
```http
GET {{base_url}}/reports/profit-loss?period=monthly
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Options |
|-----------|-------------|---------|
| period | Time period | `today`, `weekly`, `monthly`, `yearly` |
| start_date | Custom start | `01-01-2026` |
| end_date | Custom end | `31-01-2026` |

**Response:**
```json
{
    "success": true,
    "data": {
        "period": {
            "start_date": "2025-12-11",
            "end_date": "2026-01-11"
        },
        "vehicles": [...],
        "summary": {
            "total_vehicles": 10,
            "total_trips": 45,
            "total_distance": 6750.5,
            "total_revenue": 500000.00,
            "total_cost": 200000.00,
            "total_profit": 300000.00,
            "profit_margin": 60.0,
            "profitable_vehicles": 8,
            "loss_making_vehicles": 2
        }
    }
}
```

---

## Trip P&L

### Get Trip Profit/Loss
```http
GET {{base_url}}/trips/:id/profit-loss
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "trip_id": 1,
        "vehicle_id": 1,
        "vehicle_registration": "MH12AB1234",
        "driver_id": 1,
        "driver_name": "Rajesh Kumar",
        "trip_date": "2026-01-15",
        "start_location": "Mumbai",
        "end_location": "Pune",
        "distance": 150.5,
        "trip_price": 15000.00,
        "total_cost": 8500.00,
        "net_profit": 6500.00,
        "profit_margin": 43.33,
        "status": "profit",
        "payment_status": "paid",
        "pending_amount": 0,
        "cost_breakdown": [
            {"cost_id": "TC-001-002", "cost_label": "Diesel", "group_id": "TC-G-001", "amount": 5000.00, "count": 2},
            {"cost_id": "TC-002-001", "cost_label": "Toll Charges", "group_id": "TC-G-002", "amount": 350.00, "count": 3}
        ]
    }
}
```

---

### Get Multi-Trip Profit/Loss
```http
POST {{base_url}}/reports/profit-loss/trips
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "trip_ids": [1, 2, 3, 4, 5],
    "start_date": "01-01-2026",
    "end_date": "31-01-2026"
}
```

**Note:** Either `trip_ids` or date range is required.

---

## Vehicle P&L

### Get Vehicle Profit/Loss
```http
GET {{base_url}}/vehicles/:id/profit-loss?period=monthly
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "vehicle_id": 1,
        "vehicle_registration": "MH12AB1234",
        "period": {
            "start_date": "2025-12-11",
            "end_date": "2026-01-11"
        },
        "total_trips": 10,
        "total_distance": 1500.5,
        "total_revenue": 150000.00,
        "total_cost": 85000.00,
        "fuel_cost": 50000.00,
        "maintenance_cost": 15000.00,
        "other_cost": 20000.00,
        "net_profit": 65000.00,
        "profit_margin": 43.33,
        "profit_status": "profit",
        "avg_profit_per_trip": 6500.00,
        "avg_profit_per_km": 43.30,
        "cost_breakdown": [...]
    }
}
```

---

### Get Multi-Vehicle Profit/Loss
```http
POST {{base_url}}/reports/profit-loss/vehicles
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "vehicle_ids": [1, 2, 3],
    "start_date": "01-01-2026",
    "end_date": "31-01-2026"
}
```

---

## Cost Type Analysis

### Get Cost Type Profit/Loss
```http
GET {{base_url}}/reports/profit-loss/cost-type/TC-001-002
Authorization: Bearer {{token}}
```

**Note:** Use cost_id (e.g., TC-001-002 for Diesel, VMC-002-003 for Tyres)

### Get Multi Cost Type Analysis
```http
POST {{base_url}}/reports/profit-loss/cost-types
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "cost_ids": ["TC-001-002", "TC-002-001", "VMC-001-001"],
    "vehicle_ids": [1, 2, 3],
    "start_date": "01-01-2026",
    "end_date": "31-01-2026"
}
```

---

## Consolidated P&L

### Get Consolidated Profit/Loss
```http
POST {{base_url}}/reports/profit-loss/consolidated
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "vehicle_ids": [1, 2, 3],
    "trip_ids": [1, 2, 3, 4, 5],
    "cost_ids": ["TC-001-002", "TC-002-001", "TC-004-001"],
    "start_date": "01-01-2026",
    "end_date": "31-01-2026"
}
```

---

## Error Responses

### Access Denied
```json
{
    "success": false,
    "message": "Access denied",
    "error": "Profit/Loss reports are only available to Owner and General Manager"
}
```


