package com.indusjs.datetimepicker

import com.indusjs.datetimeutils.FleetDateTime
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * Utility functions for date and time operations.
 * Delegates to FleetDateTime from ijs-datetime-utils for core functionality.
 */
internal object DateTimeUtils {

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
}
