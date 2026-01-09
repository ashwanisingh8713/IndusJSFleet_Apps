# Dashboard Sections Prompt

## Overview

The DashboardScreen is the main hub of the IndusJS Fleet app. It displays key fleet metrics, alerts, and quick actions in a scrollable layout.

---

## Section Order (LazyColumn)

1. **Fleet Overview Hero Card** - Always first
2. **Quick Actions Section** - Right after overview
3. **Cost Overview Section** - Financial summary (conditional)
4. **Pending Payments Section** - Outstanding payments (conditional)
5. **Vehicle Status Section** - Vehicle breakdown
6. **Trips Status Section** - Trip breakdown
7. **Alerts Section** - Warnings and notifications
8. **Drivers Status Section** - Driver breakdown

---

## Section Details

### 1. Fleet Overview Hero Card

**Purpose:** Show key fleet metrics at a glance with visual status bar

**Data Sources:**
- `VehicleStatusSummary` - total, available, onTrip, maintenance
- `DriverStatusSummary` - total, available, onTrip
- `TripSummary` - total, active, planned, completed

**UI Elements:**
```
┌─────────────────────────────────────────────┐
│ Fleet Overview                              │
├─────────────────────────────────────────────┤
│  🚛 Vehicles    👨‍✈️ Drivers    🗺️ Trips    │
│     12            8             25          │
│  3 available  5 available   5 active        │
├─────────────────────────────────────────────┤
│ [████████░░░░] Vehicle Status Bar           │
│ ● Route ● Planned ● Available ● Maint       │
└─────────────────────────────────────────────┘
```

**Interactions:**
- Click on Vehicles → Navigate to VehiclesScreen
- Click on Drivers → Navigate to DriversScreen
- Click on Trips → Navigate to TripsScreen

---

### 2. Quick Actions Section

**Purpose:** Provide fast access to common actions

**Visibility:** Always shown

**Conditional Elements:**
- Trip Cost button: Only if `tripSummary.total > 0`
- Vehicle Cost button: Only if `vehicleStatus.total > 0`

**UI Elements:**
```
┌─────────────────────────────────────────────┐
│ ▌Quick Actions                              │
├─────────────────────────────────────────────┤
│ [Trip Cost 💰] [Vehicle Cost 🔧]            │  ← Cost actions (if applicable)
├─────────────────────────────────────────────┤
│ [🚛 Vehicles] [👨‍✈️ Drivers]                 │
│ [🗺️ Trips  ] [📍 Live Map]                  │
└─────────────────────────────────────────────┘
```

---

### 3. Cost Overview Section

**Purpose:** Show financial summary with time filter

**Data Source:** `CostOverview` from `/dashboard/cost-overview` API

**Visibility Condition:**
```kotlin
val hasNoFleet = vehicleStatus.total == 0 && tripSummary.total == 0
val hasNoCostData = costOverview.totalExpenses == 0.0 && costOverview.profitLoss == 0.0
val shouldShow = hasNoFleet || !hasNoCostData
```

**UI Elements:**
```
┌─────────────────────────────────────────────┐
│ 📈 Financial Overview        [T] [W] [M]    │  ← Filter: Today/Weekly/Monthly
├─────────────────────────────────────────────┤
│    Expenses    │    Profit    │    Trips    │
│    ₹15.2K      │    ₹8.5K     │      12     │
├─────────────────────────────────────────────┤
│ [+ Trip Cost]         [+ Vehicle Cost]      │
└─────────────────────────────────────────────┘
```

**Empty State (No Fleet):**
```
┌─────────────────────────────────────────────┐
│ Get started with your fleet                 │
│ Add vehicles and create trips to track costs│
│ [Add Vehicle]         [Create Trip]         │
└─────────────────────────────────────────────┘
```

---

### 4. Pending Payments Section

**Purpose:** Show outstanding customer payments

**Data Source:** `PendingPayments` from `/dashboard/pending-payments` API

**Visibility Condition:**
```kotlin
tripSummary.total > 0 && (pendingPayments.isNotEmpty() || totalPendingAmount > 0 || isLoading)
```

**UI Elements:**
```
┌─────────────────────────────────────────────┐
│ 💰 Pending Payments              [₹25,000]  │
├─────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────┐ │
│ │ ABC Corp                      ₹10,000   │ │
│ │ UP64AB1234 • Pune → Delhi   3d overdue  │ │
│ └─────────────────────────────────────────┘ │
│ [View all 5 pending payments →]             │
└─────────────────────────────────────────────┘
```

**Success State (All Paid):**
```
┌─────────────────────────────────────────────┐
│ ✅ All payments collected!                  │
│    Great job! No outstanding payments       │
└─────────────────────────────────────────────┘
```

---

### 5. Vehicle Status Section

**Purpose:** Show vehicle availability breakdown

**Data Source:** `VehicleStatusSummary`

**UI Elements:**
```
┌─────────────────────────────────────────────┐
│ 🚛 Vehicles                    12 total  ➜  │
├─────────────────────────────────────────────┤
│ ┌───────────┐ ┌───────────┐ ┌───────────┐  │
│ │  On Route │ │  Planned  │ │ Available │  │
│ │     3     │ │     2     │ │     5     │  │
│ └───────────┘ └───────────┘ └───────────┘  │
└─────────────────────────────────────────────┘
```

**Empty State:**
```
┌─────────────────────────────────────────────┐
│ 🚛 No vehicles yet                          │
│    Add your first vehicle to start tracking │
│                              [Add Vehicle]  │
└─────────────────────────────────────────────┘
```

---

### 6. Trips Status Section

**Purpose:** Show trip status breakdown with active trips preview

**Data Source:** `TripSummary`, `OngoingTrips`

**UI Elements:**
```
┌─────────────────────────────────────────────┐
│ 🗺️ Trips                       25 total  ➜  │
├─────────────────────────────────────────────┤
│ ┌───────────┐ ┌───────────┐ ┌───────────┐  │
│ │  Active   │ │  Planned  │ │   Done    │  │
│ │     5     │ │     8     │ │    12     │  │
│ └───────────┘ └───────────┘ └───────────┘  │
├─────────────────────────────────────────────┤
│ Active Trips                                │
│ ┌─────────────────────────────────────────┐ │
│ │ 🚛 UP64AB1234         IN_PROGRESS       │ │
│ │ Pune → Delhi          John Doe          │ │
│ └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
```

**Empty State:**
```
┌─────────────────────────────────────────────┐
│ 🗺️ No trips yet                             │
│    Create your first trip to get started    │
│                              [Create Trip]  │
└─────────────────────────────────────────────┘
```

---

### 7. Alerts Section

**Purpose:** Show important alerts, warnings, and document status

**Data Sources:**
- `AlertsSummary` from `/dashboard/alerts-status` API
- `DocumentStats` from main dashboard API
- `VehicleStatusSummary` for missing docs detection

**Alert Types:**
| Type | Icon | Color | Shows |
|------|------|-------|-------|
| DOCUMENT_EXPIRY | 📄 | Error | Vehicle registration |
| LICENSE_EXPIRY | 📋 | Error | Driver name |
| MAINTENANCE | 🔧 | Warning | Vehicle info |
| Missing Documents | 📄 | Error | Count of vehicles |

**UI Elements:**
```
┌─────────────────────────────────────────────┐
│ ⚠️ Alerts                              [5]  │
├─────────────────────────────────────────────┤
│ [Critical: 2] [Warning: 2] [Info: 1]        │  ← Priority badges
├─────────────────────────────────────────────┤
│ 📄 2 Docs Expired  📋 1 License 7d          │  ← Expiry chips
├─────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────┐ │
│ │ 📄 Missing Documents                    │ │  ← Missing docs warning
│ │    2 vehicle(s) need documents uploaded │ │
│ └─────────────────────────────────────────┘ │
├─────────────────────────────────────────────┤
│ 📄 Document Expiry - RC          [2d left]  │  ← Alert item
│    🚛 UP64AB1234                            │
│ 📋 License Expiry                [5d left]  │
│    👤 John Doe                              │
└─────────────────────────────────────────────┘
```

**Success State:**
```
┌─────────────────────────────────────────────┐
│ ✅ All clear!                               │
│    No alerts at this time                   │
└─────────────────────────────────────────────┘
```

**Alert Detection Logic:**
```kotlin
// Missing documents: vehicles exist but fewer docs than vehicles
val hasMissingDocuments = vehicleStatus.total > 0 && totalDocs < vehicleStatus.total

// Document issues: expired or expiring
val hasDocumentIssues = expiredDocs > 0 || expiringDocs > 0

// Only "All clear" when truly no issues
val hasNoAlerts = displayAlerts.isEmpty() &&
                  alertsSummary.totalAlerts == 0 &&
                  !hasMissingDocuments &&
                  !hasDocumentIssues
```

---

### 8. Drivers Status Section

**Purpose:** Show driver availability breakdown

**Data Source:** `DriverStatusSummary`

**UI Elements:**
```
┌─────────────────────────────────────────────┐
│ 👨‍✈️ Drivers                     8 total  ➜  │
├─────────────────────────────────────────────┤
│ ┌───────────┐ ┌───────────┐ ┌───────────┐  │
│ │  On Route │ │  Planned  │ │ Available │  │
│ │     2     │ │     1     │ │     5     │  │
│ └───────────┘ └───────────┘ └───────────┘  │
└─────────────────────────────────────────────┘
```

**Empty State:**
```
┌─────────────────────────────────────────────┐
│ 👨‍✈️ No drivers yet                          │
│    Add your first driver to get started     │
│                               [Add Driver]  │
└─────────────────────────────────────────────┘
```

---

## API Dependencies

| Section | API Endpoint | Method |
|---------|--------------|--------|
| Main Dashboard | `/dashboard` | GET |
| Cost Overview | `/dashboard/cost-overview?filter=today\|weekly\|monthly` | GET |
| Pending Payments | `/dashboard/pending-payments` | GET |
| Alerts Status | `/dashboard/alerts-status` | GET |

---

## State Management

```kotlin
data class State(
    val isLoading: Boolean = false,
    val stats: DashboardStats = DashboardStats(),
    val error: String? = null,
    
    // Cost Overview
    val costOverview: CostOverview = CostOverview(),
    val selectedCostFilter: CostOverviewFilter = CostOverviewFilter.TODAY,
    val isLoadingCostOverview: Boolean = false,
    
    // Pending Payments
    val pendingPayments: List<PendingPayment> = emptyList(),
    val totalPendingAmount: Double = 0.0,
    val isLoadingPendingPayments: Boolean = false,
    
    // Status Summaries
    val vehicleStatus: VehicleStatusSummary = VehicleStatusSummary(),
    val driverStatus: DriverStatusSummary = DriverStatusSummary(),
    val tripSummary: TripSummary = TripSummary(),
    
    // Alerts
    val alertsSummary: AlertsSummary = AlertsSummary(),
    val isLoadingAlertsSummary: Boolean = false,
    
    // User info
    val userName: String = "",
    val lastUpdated: String? = null
) : UiState
```

---

## Intents

```kotlin
sealed interface Intent : UiIntent {
    data object LoadDashboard : Intent
    data object RefreshDashboard : Intent
    
    // Navigation
    data object NavigateToVehicles : Intent
    data object NavigateToDrivers : Intent
    data object NavigateToTrips : Intent
    data object NavigateToMaps : Intent
    
    // Cost actions
    data class ChangeCostFilter(val filter: CostOverviewFilter) : Intent
    data object NavigateToAddTripCost : Intent
    data object NavigateToAddVehicleCost : Intent
    
    // Empty state actions
    data object NavigateToAddVehicle : Intent
    data object NavigateToAddDriver : Intent
    data object NavigateToCreateTrip : Intent
    
    // Alert actions
    data class DismissAlert(val alertId: String) : Intent
}
```

---

## Effects

```kotlin
sealed interface Effect : UiEffect {
    data object NavigateToVehicles : Effect
    data object NavigateToDrivers : Effect
    data object NavigateToTrips : Effect
    data object NavigateToMaps : Effect
    data object NavigateToAddTripCost : Effect
    data object NavigateToAddVehicleCost : Effect
    data object NavigateToAddVehicle : Effect
    data object NavigateToAddDriver : Effect
    data object NavigateToCreateTrip : Effect
    data class ShowSnackbar(val message: String) : Effect
}
```

