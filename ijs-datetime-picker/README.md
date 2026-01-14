# IJS DateTime Picker

A cross-platform DateTime picker library for Kotlin Multiplatform Compose applications.

## Features

- 📅 **Unified Date & Time Selection** - Both date and time picker visible in a single popup
- 🕐 **Picker Modes** - Support for Date+Time, Date only, or Time only selection
- 🚀 **Quick Shortcuts** - Jump to Today, Tomorrow, Next Week, Next Month with one tap
- 🎨 **Material 3 Design** - Follows Material Design 3 guidelines
- 📱 **Adaptive Layout** - Automatically adjusts for portrait/landscape orientations
- 🌍 **Cross-Platform** - Works on Android, iOS, Desktop, and Web
- ⚡ **24-hour Format** - Uses 24-hour time format
- 📆 **Dynamic Date Format** - Supports DD-MM-YYYY format

## Installation

Add the dependency to your module's `build.gradle.kts`:

```kotlin
implementation(project(":ijs-datetime-picker"))
```

## Usage

### Basic Usage (Date & Time)

```kotlin
var date by remember { mutableStateOf("") }
var time by remember { mutableStateOf("") }

FleetDateTimePicker(
    date = date,
    time = time,
    onDateTimeChange = { newDate, newTime ->
        date = newDate
        time = newTime
    }
)
```

### Date Only Picker

```kotlin
var date by remember { mutableStateOf("") }

// Using convenience function
FleetDatePicker(
    date = date,
    onDateChange = { newDate -> date = newDate },
    label = "Select Date"
)

// Or using mode parameter
FleetDateTimePicker(
    date = date,
    time = "",
    onDateTimeChange = { newDate, _ -> date = newDate },
    mode = PickerMode.DATE_ONLY
)
```

### Time Only Picker

```kotlin
var time by remember { mutableStateOf("") }

// Using convenience function
FleetTimePicker(
    time = time,
    onTimeChange = { newTime -> time = newTime },
    label = "Select Time"
)

// Or using mode parameter
FleetDateTimePicker(
    date = "",
    time = time,
    onDateTimeChange = { _, newTime -> time = newTime },
    mode = PickerMode.TIME_ONLY
)
```

### With Validation

```kotlin
FleetDateTimePicker(
    date = state.departureDate,
    time = state.departureTime,
    onDateTimeChange = { newDate, newTime ->
        viewModel.updateDateTime(newDate, newTime)
    },
    label = "Departure Date & Time",
    isError = state.dateError != null,
    errorMessage = state.dateError,
    minDate = "01-01-2026",
    maxDate = "31-12-2026"
)
```

## Picker Modes

| Mode | Description | Quick Select |
|------|-------------|--------------|
| `PickerMode.DATE_TIME` | Both date and time selection (default) | ✅ Visible |
| `PickerMode.DATE_ONLY` | Only date selection | ✅ Visible |
| `PickerMode.TIME_ONLY` | Only time selection | ❌ Hidden |

## Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `date` | `String` | Required | Current date in DD-MM-YYYY format |
| `time` | `String` | Required | Current time in HH:MM format (24-hour) |
| `onDateTimeChange` | `(String, String) -> Unit` | Required | Callback when date or time changes |
| `modifier` | `Modifier` | `Modifier` | Modifier for the container |
| `mode` | `PickerMode` | `DATE_TIME` | Selection mode (DATE_TIME, DATE_ONLY, TIME_ONLY) |
| `label` | `String` | Auto based on mode | Label text above the picker |
| `enabled` | `Boolean` | `true` | Whether the picker is enabled |
| `isError` | `Boolean` | `false` | Whether to show error state |
| `errorMessage` | `String?` | `null` | Error message to display |
| `minDate` | `String?` | `null` | Minimum selectable date (DD-MM-YYYY) |
| `maxDate` | `String?` | `null` | Maximum selectable date (DD-MM-YYYY) |

## Quick Shortcuts

The picker includes four quick date shortcuts (visible for DATE_TIME and DATE_ONLY modes):

- **Today** - Selects current date
- **Tomorrow** - Selects next day
- **+7 Days** - Selects date 7 days ahead
- **+30 Days** - Selects date 30 days ahead

Shortcuts are automatically disabled if they fall outside the specified min/max date range.

## Preview Display

The input field and dialog preview adapt based on mode:

| Mode | Input Display | Dialog Preview |
|------|--------------|----------------|
| DATE_TIME | `15-01-2026  \|  14:30` | `15-01-2026  \|  14:30` |
| DATE_ONLY | `15-01-2026` | `15-01-2026` |
| TIME_ONLY | `14:30` | `14:30` |

## License

Part of the IndusJS Fleet Management project.
