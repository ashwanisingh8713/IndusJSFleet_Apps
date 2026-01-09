package com.indusjs.fleet.core.util

/**
 * Platform-agnostic time utilities.
 */
expect fun currentTimeMillis(): Long

/**
 * Format a timestamp to a human-readable relative time string.
 */
fun formatRelativeTime(timestamp: Long?): String {
    if (timestamp == null) return "Just now"

    val now = currentTimeMillis()
    val diffMs = now - timestamp
    val diffMinutes = diffMs / 60_000L
    val diffHours = diffMs / 3_600_000L
    val diffDays = diffMs / 86_400_000L

    return when {
        diffMinutes < 1L -> "Just now"
        diffMinutes < 60L -> "$diffMinutes min ago"
        diffHours < 24L -> if (diffHours == 1L) "1 hour ago" else "$diffHours hours ago"
        diffDays < 7L -> if (diffDays == 1L) "Yesterday" else "$diffDays days ago"
        else -> "$diffDays days ago"
    }
}

/**
 * Format an ISO date string (e.g., "2026-01-04T10:30:00Z") to human-readable format.
 * Shows time in 12-hour format with AM/PM.
 * Falls back to relative time or the original string if parsing fails.
 */
fun formatLastUpdated(isoDateString: String?): String {
    if (isoDateString.isNullOrBlank()) return "Just now"

    // Check if already in human-readable format
    if (isoDateString.contains("ago") ||
        isoDateString.equals("Just now", ignoreCase = true) ||
        isoDateString.equals("Yesterday", ignoreCase = true)) {
        return isoDateString
    }

    return try {
        // Parse ISO 8601 date: "2026-01-04T10:30:00Z" or "2026-01-04T10:30:00.000Z"
        val cleanDate = isoDateString.replace("Z", "").replace("z", "")
        val parts = cleanDate.split("T")
        if (parts.size != 2) return isoDateString

        val datePart = parts[0] // "2026-01-04"
        val timePart = parts[1].split(".")[0] // "10:30:00" (remove milliseconds)

        val dateParts = datePart.split("-")
        if (dateParts.size != 3) return isoDateString

        val year = dateParts[0].toIntOrNull() ?: return isoDateString
        val month = dateParts[1].toIntOrNull() ?: return isoDateString
        val day = dateParts[2].toIntOrNull() ?: return isoDateString

        val timeParts = timePart.split(":")
        val hour24 = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        // Convert to approximate timestamp for relative time calculation
        val now = currentTimeMillis()

        // Approximate days since epoch for the given date
        // Using simplified calculation: days = (year-1970)*365 + (month-1)*30 + day
        val approxDaysSinceEpoch = ((year - 1970) * 365L) + ((month - 1) * 30L) + day
        val approxTimestamp = approxDaysSinceEpoch * 86_400_000L + (hour24 * 3_600_000L) + (minute * 60_000L)

        val diffMs = now - approxTimestamp
        val diffMinutes = diffMs / 60_000L
        val diffHours = diffMs / 3_600_000L
        val diffDays = diffMs / 86_400_000L

        // Format time as 12-hour with AM/PM
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val amPm = if (hour24 < 12) "AM" else "PM"
        val timeFormatted = "${hour12}:${minute.toString().padStart(2, '0')} $amPm"

        when {
            diffMinutes < 0L -> "Just now" // Future date
            diffMinutes < 1L -> "Just now"
            diffMinutes < 60L -> "$diffMinutes min ago"
            diffHours < 24L -> if (diffHours == 1L) "1 hour ago" else "$diffHours hours ago"
            diffDays == 1L -> "Yesterday, $timeFormatted"
            diffDays < 7L -> "$diffDays days ago"
            else -> {
                // Format as readable date: "04 Jan 2026, 10:30 AM"
                val monthName = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                       "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    .getOrNull(month - 1) ?: "Jan"
                val dayStr = day.toString().padStart(2, '0')
                "$dayStr $monthName $year, $timeFormatted"
            }
        }
    } catch (e: Exception) {
        isoDateString // Return original if parsing fails
    }
}

// ============ ISO 8601 CONVERSION UTILITIES FOR API ============

/**
 * Convert date (DDMMYYYY digits) and time (HHMM digits) to ISO 8601 format.
 * The v2 API backend expects all date/time fields in ISO 8601 format: YYYY-MM-DDTHH:MM:SSZ
 *
 * @param rawDate Date in DDMMYYYY format (8 digits, no separators)
 * @param rawTime Time in HHMM format (4 digits, no separators). Defaults to "0000" if blank.
 * @return ISO 8601 formatted string (e.g., "2026-01-04T11:30:00Z") or empty string if invalid
 */
fun convertToIsoDateTime(rawDate: String, rawTime: String = ""): String {
    if (rawDate.isBlank()) return ""

    val dateDigits = rawDate.filter { it.isDigit() }
    if (dateDigits.length != 8) return ""

    val day = dateDigits.substring(0, 2)
    val month = dateDigits.substring(2, 4)
    val year = dateDigits.substring(4, 8)

    val timeDigits = rawTime.filter { it.isDigit() }
    val hours = if (timeDigits.length >= 2) timeDigits.substring(0, 2) else "00"
    val minutes = if (timeDigits.length >= 4) timeDigits.substring(2, 4) else "00"

    return "$year-$month-${day}T$hours:$minutes:00Z"
}

/**
 * Convert date in DD-MM-YYYY format and time in HH:MM format to ISO 8601 format.
 * Used when date/time already have separators (e.g., from DateInputField).
 *
 * @param formattedDate Date in DD-MM-YYYY format
 * @param formattedTime Time in HH:MM format. Defaults to "00:00" if blank.
 * @return ISO 8601 formatted string (e.g., "2026-01-04T11:30:00Z") or empty string if invalid
 */
fun convertFormattedToIsoDateTime(formattedDate: String, formattedTime: String = ""): String {
    if (formattedDate.isBlank()) return ""

    val dateParts = formattedDate.split("-")
    if (dateParts.size != 3) return ""

    val day = dateParts[0].padStart(2, '0')
    val month = dateParts[1].padStart(2, '0')
    val year = dateParts[2]

    val timeParts = if (formattedTime.isNotBlank()) formattedTime.split(":") else emptyList()
    val hours = timeParts.getOrNull(0)?.padStart(2, '0') ?: "00"
    val minutes = timeParts.getOrNull(1)?.padStart(2, '0') ?: "00"

    return "$year-$month-${day}T$hours:$minutes:00Z"
}

/**
 * Get current date formatted as "DD Month YYYY" (e.g., "10 January 2026").
 * Uses approximate calculation from currentTimeMillis.
 */
fun getCurrentFormattedDateHumanReadable(): String {
    val now = currentTimeMillis()
    // Approximate date calculation
    val totalDays = (now / 86_400_000L) + 1  // Days since epoch
    val year = 1970 + (totalDays / 365).toInt()
    val remainingDays = (totalDays % 365).toInt()

    // Approximate month and day
    val daysInMonth = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var month = 0
    var dayOfMonth = remainingDays
    for ((index, days) in daysInMonth.withIndex()) {
        if (dayOfMonth <= days) {
            month = index
            break
        }
        dayOfMonth -= days
    }
    if (dayOfMonth <= 0) dayOfMonth = 1

    val monthNames = listOf("January", "February", "March", "April", "May", "June",
                            "July", "August", "September", "October", "November", "December")
    val monthName = monthNames.getOrElse(month) { "January" }

    return "${dayOfMonth.toString().padStart(2, '0')} $monthName $year"
}

/**
 * Get current time formatted as "HH:MM AM/PM" (e.g., "02:30 PM").
 * Uses approximate calculation from currentTimeMillis.
 */
fun getCurrentFormattedTime(): String {
    val now = currentTimeMillis()
    // Calculate time within the day
    val msInDay = now % 86_400_000L
    val totalMinutes = (msInDay / 60_000L).toInt()

    // Add IST offset (5 hours 30 minutes = 330 minutes)
    val istMinutes = (totalMinutes + 330) % (24 * 60)

    val hour24 = istMinutes / 60
    val minute = istMinutes % 60

    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    val amPm = if (hour24 < 12) "AM" else "PM"

    return "${hour12.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPm"
}

/**
 * Format an ISO date string to human-readable date format.
 * Input: "2026-01-04T11:30:00Z" or "04-01-2026"
 * Output: "04 January 2026" or "04 Jan 2026" (short format)
 */
fun formatDateToHumanReadable(dateString: String?, shortMonth: Boolean = false): String {
    if (dateString.isNullOrBlank()) return "N/A"

    val monthNamesLong = listOf("January", "February", "March", "April", "May", "June",
                                "July", "August", "September", "October", "November", "December")
    val monthNamesShort = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                 "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val monthNames = if (shortMonth) monthNamesShort else monthNamesLong

    return try {
        when {
            // ISO 8601 format: "2026-01-04T11:30:00Z"
            dateString.contains("T") -> {
                val datePart = dateString.substringBefore("T")
                val parts = datePart.split("-")
                if (parts.size == 3) {
                    val year = parts[0]
                    val month = parts[1].toIntOrNull() ?: 1
                    val day = parts[2]
                    val monthName = monthNames.getOrElse(month - 1) { "January" }
                    "$day $monthName $year"
                } else dateString
            }
            // DD-MM-YYYY format
            dateString.contains("-") -> {
                val parts = dateString.split("-")
                if (parts.size == 3) {
                    val day = parts[0]
                    val month = parts[1].toIntOrNull() ?: 1
                    val year = parts[2]
                    val monthName = monthNames.getOrElse(month - 1) { "January" }
                    "$day $monthName $year"
                } else dateString
            }
            else -> dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

/**
 * Format a cost amount to a display-friendly string with thousands separators.
 * Input: 50000.0
 * Output: "50,000" or "50,000.50" if has decimals
 */
fun formatCostAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        // No decimals - format with thousands separator
        val amountLong = amount.toLong()
        formatWithThousandsSeparator(amountLong)
    } else {
        // Has decimals - format with 2 decimal places
        val intPart = amount.toLong()
        val decimalPart = ((amount - intPart) * 100).toLong()
        "${formatWithThousandsSeparator(intPart)}.${decimalPart.toString().padStart(2, '0')}"
    }
}

/**
 * Helper function to add thousands separators to a number.
 */
private fun formatWithThousandsSeparator(number: Long): String {
    val numberStr = number.toString()
    val result = StringBuilder()
    var count = 0

    for (i in numberStr.lastIndex downTo 0) {
        if (count > 0 && count % 3 == 0) {
            result.insert(0, ',')
        }
        result.insert(0, numberStr[i])
        count++
    }

    return result.toString()
}

/**
 * Format time string for display in cost items.
 * Handles various input formats and converts to 12-hour format with AM/PM.
 *
 * Input formats supported:
 * - "14:30" or "14:30:00" -> "2:30 PM"
 * - "2026-01-04T14:30:00Z" (ISO 8601) -> "2:30 PM"
 * - "09:15" -> "9:15 AM"
 */
fun formatCostTime(timeString: String?): String {
    if (timeString.isNullOrBlank()) return "N/A"

    return try {
        // Extract hours and minutes
        val (hours, minutes) = when {
            // ISO 8601 format: "2026-01-04T14:30:00Z"
            timeString.contains("T") -> {
                val timePart = timeString.substringAfter("T").substringBefore("Z").substringBefore(".")
                val parts = timePart.split(":")
                Pair(
                    parts.getOrNull(0)?.toIntOrNull() ?: 0,
                    parts.getOrNull(1)?.toIntOrNull() ?: 0
                )
            }
            // Simple time format: "14:30" or "14:30:00"
            timeString.contains(":") -> {
                val parts = timeString.split(":")
                Pair(
                    parts.getOrNull(0)?.toIntOrNull() ?: 0,
                    parts.getOrNull(1)?.toIntOrNull() ?: 0
                )
            }
            else -> Pair(0, 0)
        }

        // Convert to 12-hour format
        val hour12 = when {
            hours == 0 -> 12
            hours > 12 -> hours - 12
            else -> hours
        }
        val amPm = if (hours < 12) "AM" else "PM"

        "$hour12:${minutes.toString().padStart(2, '0')} $amPm"
    } catch (e: Exception) {
        timeString
    }
}

/**
 * Format a date and time together for display.
 * Input: date="2026-01-04T14:30:00Z" or date="04-01-2026", time="14:30"
 * Output: "04 Jan 2026, 2:30 PM"
 */
fun formatDateTimeForDisplay(date: String?, time: String? = null): String {
    if (date.isNullOrBlank()) return "N/A"

    val formattedDate = formatDateToHumanReadable(date, shortMonth = true)

    // If time is provided separately, format it
    // If date contains time (ISO format), extract and format it
    val formattedTime = when {
        !time.isNullOrBlank() -> formatCostTime(time)
        date.contains("T") -> {
            val timePart = date.substringAfter("T")
            formatCostTime(timePart)
        }
        else -> null
    }

    return if (formattedTime != null && formattedTime != "N/A") {
        "$formattedDate, $formattedTime"
    } else {
        formattedDate
    }
}

