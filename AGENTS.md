# AGENTS.md - IndusJS Fleet (Root)

## About the App

**IndusJS Fleet** is a comprehensive **Fleet Management System** for transportation/logistics businesses. It enables fleet owners to manage vehicles, drivers, trips, costs, payments, customers, team members, and vehicle finances. Built with **Kotlin Multiplatform**, targeting **Android, iOS, and Web** (JS + WasmJS).

### Business Domain

| Domain Area | What It Does |
|-------------|-------------|
| Fleet Operations | Track vehicles (trucks, vans) — status, maintenance, documents, real-time GPS location |
| Driver Management | Profiles, licenses, assignments, costs (salary, advance, bonus, penalty) |
| Trip Planning | Route planning (Google Places), cargo, scheduling, pricing, state machine |
| Cost Tracking | Trip costs (fuel, tolls, etc.), maintenance costs (tyre, battery, etc.), driver costs |
| Payment Collection | Customer payments for trips — cash, UPI, bank transfer, cheque, card |
| Financial Reporting | Profit/Loss by vehicle, by trip, consolidated; cost analysis with date ranges |
| Vehicle Finance | Purchase records, loan tracking, EMI payments |
| Real-time Tracking | GPS location via MQTT (separate locationTracker APK publishes, fleet app subscribes) |

### User Roles (Hierarchical)

| Role | Access Level |
|------|-------------|
| **Owner** | Full access — financials, team management, all CRUD operations |
| **General Manager** | Financial access, manage Managers/Supervisors, all operations |
| **Manager** | Operational access — create trips/costs, **no** financial data (no trip_price, no P&L) |
| **Supervisor** | Limited — view-only for most features, can update trip status only |

---

## Project Modules

```
IndusJSFleet/
├── sharedUI/           # ★ CORE: All shared UI + business logic (KMP)
├── androidApp/         # Android entry point (thin shell)
├── webApp/             # Web entry point (thin shell)
├── iosApp/             # iOS entry point (Xcode project)
├── ijs-error-lib/      # Error handling: Result<T>, exception hierarchy, ErrorHandler
├── ijs-dispatcher-lib/ # Coroutine dispatchers: DispatcherProvider, test helpers
├── ijs-datetime-utils/ # Date/time utilities: FleetDateTime object
├── ijs-datetime-picker/# Compose date/time picker component
├── ijs-pdf-report/     # PDF report generation (HTML → PDF)
└── locationTracker/    # Android-only GPS tracking app (separate APK)
```

### Module Dependency Graph

```
androidApp ──→ sharedUI
webApp ─────→ sharedUI
iosApp ─────→ sharedUI (via framework)

sharedUI ──→ ijs-error-lib
sharedUI ──→ ijs-dispatcher-lib
sharedUI ──→ ijs-datetime-picker ──→ ijs-datetime-utils
sharedUI ──→ ijs-datetime-utils
sharedUI ──→ ijs-pdf-report ──→ ijs-datetime-utils

locationTracker (standalone Android app, no dependencies on sharedUI)
```

---

## Architecture: Clean Architecture + MVI + Metro DI + Navigation 3

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         PRESENTATION LAYER                              │
│  Screen (Compose) ←→ ViewModel (MviViewModel) ←→ Contract (State/      │
│                                                    Intent/Effect)       │
└──────────────────────────────────┬──────────────────────────────────────┘
                                   ↓ Uses
┌──────────────────────────────────┴──────────────────────────────────────┐
│                           DOMAIN LAYER                                  │
│         Entities ←→ Use Cases ←→ Repository Interfaces                  │
└──────────────────────────────────┬──────────────────────────────────────┘
                                   ↓ Implements
┌──────────────────────────────────┴──────────────────────────────────────┐
│                            DATA LAYER                                   │
│     DTOs (@Serializable) ←→ Mappers ←→ DataSources ←→ Repository Impls │
└─────────────────────────────────────────────────────────────────────────┘
```

### Data Flow (Request Lifecycle)

```
User taps button
  → Screen calls viewModel.sendIntent(Intent.LoadData)
    → ViewModel.handleIntent() routes to handler function
      → handler calls useCase() → returns Flow<Result<T>>
        → UseCase calls repository.getData() → Flow<Result<T>>
          → Repository:
              1. emit(Result.Loading)
              2. token = userLocalDataSource.getAuthToken()
              3. response = remoteDataSource.apiCall(token)
              4. if success → emit(Result.Success(mapper.toDomain(dto)))
              5. if error → emit(Result.Error(exception, message))
    → ViewModel collects flow → updateState { copy(data = result.data) }
  → Screen observes state via collectAsStateWithLifecycle()
  → UI recomposes with new data
```

---

## All Screens (40+)

### Authentication Flow
| Screen | Route | Description |
|--------|-------|-------------|
| Login | `Login` | Email/password → Dashboard |
| SignUp | `SignUp` | Owner registration → Dashboard |
| ForgotPassword | `ForgotPassword` | Email reset link |
| Profile | `Profile` | View/edit profile, logout |
| ChangePassword | `ChangePassword` | Update password |

### Dashboard
| Screen | Route | Description |
|--------|-------|-------------|
| Dashboard | `Dashboard` | Fleet overview, cost overview, financial summary, alerts, quick actions |

Dashboard sections: Fleet Overview (vehicle/driver/trip counts), Cost Overview (today/weekly/monthly), Financial Summary (Owner/GM only — revenue, expenses, profit), Vehicle/Driver/Trip status summaries, Alerts (document/license expiry), Quick Actions.

### Vehicles
| Screen | Route | Description |
|--------|-------|-------------|
| VehiclesList | `Vehicles` | Search, filter by status, vehicle cards |
| VehicleDetail | `VehicleDetail(id)` | Tabs: Overview, Trips, Documents, Costs |
| AddVehicle | `AddVehicle` | Register with documents (RC, Insurance, etc.) |
| MaintenanceCostEntry | `MaintenanceCostEntry(vehicleId?)` | Record maintenance expense |

Vehicle states: `inactive → active → on_route → maintenance → damaged → decommissioned`

### Drivers
| Screen | Route | Description |
|--------|-------|-------------|
| DriversList | `Drivers` | Search, filter by status |
| DriverDetail | `DriverDetail(id)` | Info, license, trip history, costs |
| CreateDriver | `CreateDriver` | Register with license info |
| DriverCostEntry | `DriverCostEntry(driverId?)` | Record driver cost (salary, advance, etc.) |

Driver states: `inactive → active → on_route → on_leave → suspended → terminated`

### Trips
| Screen | Route | Description |
|--------|-------|-------------|
| TripsList | `Trips` | Filter by status (planned, on_route, completed, etc.) |
| TripDetail | `TripDetail(id)` | Route, cargo, schedule, costs, payments |
| CreateTrip | `CreateTrip` | Vehicle + driver + route (Places API) + schedule + cargo + customer |
| TripCostEntry | `TripCostEntry(tripId?, vehicleId?)` | Record trip expense |

Trip states: `planned → on_route → completed` (or `cancelled`, `failed`, `delayed`)

### Customers
| Screen | Route | Description |
|--------|-------|-------------|
| CustomersList | `Customers` | Search customers |
| CustomerDetail | `CustomerDetail(id)` | Info, trip history, financials |
| CreateCustomer | `CreateCustomer` | Company + contact + GST + address |

### Payments
| Screen | Route | Description |
|--------|-------|-------------|
| PaymentsList | `Payments` | All payments with filters |
| PaymentDetail | `PaymentDetail(id)` | Payment details |
| AddPayment | `AddPayment(tripId?, vehicleId?)` | Record payment (mode, amount, reference) |
| EditPayment | `EditPayment(id)` | Update existing payment |

Payment status: `received`, `pending`, `cancelled`. Modes: cash, upi, bank_transfer, cheque, card.

### Reports (Owner/GM only)
| Screen | Route | Description |
|--------|-------|-------------|
| ReportsHub | `Reports` | Summary with period filter |
| VehicleProfitLoss | `VehicleProfitLoss` | Revenue vs expenses per vehicle |
| TripProfitLoss | `TripProfitLoss` | Revenue vs expenses per trip |
| CostAnalysis | `CostAnalysis` | Breakdown by cost type |
| ConsolidatedPL | `ConsolidatedPL` | Overall P&L statement |

Report periods: today, weekly, 15 days, monthly, quarterly, half-yearly, yearly, custom.

### Team Management
| Screen | Route | Description |
|--------|-------|-------------|
| TeamList | `TeamList` | All team members |
| TeamMemberDetail | `TeamMemberDetail(id)` | Member details + permissions |
| CreateTeamMember | `CreateTeamMember` | Add GM/Manager/Supervisor |

### Vehicle Finance
| Screen | Route | Description |
|--------|-------|-------------|
| VehicleFinanceList | `VehicleFinance` | All vehicles with finance status |
| VehicleFinanceDetail | `VehicleFinanceDetail(id)` | Purchase info, loan summary |
| AddPurchaseInfo | `AddPurchaseInfo` | Record purchase (cash/loan) |
| EditPurchaseInfo | `EditPurchaseInfo(id)` | Update purchase info |
| EmiPaymentHistory | `EmiPaymentHistory(id)` | EMI payments list |

### Maps & Alerts
| Screen | Route | Description |
|--------|-------|-------------|
| MapsScreen | `Maps` | Real-time vehicle tracking (MQTT subscribe) |
| AlertsList | `AlertsList` | Document expiry, license expiry, maintenance due |

---

## Navigation Flow

```
Login ──→ Dashboard ──┬──→ Vehicles ──→ VehicleDetail ──→ (Docs, Costs, Trips tabs)
                      ├──→ Drivers  ──→ DriverDetail
                      ├──→ Trips    ──→ TripDetail ──→ (Costs, Payments tabs)
                      ├──→ Customers ──→ CustomerDetail
                      ├──→ Payments ──→ PaymentDetail
                      ├──→ Reports  ──→ VehiclePL / TripPL / CostAnalysis / ConsolidatedPL
                      ├──→ Team     ──→ TeamMemberDetail
                      ├──→ Finance  ──→ VehicleFinanceDetail ──→ EmiPaymentHistory
                      ├──→ Maps
                      ├──→ Alerts
                      ├──→ TripCostEntry (quick add)
                      ├──→ MaintenanceCostEntry (quick add)
                      ├──→ DriverCostEntry (quick add)
                      └──→ Profile ──→ ChangePassword
```

---

## Entity State Machines

### Vehicle States
```
inactive ←→ active → on_route → active
                  → maintenance ←→ damaged → decommissioned
```

### Trip States
```
planned → on_route → completed
planned → cancelled
on_route → failed
on_route → delayed
```

### Driver States
```
inactive ←→ active → on_route → active
                   → on_leave → active
                   → suspended → active
                   → terminated (terminal)
```

---

## API Configuration

**Base URL:** `https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api/v2`

### Date Format Rules

| API Context | Date Format | Time Format | How to Convert |
|------------|-------------|-------------|----------------|
| Trip scheduling | ISO 8601 (`2026-03-14T15:30:00Z`) | Part of ISO | `FleetDateTime.toIso8601(date, time)` |
| Cost entries | `DD-MM-YYYY` | `HH:MM` | Send as-is from UI |
| Document expiry | `DD-MM-YYYY` | N/A | Send as-is |
| UI display | `DD-MM-YYYY` | `HH:MM` (24hr) | Default format |

---

## Technology Stack

| Category | Technology | Version |
|----------|-----------|---------|
| Language | Kotlin | 2.3.0 |
| UI Framework | Compose Multiplatform | 1.10.0 |
| Design System | Material 3 | 1.10.0-alpha05 |
| Networking | Ktor Client | 3.3.3 |
| DI Framework | Metro (ZacSweers) | 0.9.1 |
| Navigation | Navigation 3 | 1.1.0-alpha01 |
| Serialization | kotlinx-serialization | 1.9.0 |
| Local Storage | Room + multiplatform-settings | 2.8.4 / 1.3.0 |
| Date/Time | kotlinx-datetime | 0.7.1 |
| Logging | Kermit | 2.0.8 |
| Theming | MaterialKolor | 4.0.5 |
| PDF | ijs-pdf-report (internal) | - |
| Location | Google Places + Distance Matrix API | - |
| Tracking | MQTT (HiveMQ) via locationTracker | - |
| Crash Reporting | Firebase Crashlytics | BOM-managed |

---

## Build Commands

```bash
./gradlew :androidApp:assembleDebug              # Android debug APK
./gradlew :androidApp:assembleRelease             # Android release APK
./gradlew :webApp:jsBrowserDevelopmentRun          # Web JS dev server
./gradlew :webApp:wasmJsBrowserDevelopmentRun      # Web WASM dev server
./gradlew :locationTracker:assembleDebug           # Location tracker APK
```

---

## Key Files Reference

| Purpose | File Path |
|---------|-----------|
| App entry (Compose root) | `sharedUI/.../App.kt` |
| Route definitions (40+ routes) | `sharedUI/.../navigation/FleetRoute.kt` |
| Navigation wiring | `sharedUI/.../navigation/FleetNavigation.kt` |
| DI container (manual) | `sharedUI/.../di/DefaultViewModelProvider.kt` |
| ViewModel interface | `sharedUI/.../di/ViewModelProvider.kt` |
| API endpoints | `sharedUI/.../core/network/ApiConfig.kt` |
| Error extraction | `sharedUI/.../core/network/ApiErrorHandler.kt` |
| Entity states | `sharedUI/.../core/constants/StatusConstants.kt` |
| Auth events (401) | `sharedUI/.../core/auth/AuthenticationManager.kt` |
| Base MVI ViewModel | `sharedUI/.../core/mvi/MviViewModel.kt` |
| App initializer | `sharedUI/.../core/init/AppInitializer.kt` |
| Root DI graph | `sharedUI/.../di/RootGraph.kt` |

---

## Adding a New Feature Checklist

1. **Domain entity** → `domain/entity/{feature}/`
2. **Repository interface** → `domain/repository/{feature}/`
3. **Use cases** → `domain/usecase/{feature}/`
4. **DTOs** → `data/model/{feature}/` (with `@Serializable` + `@SerialName`)
5. **Mapper** → `data/mapper/{feature}/`
6. **DataSource interface + impl** → `data/datasource/{feature}/`
7. **Repository impl** → `data/repository/{feature}/`
8. **Contract** (State, Intent, Effect) → `presentation/{feature}/`
9. **ViewModel** → `presentation/{feature}/`
10. **Screen** → `presentation/{feature}/`
11. **DI Graph** → `di/{Feature}FeatureGraph.kt`
12. **Wire in DefaultViewModelProvider** → `di/DefaultViewModelProvider.kt`
13. **Route** → `navigation/FleetRoute.kt`
14. **NavEntry** → `navigation/FleetNavigation.kt`

---

## Documentation

- `.github/copilot-instructions.md` — Coding conventions + architecture guide
- `.github/prompts/` — Feature implementation prompt templates
- `Docs/api_modules/` — API endpoint documentation
- `Docs/API_DATABASE_TABLEs/` — Database schema documentation
- `Docs/architecture/` — Architecture decisions
- `sharedUI/AGENTS.md` — Detailed sharedUI module guide
- Each module has its own `AGENTS.md` with module-specific context
