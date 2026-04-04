package com.ijs.reports.presentation.vehicle

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
     * Calculate start and end dates based on the selected period.
     * Returns dates in YYYY-MM-DD format per OpenAPI spec (Docs/api_modules/openapi.json).
     */
    fun getDateRangeForPeriod(
        period: String,
        customStartDate: String,
        customEndDate: String
    ): Pair<String, String> {
        val today = today()

        return when (period) {
            "today" -> {
                val dateStr = formatDate(today)
                Pair(dateStr, dateStr)
            }
            "weekly" -> {
                val startDate = today.minus(DatePeriod(days = 6))
                Pair(formatDate(startDate), formatDate(today))
            }
            "monthly" -> {
                val startOfMonth = LocalDate(today.year, today.month, 1)
                val endOfMonth = getLastDayOfMonth(today.year, today.month.number)
                Pair(formatDate(startOfMonth), formatDate(endOfMonth))
            }
            "yearly" -> {
                val startOfYear = LocalDate(today.year, 1, 1)
                val endOfYear = LocalDate(today.year, 12, 31)
                Pair(formatDate(startOfYear), formatDate(endOfYear))
            }
            "all" -> {
                val startDate = LocalDate(2020, 1, 1)
                Pair(formatDate(startDate), formatDate(today))
            }
            "custom" -> {
                val start = customStartDate.ifBlank {
                    formatDate(LocalDate(today.year, today.month, 1))
                }
                val end = customEndDate.ifBlank {
                    formatDate(getLastDayOfMonth(today.year, today.month.number))
                }
                Pair(start, end)
            }
            else -> {
                val startOfMonth = LocalDate(today.year, today.month, 1)
                val endOfMonth = getLastDayOfMonth(today.year, today.month.number)
                Pair(formatDate(startOfMonth), formatDate(endOfMonth))
            }
        }
    }

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

    /**
     * Format date as YYYY-MM-DD per OpenAPI spec.
     */
    private fun formatDate(date: LocalDate): String {
        return date.toString() // LocalDate.toString() returns YYYY-MM-DD (ISO 8601)
    }

    private fun getLastDayOfMonth(year: Int, month: Int): LocalDate {
        return if (month == 12) {
            LocalDate(year + 1, 1, 1).minus(DatePeriod(days = 1))
        } else {
            LocalDate(year, month + 1, 1).minus(DatePeriod(days = 1))
        }
    }
}
