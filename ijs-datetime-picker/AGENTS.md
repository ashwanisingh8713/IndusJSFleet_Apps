# AGENTS.md - ijs-datetime-picker

## Purpose

Cross-platform Compose Multiplatform date/time picker UI component. Provides a Material 3 dialog-based picker with calendar grid, time wheel selectors, and quick-select shortcuts ("Today", "Tomorrow", "Next Week"). Used across the fleet app for all date/time input in trip scheduling, cost entry, document expiry, etc.

**Package:** `com.indusjs.datetimepicker`  
**Targets:** Android, iOS (x64/arm64/simulatorArm64), JS, WasmJS  
**Dependencies:** `ijs-datetime-utils` (API — re-exports `FleetDateTime`), Compose Multiplatform, `kotlinx-datetime`

---

## Source Tree

```
src/commonMain/kotlin/com/indusjs/datetimepicker/
├── FleetDateTimePicker.kt   # Main composable entry point
├── DatePickerSection.kt     # Calendar grid UI (month navigation, day selection)
├── TimePickerSection.kt     # Hour/minute wheel selectors (24hr)
├── QuickDateShortcuts.kt    # "Today", "Tomorrow", "Next Week" chips
└── DateTimeUtils.kt         # Display formatting helpers
```

---

## Key Composable

```kotlin
@Composable
fun FleetDateTimePicker(
    date: String,                        // DD-MM-YYYY format
    time: String,                        // HH:MM format (24hr)
    onDateTimeChange: (date: String, time: String) -> Unit,
    mode: PickerMode = PickerMode.DATE_TIME,
    label: String = "Date & Time",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    minDate: String? = null,             // Optional minimum date constraint
    maxDate: String? = null,             // Optional maximum date constraint
    initialDisplayDate: String? = null   // Which month to show initially
)
```

### PickerMode

```kotlin
enum class PickerMode {
    DATE_TIME,  // Both date and time
    DATE_ONLY,  // Only date (time section hidden)
    TIME_ONLY   // Only time (Quick Shortcuts hidden)
}
```

---

## Usage in Fleet App

```kotlin
// Trip scheduling — pick departure date and time
FleetDateTimePicker(
    date = state.departureDate,      // "15-03-2026"
    time = state.departureTime,      // "15:30"
    onDateTimeChange = { date, time ->
        viewModel.sendIntent(Intent.UpdateDepartureDate(date))
        viewModel.sendIntent(Intent.UpdateDepartureTime(time))
    },
    mode = PickerMode.DATE_TIME,
    label = "Departure",
    minDate = FleetDateTime.today()  // Can't pick past dates
)

// Cost entry — pick date only
FleetDateTimePicker(
    date = state.costDate,
    time = "",
    onDateTimeChange = { date, _ -> viewModel.sendIntent(Intent.UpdateDate(date)) },
    mode = PickerMode.DATE_ONLY,
    label = "Cost Date"
)

// Document expiry — date only
FleetDateTimePicker(
    date = state.expiryDate,
    time = "",
    onDateTimeChange = { date, _ -> viewModel.sendIntent(Intent.UpdateExpiry(date)) },
    mode = PickerMode.DATE_ONLY,
    label = "Expiry Date"
)
```

**All dates flow as `DD-MM-YYYY` strings. Convert to ISO 8601 only when sending to trip scheduling API.**
