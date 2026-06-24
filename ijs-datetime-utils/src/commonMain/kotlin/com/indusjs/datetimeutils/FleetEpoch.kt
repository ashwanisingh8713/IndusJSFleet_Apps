package com.indusjs.datetimeutils

import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * EpochMillis is the canonical timestamp representation across the whole stack:
 * a UTC instant in milliseconds since the Unix epoch. The backend stores it as
 * BIGINT and sends/receives it as a JSON number; on the client it is a [Long].
 *
 * See docs/UTC_MILLIS_TIMESTAMP_PLAN.md.
 */
typealias EpochMillis = Long

/**
 * Conversion, comparison and date-math helpers for [EpochMillis].
 *
 * This object is purely additive (Phase 0): it introduces the epoch-millis API
 * the migration will standardize on, without touching the existing
 * [FleetDateTime] string/ISO code. Display formatting delegates to
 * [FleetDateTime] so the app keeps a single formatting style.
 */
object FleetEpoch {

    /** Milliseconds in one 24-hour UTC day. */
    const val MILLIS_PER_DAY: Long = 24L * 60L * 60L * 1000L

    // ── Current time ──────────────────────────────────────────────────────

    /** Current instant as UTC epoch milliseconds. */
    fun now(): EpochMillis = Clock.System.now().toEpochMilliseconds()

    // ── Conversions ───────────────────────────────────────────────────────

    /**
     * Convert epoch millis to a [FleetDateTimeValue] in the given time zone
     * (defaults to the device zone — for display only). Returns null on bad input.
     */
    fun toValue(
        ms: EpochMillis?,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): FleetDateTimeValue? {
        if (ms == null) return null
        return try {
            val ldt = Instant.fromEpochMilliseconds(ms).toLocalDateTime(timeZone)
            FleetDateTimeValue(
                year = ldt.year,
                month = ldt.month.number,
                day = ldt.date.day,
                hour = ldt.hour,
                minute = ldt.minute,
                second = ldt.second
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convert a [FleetDateTimeValue] (as produced by date/time pickers) to epoch
     * millis. [timeZone] is the zone the wall-clock value is expressed in; the
     * result is always a UTC instant. Returns null on bad input.
     */
    fun fromValue(
        value: FleetDateTimeValue?,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): EpochMillis? {
        if (value == null) return null
        return try {
            // Reuse FleetDateTimeValue.toLocalDateTime() (positional constructor,
            // matches the kotlinx-datetime version this module already targets).
            value.toLocalDateTime().toInstant(timeZone).toEpochMilliseconds()
        } catch (e: Exception) {
            null
        }
    }

    // ── Display (delegates to FleetDateTime for a single formatting style) ──

    /** Epoch millis → DD-MM-YYYY for display. */
    fun toDisplayDate(
        ms: EpochMillis?,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String? = FleetDateTime.timestampToDateString(ms, timeZone)

    // ── Date-only helpers (UTC calendar day) ──────────────────────────────

    /** Start of the UTC calendar day containing [ms] (00:00:00.000 UTC). */
    fun startOfDayUtc(ms: EpochMillis): EpochMillis =
        ms.floorDiv(MILLIS_PER_DAY) * MILLIS_PER_DAY

    /** Whole UTC calendar days from [a] to [b] (negative if [b] precedes [a]). */
    fun daysBetween(a: EpochMillis, b: EpochMillis): Long =
        (startOfDayUtc(b) - startOfDayUtc(a)) / MILLIS_PER_DAY

    /** Shift [ms] by [days] UTC days (days are exact in UTC). */
    fun addDays(ms: EpochMillis, days: Long): EpochMillis = ms + days * MILLIS_PER_DAY

    // ── Comparisons (plain integer ops; null-tolerant) ────────────────────

    fun isBefore(a: EpochMillis?, b: EpochMillis?): Boolean =
        a != null && b != null && a < b

    fun isAfter(a: EpochMillis?, b: EpochMillis?): Boolean =
        a != null && b != null && a > b

    fun isEqual(a: EpochMillis?, b: EpochMillis?): Boolean = a == b

    /** True when [ms] is within [start, end] inclusive (nulls → false). */
    fun isInRange(ms: EpochMillis?, start: EpochMillis?, end: EpochMillis?): Boolean =
        ms != null && start != null && end != null && ms in start..end

    /** True when the date is strictly in the past (UTC day granularity). */
    fun isExpired(ms: EpochMillis?): Boolean = ms != null && ms < now()

    /** Days from now until [ms] (negative if already past), UTC-day granularity. */
    fun daysUntil(ms: EpochMillis?): Long? =
        if (ms == null) null else daysBetween(now(), ms)
}
