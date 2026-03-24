# 08 — Migration Execution Order

## Purpose

Step-by-step execution sequence with exact file inventories, import rewrite rules,
and the Finance ViewModel split strategy.

---

## Global Import Rewrite Rules

These rewrites apply to ALL files (sharedUI + feature modules post-move):

| Old Import | New Import |
|-----------|------------|
| `com.indusjs.fleet.core.ui.*` | `com.indusjs.uicomponents.components.*` |
| `com.indusjs.fleet.core.ui.customer.*` | `com.indusjs.uicomponents.customer.*` |
| `com.indusjs.fleet.core.ui.caretaker.*` | `com.indusjs.uicomponents.components.CaretakerComponents` (same file) |
| `com.indusjs.fleet.core.ui.costs.*` | `com.indusjs.uicomponents.components.*` |
| `com.indusjs.fleet.core.ui.history.*` | `com.indusjs.uicomponents.components.*` |
| `com.indusjs.fleet.core.ui.state.*` | `com.indusjs.uicomponents.components.*` |
| `com.indusjs.fleet.theme.*` | `com.indusjs.uicomponents.theme.*` |
| `indusjsfleet.sharedui.generated.resources.*` | `indusjsfleet.ijs_ui_components_lib.generated.resources.*` |
| `com.indusjs.fleet.navigation.FleetRoute` | **DELETE** — replace with lambda callbacks |

---

## Execution Phases

### Phase 0: Foundation (Pre-requisite)

**0a. Make `AppTheme` public in ijs-ui-components-lib**
- File: `ijs-ui-components-lib/.../theme/Theme.kt`
- Change `internal val LocalThemeIsDark` → `val LocalThemeIsDark`
- Change `internal fun AppTheme` → `fun AppTheme`
- `isAppInDarkTheme()` and `rememberThemeToggle()` are already public

**0b. Add ijs-ui-components-lib dependency to sharedUI**
- File: `sharedUI/build.gradle.kts`
- Add: `implementation(project(":ijs-ui-components-lib"))`

**0c. Delete sharedUI theme duplicates**
- Delete: `sharedUI/.../theme/Color.kt`
- Delete: `sharedUI/.../theme/Font.kt`
- Delete: `sharedUI/.../theme/Theme.kt`
- Keep directory if needed for a thin re-export wrapper, or delete entirely
- Update `App.kt`: `import com.indusjs.fleet.theme.AppTheme` → `import com.indusjs.uicomponents.theme.AppTheme`
- Update all `import com.indusjs.fleet.theme.isAppInDarkTheme` → `import com.indusjs.uicomponents.theme.isAppInDarkTheme`
- Update all `import com.indusjs.fleet.theme.rememberThemeToggle` → `import com.indusjs.uicomponents.theme.rememberThemeToggle`
- Affected files: `App.kt`, `DashboardScreen.kt`, `ProfileScreen.kt`, `LoginScreen.kt`

**0d. Delete sharedUI core/ui duplicates**
- Delete ALL files under `sharedUI/.../core/ui/`:
  - `ButtonComponents.kt`, `CardComponents.kt`, `CommonComponents.kt`
  - `CostTypeChipSelector.kt`, `DateRangePickerDialog.kt`, `FinanceComponents.kt`
  - `InputComponents.kt`, `InputFields.kt`, `PhoneComponents.kt`, `PieChart.kt`
  - `caretaker/CaretakerComponents.kt`
  - `costs/CostBreakdownComponents.kt`, `costs/TripCostComponents.kt`
  - `customer/CustomerDetailsSection.kt`, `customer/CustomerSelectionBottomSheet.kt`, `customer/SelectedCustomerCard.kt`
  - `history/HistoryComponents.kt`
  - `state/StateComponents.kt`
- Rewrite ~40+ imports across `sharedUI/presentation/` using the rules above

**0e. Delete duplicate composeResources from sharedUI**
- Delete: `sharedUI/src/commonMain/composeResources/drawable/` (46 icons — duplicates)
- Delete: `sharedUI/src/commonMain/composeResources/font/` (4 font files — duplicates)
- Keep: `sharedUI/src/commonMain/composeResources/values/strings.xml` (if app-level strings exist)

**0f. Build verification**
```bash
./gradlew :sharedUI:compileKotlinMetadata
./gradlew :androidApp:assembleDebug
```

---

### Phase 1: feat-customer (Pilot — Zero Cross-Feature Deps)

**File Inventory (9 files + 1 component subfolder):**
```
sharedUI/presentation/customers/
├── list/
│   ├── CustomersListContract.kt
│   ├── CustomersListScreen.kt
│   └── CustomersListViewModel.kt
├── detail/
│   ├── CustomerDetailContract.kt
│   ├── CustomerDetailScreen.kt
│   ├── CustomerDetailViewModel.kt
│   └── components/
│       └── PaymentsTabContent.kt
└── create/
    ├── CreateCustomerContract.kt
    ├── CreateCustomerScreen.kt
    └── CreateCustomerViewModel.kt
```

**Move to:**
```
feat-customer/src/commonMain/kotlin/com/ijs/customer/
├── presentation/
│   ├── list/
│   │   ├── CustomersListContract.kt
│   │   ├── CustomersListScreen.kt
│   │   └── CustomersListViewModel.kt
│   ├── detail/
│   │   ├── CustomerDetailContract.kt
│   │   ├── CustomerDetailScreen.kt
│   │   ├── CustomerDetailViewModel.kt
│   │   └── components/
│   │       └── PaymentsTabContent.kt
│   └── create/
│       ├── CreateCustomerContract.kt
│       ├── CreateCustomerScreen.kt
│       └── CreateCustomerViewModel.kt
└── facade/
    └── CustomerFeatureFacade.kt
```

**FleetRoute removal (7 files):**
- `CustomersListContract.kt` — replace `Effect.Navigate(FleetRoute)` with `Effect.NavigateToDetail(id)`, `Effect.NavigateToCreate`
- `CustomersListViewModel.kt` — remove FleetRoute import, use generic effects
- `CustomersListScreen.kt` — remove FleetRoute import, use lambda callbacks
- `CustomerDetailContract.kt` — remove FleetRoute from effects
- `CreateCustomerContract.kt` — remove FleetRoute from effects
- `CreateCustomerScreen.kt` — remove FleetRoute import
- `CreateCustomerViewModel.kt` — remove FleetRoute import, use generic effects

**Package changes:** `com.indusjs.fleet.presentation.customers.*` → `com.ijs.customer.presentation.*`

**Build file:** Add Compose plugins + `fleet-compose-conventions.gradle`

**sharedUI cleanup:**
- Delete `sharedUI/presentation/customers/` (entire directory)
- Update `FleetNavigation.kt` to use `customerFacade.*Screen(...)`
- Remove 3 customer VM factory methods from `ViewModelProvider.kt` + `DefaultViewModelProvider.kt`
- Add Facade construction in `DefaultViewModelProvider.kt` (or new `FleetAppOrchestrator`)

---

### Phase 2: feat-team (Zero Cross-Feature Deps)

**File Inventory (9 files):**
```
sharedUI/presentation/team/
├── list/    (TeamListContract, TeamListScreen, TeamListViewModel)
├── detail/  (TeamMemberDetailContract, TeamMemberDetailScreen, TeamMemberDetailViewModel)
└── create/  (CreateTeamMemberContract, CreateTeamMemberScreen, CreateTeamMemberViewModel)
```

**Move to:** `feat-team/.../presentation/{list,detail,create}/`

**Facade:** `TeamFeatureFacade` — 3 screen functions, no ExternalDeps

**Package:** `com.indusjs.fleet.presentation.team.*` → `com.ijs.team.presentation.*`

---

### Phase 3: feat-vehicle (ExternalDeps: drivers + team)

**File Inventory (12 files):**
```
sharedUI/presentation/vehicles/
├── VehiclesContract.kt
├── VehiclesScreen.kt
├── VehiclesViewModel.kt
├── AddVehicleContract.kt
├── AddVehicleScreen.kt
├── AddVehicleViewModel.kt
├── detail/
│   ├── VehicleDetailContract.kt
│   ├── VehicleDetailScreen.kt
│   └── VehicleDetailViewModel.kt
└── costs/
    ├── MaintenanceCostEntryContract.kt
    ├── MaintenanceCostEntryScreen.kt
    └── MaintenanceCostEntryViewModel.kt
```

**ExternalDeps:**
```kotlin
interface VehicleExternalDeps {
    suspend fun getDrivers(): Result<List<SelectableDriver>>
    suspend fun getCaretakers(): Result<List<CaretakerInfo>>
}
```

**Adapter (sharedUI):**
```kotlin
class VehicleExternalDepsAdapter(
    private val driverRepository: DriverRepository,
    private val teamRepository: TeamRepository
) : VehicleExternalDeps { ... }
```

---

### Phase 4: feat-driver (ExternalDeps: team)

**File Inventory (12 files):**
```
sharedUI/presentation/drivers/
├── DriversContract, DriversScreen, DriversViewModel
├── create/ (CreateDriverContract, CreateDriverScreen, CreateDriverViewModel)
├── detail/ (DriverDetailContract, DriverDetailScreen, DriverDetailViewModel)
└── cost/   (DriverCostEntryContract, DriverCostEntryScreen, DriverCostEntryViewModel)
```

**ExternalDeps:**
```kotlin
interface DriverExternalDeps {
    suspend fun getCaretakers(): Result<List<CaretakerInfo>>
}
```

---

### Phase 5: feat-payment (ExternalDeps: trips)

**File Inventory (7 files):**
```
sharedUI/presentation/payments/
├── PaymentsContract.kt, PaymentsScreen.kt, PaymentsViewModel.kt
├── PaymentDetailScreen.kt, PaymentDetailViewModel.kt
├── AddPaymentScreen.kt, AddPaymentViewModel.kt
└── components/  (empty — no files)
```

**ExternalDeps:**
```kotlin
interface PaymentExternalDeps {
    suspend fun getTrips(): Result<List<SelectableTrip>>
    suspend fun getUserRole(): String
}
```

---

### Phase 6: feat-finance (ExternalDeps: vehicles) — ViewModel Split

**File Inventory (5 files → becomes 4 ViewModels):**
```
sharedUI/presentation/finance/
├── VehicleFinanceContract.kt     → Split into separate contracts
├── VehicleFinanceScreen.kt       → VehicleFinanceListScreen + VehicleFinanceListVM
├── VehicleFinanceDetailScreen.kt → VehicleFinanceDetailVM
├── AddPurchaseInfoScreen.kt      → AddPurchaseInfoVM
└── EmiPaymentHistoryScreen.kt    → EmiPaymentHistoryVM
```

**Current shared-ViewModel strategy** uses `rememberSharedViewModel("vehicle_finance_flow")`.
**New strategy:** Split into 4 ViewModels. Use callbacks for data sharing:

```kotlin
class FinanceFeatureFacade(...) {
    @Composable
    fun VehicleFinanceListScreen(
        onNavigateToDetail: (vehicleId: String) -> Unit,
        onNavigateToAddPurchase: () -> Unit,
        onNavigateBack: () -> Unit
    )

    @Composable
    fun VehicleFinanceDetailScreen(
        vehicleId: String,
        onNavigateBack: () -> Unit,
        onNavigateToEdit: (vehicleId: String) -> Unit,
        onNavigateToHistory: (vehicleId: String) -> Unit
    )

    @Composable
    fun AddPurchaseInfoScreen(
        onNavigateBack: () -> Unit,
        onPurchaseCreated: () -> Unit   // Callback to trigger list refresh
    )

    @Composable
    fun EmiPaymentHistoryScreen(
        vehicleId: String,
        onNavigateBack: () -> Unit
    )
}
```

Each screen loads its own data independently. When `AddPurchaseInfoScreen` completes,
the `onPurchaseCreated` callback navigates back, and the list screen reloads via its
own `init { sendIntent(LoadData) }`.

**ExternalDeps:**
```kotlin
interface FinanceExternalDeps {
    suspend fun getVehicles(): Result<List<SelectableVehicle>>
}
```

---

### Phase 7: feat-trip (Most Complex — ExternalDeps: vehicles + drivers + customers + places)

**File Inventory (9 files):**
```
sharedUI/presentation/trips/
├── TripsContract, TripsScreen, TripsViewModel
├── create/ (CreateTripContract, CreateTripScreen, CreateTripViewModel)
├── detail/ (TripDetailContract, TripDetailScreen, TripDetailViewModel)
└── cost/   (TripCostEntryContract, TripCostEntryScreen, TripCostEntryViewModel)
```

**ExternalDeps (7 callbacks):**
```kotlin
interface TripExternalDeps {
    suspend fun getAvailableVehicles(): Result<List<SelectableVehicle>>
    suspend fun getAvailableDrivers(): Result<List<SelectableDriver>>
    suspend fun getCustomers(): Result<List<SelectableCustomer>>
    suspend fun refreshCustomers(): Result<Unit>
    suspend fun searchPlaces(query: String): List<PlacePrediction>
    suspend fun getPlaceDetails(placeId: String): PlaceDetails?
    suspend fun getUserRole(): String
}
```

**Note:** `PlacePrediction` and `PlaceDetails` are from `ijs-network-lib` (Google Places).
They're already accessible since feat-trip depends on `ijs-network-lib`. But
`GooglePlacesService` construction requires an API key that only `sharedUI` has.
So we wrap the calls via `TripExternalDeps` callbacks.

---

### Phase 8: feat-report (ExternalDeps: vehicles + trips)

**File Inventory (12 files):**
```
sharedUI/presentation/reports/
├── ReportsContract, ReportsScreen, ReportsViewModel
├── vehicle/  (VehiclePLContract, VehiclePLViewModel, VehicleProfitLossScreen)
├── trip/     (TripPLContract, TripPLViewModel, TripProfitLossScreen)
├── cost/     (CostAnalysisContract, CostAnalysisScreen, CostAnalysisViewModel)
└── consolidated/ (ConsolidatedPLContract, ConsolidatedPLScreen, ConsolidatedPLViewModel)
```

**ExternalDeps:**
```kotlin
interface ReportsExternalDeps {
    suspend fun getVehicles(): Result<List<SelectableVehicle>>
    suspend fun getTrips(startDate: String?, endDate: String?): Result<List<SelectableTrip>>
}
```

---

### Phase 9: sharedUI Final Cleanup

**Remaining in sharedUI/presentation/ (app-level screens only):**
```
presentation/
├── auth/         (LoginContract, LoginScreen, LoginViewModel)
├── onboarding/   (OnboardingContract, OnboardingScreen, OnboardingViewModel)
├── dashboard/    (DashboardContract, DashboardScreen, DashboardViewModel, components/)
├── maps/         (MapsContract, MapsScreen, MapsViewModel)
├── alerts/       (AlertsListContract, AlertsListScreen, AlertsListViewModel)
└── user/
    ├── profile/          (ProfileContract, ProfileScreen, ProfileViewModel)
    ├── signup/           (SignUpContract, SignUpScreen, SignUpViewModel)
    ├── forgotpassword/   (ForgotPasswordContract, ForgotPasswordScreen, ForgotPasswordViewModel)
    └── changepassword/   (ChangePasswordContract, ChangePasswordScreen, ChangePasswordViewModel)
```

**DI Refactor:**
- Replace `ViewModelProvider` interface (which lists every VM) with a slimmer interface
  that only has app-level VMs
- Replace `DefaultViewModelProvider` with `FleetAppOrchestrator` that creates
  Facades + ExternalDeps adapters
- `FleetNavigation.kt` calls Facade composables for all features, direct VM/Screen
  only for app-level screens

**Documentation updates:**
- `AGENTS.md` (root)
- `.github/copilot-instructions.md`
- `sharedUI/AGENTS.md`
- `sharedUI/ARCHITECTURE.md`
- Each `feat-*/AGENTS.md` (create if missing)

---

## Build Verification After Each Phase

```bash
# After each phase:
./gradlew :androidApp:assembleDebug
./gradlew :webApp:jsBrowserDevelopmentRun

# Feature module isolation check:
grep -r "import com.indusjs.fleet.navigation.FleetRoute" feat-*/
# Expected: 0 results

grep -r "import com.ijs.vehicle\|import com.ijs.driver\|import com.ijs.trip\|import com.ijs.customer\|import com.ijs.payment\|import com.ijs.team\|import com.ijs.reports\|import com.ijs.finance" feat-*/
# Expected: Only self-imports (e.g., feat-vehicle importing com.ijs.vehicle.*)
```

