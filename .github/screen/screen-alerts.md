# screen-alerts

## Overview

**Package:** `com.ijs.alerts`
**Module type:** Presentation-only feature module
**Purpose:** Displays fleet-wide alerts for document expirations, license expirations, maintenance reminders, and system notifications. Provides filtering by priority (critical, warning, info) and type (documents, licenses).

---

## Architecture

| Layer | Contents |
|-------|----------|
| **Presentation** | `AlertsListContract`, `AlertsListViewModel`, `AlertsListScreen`, `AlertsFeatureFacade` |
| **Domain** | None — uses `GetAlertsStatusUseCase` from `ijs-network-lib` |
| **Data** | None — data layer lives in `ijs-network-lib` (dashboard data sources) |

This is a **presentation-only** module. It depends on `ijs-network-lib` for the `GetAlertsStatusUseCase` and dashboard domain entities (`Alert`, `AlertsSummary`, `AlertPriority`, `AlertType`).

---

## Dependencies

```
screen-alerts → ijs-network-lib → ijs-core-lib → ijs-error-lib, ijs-dispatcher-lib, ijs-datetime-utils
```

- `api(project(":ijs-network-lib"))` — dashboard use cases, alert entities
- `ijs-ui-components-lib` — `LoadingContent`, `ErrorContent`, `EmptyContent`, icons

---

## Screens

### AlertsListScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.AlertsList` |
| ViewModel | `AlertsListViewModel` |
| Contract | `AlertsListContract` |

**Features:**
- Alert summary card with counts by priority (critical/warning/info)
- Detailed breakdown: documents expired, documents expiring soon, licenses expired, licenses expiring
- Filter chips: All, Critical, Warning, Documents, Licenses
- Alert cards with color-coded priority badges, entity info (vehicle reg / driver name), expiry countdown
- Client-side alert dismissal
- Pull-to-refresh

**State highlights:**
- `alerts: List<Alert>` — all alerts from API
- `alertsSummary: AlertsSummary` — aggregated counts
- `selectedFilter: AlertFilter` — current filter (ALL, CRITICAL, WARNING, INFO, DOCUMENTS, LICENSES)
- `filteredAlerts` — computed property filtering by selected filter

**Intents:**
- `LoadAlerts` — initial data load
- `RefreshAlerts` — pull-to-refresh
- `ChangeFilter(filter)` — switch filter chip
- `DismissAlert(alertId)` — client-side dismiss
- `NavigateBack` — back navigation

**Effects:**
- `ShowSnackbar(message)` — success/info toasts
- `ShowError(message)` — error display
- `NavigateBack` — triggers `onNavigateBack` lambda

---

## Facade

```kotlin
object AlertsFeatureFacade {
    fun AlertsListEntry(viewModel, onNavigateBack)
}
```

**Navigation callbacks:** `onNavigateBack: () -> Unit`

---

## Data Flow

```
Dashboard "View All Alerts" → FleetRoute.AlertsList
  → sharedUI creates AlertsListViewModel via DI
    → VM calls GetAlertsStatusUseCase (from ijs-network-lib)
      → DashboardRemoteDataSource.getAlertsStatus(token)
        → GET /dashboard/alerts-status
    → State updated with alerts + summary
  → AlertsListScreen renders with filter chips and alert cards
```

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/dashboard/alerts-status` | GET | Fetches alert summary with document/license expiry details |

---

## Inter-Module Communication

- **No cross-feature dependencies.** All data comes from `ijs-network-lib`.
- Navigation is lambda-based; `sharedUI` maps `onNavigateBack` to `backStack.removeLastOrNull()`.

