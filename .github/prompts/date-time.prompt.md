# Date & Time Handling

> Use this prompt for all date/time formatting, parsing, and API conversion.

## Golden Rule

- **UI** always works with `DD-MM-YYYY` and `HH:MM` (24hr) strings
- **API** for trips uses ISO 8601: `YYYY-MM-DDTHH:mm:ssZ`
- **API** for costs/documents sends `DD-MM-YYYY` and `HH:MM` as-is
- **Display** uses `DD-MMM-YYYY` and `hh:mm AM/PM` for human reading

## FleetDateTime (Single Source of Truth)

Import: `com.indusjs.datetimeutils.FleetDateTime`

### Get Current Date/Time

```kotlin
FleetDateTime.today()              // "15-03-2026"
FleetDateTime.currentTime()        // "15:30"
FleetDateTime.now()                // FleetDateTimeValue
```

### Convert for API

```kotlin
// Trip scheduling → ISO 8601
FleetDateTime.toIso8601("15-03-2026", "15:30")
// → "2026-03-15T15:30:00Z"

// Cost entries → send as-is
// "date": "15-03-2026", "time": "15:30"
```

### Format for Display

```kotlin
// Any format → human-readable date
FleetDateTime.formatAnyToDisplayDate("2026-03-15T15:30:00Z")  // "15-Mar-2026"
FleetDateTime.formatAnyToDisplayDate("2026-03-15")             // "15-Mar-2026"
FleetDateTime.formatAnyToDisplayDate("15-03-2026")             // "15-Mar-2026"

// Any format → 12-hour time
FleetDateTime.formatAnyToTime12Hour("14:30")                   // "02:30 PM"
FleetDateTime.formatAnyToTime12Hour("2026-03-15T14:30:00Z")   // "02:30 PM"

// Combined
FleetDateTime.formatAnyToDisplayDateTime12Hour("2026-03-15T14:30:00Z")
// → "15-Mar-2026 02:30 PM"
```

### Parse ISO from API

```kotlin
val value = FleetDateTime.fromIso8601("2026-03-15T15:30:00Z")
// → FleetDateTimeValue(year=2026, month=3, day=15, hour=15, minute=30)
```

### Relative Descriptions

```kotlin
FleetDateTime.toRelativeDescription("15-03-2026", "15:30")
// → "Today at 15:30" / "Tomorrow at 10:00" / "In 3 days"
```

### Validation

```kotlin
FleetDateTime.isValidDate("15-03-2026")  // true
FleetDateTime.isValidDate("32-13-2026")  // false
FleetDateTime.isValidTime("15:30")       // true
FleetDateTime.isValidTime("25:61")       // false
```

### Date Arithmetic

```kotlin
FleetDateTime.addDays("15-03-2026", 7)    // "22-03-2026"
FleetDateTime.addMonths("15-03-2026", 1)  // "15-04-2026"
FleetDateTime.isPast("01-01-2025")        // true
FleetDateTime.isFuture("01-01-2027")      // true
```

## Format Reference Table

| Context | Date Format | Time Format | Conversion |
|---------|-------------|-------------|------------|
| Trip scheduling API | ISO 8601 | ISO 8601 | `FleetDateTime.toIso8601(date, time)` |
| Cost entry API | `DD-MM-YYYY` | `HH:MM` | Send as-is |
| Document expiry API | `DD-MM-YYYY` | N/A | Send as-is |
| UI input fields | `DD-MM-YYYY` | `HH:MM` (24hr) | Default |
| UI display (human) | `DD-MMM-YYYY` | `hh:mm AM/PM` | `formatAnyToDisplayDate()` |
| FleetDateTimePicker | `DD-MM-YYYY` | `HH:MM` | Built-in |

## UI Components

```kotlin
// Date picker
FleetDateTimePicker(
    date = state.date, time = state.time,
    onDateTimeChange = { d, t -> /* update state */ },
    mode = PickerMode.DATE_TIME,
    minDate = FleetDateTime.today()
)

// Date input field (auto-formats with delimiters)
FleetDateField(value = state.date, onValueChange = { ... }, label = "Date")

// Time input field (auto-formats with colon)
FleetTimeField(value = state.time, onValueChange = { ... }, label = "Time")
```

## Common Mistakes to Avoid

❌ `"15-03-2026"` sent to trip scheduling endpoint → **WRONG**
✅ `FleetDateTime.toIso8601("15-03-2026", "15:30")` → `"2026-03-15T15:30:00Z"` → **CORRECT**

❌ `String.format()` for number formatting → **Not KMP-compatible**
✅ `kotlin.math.round()` or manual formatting → **KMP-safe**

❌ `java.time.LocalDate` → **Not available on iOS/JS/WASM**
✅ `kotlinx.datetime.LocalDate` or `FleetDateTime` → **KMP-safe**

