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
     * Check if calendar can navigate to the previous month based on minDate.
     */
    fun canNavigateToPreviousMonth(currentMonth: Int, currentYear: Int, minDate: String?): Boolean =
        FleetDateTime.canNavigateToPreviousMonth(currentMonth, currentYear, minDate)

    /**
     * Check if calendar can navigate to the next month based on maxDate.
     */
    fun canNavigateToNextMonth(currentMonth: Int, currentYear: Int, maxDate: String?): Boolean =
        FleetDateTime.canNavigateToNextMonth(currentMonth, currentYear, maxDate)

    /**
     * Get the valid year range for MonthYearPicker based on minDate and maxDate.
     */
    fun getValidYearRange(minDate: String?, maxDate: String?, defaultRangeFromNow: Int = 50): IntRange =
        FleetDateTime.getValidYearRange(minDate, maxDate, defaultRangeFromNow)

    /**
     * Check if a specific month is selectable in the MonthYearPicker.
     */
    fun isMonthSelectable(month: Int, year: Int, minDate: String?, maxDate: String?): Boolean =
        FleetDateTime.isMonthSelectable(month, year, minDate, maxDate)

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
     * Format date for display: "05-Feb-2026" (DD-MMM-YYYY)
     * Input: DD-MM-YYYY (e.g., "05-02-2026")
     * Output: "05-Feb-2026"
     */
    fun formatDateForDisplay(date: String): String {
        if (date.isBlank()) return ""
        return FleetDateTime.formatToDisplayDate(date)
    }

    /**
     * Format time for display: "02:30 PM" (12-hour format)
     * Input: HH:MM (24-hour, e.g., "14:30")
     * Output: "02:30 PM"
     */
    fun formatTimeForDisplay(time: String): String {
        if (time.isBlank()) return ""
        return try {
            val parts = time.split(":")
            if (parts.size != 2) return time
            val hour = parts[0].toIntOrNull() ?: return time
            val minute = parts[1].toIntOrNull() ?: return time
            FleetDateTime.formatTime12Hour(hour, minute)
        } catch (e: Exception) {
            time
        }
    }

    /**
     * Format date and time for display: "05-Feb-2026 02:30 PM"
     * Input: date (DD-MM-YYYY), time (HH:MM 24-hour)
     * Output: "05-Feb-2026 02:30 PM"
     */
    fun formatDateTimeForDisplay(date: String, time: String): String {
        if (date.isBlank() && time.isBlank()) return ""
        return FleetDateTime.formatToDisplayDateTime12Hour(date, time)
    }
}
