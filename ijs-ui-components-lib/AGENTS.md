# ijs-ui-components-lib — AGENTS.md

## Purpose

Centralized, reusable UI components for the **IndusJS Fleet** KMP app. Every `screen-*` module and `sharedUI` depends on this library. Provides theme, typography, color system, design tokens, canonical input/button/filter/dropdown components, dialogs, and shared composables so the 40+ screens look and behave consistently.

**Package:** `com.indusjs.uicomponents`
**Targets:** Android, iOS (x64/arm64/simulatorArm64), JS (browser), WasmJS (browser)

---

## Source Tree

```
src/commonMain/kotlin/com/indusjs/uicomponents/
├── components/
│   ├── CardComponents.kt            # FleetCard, FleetItemCard, SectionCard, FleetKeyValueRow
│   ├── CaretakerComponents.kt       # CaretakerSectionCard, CaretakerSearchField
│   ├── CommonComponents.kt          # LoadingContent, ErrorContent, EmptyContent, ScreenContent<T>
│   ├── CostBreakdownComponents.kt   # CostBreakdownSection, CostBreakdownRow
│   ├── CostTypeChipSelector.kt      # Cost type dropdown selector
│   ├── FinanceComponents.kt         # Finance-specific cards and rows
│   ├── FleetButton.kt              # ★ CANONICAL — sole button (PRIMARY/SECONDARY/GHOST/DESTRUCTIVE, SMALL/MEDIUM/LARGE)
│   ├── FleetDateRangePickerDialog.kt # ★ CANONICAL — date range picker with maxRangeDays validation + i18n
│   ├── FleetDialogs.kt              # DiscardChangesDialog, FleetConfirmationDialog, DeleteConfirmationDialog, LogoutConfirmationDialog, StatusToggleConfirmationDialog
│   ├── FleetDisplayField.kt        # ★ CANONICAL — read-only label+value display (not an input, not focusable)
│   ├── FleetDropdown.kt            # ★ CANONICAL — ExposedDropdownMenuBox wrapper, adaptive width
│   ├── FleetFilterBar.kt           # ★ CANONICAL — horizontal scrolling filter chips with date range integration
│   ├── FleetInputField.kt          # ★ CANONICAL — sole text input (TEXT/EMAIL/PHONE/PASSWORD/NUMBER/SEARCH/MULTILINE)
│   ├── FleetPullToRefreshBox.kt     # Pull-to-refresh wrapper (M3 PullToRefreshBox)
│   ├── FleetSearchField.kt         # ★ CANONICAL — search field built on FleetInputField(FieldType.SEARCH) with debounce
│   ├── FleetShimmerPlaceholder.kt   # Shimmer/skeleton loading presets (list, card, detail, dashboard)
│   ├── FleetSnackbarEffect.kt       # MVI effect → Snackbar auto-wiring utility
│   ├── FleetStepIndicator.kt        # Horizontal step/wizard progress indicator
│   ├── FleetTabBar.kt              # ★ CANONICAL — tab row built on M3 PrimaryTabRow
│   ├── FleetTopAppBar.kt            # Standardized TopAppBar with surface background
│   ├── HistoryComponents.kt         # State history timeline
│   ├── InputUtilities.kt            # Visual transformations (Date/Time/Mobile/ISO), validators, format/parse helpers
│   ├── PasswordStrengthIndicator.kt # Animated password strength meter
│   ├── PhoneComponents.kt           # Phone call utility
│   ├── PieChart.kt                  # Animated donut pie chart
│   ├── StateComponents.kt           # StateChangeDialog, StateOptionItem
│   ├── StatusColorUtils.kt          # stateColorSchemeToColor mapping
│   ├── TripCostComponents.kt        # Trip cost entry section
│   └── UiText.kt                    # UiText sealed class for i18n strings
└── theme/
    ├── Color.kt                     # Light/Dark color palette + FleetColors
    ├── FleetBreakpoints.kt          # FleetBreakpoint (Compact/Medium/Expanded) + rememberFleetBreakpoint()
    ├── FleetStatusColors.kt         # Semantic status colors (profit/loss/payment/trip)
    ├── FleetTokens.kt              # ★ Design token system: Spacing, Radius, Elevation, IconSize, Height, Width
    ├── Font.kt                      # Poppins font family (loaded via compose resources)
    └── Theme.kt                     # AppTheme (Poppins typography), FleetShapes (token-backed), LocalThemeIsDark
```

### Deleted Duplicates (no longer present)

These files were removed as they were superseded by canonical components:

| Deleted File | Superseded By |
|---|---|
| `FleetSearchBar.kt` | `FleetSearchField.kt` |
| `FleetFilterChipRow.kt` | `FleetFilterBar.kt` |
| `DateRangePickerDialog.kt` | `FleetDateRangePickerDialog.kt` |
| `ButtonComponents.kt` | `FleetButton.kt` (all usages migrated) |
| `InputComponents.kt` | `FleetInputField.kt` + `FleetDropdown.kt` + `InputUtilities.kt` (all usages migrated) |
| `InputFields.kt` | `FleetInputField.kt` + `InputUtilities.kt` (all usages migrated) |
| `components/customer/*` | Moved to `screen-customer` |

---

## Canonical Components (8)

These are the **only** components that should be used for their respective UI patterns. No alternatives should exist in any `screen-*` module.

| Component | File | Purpose |
|---|---|---|
| `FleetInputField` | `FleetInputField.kt` | All text input (via `FieldType` enum) |
| `FleetButton` | `FleetButton.kt` | All buttons (via `ButtonVariant` + `ButtonSize`) |
| `FleetSearchField` | `FleetSearchField.kt` | Search with debounce (delegates to `FleetInputField`) |
| `FleetTabBar` | `FleetTabBar.kt` | Tab navigation (M3 `PrimaryTabRow`) |
| `FleetFilterBar` | `FleetFilterBar.kt` | Filter chips + date range |
| `FleetDateRangePickerDialog` | `FleetDateRangePickerDialog.kt` | Date range selection with max range validation |
| `FleetDisplayField` | `FleetDisplayField.kt` | Read-only label+value (not focusable) |
| `FleetDropdown` | `FleetDropdown.kt` | Selection dropdown (M3 `ExposedDropdownMenuBox`) |

### Utility Module

`InputUtilities.kt` provides non-composable utilities shared across screens:
- **Visual Transformations**: `DateVisualTransformation`, `TimeVisualTransformation`, `MobileVisualTransformation`, `IsoDateVisualTransformation`
- **Validators**: `isValidEmail`, `isValidMobile`, `isValidDateRaw`, `isValidTimeRaw`
- **Format/Parse**: `filterDigitsOnly`, `formatToDdMmYyyy`, `convertDdMmYyyyToIso`, `convertIsoToDdMmYyyy`, etc.

---

## Design Token System

All dimensions come from `FleetTokens` (in `theme/FleetTokens.kt`). No hardcoded `dp`/`sp` values in component code.

| Token Group | Examples |
|---|---|
| `FleetTokens.Spacing` | XS(4), S(8), M(12), L(16), XL(24), XXL(32) |
| `FleetTokens.Radius` | XS(2), S(4), M(8), ML(10), L(12), XL(16), Pill(999) |
| `FleetTokens.Elevation` | None(0), S(2), M(4), L(8), XL(12) |
| `FleetTokens.IconSize` | S(16), M(20), Default(24), L(32), XL(48) |
| `FleetTokens.Height` | ButtonSmall(36), ButtonMedium(44), MinTouchTarget(44) |

### Typography

Typography uses **Poppins** font family loaded via Compose Resources. All text styles flow through `MaterialTheme.typography` (standard M3 roles: display, headline, title, body, label).

### Breakpoints

`FleetBreakpoints.kt` provides adaptive layout via `rememberFleetBreakpoint()`:
- **Compact**: < 600dp (phones)
- **Medium**: 600-839dp (tablets portrait)
- **Expanded**: >= 840dp (tablets landscape, desktop)

---

## Key Components

### Layout & Containers
- **`ScreenContent<T>`** — Wraps loading/error/success states into a single composable. Every list/detail screen uses this.
- **`FleetPullToRefreshBox`** — Pull-to-refresh wrapper for scrollable content.
- **`FleetTopAppBar`** — Standardized TopAppBar with surface background, back button, icon tinting.

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

## Rules

1. **No hardcoded dp/sp** — Use `FleetTokens` for all dimensions.
2. **No duplicate components** — Use the 8 canonical components listed above.
3. **No platform imports in commonMain** — Keep everything multiplatform.
4. **Dark mode mandatory** — Use `MaterialTheme.colorScheme` for all colors.
5. **44dp minimum touch target** — All interactive elements.
6. **This library never depends on `screen-*` modules** — Dependency flows one way.
