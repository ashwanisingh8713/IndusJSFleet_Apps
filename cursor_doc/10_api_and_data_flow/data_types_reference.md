# Data Types & Enums Reference

## Vehicle Types
```
truck, van, bus, car, motorcycle, trailer
```

## Vehicle States
```
inactive, active, on_route, maintenance, damaged, decommissioned
```

State transition rules:
- `inactive` ↔ `active` (bidirectional)
- `active` → `on_route` → `active` (after trip)
- `active` → `maintenance` ↔ `damaged`
- `damaged` → `decommissioned` (terminal from damaged)

## Driver States
```
inactive, active, on_route, on_leave, suspended, terminated
```

State transition rules:
- `inactive` ↔ `active` (bidirectional)
- `active` → `on_route` → `active` (after trip)
- `active` → `on_leave` → `active`
- `active` → `suspended` → `active`
- Any → `terminated` (terminal state)

## Trip States
```
planned, on_route, completed, cancelled, failed, delayed
```

State transition rules:
- `planned` → `on_route` → `completed`
- `planned` → `cancelled`
- `on_route` → `failed`
- `on_route` → `delayed`

## Cargo Types
```
Gitti, Balu, Bhakshi, Enta, Hazardous, Valuable, Others
```

## Trip Cost Types (3-level hierarchy)
```
Category → Group → Item

Trip Costs:
  Fuel & Energy → fuel, cng, electric_charge
  Toll & Permits → toll, permit, entry_tax
  Loading → loading_charges, unloading_charges
  Driver → driver_allowance, driver_food
  Parking → parking
  Documentation → chalan, weighbridge
  Insurance → insurance
  Miscellaneous → other
```

## Maintenance Cost Types
```
tyre, battery, servicing, engine_repair, body_repair,
electrical, ac_repair, other
```

## Driver Cost Types
```
Earnings:
  salary, advance, bonus, overtime, incentive

Deductions:
  penalty, damage_recovery, loan_recovery

Other:
  food_allowance, travel_allowance, other
```

**Deduction group ID:** `DC-G-003`

## Payment Status
```
received, pending, cancelled
```

## Payment Modes
```
cash, upi, bank_transfer, cheque, card
```

## User Roles (Hierarchical)
```
owner > general_manager > manager > supervisor > driver
```

**API format:** `owner`, `general_manager`, `manager`, `supervisor`, `driver`

## Report Periods
```
today, weekly, 15_days, monthly, quarterly, half_yearly, yearly, custom
```

## Date/Time Formats

| Context | Format | Example |
|---------|--------|---------|
| UI display (date) | DD-MM-YYYY | `14-03-2026` |
| UI display (time) | HH:MM (24hr) | `15:30` |
| Trip scheduling API | ISO 8601 | `2026-03-14T15:30:00Z` |
| Cost entry API | DD-MM-YYYY + HH:MM | `"date": "14-03-2026", "time": "15:30"` |
| Bulk cost API | ISO 8601 | `2026-03-14T15:30:00Z` |
| Document expiry API | DD-MM-YYYY | `"expiry_date": "31-12-2026"` |
| Driver license API | DD-MM-YYYY | `"license_expiry": "31-12-2026"` |

## Document Types (Vehicle)
```
RC (Registration Certificate), Insurance, Permit, Fitness Certificate,
Pollution Certificate, Tax Receipt, Other
```

## Alert Types
```
document_expiry, license_expiry, maintenance_due,
insurance_expiry, permit_expiry, fitness_expiry
```

## Finance / Purchase Types
```
cash, loan
```

## Loan Status
```
active, completed, defaulted
```
