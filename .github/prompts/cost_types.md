# Cost Types Reference

This document defines all cost types used in the Fleet Management System.

## Cost Structure

All costs use a structured ID system:
- `cost_id`: Unique identifier (e.g., TC-001-002)
- `cost_label`: Human-readable label (e.g., Diesel)
- `group_id`: Group identifier (e.g., TC-G-001)

---

## Trip Costs

### TC-G-001: Fuel & Energy
| Cost ID | Label | Extra Fields |
|---------|-------|--------------|
| TC-001-001 | Petrol | fuel_quantity, fuel_rate, km_per_liter |
| TC-001-002 | Diesel | fuel_quantity, fuel_rate, km_per_liter |
| TC-001-003 | CNG / LPG | fuel_quantity, fuel_rate, km_per_liter |
| TC-001-004 | EV Charging | fuel_quantity, fuel_rate, km_per_liter |

### TC-G-002: Toll & Parking
| Cost ID | Label |
|---------|-------|
| TC-002-001 | Toll Charges |
| TC-002-002 | Parking Fees |
| TC-002-003 | Entry Charges |

### TC-G-003: Loading & Unloading
| Cost ID | Label |
|---------|-------|
| TC-003-001 | Loading Charges |
| TC-003-002 | Unloading Charges |
| TC-003-003 | Crane / Forklift |
| TC-003-004 | Labor Charges |

### TC-G-004: Driver Expenses
| Cost ID | Label |
|---------|-------|
| TC-004-001 | Driver Allowance |
| TC-004-002 | Food & Refreshments |
| TC-004-003 | Accommodation |

### TC-G-005: Permits & Compliance
| Cost ID | Label |
|---------|-------|
| TC-005-001 | State Permit |
| TC-005-002 | National Permit |
| TC-005-003 | Special Permit |
| TC-005-004 | Chalan / Fine |

### TC-G-006: Miscellaneous
| Cost ID | Label | Notes |
|---------|-------|-------|
| TC-006-001 | Police |  |
| TC-006-002 | RTO |  |
| TC-006-003 | Weighbridge |  |
| TC-006-004 | Commission / Agent |  |
| TC-006-005 | Other | Requires `custom_cost_label` |

---

## Vehicle Maintenance Costs

### VMC-G-001: Regular Maintenance
| Cost ID | Label |
|---------|-------|
| VMC-001-001 | Engine Oil Change |
| VMC-001-002 | Oil Filter Replacement |
| VMC-001-003 | Air Filter Replacement |
| VMC-001-004 | Wheel Alignment & Balancing |
| VMC-001-005 | General Servicing Labor |

### VMC-G-002: Repairs & Replacements
| Cost ID | Label |
|---------|-------|
| VMC-002-001 | Brake Pads / Discs |
| VMC-002-002 | Battery Replacement |
| VMC-002-003 | Tyres Replacement |
| VMC-002-004 | Clutch Repair |
| VMC-002-005 | Suspension Repair |

### VMC-G-003: Electrical & AC
| Cost ID | Label |
|---------|-------|
| VMC-003-001 | AC Service / Repair |
| VMC-003-002 | Electrical Wiring |
| VMC-003-003 | Alternator / Starter Motor |
| VMC-003-004 | Lights & Indicators |

### VMC-G-004: Body & Exterior
| Cost ID | Label |
|---------|-------|
| VMC-004-001 | Denting & Painting |
| VMC-004-002 | Windshield / Glass |
| VMC-004-003 | Body Parts |
| VMC-004-004 | Washing & Cleaning |

### VMC-G-005: Engine & Transmission
| Cost ID | Label |
|---------|-------|
| VMC-005-001 | Engine Overhaul |
| VMC-005-002 | Gearbox Repair |
| VMC-005-003 | Radiator / Cooling System |
| VMC-005-004 | Fuel System |

### VMC-G-006: Miscellaneous
| Cost ID | Label | Notes |
|---------|-------|-------|
| VMC-006-001 | Accessories |  |
| VMC-006-002 | GPS / Tracking Device |  |
| VMC-006-003 | Other | Requires `custom_cost_label` |

### VMC-G-007: Wheels & Tires
| Cost ID | Label |
|---------|-------|
| VMC-007-001 | Tire Rotation |
| VMC-007-002 | Wheel Alignment |
| VMC-007-003 | Tire Replacement |

---

## API Endpoints

### Get Trip Cost Types
```http
GET /api/v2/trip-costs/types
```

### Get Maintenance Cost Types
```http
GET /api/v2/maintenance-costs/types
```

---

## Request Examples

### Create Fuel Cost (Diesel)
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
    "fuel_quantity": 55.5,
    "fuel_rate": 90.09,
    "km_per_liter": 4.5
}
```

### Create Maintenance Cost (Tyres)
```json
{
    "vehicle_id": 1,
    "cost_id": "VMC-002-003",
    "cost_label": "Tyres Replacement",
    "group_id": "VMC-G-002",
    "amount": 25000.00,
    "date": "20-12-2025",
    "vendor_name": "Sharma Tyre Works"
}
```

### Create "Other" Cost with Custom Label
```json
{
    "cost_id": "TC-006-005",
    "cost_label": "Other",
    "group_id": "TC-G-006",
    "custom_cost_label": "Driver Meals",
    "amount": 200.00
}
```

---

## Validation Rules

1. `cost_id` must exist in the system
2. `group_id` must match the cost_id's group
3. For Fuel group (TC-G-001): `fuel_quantity`, `fuel_rate`, `km_per_liter` are optional but recommended
4. For "Other" types (TC-006-005, VMC-006-003): `custom_cost_label` should be provided

