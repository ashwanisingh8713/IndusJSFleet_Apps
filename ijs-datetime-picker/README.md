# IJS DateTime Picker

A cross-platform DateTime picker library for Kotlin Multiplatform Compose applications.

## Features

- 📅 **Unified Date & Time Selection** - Both date and time picker visible in a single popup
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

### Basic Usage

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

### Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `date` | `String` | Required | Current date in DD-MM-YYYY format |
| `time` | `String` | Required | Current time in HH:MM format (24-hour) |
| `onDateTimeChange` | `(String, String) -> Unit` | Required | Callback when date or time changes |
| `modifier` | `Modifier` | `Modifier` | Modifier for the container |
| `label` | `String` | `"Date & Time"` | Label text above the picker |
| `enabled` | `Boolean` | `true` | Whether the picker is enabled |
| `isError` | `Boolean` | `false` | Whether to show error state |
| `errorMessage` | `String?` | `null` | Error message to display |
| `minDate` | `String?` | `null` | Minimum selectable date (DD-MM-YYYY) |
| `maxDate` | `String?` | `null` | Maximum selectable date (DD-MM-YYYY) |

## Quick Shortcuts

The picker includes four quick date shortcuts at the bottom:

- **Today** - Selects current date
- **Tomorrow** - Selects next day
- **Next Week** - Selects date 7 days ahead
- **Next Month** - Selects date 30 days ahead

Shortcuts are automatically disabled if they fall outside the specified min/max date range.

## License

Part of the IndusJS Fleet Management project.

