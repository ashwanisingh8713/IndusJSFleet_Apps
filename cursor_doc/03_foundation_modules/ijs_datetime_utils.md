# ijs-datetime-utils — Date/Time Utilities

**Namespace:** `com.indusjs.datetimeutils`
**Dependencies:** `kotlinx-datetime` only
**Single file:** `FleetDateTime.kt` (~1858 lines)

## Core Types

### `FleetDateTime` (object)

The single entry point for all date/time operations across the project.

**Constants:** `DEFAULT_DATE_FORMAT`, `DEFAULT_TIME_FORMAT`, `DEFAULT_DATETIME_FORMAT`, `ISO_8601_FORMAT`

### `FleetDateTimeValue` (data class)

```kotlin
data class FleetDateTimeValue(
    val year: Int, val month: Int, val day: Int,
    val hour: Int, val minute: Int, val second: Int
)
```

Methods: `toDateString()`, `toTimeString()`, `toDisplayDate()`, `toTime12HourString()`, etc.

### `DateTimeDifference` (data class)

```kotlin
data class DateTimeDifference(
    val totalDays: Long, val totalHours: Long, val totalMinutes: Long, val totalSeconds: Long,
    val years: Int, val months: Int, val days: Int,
    val hours: Int, val minutes: Int, val seconds: Int,
    val isNegative: Boolean
)
```

Methods: `isZero`, `formatted`, `shortFormatted`, `humanReadable`

## Function Categories

### Now / Today
- `now(TimeZone?)`, `today(TimeZone?)`, `currentTime(TimeZone?)`, `currentDateTime(TimeZone?)`

### Parsing
- `parse(dateTime)`, `parse(date, time)`, `parseDate(date)`, `parseTime(time)`

### Formatting
- `formatDate(value)`, `formatTime(value)`, `formatDateTime(value)`
- `formatDisplayDate(value)`, `formatDisplayDateTime(value)`
- `formatTime12Hour(value)` — AM/PM format
- `formatDisplayDateTime12Hour(value)`

### ISO 8601 ↔ Display Conversion
- `toIso8601(date, time)` — DD-MM-YYYY + HH:MM → 2026-03-14T15:30:00Z
- `fromIso8601(isoString)` → FleetDateTimeValue
- `formatIsoToDisplayDate(iso)` → "14-03-2026"
- `formatIsoToTime12Hour(iso)` → "3:30 PM"
- `formatAnyToDisplayDate(anyFormat)` — handles both ISO and DD-MM-YYYY

### Comparison
- `isBefore(dateTime1, dateTime2)`, `isAfter(...)`, `isEqual(...)`
- `isInRange(dateTime, start, end)`, `isDateInRange(date, start, end)`

### Calendar Predicates
- `isToday(date)`, `isTomorrow(...)`, `isYesterday(...)`, `isThisWeek(...)`, `isNextWeek(...)`
- `isPast(dateTime)`, `isFuture(dateTime)`, `isWeekend(date)`, `isWeekday(date)`

### Manipulation
- `addDays(date, n)`, `addWeeks(...)`, `addMonths(...)`, `addYears(...)`
- `addHours(dateTime, n)`, `addMinutes(...)`
- `startOfDay(...)`, `endOfDay(...)`, `startOfWeek(...)`, `endOfWeek(...)`
- `startOfMonth(...)`, `endOfMonth(...)`, `startOfYear(...)`, `endOfYear(...)`

### Calendar Metadata
- `getDayOfWeek(date)`, `getDayOfWeekName(date)`, `getMonthName(month)`
- `getDaysInMonth(year, month)`, `isLeapYear(year)`
- `getFirstDayOfWeekInMonth(year, month)`

### Validation
- `isValidDate(date)`, `isValidTime(time)`, `isValidDateTime(dateTime)`

### Duration Formatting
- `formatDuration(seconds)`, `formatDurationShort(...)`, `formatDurationHumanReadable(...)`

### Cost Date Validation
- `validateCostDateTimeForTrip(...)` — ensures cost date is within trip date range
- `validateMaintenanceCostDate(...)`, `validateDriverCostDate(...)`
- `getMinDateForMaintenance()`, `getMinDateForTripCost()`, `getMaxDateForTripCost()`

### String Extensions
- `"2026-03-14T15:30:00Z".isoToDisplayDate()` → "14-03-2026"
- `"14-03-2026".toDisplayDate()` → formatted
- `"2026-03-14T15:30:00Z".toTime12Hour()` → "3:30 PM"

## Date Format Rules (API Integration)

| Context | Input Format | API Format | Conversion |
|---------|-------------|------------|------------|
| Trip scheduling | DD-MM-YYYY + HH:MM | ISO 8601 | `FleetDateTime.toIso8601(date, time)` |
| Cost entries | DD-MM-YYYY + HH:MM | DD-MM-YYYY + HH:MM | Send as-is |
| Bulk costs | DD-MM-YYYY + HH:MM | ISO 8601 | `TimeUtils.convertFormattedToIsoDateTime()` |
| Document expiry | DD-MM-YYYY | DD-MM-YYYY | Send as-is |
| UI display | Any | DD-MM-YYYY + HH:MM (24hr) | Default format |
