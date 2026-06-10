# 12. Driver Costs API

Driver cost management for salaries, incentives, deductions, and other driver-related expenses.

## Access Control

| Role | Access |
|------|--------|
| Owner | Full access |
| General Manager | Full access |
| Manager | No access |
| Supervisor | No access |

---

## Cost Structure

All driver costs require structured IDs:

| Field | Description | Example |
|-------|-------------|---------|
| `cost_id` | Unique cost identifier | DC-001-001 |
| `cost_label` | Human-readable label | Monthly Salary |
| `group_id` | Group identifier | DC-G-001 |

---

## Cost Groups

### DC-G-001: Salary & Wages
| Cost ID | Label |
|---------|-------|
| DC-001-001 | Monthly Salary |
| DC-001-002 | Daily Wages |
| DC-001-003 | Overtime Pay |
| DC-001-004 | Holiday Pay |

### DC-G-002: Incentives & Bonuses
| Cost ID | Label |
|---------|-------|
| DC-002-001 | Trip Bonus |
| DC-002-002 | Performance Bonus |
| DC-002-003 | Fuel Savings Bonus |
| DC-002-004 | On-Time Delivery Bonus |
| DC-002-005 | Safety Bonus |

### DC-G-003: Deductions
| Cost ID | Label |
|---------|-------|
| DC-003-001 | Advance Recovery |
| DC-003-002 | Damage Deduction |
| DC-003-003 | Fine Deduction |
| DC-003-004 | Loan EMI |
| DC-003-005 | Insurance Premium |

### DC-G-004: Other
| Cost ID | Label |
|---------|-------|
| DC-004-001 | Training Cost |
| DC-004-002 | Uniform |
| DC-004-003 | Medical Expense |
| DC-004-004 | License Renewal |
| DC-004-005 | Other |

---

## Endpoints

### Get Driver Cost Types

```http
GET {{base_url}}/cost-types/driver
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "category_id": "DC-001",
        "category_name": "Driver Costs",
        "groups": [
            {
                "group_id": "DC-G-001",
                "group_name": "Salary & Wages",
                "items": [
                    {"id": "DC-001-001", "value": "DC-001-001", "label": "Monthly Salary"},
                    {"id": "DC-001-002", "value": "DC-001-002", "label": "Daily Wages"}
                ]
            }
        ]
    }
}
```

---

### Create Driver Cost

```http
POST {{base_url}}/drivers/:id/costs
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body (Salary):**
```json
{
    "driver_id": 1,
    "cost_id": "DC-001-001",
    "cost_label": "Monthly Salary",
    "group_id": "DC-G-001",
    "amount": 25000.00,
    "date": "31-12-2025",
    "month": "2025-12",
    "description": "December 2025 Salary",
    "notes": "Paid via bank transfer"
}
```

**Request Body (Trip Bonus - linked to specific trip):**
```json
{
    "driver_id": 1,
    "trip_id": 1,
    "cost_id": "DC-002-001",
    "cost_label": "Trip Bonus",
    "group_id": "DC-G-002",
    "amount": 500.00,
    "date": "15-12-2025",
    "description": "Bonus for long-haul trip"
}
```

**Request Body (Deduction):**
```json
{
    "driver_id": 1,
    "cost_id": "DC-003-001",
    "cost_label": "Advance Recovery",
    "group_id": "DC-G-003",
    "amount": 2000.00,
    "date": "31-12-2025",
    "month": "2025-12",
    "is_deduction": true
}
```

**Response:**
```json
{
    "success": true,
    "message": "Driver cost created successfully",
    "data": {
        "id": 1,
        "driver_id": 1,
        "cost_id": "DC-001-001",
        "cost_label": "Monthly Salary",
        "group_id": "DC-G-001",
        "amount": 25000.00,
        "date": "2025-12-31T00:00:00Z",
        "month": "2025-12",
        "is_deduction": false,
        "created_by_user": {
            "id": 1,
            "first_name": "Owner",
            "last_name": "User",
            "role": "owner"
        }
    }
}
```

---

### Get Driver Costs

```http
GET {{base_url}}/drivers/:id/costs
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Param | Type | Description |
|-------|------|-------------|
| page | int | Page number (default: 1) |
| per_page | int | Items per page (default: 20) |
| group_id | string | Filter by group (DC-G-001 to DC-G-004) |
| cost_id | string | Filter by specific cost type |
| month | string | Filter by month (YYYY-MM) |
| start_date | string | Filter from date (DD-MM-YYYY) |
| end_date | string | Filter to date (DD-MM-YYYY) |

**Response:**
```json
{
    "success": true,
    "data": {
        "costs": [
            {
                "id": 1,
                "driver_id": 1,
                "cost_id": "DC-001-001",
                "cost_label": "Monthly Salary",
                "group_id": "DC-G-001",
                "amount": 25000.00,
                "date": "2025-12-31T00:00:00Z",
                "month": "2025-12"
            }
        ],
        "summary": {
            "total_salary": 25000.00,
            "total_incentives": 5000.00,
            "total_deductions": 2000.00,
            "total_other": 500.00,
            "net_earnings": 28500.00
        }
    },
    "pagination": {
        "page": 1,
        "per_page": 20,
        "total": 5,
        "total_pages": 1
    }
}
```

---

### Get Driver Financial Summary

```http
GET {{base_url}}/drivers/:id/financial-summary
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Param | Type | Options |
|-------|------|---------|
| period | string | today, weekly, monthly, quarterly, yearly, custom |
| start_date | string | DD-MM-YYYY (for custom) |
| end_date | string | DD-MM-YYYY (for custom) |

**Response:**
```json
{
    "success": true,
    "data": {
        "driver": {
            "id": 1,
            "name": "Rajesh Kumar",
            "status": "active"
        },
        "period": {
            "type": "monthly",
            "start_date": "2025-12-12",
            "end_date": "2026-01-12"
        },
        "trips": {
            "total": 25,
            "completed": 24,
            "cancelled": 1,
            "total_distance": 3750.5
        },
        "revenue_generated": {
            "total": 250000.00,
            "avg_per_trip": 10416.67
        },
        "earnings": {
            "salary": 25000.00,
            "incentives": 10000.00,
            "deductions": 2000.00,
            "other_costs": 500.00,
            "net": 33500.00,
            "breakdown": [
                {"cost_id": "DC-001-001", "cost_label": "Monthly Salary", "group_id": "DC-G-001", "amount": 25000, "count": 1},
                {"cost_id": "DC-002-001", "cost_label": "Trip Bonus", "group_id": "DC-G-002", "amount": 10000, "count": 5}
            ]
        },
        "trip_costs_attributed": {
            "fuel": 45000.00,
            "toll": 12000.00,
            "loading": 5000.00,
            "other": 3000.00,
            "total": 65000.00
        },
        "efficiency": {
            "fuel_efficiency": 4.35,
            "avg_trip_time_hours": 4.2,
            "on_time_rate": 96.0,
            "trip_cost_per_km": 17.33
        },
        "ranking": {
            "efficiency": 2,
            "revenue_generated": 3,
            "total_drivers": 8
        }
    }
}
```

---

### Get Driver Earnings

```http
GET {{base_url}}/drivers/:id/earnings
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Param | Type | Options |
|-------|------|---------|
| period | string | today, weekly, monthly, quarterly, yearly |

**Response:**
```json
{
    "success": true,
    "data": {
        "driver": {
            "id": 1,
            "name": "Rajesh Kumar"
        },
        "period": {
            "type": "monthly",
            "start_date": "2025-12-12",
            "end_date": "2026-01-12"
        },
        "totals": {
            "salary": 25000.00,
            "incentives": 10000.00,
            "deductions": 2000.00,
            "other": 500.00,
            "net_earnings": 33500.00
        },
        "monthly_breakdown": [
            {
                "month": "2025-12",
                "salary": 25000.00,
                "incentives": 10000.00,
                "deductions": 2000.00,
                "other": 500.00,
                "net": 33500.00
            }
        ],
        "trip_count": 24
    }
}
```

---

### Bulk Create Driver Costs

```http
POST {{base_url}}/drivers/:id/costs/bulk
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "costs": [
        {
            "cost_id": "DC-001-001",
            "cost_label": "Monthly Salary",
            "group_id": "DC-G-001",
            "amount": 25000.00,
            "date": "31-12-2025",
            "month": "2025-12"
        },
        {
            "cost_id": "DC-002-002",
            "cost_label": "Performance Bonus",
            "group_id": "DC-G-002",
            "amount": 5000.00,
            "date": "31-12-2025"
        }
    ]
}
```

**Response:**
```json
{
    "success": true,
    "message": "Bulk driver costs processed",
    "data": {
        "created_count": 2,
        "error_count": 0,
        "costs": [...]
    }
}
```

---

### Update Driver Cost

```http
PUT {{base_url}}/drivers/:id/costs/:costId
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "amount": 27000.00,
    "notes": "Updated salary amount"
}
```

---

### Delete Driver Cost

```http
DELETE {{base_url}}/drivers/:id/costs/:costId
Authorization: Bearer {{token}}
```

---

## Notes

1. **Trip-Specific Costs:** For bonuses tied to specific trips (like trip bonus), include `trip_id` in the request body.

2. **Deductions:** Costs in group DC-G-003 are automatically marked as deductions. You can also explicitly set `is_deduction: true`.

3. **Monthly Costs:** For monthly salaries and recurring costs, include the `month` field in YYYY-MM format for easy filtering and reporting.

4. **Net Earnings Calculation:**
   ```
   Net Earnings = Salary + Incentives - Deductions + Other
   ```

5. **Financial Summary:** The `/financial-summary` endpoint provides a complete overview including trips completed, revenue generated, earnings breakdown, trip costs attributed to the driver, and efficiency metrics.

