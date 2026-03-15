# Cost Types Reference

> Use this prompt when working with Trip Costs, Maintenance Costs, or Driver Costs.

## Cost Structure

All costs use structured IDs: `{PREFIX}-{GROUP}-{ITEM}`

- `cost_id`: Unique ID (e.g., `TC-001-002`)
- `cost_label`: Human name (e.g., `Diesel`)
- `group_id`: Group (e.g., `TC-G-001`)

Cost types are **cached locally** at app startup via `AppInitializer` → `InitializeCostTypesUseCase`.

## Trip Costs (TC)

| Group | Group Name | Cost IDs | Labels |
|-------|------------|----------|--------|
| TC-G-001 | Fuel & Energy | TC-001-001..004 | Petrol, Diesel, CNG/LPG, EV Charging |
| TC-G-002 | Toll & Parking | TC-002-001..003 | Toll Charges, Parking Fees, Entry Charges |
| TC-G-003 | Loading & Unloading | TC-003-001..004 | Loading, Unloading, Crane/Forklift, Labor |
| TC-G-004 | Driver Expenses | TC-004-001..003 | Driver Allowance, Food & Refreshments, Accommodation |
| TC-G-005 | Permits & Compliance | TC-005-001..004 | State Permit, National Permit, RTO Challan, Weight Fine |
| TC-G-006 | Miscellaneous | TC-006-001..005 | Phone/Communication, Vehicle Cleaning, Miscellaneous, Tip/Bribe, Other |

**Fuel costs** have extra fields: `fuel_quantity`, `fuel_rate`, `km_per_liter`

## Maintenance Costs (VMC)

| Group | Group Name | Cost IDs |
|-------|------------|----------|
| VMC-G-001 | Regular Maintenance | Oil change, Filter replacement, Brake service, Coolant, Belt replacement |
| VMC-G-002 | Repairs & Replacements | Clutch, Suspension, Steering, Radiator, Exhaust |
| VMC-G-003 | Electrical & AC | Battery, Alternator, Starter motor, Wiring, AC repair |
| VMC-G-004 | Body & Exterior | Denting, Painting, Windshield, Mirror, Bumper |
| VMC-G-005 | Engine & Transmission | Engine overhaul, Gearbox, Turbo, Fuel injection, Head gasket |
| VMC-G-006 | Miscellaneous | Towing, Accessories, Cleaning, Documentation, Other |
| VMC-G-007 | Wheels & Tires | New tyre, Tyre repair, Wheel alignment, Wheel balancing, Tyre rotation |

## Driver Costs (DC)

| Group | Group Name | Cost IDs | Labels |
|-------|------------|----------|--------|
| DC-G-001 | Salary & Wages | DC-001-001..004 | Monthly Salary, Daily Wages, Overtime, Incentive |
| DC-G-002 | Incentives & Bonuses | DC-002-001..005 | Trip Bonus, Festival Bonus, Performance Bonus, Referral Bonus, Other |
| DC-G-003 | Deductions | DC-003-001..005 | Advance Recovery, Fine/Penalty, Damage Deduction, Loan EMI, Other |
| DC-G-004 | Other | DC-004-001..005 | Insurance, Medical, Training, Uniform, Other |

**Deductions** have `is_deduction = true` flag.

## API Endpoints

```
GET /cost-types/trip          → cached locally
GET /cost-types/maintenance   → cached locally
GET /cost-types/driver        → cached locally
```

## Date Format for Costs

**Always** send date as `DD-MM-YYYY` and time as `HH:MM`:
```json
{
    "cost_id": "TC-001-002",
    "amount": 5000.00,
    "date": "15-03-2026",
    "time": "14:30",
    "notes": "Diesel refill at HP pump"
}
```

**Never** send ISO 8601 for cost entries.

## UI Component

Use `CostTypeChipSelector` for cost type selection:
```kotlin
CostTypeChipSelector(
    costTypes = state.costTypes,
    selectedCostType = state.selectedCostType,
    onCostTypeSelected = { viewModel.sendIntent(Intent.SelectCostType(it)) }
)
```

## Cost Entry Screens

| Screen | Route | Vehicle? | Trip? | Driver? |
|--------|-------|----------|-------|---------|
| `TripCostEntryScreen` | `TripCostEntry(tripId?, vehicleId?)` | Auto-filled | Optional | No |
| `MaintenanceCostEntryScreen` | `MaintenanceCostEntry(vehicleId?)` | Optional | No | No |
| `DriverCostEntryScreen` | `DriverCostEntry(driverId?)` | No | No | Optional |

## P&L Revenue Formula

```
Revenue = SUM(trip_price) for completed trips
Trip Costs = SUM(trip_costs)
Maintenance Costs = SUM(vehicle_maintenance_costs)
Driver Costs = SUM(driver_costs)
Total Expenses = Trip Costs + Maintenance Costs + Driver Costs
Net Profit = Revenue - Total Expenses
Profit Margin = (Net Profit / Revenue) × 100
```

