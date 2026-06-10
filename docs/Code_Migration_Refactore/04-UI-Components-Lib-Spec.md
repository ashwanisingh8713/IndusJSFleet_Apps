# 04 — UI Components Library Specification

## Module: `ijs-ui-components-lib`

**Package:** `com.indusjs.uicomponents`
**Purpose:** Centralized shared UI components, theme, icons, and fonts used across all feature modules and `sharedUI`.

---

## Dependencies

```kotlin
// build.gradle.kts
commonMain.dependencies {
    api(project(":ijs-core-lib"))   // For shared DTOs, MVI base, error types, StatusConstants

    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.resources)
    implementation(libs.compose.material3)
    implementation(libs.kermit)
    implementation(libs.kotlinx.datetime)
}
```

---

## Resources (All Centralized)

### Drawables (45 icons)
```
ic_add.xml, ic_arrow_back.xml, ic_bus.xml, ic_calendar.xml, ic_car.xml,
ic_check.xml, ic_chevron_right.xml, ic_close.xml, ic_cost.xml, ic_cyclone.xml,
ic_dark_mode.xml, ic_dashboard.xml, ic_delete.xml, ic_download.xml, ic_driver.xml,
ic_edit.xml, ic_email.xml, ic_filter.xml, ic_fleet_logo.xml, ic_help.xml,
ic_history.xml, ic_info.xml, ic_light_mode.xml, ic_lock.xml, ic_logout.xml,
ic_map.xml, ic_menu.xml, ic_moon.xml, ic_more_vert.xml, ic_motorcycle.xml,
ic_notifications.xml, ic_phone.xml, ic_profile.xml, ic_refresh.xml,
ic_rotate_right.xml, ic_search.xml, ic_settings.xml, ic_sun.xml, ic_team.xml,
ic_time.xml, ic_trailer.xml, ic_trip.xml, ic_truck.xml, ic_van.xml,
ic_vehicle.xml, ic_warning.xml
```

### Fonts (4 files)
```
poppins_bold.ttf, poppins_medium.ttf, poppins_regular.ttf, poppins_semibold.ttf
```

### Strings
```
values/strings.xml
```

---

## Components Inventory

### sharedUI core/ui Deletion Status: ✅ COMPLETED

All 15+ files deleted from `sharedUI/core/ui/`. Full 1:1 parity verified.
`sharedUI/build.gradle.kts` now has `implementation(project(":ijs-ui-components-lib"))`.

---

### Generic Input Components (`components/`)

| File | Components | Dependencies |
|------|-----------|-------------|
| `InputComponents.kt` | `FleetTextField`, `FleetDateField`, `FleetTimeField`, `FleetMobileField`, `FleetEmailField`, `FleetPasswordField`, `FleetDropdownField`, `FleetPhoneField`, `FleetSearchField`, `DateVisualTransformation`, `TimeVisualTransformation`, `MobileVisualTransformation`, `filterDigitsOnly()` | Compose M3, resources |
| `InputComponents.kt` (validators) | `isValidEmail()`, `isValidMobile()`, `isValidDateRaw()`, `isValidTimeRaw()`, `formatTimeRaw()`, `parseTimeToRaw()` | Pure Kotlin |
| `InputFields.kt` | `DateInputField`, `TimeInputField` | Compose M3 |

### Button Components

| File | Components | Dependencies |
|------|-----------|-------------|
| `ButtonComponents.kt` | `FleetPrimaryButton`, `FleetSecondaryButton`, `FleetTextButton` | Compose M3 |

### Card Components

| File | Components | Dependencies |
|------|-----------|-------------|
| `CardComponents.kt` | `FleetCard`, `FleetListItem`, `StatusBadge`, `InfoRow` | Compose M3 |

### State Display Components

| File | Components | Dependencies |
|------|-----------|-------------|
| `CommonComponents.kt` | `LoadingContent`, `ErrorContent`, `EmptyContent`, `ScreenContent`, `StatusDot` | Compose M3, `ijs-error-lib` (`FleetErrorContext`, `toErrorInfo`) |

### Phone

| File | Components | Dependencies |
|------|-----------|-------------|
| `PhoneComponents.kt` | `ClickablePhoneRow` | Resources (ic_phone), `PhoneCallUtil` from ijs-core-lib |

### Charts

| File | Components | Dependencies |
|------|-----------|-------------|
| `PieChart.kt` | `PieChart`, `DonutChart`, `PieChartData`, `PieChartWithLegend` | Compose Canvas |

### Finance UI

| File | Components | Dependencies |
|------|-----------|-------------|
| `FinanceComponents.kt` | `LoanProgressCircle`, `FinanceColors`, `FinanceSummaryCard`, `FinanceStatRow`, `CopyableText` | Compose M3, `formatCurrency` from ijs-core-lib |

### Date Range

| File | Components | Dependencies |
|------|-----------|-------------|
| `DateRangePickerDialog.kt` | `DateRangePickerDialog`, `PeriodSelector`, quick period shortcuts | Compose M3, kotlinx-datetime |

### Cost Type Selector

| File | Components | Dependencies |
|------|-----------|-------------|
| `CostTypeChipSelector.kt` | `CostTypeTwoLevelSelector`, `CostTypeSelection`, `CostTypeGroup` (typealias for `CostTypeGroupDto`) | `CostTypeGroupDto` from ijs-core-lib |

### History

| File | Components | Dependencies |
|------|-----------|-------------|
| `HistoryComponents.kt` | `HistoryItemCard`, `HistoryTimelineSection`, `HistoryListWithPagination` | `HistoryItemDto` from ijs-core-lib |

### Cost Display

| File | Components | Dependencies |
|------|-----------|-------------|
| `TripCostComponents.kt` | `TotalCostHeader`, `CostCategoryBreakdown`, `CostItemCard`, `CostDetailDialog` | `TripCostDto` from ijs-core-lib |
| `CostBreakdownComponents.kt` | `CostBreakdownItemCard`, `CostBreakdownList`, `CostAnalysisChart` | `CostBreakdownItemDto` (moved to ijs-core-lib) |

### Caretaker (Refactored)

| File | Components | Dependencies |
|------|-----------|-------------|
| `CaretakerComponents.kt` | `CaretakerDropdownField`, `CaretakerCard` | `CaretakerInfo` (shared contract in ijs-core-lib) — **NOT** `TeamMemberDto` |

### Customer (Refactored)

| File | Components | Dependencies |
|------|-----------|-------------|
| `CustomerSelectionBottomSheet.kt` | `CustomerSelectionBottomSheet` | `SelectableCustomer` (shared contract in ijs-core-lib) — **NOT** `Customer` entity |
| `CustomerDetailsSection.kt` | `CustomerDetailsSection` | `SelectableCustomer`, resources |
| `SelectedCustomerCard.kt` | `SelectedCustomerCard` | `SelectableCustomer` |

### State (Refactored)

| File | Components | Dependencies |
|------|-----------|-------------|
| `StateComponents.kt` | `StateChangeDialog`, `StateOption`, `StatusChip` | `StatusConstants` from ijs-core-lib (generic, no feature entity imports) |

**Note:** Feature-specific state helper functions (e.g., `getVehicleStateOptions()`, `getDriverStateOptions()`, `getTripStateOptions()`) move into their respective feature modules.

### Theme

| File | Contents |
|------|---------|
| `theme/Color.kt` | `LightColorScheme`, `DarkColorScheme` |
| `theme/Font.kt` | `FleetTypography`, Poppins font family |
| `theme/Theme.kt` | `FleetTheme`, `FleetShapes` |

---

## Resource Import Path Change

**Before (sharedUI resources):**
```kotlin
import indusjsfleet.sharedui.generated.resources.*
```

**After (ijs-ui-components-lib resources):**
```kotlin
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
```

All files in `sharedUI` and all feature modules must update to the new import path.

---

## What Does NOT Go Into ijs-ui-components-lib

| Component | Stays In | Reason |
|-----------|---------|--------|
| `FleetNavigation.kt` | sharedUI | Navigation orchestration |
| `FleetRoute.kt` | sharedUI | Route definitions |
| `ViewModelProvider.kt` | sharedUI | DI wiring |
| `DashboardScreen` / components | sharedUI | App-level screen |
| `LoginScreen` / auth screens | sharedUI | App-level screen |
| `MapsScreen` | sharedUI | App-level screen |
| `AlertsListScreen` | sharedUI | App-level screen |
| Feature-specific composables | Respective feature module | Belong with their feature |

