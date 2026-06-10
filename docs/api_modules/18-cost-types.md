# 18. Cost Types (Reference Data)

Cost type reference data endpoints for retrieving structured cost IDs used across trip costs, maintenance costs, and driver costs.

## Overview

All costs in the system use a structured ID system with `cost_id`, `cost_label`, and `group_id`. These endpoints return the full catalog of available cost types, organized by groups. Use these to populate cost-type selection dropdowns in create/edit cost screens.

## Role Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| View Cost Types | ✅ | ✅ | ✅ | ✅ |

---

## Response Schema

All three endpoints return data in the same `CostTypesResponse` structure:

```json
{
    "success": true,
    "data": {
        "category_id": "TC-001",
        "category_name": "Trip Costs",
        "groups": [
            {
                "group_id": "TC-G-001",
                "group_name": "Fuel & Energy",
                "items": [
                    {
                        "id": "TC-001-001",
                        "value": "TC-001-001",
                        "label": "Petrol"
                    }
                ]
            }
        ]
    }
}
```

| Field | Description |
|-------|-------------|
| `category_id` | Top-level category identifier |
| `category_name` | Human-readable category name |
| `groups[].group_id` | Group identifier (use as `group_id` in cost requests) |
| `groups[].group_name` | Human-readable group name |
| `groups[].items[].id` | Cost identifier (use as `cost_id` in cost requests) |
| `groups[].items[].value` | Same as `id` (for form binding) |
| `groups[].items[].label` | Human-readable label (use as `cost_label` in cost requests) |

---

## Endpoints

### Get Trip Cost Types

```http
GET {{base_url}}/cost-types/trip
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
            },
            {
                "group_id": "TC-G-003",
                "group_name": "Loading & Unloading",
                "items": [
                    {"id": "TC-003-001", "value": "TC-003-001", "label": "Loading Charges"},
                    {"id": "TC-003-002", "value": "TC-003-002", "label": "Unloading Charges"},
                    {"id": "TC-003-003", "value": "TC-003-003", "label": "Crane / Forklift"},
                    {"id": "TC-003-004", "value": "TC-003-004", "label": "Labor Charges"}
                ]
            },
            {
                "group_id": "TC-G-004",
                "group_name": "Driver Expenses",
                "items": [
                    {"id": "TC-004-001", "value": "TC-004-001", "label": "Driver Allowance"},
                    {"id": "TC-004-002", "value": "TC-004-002", "label": "Food Expense"},
                    {"id": "TC-004-003", "value": "TC-004-003", "label": "Accommodation"}
                ]
            },
            {
                "group_id": "TC-G-005",
                "group_name": "Permits & Compliance",
                "items": [
                    {"id": "TC-005-001", "value": "TC-005-001", "label": "State Permit"},
                    {"id": "TC-005-002", "value": "TC-005-002", "label": "National Permit"},
                    {"id": "TC-005-003", "value": "TC-005-003", "label": "Special Permit"},
                    {"id": "TC-005-004", "value": "TC-005-004", "label": "Chalan / Fine"}
                ]
            },
            {
                "group_id": "TC-G-006",
                "group_name": "Miscellaneous",
                "items": [
                    {"id": "TC-006-001", "value": "TC-006-001", "label": "Police"},
                    {"id": "TC-006-002", "value": "TC-006-002", "label": "RTO"},
                    {"id": "TC-006-003", "value": "TC-006-003", "label": "Weighbridge"},
                    {"id": "TC-006-004", "value": "TC-006-004", "label": "Commission / Agent"},
                    {"id": "TC-006-005", "value": "TC-006-005", "label": "Other"}
                ]
            }
        ]
    }
}
```

**Special Fields for Fuel & Energy Group (TC-G-001):**

When creating a cost with `group_id: "TC-G-001"`, additional fields are available:

| Field | Type | Description |
|-------|------|-------------|
| `fuel_quantity` | number | Liters or kWh filled |
| `fuel_rate` | number | Rate per unit (₹/liter) |
| `km_per_liter` | number | Fuel efficiency |

**Special Fields for Other (TC-006-005):**

| Field | Type | Description |
|-------|------|-------------|
| `custom_cost_label` | string | Custom description for "Other" expenses |

---

### Get Maintenance Cost Types

```http
GET {{base_url}}/cost-types/maintenance
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
            },
            {
                "group_id": "VMC-G-003",
                "group_name": "Electrical & AC",
                "items": [
                    {"id": "VMC-003-001", "value": "VMC-003-001", "label": "AC Service / Repair"},
                    {"id": "VMC-003-002", "value": "VMC-003-002", "label": "Electrical Wiring"},
                    {"id": "VMC-003-003", "value": "VMC-003-003", "label": "Alternator / Starter Motor"},
                    {"id": "VMC-003-004", "value": "VMC-003-004", "label": "Headlights / Tail Lights"}
                ]
            },
            {
                "group_id": "VMC-G-004",
                "group_name": "Body & Exterior",
                "items": [
                    {"id": "VMC-004-001", "value": "VMC-004-001", "label": "Denting & Painting"},
                    {"id": "VMC-004-002", "value": "VMC-004-002", "label": "Windshield / Glass"},
                    {"id": "VMC-004-003", "value": "VMC-004-003", "label": "Body Parts Replacement"},
                    {"id": "VMC-004-004", "value": "VMC-004-004", "label": "Washing & Cleaning"}
                ]
            },
            {
                "group_id": "VMC-G-005",
                "group_name": "Engine & Transmission",
                "items": [
                    {"id": "VMC-005-001", "value": "VMC-005-001", "label": "Engine Overhaul"},
                    {"id": "VMC-005-002", "value": "VMC-005-002", "label": "Gearbox Repair"},
                    {"id": "VMC-005-003", "value": "VMC-005-003", "label": "Radiator Repair"},
                    {"id": "VMC-005-004", "value": "VMC-005-004", "label": "Fuel System Repair"}
                ]
            },
            {
                "group_id": "VMC-G-006",
                "group_name": "Miscellaneous",
                "items": [
                    {"id": "VMC-006-001", "value": "VMC-006-001", "label": "Accessories"},
                    {"id": "VMC-006-002", "value": "VMC-006-002", "label": "GPS / Tracking Device"},
                    {"id": "VMC-006-003", "value": "VMC-006-003", "label": "Other"}
                ]
            },
            {
                "group_id": "VMC-G-007",
                "group_name": "Wheels & Tires",
                "items": [
                    {"id": "VMC-007-001", "value": "VMC-007-001", "label": "Tire Rotation"},
                    {"id": "VMC-007-002", "value": "VMC-007-002", "label": "Wheel Alignment"},
                    {"id": "VMC-007-003", "value": "VMC-007-003", "label": "Tire Replacement"}
                ]
            }
        ]
    }
}
```

**Special Fields for Other (VMC-006-003):**

| Field | Type | Description |
|-------|------|-------------|
| `custom_cost_label` | string | Custom description for "Other" expenses |

---

### Get Driver Cost Types

```http
GET {{base_url}}/cost-types/driver
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Driver cost types retrieved successfully",
    "data": {
        "category_id": "DC-001",
        "category_name": "Driver Costs",
        "groups": [
            {
                "group_id": "DC-G-001",
                "group_name": "Salary & Wages",
                "items": [
                    {"id": "DC-001-001", "value": "DC-001-001", "label": "Monthly Salary"},
                    {"id": "DC-001-002", "value": "DC-001-002", "label": "Daily Wages"},
                    {"id": "DC-001-003", "value": "DC-001-003", "label": "Overtime Pay"},
                    {"id": "DC-001-004", "value": "DC-001-004", "label": "Holiday Pay"}
                ]
            },
            {
                "group_id": "DC-G-002",
                "group_name": "Incentives & Bonuses",
                "items": [
                    {"id": "DC-002-001", "value": "DC-002-001", "label": "Trip Bonus"},
                    {"id": "DC-002-002", "value": "DC-002-002", "label": "Performance Bonus"},
                    {"id": "DC-002-003", "value": "DC-002-003", "label": "Fuel Savings Bonus"},
                    {"id": "DC-002-004", "value": "DC-002-004", "label": "On-Time Delivery Bonus"},
                    {"id": "DC-002-005", "value": "DC-002-005", "label": "Safety Bonus"}
                ]
            },
            {
                "group_id": "DC-G-003",
                "group_name": "Deductions",
                "items": [
                    {"id": "DC-003-001", "value": "DC-003-001", "label": "Advance Recovery"},
                    {"id": "DC-003-002", "value": "DC-003-002", "label": "Damage Deduction"},
                    {"id": "DC-003-003", "value": "DC-003-003", "label": "Fine Deduction"},
                    {"id": "DC-003-004", "value": "DC-003-004", "label": "Loan EMI"},
                    {"id": "DC-003-005", "value": "DC-003-005", "label": "Insurance Premium"}
                ]
            },
            {
                "group_id": "DC-G-004",
                "group_name": "Other",
                "items": [
                    {"id": "DC-004-001", "value": "DC-004-001", "label": "Training Cost"},
                    {"id": "DC-004-002", "value": "DC-004-002", "label": "Uniform"},
                    {"id": "DC-004-003", "value": "DC-004-003", "label": "Medical Expense"},
                    {"id": "DC-004-004", "value": "DC-004-004", "label": "License Renewal"},
                    {"id": "DC-004-005", "value": "DC-004-005", "label": "Other"}
                ]
            }
        ]
    }
}
```

**Special Fields for Deductions Group (DC-G-003):**
- Costs in this group are automatically marked with `is_deduction: true`
- You can also explicitly set `is_deduction: true` on any cost entry

---

## Cost ID Naming Convention

| Prefix | Category | Example |
|--------|----------|---------|
| `TC-` | Trip Costs | `TC-001-002` (Diesel) |
| `VMC-` | Vehicle Maintenance Costs | `VMC-002-003` (Tyres) |
| `DC-` | Driver Costs | `DC-001-001` (Monthly Salary) |

**ID Structure:** `{PREFIX}-{GROUP_NUMBER}-{ITEM_NUMBER}`
**Group ID Structure:** `{PREFIX}-G-{GROUP_NUMBER}`

---

## Usage

When creating a cost entry (trip cost, maintenance cost, or driver cost), you must provide:

| Field | Source | Description |
|-------|--------|-------------|
| `cost_id` | `items[].id` | The unique cost type identifier |
| `cost_label` | `items[].label` | The human-readable label |
| `group_id` | `groups[].group_id` | The group this cost belongs to |

**Example — Creating a Diesel Trip Cost:**
```json
{
    "trip_id": 1,
    "vehicle_id": 1,
    "cost_id": "TC-001-002",
    "cost_label": "Diesel",
    "group_id": "TC-G-001",
    "amount": 5000.00,
    "date": "20-12-2025",
    "time": "10:30"
}
```

---

## Caching Recommendation

Cost types rarely change. It is recommended to:
1. Fetch cost types once on app startup or login
2. Cache them locally (in-memory or multiplatform-settings)
3. Refresh on pull-to-refresh or after a configurable interval

---

*Last Updated: April 2026*

