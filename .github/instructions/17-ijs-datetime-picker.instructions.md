# ijs-datetime-picker — IndusJS Fleet

## Purpose

Compose date/time picker component. Material 3 styled, multiplatform.

## Package: `com.indusjs.datetimepicker`

## Key Components

| Component | Purpose |
|-----------|---------|
| `FleetDateTimePicker` | Combined date + time picker |
| `DatePickerSection` | Date-only picker |
| `TimePickerSection` | Time-only picker (24hr) |
| `QuickDateShortcuts` | Today, Tomorrow, Next Week |

## Usage

```kotlin
FleetDateTimePicker(
    selectedDate = state.date,
    selectedTime = state.time,
    onDateSelected = { viewModel.sendIntent(Intent.UpdateDate(it)) },
    onTimeSelected = { viewModel.sendIntent(Intent.UpdateTime(it)) }
)
```

## Formats

- Date output: `DD-MM-YYYY`
- Time output: `HH:MM` (24-hour)

## Module Path

`ijs-datetime-picker/src/commonMain/kotlin/com/indusjs/datetimepicker/`

## Depends On: `ijs-datetime-utils`, Compose Multiplatform, Material 3
## Depended On By: `sharedUI` (used in cost entry and trip creation screens)

