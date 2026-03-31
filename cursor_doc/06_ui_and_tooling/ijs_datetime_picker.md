# ijs-datetime-picker — Date/Time Picker Component

**Namespace:** `com.indusjs.datetimepicker`
**Depends on:** `ijs-datetime-utils` (api), Compose Material 3

## File Tree (5 files, all commonMain)

```
ijs-datetime-picker/src/commonMain/kotlin/com/indusjs/datetimepicker/
├── FleetDateTimePicker.kt    # Main picker field + dialog
├── DatePickerSection.kt      # Calendar grid with month/year pickers
├── TimePickerSection.kt      # Hour/minute wheel pickers
├── QuickDateShortcuts.kt     # Quick date chip row (Today, Tomorrow, etc.)
└── DateTimeUtils.kt          # Thin facade over FleetDateTime
```

## Public Composables

### FleetDateTimePicker

```kotlin
@Composable
fun FleetDateTimePicker(
    date: String,                        // DD-MM-YYYY
    time: String,                        // HH:MM
    onDateTimeChange: (date: String, time: String) -> Unit,
    modifier: Modifier = Modifier,
    mode: PickerMode = PickerMode.DATE_TIME,
    label: String = "Date & Time",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    minDate: String? = null,             // DD-MM-YYYY constraint
    maxDate: String? = null,             // DD-MM-YYYY constraint
    initialDisplayDate: String? = null
)
```

### Convenience Wrappers

```kotlin
@Composable
fun FleetDatePicker(...)   // mode = DATE_ONLY
@Composable
fun FleetTimePicker(...)   // mode = TIME_ONLY
```

### PickerMode Enum
- `DATE_TIME` — Full date + time picker
- `DATE_ONLY` — Calendar only
- `TIME_ONLY` — Time wheels only

## Internal Components

| Component | Purpose |
|-----------|---------|
| `PickerDialog` | Dialog container with header and actions |
| `PreviewSection` | Shows selected date/time preview |
| `DatePickerSection` | Calendar grid, month/year navigation |
| `MonthYearPicker` | Month + year dropdown selectors |
| `CompactCalendarGrid` | Day cells in grid layout |
| `TimePickerSection` | Hour (0-23) and minute (0-59) scroll wheels |
| `TimeWheelColumn` | Individual scrollable wheel column |
| `QuickDateShortcuts` | Chip row: Today, Tomorrow, Next Week, etc. |

## DateTimeUtils Object

Thin facade for picker-internal use, delegates to `FleetDateTime`:
- `getCurrentDate()`, `getCurrentTime()`
- `getDateFromToday(days)`, `formatDateParts(y, m, d)`
- `getDaysInMonth(year, month)`, `isLeapYear(year)`
- `isDayEnabled(date, minDate, maxDate)`
- `parseDate(date) → LocalDate?`, `formatDate(LocalDate)`

## Usage

```kotlin
FleetDateTimePicker(
    date = state.departureDate,
    time = state.departureTime,
    onDateTimeChange = { date, time ->
        viewModel.sendIntent(Intent.UpdateDeparture(date, time))
    },
    label = "Departure",
    minDate = FleetDateTime.today().toDateString(),
    isError = state.departureDateError != null,
    errorMessage = state.departureDateError
)
```

Used by: `screen-vehicle`, `screen-driver`, `screen-trip`, `screen-payment`, `screen-report`, `screen-finance`, `sharedUI`
