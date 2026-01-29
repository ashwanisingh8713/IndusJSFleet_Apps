# Trips Module

## Overview

The Trips module manages the complete trip lifecycle from planning to completion, including cost tracking, status management, customer coordination, and cargo handling.

---

## Features

- Trip planning and scheduling
- Route management with Google Maps integration
- Trip cost tracking (fuel, toll, driver allowance, etc.)
- Status management with state machine
- Customer and cargo management
- Real-time location tracking
- Payment tracking integration
- PDF report generation

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| Trips List | `FleetRoute.Trips` | List all trips with filters |
| Trip Detail | `FleetRoute.TripDetail` | Trip details with sections |
| Create Trip | `FleetRoute.CreateTrip` | Plan new trip |
| Trip Cost Entry | `FleetRoute.TripCostEntry` | Add trip cost |

---

## Trip Entity

### Core Information

| Field | Description | Required |
|-------|-------------|:--------:|
| Vehicle | Assigned vehicle | ✅ |
| Driver | Assigned driver | ✅ |
| Customer | Customer for billing | ✅ |

### Route Information

| Field | Description | Required |
|-------|-------------|:--------:|
| Start Location | Departure point | ✅ |
| Start Coordinates | Latitude/Longitude | Auto |
| End Location | Destination point | ✅ |
| End Coordinates | Latitude/Longitude | Auto |
| Estimated Distance | Calculated distance (km) | Auto |

### Schedule

| Field | Description | Required |
|-------|-------------|:--------:|
| Departure Date | Scheduled start date | ✅ |
| Departure Time | Scheduled start time | ✅ |
| Arrival Date | Expected end date | ✅ |
| Arrival Time | Expected end time | ✅ |

### Cargo Information

| Field | Description | Required |
|-------|-------------|:--------:|
| Cargo Type | Type of cargo | ✅ |
| Cargo Weight | Weight value | ✅ |
| Weight Unit | KG, Ton, Quintal, etc. | ✅ |
| Description | Cargo description | ❌ |

### Pricing (Owner/GM only)

| Field | Description | Required |
|-------|-------------|:--------:|
| Expected Trip Price | Quoted price | ✅ |
| Paid Amount | Amount received | Auto |
| Payment Status | Pending/Partial/Paid | Auto |

---

## Trip Status State Machine

### States

| State | Description | Editable | Can Cancel |
|-------|-------------|:--------:|:----------:|
| Planned | Scheduled, not started | ✅ | ✅ |
| On Route | Trip in progress | Limited | ✅ |
| Delayed | Temporarily stopped | ❌ | ✅ |
| Completed | Successfully finished | ❌ | ❌ |
| Cancelled | Trip cancelled | ❌ | ❌ |
| Failed | Trip failed | ❌ | ❌ |

### State Transitions

```
                    ┌─────────────┐
    Create Trip ───▶│   PLANNED   │
                    └──────┬──────┘
                           │
              Start Trip   │   Cancel
                           ▼
                    ┌─────────────┐
                    │  ON_ROUTE   │◄────┐
                    └──────┬──────┘     │
                           │            │
         ┌─────────────────┼────────────┤
         │                 │            │
         ▼                 ▼            │ Resume
  ┌─────────────┐   ┌─────────────┐     │
  │  COMPLETED  │   │   DELAYED   │─────┘
  └─────────────┘   └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │   FAILED    │
                    └─────────────┘
```

### Status Colors

| Status | Background | Icon |
|--------|------------|------|
| Planned | Blue | Calendar |
| On Route | Green | Truck |
| Delayed | Orange | Clock |
| Completed | Teal | Check |
| Cancelled | Gray | X |
| Failed | Red | Alert |

---

## Trip Detail Sections

### 1. Hero Section

Displays key trip information:
- Trip status badge
- Trip price (Owner/GM only)
- Vehicle number
- Driver name
- Distance & Duration
- Priority indicator

### 2. Route & Schedule Section

| Field | Description |
|-------|-------------|
| Departure | Location with date/time |
| Arrival | Location with date/time |
| Distance | Estimated/Actual distance |

### 3. Actual Times Section

Shown when trip has started:
- Actual start time
- Actual end time (if completed)
- Actual duration

### 4. Cargo Details Section

| Field | Description |
|-------|-------------|
| Cargo Type | Type of cargo |
| Weight | Weight with unit |
| Description | Cargo description |

### 5. Customer Details Section

| Field | Description |
|-------|-------------|
| Customer Name | Company/person name |
| Contact | Phone number (clickable) |
| Payment Status | Pending/Partial/Paid |

### 6. Cost Summary Section

| Field | Description |
|-------|-------------|
| Total Cost | Sum of all trip costs |
| Fuel Cost | Fuel expenses |
| Toll Cost | Toll charges |
| Other Costs | All other expenses |

---

## Create Trip Form

### Section Order

1. **Schedule**
   - Departure Date & Time
   - Expected Arrival Date & Time
   
2. **Vehicle & Driver**
   - Vehicle selection (dropdown)
   - Driver selection (dropdown)

3. **Route**
   - Start Location (Google Places autocomplete)
   - End Location (Google Places autocomplete)
   - Distance (auto-calculated)

4. **Cargo Details**
   - Cargo Type (dropdown)
   - Cargo Weight (numeric)
   - Weight Unit (dropdown)
   - Cargo Description

5. **Customer Details**
   - Customer selection (from local DB)
   - Add New Customer option

6. **Pricing** (Owner/GM only)
   - Expected Trip Price

7. **Priority**
   - Normal / High / Urgent

8. **Notes**
   - Additional instructions

### Validations

| Validation | Rule |
|------------|------|
| Arrival Date | Must be after Departure Date |
| Vehicle | Must be available (not on route) |
| Driver | Must be available (not on route) |
| Customer | Required for payment tracking |
| Cargo Weight | Must be positive number |
| Trip Price | Must be positive number |

---

## Cargo Types

| Type | Display Label |
|------|---------------|
| gitti | Gitti |
| balu | Balu (Sand) |
| bhakshi | Bhakshi |
| enta | Enta (Bricks) |
| hazardous | Hazardous |
| valuable | Valuable |
| others | Others |

---

## Weight Units

| Unit | Display Label |
|------|---------------|
| KG | Kilograms |
| m.ton | Metric Tons |
| quintal | Quintals |
| liter | Liters |
| ft3 | Cubic Feet |

---

## Priority Levels

| Priority | Color | Use Case |
|----------|-------|----------|
| Normal | Gray | Standard delivery |
| High | Orange | Prioritized delivery |
| Urgent | Red | Time-critical delivery |

---

## Role-Based Permissions

| Action | Owner | GM | Manager | Supervisor |
|--------|:-----:|:--:|:-------:|:----------:|
| View Trips | ✅ | ✅ | ✅ | Assigned |
| Create Trip | ✅ | ✅ | ✅ | ❌ |
| Edit Trip (Planned) | ✅ | ✅ | ✅ | ❌ |
| Edit Trip (Any State) | ✅ | ✅ | ❌ | ❌ |
| Cancel Trip | ✅ | ✅ | ✅ | ❌ |
| Start Trip | ✅ | ✅ | ✅ | ❌ |
| Complete Trip | ✅ | ✅ | ✅ | ❌ |
| View Trip Price | ✅ | ✅ | ❌ | ❌ |
| Edit Trip Price | ✅ | ✅ | ❌ | ❌ |
| Add Trip Cost | ✅ | ✅ | ✅ | ✅ |
| Delete Trip Cost | ✅ | ✅ | ✅ | ❌ |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/trips` | GET | List trips with filters |
| `/trips` | POST | Create trip |
| `/trips/{id}` | GET | Get trip details |
| `/trips/{id}` | PUT | Update trip |
| `/trips/{id}/start` | PATCH | Start trip |
| `/trips/{id}/complete` | PATCH | Complete trip |
| `/trips/{id}/cancel` | PATCH | Cancel trip |
| `/trips/{id}/delay` | PATCH | Mark delayed |
| `/trips/{id}/resume` | PATCH | Resume from delay |
| `/trips/{id}/costs` | GET | Get trip costs |
| `/trips/{id}/costs` | POST | Add trip cost |
| `/trips/{id}/payments` | GET | Get trip payments |

---

## Trip Cost Types

| Cost ID | Label | Group |
|---------|-------|-------|
| TC-001-001 | Petrol | Fuel |
| TC-001-002 | Diesel | Fuel |
| TC-002-001 | Toll Charges | Toll |
| TC-003-001 | Loading Charges | Loading/Unloading |
| TC-003-002 | Unloading Charges | Loading/Unloading |
| TC-004-001 | Driver Allowance | Driver |
| TC-005-001 | Parking Charges | Parking |

---

## Google Maps Integration

### Location Autocomplete

- Uses Google Places API
- Provides location suggestions as user types
- Returns coordinates for distance calculation

### Distance Calculation

- Uses Google Distance Matrix API
- Calculates road distance between points
- Auto-populates estimated distance field

---

## Related Modules

- [Vehicles](../vehicles/) - Vehicle assignment
- [Drivers](../drivers/) - Driver assignment
- [Customers](../customers/) - Customer selection
- [Payments](../payments/) - Payment tracking
- [Costs](../costs/) - Cost entry
- [Reports](../reports/) - Trip P&L

---

## Related Documentation

- [Modules Overview](../README.md)
- [User Roles](../../user-roles/README.md)
