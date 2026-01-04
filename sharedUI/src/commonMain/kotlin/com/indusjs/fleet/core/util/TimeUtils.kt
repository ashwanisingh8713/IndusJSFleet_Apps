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
