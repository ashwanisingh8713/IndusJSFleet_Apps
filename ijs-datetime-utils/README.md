# IJS DateTime Utils

A cross-platform Date & Time utility library for Kotlin Multiplatform applications.

## Features

- 📅 **Date/Time Parsing & Formatting** - Parse and format dates in DD-MM-YYYY HH:mm format
- 🔄 **Difference Calculations** - Calculate duration between two date/times
- 📝 **Relative Descriptions** - "Today at 15:30", "Tomorrow at 10:00", "Next week on Monday"
- ⚖️ **Comparison Utilities** - Check if dates are before/after/equal
- 📆 **Date Checks** - isToday, isTomorrow, isThisWeek, isPast, isFuture, etc.
- ➕ **Date Manipulation** - Add days, weeks, months, hours, minutes
- 🌍 **Timezone Support** - System default timezone with optional custom timezone
- 🔗 **ISO 8601 Conversion** - Convert to/from ISO format for API compatibility
- ✅ **Validation** - Validate date/time strings

## Installation

Add the dependency to your module's `build.gradle.kts`:

```kotlin
implementation(project(":ijs-datetime-utils"))
```

## Usage

### Current Date/Time

```kotlin
val today = FleetDateTime.today()              // "14-01-2026"
val now = FleetDateTime.currentTime()          // "15:30"
val dateTime = FleetDateTime.currentDateTime() // "14-01-2026 15:30"
```

### Parsing

```kotlin
val parsed = FleetDateTime.parse("14-01-2026 15:30")
val fromParts = FleetDateTime.parse("14-01-2026", "15:30")
```

### Difference Calculation

```kotlin
val diff = FleetDateTime.difference(
    startDate = "14-01-2026", startTime = "08:00",
    endDate = "16-01-2026", endTime = "17:30"
)
println(diff?.formatted())      // "2 days 9 hours 30 minutes"
println(diff?.shortFormatted()) // "2d 9h 30m"
```

### Relative Description

```kotlin
FleetDateTime.toRelativeDescription("14-01-2026", "15:30") // "Today at 15:30"
FleetDateTime.toRelativeDescription("15-01-2026", "10:00") // "Tomorrow at 10:00"
FleetDateTime.toRelativeDescription("21-01-2026", "14:00") // "Next week on Tuesday at 14:00"
FleetDateTime.toRelativeDescription("14-02-2026", "09:00") // "Next month on 14th at 09:00"
```

### Comparison

```kotlin
FleetDateTime.isBefore("14-01-2026", "08:00", "14-01-2026", "17:00") // true
FleetDateTime.isAfter("15-01-2026", "10:00", "14-01-2026", "10:00")  // true
```

### Date Checks

```kotlin
FleetDateTime.isToday("14-01-2026")           // true
FleetDateTime.isTomorrow("15-01-2026")        // true
FleetDateTime.isThisWeek("16-01-2026")        // true
FleetDateTime.isFuture("20-01-2026 10:00")    // true
FleetDateTime.isPast("10-01-2026 10:00")      // true
```

### Date Manipulation

```kotlin
FleetDateTime.addDays("14-01-2026", 7)        // "21-01-2026"
FleetDateTime.addWeeks("14-01-2026", 2)       // "28-01-2026"
FleetDateTime.addMonths("14-01-2026", 1)      // "14-02-2026"
FleetDateTime.startOfMonth("14-01-2026")      // "01-01-2026"
FleetDateTime.endOfMonth("14-01-2026")        // "31-01-2026"
```

### ISO 8601 Conversion

```kotlin
// To ISO (for API calls)
FleetDateTime.toIso8601("14-01-2026", "15:30") // "2026-01-14T15:30:00Z"

// From ISO (from API responses)
FleetDateTime.fromIso8601("2026-01-14T15:30:00Z") // FleetDateTimeValue
```

### Validation

```kotlin
FleetDateTime.isValidDate("14-01-2026")       // true
FleetDateTime.isValidDate("32-01-2026")       // false
FleetDateTime.isValidTime("15:30")            // true
FleetDateTime.isValidTime("25:00")            // false
```

## Compatibility

This module is compatible with `ijs-datetime-picker` and uses the same date/time formats:
- Date: `DD-MM-YYYY`
- Time: `HH:mm` (24-hour)
- DateTime: `DD-MM-YYYY HH:mm`

## License

Part of the IndusJS Fleet Management project.

