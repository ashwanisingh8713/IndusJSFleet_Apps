# 05 — Migration Checklist Template

## Per-Module Migration Steps

Use this checklist for each feature module migration. Copy and track per module.

---

## Pre-Migration Verification

- [ ] Read the module's current `build.gradle.kts`
- [ ] List all presentation files in `sharedUI/presentation/{feature}/` to move
- [ ] List all shared UI components in `sharedUI/core/ui/{feature}/` to move
- [ ] Identify ALL cross-feature dependencies (refer to `02-Cross-Feature-Dependency-Matrix.md`)
- [ ] Confirm `ijs-ui-components-lib` is built and passing
- [ ] Confirm shared data contracts exist in `ijs-core-lib`

### Pre-Requisite: Delete sharedUI Duplicates (One-Time)

- [ ] Add `implementation(project(":ijs-ui-components-lib"))` to `sharedUI/build.gradle.kts`
- [ ] Make `AppTheme` and `LocalThemeIsDark` public in `ijs-ui-components-lib/theme/Theme.kt`
- [ ] Delete `sharedUI/theme/` (Color.kt, Font.kt, Theme.kt) — redirect to `com.indusjs.uicomponents.theme.*`
- [ ] Delete ALL files under `sharedUI/core/ui/` — redirect to `com.indusjs.uicomponents.components.*`
- [ ] Delete `sharedUI/composeResources/drawable/` and `font/` — keep `values/strings.xml`
- [ ] Rewrite ~40+ import statements in remaining `sharedUI/presentation/` files
- [ ] Build verify: `./gradlew :androidApp:assembleDebug`

---

## Step 1: Update build.gradle.kts

- [ ] Change `namespace` to new package (e.g., `com.indusjs.customer`)
- [ ] Apply `fleet-compose-conventions.gradle`
- [ ] Add `implementation(project(":ijs-ui-components-lib"))`
- [ ] Verify existing `api(project(":ijs-network-lib"))` remains
- [ ] Add Compose plugins if not already present via conventions
- [ ] **Do NOT add any other feature module dependencies**

## Step 2: Create ExternalDeps Interface (if needed)

- [ ] Create `facade/{Feature}ExternalDeps.kt` in the module
- [ ] Define callback methods using shared contracts from `ijs-core-lib`
- [ ] Use `Result<T>` return types from `ijs-error-lib`

## Step 3: Move Presentation Files

- [ ] Create `presentation/` package in the feature module
- [ ] Move Contract files (State, Intent, Effect)
- [ ] Move ViewModel files
- [ ] Move Screen files
- [ ] Move feature-specific UI components (from `sharedUI/core/ui/{feature}/`)
- [ ] Update all package declarations

## Step 4: Remove FleetRoute References

- [ ] Replace ALL `import com.indusjs.fleet.navigation.FleetRoute` with lambda callbacks
- [ ] In Contracts: Replace `Effect.NavigateTo(FleetRoute.X)` with generic effects like `Effect.NavigateToDetail(id)`
- [ ] In ViewModels: Replace `sendEffect(Effect.NavigateTo(...))` with generic navigation effects
- [ ] In Screens: Accept navigation lambdas as parameters instead of route references

## Step 5: Update Resource Imports

- [ ] Replace `indusjsfleet.sharedui.generated.resources.*` with `indusjsfleet.ijs_ui_components_lib.generated.resources.*`
- [ ] Verify all `painterResource(Res.drawable.ic_*)` calls still resolve

## Step 6: Replace Cross-Feature Dependencies

- [ ] Replace direct repository/use case imports with `ExternalDeps` callback calls
- [ ] Replace feature-specific entity types (e.g., `Driver`, `Vehicle`) with shared contracts (e.g., `SelectableDriver`, `SelectableVehicle`) where used for selection/display only
- [ ] Keep full domain entities for the module's OWN data

## Step 7: Create Facade

- [ ] Create `facade/{Feature}FeatureFacade.kt`
- [ ] Expose `@Composable` screen entry-point functions
- [ ] Accept navigation lambdas (no FleetRoute)
- [ ] Accept ExternalDeps (if applicable)
- [ ] Manage ViewModel creation internally

## Step 8: Update sharedUI

- [ ] Delete moved presentation files from `sharedUI/presentation/{feature}/`
- [ ] Delete moved UI components from `sharedUI/core/ui/{feature}/`
- [ ] Update `FleetNavigation.kt` — replace direct Screen/ViewModel references with Facade calls
- [ ] Update `ViewModelProvider.kt` — remove feature ViewModel factory methods
- [ ] Update `DefaultViewModelProvider.kt` — remove feature ViewModel creation, add Facade construction
- [ ] Create `ExternalDeps` adapter in `sharedUI/di/adapters/` (if applicable)
- [ ] Update `sharedUI/build.gradle.kts` — verify feature module dependency still present

## Step 9: Verify

- [ ] Build `androidApp`: `./gradlew :androidApp:assembleDebug`
- [ ] Build `webApp`: `./gradlew :webApp:jsBrowserDevelopmentRun`
- [ ] Verify feature module has NO imports from other feature modules
- [ ] Verify feature module has NO imports of `FleetRoute`
- [ ] Verify all screens navigate correctly (manual test)
- [ ] Verify feature module line count per file ≤ 500
- [ ] Run any existing tests: `./gradlew :{module}:allTests`

---

## Post-Migration Cleanup

- [ ] Remove unused imports in sharedUI
- [ ] Update AGENTS.md if module structure changed
- [ ] Update copilot-instructions.md module catalog
- [ ] Commit with descriptive message: `feat: migrate {feature} module to full-stack with Facade pattern`

---

## Module-Specific Notes

### feat-customer (Pilot)
- Zero cross-feature deps — no ExternalDeps needed
- Remove `FleetRoute` from `CustomersListContract`, `CreateCustomerContract`, `CustomerDetailContract`
- `CustomerSelectionBottomSheet` already moved to `ijs-ui-components-lib` (uses `SelectableCustomer`)

### feat-team
- `UserLocalDataSource` is from `ijs-network-lib` — direct access OK
- `PermissionUtils` is from `ijs-core-lib` — direct access OK

### feat-vehicle
- Needs `VehicleExternalDeps` for drivers and caretakers
- `StateComponents` vehicle helpers move into this module
- File picker callbacks passed through Facade as lambdas

### feat-driver
- Needs `DriverExternalDeps` for caretakers
- `StateComponents` driver helpers move into this module

### feat-trip (Most Complex)
- Needs `TripExternalDeps` with 5+ callbacks
- `GooglePlacesService` access via `searchPlaces()` callback
- Customer selection via `getCustomers()` callback
- Vehicle/Driver selection via shared contracts

### feat-payment
- Needs `PaymentExternalDeps` for trip data
- `AddPaymentVM` uses `TripRepository` — replaced with `getTrips()` callback

### feat-finance
- Needs `FinanceExternalDeps` for vehicle data
- **ViewModel Split Required:** Current shared ViewModel pattern (`rememberSharedViewModel("vehicle_finance_flow")`) must be replaced with 4 separate ViewModels:
  - `VehicleFinanceListViewModel` — list + filter
  - `VehicleFinanceDetailViewModel` — single vehicle detail
  - `AddPurchaseInfoViewModel` — create/edit purchase
  - `EmiPaymentHistoryViewModel` — EMI payment list
- Each screen loads data independently. Cross-screen refresh via navigation callbacks
  (e.g., `onPurchaseCreated` navigates back, list screen reloads in `init {}`).

### feat-report
- Needs `ReportsExternalDeps` for vehicle and trip data
- `CostBreakdownComponents.kt` uses `CostBreakdownItemDto` (now in ijs-core-lib)

