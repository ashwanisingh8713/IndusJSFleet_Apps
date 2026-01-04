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
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        // Convert to approximate timestamp for relative time calculation
        // This is a simplified calculation - good enough for relative display
        val now = currentTimeMillis()

        // Approximate days since epoch for the given date
        // Using simplified calculation: days = (year-1970)*365 + (month-1)*30 + day
        val approxDaysSinceEpoch = ((year - 1970) * 365L) + ((month - 1) * 30L) + day
        val approxTimestamp = approxDaysSinceEpoch * 86_400_000L + (hour * 3_600_000L) + (minute * 60_000L)

        val diffMs = now - approxTimestamp
        val diffMinutes = diffMs / 60_000L
        val diffHours = diffMs / 3_600_000L
        val diffDays = diffMs / 86_400_000L

        when {
            diffMinutes < 0L -> "Just now" // Future date
            diffMinutes < 1L -> "Just now"
            diffMinutes < 60L -> "$diffMinutes min ago"
            diffHours < 24L -> if (diffHours == 1L) "1 hour ago" else "$diffHours hours ago"
            diffDays == 1L -> "Yesterday"
            diffDays < 7L -> "$diffDays days ago"
            else -> {
                // Format as readable date: "04 Jan 2026, 10:30"
                val monthName = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                       "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    .getOrNull(month - 1) ?: "Jan"
                val dayStr = day.toString().padStart(2, '0')
                val hourStr = hour.toString().padStart(2, '0')
                val minStr = minute.toString().padStart(2, '0')
                "$dayStr $monthName $year, $hourStr:$minStr"
            }
        }
    } catch (e: Exception) {
        isoDateString // Return original if parsing fails
    }
}

