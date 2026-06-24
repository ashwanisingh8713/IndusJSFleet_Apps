package com.ijs.reports.presentation.vehicle

import com.indusjs.datetimeutils.FleetEpoch
import com.indusjs.fleet.core.util.convertToEpochMillis
import com.indusjs.fleet.core.util.currentTimeMillis
import kotlinx.datetime.*

/**
 * Utility for calculating date ranges based on report periods.
 * Uses kotlinx-datetime for proper date calculations.
 * Extracted from VehiclePLViewModel to keep it under 500 lines.
 */
object VehiclePLDateCalculator {

    private fun today(): LocalDate {
        return Instant.fromEpochMilliseconds(currentTimeMillis())
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    /**
     * Calculate the start and end of the selected period as UTC epoch millis.
     * For "custom", the customStartDate/customEndDate picker strings (DD-MM-YYYY)
     * are converted; blank custom bounds fall back to the current month.
     * Returns null bounds when conversion fails.
     */
    fun getDateRangeForPeriod(
        period: String,
        customStartDate: String,
        customEndDate: String
    ): Pair<Long?, Long?> {
        val today = today()

        return when (period) {
            "today" -> {
                val ms = toEpochMillis(today)
                Pair(ms, ms)
            }
            "weekly" -> {
                val startDate = today.minus(DatePeriod(days = 6))
                Pair(toEpochMillis(startDate), toEpochMillis(today))
            }
            "monthly" -> {
                val startOfMonth = LocalDate(today.year, today.month, 1)
                val endOfMonth = getLastDayOfMonth(today.year, today.month.number)
                Pair(toEpochMillis(startOfMonth), toEpochMillis(endOfMonth))
            }
            "yearly" -> {
                val startOfYear = LocalDate(today.year, 1, 1)
                val endOfYear = LocalDate(today.year, 12, 31)
                Pair(toEpochMillis(startOfYear), toEpochMillis(endOfYear))
            }
            "all" -> {
                val startDate = LocalDate(2020, 1, 1)
                Pair(toEpochMillis(startDate), toEpochMillis(today))
            }
            "custom" -> {
                val start = customStartDate.takeIf { it.isNotBlank() }
                    ?.let { convertToEpochMillis(it) }
                    ?: toEpochMillis(LocalDate(today.year, today.month, 1))
                val end = customEndDate.takeIf { it.isNotBlank() }
                    ?.let { convertToEpochMillis(it) }
                    ?: toEpochMillis(getLastDayOfMonth(today.year, today.month.number))
                Pair(start, end)
            }
            else -> {
                val startOfMonth = LocalDate(today.year, today.month, 1)
                val endOfMonth = getLastDayOfMonth(today.year, today.month.number)
                Pair(toEpochMillis(startOfMonth), toEpochMillis(endOfMonth))
            }
        }
    }

    /** Convert a calendar date (interpreted at local midnight) to UTC epoch millis. */
    private fun toEpochMillis(date: LocalDate): Long? = FleetEpoch.fromValue(
        com.indusjs.datetimeutils.FleetDateTimeValue(
            year = date.year, month = date.month.number, day = date.day,
            hour = 0, minute = 0, second = 0
        )
    )

    /**
     * Generate a human-readable label for the current period.
     */
    fun getCurrentPeriodLabel(
        period: String,
        customStartDate: String,
        customEndDate: String
    ): String {
        val today = today()
        val monthName = today.month.name.lowercase()
            .replaceFirstChar { it.uppercase() }

        return when (period) {
            "today" -> "$monthName ${today.dayOfMonth}, ${today.year}"
            "weekly" -> "Week of ${monthName.take(3)} ${today.dayOfMonth}, ${today.year}"
            "monthly" -> "$monthName ${today.year}"
            "yearly" -> "${today.year}"
            "all" -> "All Time"
            "custom" -> {
                val start = customStartDate.ifBlank { "Start" }
                val end = customEndDate.ifBlank { "End" }
                "$start - $end"
            }
            else -> "$monthName ${today.year}"
        }
    }

    private fun getLastDayOfMonth(year: Int, month: Int): LocalDate {
        return if (month == 12) {
            LocalDate(year + 1, 1, 1).minus(DatePeriod(days = 1))
        } else {
            LocalDate(year, month + 1, 1).minus(DatePeriod(days = 1))
        }
    }
}
