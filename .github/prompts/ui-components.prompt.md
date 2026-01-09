# UI Components Prompt

## Overview

IndusJS Fleet uses a consistent set of reusable UI components built on Material 3. All components should be used from `core/ui/` to maintain consistency.

---

## Input Components

### FleetTextField
**Location:** `core/ui/FleetTextField.kt`
**Use Case:** Standard text input

```kotlin
FleetTextField(
    value = state.name,
    onValueChange = { viewModel.sendIntent(Intent.UpdateName(it)) },
    label = "Vehicle Name",
    placeholder = "Enter name",
    isError = state.nameError != null,
    errorMessage = state.nameError,
    singleLine = true
)
```

---

### FleetDateField
**Location:** `core/ui/FleetDateField.kt`
**Use Case:** Date input with DD-MM-YYYY format

**Features:**
- Auto-adds delimiters (DD-MM-YYYY)
- Validates date format
- Shows hint text "DD-MM-YYYY"

```kotlin
FleetDateField(
    value = state.departureDate,
    onValueChange = { viewModel.sendIntent(Intent.UpdateDepartureDate(it)) },
    label = "Departure Date",
    placeholder = "DD-MM-YYYY",
    isError = state.departureDateError != null,
    errorMessage = state.departureDateError
)
```

---

### FleetTimeField
**Location:** `core/ui/FleetTimeField.kt`
**Use Case:** Time input with HH:MM 24-hour format

**Features:**
- Auto-adds colon delimiter
- Validates 24-hour format
- Shows hint text "HH:MM"
- Label shows "Time (24hr)"

```kotlin
FleetTimeField(
    value = state.departureTime,
    onValueChange = { viewModel.sendIntent(Intent.UpdateDepartureTime(it)) },
    label = "Departure Time",
    placeholder = "HH:MM",
    isError = state.departureTimeError != null,
    errorMessage = state.departureTimeError
)
```

---

### FleetMobileField
**Location:** `core/ui/FleetMobileField.kt`
**Use Case:** 10-digit mobile number input

**Features:**
- Numeric keyboard
- 10 digit limit
- Validates mobile format

```kotlin
FleetMobileField(
    value = state.mobile,
    onValueChange = { viewModel.sendIntent(Intent.UpdateMobile(it)) },
    label = "Mobile Number",
    isError = state.mobileError != null,
    errorMessage = state.mobileError
)
```

---

### FleetEmailField
**Location:** `core/ui/FleetEmailField.kt`
**Use Case:** Email input with validation

**Features:**
- Email keyboard
- Validates email format

```kotlin
FleetEmailField(
    value = state.email,
    onValueChange = { viewModel.sendIntent(Intent.UpdateEmail(it)) },
    label = "Email Address",
    isError = state.emailError != null,
    errorMessage = state.emailError
)
```

---

### FleetPasswordField
**Location:** `core/ui/FleetPasswordField.kt`
**Use Case:** Password input with visibility toggle

**Features:**
- Visibility toggle icon
- Secure text entry

```kotlin
FleetPasswordField(
    value = state.password,
    onValueChange = { viewModel.sendIntent(Intent.UpdatePassword(it)) },
    label = "Password",
    isError = state.passwordError != null,
    errorMessage = state.passwordError
)
```

---

## State Components

### LoadingContent
**Location:** `core/ui/LoadingContent.kt`
**Use Case:** Show loading spinner

```kotlin
LoadingContent(message = "Loading vehicles...")
```

---

### ErrorContent
**Location:** `core/ui/ErrorContent.kt`
**Use Case:** Show error with retry button

```kotlin
ErrorContent(
    error = state.error,
    screenContext = FleetErrorContext.VEHICLES,
    onRetry = { viewModel.sendIntent(Intent.LoadVehicles) }
)
```

**Screen Contexts:**
- `DASHBOARD`, `VEHICLES`, `DRIVERS`, `TRIPS`, `COSTS`, `AUTH`

---

### EmptyContent
**Location:** `core/ui/EmptyContent.kt`
**Use Case:** Show empty state with action

```kotlin
EmptyContent(
    iconRes = Res.drawable.ic_vehicle,
    title = "No vehicles yet",
    message = "Add your first vehicle to start tracking",
    actionLabel = "Add Vehicle",
    onAction = { viewModel.sendIntent(Intent.AddVehicle) }
)
```

---

### ScreenContent
**Location:** `core/ui/ScreenContent.kt`
**Use Case:** Wrapper handling loading/error/content states

```kotlin
ScreenContent(
    isLoading = state.isLoading,
    error = state.error,
    screenContext = FleetErrorContext.VEHICLES,
    onRetry = { viewModel.sendIntent(Intent.LoadData) }
) {
    // Your content here
    LazyColumn { ... }
}
```

---

## Card Components

### FleetCard
**Location:** `core/ui/FleetCard.kt`
**Use Case:** Standard card with consistent styling

```kotlin
FleetCard(
    modifier = Modifier.fillMaxWidth(),
    onClick = { /* optional click handler */ }
) {
    // Card content
}
```

---

### SectionCard
**Use Case:** Section with title and content

```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Header
        Row {
            Icon(...)
            Text("Section Title", fontWeight = FontWeight.Bold)
        }
        // Content
        ...
    }
}
```

---

## Button Components

### FleetPrimaryButton
**Use Case:** Primary action button

```kotlin
FleetPrimaryButton(
    text = "Save Vehicle",
    onClick = { viewModel.sendIntent(Intent.Save) },
    enabled = state.isValid,
    isLoading = state.isSaving
)
```

---

### FleetSecondaryButton
**Use Case:** Secondary action button

```kotlin
FleetSecondaryButton(
    text = "Cancel",
    onClick = onNavigateBack
)
```

---

### FilledTonalButton
**Use Case:** Medium emphasis action

```kotlin
FilledTonalButton(
    onClick = { /* action */ },
    modifier = Modifier.height(48.dp),
    shape = RoundedCornerShape(12.dp)
) {
    Icon(painterResource(Res.drawable.ic_add), null)
    Spacer(Modifier.width(8.dp))
    Text("Add Cost")
}
```

---

## Status Components

### StatusChip
**Use Case:** Show status with color

```kotlin
Surface(
    shape = RoundedCornerShape(8.dp),
    color = statusColor.copy(alpha = 0.1f)
) {
    Text(
        text = statusLabel,
        color = statusColor,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    )
}
```

**Status Colors:**
- Active/On Route: `Color(0xFF4CAF50)` (Green)
- Planned: `Color(0xFF2196F3)` (Blue)
- Available: `MaterialTheme.colorScheme.tertiary`
- Maintenance: `Color(0xFFFF9800)` (Orange)
- Inactive/Cancelled: `Color(0xFF9E9E9E)` (Gray)
- Error/Expired: `MaterialTheme.colorScheme.error`

---

### AlertBadge
**Use Case:** Show alert priority

```kotlin
Surface(
    shape = RoundedCornerShape(6.dp),
    color = alertColor.copy(alpha = 0.12f)
) {
    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text("$count", fontWeight = FontWeight.Bold, color = alertColor)
        Text(" Critical", color = alertColor)
    }
}
```

---

## Navigation Components

### TopAppBar Pattern
```kotlin
TopAppBar(
    title = { Text("Screen Title") },
    navigationIcon = {
        IconButton(onClick = onNavigateBack) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    },
    actions = {
        IconButton(onClick = { /* refresh */ }) {
            Icon(painterResource(Res.drawable.ic_refresh), "Refresh")
        }
    },
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
)
```

---

### FloatingActionButton Pattern
```kotlin
FloatingActionButton(
    onClick = { viewModel.sendIntent(Intent.AddItem) },
    containerColor = MaterialTheme.colorScheme.primary
) {
    Icon(
        painter = painterResource(Res.drawable.ic_add),
        contentDescription = "Add"
    )
}
```

---

## Icons Reference

| Icon | Resource | Use |
|------|----------|-----|
| Back | `Res.drawable.ic_arrow_back` | Navigation back |
| Menu | `Res.drawable.ic_menu` | Hamburger menu |
| Add | `Res.drawable.ic_add` | Add action |
| Edit | `Res.drawable.ic_edit` | Edit action |
| Delete | `Res.drawable.ic_delete` | Delete action |
| Close | `Res.drawable.ic_close` | Close/dismiss |
| Refresh | `Res.drawable.ic_refresh` | Refresh data |
| Check | `Res.drawable.ic_check` | Success/done |
| Vehicle | `Res.drawable.ic_vehicle` | Vehicle entity |
| Truck | `Res.drawable.ic_truck` | Vehicle icon |
| Driver | `Res.drawable.ic_driver` | Driver entity |
| Trip | `Res.drawable.ic_trip` | Trip entity |
| Map | `Res.drawable.ic_map` | Maps/location |
| Settings | `Res.drawable.ic_settings` | Settings |
| Notifications | `Res.drawable.ic_notifications` | Alerts |
| ChevronRight | `Res.drawable.ic_chevron_right` | Navigate/expand |

---

## Theme Colors

### Primary Palette
```kotlin
MaterialTheme.colorScheme.primary          // Primary brand color
MaterialTheme.colorScheme.onPrimary        // Text on primary
MaterialTheme.colorScheme.primaryContainer // Light primary background
MaterialTheme.colorScheme.onPrimaryContainer
```

### Surface Colors
```kotlin
MaterialTheme.colorScheme.surface          // Card backgrounds
MaterialTheme.colorScheme.surfaceVariant   // Subtle backgrounds
MaterialTheme.colorScheme.onSurface        // Text on surface
MaterialTheme.colorScheme.onSurfaceVariant // Secondary text
```

### Status Colors
```kotlin
MaterialTheme.colorScheme.error            // Error/critical
MaterialTheme.colorScheme.tertiary         // Warning/available
Color(0xFF4CAF50)                          // Success/active (Green)
Color(0xFF2196F3)                          // Info/planned (Blue)
Color(0xFFFF9800)                          // Caution (Orange)
Color(0xFF9E9E9E)                          // Inactive (Gray)
```

---

## Spacing Guidelines

| Size | Value | Use |
|------|-------|-----|
| XS | 4.dp | Icon spacing |
| S | 8.dp | Element spacing |
| M | 12.dp | Section spacing |
| L | 16.dp | Card padding |
| XL | 24.dp | Screen padding |

---

## Typography

```kotlin
MaterialTheme.typography.headlineLarge   // Screen titles
MaterialTheme.typography.titleLarge      // Section titles
MaterialTheme.typography.titleMedium     // Card titles
MaterialTheme.typography.bodyLarge       // Body text
MaterialTheme.typography.bodyMedium      // Secondary text
MaterialTheme.typography.labelMedium     // Labels
MaterialTheme.typography.labelSmall      // Captions
```

