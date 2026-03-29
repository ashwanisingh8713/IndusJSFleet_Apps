# screen-dashboard

## Overview

**Package:** `com.ijs.dashboard`
**Module type:** Presentation-only feature module
**Purpose:** Main app landing screen after login. Provides a consolidated fleet overview with statistics, cost summaries, financial data (role-restricted), alerts, quick action buttons, and a navigation drawer for accessing all features.

---

## Architecture

| Layer | Contents |
|-------|----------|
| **Presentation** | `DashboardContract`, `DashboardViewModel`, `DashboardScreen`, `DashboardFeatureFacade`, component files |
| **Domain** | None — uses use cases from `ijs-network-lib` |
| **Data** | None — data layer lives in `ijs-network-lib` (dashboard data sources) |

**Component files (refactored for 500-line limit):**
- `AlertsSection.kt` — alerts summary and navigation
- `CostOverviewSection.kt` — trip/maintenance/driver cost cards with period filter
- `DashboardSharedComponents.kt` — reusable stat cards, section headers
- `FleetOverviewSection.kt` — vehicle/driver/trip count cards
- `NavigationDrawerContent.kt` — hamburger menu with all navigation items
- `TripsSection.kt` — trip status summary
- `VehicleDriverSections.kt` — vehicle/driver status breakdowns

---

## Dependencies

```
screen-dashboard → ijs-network-lib → ijs-core-lib
```

No cross-feature module dependencies. All data from `ijs-network-lib` dashboard use cases.

---

## Screens

### DashboardScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.Dashboard` |
| ViewModel | `DashboardViewModel` |
| Contract | `DashboardContract` |

**Sections (in scroll order):**
1. **Fleet Overview** — Vehicle count, Driver count, Active trips count
2. **Cost Overview** — Today/Weekly/Monthly/Quarterly cost breakdown (trip costs, maintenance costs, driver costs) with filter chips
3. **Financial Summary** (Owner/GM only) — Total revenue, expenses, net profit for selected period
4. **Vehicle Status** — Count per status (active, on_route, maintenance, etc.)
5. **Driver Status** — Count per status (active, on_route, on_leave, etc.)
6. **Trip Summary** — Planned, in-progress, completed trip counts
7. **Alerts** — Critical/warning alert counts with "View All" navigation
8. **Quick Actions** — FABs for Add Trip Cost, Add Maintenance Cost, Add Driver Cost, Add Vehicle, Add Driver, Create Trip

**Navigation Drawer items:**
Dashboard, Vehicles, Drivers, Trips, Customers, Payments, Reports, Team, Vehicle Finance, Maps, Alerts, Profile

**State highlights:**
- `stats: DashboardStats` — fleet-wide counters
- `costOverview: CostOverview` — cost breakdown by type
- `financialSummary: FinancialSummary?` — revenue/expense/profit (null for Manager/Supervisor)
- `vehicleStatus: VehicleStatusSummary`, `driverStatus: DriverStatusSummary`, `tripSummary: TripSummary`
- `alertsSummary: AlertsSummary` — alert counts
- `selectedCostFilter: CostOverviewFilter` — TODAY, WEEKLY, MONTHLY, QUARTERLY
- `userName`, `userRole` — displayed in drawer header
- Role-based visibility: `canViewFinancials` computed from `userRole`

**Key Intents:** `LoadDashboard`, `RefreshDashboard`, `ChangeCostFilter`, `ChangeFinancialPeriod`, `LoadPendingPayments`, `LoadAlertsSummary`, Navigate*

---

## Facade

```kotlin
object DashboardFeatureFacade {
    fun DashboardEntry(
        viewModel,
        onNavigateToVehicles, onNavigateToDrivers, onNavigateToTrips,
        onNavigateToMaps, onNavigateToProfile, onNavigateToTeam,
        onNavigateToReports, onNavigateToAddTripCost, onNavigateToAddVehicleCost,
        onNavigateToAddVehicle, onNavigateToAddDriver, onNavigateToCreateTrip,
        onNavigateToAddDriverCost, onNavigateToAlertsList, onNavigateToCustomers,
        onNavigateToPayments, onNavigateToVehicleFinance
    )
}
```

**18 navigation callbacks** — all mapped to `FleetRoute` in `sharedUI/FleetNavigation.kt`.

---

## Use Cases (from ijs-network-lib)

| Use Case | Description |
|----------|-------------|
| `GetDashboardStatsUseCase` | Fleet overview counts |
| `GetCostOverviewUseCase` | Cost breakdown by period |
| `GetFinancialSummaryUseCase` | Revenue/expense/profit (Owner/GM) |
| `GetPendingPaymentsUseCase` | Outstanding payment list |
| `GetAlertsStatusUseCase` | Alert summary with counts |
| `GetUserInfoUseCase` | Current user name and role |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/dashboard` | GET | Fleet stats (vehicles, drivers, trips counts + status) |
| `/dashboard/cost-overview?filter=` | GET | Cost breakdown by period filter |
| `/dashboard/financial-summary?period=` | GET | Financial summary (Owner/GM only) |
| `/dashboard/pending-payments` | GET | Outstanding payment summary |
| `/dashboard/alerts-status` | GET | Alert counts and details |

---

## Role-Based Access

| Section | Owner | GM | Manager | Supervisor |
|---------|-------|-----|---------|------------|
| Fleet Overview | ✅ | ✅ | ✅ | ✅ |
| Cost Overview | ✅ | ✅ | ✅ | ❌ |
| Financial Summary | ✅ | ✅ | ❌ | ❌ |
| Quick Actions | ✅ | ✅ | ✅ (limited) | ❌ |
| Reports nav item | ✅ | ✅ | ❌ | ❌ |

