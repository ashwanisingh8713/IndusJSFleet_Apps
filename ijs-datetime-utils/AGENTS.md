# AGENTS.md - ijs-datetime-utils

## Purpose

Cross-platform date/time utility library for all IndusJS apps. Single file `FleetDateTime.kt` (~1858 lines) providing comprehensive date/time operations built on `kotlinx-datetime`. This is the **single source of truth** for all date parsing, formatting, conversion, and validation across the fleet app.

**Package:** `com.indusjs.datetimeutils`  
**Targets:** Android, iOS (x64/arm64/simulatorArm64), JS, WasmJS  
**Dependencies:** `kotlinx-datetime` only

---

## Key Object: `FleetDateTime`

All functions are static on the `FleetDateTime` singleton object.

### Default Formats

| Format | Pattern | Example |
|--------|---------|---------|
| Date | `DD-MM-YYYY` | `15-03-2026` |
| Time | `HH:mm` (24hr) | `15:30` |
| DateTime | `DD-MM-YYYY HH:mm` | `15-03-2026 15:30` |
| Display Date | `DD-MMM-YYYY` | `15-Mar-2026` |
| Display Time | `hh:mm AM/PM` | `03:30 PM` |
| ISO 8601 | `YYYY-MM-DDTHH:mm:ssZ` | `2026-03-15T15:30:00Z` |

### Core Functions

**Current date/time:**
```kotlin
FleetDateTime.today()              // "15-03-2026"
FleetDateTime.currentTime()        // "15:30"
FleetDateTime.currentDateTime()    // "15-03-2026 15:30"
FleetDateTime.now()                // FleetDateTimeValue
```

**ISO 8601 conversion (critical for Trip API):**
```kotlin
FleetDateTime.toIso8601("15-03-2026", "15:30")     // "2026-03-15T15:30:00Z"
FleetDateTime.fromIso8601("2026-03-15T15:30:00Z")   // FleetDateTimeValue
```

**Universal format converters (any format → display):**
```kotlin
FleetDateTime.formatAnyToDisplayDate("2026-03-15T15:30:00Z")  // "15-Mar-2026"
FleetDateTime.formatAnyToDisplayDate("2026-03-15")             // "15-Mar-2026"
FleetDateTime.formatAnyToDisplayDate("15-03-2026")             // "15-Mar-2026"
FleetDateTime.formatAnyToTime12Hour("14:30")                   // "02:30 PM"
FleetDateTime.formatAnyToTime12Hour("2026-03-15T14:30:00Z")   // "02:30 PM"
FleetDateTime.formatAnyToDisplayDateTime12Hour("2026-03-15T14:30:00Z") // "15-Mar-2026 02:30 PM"
```

**Relative descriptions:**
```kotlin
FleetDateTime.toRelativeDescription("15-03-2026", "15:30") // "Today at 15:30"
FleetDateTime.toRelativeDescription("16-03-2026", "10:00") // "Tomorrow at 10:00"
```

**Difference calculation:**
```kotlin
val diff = FleetDateTime.difference("14-03-2026", "08:00", "16-03-2026", "17:30")
diff?.formatted()      // "2 days 9 hours 30 minutes"
diff?.shortFormatted() // "2d 9h 30m"
```

**Date checks:** `isToday()`, `isTomorrow()`, `isPast()`, `isFuture()`, `isThisWeek()`

**Date manipulation:** `addDays()`, `addWeeks()`, `addMonths()`, `addHours()`, `addMinutes()`

**Validation:** `isValidDate("15-03-2026")`, `isValidTime("15:30")`

### FleetDateTimeValue (data class)

```kotlin
data class FleetDateTimeValue(
    val year: Int, val month: Int, val day: Int,
    val hour: Int, val minute: Int, val second: Int = 0
)
```

---

## CRITICAL: Date Format Rules for API

| API Endpoint | Date Format | How to Convert |
|-------------|-------------|----------------|
| Trip scheduling | ISO 8601 (`2026-03-15T15:30:00Z`) | `FleetDateTime.toIso8601(date, time)` |
| Cost entries | `DD-MM-YYYY` + `HH:MM` | Send as-is (UI format = API format) |
| Document expiry | `DD-MM-YYYY` | Send as-is |
| UI display (human readable) | `DD-MMM-YYYY` + `hh:mm AM/PM` | `formatAnyToDisplayDate()` / `formatAnyToTime12Hour()` |

**Never send DD-MM-YYYY to a trip scheduling endpoint. Always convert with `toIso8601()`.**
