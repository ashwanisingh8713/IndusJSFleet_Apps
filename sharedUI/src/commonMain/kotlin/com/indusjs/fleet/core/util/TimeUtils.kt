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
        val parsed = com.indusjs.datetimeutils.FleetDateTime.fromIso8601(isoDateString)
            ?: return isoDateString

        val hour24 = parsed.hour
        val minute = parsed.minute

        // Convert to approximate timestamp for relative time calculation
        val now = currentTimeMillis()

        // Approximate days since epoch for the given date
        val approxDaysSinceEpoch = ((parsed.year - 1970) * 365L) + ((parsed.month - 1) * 30L) + parsed.day
        val approxTimestamp = approxDaysSinceEpoch * 86_400_000L + (hour24 * 3_600_000L) + (minute * 60_000L)

        val diffMs = now - approxTimestamp
        val diffMinutes = diffMs / 60_000L
        val diffHours = diffMs / 3_600_000L
        val diffDays = diffMs / 86_400_000L

        // Format time as 12-hour with AM/PM using FleetDateTime
        val timeFormatted = com.indusjs.datetimeutils.FleetDateTime.formatTime12Hour(hour24, minute)

        when {
            diffMinutes < 0L -> "Just now" // Future date
            diffMinutes < 1L -> "Just now"
            diffMinutes < 60L -> "$diffMinutes min ago"
            diffHours < 24L -> if (diffHours == 1L) "1 hour ago" else "$diffHours hours ago"
            diffDays == 1L -> "Yesterday, $timeFormatted"
            diffDays < 7L -> "$diffDays days ago"
            else -> {
                // Format as readable date: "04-Jan-2026 10:30 AM"
                "${com.indusjs.datetimeutils.FleetDateTime.formatDisplayDate(parsed)} $timeFormatted"
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
 * Get current date formatted as "DD-MMM-YYYY" (e.g., "10-Jan-2026").
 * Uses FleetDateTime for proper date formatting.
 */
fun getCurrentFormattedDateHumanReadable(): String {
    val now = com.indusjs.datetimeutils.FleetDateTime.now()
    return com.indusjs.datetimeutils.FleetDateTime.formatDisplayDate(now)
}

/**
 * Get current time formatted as "hh:mm AM/PM" (e.g., "02:30 PM").
 * Uses FleetDateTime for proper 12-hour format.
 */
fun getCurrentFormattedTime(): String {
    val now = com.indusjs.datetimeutils.FleetDateTime.now()
    return com.indusjs.datetimeutils.FleetDateTime.formatTime12Hour(now)
}

/**
 * Format an ISO date string to human-readable date format.
 * Delegates to FleetDateTime.formatAnyToDisplayDate().
 * Input: "2026-01-04T11:30:00Z" or "04-01-2026" or "2026-01-04"
 * Output: "04-Jan-2026" (DD-MMM-YYYY format)
 */
fun formatDateToHumanReadable(dateString: String?, shortMonth: Boolean = false): String =
    com.indusjs.datetimeutils.FleetDateTime.formatAnyToDisplayDate(dateString)

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
 * Delegates to FleetDateTime.formatAnyToTime12Hour().
 *
 * Input formats supported:
 * - "14:30" or "14:30:00" -> "02:30 PM"
 * - "2026-01-04T14:30:00Z" (ISO 8601) -> "02:30 PM"
 * - "09:15" -> "09:15 AM"
 */
fun formatCostTime(timeString: String?): String =
    com.indusjs.datetimeutils.FleetDateTime.formatAnyToTime12Hour(timeString)

/**
 * Format a date and time together for display.
 * Delegates to FleetDateTime.formatAnyToDisplayDateTime12Hour().
 * Input: date="2026-01-04T14:30:00Z" or date="04-01-2026", time="14:30"
 * Output: "04-Jan-2026 02:30 PM"
 */
fun formatDateTimeForDisplay(date: String?, time: String? = null): String =
    com.indusjs.datetimeutils.FleetDateTime.formatAnyToDisplayDateTime12Hour(date, time)

