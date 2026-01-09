# Application Use Cases Prompt

## Overview

IndusJS Fleet is a Fleet Management application for managing vehicles, drivers, trips, and costs. This document outlines all application use cases and their implementation requirements.

---

## Module 1: Vehicle Management

### Use Case 1.1: Add Vehicle
**Actor:** Owner, Manager
**Description:** Register a new vehicle with basic info and documents

**Steps:**
1. Navigate to Vehicles → Add Vehicle
2. Fill basic info (registration, make, model, year, type, fuel, capacity, color)
3. Upload documents (RC, Insurance, Pollution, Permit)
4. Submit → Vehicle created

**API:** `POST /vehicles`

**Validation:**
- Registration number: Required, unique
- Make/Model: Required
- Year: 1900-2100
- Documents: Optional but recommended

---

### Use Case 1.2: View Vehicle Details
**Actor:** All users
**Description:** View complete vehicle information with tabs

**Tabs:**
1. **Overview** - Basic info, assigned driver, current trip
2. **Trips** - Trip history for this vehicle
3. **Routes & Stops** - Route details (planned)
4. **Documents** - Uploaded documents with expiry status
5. **Costs** - Maintenance and trip costs with filters

**API:** `GET /vehicles/{id}`

---

### Use Case 1.3: Edit Vehicle
**Actor:** Owner, Manager
**Description:** Update vehicle information

**Editable Fields:**
- Basic info (make, model, year, etc.)
- Assigned driver (dropdown)
- Vehicle state (active, maintenance, inactive)

**API:** `PUT /vehicles/{id}`

---

### Use Case 1.4: Upload Vehicle Document
**Actor:** Owner, Manager
**Description:** Upload document with expiry tracking

**Document Types:**
- registration_certificate (RC)
- insurance
- pollution_certificate
- permit
- fitness_certificate

**API:** `POST /vehicles/{id}/documents`

---

### Use Case 1.5: Track Vehicle Costs
**Actor:** Owner, Manager, Supervisor
**Description:** View and filter vehicle maintenance costs

**Cost Types:**
- tyre, battery, servicing, engine_repair, body_repair, electrical, ac_repair, other

**Filters:**
- Date range (from/to)
- Cost type (multi-select)
- Sort by date/amount

**API:** `GET /vehicles/{id}/maintenance-costs`

---

## Module 2: Driver Management

### Use Case 2.1: Add Driver
**Actor:** Owner, Manager
**Description:** Register a new driver

**Required Fields:**
- First name, Last name
- Mobile number (10 digits)
- License number
- License expiry date
- License type

**API:** `POST /drivers`

---

### Use Case 2.2: View Driver Details
**Actor:** All users
**Description:** View driver information and status

**Sections:**
- Personal info (name, mobile, email)
- License info (number, type, expiry)
- Assigned vehicle (if any)
- Current trip assignment
- Additional info (created by, created at)

**API:** `GET /drivers/{id}`

---

### Use Case 2.3: Edit Driver
**Actor:** Owner, Manager
**Description:** Update driver information

**Editable Fields:**
- Personal info
- License info
- Status (active/inactive)

**API:** `PUT /drivers/{id}`

---

### Use Case 2.4: Toggle Driver Status
**Actor:** Owner, Manager
**Description:** Activate or deactivate a driver

**API:** `PATCH /drivers/{id}/toggle-active`

---

## Module 3: Trip Management

### Use Case 3.1: Create Trip
**Actor:** Owner, Manager, Supervisor
**Description:** Plan a new trip with full details

**Sections:**
1. **Vehicle & Driver** - Select from available list (shows occupied status)
2. **Route** - Start/End location with Google Places autocomplete
3. **Schedule** - Departure date/time, Arrival date/time
4. **Cargo** - Type, description, weight
5. **Customer** - Name, contact
6. **Additional** - Priority, notes

**API:** `POST /trips`

**Date/Time Format:**
- UI: DD-MM-YYYY, HH:MM (24hr)
- API: ISO 8601 (YYYY-MM-DDTHH:mm:ssZ)

**Distance Calculation:**
- Uses Google Distance Matrix API
- Auto-calculated from start/end locations

---

### Use Case 3.2: View Trip Details
**Actor:** All users
**Description:** View complete trip information

**Sections:**
- Route info (start, end, distance, duration)
- Schedule (departure, arrival times)
- Vehicle & Driver info
- Cargo details
- Customer info
- Cost summary
- Trip state with actions

**API:** `GET /trips/{id}`

---

### Use Case 3.3: Edit Trip
**Actor:** Owner, Manager
**Description:** Update trip details before departure

**Editable Fields:**
- Vehicle, Driver (if not in progress)
- Locations, Schedule
- Cargo, Customer
- Priority, Notes

**API:** `PUT /trips/{id}`

---

### Use Case 3.4: Cancel Trip
**Actor:** Owner, Manager
**Description:** Cancel a planned trip

**Conditions:**
- Only from Trip Detail screen
- Only for planned/in_progress trips

**API:** `PATCH /trips/{id}/cancel`

---

### Use Case 3.5: Update Trip State
**Actor:** Owner, Manager, Supervisor, Driver
**Description:** Change trip status

**State Flow:**
```
planned → in_progress → completed
    ↓
 cancelled
```

**API:** `PATCH /trips/{id}/state`

---

## Module 4: Cost Tracking

### Use Case 4.1: Add Trip Cost
**Actor:** Owner, Manager, Supervisor
**Description:** Record expense for a trip

**Cost Types:**
- fuel, toll, driver_allowance, parking
- loading_charges, unloading_charges
- chalan, permit, insurance, other

**Required Fields:**
- Trip (dropdown)
- Cost type
- Amount
- Date, Time

**API:** `POST /trips/{id}/costs`

---

### Use Case 4.2: Add Vehicle Maintenance Cost
**Actor:** Owner, Manager
**Description:** Record maintenance expense for a vehicle

**Cost Types:**
- tyre, battery, servicing
- engine_repair, body_repair, electrical
- ac_repair, other

**Required Fields:**
- Vehicle (dropdown)
- Cost type
- Amount
- Date, Time
- Description (optional)
- Vendor name (optional)
- Invoice number (optional)

**API:** `POST /vehicles/{id}/maintenance-costs`

---

### Use Case 4.3: View Cost Overview
**Actor:** Owner, Manager
**Description:** See financial summary on dashboard

**Filters:**
- Today / Weekly / Monthly

**Metrics:**
- Total expenses
- Profit/Loss
- Completed trips count

**API:** `GET /dashboard/cost-overview?filter=today|weekly|monthly`

---

## Module 5: Dashboard & Alerts

### Use Case 5.1: View Dashboard
**Actor:** All users
**Description:** See fleet overview and key metrics

**Sections:**
- Fleet Overview (vehicles, drivers, trips)
- Quick Actions
- Cost Overview
- Pending Payments
- Vehicle/Driver/Trip Status
- Alerts

**API:** `GET /dashboard`

---

### Use Case 5.2: View Alerts
**Actor:** All users
**Description:** See important notifications

**Alert Types:**
- Document expiry (vehicle)
- License expiry (driver)
- Maintenance due
- Missing documents

**Alert Priority:**
- Critical (expired, overdue)
- Warning (expiring soon)
- Info (upcoming)

**API:** `GET /dashboard/alerts-status`

---

### Use Case 5.3: View Pending Payments
**Actor:** Owner, Manager
**Description:** See outstanding customer payments

**Display:**
- Customer name
- Trip info
- Pending amount
- Days overdue

**API:** `GET /dashboard/pending-payments`

---

## Module 6: Team Management

### Use Case 6.1: Add Team Member
**Actor:** Owner only
**Description:** Register a manager or supervisor

**Roles:**
- manager - Can manage vehicles, drivers, trips
- supervisor - Can view and update trips

**API:** `POST /team/members`

---

### Use Case 6.2: View Team List
**Actor:** Owner, Manager
**Description:** See all team members

**API:** `GET /team/members`

---

## Module 7: User Management

### Use Case 7.1: Login
**Actor:** All users
**Description:** Authenticate with email/mobile and password

**API:** `POST /auth/login`

---

### Use Case 7.2: Sign Up
**Actor:** New user (becomes Owner)
**Description:** Register new account

**API:** `POST /auth/signup`

---

### Use Case 7.3: Forgot Password
**Actor:** All users
**Description:** Reset password via email

**API:** `POST /auth/forgot-password`

---

### Use Case 7.4: Change Password
**Actor:** Authenticated users
**Description:** Update password from profile

**API:** `POST /auth/change-password`

---

### Use Case 7.5: View/Edit Profile
**Actor:** Authenticated users
**Description:** View and update profile info

**API:** `GET /users/profile`, `PUT /users/profile`

---

## Module 8: Maps & Tracking

### Use Case 8.1: View Live Map
**Actor:** All users
**Description:** See vehicles on map with real-time positions

**Features:**
- Vehicle markers
- Current location
- Trip routes (planned)

**API:** Location updates via polling/websocket

---

## Data Types Reference

### Cargo Types
```
Gitti, Balu, Bhakshi, Enta, Hazardous, Valuable, Others
```

### Trip Cost Types
```
fuel, toll, driver_allowance, parking, loading_charges, 
unloading_charges, chalan, permit, insurance, other
```

### Maintenance Cost Types
```
tyre, battery, servicing, engine_repair, body_repair, 
electrical, ac_repair, other
```

### Payment Status
```
pending, partial, paid
```

### Trip States
```
planned, in_progress, completed, cancelled
```

### Vehicle States
```
active, maintenance, inactive
```

### User Roles
```
owner, manager, supervisor, driver
```

---

## Date/Time Handling

### Display Format (UI)
- Date: `DD-MM-YYYY` (e.g., "04-01-2026")
- Time: `HH:MM` 24-hour format (e.g., "14:30")

### API Format (Request/Response)
- ISO 8601: `YYYY-MM-DDTHH:mm:ssZ` (e.g., "2026-01-04T14:30:00Z")

### Conversion Functions
```kotlin
// UI to API: "04-01-2026" + "14:30" → "2026-01-04T14:30:00Z"
fun toIsoDateTime(date: String, time: String): String

// API to UI: "2026-01-04T14:30:00Z" → "04-01-2026", "14:30"
fun fromIsoToDisplay(iso: String): Pair<String, String>
```

---

## Validation Rules

| Field | Rule |
|-------|------|
| Email | Valid email format |
| Mobile | 10 digits, numeric only |
| Date | DD-MM-YYYY format |
| Time | HH:MM 24-hour format |
| Amount | Positive number |
| Registration | Non-empty, alphanumeric |
| License | Non-empty |

