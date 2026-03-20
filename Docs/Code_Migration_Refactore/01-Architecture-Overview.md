# 01 — Architecture Overview (Post-Migration)

## Goal

Refactor each `ijs-*-lib` feature module from **data-only** to **self-contained full-stack** (Data + Domain + Presentation + UI), exposing a **Facade** to `sharedUI`. Feature modules are fully isolated — they never depend on each other.

---

## Post-Migration Module Dependency Graph

```
androidApp ──→ sharedUI
webApp ─────→ sharedUI
iosApp ─────→ sharedUI (via framework)

sharedUI ──→ ijs-vehicle-lib  ──→ ijs-network-lib + ijs-ui-components-lib
sharedUI ──→ ijs-driver-lib   ──→ ijs-network-lib + ijs-ui-components-lib
sharedUI ──→ ijs-trip-lib     ──→ ijs-network-lib + ijs-ui-components-lib
sharedUI ──→ ijs-customer-lib ──→ ijs-network-lib + ijs-ui-components-lib
sharedUI ──→ ijs-payment-lib  ──→ ijs-network-lib + ijs-ui-components-lib
sharedUI ──→ ijs-team-lib     ──→ ijs-network-lib + ijs-ui-components-lib
sharedUI ──→ ijs-reports-lib  ──→ ijs-network-lib + ijs-ui-components-lib
sharedUI ──→ ijs-finance-lib  ──→ ijs-network-lib + ijs-ui-components-lib
sharedUI ──→ ijs-ui-components-lib ──→ ijs-core-lib

ijs-network-lib ──→ ijs-core-lib
ijs-core-lib    ──→ ijs-error-lib + ijs-dispatcher-lib + ijs-datetime-utils
ijs-ui-components-lib ──→ ijs-core-lib
```

### Key Rules

1. **Feature modules depend ONLY on `ijs-network-lib` and `ijs-ui-components-lib`** — never on each other.
2. **`FleetRoute` is NEVER imported by any feature module.** Navigation is via lambda callbacks.
3. **All common/shared data classes live in `ijs-core-lib`.**
4. **All shared icons, fonts, theme, and common UI components live in `ijs-ui-components-lib`.**

---

## Feature Module Structure (Post-Migration)

Each feature module becomes a self-contained full-stack module:

```
ijs-{feature}-lib/
├── src/commonMain/kotlin/com/indusjs/{feature}/
│   ├── data/
│   │   ├── datasource/{feature}/   # Remote data source + impl
│   │   ├── mapper/{feature}/       # DTO ↔ Entity mappers
│   │   ├── model/{feature}/        # DTOs (@Serializable)
│   │   └── repository/{feature}/   # Repository impl
│   ├── domain/
│   │   ├── entity/{feature}/       # Domain entities
│   │   ├── repository/{feature}/   # Repository interface
│   │   └── usecase/{feature}/      # Use cases
│   ├── presentation/
│   │   ├── list/                   # List screen (Contract, ViewModel, Screen)
│   │   ├── detail/                 # Detail screen
│   │   ├── create/                 # Create/Add screen
│   │   ├── cost/                   # Cost entry (if applicable)
│   │   └── components/             # Feature-specific UI components
│   └── facade/
│       ├── {Feature}FeatureFacade.kt     # Public API surface
│       └── {Feature}ExternalDeps.kt      # Cross-feature callback interface
└── src/commonMain/composeResources/      # Feature-specific resources (optional)
```

---

## sharedUI Structure (Post-Migration — Thin Orchestrator)

```
sharedUI/
├── src/commonMain/kotlin/com/indusjs/fleet/
│   ├── App.kt                                # Compose root
│   ├── navigation/
│   │   ├── FleetRoute.kt                     # All route definitions (ONLY place FleetRoute exists)
│   │   └── FleetNavigation.kt                # NavHost — calls Facades, maps lambdas to routes
│   ├── di/
│   │   ├── FleetAppOrchestrator.kt           # Creates Facades + ExternalDeps adapters
│   │   ├── FeatureRepositoryFactory.kt       # Wires feature-lib repositories
│   │   ├── ViewModelProvider.kt              # Interface + CompositionLocal (reduced)
│   │   └── adapters/                         # ExternalDeps adapter implementations
│   │       ├── VehicleExternalDepsAdapter.kt
│   │       ├── DriverExternalDepsAdapter.kt
│   │       ├── TripExternalDepsAdapter.kt
│   │       ├── PaymentExternalDepsAdapter.kt
│   │       ├── FinanceExternalDepsAdapter.kt
│   │       └── ReportsExternalDepsAdapter.kt
│   ├── presentation/                          # App-level screens ONLY
│   │   ├── auth/                              # Login, SignUp, ForgotPassword
│   │   ├── onboarding/                        # Onboarding
│   │   ├── dashboard/                         # Dashboard
│   │   ├── maps/                              # Maps
│   │   ├── alerts/                            # Alerts
│   │   └── user/                              # Profile, ChangePassword
│   ├── core/                                  # App-level utilities
│   │   ├── auth/                              # AuthenticationManager
│   │   ├── init/                              # AppInitializer
│   │   └── network/                           # HttpClientProvider, ApiConfig
│   ├── data/                                  # Room database, local data sources
│   └── theme/                                 # (MOVED to ijs-ui-components-lib)
```

---

## ijs-ui-components-lib Structure

```
ijs-ui-components-lib/
├── src/commonMain/kotlin/com/indusjs/uicomponents/
│   ├── components/
│   │   ├── ButtonComponents.kt
│   │   ├── CardComponents.kt
│   │   ├── CommonComponents.kt      # LoadingContent, ErrorContent, EmptyContent, ScreenContent
│   │   ├── InputComponents.kt       # FleetTextField, FleetDateField, etc.
│   │   ├── InputFields.kt           # DateInputField, DateVisualTransformation
│   │   ├── PhoneComponents.kt       # ClickablePhoneRow
│   │   ├── PieChart.kt
│   │   ├── FinanceComponents.kt     # LoanProgressCircle, FinanceColors
│   │   ├── DateRangePickerDialog.kt
│   │   ├── CostTypeChipSelector.kt  # CostTypeTwoLevelSelector, CostTypeSelection
│   │   ├── HistoryComponents.kt
│   │   ├── CaretakerComponents.kt   # Refactored → uses CaretakerInfo
│   │   ├── StateComponents.kt       # Generic StateChangeDialog, StateOption
│   │   ├── TripCostComponents.kt    # Uses TripCostDto from ijs-core-lib
│   │   └── CostBreakdownComponents.kt # Uses CostBreakdownItemDto from ijs-core-lib
│   ├── customer/
│   │   ├── CustomerSelectionBottomSheet.kt  # Uses SelectableCustomer
│   │   ├── CustomerDetailsSection.kt        # Uses SelectableCustomer
│   │   └── SelectedCustomerCard.kt          # Uses SelectableCustomer
│   └── theme/
│       ├── Color.kt
│       ├── Font.kt
│       └── Theme.kt
├── src/commonMain/composeResources/
│   ├── drawable/       # All 45 SVG/XML icons
│   ├── font/           # 4 Poppins font files
│   └── values/         # strings.xml
└── build.gradle.kts
```

---

## Facade Pattern

### Facade Contract

Each feature module exposes a single **Facade** as its public API:

```kotlin
// In ijs-customer-lib
class CustomerFeatureFacade(
    private val customerRepository: CustomerRepository,
    private val dispatcherProvider: DispatcherProvider
) {
    @Composable
    fun CustomerListScreen(
        onNavigateToDetail: (customerId: String) -> Unit,
        onNavigateToCreate: () -> Unit,
        onNavigateBack: () -> Unit
    ) { /* Internal ViewModel creation + Screen rendering */ }

    @Composable
    fun CustomerDetailScreen(
        customerId: String,
        onNavigateBack: () -> Unit
    ) { /* ... */ }

    @Composable
    fun CreateCustomerScreen(
        onNavigateBack: () -> Unit,
        onCustomerCreated: (customerId: String) -> Unit
    ) { /* ... */ }
}
```

### ExternalDeps (for cross-feature data)

```kotlin
// In ijs-vehicle-lib
interface VehicleExternalDeps {
    suspend fun getDrivers(): Result<List<SelectableDriver>>
    suspend fun getCaretakers(): Result<List<CaretakerInfo>>
}

// In sharedUI — adapter wired at DI layer
class VehicleExternalDepsAdapter(
    private val driverRepository: DriverRepository,
    private val teamRepository: TeamRepository
) : VehicleExternalDeps {
    override suspend fun getDrivers() = driverRepository.getDrivers().map { ... }
    override suspend fun getCaretakers() = teamRepository.getTeamMembers().map { ... }
}
```

### sharedUI Navigation Wiring (Post-Migration)

```kotlin
// FleetNavigation.kt — calls Facades with lambda callbacks
is FleetRoute.Customers -> NavEntry(route) {
    customerFacade.CustomerListScreen(
        onNavigateToDetail = { backStack.add(FleetRoute.CustomerDetail(it)) },
        onNavigateToCreate = { backStack.add(FleetRoute.CreateCustomer) },
        onNavigateBack = { backStack.removeLastOrNull() }
    )
}
```

---

## Migration Order

| Step | Module | Cross-Feature Deps | Complexity |
|------|--------|-------------------|------------|
| 1 | `ijs-ui-components-lib` (new) | None — foundation | Low |
| 2 | Common data contracts in `ijs-core-lib` | None — foundation | Low |
| 3 | `ijs-customer-lib` (pilot) | None | Low |
| 4 | `ijs-team-lib` | `UserLocalDataSource` (network-lib, allowed) | Low |
| 5 | `ijs-vehicle-lib` | drivers, team (via ExternalDeps) | Medium |
| 6 | `ijs-driver-lib` | team (via ExternalDeps) | Medium |
| 7 | `ijs-payment-lib` | trips (via ExternalDeps) | Medium |
| 8 | `ijs-finance-lib` | vehicles (via ExternalDeps) | Medium |
| 9 | `ijs-trip-lib` | vehicles, drivers, customers (via ExternalDeps) | High |
| 10 | `ijs-reports-lib` | vehicles, trips (via ExternalDeps) | Medium |
| 11 | `sharedUI` cleanup | N/A | Low |

