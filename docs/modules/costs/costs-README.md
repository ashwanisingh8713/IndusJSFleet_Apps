# Costs Module

## Overview

The Costs module handles cost tracking and entry for trips (fuel, toll, etc.) and vehicle maintenance. Cost types are fetched from the server and cached locally for offline access.

---

## Features

- Trip cost entry and tracking
- Maintenance cost entry and tracking
- Driver cost tracking (Owner/GM only)
- Cost types from server with local caching
- Cost filtering by type and date range
- Cost summary and breakdown
- Refresh cost types from server

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| Trip Cost Entry | `FleetRoute.TripCostEntry` | Add cost to a trip |
| Maintenance Cost Entry | `FleetRoute.MaintenanceCostEntry` | Add vehicle maintenance cost |
| Driver Cost Entry | `FleetRoute.DriverCostEntry` | Add driver-related cost |

---

## Cost Categories

### 1. Trip Costs

Expenses incurred during a trip.

| Cost ID | Label | Group |
|---------|-------|-------|
| TC-001-001 | Petrol | Fuel |
| TC-001-002 | Diesel | Fuel |
| TC-001-003 | CNG | Fuel |
| TC-002-001 | Toll Charges | Toll |
| TC-002-002 | State Border Tax | Toll |
| TC-003-001 | Loading Charges | Loading/Unloading |
| TC-003-002 | Unloading Charges | Loading/Unloading |
| TC-004-001 | Driver Allowance | Driver |
| TC-004-002 | Helper Allowance | Driver |
| TC-005-001 | Parking Charges | Parking |
| TC-006-001 | Permit Charges | Permit |
| TC-006-002 | Chalan | Permit |
| TC-007-001 | Other Expenses | Other |

### 2. Maintenance Costs

Vehicle maintenance and repair expenses.

| Cost ID | Label | Group |
|---------|-------|-------|
| VMC-001-001 | Engine Oil Change | Regular Service |
| VMC-001-002 | Oil Filter Replacement | Regular Service |
| VMC-001-003 | Air Filter Replacement | Regular Service |
| VMC-002-001 | Front Tyre Replacement | Tyres |
| VMC-002-002 | Rear Tyre Replacement | Tyres |
| VMC-002-003 | Tyre Puncture Repair | Tyres |
| VMC-003-001 | Battery Replacement | Electrical |
| VMC-003-002 | Alternator Repair | Electrical |
| VMC-003-003 | Starter Motor Repair | Electrical |
| VMC-004-001 | Brake Pad Replacement | Brakes |
| VMC-004-002 | Brake Disc Replacement | Brakes |
| VMC-005-001 | AC Repair | AC |
| VMC-006-001 | Body Repair | Body |
| VMC-007-001 | Other Maintenance | Other |

### 3. Driver Costs

Driver-related expenses (Owner/GM only).

| Cost ID | Label | Group |
|---------|-------|-------|
| DC-001-001 | Salary | Salary |
| DC-001-002 | Bonus | Salary |
| DC-002-001 | Advance | Advance |
| DC-003-001 | Medical | Benefits |
| DC-003-002 | Insurance | Benefits |
| DC-004-001 | Other | Other |

---

## Cost Type Local Storage

### Database Table: cost_types

| Column | Type | Description |
|--------|------|-------------|
| id | String | Cost type ID |
| label | String | Display label |
| group_id | String | Group identifier |
| group_label | String | Group display name |
| category | String | trip / maintenance / driver |
| is_active | Boolean | Active status |
| updated_at | String | Last update timestamp |

### Sync Strategy

| Trigger | Action |
|---------|--------|
| First App Launch | Fetch all cost types |
| Cost Entry Screen | Load from local DB |
| Refresh Button | Fetch and update local DB |

---

## Trip Cost Entry Screen

### Required Fields

| Field | Description |
|-------|-------------|
| Trip | Pre-selected or select from list |
| Cost Type | Dropdown from local DB |
| Amount | Cost amount |
| Date | Cost date |
| Time | Cost time |

### Optional Fields

| Field | Description |
|-------|-------------|
| Notes | Additional remarks |

### Validations

| Rule | Description |
|------|-------------|
| Amount | Must be positive number |
| Date | Must be on or after trip start date |
| Cost Type | Must be selected |

---

## Maintenance Cost Entry Screen

### Required Fields

| Field | Description |
|-------|-------------|
| Vehicle | Pre-selected or select from list |
| Cost Type | Dropdown from local DB |
| Amount | Cost amount |
| Date | Maintenance date |
| Time | Time of expense |

### Optional Fields

| Field | Description |
|-------|-------------|
| Description | Work description |
| Vendor Name | Service provider |
| Invoice Number | Invoice reference |
| Notes | Additional remarks |

### Validations

| Rule | Description |
|------|-------------|
| Amount | Must be positive number |
| Date | Cannot be future date |
| Cost Type | Must be selected |

---

## Cost Filters

### In Vehicle Detail > Costs Tab

| Filter | Options |
|--------|---------|
| Date Range | From Date, To Date |
| Cost Type | All / Specific cost types from local DB |
| Sort By | Date (asc/desc) |

### Filter Bottom Sheet

- Multi-select cost types
- Date range picker using ijs-datetime-picker
- Apply/Reset buttons

---

## Cost Summary

### Trip Cost Summary

| Metric | Description |
|--------|-------------|
| Total Cost | Sum of all trip costs |
| Fuel Cost | Fuel expenses only |
| Toll Cost | Toll charges only |
| Loading/Unloading | Loading + Unloading |
| Other Costs | All other expenses |
| Cost Count | Number of cost entries |

### Maintenance Cost Summary

| Metric | Description |
|--------|-------------|
| Total Cost | Sum of all maintenance |
| By Category | Grouped by cost group |
| Count | Number of entries |

---

## Cost Breakdown

API returns cost breakdown with:

| Field | Description |
|-------|-------------|
| cost_id | Cost type ID |
| cost_label | Cost type label |
| group_id | Group ID |
| total_cost | Sum for this type |
| count | Number of entries |

---

## Role-Based Permissions

| Action | Owner | GM | Manager | Supervisor |
|--------|:-----:|:--:|:-------:|:----------:|
| View Trip Costs | ✅ | ✅ | ✅ | ✅ |
| Add Trip Cost | ✅ | ✅ | ✅ | ✅ |
| Edit Trip Cost | ✅ | ✅ | ✅ | Own only |
| Delete Trip Cost | ✅ | ✅ | ✅ | ❌ |
| View Maintenance Costs | ✅ | ✅ | ✅ | ✅ |
| Add Maintenance Cost | ✅ | ✅ | ✅ | ✅ |
| Delete Maintenance Cost | ✅ | ✅ | ✅ | ❌ |
| View Driver Costs | ✅ | ✅ | ❌ | ❌ |
| Add Driver Cost | ✅ | ✅ | ❌ | ❌ |

---

## API Endpoints

### Cost Types

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/cost-types/trip` | GET | Trip cost types |
| `/cost-types/maintenance` | GET | Maintenance cost types |
| `/cost-types/driver` | GET | Driver cost types |

### Trip Costs

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/trips/{id}/costs` | GET | Get trip costs |
| `/trips/{id}/costs` | POST | Add trip cost |
| `/trips/{id}/costs/{costId}` | PUT | Update trip cost |
| `/trips/{id}/costs/{costId}` | DELETE | Delete trip cost |

### Maintenance Costs

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/vehicles/{id}/maintenance-costs` | GET | Get maintenance costs |
| `/vehicles/{id}/maintenance-costs` | POST | Add maintenance cost |
| `/vehicles/{id}/maintenance-costs/{costId}` | PUT | Update cost |
| `/vehicles/{id}/maintenance-costs/{costId}` | DELETE | Delete cost |

### Vehicle Trip Costs

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/vehicles/{id}/trip-costs` | GET | All trip costs for vehicle |

---

## Date Handling

| Field | Display Format | API Format |
|-------|---------------|------------|
| Date | DD-MM-YYYY | ISO 8601 |
| Time | HH:MM | HH:MM |

Uses ijs-datetime-picker for date/time selection.

---

## Integration with Other Modules

### Vehicles Module

- Maintenance costs shown in Vehicle Detail
- Cost filter in Costs tab
- Cost summary in overview

### Trips Module

- Trip costs shown in Trip Detail
- Add cost from trip screen
- Cost summary in trip

### Reports Module

- Costs contribute to P&L calculations
- Cost breakdown in reports

---

## Related Modules

- [Vehicles](../vehicles/) - Maintenance costs
- [Trips](../trips/) - Trip costs
- [Reports](../reports/) - Cost analysis

---

## Related Documentation

- [Modules Overview](../README.md)
- [User Roles](../../user-roles/README.md)
