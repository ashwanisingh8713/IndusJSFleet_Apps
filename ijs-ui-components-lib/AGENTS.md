# ijs-ui-components-lib — AGENTS.md

## Purpose

Centralized, reusable UI components for the **IndusJS Fleet** KMP app. Every `screen-*` module and `sharedUI` depends on this library. Provides theme, typography, color system, input fields, dialogs, and shared composables so the 40+ screens look and behave consistently.

**Package:** `com.indusjs.uicomponents`
**Targets:** Android, iOS (x64/arm64/simulatorArm64), JS (browser), WasmJS (browser)

---

## Source Tree

```
src/commonMain/kotlin/com/indusjs/uicomponents/
├── components/
│   ├── ButtonComponents.kt          # FleetPrimaryButton, FleetSecondaryButton, FleetDestructiveButton, FleetTextButton
│   ├── CardComponents.kt            # FleetCard, FleetItemCard, SectionCard, FleetKeyValueRow
│   ├── CaretakerComponents.kt       # CaretakerSectionCard, CaretakerSearchField
│   ├── CommonComponents.kt          # LoadingContent, ErrorContent, EmptyContent, ScreenContent<T>
│   ├── CostBreakdownComponents.kt   # CostBreakdownSection, CostBreakdownRow
│   ├── CostTypeChipSelector.kt      # Cost type dropdown selector
│   ├── DateRangePickerDialog.kt     # Date range picker with presets (today, week, month, custom)
│   ├── FinanceComponents.kt         # Finance-specific cards and rows
│   ├── FleetDialogs.kt              # DiscardChangesDialog, FleetConfirmationDialog, DeleteConfirmationDialog, LogoutConfirmationDialog, StatusToggleConfirmationDialog
│   ├── FleetFilterChipRow.kt        # Generic horizontal scrolling filter chip row with counts
│   ├── FleetPullToRefreshBox.kt     # Pull-to-refresh wrapper (M3 PullToRefreshBox)
│   ├── FleetSearchBar.kt            # Standardized search field with clear button
│   ├── FleetShimmerPlaceholder.kt   # Shimmer/skeleton loading presets (list, card, detail, dashboard)
│   ├── FleetSnackbarEffect.kt       # MVI effect → Snackbar auto-wiring utility
│   ├── FleetStepIndicator.kt        # Horizontal step/wizard progress indicator
│   ├── FleetTopAppBar.kt            # Standardized TopAppBar with surface background
│   ├── HistoryComponents.kt         # State history timeline
│   ├── InputComponents.kt           # FleetTextField, FleetPasswordField, FleetEmailField, FleetMobileField, FleetDateField, FleetTimeField, FleetDropdown
│   ├── InputFields.kt               # DateInputField, TimeInputField (VisualTransformation variants)
│   ├── PasswordStrengthIndicator.kt # Animated password strength meter
│   ├── PhoneComponents.kt           # Phone call utility
│   ├── PieChart.kt                  # Animated donut pie chart
│   ├── StateComponents.kt           # StateChangeDialog, StateOptionItem
│   ├── StatusColorUtils.kt          # stateColorSchemeToColor mapping
│   ├── TripCostComponents.kt        # Trip cost entry section
│   ├── UiText.kt                    # UiText sealed class for i18n strings
│   └── customer/
│       ├── CustomerDetailsSection.kt
│       ├── CustomerSelectionBottomSheet.kt
│       └── SelectedCustomerCard.kt
└── theme/
    ├── Color.kt                     # Light/Dark color palette + FleetColors
    ├── FleetStatusColors.kt         # Semantic status colors (profit/loss/payment/trip)
    ├── Font.kt                      # Poppins font family
    └── Theme.kt                     # AppTheme, FleetTypography, FleetShapes, LocalThemeIsDark
```

---

## Key Components

### Layout & Containers
- **`ScreenContent<T>`** — Wraps loading/error/success states into a single composable. Every list/detail screen uses this.
- **`FleetPullToRefreshBox`** — Pull-to-refresh wrapper for scrollable content.
- **`FleetTopAppBar`** — Standardized TopAppBar with surface background, back button, icon tinting.

### Input & Forms
- **`FleetTextField`** — Standard text field with label, error, and icon support.
- **`FleetDateField` / `FleetTimeField`** — Date/time pickers wired to `FleetDateTimePicker`.
- **`FleetDropdown`** — Material 3 ExposedDropdownMenu wrapper.
- **`FleetSearchBar`** — Search field with leading icon and animated clear button.

### Dialogs
- **`FleetConfirmationDialog`** — Generic confirmation with customizable title/message/actions.
- **`DiscardChangesDialog`** — "Discard changes?" for edit screens.
- **`DeleteConfirmationDialog`** — Entity-aware delete confirmation.
- **`StatusToggleConfirmationDialog`** — Activate/deactivate entity confirmation.

### Data Display
- **`FleetCard` / `FleetItemCard`** — Consistent card styling for lists and details.
- **`FleetKeyValueRow`** — Label: Value rows in detail screens.
- **`CostBreakdownSection`** — Cost category breakdown with amounts.
- **`PieChart`** — Animated donut chart for cost analysis / reports.
- **`FleetStepIndicator`** — Wizard progress indicator (used in CreateTrip).

### Loading & Feedback
- **`FleetShimmerPlaceholder`** — Skeleton loading presets: `ShimmerListScreen`, `ShimmerDetailScreen`, `ShimmerDashboard`, `ShimmerCard`, `ShimmerListItem`.
- **`FleetSnackbarEffect`** — Auto-wire MVI effects to SnackbarHostState.
- **`FleetFilterChipRow`** — Horizontal scrolling filter chips with optional counts.

### Theme
- **`AppTheme`** — MaterialKolor-based dynamic theme with Poppins font family.
- **`FleetStatusColors`** — Semantic colors for profit/loss, payment status, trip states.

---

## Usage Patterns

```kotlin
// ScreenContent wrapping
ScreenContent(
    result = state.dataResult,
    screenContext = FleetErrorContext.VEHICLES,
    onRetry = { viewModel.sendIntent(Intent.Refresh) }
) { data ->
    LazyColumn { items(data) { VehicleCard(it) } }
}

// FleetTopAppBar
FleetTopAppBar(
    title = "Vehicle Detail",
    onNavigateBack = { navController.pop() },
    actions = { IconButton(onClick = { ... }) { Icon(...) } }
)

// Shimmer loading
ShimmerListScreen(itemCount = 6, showFilterChips = true)

// Snackbar effect wiring
val snackbarHostState = rememberFleetSnackbarHostState()
FleetSnackbarEffect(snackbarHostState, viewModel.effect) { effect ->
    when (effect) {
        is Effect.ShowSnackbar -> SnackbarMessage(effect.message)
        else -> null
    }
}
```

---

## Dependencies

- Compose Multiplatform (runtime, foundation, material3)
- MaterialKolor — Dynamic color theme generation
- `ijs-core-lib` (api) — `FleetErrorContext`, `StatusConstants`, `FormatUtils`
- `ijs-datetime-picker` (implementation) — Date/time picker integration

---

## Consumers

Every `screen-*` module and `sharedUI` imports this library. Components are designed to be self-contained — pass data in, get callbacks out, no ViewModel coupling.
