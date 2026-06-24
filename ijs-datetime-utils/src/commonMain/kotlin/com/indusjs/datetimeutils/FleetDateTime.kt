package com.indusjs.datetimeutils

import kotlinx.datetime.*
import kotlin.math.abs
import kotlin.time.Clock

/**
 * Cross-platform Date & Time utility class.
 *
 * Default formats:
 * - Date: DD-MM-YYYY
 * - Time: HH:mm (24-hour)
 * - DateTime: DD-MM-YYYY HH:mm
 *
 * Compatible with ijs-datetime-picker module.
 *
 * @author IndusJS Fleet
 */
object FleetDateTime {

    // ══════════════════════════════════════════════════════════════════════════════
    // CONSTANTS
    // ══════════════════════════════════════════════════════════════════════════════

    const val DEFAULT_DATE_FORMAT = "DD-MM-YYYY"
    const val DEFAULT_TIME_FORMAT = "HH:mm"
    const val DEFAULT_DATETIME_FORMAT = "DD-MM-YYYY HH:mm"
    const val ISO_8601_FORMAT = "YYYY-MM-DDTHH:mm:ssZ"

    private val DAYS_OF_WEEK = listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    )

    private val MONTHS = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val MONTHS_SHORT = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    // ══════════════════════════════════════════════════════════════════════════════
    // CURRENT DATE/TIME
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Get current date and time as FleetDateTimeValue.
     */
    fun now(timeZone: TimeZone = TimeZone.currentSystemDefault()): FleetDateTimeValue {
        val instant = Clock.System.now()
        val localDateTime = instant.toLocalDateTime(timeZone)
        return FleetDateTimeValue(
            year = localDateTime.year,
            month = localDateTime.month.number,
            day = localDateTime.date.day,
            hour = localDateTime.hour,
            minute = localDateTime.minute,
            second = localDateTime.second
        )
    }

    /**
     * Get current date in DD-MM-YYYY format.
     */
    fun today(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
        val today = Clock.System.todayIn(timeZone)
        return formatDateParts(today.day, today.month.number, today.year)
    }

    /**
     * Get current time in HH:mm format.
     */
    fun currentTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
        val now = Clock.System.now().toLocalDateTime(timeZone)
        return formatTimeParts(now.hour, now.minute)
    }

    /**
     * Get current date and time in DD-MM-YYYY HH:mm format.
     */
    fun currentDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
        val now = Clock.System.now().toLocalDateTime(timeZone)
        return "${formatDateParts(now.date.day, now.month.number, now.year)} ${formatTimeParts(now.hour, now.minute)}"
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // PARSING
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Parse datetime string "DD-MM-YYYY HH:mm" to FleetDateTimeValue.
     */
    fun parse(dateTime: String): FleetDateTimeValue? {
        return try {
            val parts = dateTime.trim().split(" ")
            if (parts.size != 2) return null
            parse(parts[0], parts[1])
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse separate date and time strings to FleetDateTimeValue.
     * @param date Date in DD-MM-YYYY format
     * @param time Time in HH:mm format
     */
    fun parse(date: String, time: String): FleetDateTimeValue? {
        return try {
            val dateParts = date.trim().split("-")
            val timeParts = time.trim().split(":")
            if (dateParts.size != 3 || timeParts.size < 2) return null

            FleetDateTimeValue(
                year = dateParts[2].toInt(),
                month = dateParts[1].toInt(),
                day = dateParts[0].toInt(),
                hour = timeParts[0].toInt(),
                minute = timeParts[1].toInt(),
                second = if (timeParts.size > 2) timeParts[2].toInt() else 0
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse date string "DD-MM-YYYY" to FleetDateTimeValue (time defaults to 00:00).
     */
    fun parseDate(date: String): FleetDateTimeValue? {
        return try {
            val parts = date.trim().split("-")
            if (parts.size != 3) return null

            FleetDateTimeValue(
                year = parts[2].toInt(),
                month = parts[1].toInt(),
                day = parts[0].toInt()
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse time string "HH:mm" to Pair<Hour, Minute>.
     */
    fun parseTime(time: String): Pair<Int, Int>? {
        return try {
            val parts = time.trim().split(":")
            if (parts.size < 2) return null
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            null
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // FORMATTING
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Format FleetDateTimeValue to "DD-MM-YYYY".
     */
    fun formatDate(value: FleetDateTimeValue): String {
        return formatDateParts(value.day, value.month, value.year)
    }

    /**
     * Format FleetDateTimeValue to "HH:mm".
     */
    fun formatTime(value: FleetDateTimeValue): String {
        return formatTimeParts(value.hour, value.minute)
    }

    /**
     * Format FleetDateTimeValue to "DD-MM-YYYY HH:mm".
     */
    fun formatDateTime(value: FleetDateTimeValue): String {
        return "${formatDate(value)} ${formatTime(value)}"
    }

    /**
     * Format date parts to "DD-MM-YYYY".
     */
    fun formatDateParts(day: Int, month: Int, year: Int): String {
        val dayStr = if (day < 10) "0$day" else "$day"
        val monthStr = if (month < 10) "0$month" else "$month"
        return "$dayStr-$monthStr-$year"
    }

    /**
     * Format time parts to "HH:mm".
     */
    fun formatTimeParts(hour: Int, minute: Int): String {
        val hourStr = if (hour < 10) "0$hour" else "$hour"
        val minuteStr = if (minute < 10) "0$minute" else "$minute"
        return "$hourStr:$minuteStr"
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // DISPLAY FORMATTING (DD-MMM-YYYY)
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Format FleetDateTimeValue to "DD-MMM-YYYY" (e.g., "05-Feb-2026").
     */
    fun formatDisplayDate(value: FleetDateTimeValue): String {
        val dayStr = if (value.day < 10) "0${value.day}" else "${value.day}"
        val monthStr = MONTHS_SHORT.getOrElse(value.month - 1) { "Unk" }
        return "$dayStr-$monthStr-${value.year}"
    }

    /**
     * Format FleetDateTimeValue to "DD-MMM-YYYY HH:mm" (e.g., "05-Feb-2026 14:30").
     */
    fun formatDisplayDateTime(value: FleetDateTimeValue): String {
        return "${formatDisplayDate(value)} ${formatTime(value)}"
    }

    /**
     * Format DD-MM-YYYY to DD-MMM-YYYY (e.g., "05-02-2026" -> "05-Feb-2026").
     */
    fun formatToDisplayDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "N/A"
        val value = parseDate(dateString) ?: return dateString
        return formatDisplayDate(value)
    }

    /**
     * Format any date format to DD-MMM-YYYY display format.
     * Supports: ISO 8601, YYYY-MM-DD, DD-MM-YYYY formats.
     * Examples:
     * - "2026-02-05T14:30:00Z" -> "05-Feb-2026"
     * - "2026-02-05" -> "05-Feb-2026"
     * - "05-02-2026" -> "05-Feb-2026"
     */
    fun formatAnyToDisplayDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "N/A"

        // Try ISO 8601 format first (e.g., "2026-02-05T14:30:00Z")
        val isoResult = fromIso8601(dateString)
        if (isoResult != null) {
            return formatDisplayDate(isoResult)
        }

        // Try DD-MM-YYYY format
        val ddMmYyyyResult = parseDate(dateString)
        if (ddMmYyyyResult != null) {
            return formatDisplayDate(ddMmYyyyResult)
        }

        // Try YYYY-MM-DD format
        val parts = dateString.split("-")
        if (parts.size == 3 && parts[0].length == 4) {
            return try {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                formatDisplayDate(FleetDateTimeValue(year, month, day))
            } catch (e: Exception) {
                dateString
            }
        }

        return dateString
    }

    /**
     * Format any time format to 12-hour format with AM/PM.
     * Supports: ISO 8601, HH:mm, HH:mm:ss formats.
     * Examples:
     * - "2026-02-05T14:30:00Z" -> "02:30 PM"
     * - "14:30" -> "02:30 PM"
     * - "14:30:00" -> "02:30 PM"
     * - "09:15" -> "09:15 AM"
     */
    fun formatAnyToTime12Hour(timeString: String?): String {
        if (timeString.isNullOrBlank()) return "N/A"

        return try {
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
                else -> return timeString
            }
            formatTime12Hour(hours, minutes)
        } catch (e: Exception) {
            timeString
        }
    }

    /**
     * Format any datetime format to "DD-MMM-YYYY hh:mm AM/PM".
     * Supports: ISO 8601, separate date/time strings.
     * Examples:
     * - formatAnyToDisplayDateTime12Hour("2026-02-05T14:30:00Z") -> "05-Feb-2026 02:30 PM"
     * - formatAnyToDisplayDateTime12Hour("05-02-2026", "14:30") -> "05-Feb-2026 02:30 PM"
     */
    fun formatAnyToDisplayDateTime12Hour(dateString: String?, timeString: String? = null): String {
        if (dateString.isNullOrBlank()) return "N/A"

        // If timeString is provided separately
        if (!timeString.isNullOrBlank()) {
            val formattedDate = formatAnyToDisplayDate(dateString)
            val formattedTime = formatAnyToTime12Hour(timeString)
            return if (formattedTime != "N/A") "$formattedDate $formattedTime" else formattedDate
        }

        // Try to parse as ISO 8601 (includes time)
        val isoResult = fromIso8601(dateString)
        if (isoResult != null) {
            return formatDisplayDateTime12Hour(isoResult)
        }

        // If date contains "T", try to extract and format time separately
        if (dateString.contains("T")) {
            val datePart = dateString.substringBefore("T")
            val timePart = dateString.substringAfter("T")
            val formattedDate = formatAnyToDisplayDate(datePart)
            val formattedTime = formatAnyToTime12Hour(timePart)
            return if (formattedTime != "N/A") "$formattedDate $formattedTime" else formattedDate
        }

        // Just date, no time
        return formatAnyToDisplayDate(dateString)
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // 12-HOUR FORMAT WITH AM/PM
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Format time to 12-hour format with AM/PM (e.g., "02:30 PM").
     * Handles edge cases:
     * - Hour 0 (midnight) → 12:00 AM
     * - Hour 12 (noon) → 12:00 PM
     * - Hours 1-11 → 01:00-11:00 AM
     * - Hours 13-23 → 01:00-11:00 PM
     */
    fun formatTime12Hour(hour: Int, minute: Int): String {
        val period = if (hour < 12) "AM" else "PM"
        val hour12 = when {
            hour == 0 -> 12      // Midnight
            hour == 12 -> 12     // Noon
            hour > 12 -> hour - 12
            else -> hour
        }
        val hourStr = if (hour12 < 10) "0$hour12" else "$hour12"
        val minuteStr = if (minute < 10) "0$minute" else "$minute"
        return "$hourStr:$minuteStr $period"
    }

    /**
     * Format FleetDateTimeValue time to 12-hour format with AM/PM.
     */
    fun formatTime12Hour(value: FleetDateTimeValue): String {
        return formatTime12Hour(value.hour, value.minute)
    }

    /**
     * Format FleetDateTimeValue to "DD-MMM-YYYY hh:mm AM/PM" (e.g., "05-Feb-2026 02:30 PM").
     * Primary display format for user-facing datetime.
     */
    fun formatDisplayDateTime12Hour(value: FleetDateTimeValue): String {
        return "${formatDisplayDate(value)} ${formatTime12Hour(value)}"
    }

    /**
     * Format DD-MM-YYYY HH:mm to "DD-MMM-YYYY hh:mm AM/PM" (12-hour format).
     */
    fun formatToDisplayDateTime12Hour(dateTime: String?): String {
        if (dateTime.isNullOrBlank()) return "N/A"
        val value = parse(dateTime) ?: return dateTime
        return formatDisplayDateTime12Hour(value)
    }

    /**
     * Format DD-MM-YYYY and HH:mm to "DD-MMM-YYYY hh:mm AM/PM" (12-hour format).
     */
    fun formatToDisplayDateTime12Hour(date: String?, time: String?): String {
        if (date.isNullOrBlank()) return "N/A"
        val timeStr = time ?: "00:00"
        val value = parse(date, timeStr) ?: return "$date $timeStr"
        return formatDisplayDateTime12Hour(value)
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // EPOCH-MILLIS DISPLAY ENTRY POINTS (canonical; backend sends/accepts numbers)
    // ══════════════════════════════════════════════════════════════════════════════
    // All API timestamps are now UTC epoch millis (Long). These delegate to
    // FleetEpoch.toValue(ms) (device zone) and the existing FleetDateTimeValue
    // formatters so display style stays consistent. 0/null => "N/A".
    // See docs/UTC_MILLIS_APP_MIGRATION_PLAN.md.

    /**
     * Epoch millis → "DD-MMM-YYYY" (e.g., "05-Feb-2026"). "N/A" when null/0.
     */
    fun formatDate(
        ms: Long?,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        if (ms == null || ms <= 0L) return "N/A"
        val value = FleetEpoch.toValue(ms, timeZone) ?: return "N/A"
        return formatDisplayDate(value)
    }

    /**
     * Epoch millis → "DD-MMM-YYYY hh:mm AM/PM" (e.g., "05-Feb-2026 02:30 PM").
     * "N/A" when null/0.
     */
    fun formatDateTime12Hour(
        ms: Long?,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        if (ms == null || ms <= 0L) return "N/A"
        val value = FleetEpoch.toValue(ms, timeZone) ?: return "N/A"
        return formatDisplayDateTime12Hour(value)
    }

    /**
     * Epoch millis → "hh:mm AM/PM" (e.g., "02:30 PM"). "N/A" when null/0.
     */
    fun formatTime12Hour(
        ms: Long?,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        if (ms == null || ms <= 0L) return "N/A"
        val value = FleetEpoch.toValue(ms, timeZone) ?: return "N/A"
        return formatTime12Hour(value)
    }

    /**
     * Epoch millis → relative "last updated" label ("Just now", "5 min ago",
     * "Yesterday at 10:30 AM", or "05-Feb-2026 02:30 PM" beyond a week).
     * "N/A" when null/0.
     */
    fun formatRelative(
        ms: Long?,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        if (ms == null || ms <= 0L) return "N/A"
        val value = FleetEpoch.toValue(ms, timeZone) ?: return "N/A"
        return toRelativeDescription(value, timeZone)
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // ISO 8601 CONVERSION
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Parse ISO 8601 format (YYYY-MM-DDTHH:mm:ssZ) to FleetDateTimeValue.
     */
    fun fromIso8601(isoString: String): FleetDateTimeValue? {
        return try {
            val cleaned = isoString.trim().replace("Z", "").replace("z", "")
            val parts = cleaned.split("T")
            if (parts.isEmpty()) return null

            val dateParts = parts[0].split("-")
            if (dateParts.size != 3) return null

            val timeParts = if (parts.size > 1) parts[1].split(":") else listOf("0", "0", "0")

            FleetDateTimeValue(
                year = dateParts[0].toInt(),
                month = dateParts[1].toInt(),
                day = dateParts[2].toInt(),
                hour = timeParts.getOrNull(0)?.toInt() ?: 0,
                minute = timeParts.getOrNull(1)?.toInt() ?: 0,
                second = timeParts.getOrNull(2)?.substringBefore(".")?.toInt() ?: 0
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convert ISO 8601 to DD-MM-YYYY format (date only).
     */
    fun fromIso8601ToDate(isoString: String): String? {
        val value = fromIso8601(isoString) ?: return null
        return formatDate(value)
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // DIFFERENCE CALCULATIONS
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Calculate difference between two datetime strings.
     * @param start Start datetime in "DD-MM-YYYY HH:mm" format
     * @param end End datetime in "DD-MM-YYYY HH:mm" format
     */
    fun difference(start: String, end: String): DateTimeDifference? {
        val startValue = parse(start) ?: return null
        val endValue = parse(end) ?: return null
        return differenceBetween(startValue, endValue)
    }

    /**
     * Calculate difference between two date/time pairs.
     */
    fun difference(
        startDate: String,
        startTime: String,
        endDate: String,
        endTime: String
    ): DateTimeDifference? {
        val startValue = parse(startDate, startTime) ?: return null
        val endValue = parse(endDate, endTime) ?: return null
        return differenceBetween(startValue, endValue)
    }

    /**
     * Calculate difference between two FleetDateTimeValue objects.
     */
    fun differenceBetween(start: FleetDateTimeValue, end: FleetDateTimeValue): DateTimeDifference {
        val startInstant = start.toInstant()
        val endInstant = end.toInstant()

        val durationSeconds = (endInstant.epochSeconds - startInstant.epochSeconds)
        val isNegative = durationSeconds < 0
        val absDuration = abs(durationSeconds)

        val totalMinutes = absDuration / 60
        val totalHours = totalMinutes / 60
        val totalDays = totalHours / 24

        val days = (totalDays).toInt()
        val hours = (totalHours % 24).toInt()
        val minutes = (totalMinutes % 60).toInt()
        val seconds = (absDuration % 60).toInt()

        // Calculate approximate years and months
        val years = days / 365
        val remainingDaysAfterYears = days % 365
        val months = remainingDaysAfterYears / 30
        val remainingDays = remainingDaysAfterYears % 30

        return DateTimeDifference(
            totalDays = totalDays,
            totalHours = totalHours,
            totalMinutes = totalMinutes,
            totalSeconds = absDuration,
            years = years,
            months = months,
            days = remainingDays,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            isNegative = isNegative
        )
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // RELATIVE TIME DESCRIPTION
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Convert datetime string to relative description.
     * Examples: "Today at 15:30", "Tomorrow at 10:00", "Next week on Monday at 09:00"
     */
    fun toRelativeDescription(
        dateTime: String,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        val parsed = parse(dateTime) ?: return dateTime
        return toRelativeDescription(parsed, timeZone)
    }

    /**
     * Convert date and time to relative description.
     */
    fun toRelativeDescription(
        date: String,
        time: String,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        val parsed = parse(date, time) ?: return "$date $time"
        return toRelativeDescription(parsed, timeZone)
    }

    /**
     * Convert FleetDateTimeValue to relative description.
     * Uses 12-hour format with AM/PM for time display.
     * Examples: "Today at 02:30 PM", "Tomorrow at 10:00 AM"
     */
    fun toRelativeDescription(
        value: FleetDateTimeValue,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        val today = Clock.System.todayIn(timeZone)
        val targetDate = LocalDate(value.year, value.month, value.day)
        val timeStr = formatTime12Hour(value)

        val daysDiff = (targetDate.toEpochDays() - today.toEpochDays()).toInt()

        return when {
            daysDiff == 0 -> "Today at $timeStr"
            daysDiff == 1 -> "Tomorrow at $timeStr"
            daysDiff == -1 -> "Yesterday at $timeStr"
            daysDiff in 2..6 -> "In $daysDiff days at $timeStr"
            daysDiff in -6..-2 -> "${abs(daysDiff)} days ago at $timeStr"
            daysDiff in 7..13 -> {
                val dayName = getDayOfWeekName(targetDate)
                "Next week on $dayName at $timeStr"
            }
            daysDiff in -13..-7 -> {
                val dayName = getDayOfWeekName(targetDate)
                "Last week on $dayName at $timeStr"
            }
            daysDiff in 14..30 -> {
                val dayName = getDayOfWeekName(targetDate)
                "In ${daysDiff / 7} weeks on $dayName at $timeStr"
            }
            daysDiff in -30..-14 -> {
                val dayName = getDayOfWeekName(targetDate)
                "${abs(daysDiff) / 7} weeks ago on $dayName at $timeStr"
            }
            daysDiff in 31..60 -> {
                val ordinal = getOrdinal(value.day)
                "Next month on $ordinal at $timeStr"
            }
            daysDiff in -60..-31 -> {
                val ordinal = getOrdinal(value.day)
                "Last month on $ordinal at $timeStr"
            }
            else -> {
                val monthName = MONTHS_SHORT[value.month - 1]
                val ordinal = getOrdinal(value.day)
                "$ordinal $monthName ${value.year} at $timeStr"
            }
        }
    }

    /**
     * Get ordinal suffix for a day number (1st, 2nd, 3rd, 4th, etc.)
     */
    private fun getOrdinal(day: Int): String {
        return when {
            day in 11..13 -> "${day}th"
            day % 10 == 1 -> "${day}st"
            day % 10 == 2 -> "${day}nd"
            day % 10 == 3 -> "${day}rd"
            else -> "${day}th"
        }
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // COMPARISON
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Check if start datetime is before end datetime.
     */
    fun isBefore(start: String, end: String): Boolean {
        val startValue = parse(start) ?: return false
        val endValue = parse(end) ?: return false
        return startValue.toComparable() < endValue.toComparable()
    }

    /**
     * Check if start date/time is before end date/time.
     */
    fun isBefore(startDate: String, startTime: String, endDate: String, endTime: String): Boolean {
        val startValue = parse(startDate, startTime) ?: return false
        val endValue = parse(endDate, endTime) ?: return false
        return startValue.toComparable() < endValue.toComparable()
    }

    /**
     * Check if start datetime is after end datetime.
     */
    fun isAfter(start: String, end: String): Boolean {
        val startValue = parse(start) ?: return false
        val endValue = parse(end) ?: return false
        return startValue.toComparable() > endValue.toComparable()
    }

    /**
     * Check if start date/time is after end date/time.
     */
    fun isAfter(startDate: String, startTime: String, endDate: String, endTime: String): Boolean {
        val startValue = parse(startDate, startTime) ?: return false
        val endValue = parse(endDate, endTime) ?: return false
        return startValue.toComparable() > endValue.toComparable()
    }

    /**
     * Check if two datetimes are equal.
     */
    fun isEqual(first: String, second: String): Boolean {
        val firstValue = parse(first) ?: return false
        val secondValue = parse(second) ?: return false
        return firstValue.toComparable() == secondValue.toComparable()
    }

    /**
     * Check if datetime is within the specified range (inclusive).
     */
    fun isInRange(dateTime: String, minDateTime: String?, maxDateTime: String?): Boolean {
        if (minDateTime == null && maxDateTime == null) return true

        val value = parse(dateTime) ?: return false
        val comparable = value.toComparable()

        if (minDateTime != null) {
            val minValue = parse(minDateTime) ?: return false
            if (comparable < minValue.toComparable()) return false
        }

        if (maxDateTime != null) {
            val maxValue = parse(maxDateTime) ?: return false
            if (comparable > maxValue.toComparable()) return false
        }

        return true
    }

    /**
     * Check if date is within the specified range (inclusive).
     * Date format: DD-MM-YYYY
     */
    fun isDateInRange(date: String, minDate: String?, maxDate: String?): Boolean {
        if (minDate == null && maxDate == null) return true

        val dateValue = parseDate(date)?.toDateComparable() ?: return false

        if (minDate != null) {
            val minValue = parseDate(minDate)?.toDateComparable() ?: return false
            if (dateValue < minValue) return false
        }

        if (maxDate != null) {
            val maxValue = parseDate(maxDate)?.toDateComparable() ?: return false
            if (dateValue > maxValue) return false
        }

        return true
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // CALENDAR NAVIGATION HELPERS
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Check if calendar can navigate to the previous month based on minDate.
     * Returns false if all days in the previous month would be before minDate.
     *
     * @param currentMonth Current calendar month (1-12)
     * @param currentYear Current calendar year
     * @param minDate Minimum allowed date in DD-MM-YYYY format (nullable)
     * @return true if navigation to previous month is allowed
     */
    fun canNavigateToPreviousMonth(currentMonth: Int, currentYear: Int, minDate: String?): Boolean {
        if (minDate == null) return true

        // Calculate previous month and year
        val prevMonth = if (currentMonth == 1) 12 else currentMonth - 1
        val prevYear = if (currentMonth == 1) currentYear - 1 else currentYear

        // Get the last day of the previous month
        val lastDayOfPrevMonth = getDaysInMonth(prevYear, prevMonth)

        // Check if the last day of previous month is >= minDate
        val lastDayDate = formatDateParts(lastDayOfPrevMonth, prevMonth, prevYear)
        return isDateInRange(lastDayDate, minDate, null)
    }

    /**
     * Check if calendar can navigate to the next month based on maxDate.
     * Returns false if all days in the next month would be after maxDate.
     *
     * @param currentMonth Current calendar month (1-12)
     * @param currentYear Current calendar year
     * @param maxDate Maximum allowed date in DD-MM-YYYY format (nullable)
     * @return true if navigation to next month is allowed
     */
    fun canNavigateToNextMonth(currentMonth: Int, currentYear: Int, maxDate: String?): Boolean {
        if (maxDate == null) return true

        // Calculate next month and year
        val nextMonth = if (currentMonth == 12) 1 else currentMonth + 1
        val nextYear = if (currentMonth == 12) currentYear + 1 else currentYear

        // Check if the first day of next month is <= maxDate
        val firstDayDate = formatDateParts(1, nextMonth, nextYear)
        return isDateInRange(firstDayDate, null, maxDate)
    }

    /**
     * Get the valid year range for MonthYearPicker based on minDate and maxDate.
     *
     * @param minDate Minimum allowed date in DD-MM-YYYY format (nullable)
     * @param maxDate Maximum allowed date in DD-MM-YYYY format (nullable)
     * @param defaultRangeFromNow Default range in years if no min/max specified (default: 50)
     * @return IntRange of valid years
     */
    fun getValidYearRange(minDate: String?, maxDate: String?, defaultRangeFromNow: Int = 50): IntRange {
        val currentYear = now().year

        val minYear = minDate?.let { parseDate(it)?.year } ?: (currentYear - defaultRangeFromNow)
        val maxYear = maxDate?.let { parseDate(it)?.year } ?: (currentYear + defaultRangeFromNow)

        return minYear..maxYear
    }

    /**
     * Check if a specific month is selectable in the MonthYearPicker.
     * For boundary years, not all months may be valid.
     *
     * @param month Month to check (1-12)
     * @param year Year to check
     * @param minDate Minimum allowed date in DD-MM-YYYY format (nullable)
     * @param maxDate Maximum allowed date in DD-MM-YYYY format (nullable)
     * @return true if the month is selectable
     */
    fun isMonthSelectable(month: Int, year: Int, minDate: String?, maxDate: String?): Boolean {
        // Get the last day of the month to check against minDate
        val lastDayOfMonth = getDaysInMonth(year, month)
        val lastDayDate = formatDateParts(lastDayOfMonth, month, year)

        // Get the first day of the month to check against maxDate
        val firstDayDate = formatDateParts(1, month, year)

        // Month is selectable if:
        // - Its last day is >= minDate (at least some part is after minDate)
        // - Its first day is <= maxDate (at least some part is before maxDate)
        val passesMinCheck = if (minDate != null) {
            val minValue = parseDate(minDate)?.toDateComparable() ?: return true
            val lastDayValue = parseDate(lastDayDate)?.toDateComparable() ?: return true
            lastDayValue >= minValue
        } else true

        val passesMaxCheck = if (maxDate != null) {
            val maxValue = parseDate(maxDate)?.toDateComparable() ?: return true
            val firstDayValue = parseDate(firstDayDate)?.toDateComparable() ?: return true
            firstDayValue <= maxValue
        } else true

        return passesMinCheck && passesMaxCheck
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // DATE CHECKS
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Check if date is today.
     */
    fun isToday(date: String, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
        val parsed = parseDate(date) ?: return false
        val today = Clock.System.todayIn(timeZone)
        return parsed.year == today.year &&
                parsed.month == today.month.number &&
                parsed.day == today.day
    }

    /**
     * Check if date is tomorrow.
     */
    fun isTomorrow(date: String, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
        val parsed = parseDate(date) ?: return false
        val tomorrow = Clock.System.todayIn(timeZone).plus(1, DateTimeUnit.DAY)
        return parsed.year == tomorrow.year &&
                parsed.month == tomorrow.month.number &&
                parsed.day == tomorrow.day
    }

    /**
     * Check if date is yesterday.
     */
    fun isYesterday(date: String, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
        val parsed = parseDate(date) ?: return false
        val yesterday = Clock.System.todayIn(timeZone).minus(1, DateTimeUnit.DAY)
        return parsed.year == yesterday.year &&
                parsed.month == yesterday.month.number &&
                parsed.day == yesterday.day
    }

    /**
     * Check if date is within this week (Monday to Sunday).
     */
    fun isThisWeek(date: String, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
        val parsed = parseDate(date) ?: return false
        val targetDate = LocalDate(parsed.year, parsed.month, parsed.day)
        val today = Clock.System.todayIn(timeZone)

        val startOfWeek = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        val endOfWeek = startOfWeek.plus(6, DateTimeUnit.DAY)

        return targetDate >= startOfWeek && targetDate <= endOfWeek
    }

    /**
     * Check if date is in next week.
     */
    fun isNextWeek(date: String, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
        val parsed = parseDate(date) ?: return false
        val targetDate = LocalDate(parsed.year, parsed.month, parsed.day)
        val today = Clock.System.todayIn(timeZone)

        val startOfNextWeek = today.plus(7 - today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        val endOfNextWeek = startOfNextWeek.plus(6, DateTimeUnit.DAY)

        return targetDate >= startOfNextWeek && targetDate <= endOfNextWeek
    }

    /**
     * Check if date is in last week.
     */
    fun isLastWeek(date: String, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
        val parsed = parseDate(date) ?: return false
        val targetDate = LocalDate(parsed.year, parsed.month, parsed.day)
        val today = Clock.System.todayIn(timeZone)

        val startOfLastWeek = today.minus(today.dayOfWeek.ordinal + 7, DateTimeUnit.DAY)
        val endOfLastWeek = startOfLastWeek.plus(6, DateTimeUnit.DAY)

        return targetDate >= startOfLastWeek && targetDate <= endOfLastWeek
    }

    /**
     * Check if date is in this month.
     */
    fun isThisMonth(date: String, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
        val parsed = parseDate(date) ?: return false
        val today = Clock.System.todayIn(timeZone)
        return parsed.year == today.year && parsed.month == today.month.number
    }

    /**
     * Check if date is in next month.
     */
    fun isNextMonth(date: String, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
        val parsed = parseDate(date) ?: return false
        val today = Clock.System.todayIn(timeZone)
        val nextMonth = if (today.month.number == 12) {
            Pair(today.year + 1, 1)
        } else {
            Pair(today.year, today.month.number + 1)
        }
        return parsed.year == nextMonth.first && parsed.month == nextMonth.second
    }

    /**
     * Check if datetime is in the past.
     */
    fun isPast(
        dateTime: String,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Boolean {
        val parsed = parse(dateTime) ?: return false
        val now = now(timeZone)
        return parsed.toComparable() < now.toComparable()
    }

    /**
     * Check if datetime is in the future.
     */
    fun isFuture(
        dateTime: String,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Boolean {
        val parsed = parse(dateTime) ?: return false
        val now = now(timeZone)
        return parsed.toComparable() > now.toComparable()
    }

    /**
     * Check if date is a weekend (Saturday or Sunday).
     */
    fun isWeekend(date: String): Boolean {
        val parsed = parseDate(date) ?: return false
        val localDate = LocalDate(parsed.year, parsed.month, parsed.day)
        return localDate.dayOfWeek == DayOfWeek.SATURDAY || localDate.dayOfWeek == DayOfWeek.SUNDAY
    }

    /**
     * Check if date is a weekday (Monday to Friday).
     */
    fun isWeekday(date: String): Boolean {
        return !isWeekend(date)
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // DATE MANIPULATION
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Add days to a date.
     * @param date Date in DD-MM-YYYY format
     * @param days Number of days to add (can be negative)
     * @return New date in DD-MM-YYYY format
     */
    fun addDays(date: String, days: Int): String? {
        val parsed = parseDate(date) ?: return null
        val localDate = LocalDate(parsed.year, parsed.month, parsed.day)
        val newDate = localDate.plus(days, DateTimeUnit.DAY)
        return formatDateParts(newDate.day, newDate.month.number, newDate.year)
    }

    /**
     * Add weeks to a date.
     */
    fun addWeeks(date: String, weeks: Int): String? {
        return addDays(date, weeks * 7)
    }

    /**
     * Add months to a date.
     */
    fun addMonths(date: String, months: Int): String? {
        val parsed = parseDate(date) ?: return null
        val localDate = LocalDate(parsed.year, parsed.month, parsed.day)
        val newDate = localDate.plus(months, DateTimeUnit.MONTH)
        return formatDateParts(newDate.day, newDate.month.number, newDate.year)
    }

    /**
     * Add years to a date.
     */
    fun addYears(date: String, years: Int): String? {
        val parsed = parseDate(date) ?: return null
        val localDate = LocalDate(parsed.year, parsed.month, parsed.day)
        val newDate = localDate.plus(years, DateTimeUnit.YEAR)
        return formatDateParts(newDate.day, newDate.month.number, newDate.year)
    }

    /**
     * Add hours to a datetime.
     * @param dateTime DateTime in "DD-MM-YYYY HH:mm" format
     * @param hours Number of hours to add (can be negative)
     */
    fun addHours(dateTime: String, hours: Int): String? {
        val parsed = parse(dateTime) ?: return null
        val instant = parsed.toInstant()
        val newInstant = instant.plus(hours * 3600, DateTimeUnit.SECOND)
        val newDateTime = newInstant.toLocalDateTime(TimeZone.UTC)
        return formatDateTime(
            FleetDateTimeValue(
                year = newDateTime.year,
                month = newDateTime.month.number,
                day = newDateTime.date.day,
                hour = newDateTime.hour,
                minute = newDateTime.minute,
                second = newDateTime.second
            )
        )
    }

    /**
     * Add minutes to a datetime.
     */
    fun addMinutes(dateTime: String, minutes: Int): String? {
        val parsed = parse(dateTime) ?: return null
        val instant = parsed.toInstant()
        val newInstant = instant.plus(minutes * 60, DateTimeUnit.SECOND)
        val newDateTime = newInstant.toLocalDateTime(TimeZone.UTC)
        return formatDateTime(
            FleetDateTimeValue(
                year = newDateTime.year,
                month = newDateTime.month.number,
                day = newDateTime.date.day,
                hour = newDateTime.hour,
                minute = newDateTime.minute,
                second = newDateTime.second
            )
        )
    }

    /**
     * Get start of day (00:00) for a date.
     */
    fun startOfDay(date: String): String? {
        val parsed = parseDate(date) ?: return null
        return "${formatDate(parsed)} 00:00"
    }

    /**
     * Get end of day (23:59) for a date.
     */
    fun endOfDay(date: String): String? {
        val parsed = parseDate(date) ?: return null
        return "${formatDate(parsed)} 23:59"
    }

    /**
     * Get start of week (Monday) for a date.
     */
    fun startOfWeek(date: String): String? {
        val parsed = parseDate(date) ?: return null
        val localDate = LocalDate(parsed.year, parsed.month, parsed.day)
        val monday = localDate.minus(localDate.dayOfWeek.ordinal, DateTimeUnit.DAY)
        return formatDateParts(monday.day, monday.month.number, monday.year)
    }

    /**
     * Get end of week (Sunday) for a date.
     */
    fun endOfWeek(date: String): String? {
        val parsed = parseDate(date) ?: return null
        val localDate = LocalDate(parsed.year, parsed.month, parsed.day)
        val sunday = localDate.plus(6 - localDate.dayOfWeek.ordinal, DateTimeUnit.DAY)
        return formatDateParts(sunday.day, sunday.month.number, sunday.year)
    }

    /**
     * Get first day of month for a date.
     */
    fun startOfMonth(date: String): String? {
        val parsed = parseDate(date) ?: return null
        return formatDateParts(1, parsed.month, parsed.year)
    }

    /**
     * Get last day of month for a date.
     */
    fun endOfMonth(date: String): String? {
        val parsed = parseDate(date) ?: return null
        val lastDay = getDaysInMonth(parsed.year, parsed.month)
        return formatDateParts(lastDay, parsed.month, parsed.year)
    }

    /**
     * Get first day of year for a date.
     */
    fun startOfYear(date: String): String? {
        val parsed = parseDate(date) ?: return null
        return formatDateParts(1, 1, parsed.year)
    }

    /**
     * Get last day of year for a date.
     */
    fun endOfYear(date: String): String? {
        val parsed = parseDate(date) ?: return null
        return formatDateParts(31, 12, parsed.year)
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // UTILITY FUNCTIONS
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Get day of week for a date.
     */
    fun getDayOfWeek(date: String): DayOfWeek? {
        val parsed = parseDate(date) ?: return null
        return LocalDate(parsed.year, parsed.month, parsed.day).dayOfWeek
    }

    /**
     * Get day of week name for a date.
     */
    fun getDayOfWeekName(date: String): String {
        val dayOfWeek = getDayOfWeek(date) ?: return "Unknown"
        return DAYS_OF_WEEK[dayOfWeek.ordinal]
    }

    /**
     * Get day of week name from LocalDate.
     */
    private fun getDayOfWeekName(date: LocalDate): String {
        return DAYS_OF_WEEK[date.dayOfWeek.ordinal]
    }

    /**
     * Get month name (full).
     */
    fun getMonthName(month: Int): String {
        return if (month in 1..12) MONTHS[month - 1] else "Unknown"
    }

    /**
     * Get month name (short).
     */
    fun getMonthNameShort(month: Int): String {
        return if (month in 1..12) MONTHS_SHORT[month - 1] else "Unk"
    }

    /**
     * Get number of days in a month.
     */
    fun getDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 30
        }
    }

    /**
     * Check if year is a leap year.
     */
    fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    /**
     * Get day of week (0=Sunday, 6=Saturday) for first day of month.
     * Compatible with calendar grid rendering.
     */
    fun getFirstDayOfWeekInMonth(year: Int, month: Int): Int {
        val date = LocalDate(year, month, 1)
        return when (date.dayOfWeek) {
            DayOfWeek.SUNDAY -> 0
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
            else -> 0
        }
    }

    /**
     * Get date X days from today.
     */
    fun getDateFromToday(
        daysAhead: Int,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        val today = Clock.System.todayIn(timeZone)
        val targetDate = today.plus(daysAhead, DateTimeUnit.DAY)
        return formatDateParts(targetDate.day, targetDate.month.number, targetDate.year)
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // VALIDATION
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Validate date string (DD-MM-YYYY).
     */
    fun isValidDate(date: String): Boolean {
        val parsed = parseDate(date) ?: return false
        if (parsed.month < 1 || parsed.month > 12) return false
        if (parsed.day < 1 || parsed.day > getDaysInMonth(parsed.year, parsed.month)) return false
        if (parsed.year < 1900 || parsed.year > 2100) return false
        return true
    }

    /**
     * Validate time string (HH:mm).
     */
    fun isValidTime(time: String): Boolean {
        val parsed = parseTime(time) ?: return false
        return parsed.first in 0..23 && parsed.second in 0..59
    }

    /**
     * Validate datetime string (DD-MM-YYYY HH:mm).
     */
    fun isValidDateTime(dateTime: String): Boolean {
        val parts = dateTime.trim().split(" ")
        if (parts.size != 2) return false
        return isValidDate(parts[0]) && isValidTime(parts[1])
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // DURATION FORMATTING
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Format duration to human-readable string.
     * Example: "2 days 5 hours 30 minutes"
     */
    fun formatDuration(difference: DateTimeDifference): String {
        val parts = mutableListOf<String>()

        if (difference.years > 0) parts.add("${difference.years} year${if (difference.years > 1) "s" else ""}")
        if (difference.months > 0) parts.add("${difference.months} month${if (difference.months > 1) "s" else ""}")
        if (difference.days > 0) parts.add("${difference.days} day${if (difference.days > 1) "s" else ""}")
        if (difference.hours > 0) parts.add("${difference.hours} hour${if (difference.hours > 1) "s" else ""}")
        if (difference.minutes > 0) parts.add("${difference.minutes} minute${if (difference.minutes > 1) "s" else ""}")

        if (parts.isEmpty()) {
            return if (difference.seconds > 0) {
                "${difference.seconds} second${if (difference.seconds > 1) "s" else ""}"
            } else {
                "0 minutes"
            }
        }

        val result = parts.joinToString(" ")
        return if (difference.isNegative) "-$result" else result
    }

    /**
     * Format duration to short string.
     * Example: "2d 5h 30m"
     */
    fun formatDurationShort(difference: DateTimeDifference): String {
        val parts = mutableListOf<String>()

        if (difference.years > 0) parts.add("${difference.years}y")
        if (difference.months > 0) parts.add("${difference.months}mo")
        if (difference.days > 0) parts.add("${difference.days}d")
        if (difference.hours > 0) parts.add("${difference.hours}h")
        if (difference.minutes > 0) parts.add("${difference.minutes}m")

        if (parts.isEmpty()) {
            return if (difference.seconds > 0) "${difference.seconds}s" else "0m"
        }

        val result = parts.joinToString(" ")
        return if (difference.isNegative) "-$result" else result
    }

    /**
     * Format duration to approximate human-readable string.
     * Example: "About 2 days", "About 3 hours"
     */
    fun formatDurationHumanReadable(difference: DateTimeDifference): String {
        return when {
            difference.totalDays >= 365 -> {
                val years = (difference.totalDays / 365).toInt()
                "About $years year${if (years > 1) "s" else ""}"
            }
            difference.totalDays >= 30 -> {
                val months = (difference.totalDays / 30).toInt()
                "About $months month${if (months > 1) "s" else ""}"
            }
            difference.totalDays >= 7 -> {
                val weeks = (difference.totalDays / 7).toInt()
                "About $weeks week${if (weeks > 1) "s" else ""}"
            }
            difference.totalDays >= 1 -> {
                "About ${difference.totalDays} day${if (difference.totalDays > 1) "s" else ""}"
            }
            difference.totalHours >= 1 -> {
                "About ${difference.totalHours} hour${if (difference.totalHours > 1) "s" else ""}"
            }
            difference.totalMinutes >= 1 -> {
                "About ${difference.totalMinutes} minute${if (difference.totalMinutes > 1) "s" else ""}"
            }
            else -> "Less than a minute"
        }.let { if (difference.isNegative) "$it ago" else it }
    }

    // ══════════════════════════════════════════════════════════════════════════════════
    // DATE CONSTRAINT UTILITIES (for Cost Entry Screens)
    // ══════════════════════════════════════════════════════════════════════════════════

    /**
     * Get tomorrow's date in DD-MM-YYYY format.
     * Useful for max date constraint (costs can be recorded up to tomorrow).
     */
    fun getTomorrowDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
        return getDateFromToday(1, timeZone)
    }

    /**
     * Get date N years ago from today in DD-MM-YYYY format.
     * Useful for DOB constraints (e.g., driver must be at least 18 years old).
     *
     * @param years Number of years to go back
     */
    fun getDateYearsAgo(
        years: Int,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        val today = Clock.System.todayIn(timeZone)
        val targetDate = today.minus(years, DateTimeUnit.YEAR)
        return formatDateParts(targetDate.day, targetDate.month.number, targetDate.year)
    }

    /**
     * Convert epoch milliseconds to DD-MM-YYYY format.
     * Useful for converting entity timestamps (Long) to date strings for picker constraints.
     *
     * @param timestampMillis Epoch milliseconds (null returns null)
     * @param timeZone Timezone for conversion
     */
    fun timestampToDateString(
        timestampMillis: Long?,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String? {
        if (timestampMillis == null || timestampMillis <= 0) return null
        return try {
            val instant = Instant.fromEpochMilliseconds(timestampMillis)
            val localDateTime = instant.toLocalDateTime(timeZone)
            formatDateParts(localDateTime.date.day, localDateTime.month.number, localDateTime.year)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convert epoch seconds to DD-MM-YYYY format.
     *
     * @param timestampSeconds Epoch seconds (null returns null)
     * @param timeZone Timezone for conversion
     */
    fun timestampSecondsToDateString(
        timestampSeconds: Long?,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String? {
        if (timestampSeconds == null || timestampSeconds <= 0) return null
        return timestampToDateString(timestampSeconds * 1000, timeZone)
    }

    /**
     * Get minimum date for Maintenance Cost entry.
     * Returns vehicle creation date or fallback to "01-01-2000".
     *
     * @param vehicleCreatedAt Vehicle creation date in DD-MM-YYYY or ISO 8601 format
     */
    fun getMinDateForMaintenance(vehicleCreatedAt: String?): String {
        if (vehicleCreatedAt.isNullOrBlank()) return "01-01-2000"

        // Try parsing as DD-MM-YYYY first
        val parsed = parseDate(vehicleCreatedAt)
        if (parsed != null) return formatDate(parsed)

        // Try parsing as ISO 8601
        val fromIso = fromIso8601ToDate(vehicleCreatedAt)
        if (fromIso != null) return fromIso

        return "01-01-2000"
    }

    /**
     * Get minimum date for Trip Cost entry.
     * Returns trip start date/time.
     *
     * @param tripStartDateTime Trip start datetime in DD-MM-YYYY HH:mm or ISO 8601 format
     */
    fun getMinDateForTripCost(tripStartDateTime: String?): String? {
        if (tripStartDateTime.isNullOrBlank()) return null

        // Try parsing as DD-MM-YYYY HH:mm
        val parsed = parse(tripStartDateTime)
        if (parsed != null) return formatDate(parsed)

        // Try parsing as DD-MM-YYYY (date only)
        val dateOnly = parseDate(tripStartDateTime)
        if (dateOnly != null) return formatDate(dateOnly)

        // Try parsing as ISO 8601
        val fromIso = fromIso8601ToDate(tripStartDateTime)
        if (fromIso != null) return fromIso

        return null
    }

    /**
     * Get maximum date for Trip Cost entry.
     * Returns trip end date or tomorrow if trip is ongoing.
     *
     * @param tripEndDateTime Trip end datetime in DD-MM-YYYY HH:mm or ISO 8601 format
     * @param tripIsCompleted Whether the trip is completed
     */
    fun getMaxDateForTripCost(
        tripEndDateTime: String?,
        tripIsCompleted: Boolean = false,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        // If trip is not completed or no end date, use tomorrow
        if (!tripIsCompleted || tripEndDateTime.isNullOrBlank()) {
            return getTomorrowDate(timeZone)
        }

        // Try parsing as DD-MM-YYYY HH:mm
        val parsed = parse(tripEndDateTime)
        if (parsed != null) return formatDate(parsed)

        // Try parsing as DD-MM-YYYY (date only)
        val dateOnly = parseDate(tripEndDateTime)
        if (dateOnly != null) return formatDate(dateOnly)

        // Try parsing as ISO 8601
        val fromIso = fromIso8601ToDate(tripEndDateTime)
        if (fromIso != null) return fromIso

        return getTomorrowDate(timeZone)
    }

    /**
     * Get minimum date for Driver Cost entry.
     * Returns driver joining date or fallback to "01-01-2000".
     *
     * @param driverJoiningDate Driver joining date in DD-MM-YYYY or ISO 8601 format, or timestamp
     */
    fun getMinDateForDriverCost(driverJoiningDate: String?): String {
        if (driverJoiningDate.isNullOrBlank()) return "01-01-2000"

        // Try parsing as DD-MM-YYYY first
        val parsed = parseDate(driverJoiningDate)
        if (parsed != null) return formatDate(parsed)

        // Try parsing as ISO 8601
        val fromIso = fromIso8601ToDate(driverJoiningDate)
        if (fromIso != null) return fromIso

        return "01-01-2000"
    }

    /**
     * Get minimum date for Driver Cost entry from timestamp.
     *
     * @param joiningDateTimestamp Driver joining date as epoch milliseconds
     */
    fun getMinDateForDriverCostFromTimestamp(
        joiningDateTimestamp: Long?,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        val dateString = timestampToDateString(joiningDateTimestamp, timeZone)
        return dateString ?: "01-01-2000"
    }

    /**
     * Validate that a cost date is within trip duration.
     * For full datetime validation.
     *
     * @param costDateTime Cost date/time in DD-MM-YYYY HH:mm format
     * @param tripStartDateTime Trip start date/time
     * @param tripEndDateTime Trip end date/time (null for ongoing trips)
     * @return Pair of (isValid, errorMessage)
     */
    fun validateCostDateTimeForTrip(
        costDateTime: String,
        tripStartDateTime: String?,
        tripEndDateTime: String?
    ): Pair<Boolean, String?> {
        if (tripStartDateTime.isNullOrBlank()) {
            return Pair(true, null) // No constraint if no start date
        }

        val costParsed = parse(costDateTime) ?: parseDate(costDateTime)
        if (costParsed == null) {
            return Pair(false, "Invalid date format")
        }

        // Parse trip start
        val startParsed = parse(tripStartDateTime)
            ?: parseDate(tripStartDateTime)
            ?: fromIso8601(tripStartDateTime)

        if (startParsed != null && costParsed.toComparable() < startParsed.toComparable()) {
            return Pair(false, "Date must be after trip start")
        }

        // Parse trip end (if exists)
        if (!tripEndDateTime.isNullOrBlank()) {
            val endParsed = parse(tripEndDateTime)
                ?: parseDate(tripEndDateTime)
                ?: fromIso8601(tripEndDateTime)

            if (endParsed != null && costParsed.toComparable() > endParsed.toComparable()) {
                return Pair(false, "Date must be before trip end")
            }
        }

        return Pair(true, null)
    }

    /**
     * Validate that a maintenance cost date is within valid range.
     *
     * @param costDate Cost date in DD-MM-YYYY format
     * @param vehicleCreatedAt Vehicle creation date
     * @return Pair of (isValid, errorMessage)
     */
    fun validateMaintenanceCostDate(
        costDate: String,
        vehicleCreatedAt: String?
    ): Pair<Boolean, String?> {
        val costParsed = parseDate(costDate)
        if (costParsed == null) {
            return Pair(false, "Invalid date format")
        }

        val minDate = getMinDateForMaintenance(vehicleCreatedAt)
        val minParsed = parseDate(minDate)

        if (minParsed != null && costParsed.toDateComparable() < minParsed.toDateComparable()) {
            return Pair(false, "Date must be after vehicle registration")
        }

        // Check not in future (max is tomorrow)
        val tomorrow = getTomorrowDate()
        val tomorrowParsed = parseDate(tomorrow)
        if (tomorrowParsed != null && costParsed.toDateComparable() > tomorrowParsed.toDateComparable()) {
            return Pair(false, "Date cannot be more than tomorrow")
        }

        return Pair(true, null)
    }

    /**
     * Validate that a driver cost date is within valid range.
     *
     * @param costDate Cost date in DD-MM-YYYY format
     * @param driverJoiningDate Driver joining date
     * @return Pair of (isValid, errorMessage)
     */
    fun validateDriverCostDate(
        costDate: String,
        driverJoiningDate: String?
    ): Pair<Boolean, String?> {
        val costParsed = parseDate(costDate)
        if (costParsed == null) {
            return Pair(false, "Invalid date format")
        }

        val minDate = getMinDateForDriverCost(driverJoiningDate)
        val minParsed = parseDate(minDate)

        if (minParsed != null && costParsed.toDateComparable() < minParsed.toDateComparable()) {
            return Pair(false, "Date must be after driver joining date")
        }

        // Check not in future (max is tomorrow)
        val tomorrow = getTomorrowDate()
        val tomorrowParsed = parseDate(tomorrow)
        if (tomorrowParsed != null && costParsed.toDateComparable() > tomorrowParsed.toDateComparable()) {
            return Pair(false, "Date cannot be more than tomorrow")
        }

        return Pair(true, null)
    }
}

// ══════════════════════════════════════════════════════════════════════════════════
// DATA CLASSES
// ══════════════════════════════════════════════════════════════════════════════════

/**
 * Represents a parsed date/time value.
 */
data class FleetDateTimeValue(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0
) {
    /**
     * Format to "DD-MM-YYYY".
     */
    fun toDateString(): String = FleetDateTime.formatDateParts(day, month, year)

    /**
     * Format to "HH:mm".
     */
    fun toTimeString(): String = FleetDateTime.formatTimeParts(hour, minute)

    /**
     * Format to "DD-MM-YYYY HH:mm".
     */
    fun toDateTimeString(): String = "${toDateString()} ${toTimeString()}"

    /**
     * Format to "DD-MMM-YYYY" (e.g., "05-Feb-2026").
     */
    fun toDisplayDate(): String = FleetDateTime.formatDisplayDate(this)

    /**
     * Format to "hh:mm AM/PM" (e.g., "02:30 PM").
     */
    fun toTime12HourString(): String = FleetDateTime.formatTime12Hour(this)

    /**
     * Format to "DD-MMM-YYYY hh:mm AM/PM" (e.g., "05-Feb-2026 02:30 PM").
     * Primary display format for user-facing datetime.
     */
    fun toDisplayDateTime12Hour(): String = FleetDateTime.formatDisplayDateTime12Hour(this)

    /**
     * Convert to comparable Long for sorting (YYYYMMDDHHmmss).
     */
    internal fun toComparable(): Long {
        return year.toLong() * 10000000000L +
                month.toLong() * 100000000L +
                day.toLong() * 1000000L +
                hour.toLong() * 10000L +
                minute.toLong() * 100L +
                second.toLong()
    }

    /**
     * Convert to comparable Int for date-only comparison (YYYYMMDD).
     */
    internal fun toDateComparable(): Int {
        return year * 10000 + month * 100 + day
    }

    /**
     * Convert to Instant for calculations.
     */
    internal fun toInstant(): Instant {
        val localDateTime = LocalDateTime(year, month, day, hour, minute, second)
        return localDateTime.toInstant(TimeZone.UTC)
    }

    /**
     * Convert to LocalDate.
     */
    fun toLocalDate(): LocalDate = LocalDate(year, month, day)

    /**
     * Convert to LocalDateTime.
     */
    fun toLocalDateTime(): LocalDateTime = LocalDateTime(year, month, day, hour, minute, second)
}

/**
 * Represents the difference between two date/times.
 */
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
    /**
     * Check if difference is zero.
     */
    fun isZero(): Boolean = totalSeconds == 0L

    /**
     * Format to human-readable string.
     * Example: "2 days 5 hours 30 minutes"
     */
    fun formatted(): String = FleetDateTime.formatDuration(this)

    /**
     * Format to short string.
     * Example: "2d 5h 30m"
     */
    fun shortFormatted(): String = FleetDateTime.formatDurationShort(this)

    /**
     * Format to approximate human-readable string.
     * Example: "About 2 days"
     */
    fun humanReadable(): String = FleetDateTime.formatDurationHumanReadable(this)
}

// ══════════════════════════════════════════════════════════════════════════════
// STRING EXTENSION FUNCTIONS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Extension function to convert any date string to display format (DD-MMM-YYYY).
 * Supports: ISO 8601, YYYY-MM-DD, DD-MM-YYYY formats.
 *
 * Examples:
 * - "2026-02-05T14:30:00Z".toDisplayDate() -> "05-Feb-2026"
 * - "2026-02-05".toDisplayDate() -> "05-Feb-2026"
 * - "05-02-2026".toDisplayDate() -> "05-Feb-2026"
 */
fun String.toDisplayDate(): String = FleetDateTime.formatAnyToDisplayDate(this)

/**
 * Extension function to convert any datetime string to 12-hour display format.
 * Output: DD-MMM-YYYY hh:mm AM/PM (e.g., "05-Feb-2026 02:30 PM")
 *
 * Supports: ISO 8601 and various date formats.
 *
 * Examples:
 * - "2026-02-05T14:30:00Z".toDisplayDateTime12Hour() -> "05-Feb-2026 02:30 PM"
 * - "05-02-2026 14:30".toDisplayDateTime12Hour() -> "05-Feb-2026 02:30 PM"
 */
fun String.toDisplayDateTime12Hour(): String = FleetDateTime.formatAnyToDisplayDateTime12Hour(this)

/**
 * Extension function to convert any time string to 12-hour format.
 *
 * Examples:
 * - "14:30".toTime12Hour() -> "02:30 PM"
 * - "2026-02-05T14:30:00Z".toTime12Hour() -> "02:30 PM"
 */
fun String.toTime12Hour(): String = FleetDateTime.formatAnyToTime12Hour(this)

