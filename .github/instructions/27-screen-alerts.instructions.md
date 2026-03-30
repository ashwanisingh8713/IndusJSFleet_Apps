# screen-alerts — IndusJS Fleet

## Purpose

Alerts feature: document expiry, license expiry, maintenance due alerts list.
Data layer in `ijs-network-lib`.

## Package: `com.ijs.alerts`

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| AlertsListScreen | `AlertsList` | All active alerts |

## Key Files

| File | Purpose |
|------|---------|
| `presentation/AlertsFeatureFacade.kt` | Facade — 1 entry point |
| `presentation/AlertsListContract.kt` | State/Intent/Effect |
| `presentation/AlertsListViewModel.kt` | ViewModel |
| `presentation/AlertsListScreen.kt` | UI |

## Alert Types

- **Document Expiry** — RC, Insurance, Fitness, Permit, PUC
- **License Expiry** — Driver license nearing expiry
- **Maintenance Due** — Scheduled maintenance overdue

## Module Path

`screen-alerts/src/commonMain/kotlin/com/ijs/alerts/`

## Depends On: `ijs-network-lib`

