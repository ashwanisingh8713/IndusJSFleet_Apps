# AGENTS.md — screen-dashboard

## Purpose

**Dashboard** feature module — the main landing screen after login. Displays fleet overview, cost overview, financial summary (Owner/GM only), vehicle/driver/trip status summaries, alerts, and quick action buttons. Split into section components for maintainability.

**Package:** `com.ijs.dashboard`
**Targets:** Android, iOS, JS, WasmJS

---

## Source Tree

```
src/commonMain/kotlin/com/ijs/dashboard/
├── LogTags.kt
└── presentation/
    ├── DashboardContract.kt               # MVI contract (State/Intent/Effect)
    ├── DashboardFeatureFacade.kt          # DI entry point
    ├── DashboardScreen.kt                 # Main dashboard layout with navigation drawer
    ├── DashboardViewModel.kt              # Loads dashboard stats, financial summary, alerts
    └── components/
        ├── AlertsSection.kt              # Document/license expiry alerts
        ├── CostOverviewSection.kt        # Today/weekly/monthly cost summary cards
        ├── DashboardSharedComponents.kt  # Shared dashboard UI helpers
        ├── FleetOverviewSection.kt       # Vehicle/driver/trip count cards
        ├── NavigationDrawerContent.kt    # Side drawer with feature navigation links
        ├── TripsSection.kt               # Recent/active trips summary
        └── VehicleDriverSections.kt      # Vehicle + driver status summaries
```

---

## Module Dependencies

| Kind | Dependency |
|------|------------|
| api | `:ijs-network-lib` |
| libs | kotlinx-coroutines, kotlinx-serialization, kermit |

**Presentation-only** — No `data/` or `domain/` layers. Uses `DashboardRepository` and `UserRepository` from `ijs-network-lib` directly.

---

## Screen

| Screen | Route | Description |
|--------|-------|-------------|
| DashboardScreen | `Dashboard` | Main landing screen with drawer navigation |

---

## Dashboard Sections

| Section | Component | Visible To |
|---------|-----------|-----------|
| Fleet Overview | `FleetOverviewSection` | All roles |
| Cost Overview | `CostOverviewSection` | All roles |
| Financial Summary | Part of `DashboardScreen` | Owner, GM only |
| Vehicle Status | `VehicleDriverSections` | All roles |
| Driver Status | `VehicleDriverSections` | All roles |
| Trips | `TripsSection` | All roles |
| Alerts | `AlertsSection` | All roles |
| Navigation Drawer | `NavigationDrawerContent` | All roles (items filtered by role) |

---

## Key Patterns

- **Role-based visibility** — Financial summary (revenue, expenses, profit) is hidden for Manager and Supervisor roles. The ViewModel checks `UserRole` from `UserLocalDataSource`.
- **Navigation drawer** — `NavigationDrawerContent` provides the app's primary navigation. Menu items are filtered by role (e.g., Reports only visible to Owner/GM).
- **Section decomposition** — Each dashboard section is a separate composable file under `components/` for maintainability.
- **Offline-first** — `DashboardRepository` in `ijs-network-lib` supports cached data via `DashboardLocalDataSource` (Room impl in `sharedUI`).
