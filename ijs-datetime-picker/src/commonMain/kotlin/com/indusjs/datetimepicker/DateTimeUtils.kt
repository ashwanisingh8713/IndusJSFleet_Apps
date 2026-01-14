package com.indusjs.datetimepicker

import com.indusjs.datetimeutils.FleetDateTime
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * Utility functions for date and time operations.
 * Delegates to FleetDateTime from ijs-datetime-utils for core functionality.
 */
object DateTimeUtils {

    /**
     * Get current date in DD-MM-YYYY format.
     */
    fun getCurrentDate(): String = FleetDateTime.today()

    /**
     * Get current date parts (day, month, year).
     */
    fun getCurrentDateParts(): Triple<Int, Int, Int> {
        val now = FleetDateTime.now()
        return Triple(now.day, now.month, now.year)
    }

    /**
     * Get current time parts (hour, minute).
     */
    fun getCurrentTimeParts(): Pair<Int, Int> {
        val now = FleetDateTime.now()
        return Pair(now.hour, now.minute)
    }

    /**
     * Get current time in HH:MM format.
     */
    fun getCurrentTime(): String = FleetDateTime.currentTime()

    /**
     * Get date string for a date X days from today.
     */
    fun getDateFromToday(daysAhead: Int): String = FleetDateTime.getDateFromToday(daysAhead)

    /**
     * Format date parts to DD-MM-YYYY string.
     */
    fun formatDateParts(day: Int, month: Int, year: Int): String =
        FleetDateTime.formatDateParts(day, month, year)

    /**
     * Format time parts to HH:MM string.
     */
    fun formatTimeParts(hour: Int, minute: Int): String =
        FleetDateTime.formatTimeParts(hour, minute)

    /**
     * Get month name.
     */
    fun getMonthName(month: Int): String = FleetDateTime.getMonthName(month)

    /**
     * Get number of days in a month.
     */
    fun getDaysInMonth(year: Int, month: Int): Int = FleetDateTime.getDaysInMonth(year, month)

    /**
     * Check if year is a leap year.
     */
    fun isLeapYear(year: Int): Boolean = FleetDateTime.isLeapYear(year)

    /**
     * Get the day of week (0=Sunday, 6=Saturday) for the first day of a month.
     */
    fun getFirstDayOfWeek(year: Int, month: Int): Int =
        FleetDateTime.getFirstDayOfWeekInMonth(year, month)

    /**
     * Check if a specific day is enabled based on min/max date constraints.
     */
    fun isDayEnabled(
        day: Int,
        month: Int,
        year: Int,
        minDate: String?,
        maxDate: String?
    ): Boolean {
        val currentDate = formatDateParts(day, month, year)
        return isDateInRange(currentDate, minDate, maxDate)
    }

    /**
     * Check if a date string is within the specified range.
     * Date format: DD-MM-YYYY
     */
    fun isDateInRange(date: String, minDate: String?, maxDate: String?): Boolean =
        FleetDateTime.isDateInRange(date, minDate, maxDate)

    /**
     * Parse date string (DD-MM-YYYY) to LocalDate.
     */
    fun parseDate(dateString: String): LocalDate? {
        val parsed = FleetDateTime.parseDate(dateString) ?: return null
        return LocalDate(parsed.year, parsed.month, parsed.day)
    }

    /**
     * Format LocalDate to DD-MM-YYYY string.
     */
    @Suppress("DEPRECATION")
    fun formatDate(date: LocalDate): String =
        formatDateParts(date.dayOfMonth, date.monthNumber, date.year)

    /**
     * Format date for display: "18 April 2024"
     * Input: DD-MM-YYYY (e.g., "18-04-2024")
     * Output: "18 April 2024"
     */
    fun formatDateForDisplay(date: String): String {
        if (date.isBlank()) return ""
        return try {
            val parts = date.split("-")
            if (parts.size != 3) return date
            val day = parts[0].toIntOrNull() ?: return date
            val month = parts[1].toIntOrNull() ?: return date
            val year = parts[2].toIntOrNull() ?: return date
            "$day ${getMonthName(month)} $year"
        } catch (e: Exception) {
            date
        }
    }

    /**
     * Format time for display: "10:30 AM" (12-hour format)
     * Input: HH:MM (24-hour, e.g., "14:30")
     * Output: "2:30 PM"
     */
    fun formatTimeForDisplay(time: String): String {
        if (time.isBlank()) return ""
        return try {
            val parts = time.split(":")
            if (parts.size != 2) return time
            val hour = parts[0].toIntOrNull() ?: return time
            val minute = parts[1].toIntOrNull() ?: return time

            val period = if (hour >= 12) "PM" else "AM"
            val hour12 = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            val minuteStr = if (minute < 10) "0$minute" else "$minute"
            "$hour12:$minuteStr $period"
        } catch (e: Exception) {
            time
        }
    }

    /**
     * Format date and time for display: "18 April 2024, 10:30 AM"
     * Input: date (DD-MM-YYYY), time (HH:MM 24-hour)
     * Output: "18 April 2024, 10:30 AM"
     */
    fun formatDateTimeForDisplay(date: String, time: String): String {
        val formattedDate = formatDateForDisplay(date)
        val formattedTime = formatTimeForDisplay(time)

        return when {
            formattedDate.isNotBlank() && formattedTime.isNotBlank() -> "$formattedDate, $formattedTime"
            formattedDate.isNotBlank() -> formattedDate
            formattedTime.isNotBlank() -> formattedTime
            else -> ""
        }
    }
}
