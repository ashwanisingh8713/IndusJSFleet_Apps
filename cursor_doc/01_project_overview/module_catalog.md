# Module Catalog

## All 22 Gradle Modules

```
IndusJSFleet/                           # Root project
├── sharedUI/                           # Core orchestration: Nav, DI, theming, Room DB — NO presentation code
├── androidApp/                         # Android entry point (thin shell)
├── webApp/                             # Web entry point (JS + WasmJS, thin shell)
├── iosApp/                             # iOS entry point (Xcode, SwiftUI host)
│
├── ijs-core-lib/                       # Foundation: MVI, constants, utilities, base contracts, shared DTOs
├── ijs-network-lib/                    # Networking: HTTP client, auth, user/dashboard/costs data layer
├── ijs-error-lib/                      # Error handling: Result<T>, exception hierarchy, ErrorHandler
├── ijs-dispatcher-lib/                 # Coroutine dispatchers: DispatcherProvider + platform actuals
├── ijs-datetime-utils/                 # Date/time utilities: FleetDateTime object
├── ijs-datetime-picker/                # Compose date/time picker component
├── ijs-logger-lib/                     # Logging: KermitFleetLogger, file logging, crash handler
├── ijs-ui-components-lib/              # Reusable UI components, theme (Color/Typography/Shapes), icons
├── ijs-pdf-report/                     # PDF report generation (HTML → PDF, platform-specific)
│
├── screen-vehicle/                     # Vehicle feature: data + domain + presentation
├── screen-driver/                      # Driver feature: data + domain + presentation
├── screen-trip/                        # Trip feature: data + domain + presentation
├── screen-customer/                    # Customer feature: data + domain + presentation
├── screen-payment/                     # Payment feature: data + domain + presentation
├── screen-team/                        # Team feature: data + domain + presentation
├── screen-report/                      # Reports feature: data + domain + presentation
├── screen-finance/                     # Finance feature: data + domain + presentation
│
├── screen-user/                        # User/Auth: presentation only (login, signup, profile, etc.)
├── screen-onboarding/                  # Onboarding: presentation only
├── screen-dashboard/                   # Dashboard: presentation only
├── screen-alerts/                      # Alerts: presentation only
├── screen-map/                         # Maps: presentation only
│
└── locationTracker/                    # Standalone Android GPS tracker app (separate APK, MQTT)
```

## Module Categories

### Foundation Modules (No Feature Logic)

| Module | Namespace | What It Provides |
|--------|-----------|-----------------|
| `ijs-error-lib` | `com.indusjs.error` | `Result<T>`, `IjsException` hierarchy, `ErrorHandler`, `ErrorClassifier`, HTTP codes |
| `ijs-dispatcher-lib` | `com.indusjs.dispatcher` | `DispatcherProvider` interface, platform implementations, coroutine scope helpers |
| `ijs-datetime-utils` | `com.indusjs.datetimeutils` | `FleetDateTime` object — DD-MM-YYYY ↔ ISO 8601, validation, manipulation, calendars |
| `ijs-core-lib` | `com.indusjs.fleet.core` | MVI base classes, `StatusConstants`, utilities, shared DTOs, base interfaces |
| `ijs-logger-lib` | `com.indusjs.logger` | `IjsLogger`, `KermitFleetLogger`, file logging, platform crash handlers |

### Infrastructure Module

| Module | Namespace | What It Provides |
|--------|-----------|-----------------|
| `ijs-network-lib` | `com.indusjs.fleet.network` | HTTP client (Ktor), auth flow, User/Dashboard/Costs data layer, Google Places |

### Feature Modules (Data + Domain + Presentation)

| Module | Namespace | Screens | Has Use Cases |
|--------|-----------|---------|---------------|
| `screen-vehicle` | `com.ijs.vehicle` | List, Detail, Add, MaintenanceCost | Yes (7) |
| `screen-driver` | `com.ijs.driver` | List, Detail, Create, DriverCost | Yes (8) |
| `screen-trip` | `com.ijs.trip` | List, Detail, Create, TripCost | Yes (multiple) |
| `screen-customer` | `com.ijs.customer` | List, Detail, Create | Yes |
| `screen-payment` | `com.ijs.payment` | List, Detail, Add/Edit | No (repo direct) |
| `screen-team` | `com.ijs.team` | List, Detail, Create | No (repo direct) |
| `screen-report` | `com.ijs.reports` | Hub, VehiclePL, TripPL, CostAnalysis, ConsolidatedPL | Yes (9) |
| `screen-finance` | `com.ijs.finance` | List, Detail, AddPurchase, EMI History | No (1 shared VM) |

### Presentation-Only Modules

| Module | Namespace | Screens |
|--------|-----------|---------|
| `screen-user` | `com.ijs.user` | Login, SignUp, ForgotPassword, Profile, ChangePassword |
| `screen-onboarding` | `com.ijs.onboarding` | Onboarding flow |
| `screen-dashboard` | `com.ijs.dashboard` | Dashboard (fleet overview, costs, financials, alerts) |
| `screen-alerts` | `com.ijs.alerts` | Alerts list |
| `screen-map` | `com.ijs.map` | Real-time vehicle tracking |

### UI / Tooling Modules

| Module | Namespace | What It Provides |
|--------|-----------|-----------------|
| `ijs-ui-components-lib` | `com.indusjs.uicomponents` | Theme, reusable Compose components (50+ composables), icons (46 SVGs) |
| `ijs-datetime-picker` | `com.indusjs.datetimepicker` | Date/time picker dialogs with Material 3 styling |
| `ijs-pdf-report` | `com.indusjs.pdfreport` | HTML→PDF generation with platform-specific renderers |

### App Shell Modules

| Module | Purpose |
|--------|---------|
| `androidApp` | Thin Android shell: `FleetApplication` + `AppActivity` + Firebase |
| `webApp` | Thin web shell: `ComposeViewport { App() }` + `index.html` |
| `iosApp` | Xcode project: SwiftUI host → `MainViewController()` + Firebase Pods |
| `locationTracker` | Standalone Android GPS tracker (MQTT publish, no sharedUI dependency) |

## Dependency Graph

```
androidApp ──→ sharedUI ──→ all screen-* modules
webApp ─────→ sharedUI       ──→ ijs-network-lib ──→ ijs-core-lib
iosApp ─────→ sharedUI            ──→ ijs-error-lib
                                  ──→ ijs-dispatcher-lib
                                  ──→ ijs-datetime-utils
                             ──→ ijs-ui-components-lib ──→ ijs-core-lib
                             ──→ ijs-datetime-picker ──→ ijs-datetime-utils
                             ──→ ijs-pdf-report ──→ ijs-datetime-utils
                             ──→ ijs-logger-lib ──→ ijs-core-lib

locationTracker (standalone, no sharedUI dependency)
```
