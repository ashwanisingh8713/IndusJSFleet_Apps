# Date & Time Handling Prompt

## Overview

Date and time handling is critical in IndusJS Fleet. The UI uses Indian format (DD-MM-YYYY) while the API uses ISO 8601 format. This document covers all conversion and validation requirements.

---

## Format Specifications

### UI Display Format
| Type | Format | Example |
|------|--------|---------|
| Date | DD-MM-YYYY | 04-01-2026 |
| Time | HH:MM (24hr) | 14:30 |
| DateTime | DD-MM-YYYY HH:MM | 04-01-2026 14:30 |

### API Request/Response Format
| Type | Format | Example |
|------|--------|---------|
| DateTime | ISO 8601 | 2026-01-04T14:30:00Z |
| Date only | YYYY-MM-DD | 2026-01-04 |
| Time only | HH:MM:SS | 14:30:00 |

---

## Conversion Functions

### Location
`core/util/DateTimeUtils.kt` or `core/util/TimeUtils.kt`

### UI to API Conversion

```kotlin
/**
 * Convert UI date (DD-MM-YYYY) and time (HH:MM) to ISO 8601 format.
 * Example: "04-01-2026" + "14:30" → "2026-01-04T14:30:00Z"
 */
fun toIsoDateTime(date: String, time: String): String {
    require(date.matches(Regex("\\d{2}-\\d{2}-\\d{4}"))) { "Invalid date format" }
    require(time.matches(Regex("\\d{2}:\\d{2}"))) { "Invalid time format" }
    
    val (day, month, year) = date.split("-")
    return "${year}-${month}-${day}T${time}:00Z"
}

/**
 * Convert UI date (DD-MM-YYYY) to ISO date format.
 * Example: "04-01-2026" → "2026-01-04"
 */
fun toIsoDate(date: String): String {
    val (day, month, year) = date.split("-")
    return "${year}-${month}-${day}"
}
```

### API to UI Conversion

```kotlin
/**
 * Convert ISO 8601 to display date (DD-MM-YYYY).
 * Example: "2026-01-04T14:30:00Z" → "04-01-2026"
 */
fun fromIsoToDisplayDate(iso: String): String {
    val date = iso.substringBefore("T")
    val (year, month, day) = date.split("-")
    return "$day-$month-$year"
}

/**
 * Convert ISO 8601 to display time (HH:MM).
 * Example: "2026-01-04T14:30:00Z" → "14:30"
 */
fun fromIsoToDisplayTime(iso: String): String {
    val time = iso.substringAfter("T").substringBefore("Z")
    return time.substring(0, 5) // HH:MM
}

/**
 * Convert ISO 8601 to both date and time.
 * Returns Pair(date, time) in display format.
 */
fun fromIsoToDisplay(iso: String): Pair<String, String> {
    return Pair(fromIsoToDisplayDate(iso), fromIsoToDisplayTime(iso))
}
```

---

## Validation Functions

### Location
`core/util/ValidationUtils.kt`

```kotlin
/**
 * Validate date in DD-MM-YYYY format.
 */
fun validateDate(date: String, required: Boolean = true): ValidationResult {
    if (date.isEmpty()) {
        return if (required) ValidationResult.Error("Date is required")
               else ValidationResult.Success
    }
    
    if (!date.matches(Regex("\\d{2}-\\d{2}-\\d{4}"))) {
        return ValidationResult.Error("Invalid format. Use DD-MM-YYYY")
    }
    
    val (day, month, year) = date.split("-").map { it.toIntOrNull() ?: 0 }
    
    if (month !in 1..12) return ValidationResult.Error("Invalid month")
    if (day !in 1..31) return ValidationResult.Error("Invalid day")
    if (year !in 1900..2100) return ValidationResult.Error("Invalid year")
    
    // Additional day validation based on month
    val maxDays = when (month) {
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    if (day > maxDays) return ValidationResult.Error("Invalid day for this month")
    
    return ValidationResult.Success
}

/**
 * Validate time in HH:MM 24-hour format.
 */
fun validateTime(time: String, required: Boolean = true): ValidationResult {
    if (time.isEmpty()) {
        return if (required) ValidationResult.Error("Time is required")
               else ValidationResult.Success
    }
    
    if (!time.matches(Regex("\\d{2}:\\d{2}"))) {
        return ValidationResult.Error("Invalid format. Use HH:MM")
    }
    
    val (hour, minute) = time.split(":").map { it.toIntOrNull() ?: -1 }
    
    if (hour !in 0..23) return ValidationResult.Error("Hour must be 00-23")
    if (minute !in 0..59) return ValidationResult.Error("Minute must be 00-59")
    
    return ValidationResult.Success
}

sealed interface ValidationResult {
    data object Success : ValidationResult
    data class Error(val message: String) : ValidationResult
}
```

---

## Input Components

### FleetDateField
**Auto-adds delimiters as user types:**
- User types: `0401`
- Displayed: `04-01-`
- Final: `04-01-2026`

```kotlin
@Composable
fun FleetDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "DD-MM-YYYY",
    isError: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    // Implementation with auto-delimiter
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val formatted = formatDateInput(newValue)
            if (formatted.length <= 10) {
                onValueChange(formatted)
            }
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        isError = isError,
        supportingText = errorMessage?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}

private fun formatDateInput(input: String): String {
    val digits = input.filter { it.isDigit() }
    return buildString {
        digits.forEachIndexed { index, c ->
            append(c)
            if ((index == 1 || index == 3) && index < digits.length - 1) {
                append("-")
            }
        }
    }
}
```

### FleetTimeField
**Auto-adds colon as user types:**
- User types: `1430`
- Displayed: `14:30`

```kotlin
@Composable
fun FleetTimeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "HH:MM",
    isError: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val formatted = formatTimeInput(newValue)
            if (formatted.length <= 5) {
                onValueChange(formatted)
            }
        },
        label = { Text("$label (24hr)") },
        placeholder = { Text(placeholder) },
        isError = isError,
        supportingText = errorMessage?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}

private fun formatTimeInput(input: String): String {
    val digits = input.filter { it.isDigit() }
    return buildString {
        digits.forEachIndexed { index, c ->
            append(c)
            if (index == 1 && index < digits.length - 1) {
                append(":")
            }
        }
    }
}
```

---

## Usage Examples

### Create Trip Request
```kotlin
// UI State
val departureDate = "04-01-2026"  // DD-MM-YYYY
val departureTime = "14:30"        // HH:MM
val arrivalDate = "06-01-2026"
val arrivalTime = "10:00"

// Convert for API
val createTripRequest = CreateTripRequest(
    scheduledDate = toIsoDateTime(departureDate, departureTime),  // 2026-01-04T14:30:00Z
    plannedStart = toIsoDateTime(departureDate, departureTime),   // 2026-01-04T14:30:00Z
    plannedEnd = toIsoDateTime(arrivalDate, arrivalTime),         // 2026-01-06T10:00:00Z
    // ... other fields
)
```

### Display Trip Details
```kotlin
// From API
val trip = tripResponse.data

// Convert for display
val (departureDate, departureTime) = fromIsoToDisplay(trip.plannedStart)
val (arrivalDate, arrivalTime) = fromIsoToDisplay(trip.plannedEnd)

// Display
Text("Departure: $departureDate at $departureTime")  // Departure: 04-01-2026 at 14:30
Text("Arrival: $arrivalDate at $arrivalTime")        // Arrival: 06-01-2026 at 10:00
```

### Validate Before Submit
```kotlin
fun validateSchedule(): Boolean {
    val dateResult = validateDate(state.departureDate)
    if (dateResult is ValidationResult.Error) {
        updateState { copy(departureDateError = dateResult.message) }
        return false
    }
    
    val timeResult = validateTime(state.departureTime)
    if (timeResult is ValidationResult.Error) {
        updateState { copy(departureTimeError = timeResult.message) }
        return false
    }
    
    return true
}
```

---

## Common Pitfalls

### ❌ DON'T: Send UI format to API
```kotlin
// WRONG - API will reject this
CreateTripRequest(
    scheduledDate = "04-01-2026",  // ❌ DD-MM-YYYY
    startTime = "14:30"            // ❌ HH:MM only
)
```

### ✅ DO: Convert to ISO format
```kotlin
// CORRECT
CreateTripRequest(
    scheduledDate = toIsoDateTime(date, time),  // ✅ 2026-01-04T14:30:00Z
    plannedStart = toIsoDateTime(date, time)    // ✅ 2026-01-04T14:30:00Z
)
```

### ❌ DON'T: Display ISO format to user
```kotlin
// WRONG - User sees "2026-01-04T14:30:00Z"
Text(trip.plannedStart)
```

### ✅ DO: Convert to display format
```kotlin
// CORRECT - User sees "04-01-2026 at 14:30"
val (date, time) = fromIsoToDisplay(trip.plannedStart)
Text("$date at $time")
```

---

## API Field Reference

### Trip Date/Time Fields
| API Field | Format | Purpose |
|-----------|--------|---------|
| scheduled_date | ISO 8601 | Trip scheduled date |
| start_time | ISO 8601 | Departure time (full datetime) |
| delivery_date | ISO 8601 | Expected delivery date |
| delivery_time | ISO 8601 | Delivery time (full datetime) |
| planned_start | ISO 8601 | Combined start datetime |
| planned_end | ISO 8601 | Combined end datetime |

### Document/License Fields
| API Field | Format | Purpose |
|-----------|--------|---------|
| expiry_date | ISO 8601 | Document expiry |
| license_expiry | ISO 8601 | Driver license expiry |
| created_at | ISO 8601 | Record creation time |
| updated_at | ISO 8601 | Last update time |

---

## Timezone Considerations

1. All API times are in **UTC** (indicated by `Z` suffix)
2. Display times should be converted to **local timezone** (IST for India)
3. Use `kotlinx-datetime` for timezone conversion if needed

```kotlin
// For displaying "last updated" in local time
fun formatLastUpdated(isoDateTime: String?): String {
    if (isoDateTime == null) return ""
    // Convert to local timezone and format
    // Implementation depends on kotlinx-datetime
}
```

