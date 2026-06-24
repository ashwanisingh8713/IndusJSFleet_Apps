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

// NOTE: The legacy ISO-8601 request builders (convertToIsoDateTime /
// convertFormattedToIsoDateTime) were removed in the epoch-millis migration —
// build request payloads with convertToEpochMillis(rawDate, rawTime) instead.

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

// ============ EPOCH-MILLIS API (canonical; backend sends/accepts numbers) ============
// These are the Long-based counterparts of the ISO-string helpers above. All API
// timestamps are now UTC epoch millis (Long); use these instead of the String
// variants. See docs/UTC_MILLIS_APP_MIGRATION_PLAN.md.

/**
 * Convert a picked date (DD-MM-YYYY or DDMMYYYY) and time (HH:MM or HHMM) to UTC
 * epoch milliseconds. The picker collects a local wall-clock value; it is
 * interpreted in [timeZone] (device zone) and returned as a UTC instant.
 * Returns null when the date is blank/invalid. Replaces convertToIsoDateTime/
 * convertFormattedToIsoDateTime for building request payloads.
 */
fun convertToEpochMillis(rawDate: String, rawTime: String = ""): Long? {
    if (rawDate.isBlank()) return null
    val d = rawDate.filter { it.isDigit() }
    if (d.length != 8) return null
    val day = d.substring(0, 2).toIntOrNull() ?: return null
    val month = d.substring(2, 4).toIntOrNull() ?: return null
    val year = d.substring(4, 8).toIntOrNull() ?: return null
    val t = rawTime.filter { it.isDigit() }
    val hour = if (t.length >= 2) t.substring(0, 2).toIntOrNull() ?: 0 else 0
    val minute = if (t.length >= 4) t.substring(2, 4).toIntOrNull() ?: 0 else 0
    val value = com.indusjs.datetimeutils.FleetDateTimeValue(
        year = year, month = month, day = day, hour = hour, minute = minute, second = 0
    )
    // FleetEpoch.fromValue interprets the wall-clock value in the device zone
    // (its default) and returns a UTC instant.
    return com.indusjs.datetimeutils.FleetEpoch.fromValue(value)
}

/** Epoch millis → "DD-MMM-YYYY" (e.g. "04-Jan-2026"). Empty string when null. */
fun formatDateToHumanReadable(timestampMillis: Long?, shortMonth: Boolean = false): String {
    val value = com.indusjs.datetimeutils.FleetEpoch.toValue(timestampMillis) ?: return ""
    return com.indusjs.datetimeutils.FleetDateTime.formatDisplayDate(value)
}

/** Epoch millis → "DD-MMM-YYYY hh:mm AM/PM". Empty string when null. */
fun formatDateTimeForDisplay(timestampMillis: Long?): String {
    val value = com.indusjs.datetimeutils.FleetEpoch.toValue(timestampMillis) ?: return ""
    val date = com.indusjs.datetimeutils.FleetDateTime.formatDisplayDate(value)
    val time = com.indusjs.datetimeutils.FleetDateTime.formatTime12Hour(value)
    return "$date $time"
}

/**
 * Epoch millis → relative "last updated" label ("Just now", "5 min ago",
 * "Yesterday, 10:30 AM", or "04-Jan-2026 10:30 AM" beyond a week).
 */
fun formatLastUpdated(timestampMillis: Long?): String {
    if (timestampMillis == null || timestampMillis <= 0L) return "Just now"
    val now = currentTimeMillis()
    val diffMs = now - timestampMillis
    val diffMinutes = diffMs / 60_000L
    val diffHours = diffMs / 3_600_000L
    val diffDays = diffMs / 86_400_000L
    val value = com.indusjs.datetimeutils.FleetEpoch.toValue(timestampMillis)
    val timeFormatted = value?.let { com.indusjs.datetimeutils.FleetDateTime.formatTime12Hour(it) } ?: ""
    return when {
        diffMinutes < 1L -> "Just now"
        diffMinutes < 60L -> "$diffMinutes min ago"
        diffHours < 24L -> if (diffHours == 1L) "1 hour ago" else "$diffHours hours ago"
        diffDays == 1L -> "Yesterday, $timeFormatted"
        diffDays < 7L -> "$diffDays days ago"
        else -> value?.let {
            "${com.indusjs.datetimeutils.FleetDateTime.formatDisplayDate(it)} $timeFormatted"
        } ?: "Just now"
    }
}

