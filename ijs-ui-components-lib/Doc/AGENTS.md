# ijs-ui-components-lib — AGENTS.md

## Module Overview

Centralized, reusable UI components for the **IndusJS Fleet** KMP app. Every screen-* module and sharedUI depends on this library. Provides theme, typography, color system, input fields, and shared composables so the 40+ screens look and behave consistently.

## Package Structure

```
com.indusjs.uicomponents/
├── components/                  # All reusable Compose components
│   ├── ButtonComponents.kt      # FleetPrimaryButton, FleetSecondaryButton, FleetDestructiveButton, FleetTextButton
│   ├── CardComponents.kt        # FleetCard, FleetItemCard, SectionCard, FleetKeyValueRow
│   ├── CaretakerComponents.kt   # CaretakerSectionCard, CaretakerSearchField
│   ├── CommonComponents.kt      # LoadingContent, ErrorContent, EmptyContent, ScreenContent<T>
│   ├── CostBreakdownComponents.kt # CostBreakdownSection, CostBreakdownRow
│   ├── CostTypeChipSelector.kt  # Cost type dropdown selector
│   ├── DateRangePickerDialog.kt  # Date range picker with presets
│   ├── FinanceComponents.kt     # Finance-specific cards and rows
│   ├── FleetDialogs.kt          # ★ NEW — DiscardChangesDialog, FleetConfirmationDialog, DeleteConfirmationDialog, LogoutConfirmationDialog, StatusToggleConfirmationDialog
│   ├── FleetFilterChipRow.kt    # ★ NEW — Generic horizontal filter chip row with counts
│   ├── FleetPullToRefreshBox.kt  # ★ NEW — Pull-to-refresh wrapper (M3 PullToRefreshBox)
│   ├── FleetSearchBar.kt        # ★ NEW — Standardized search field with clear button
│   ├── FleetShimmerPlaceholder.kt# ★ NEW — Shimmer/skeleton loading presets (list, card, detail, dashboard)
│   ├── FleetSnackbarEffect.kt   # ★ NEW — MVI effect → Snackbar auto-wiring utility
│   ├── FleetStepIndicator.kt    # ★ NEW — Horizontal step/wizard progress indicator
│   ├── FleetTopAppBar.kt        # ★ NEW — Standardized TopAppBar with surface background
│   ├── HistoryComponents.kt     # State history timeline
│   ├── InputComponents.kt       # FleetTextField, FleetPasswordField, FleetEmailField, FleetMobileField, FleetDateField, FleetTimeField, FleetDropdown
│   ├── InputFields.kt           # DateInputField, TimeInputField (VisualTransformation variants)
│   ├── PasswordStrengthIndicator.kt # ★ NEW — Animated password strength meter
│   ├── PhoneComponents.kt       # Phone call utility
│   ├── PieChart.kt              # Animated donut pie chart
│   ├── StateComponents.kt       # StateChangeDialog, StateOptionItem
│   ├── StatusColorUtils.kt      # stateColorSchemeToColor mapping
│   ├── TripCostComponents.kt    # Trip cost entry section
│   ├── UiText.kt                # UiText sealed class for i18n strings
│   └── customer/                # Customer-specific shared components
│       ├── CustomerDetailsSection.kt
│       ├── CustomerSelectionBottomSheet.kt
│       └── SelectedCustomerCard.kt
└── theme/
    ├── Color.kt                 # Light/Dark color palette + FleetColors
    ├── FleetStatusColors.kt     # Semantic status colors (profit/loss/payment/trip)
    ├── Font.kt                  # Poppins font family
    └── Theme.kt                 # AppTheme, FleetTypography, FleetShapes, LocalThemeIsDark

## New Components (UX Enhancement)

### FleetTopAppBar
Standardized TopAppBar with surface background, consistent back button, icon tinting.
```kotlin
FleetTopAppBar(
    title = "Vehicle Detail",
    onNavigateBack = { navController.pop() },
    actions = { IconButton(onClick = { ... }) { Icon(...) } }
)
```

### FleetDialogs
Pre-built confirmation dialogs for destructive actions.
```kotlin
DiscardChangesDialog(showDialog, onDiscard = { navigateBack() }, onKeepEditing = { dismiss() })
DeleteConfirmationDialog(showDialog, entityName = "vehicle", entityDetail = "MH-12-AB-1234", onConfirmDelete = { ... }, onDismiss = { ... })
LogoutConfirmationDialog(showDialog, onConfirmLogout = { ... }, onDismiss = { ... })
StatusToggleConfirmationDialog(showDialog, entityName = "driver", currentlyActive = true, onConfirm = { ... }, onDismiss = { ... })
```

### FleetFilterChipRow
Generic horizontal scrolling filter chip row with optional counts.
```kotlin
FleetFilterChipRow(
    options = listOf(
        FilterChipOption("all", "All", count = 25),
        FilterChipOption("active", "Active", count = 18)
    ),
    selectedValue = state.filter,
    onSelected = { viewModel.sendIntent(Intent.FilterBy(it)) }
)
```

### FleetShimmerPlaceholder
Shimmer/skeleton loading presets for list, card, detail, and dashboard screens.
```kotlin
ShimmerListScreen(itemCount = 6, showFilterChips = true)
ShimmerDetailScreen(sectionCount = 3)
ShimmerDashboard()
ShimmerCard(contentHeight = 80.dp)
ShimmerListItem(showAvatar = true, showTrailing = true)
```

### FleetPullToRefreshBox
Pull-to-refresh wrapper.
```kotlin
FleetPullToRefreshBox(
    isRefreshing = state.isRefreshing,
    onRefresh = { viewModel.sendIntent(Intent.Refresh) }
) {
    LazyColumn { ... }
}
```

### FleetSearchBar
Standardized search field with leading search icon and animated clear button.
```kotlin
FleetSearchBar(
    query = state.searchQuery,
    onQueryChange = { viewModel.sendIntent(Intent.Search(it)) },
    placeholder = "Search vehicles…"
)
```

### PasswordStrengthIndicator
Animated password strength meter with color bar.
```kotlin
PasswordStrengthIndicator(password = state.password)
```

### FleetSnackbarEffect
Auto-wire MVI effects to SnackbarHostState.
```kotlin
val snackbarHostState = rememberFleetSnackbarHostState()
FleetSnackbarEffect(snackbarHostState, viewModel.effect) { effect ->
    when (effect) {
        is Effect.ShowSnackbar -> SnackbarMessage(effect.message)
        else -> null
    }
}
```

### FleetStepIndicator
Horizontal step/wizard progress indicator.
```kotlin
FleetStepIndicator(
    steps = listOf(StepInfo("Schedule"), StepInfo("Route"), StepInfo("Cargo"), StepInfo("Review")),
    currentStep = state.currentStep
)
```
```

