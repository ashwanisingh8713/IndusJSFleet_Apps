# Application Modules Overview

## Overview

IndusJS Fleet is organized into feature modules following Clean Architecture principles. Each module is self-contained with its own domain, data, and presentation layers.

---

## Module Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Application Modules                          │
├─────────────────────────────────────────────────────────────────┤
│ Auth │ Dashboard │ Vehicles │ Drivers │ Trips │ Payments │ ...  │
├─────────────────────────────────────────────────────────────────┤
│                        Core Module                               │
│       (MVI, Network, Error, Utils, UI Components, Theme)         │
├─────────────────────────────────────────────────────────────────┤
│                     Shared Libraries                             │
│    (ijs-error-lib, ijs-dispatcher-lib, ijs-datetime-picker)     │
└─────────────────────────────────────────────────────────────────┘
```

---

## Module List

| Module | Description | Documentation |
|--------|-------------|---------------|
| Auth | User authentication & session management | [auth/](./auth/) |
| Dashboard | Fleet overview & statistics | [dashboard/](./dashboard/) |
| Vehicles | Vehicle lifecycle management | [vehicles/](./vehicles/) |
| Drivers | Driver management & assignments | [drivers/](./drivers/) |
| Trips | Trip planning & execution | [trips/](./trips/) |
| Payments | Payment recording & tracking | [payments/](./payments/) |
| Customers | Customer management | [customers/](./customers/) |
| Costs | Cost tracking & entry | [costs/](./costs/) |
| Team | Team member management | [team/](./team/) |
| Reports | Analytics & P&L reports | [reports/](./reports/) |
| Alerts | Document/license expiry alerts | [alerts/](./alerts/) |

---

## Core Module

The Core module provides foundational components used by all feature modules.

### Components

| Component | Description |
|-----------|-------------|
| **MVI** | Base MviViewModel, UiState, UiIntent, UiEffect |
| **Network** | HttpClient factory, ApiConfig, AuthInterceptor |
| **Error** | FleetException hierarchy, ErrorContext |
| **Constants** | StatusConstants, CargoTypes, CostTypes |
| **Utils** | TimeUtils, ValidationUtils, Extensions |
| **UI** | Reusable Compose components |

### Folder Structure

```
core/
├── mvi/                 # MVI base classes
├── network/             # Network configuration
├── constants/           # App-wide constants
├── util/                # Utility functions
└── ui/                  # Reusable UI components
```

---

## Shared Libraries

| Library | Description |
|---------|-------------|
| **ijs-error-lib** | Error handling with Result sealed class and FleetException hierarchy |
| **ijs-dispatcher-lib** | Coroutine dispatcher management for multiplatform |
| **ijs-datetime-picker** | Cross-platform date/time picker component |
| **ijs-datetime-utils** | Date/time calculation and formatting utilities |

---

## Module Dependencies

```
                         ┌─────────────┐
                         │    Core     │
                         └──────┬──────┘
                                │
         ┌──────────────────────┼──────────────────────┐
         │                      │                      │
    ┌────▼────┐           ┌─────▼─────┐          ┌─────▼─────┐
    │  Auth   │           │ Dashboard │          │ Settings  │
    └────┬────┘           └─────┬─────┘          └───────────┘
         │                      │
         │    ┌─────────────────┼─────────────────┐
         │    │                 │                 │
         │ ┌──▼───┐        ┌────▼────┐       ┌────▼────┐
         │ │Vehicles│      │ Drivers │       │  Trips  │
         │ └───┬───┘       └────┬────┘       └────┬────┘
         │     │                │                 │
         │     │          ┌─────▼─────┐           │
         │     └──────────│ Customers │───────────┘
         │                └─────┬─────┘
         │                      │
         │                ┌─────▼─────┐
         └────────────────│ Payments  │
                          └─────┬─────┘
                                │
                          ┌─────▼─────┐
                          │  Reports  │
                          └───────────┘
```

---

## Related Documentation

- [User Roles & Permissions](../user-roles/README.md)
- [Architecture Guide](../architecture/README.md)
- [API Reference](../postman_collections/Fleet_Management_API_v2.postman_collection.json)
