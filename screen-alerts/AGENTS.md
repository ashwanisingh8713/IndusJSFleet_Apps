# AGENTS.md — screen-alerts

## Purpose

**Alerts** feature module. Displays actionable alerts for document expiry (RC, insurance, permit), driver license expiry, and maintenance due dates. Presentation-only module — alert data comes from the dashboard API via `ijs-network-lib`.

**Package:** `com.ijs.alerts`
**Targets:** Android, iOS, JS, WasmJS

---

## Source Tree

```
src/commonMain/kotlin/com/ijs/alerts/
├── LogTags.kt
└── presentation/
    ├── AlertsFeatureFacade.kt             # DI entry point
    ├── AlertsListContract.kt              # MVI contract (State/Intent/Effect)
    ├── AlertsListScreen.kt                # Alerts list with category grouping
    └── AlertsListViewModel.kt             # Loads alerts, handles filtering
```

---

## Module Dependencies

| Kind | Dependency |
|------|------------|
| api | `:ijs-network-lib` |
| libs | kotlinx-coroutines, kotlinx-serialization, kermit |

**Minimal module** — Only 5 source files. Presentation-only with no data/domain layer.

---

## Screen

| Screen | Route | Description |
|--------|-------|-------------|
| AlertsListScreen | `AlertsList` | Document/license expiry + maintenance due alerts |

---

## Alert Types

| Type | Description |
|------|-------------|
| Document expiry | RC, insurance, permit, fitness certificate approaching/past expiry |
| License expiry | Driver licenses approaching/past expiry |
| Maintenance due | Vehicles due for scheduled maintenance |

---

## Key Patterns

- **Data from dashboard API** — Alerts are a subset of dashboard stats. The ViewModel calls `DashboardRepository` to get alert data.
- **Category grouping** — Alerts are grouped by type (documents, licenses, maintenance) with expandable sections.
