# Plan: Move Date Formatting Functions to ijs-datetime-utils

## Status: ✅ COMPLETED

## Overview

Centralize all date/time formatting functions in `FleetDateTime.kt` (ijs-datetime-utils) and update screens to use these centralized functions instead of duplicating formatting logic across TimeUtils.kt and screen files.

---

## Analysis

### Current State

1. **TimeUtils.kt** contains several date formatting functions that duplicate or wrap FleetDateTime functionality:
   - `formatCostTime()` - Converts any time format to 12-hour AM/PM
   - `formatDateTimeForDisplay()` - Combines date and time formatting
   - `formatDateToHumanReadable()` - Already delegates to FleetDateTime
   - `formatLastUpdated()` - Already uses FleetDateTime
   - `getCurrentFormattedDateHumanReadable()` - Already uses FleetDateTime
   - `getCurrentFormattedTime()` - Already uses FleetDateTime

2. **Screen-level duplicate functions**:
   - `VehicleFinanceDetailScreen.kt` - `formatDueDateDisplay()` handles ISO, YYYY-MM-DD, DD-MM-YYYY
   - `VehicleFinanceScreen.kt` - `formatDueDateDisplay()` (duplicate)
   - `EmiPaymentHistoryScreen.kt` - `formatDateDisplay()` (similar logic)

3. **Missing in FleetDateTime**:
   - No function to handle YYYY-MM-DD format parsing
   - No unified "format any date format" function
   - No function to extract and format time from any format

---

## Changes Required

### 1. Add New Functions to FleetDateTime.kt

**Location:** After `formatToDisplayDate()` function (around line 258)

Add these functions:

```kotlin
/**
 * Format any date format to DD-MMM-YYYY display format.
 * Supports: ISO 8601, YYYY-MM-DD, DD-MM-YYYY formats.
 * Examples:
 * - "2026-02-05T14:30:00Z" -> "05-Feb-2026"
 * - "2026-02-05" -> "05-Feb-2026"
 * - "05-02-2026" -> "05-Feb-2026"
 */
fun formatAnyToDisplayDate(dateString: String?): String

/**
 * Format ISO 8601 datetime string to 12-hour time only (e.g., "02:30 PM").
 */
fun formatIsoToTime12Hour(isoString: String?): String

/**
 * Format any time format to 12-hour format with AM/PM.
 * Supports: ISO 8601, HH:mm, HH:mm:ss formats.
 * Examples:
 * - "2026-02-05T14:30:00Z" -> "02:30 PM"
 * - "14:30" -> "02:30 PM"
 * - "14:30:00" -> "02:30 PM"
 */
fun formatAnyToTime12Hour(timeString: String?): String

/**
 * Format any datetime format to "DD-MMM-YYYY hh:mm AM/PM".
 * Supports: ISO 8601, separate date/time strings.
 * Examples:
 * - formatAnyToDisplayDateTime12Hour("2026-02-05T14:30:00Z") -> "05-Feb-2026 02:30 PM"
 * - formatAnyToDisplayDateTime12Hour("05-02-2026", "14:30") -> "05-Feb-2026 02:30 PM"
 */
fun formatAnyToDisplayDateTime12Hour(dateString: String?, timeString: String? = null): String
```

---

### 2. Update TimeUtils.kt

**Remove these functions** (replace with FleetDateTime delegations):

| Old Function | New Implementation |
|--------------|-------------------|
| `formatCostTime()` | `FleetDateTime.formatAnyToTime12Hour()` |
| `formatDateTimeForDisplay()` | `FleetDateTime.formatAnyToDisplayDateTime12Hour()` |
| `formatDateToHumanReadable()` | `FleetDateTime.formatAnyToDisplayDate()` |

**Simplified TimeUtils.kt content:**

```kotlin
package com.indusjs.fleet.core.util

import com.indusjs.datetimeutils.FleetDateTime

expect fun currentTimeMillis(): Long

fun formatRelativeTime(timestamp: Long?): String { /* keep existing */ }

fun formatLastUpdated(isoDateString: String?): String { /* keep existing */ }

// ISO 8601 Conversion
fun convertToIsoDateTime(rawDate: String, rawTime: String = ""): String { /* keep existing */ }
fun convertFormattedToIsoDateTime(formattedDate: String, formattedTime: String = ""): String { /* keep existing */ }

// Delegate to FleetDateTime
fun getCurrentFormattedDateHumanReadable(): String = FleetDateTime.formatDisplayDate(FleetDateTime.now())
fun getCurrentFormattedTime(): String = FleetDateTime.formatTime12Hour(FleetDateTime.now())
fun formatDateToHumanReadable(dateString: String?, shortMonth: Boolean = false): String = 
    FleetDateTime.formatAnyToDisplayDate(dateString)
fun formatCostTime(timeString: String?): String = FleetDateTime.formatAnyToTime12Hour(timeString)
fun formatDateTimeForDisplay(date: String?, time: String? = null): String = 
    FleetDateTime.formatAnyToDisplayDateTime12Hour(date, time)

// Keep cost formatting
fun formatCostAmount(amount: Double): String { /* keep existing */ }
private fun formatWithThousandsSeparator(number: Long): String { /* keep existing */ }
```

---

### 3. Update Screen Files

#### VehicleFinanceDetailScreen.kt

**Replace** the `formatDueDateDisplay()` function (lines ~816-845):

```kotlin
private fun formatDueDateDisplay(dateString: String?): String =
    FleetDateTime.formatAnyToDisplayDate(dateString)
```

#### VehicleFinanceScreen.kt

**Replace** the `formatDueDateDisplay()` function (lines ~854-888):

```kotlin
private fun formatDueDateDisplay(dateString: String?): String =
    FleetDateTime.formatAnyToDisplayDate(dateString)
```

#### EmiPaymentHistoryScreen.kt

**Replace** the `formatDateDisplay()` function (lines ~396-410):

```kotlin
private fun formatDateDisplay(dateString: String?): String =
    FleetDateTime.formatAnyToDisplayDate(dateString)
```

---

### 4. Update Documentation

**File:** `Docs/modules/datetime-functionality.md`

Add to the **Display Formatting** table:

| Function | Description | Returns |
|----------|-------------|---------|
| `formatAnyToDisplayDate(dateString: String?)` | Any format to DD-MMM-YYYY | `String` |
| `formatIsoToTime12Hour(isoString: String?)` | ISO to "hh:mm AM/PM" (time only) | `String` |
| `formatAnyToTime12Hour(timeString: String?)` | Any time format to "hh:mm AM/PM" | `String` |
| `formatAnyToDisplayDateTime12Hour(dateString: String?, timeString: String?)` | Any format to "DD-MMM-YYYY hh:mm AM/PM" | `String` |

Add usage example:

```kotlin
// Format any date format to display
val display1 = FleetDateTime.formatAnyToDisplayDate("2026-02-05T14:30:00Z") // "05-Feb-2026"
val display2 = FleetDateTime.formatAnyToDisplayDate("2026-02-05")           // "05-Feb-2026"
val display3 = FleetDateTime.formatAnyToDisplayDate("05-02-2026")           // "05-Feb-2026"

// Format any time format to 12-hour
val time1 = FleetDateTime.formatAnyToTime12Hour("14:30")                    // "02:30 PM"
val time2 = FleetDateTime.formatAnyToTime12Hour("2026-02-05T14:30:00Z")     // "02:30 PM"

// Format any datetime to display
val dt1 = FleetDateTime.formatAnyToDisplayDateTime12Hour("2026-02-05T14:30:00Z")       // "05-Feb-2026 02:30 PM"
val dt2 = FleetDateTime.formatAnyToDisplayDateTime12Hour("05-02-2026", "14:30")        // "05-Feb-2026 02:30 PM"
```

---

## Files to Modify

| File | Action |
|------|--------|
| `ijs-datetime-utils/.../FleetDateTime.kt` | Add 4 new functions |
| `sharedUI/.../core/util/TimeUtils.kt` | Simplify, delegate to FleetDateTime |
| `sharedUI/.../finance/VehicleFinanceDetailScreen.kt` | Replace formatDueDateDisplay |
| `sharedUI/.../finance/VehicleFinanceScreen.kt` | Replace formatDueDateDisplay |
| `sharedUI/.../finance/EmiPaymentHistoryScreen.kt` | Replace formatDateDisplay |
| `Docs/modules/datetime-functionality.md` | Add new function documentation |

---

## Implementation Order

1. Add new functions to `FleetDateTime.kt`
2. Build `ijs-datetime-utils` to verify compilation
3. Update `TimeUtils.kt` to delegate to FleetDateTime
4. Update screen files (VehicleFinanceDetailScreen, VehicleFinanceScreen, EmiPaymentHistoryScreen)
5. Build `sharedUI` to verify all changes compile
6. Update documentation

---

## Benefits

1. **Single source of truth** - All date formatting logic in FleetDateTime
2. **Reduced code duplication** - No more duplicate formatDueDateDisplay functions
3. **Consistent formatting** - Same format output across all screens
4. **Easier maintenance** - Fix once, apply everywhere
5. **Better testability** - Core library functions can be unit tested
