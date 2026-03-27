# IndusJS Fleet — Application Overview

## Description

**IndusJS Fleet** is a comprehensive, cross-platform Fleet Management System built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**. It targets **Android**, **iOS**, and **Web** (JS + WasmJS) from a single shared codebase. The application enables transportation and logistics businesses — from single-truck owners to multi-vehicle fleet operators — to digitize and streamline every aspect of their fleet operations: vehicle lifecycle management, driver workforce administration, trip planning with live GPS tracking, granular cost accounting, customer billing, payment collection, financial reporting, and team collaboration with role-based access control.

The system comprises two deployable applications:

| Application | Description | Platforms |
|-------------|-------------|-----------|
| **Fleet Management App** | Used by fleet owners, managers, and supervisors to manage all operations | Android, iOS, Web |
| **Location Tracker App** | Lightweight standalone APK installed on driver devices; publishes real-time GPS coordinates via MQTT | Android only |

### Technology Highlights

- **Single codebase** — Kotlin Multiplatform with Compose Multiplatform UI
- **Material 3** design system with dynamic day/night theming
- **Clean Architecture + MVI** pattern across all feature modules
- **Metro DI** (by ZacSweers) for dependency injection
- **Navigation 3** for type-safe multiplatform navigation
- **Ktor Client** for networking with JWT token-based auth
- **Room + multiplatform-settings** for offline caching
- **MQTT (HiveMQ)** for real-time vehicle tracking
- **Google Places + Distance Matrix API** for route planning

---

## Purpose

IndusJS Fleet exists to solve the operational chaos that plagues India's fragmented road transport industry. Most fleet operators — especially small and mid-sized businesses running 5–100 vehicles — still rely on paper registers, phone calls, and WhatsApp messages to coordinate daily operations. This leads to:

- **No visibility** into where vehicles are or what drivers are doing in real time.
- **Untracked expenses** — fuel, tolls, maintenance, and driver costs slip through the cracks.
- **Delayed or missed payments** from customers with no centralized billing.
- **Expired documents** (insurance, fitness certificates, permits) causing fines and impounded vehicles.
- **No financial clarity** — owners cannot tell which vehicles or routes are profitable.
- **Communication overhead** — managers, supervisors, and drivers operate in information silos.

IndusJS Fleet digitizes these workflows into a single platform, giving fleet operators a **real-time command center** for their entire business.

---

## Why Is This Fleet Management App Required?

### 1. The Indian Road Transport Problem

India's road freight market is valued at over **₹14 lakh crore** (~$170B), with ~85% operated by small fleet owners (1–20 vehicles). These operators face daily challenges that cost them money, time, and compliance:

| Challenge | Impact |
|-----------|--------|
| **Manual record-keeping** | Lost data, duplicate entries, accounting errors |
| **No trip cost tracking** | Fuel pilferage, inflated toll claims, unaccounted expenses |
| **Paper-based compliance** | Expired permits/insurance → vehicle seizure, heavy fines (₹5,000–₹50,000+) |
| **Cash-based payments** | Payment disputes, delayed collections, cash flow crises |
| **No route optimization** | Deadhead miles, inefficient vehicle utilization |
| **Driver management chaos** | License expiry surprises, salary disputes, untracked advances |
| **Zero financial insight** | Cannot identify loss-making vehicles or unprofitable routes |

### 2. Why Not Existing Solutions?

| Gap in Market | IndusJS Fleet Advantage |
|---------------|------------------------|
| Enterprise tools (Fleetio, Samsara) are **too expensive** for Indian SMBs | Built for Indian fleet economics — ₹-denominated, GST-aware, local payment modes (UPI, cheque) |
| Most apps are **Android-only** | Kotlin Multiplatform → single codebase for Android, iOS, and Web |
| Existing apps focus only on GPS tracking | **End-to-end** — vehicles, drivers, trips, costs, payments, finance, reports, team management |
| No **role-based access** for Indian fleet hierarchy | Hierarchical roles (Owner → GM → Manager → Supervisor → Driver) with granular permission control |
| No **offline support** for areas with poor connectivity | Local caching via Room for customers, team members, and dashboard data |
| No **cost categorization** for Indian transport needs | Structured cost taxonomy (50+ cost types) with group-level aggregation |

### 3. Business Value Delivered

| Benefit | How |
|---------|-----|
| **Save 2–4 hours/day** | Eliminates manual coordination and paperwork |
| **Reduce fuel pilferage by 10–15%** | Per-trip fuel cost tracking with quantity, rate, and km/liter |
| **Eliminate compliance fines** | Proactive document/license expiry alerts (7/30/90-day warnings) |
| **Improve cash flow** | Real-time payment tracking and pending payment dashboards |
| **Enable data-driven decisions** | Vehicle-level and trip-level Profit & Loss reports |
| **Scale operations without scaling headcount** | Role-based team delegation with hierarchical permissions |
| **Track fleet in real time** | GPS tracking via MQTT with speed, heading, and battery info |

---

## Features

### 1. Core Fleet Operations

#### Vehicle Management
- Register vehicles with full details: registration number, make/model, year, fuel type, chassis number, engine number
- **6 lifecycle states**: `inactive → active → on_route → maintenance → damaged → decommissioned`
- Upload and manage documents (RC, Insurance, Fitness Certificate, National Permit, State Permit, PUC) with **expiry date tracking**
- Assign caretakers (team members) to vehicles
- View vehicle-specific: trips, documents, maintenance costs
- **Screens**: Vehicle List (search + status filter) → Vehicle Detail (4 tabs: Overview, Trips, Documents, Costs) → Add Vehicle

#### Driver Management
- Register drivers with license details: license number, type, expiry date
- **6 lifecycle states**: `inactive → active → on_route → on_leave → suspended → terminated`
- Record driver costs: salary, advance, bonus, penalty, deductions
- Assign drivers to vehicles and trips
- Emergency contact tracking
- **Screens**: Driver List → Driver Detail → Create Driver → Driver Cost Entry

#### Trip Planning & Execution
- **Multi-step trip creation workflow**:
  1. Select Vehicle (only `active` status vehicles)
  2. Select Driver (only `active` status drivers)
  3. Set Route — Google Places autocomplete for start/end locations + Distance Matrix for auto-distance calculation
  4. Schedule — Departure and estimated arrival date/time
  5. Cargo — Type (Gitti, Balu, Bhakshi, Enta, Hazardous, Valuable, Others) + weight
  6. Customer Assignment
  7. Pricing (Owner/GM only) — trip price + advance payment
- **Trip state machine**: `planned → on_route → completed` (with `cancelled`, `delayed`, `failed` branches)
- Trip state changes automatically update linked vehicle and driver states (handled by backend)
- Track trip costs and customer payments per trip
- **Screens**: Trip List (status filter chips) → Trip Detail → Create Trip → Trip Cost Entry

#### Customer Management
- Register customers with: company name, contact person, mobile, email, GST number, address (city, state)
- Track trip history per customer
- View customer financial summaries: total trips, total revenue, outstanding amount
- **Offline cached** for fast access in areas with poor connectivity
- Toggle customer active/inactive status
- **Screens**: Customer List (search) → Customer Detail (3 tabs: Info, Trips, Financials) → Create Customer

---

### 2. Financial Management

#### Cost Tracking (50+ Structured Cost Types)
Record expenses across **3 categories** with structured IDs (`{PREFIX}-{GROUP}-{ITEM}`):
- **Trip Costs (TC)** — 6 groups, 20 cost items (fuel, toll, loading, driver expenses, permits, miscellaneous)
- **Vehicle Maintenance Costs (VMC)** — 7 groups, 30+ cost items (regular maintenance, repairs, electrical, body, engine, wheels, miscellaneous)
- **Driver Costs (DC)** — 4 groups, 18 cost items (salary, bonuses, deductions, other)

Each cost entry includes: amount, date (DD-MM-YYYY), time (HH:MM 24hr), notes, and optional metadata.
Fuel costs have extra fields: `fuel_quantity`, `fuel_rate`, `km_per_liter`.
Driver deduction costs have `is_deduction = true` flag.

Cost types are **cached locally** at app startup for instant selection.

#### Payment Collection
- Record customer payments against trips
- **5 payment modes**: Cash, UPI, Bank Transfer, Card, Credit
- **4 payment types**: Advance, Partial, Final, Refund
- **3 statuses**: Received, Pending, Cancelled
- Reference number tracking for bank/UPI payments
- View payments by trip, by customer, or globally
- **Screens**: Payment List (filters) → Payment Detail → Add Payment → Edit Payment

#### Vehicle Finance
- Record vehicle purchase details: cash purchase or loan purchase
- **Loan tracking**: down payment, loan amount, interest rate, tenure (months), monthly EMI
- EMI payment history with amount, date, status, notes
- Loan summary dashboard: total paid, remaining amount, next due date
- **Owner and General Manager access only**
- **Screens**: Finance List → Finance Detail → Add/Edit Purchase Info → EMI Payment History

#### Reports & Analytics
- **Profit & Loss** by vehicle, by trip, consolidated fleet-wide
- **Cost Analysis** with breakdown by category and pie chart visualization
- **8 report periods**: Daily, Weekly, 15-day, Monthly, Quarterly, Half-Yearly, Yearly, Custom date range
- **Financial year**: April 1 – March 31 (Indian standard)
- **PDF export** for all report types (platform-specific: Android WebView, iOS WKWebView, Web browser print)
- **Owner and General Manager access only**
- **Screens**: Reports Hub → Vehicle P&L → Trip P&L → Cost Analysis → Consolidated P&L

---

### 3. Operations & Monitoring

#### Dashboard (Central Hub)
6 sections on the main screen:

| Section | Roles | Content |
|---------|-------|---------|
| **Fleet Overview** | All | Vehicle, driver, trip counts by status |
| **Cost Overview** | All | Today's / this week's / this month's costs, breakdown by type |
| **Financial Summary** | Owner, GM | Total revenue, expenses, net profit, profit margin |
| **Pending Payments** | Owner, GM | Outstanding payment amounts, recent pending list |
| **Alerts** | All | Document expiry, license expiry, maintenance due warnings |
| **Quick Actions** | All (role-filtered) | Add Trip Cost, Add Maintenance Cost, Add Driver Cost, Create Trip, Add Vehicle, Add Driver |

Dashboard data is **offline cached** — displays instantly on app launch with background API refresh.

#### Real-Time GPS Tracking
- **Location Tracker App** (separate Android APK) on driver devices:
  - Uses `FusedLocationProviderClient` for GPS
  - Publishes to MQTT topic: `fleet/vehicle/{registration_number}/location`
  - Via HiveMQ broker
- **Fleet App** subscribes to broker and displays live vehicle positions on map
- MQTT message payload:
  ```json
  {
    "registration_number": "MH12AB1234",
    "lat": 19.0760,
    "lng": 72.8777,
    "speed": 45.5,
    "heading": 180.0,
    "accuracy": 5.0,
    "bat": 85,
    "ts": 1703500800000,
    "provider": "gps"
  }
  ```

#### Alerts System
Proactive notifications for compliance risks:

| Alert Type | Source | Examples |
|------------|--------|----------|
| **Document Expiry** | Vehicle documents | Insurance, fitness certificate, permit, PUC nearing/past expiry |
| **License Expiry** | Driver profiles | Driving licenses expiring soon |
| **Maintenance Due** | Vehicle maintenance schedule | Scheduled maintenance reminders |

**Severity levels:**
- 🔴 **Critical** — expired or expiring within 7 days
- 🟠 **Warning** — expiring within 30 days
- 🔵 **Info** — expiring within 90 days

#### Team Management
- Add team members with hierarchical roles: General Manager, Manager, Supervisor
- Each role has specific permission levels (see [User Roles & Permissions](#user-roles--permissions))
- **Owner** creates any role; **GM** creates Manager/Supervisor only
- Toggle member active/inactive status
- Team members cached locally for caretaker assignment in vehicle/driver forms
- **Screens**: Team List → Team Member Detail → Create Team Member

---

### 4. User & Access Control

#### Authentication
- Email/password login and signup (Owner registration)
- Forgot password with email reset link
- Profile view/edit and password change
- JWT token-based session with automatic 401 → re-login handling

#### Onboarding
- First-time user onboarding flow for new fleet owners
- Guides through initial app setup

---

### 5. Platform & Technical Features

| Feature | Details |
|---------|---------|
| **Cross-Platform** | Single Kotlin codebase → Android (primary), iOS (Swift interop via KMP framework), Web (JS + WasmJS) |
| **Offline Support** | Dashboard, customers, team members cached via Room database; instant display with background refresh |
| **PDF Export** | Vehicle costs, trip costs, driver costs, P&L statements, payment summaries, customer financials; Android (WebView), iOS (WKWebView), Web (browser print) |
| **Material 3 Theming** | Dynamic day/night (light/dark) theme support via MaterialKolor; consistent design across all screens |
| **Structured Logging** | `FleetLogger` (via Kermit) with module-tagged logs across all platforms |
| **Crash Reporting** | Firebase Crashlytics on Android |

---

## Cost Types

IndusJS Fleet uses a **structured cost taxonomy** with unique IDs for every expense type. Format: `{PREFIX}-{GROUP_NUMBER}-{ITEM_NUMBER}`.

Cost types are fetched from the API and **cached locally** at app startup via `AppInitializer` → `InitializeCostTypesUseCase`.

### Trip Costs (TC)

Recorded against a specific trip and its assigned vehicle.

| Group ID | Group Name | Cost ID | Cost Label | Special Fields |
|----------|------------|---------|------------|----------------|
| **TC-G-001** | **Fuel & Energy** | TC-001-001 | Petrol | `fuel_quantity`, `fuel_rate`, `km_per_liter` |
| | | TC-001-002 | Diesel | `fuel_quantity`, `fuel_rate`, `km_per_liter` |
| | | TC-001-003 | CNG/LPG | `fuel_quantity`, `fuel_rate`, `km_per_liter` |
| | | TC-001-004 | EV Charging | `fuel_quantity`, `fuel_rate`, `km_per_liter` |
| **TC-G-002** | **Toll & Parking** | TC-002-001 | Toll Charges | — |
| | | TC-002-002 | Parking Fees | — |
| | | TC-002-003 | Entry Charges | — |
| **TC-G-003** | **Loading & Unloading** | TC-003-001 | Loading Charges | — |
| | | TC-003-002 | Unloading Charges | — |
| | | TC-003-003 | Crane/Forklift Charges | — |
| | | TC-003-004 | Labor Charges | — |
| **TC-G-004** | **Driver Expenses** | TC-004-001 | Driver Allowance | — |
| | | TC-004-002 | Food & Refreshments | — |
| | | TC-004-003 | Accommodation | — |
| **TC-G-005** | **Permits & Compliance** | TC-005-001 | State Permit | — |
| | | TC-005-002 | National Permit | — |
| | | TC-005-003 | RTO Challan | — |
| | | TC-005-004 | Weight Fine | — |
| **TC-G-006** | **Miscellaneous** | TC-006-001 | Phone/Communication | — |
| | | TC-006-002 | Vehicle Cleaning | — |
| | | TC-006-003 | Miscellaneous | — |
| | | TC-006-004 | Tip/Bribe | — |
| | | TC-006-005 | Other | — |

### Vehicle Maintenance Costs (VMC)

Recorded against a specific vehicle, independent of any trip.

| Group ID | Group Name | Cost Items |
|----------|------------|------------|
| **VMC-G-001** | **Regular Maintenance** | Oil change, Filter replacement, Brake service, Coolant flush, Belt replacement |
| **VMC-G-002** | **Repairs & Replacements** | Clutch repair, Suspension, Steering, Radiator, Exhaust system |
| **VMC-G-003** | **Electrical & AC** | Battery replacement, Alternator, Starter motor, Wiring repair, AC repair/recharge |
| **VMC-G-004** | **Body & Exterior** | Denting/straightening, Painting/touch-up, Windshield replacement, Mirror replacement, Bumper repair |
| **VMC-G-005** | **Engine & Transmission** | Engine overhaul, Gearbox repair, Turbocharger, Fuel injection system, Head gasket |
| **VMC-G-006** | **Miscellaneous** | Towing charges, Accessories, Vehicle cleaning, Documentation fees, Other |
| **VMC-G-007** | **Wheels & Tires** | New tyre purchase, Tyre puncture repair, Wheel alignment, Wheel balancing, Tyre rotation |

### Driver Costs (DC)

Recorded against a specific driver.

| Group ID | Group Name | Cost ID | Cost Label | Deduction? |
|----------|------------|---------|------------|:----------:|
| **DC-G-001** | **Salary & Wages** | DC-001-001 | Monthly Salary | No |
| | | DC-001-002 | Daily Wages | No |
| | | DC-001-003 | Overtime Pay | No |
| | | DC-001-004 | Incentive | No |
| **DC-G-002** | **Incentives & Bonuses** | DC-002-001 | Trip Bonus | No |
| | | DC-002-002 | Festival Bonus | No |
| | | DC-002-003 | Performance Bonus | No |
| | | DC-002-004 | Referral Bonus | No |
| | | DC-002-005 | Other Bonus | No |
| **DC-G-003** | **Deductions** | DC-003-001 | Advance Recovery | **Yes** |
| | | DC-003-002 | Fine/Penalty | **Yes** |
| | | DC-003-003 | Damage Deduction | **Yes** |
| | | DC-003-004 | Loan EMI | **Yes** |
| | | DC-003-005 | Other Deduction | **Yes** |
| **DC-G-004** | **Other Expenses** | DC-004-001 | Insurance | No |
| | | DC-004-002 | Medical | No |
| | | DC-004-003 | Training | No |
| | | DC-004-004 | Uniform | No |
| | | DC-004-005 | Other | No |

> **Deduction costs** (DC-G-003) have `is_deduction = true` flag. These amounts are **subtracted** from driver earnings in P&L calculations.

### Cost Entry API Format

All cost entries use DD-MM-YYYY date format (**never** ISO 8601):

```json
{
  "cost_id": "TC-001-002",
  "amount": 5000.00,
  "date": "15-03-2026",
  "time": "14:30",
  "notes": "Diesel refill at HP pump",
  "fuel_quantity": 75.5,
  "fuel_rate": 89.50,
  "km_per_liter": 4.2
}
```

### P&L Revenue Formula

```
Revenue           = SUM(trip_price) for completed trips in the period
Trip Costs        = SUM(all trip cost entries)
Maintenance Costs = SUM(all vehicle maintenance cost entries)
Driver Costs      = SUM(all driver cost entries, deductions subtracted)
EMI Costs         = SUM(vehicle loan EMI payments in the period)

Total Expenses    = Trip Costs + Maintenance Costs + Driver Costs + EMI Costs
Net Profit        = Revenue − Total Expenses
Profit Margin     = (Net Profit / Revenue) × 100%
```

---

## Entity State Machines

### Vehicle Lifecycle

```
               ┌─────────┐
               │ inactive │ ←──────────────────────┐
               └────┬─────┘                        │
                    ↕                               │
               ┌────┴─────┐                        │
          ┌───→│  active   │←──────┐               │
          │    └──┬───┬────┘       │               │
          │       │   │            │               │
          │       ↓   ↓            │               │
          │  on_route  maintenance │               │
          │       │       ↕        │               │
          │       │    damaged ────┘               │
          └───────┘       │                        │
                          ↓                        │
                   decommissioned                  │
```

| State | Available for Trip Assignment? |
|-------|:-----------------------------:|
| `inactive` | ❌ |
| `active` | ✅ |
| `on_route` | ❌ |
| `maintenance` | ❌ |
| `damaged` | ❌ |
| `decommissioned` | ❌ |

### Driver Lifecycle

```
               ┌─────────┐
               │ inactive │
               └────┬─────┘
                    ↕
               ┌────┴─────┐
          ┌───→│  active   │←──────────┐
          │    └──┬──┬──┬──┘           │
          │       │  │  │              │
          │       ↓  ↓  ↓             │
          │  on_route on_leave suspended
          │       │     │         │
          └───────┘     └─────────┘
                              │
                              ↓
                         terminated (terminal)
```

| State | Available for Trip Assignment? |
|-------|:-----------------------------:|
| `inactive` | ❌ |
| `active` | ✅ |
| `on_route` | ❌ |
| `on_leave` | ❌ |
| `suspended` | ❌ |
| `terminated` | ❌ (terminal, no further transitions) |

### Trip Lifecycle

```
          ┌─────────┐
          │ planned  │──────────→ cancelled
          └────┬─────┘
               ↓
          ┌─────────┐
          │on_route  │──────────→ cancelled
          └──┬──┬────┘──────────→ failed
             │  │
             ↓  ↓
       completed delayed
                   │
                   ├──→ completed
                   └──→ failed
```

| State | Can Cancel? | Can Start? | Terminal? |
|-------|:-----------:|:----------:|:---------:|
| `planned` | ✅ | ✅ | No |
| `on_route` | ✅ | ❌ | No |
| `delayed` | ❌ | ❌ | No |
| `completed` | ❌ | ❌ | **Yes** |
| `cancelled` | ❌ | ❌ | **Yes** |
| `failed` | ❌ | ❌ | **Yes** |

### Cross-Entity State Sync

Trip state transitions automatically update linked entities (handled by backend API):

| Trip Transition | Vehicle State Change | Driver State Change |
|----------------|---------------------|---------------------|
| `planned → on_route` | → `on_route` | → `on_route` |
| `on_route → completed` | → `active` | → `active` |
| `on_route → cancelled` | → `active` | → `active` |
| `on_route → failed` | → `active` | → `active` |

---

## Payment Modes & Statuses

### Payment Modes

| Mode | Description | Reference # Required? |
|------|-------------|:---------------------:|
| **Cash** | Physical cash payment | No |
| **UPI** | Unified Payments Interface (Google Pay, PhonePe, Paytm, etc.) | Recommended |
| **Bank Transfer** | NEFT / RTGS / IMPS bank transfer | Yes |
| **Card** | Debit or Credit card payment | Recommended |
| **Credit** | Payment on credit (to be collected later) | No |

### Payment Types

| Type | Description | Typical Usage |
|------|-------------|---------------|
| **Advance** | Pre-trip payment | Before trip starts |
| **Partial** | Partial payment | During or after trip |
| **Final** | Remaining balance settlement | After trip completion |
| **Refund** | Money returned to customer | Trip cancellation or modification |

### Payment Statuses

| Status | Description | Color |
|--------|-------------|-------|
| **Received** | Payment collected and confirmed | 🟢 Green |
| **Pending** | Payment expected but not yet received | 🟡 Yellow |
| **Cancelled** | Payment voided | 🔴 Red |

---

## User Roles & Permissions

IndusJS Fleet uses a **5-tier hierarchical role system**. Each role inherits view permissions from roles below it, with specific write/action permissions:

```
Owner (full access)
  └── General Manager (financial access, manage M/S)
        └── Manager (operational access, NO financials)
              └── Supervisor (view-only, can update trip status)
                    └── Driver (location tracker only — separate app)
```

### Detailed Permission Matrix

| Feature Area | Owner | General Manager | Manager | Supervisor |
|-------------|:-----:|:---------------:|:-------:|:----------:|
| **Dashboard — basic stats** | ✅ | ✅ | ✅ | ✅ |
| **Dashboard — Financial Summary** | ✅ | ✅ | ❌ | ❌ |
| **Dashboard — Pending Payments** | ✅ | ✅ | ❌ | ❌ |
| **View `trip_price` field** | ✅ | ✅ | ❌ | ❌ |
| **Create / Edit Vehicle** | ✅ | ✅ | ✅ | ❌ |
| **Delete Vehicle** | ✅ | ✅ | ❌ | ❌ |
| **Create / Edit Driver** | ✅ | ✅ | ✅ | ❌ |
| **Delete Driver** | ✅ | ✅ | ❌ | ❌ |
| **Create Trip** | ✅ | ✅ | ✅ | ❌ |
| **Update Trip Status** | ✅ | ✅ | ✅ | ✅ |
| **Cancel Trip** | ✅ | ✅ | ✅ | ❌ |
| **Add Trip Cost** | ✅ | ✅ | ✅ | ✅ |
| **Delete Trip Cost** | ✅ | ✅ | ✅ | ❌ |
| **Add Maintenance Cost** | ✅ | ✅ | ✅ | ✅ |
| **Add Driver Cost** | ✅ | ✅ | ✅ | ❌ |
| **View Payments** | ✅ | ✅ | ✅ | ✅ |
| **Add / Edit Payment** | ✅ | ✅ | ✅ | ❌ |
| **Delete Payment** | ✅ | ✅ | ❌ | ❌ |
| **View Reports (P&L)** | ✅ | ✅ | ❌ | ❌ |
| **Export PDF Reports** | ✅ | ✅ | ❌ | ❌ |
| **Manage Team — create any role** | ✅ | ❌ | ❌ | ❌ |
| **Manage Team — create M/S** | ✅ | ✅ | ❌ | ❌ |
| **Delete Team Member** | ✅ | ✅ (M/S only) | ❌ | ❌ |
| **Vehicle Finance** | ✅ | ✅ | ❌ | ❌ |
| **Customer Management** | ✅ | ✅ | ✅ | ✅ (view only) |
| **Profile / Change Password** | ✅ | ✅ | ✅ | ✅ |

### API Role Values

| Display Name | API Value |
|-------------|-----------|
| Owner | `owner` |
| General Manager | `general_manager` |
| Manager | `manager` |
| Supervisor | `supervisor` |

---

## Navigation & Screen Map

### Complete Screen Inventory (40+ screens)

```
Login ──→ Dashboard ──┬──→ Vehicles ──→ VehicleDetail (4 tabs) ──→ AddVehicle
                      │                                          ──→ MaintenanceCostEntry
                      ├──→ Drivers  ──→ DriverDetail ──→ CreateDriver
                      │                               ──→ DriverCostEntry
                      ├──→ Trips    ──→ TripDetail ──→ CreateTrip
                      │                             ──→ TripCostEntry
                      ├──→ Customers ──→ CustomerDetail ──→ CreateCustomer
                      ├──→ Payments ──→ PaymentDetail ──→ AddPayment / EditPayment
                      ├──→ Reports  ──→ VehiclePL / TripPL / CostAnalysis / ConsolidatedPL
                      ├──→ Team     ──→ TeamMemberDetail ──→ CreateTeamMember
                      ├──→ Finance  ──→ VehicleFinanceDetail ──→ Add/EditPurchaseInfo
                      │                                      ──→ EmiPaymentHistory
                      ├──→ Maps (Real-time tracking)
                      ├──→ Alerts
                      └──→ Profile ──→ ChangePassword
```

### Quick Actions from Dashboard

| Action | Route | Required Role |
|--------|-------|--------------|
| Add Trip Cost | TripCostEntry | All |
| Add Maintenance Cost | MaintenanceCostEntry | All |
| Add Driver Cost | DriverCostEntry | Owner, GM, Manager |
| Create Trip | CreateTrip | Owner, GM, Manager |
| Add Vehicle | AddVehicle | Owner, GM, Manager |
| Add Driver | CreateDriver | Owner, GM, Manager |

---

## Cargo Types

Used in trip creation to categorize the type of goods being transported:

| Cargo Type | Description | Typical Vehicles |
|------------|-------------|-----------------|
| **Gitti** | Crushed stone/gravel for construction | Tippers, dump trucks |
| **Balu** | Sand for construction | Tippers, dump trucks |
| **Bhakshi** | Mixed construction material | Open-body trucks |
| **Enta** | Bricks | Open-body trucks |
| **Hazardous** | Hazardous materials (chemicals, flammables) | Tankers, specialized vehicles |
| **Valuable** | High-value goods requiring special handling | Closed-body trucks, containers |
| **Others** | Any other cargo type | Any vehicle type |

---

## Document Types (Vehicle)

Documents tracked per vehicle with expiry dates for compliance:

| Document Type | Abbreviation | Mandatory? | Typical Validity |
|--------------|-------------|:----------:|-----------------|
| **Registration Certificate** | RC | Yes | Lifetime (with renewal for commercial) |
| **Insurance** | INS | Yes | 1 year |
| **Fitness Certificate** | FC | Yes | 2 years (new), 1 year (old) |
| **National Permit** | NP | Depends on routes | 5 years |
| **State Permit** | SP | Depends on routes | 5 years |
| **PUC Certificate** | PUC | Yes | 6 months – 1 year |
| **Other** | — | No | Varies |

