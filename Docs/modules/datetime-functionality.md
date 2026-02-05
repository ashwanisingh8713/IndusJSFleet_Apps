# Date & Time Functionality Documentation

## Overview

This document provides a comprehensive reference for all Date & Time functionality in the IndusJS Fleet application.

---

## 1. DateTime Libraries

| Library | Module | Purpose |
|---------|--------|---------|
| **ijs-datetime-picker** | `/ijs-datetime-picker/` | Date/Time picker UI components |
| **ijs-datetime-utils** | `/ijs-datetime-utils/` | Date/Time utility functions |

---

## 2. DateTime Picker Components

**Location:** `ijs-datetime-picker/src/commonMain/kotlin/com/indusjs/datetimepicker/`

| Component | File | Purpose |
|-----------|------|---------|
| `FleetDateTimePicker` | `FleetDateTimePicker.kt` | Combined Date & Time picker popup |
| `FleetDatePicker` | `FleetDateTimePicker.kt` | Date-only picker (wrapper) |
| `DatePickerSection` | `DatePickerSection.kt` | Calendar date selection UI |
| `TimePickerSection` | `TimePickerSection.kt` | Time selection UI (hour/minute carousel) |
| `QuickDateShortcuts` | `QuickDateShortcuts.kt` | Quick select buttons (Today, Tomorrow, etc.) |
| `DateTimeUtils` | `DateTimeUtils.kt` | Formatting utilities for picker |
| `PickerMode` | `FleetDateTimePicker.kt` | Enum: `DATE_TIME`, `DATE_ONLY`, `TIME_ONLY` |

### PickerMode Enum

```kotlin
enum class PickerMode {
    DATE_TIME,   // Shows both date and time
    DATE_ONLY,   // Shows only date picker
    TIME_ONLY    // Shows only time picker
}
```

---

## 3. FleetDateTime Utility Functions

**Location:** `ijs-datetime-utils/src/commonMain/kotlin/com/indusjs/datetimeutils/FleetDateTime.kt`

### Current Date/Time

| Function | Description | Returns |
|----------|-------------|---------|
| `now()` | Get current date/time | `FleetDateTimeValue` |
| `today()` | Get current date | `String` (DD-MM-YYYY) |
| `currentTime()` | Get current time | `String` (HH:mm) |
| `currentDateTime()` | Get current date/time | `String` (DD-MM-YYYY HH:mm) |

### Parsing

| Function | Description | Returns |
|----------|-------------|---------|
| `parse(dateTime: String)` | Parse DD-MM-YYYY HH:mm | `FleetDateTimeValue?` |
| `parse(date: String, time: String)` | Parse separate date and time | `FleetDateTimeValue?` |
| `parseDate(date: String)` | Parse DD-MM-YYYY | `FleetDateTimeValue?` |
| `parseTime(time: String)` | Parse HH:mm | `Pair<Int, Int>?` |

### ISO 8601 Conversion

| Function | Description | Returns |
|----------|-------------|---------|
| `toIso8601(dateTime: String)` | Convert to ISO format | `String?` |
| `toIso8601(date: String, time: String)` | Convert to ISO format | `String?` |
| `toIso8601(value: FleetDateTimeValue)` | Convert to ISO format | `String` |
| `fromIso8601(isoString: String)` | Parse ISO format | `FleetDateTimeValue?` |
| `fromIso8601ToDateTime(isoString: String)` | ISO to DD-MM-YYYY HH:mm | `String?` |
| `fromIso8601ToDate(isoString: String)` | ISO to DD-MM-YYYY | `String?` |
| `fromIso8601ToTime(isoString: String)` | ISO to HH:mm | `String?` |

### Display Formatting

| Function | Description | Returns |
|----------|-------------|---------|
| `formatDate(value: FleetDateTimeValue)` | Format to DD-MM-YYYY | `String` |
| `formatTime(value: FleetDateTimeValue)` | Format to HH:mm | `String` |
| `formatDateTime(value: FleetDateTimeValue)` | Format to DD-MM-YYYY HH:mm | `String` |
| `formatDisplayDate(value: FleetDateTimeValue)` | Format to DD-MMM-YYYY | `String` |
| `formatDisplayDateTime(value: FleetDateTimeValue)` | Format to DD-MMM-YYYY HH:mm | `String` |
| `formatIsoToDisplayDate(isoString: String?)` | ISO to DD-MMM-YYYY | `String` |
| `formatIsoToDisplayDateTime(isoString: String?)` | ISO to DD-MMM-YYYY HH:mm | `String` |
| `formatIsoToMonthYear(isoString: String?)` | ISO to "MMM YYYY" | `String` |
| `formatToDisplayDate(dateString: String?)` | DD-MM-YYYY to DD-MMM-YYYY | `String` |

### Universal Format Functions (Any Input Format)

| Function | Description | Returns |
|----------|-------------|---------|
| `formatAnyToDisplayDate(dateString: String?)` | Any format (ISO/YYYY-MM-DD/DD-MM-YYYY) to DD-MMM-YYYY | `String` |
| `formatIsoToTime12Hour(isoString: String?)` | ISO to "hh:mm AM/PM" (time only) | `String` |
| `formatAnyToTime12Hour(timeString: String?)` | Any time format to "hh:mm AM/PM" | `String` |
| `formatAnyToDisplayDateTime12Hour(dateString: String?, timeString: String?)` | Any format to "DD-MMM-YYYY hh:mm AM/PM" | `String` |

### 12-Hour Format with AM/PM (Recommended for User-Facing Display)

| Function | Description | Returns |
|----------|-------------|---------|
| `formatTime12Hour(hour: Int, minute: Int)` | Format to "hh:mm AM/PM" | `String` |
| `formatTime12Hour(value: FleetDateTimeValue)` | Format value to 12-hour time | `String` |
| `formatDisplayDateTime12Hour(value: FleetDateTimeValue)` | Format to "DD-MMM-YYYY hh:mm AM/PM" | `String` |
| `formatIsoToDisplayDateTime12Hour(isoString: String?)` | ISO to "DD-MMM-YYYY hh:mm AM/PM" | `String` |
| `formatToDisplayDateTime12Hour(dateTime: String?)` | DD-MM-YYYY HH:mm to 12-hour display | `String` |
| `formatToDisplayDateTime12Hour(date: String?, time: String?)` | Separate date/time to 12-hour display | `String` |

### Comparison

| Function | Description | Returns |
|----------|-------------|---------|
| `isBefore(start, end)` | Check if start is before end | `Boolean` |
| `isAfter(start, end)` | Check if start is after end | `Boolean` |
| `isEqual(first, second)` | Check if equal | `Boolean` |
| `isInRange(dateTime, min, max)` | Check if in range | `Boolean` |
| `isDateInRange(date, min, max)` | Check if date in range | `Boolean` |

### Date Checks

| Function | Description | Returns |
|----------|-------------|---------|
| `isToday(date)` | Check if today | `Boolean` |
| `isTomorrow(date)` | Check if tomorrow | `Boolean` |
| `isYesterday(date)` | Check if yesterday | `Boolean` |
| `isThisWeek(date)` | Check if this week | `Boolean` |
| `isNextWeek(date)` | Check if next week | `Boolean` |
| `isThisMonth(date)` | Check if this month | `Boolean` |
| `isNextMonth(date)` | Check if next month | `Boolean` |
| `isPast(dateTime)` | Check if in past | `Boolean` |
| `isFuture(dateTime)` | Check if in future | `Boolean` |
| `isWeekend(date)` | Check if weekend | `Boolean` |
| `isWeekday(date)` | Check if weekday | `Boolean` |

### Date Manipulation

| Function | Description | Returns |
|----------|-------------|---------|
| `addDays(date, days)` | Add days to date | `String?` |
| `addWeeks(date, weeks)` | Add weeks to date | `String?` |
| `addMonths(date, months)` | Add months to date | `String?` |
| `addYears(date, years)` | Add years to date | `String?` |
| `addHours(dateTime, hours)` | Add hours to datetime | `String?` |
| `addMinutes(dateTime, minutes)` | Add minutes to datetime | `String?` |
| `startOfDay(date)` | Get 00:00 of date | `String?` |
| `endOfDay(date)` | Get 23:59 of date | `String?` |
| `startOfWeek(date)` | Get Monday of week | `String?` |
| `endOfWeek(date)` | Get Sunday of week | `String?` |
| `startOfMonth(date)` | Get first day of month | `String?` |
| `endOfMonth(date)` | Get last day of month | `String?` |
| `startOfYear(date)` | Get first day of year | `String?` |
| `endOfYear(date)` | Get last day of year | `String?` |

### Difference Calculations

| Function | Description | Returns |
|----------|-------------|---------|
| `difference(start, end)` | Calculate difference | `DateTimeDifference?` |
| `differenceBetween(start, end)` | Calculate between values | `DateTimeDifference` |
| `formatDuration(difference)` | Human readable duration | `String` |
| `formatDurationShort(difference)` | Short duration (2d 5h) | `String` |
| `formatDurationHumanReadable(difference)` | Approximate duration | `String` |

### Relative Time Description

| Function | Description | Returns |
|----------|-------------|---------|
| `toRelativeDescription(dateTime)` | "Today at 15:30" | `String` |
| `toRelativeDescription(date, time)` | "Tomorrow at 10:00" | `String` |

### Utility Functions

| Function | Description | Returns |
|----------|-------------|---------|
| `getDayOfWeek(date)` | Get day of week | `DayOfWeek?` |
| `getDayOfWeekName(date)` | Get day name | `String` |
| `getMonthName(month)` | Get month name | `String` |
| `getMonthNameShort(month)` | Get short month name | `String` |
| `getDaysInMonth(year, month)` | Get days in month | `Int` |
| `isLeapYear(year)` | Check if leap year | `Boolean` |
| `getFirstDayOfWeekInMonth(year, month)` | Get first weekday | `Int` |
| `getDateFromToday(daysAhead)` | Get future date | `String` |
| `getTomorrowDate()` | Get tomorrow's date | `String` |
| `getDateYearsAgo(years)` | Get past date | `String` |

### Validation

| Function | Description | Returns |
|----------|-------------|---------|
| `isValidDate(date)` | Validate DD-MM-YYYY | `Boolean` |
| `isValidTime(time)` | Validate HH:mm | `Boolean` |
| `isValidDateTime(dateTime)` | Validate DD-MM-YYYY HH:mm | `Boolean` |

### Cost Date Constraints

| Function | Description | Returns |
|----------|-------------|---------|
| `getMinDateForMaintenance(vehicleCreatedAt)` | Min date for maintenance cost | `String` |
| `getMinDateForTripCost(tripStartDateTime)` | Min date for trip cost | `String?` |
| `getMaxDateForTripCost(tripEndDateTime, isCompleted)` | Max date for trip cost | `String` |
| `getMinDateForDriverCost(driverJoiningDate)` | Min date for driver cost | `String` |
| `validateCostDateTimeForTrip(costDateTime, tripStart, tripEnd)` | Validate cost date | `Pair<Boolean, String?>` |
| `validateMaintenanceCostDate(costDate, vehicleCreatedAt)` | Validate maintenance date | `Pair<Boolean, String?>` |
| `validateDriverCostDate(costDate, driverJoiningDate)` | Validate driver cost date | `Pair<Boolean, String?>` |

### Calendar Navigation Helpers

| Function | Description | Returns |
|----------|-------------|---------|
| `canNavigateToPreviousMonth(month, year, minDate)` | Check if can go back | `Boolean` |
| `canNavigateToNextMonth(month, year, maxDate)` | Check if can go forward | `Boolean` |
| `getValidYearRange(minDate, maxDate, defaultRange)` | Get valid year range | `IntRange` |
| `isMonthSelectable(month, year, minDate, maxDate)` | Check if month selectable | `Boolean` |

---

## 4. Data Classes

### FleetDateTimeValue

```kotlin
data class FleetDateTimeValue(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0
) {
    fun toDateString(): String           // DD-MM-YYYY
    fun toTimeString(): String           // HH:mm
    fun toDateTimeString(): String       // DD-MM-YYYY HH:mm
    fun toDisplayDate(): String          // DD-MMM-YYYY
    fun toTime12HourString(): String     // hh:mm AM/PM
    fun toDisplayDateTime12Hour(): String // DD-MMM-YYYY hh:mm AM/PM (Primary display format)
    fun toLocalDate(): LocalDate
    fun toLocalDateTime(): LocalDateTime
}
```

### DateTimeDifference

```kotlin
data class DateTimeDifference(
    val totalDays: Long,
    val totalHours: Long,
    val totalMinutes: Long,
    val totalSeconds: Long,
    val years: Int,
    val months: Int,
    val days: Int,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val isNegative: Boolean
) {
    fun isZero(): Boolean
    fun formatted(): String       // "2 days 5 hours 30 minutes"
    fun shortFormatted(): String  // "2d 5h 30m"
    fun humanReadable(): String   // "About 2 days"
}
```

---

## 5. Core UI Date/Time Fields

**Location:** `core/ui/InputComponents.kt`

| Component | Purpose |
|-----------|---------|
| `FleetDateField` | Manual date input with DD-MM-YYYY format and auto-delimiter |
| `FleetTimeField` | Manual time input with HH:mm format and auto-colon |
| `FleetDateFieldCompact` | Compact date field variant |
| `FleetTimeFieldCompact` | Compact time field variant |

---

## 6. Screens Using FleetDateTimePicker

| Screen | File | Fields | Mode |
|--------|------|--------|------|
| **CreateTripScreen** | `trips/create/CreateTripScreen.kt` | Departure, Arrival Date & Time | `DATE_TIME` |
| **TripDetailScreen** (Edit) | `trips/detail/TripDetailScreen.kt` | Departure, Arrival Date & Time | `DATE_TIME` |
| **TripCostEntryScreen** | `trips/cost/TripCostEntryScreen.kt` | Cost Date & Time | `DATE_TIME` |
| **MaintenanceCostEntryScreen** | `vehicles/costs/MaintenanceCostEntryScreen.kt` | Cost Date & Time | `DATE_TIME` |
| **DriverCostEntryScreen** | `drivers/cost/DriverCostEntryScreen.kt` | Cost Date & Time | `DATE_TIME` |
| **VehicleProfitLossScreen** | `reports/vehicle/VehicleProfitLossScreen.kt` | From/To Date (Custom range) | `DATE_ONLY` |
| **TripProfitLossScreen** | `reports/trip/TripProfitLossScreen.kt` | From/To Date (Custom range) | `DATE_ONLY` |
| **VehicleFinanceDetailScreen** | `finance/VehicleFinanceDetailScreen.kt` | EMI Payment Date | `DATE_TIME` |
| **AddPurchaseInfoScreen** | `finance/AddPurchaseInfoScreen.kt` | Purchase Date, Loan Start Date | `DATE_ONLY` |
| **AddPaymentScreen** | `payments/AddPaymentScreen.kt` | Payment Date, Due Date | `DATE_TIME` / `DATE_ONLY` |
| **PaymentsScreen** | `payments/PaymentsScreen.kt` | Date filter | `DATE_ONLY` |

---

## 7. Screens Using FleetDatePicker (Date Only)

| Screen | File | Fields |
|--------|------|--------|
| **AddVehicleScreen** | `vehicles/AddVehicleScreen.kt` | Document Expiry Dates (Insurance, PUC, Fitness, Road Tax, Permit) |
| **CreateDriverScreen** | `drivers/create/CreateDriverScreen.kt` | Date of Birth, License Expiry, Joining Date |

---

## 8. Screens Using FleetDateField (Manual Input)

| Screen | File | Fields |
|--------|------|--------|
| **DriverDetailScreen** | `drivers/detail/DriverDetailScreen.kt` | Driver Cost Date Filter (From/To) |
| **CostAnalysisScreen** | `reports/cost/CostAnalysisScreen.kt` | Date Range Filter |
| **ConsolidatedPLScreen** | `reports/consolidated/ConsolidatedPLScreen.kt` | Date Range Filter |
| **DateRangePickerDialog** | `core/ui/DateRangePickerDialog.kt` | From/To Date inputs |

---

## 9. Screens Using FleetDateTime Utility

| Screen | File | Usage |
|--------|------|-------|
| **VehicleFinanceDetailScreen** | `finance/VehicleFinanceDetailScreen.kt` | Date formatting |
| **VehicleFinanceScreen** | `finance/VehicleFinanceScreen.kt` | Next EMI date formatting |
| **EmiPaymentHistoryScreen** | `finance/EmiPaymentHistoryScreen.kt` | Payment date formatting |
| **CreateTripScreen** | `trips/create/CreateTripScreen.kt` | Date validation & conversion |
| **PaymentsScreen** | `payments/PaymentsScreen.kt` | Date filtering & display |
| **PaymentDetailScreen** | `payments/PaymentDetailScreen.kt` | Date formatting |
| **AddPaymentViewModel** | `payments/AddPaymentViewModel.kt` | Date conversion for API |
| **CustomerDetailViewModel** | `customers/detail/CustomerDetailViewModel.kt` | PDF date formatting |
| **TripsTabContent** | `customers/detail/components/TripsTabContent.kt` | Trip date display |
| **PaymentsTabContent** | `customers/detail/components/PaymentsTabContent.kt` | Payment date display & grouping |

---

## 10. Summary Statistics

| Category | Count |
|----------|-------|
| **DateTime Picker Components** | 5 |
| **DateTime Utility Functions** | 50+ functions in FleetDateTime |
| **Screens using FleetDateTimePicker** | 11 |
| **Screens using FleetDatePicker** | 2 |
| **Screens using FleetDateField** | 4 |
| **Screens using FleetDateTime Utility** | 10 |

---

## 11. Best Practices

### General
1. **Use `FleetDateTime`** for all date operations across the app
2. **Use `FleetDateTimePicker`** for user input (consistent UX)
3. **Use `PickerMode.DATE_ONLY`** for date-only fields
4. **Always validate dates** with min/max constraints

### Display Formatting
5. Use `formatIsoToDisplayDate()` for date-only display (DD-MMM-YYYY)
6. Use `formatIsoToDisplayDateTime12Hour()` for datetime display (DD-MMM-YYYY hh:mm AM/PM) - **Recommended for user-facing content**
7. Use `formatIsoToMonthYear()` for grouping by month

### API Communication
8. Use `toIso8601()` before sending dates to API
9. Use `fromIso8601()` when receiving dates from API

### Constraints
10. Use `getMinDateForMaintenance()` for vehicle cost constraints
11. Use `getMinDateForTripCost()` for trip cost constraints
12. Use `getMinDateForDriverCost()` for driver cost constraints
13. Use `getTomorrowDate()` for max date (costs can be recorded up to tomorrow)

### Validation
14. Always validate with `isValidDate()` / `isValidTime()` before processing
15. Use `validateCostDateTimeForTrip()` for trip cost date validation
16. Use `validateMaintenanceCostDate()` for maintenance cost date validation

### 12-Hour Format Guidelines
17. Use 12-hour format (AM/PM) for all user-facing datetime displays
18. Input fields can remain in 24-hour format for easier data entry
19. Picker preview should show 12-hour format for consistency
20. PDF exports should use 12-hour format for datetime

---

## 12. Usage Examples

### Using FleetDateTimePicker

```kotlin
var showPicker by remember { mutableStateOf(false) }
var selectedDate by remember { mutableStateOf("") }
var selectedTime by remember { mutableStateOf("") }

FleetDateTimePicker(
    isVisible = showPicker,
    initialDate = selectedDate,
    initialTime = selectedTime,
    mode = PickerMode.DATE_TIME,
    minDate = FleetDateTime.today(),
    maxDate = FleetDateTime.addMonths(FleetDateTime.today(), 3),
    onConfirm = { date, time ->
        selectedDate = date
        selectedTime = time
        showPicker = false
    },
    onDismiss = { showPicker = false }
)
```

### Using FleetDateTime for API

```kotlin
// Converting user input to API format
val departureIso = FleetDateTime.toIso8601(
    state.departureDate,  // DD-MM-YYYY
    state.departureTime   // HH:mm
)

// Converting API response to display format (12-hour with AM/PM - recommended)
val displayDate = FleetDateTime.formatIsoToDisplayDateTime12Hour(trip.scheduledDate)
// Result: "05-Feb-2026 02:30 PM"

// For date-only display
val dateOnly = FleetDateTime.formatIsoToDisplayDate(trip.scheduledDate)
// Result: "05-Feb-2026"
```

### 12-Hour Time Formatting

```kotlin
// Format time to 12-hour with AM/PM
val time12Hour = FleetDateTime.formatTime12Hour(14, 30)
// Result: "02:30 PM"

// Format FleetDateTimeValue to 12-hour display
val value = FleetDateTime.now()
val display = value.toDisplayDateTime12Hour()
// Result: "05-Feb-2026 02:30 PM"

// From separate date/time strings
val formattedDateTime = FleetDateTime.formatToDisplayDateTime12Hour("05-02-2026", "14:30")
// Result: "05-Feb-2026 02:30 PM"
```

### Universal Format Functions (Any Input)

```kotlin
// Format any date format to display (handles ISO, YYYY-MM-DD, DD-MM-YYYY)
val display1 = FleetDateTime.formatAnyToDisplayDate("2026-02-05T14:30:00Z") // "05-Feb-2026"
val display2 = FleetDateTime.formatAnyToDisplayDate("2026-02-05")           // "05-Feb-2026"
val display3 = FleetDateTime.formatAnyToDisplayDate("05-02-2026")           // "05-Feb-2026"

// Format any time format to 12-hour
val time1 = FleetDateTime.formatAnyToTime12Hour("14:30")                    // "02:30 PM"
val time2 = FleetDateTime.formatAnyToTime12Hour("14:30:00")                 // "02:30 PM"
val time3 = FleetDateTime.formatAnyToTime12Hour("2026-02-05T14:30:00Z")     // "02:30 PM"

// Format any datetime to display (handles all input combinations)
val dt1 = FleetDateTime.formatAnyToDisplayDateTime12Hour("2026-02-05T14:30:00Z")       // "05-Feb-2026 02:30 PM"
val dt2 = FleetDateTime.formatAnyToDisplayDateTime12Hour("05-02-2026", "14:30")        // "05-Feb-2026 02:30 PM"
val dt3 = FleetDateTime.formatAnyToDisplayDateTime12Hour("2026-02-05")                 // "05-Feb-2026"
```

### Date Validation

```kotlin
// Validate trip cost date is within trip duration
val (isValid, errorMessage) = FleetDateTime.validateCostDateTimeForTrip(
    costDateTime = "${costDate} ${costTime}",
    tripStartDateTime = trip.startTime,
    tripEndDateTime = trip.endTime
)

if (!isValid) {
    showError(errorMessage ?: "Invalid date")
}
```

### Date Constraints for Picker

```kotlin
// For maintenance cost entry
val minDate = FleetDateTime.getMinDateForMaintenance(vehicle.createdAt)
val maxDate = FleetDateTime.getTomorrowDate()

FleetDateTimePicker(
    minDate = minDate,  // Vehicle creation date
    maxDate = maxDate,  // Tomorrow
    ...
)
```

### Grouping by Month

```kotlin
// Group payments by month
val groupedPayments = payments.groupBy { payment ->
    FleetDateTime.formatIsoToMonthYear(payment.paymentDate)
}
// Result: Map<String, List<Payment>>
// "Feb 2026" -> [payment1, payment2]
// "Jan 2026" -> [payment3]
```
