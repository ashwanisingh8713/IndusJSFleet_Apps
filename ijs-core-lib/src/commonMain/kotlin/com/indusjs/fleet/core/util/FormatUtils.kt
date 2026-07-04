package com.indusjs.fleet.core.util

import kotlin.concurrent.Volatile
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Utility functions for formatting numbers, currency, and percentages
 * in a cross-platform compatible way.
 */

/**
 * Localized compact-currency unit words (thousand / lakh / crore). Defaults are the English
 * abbreviations; [com.indusjs.fleet.core.util.CompactCurrencyUnits] is repopulated from string
 * resources at the app root whenever the in-app language changes (see sharedUI App root), so the
 * plain [formatCurrency] callers stay locale-correct without threading a Composable through ~48 sites.
 * Process-wide by design — mirrors how `Locale.setDefault` drives the rest of formatting.
 */
object CompactCurrencyUnits {
    @Volatile var thousand: String = "K"
    @Volatile var lakh: String = "L"
    @Volatile var crore: String = "Cr"
}

/**
 * Format a number as Indian Rupee currency, compact (thousand / lakh / crore) using
 * [CompactCurrencyUnits] so the unit word follows the in-app language (₹1.2 Cr / ₹1.2 करोड़).
 */
fun formatCurrency(value: Double): String {
    val absValue = value.absoluteValue
    val sign = if (value < 0) "-" else ""

    return when {
        absValue >= 10_000_000 -> "${sign}₹${formatIndianNumber(absValue / 10_000_000, 2)} ${CompactCurrencyUnits.crore}"
        absValue >= 100_000 -> "${sign}₹${formatIndianNumber(absValue / 100_000, 2)} ${CompactCurrencyUnits.lakh}"
        absValue >= 1000 -> "${sign}₹${formatIndianNumber(absValue / 1000, 1)} ${CompactCurrencyUnits.thousand}"
        else -> "${sign}₹${formatWithCommas(absValue.roundToInt().toLong())}"
    }
}

/**
 * Canonical "quiet money" formatter: full Indian-grouped rupees, with paise shown (2dp) ONLY when
 * nonzero — whole rupees read ₹60,000 (not ₹60,000.00), per §H. No compact K/L/Cr abbreviation.
 */
fun formatCurrencyFull(value: Double): String {
    val sign = if (value < 0) "-" else ""
    val abs = value.absoluteValue
    var whole = abs.toLong()
    var paise = ((abs - whole) * 100).roundToInt()
    if (paise >= 100) { whole += 1; paise = 0 } // carry when paise rounds up to a full rupee
    val grouped = formatWithCommas(whole)
    return if (paise == 0) "${sign}₹$grouped"
           else "${sign}₹$grouped.${paise.toString().padStart(2, '0')}"
}

/**
 * Format a percentage value.
 */
fun formatPercentage(value: Double): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return if (rounded == rounded.toLong().toDouble()) {
        "${rounded.toLong()}%"
    } else {
        "${rounded}%"
    }
}

/**
 * Format a number with commas for thousands separator.
 */
fun formatWithCommas(value: Long): String {
    if (value == 0L) return "0"

    val str = value.absoluteValue.toString()
    val result = StringBuilder()
    var count = 0

    for (i in str.length - 1 downTo 0) {
        result.insert(0, str[i])
        count++
        // Indian number system: first comma after 3 digits, then every 2 digits
        if (i > 0) {
            if (count == 3 || (count > 3 && (count - 3) % 2 == 0)) {
                result.insert(0, ',')
            }
        }
    }

    return if (value < 0) "-$result" else result.toString()
}

/**
 * Format an Indian number to specified decimal places.
 */
private fun formatIndianNumber(value: Double, decimals: Int): String {
    val factor = 10.0.pow(decimals)
    val rounded = (value * factor).roundToInt() / factor

    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}

/**
 * Format a distance in kilometers.
 */
fun formatDistance(distanceKm: Double): String {
    return when {
        distanceKm >= 1000 -> "${formatIndianNumber(distanceKm / 1000, 1)} K km"
        distanceKm >= 1 -> "${distanceKm.roundToInt()} km"
        else -> "${(distanceKm * 1000).roundToInt()} m"
    }
}

/**
 * Format a weight value with appropriate unit.
 */
fun formatWeight(weightKg: Double, unit: String = "kg"): String {
    return when {
        weightKg >= 1000 -> "${formatIndianNumber(weightKg / 1000, 2)} tonnes"
        else -> "${weightKg.roundToInt()} $unit"
    }
}

/**
 * Format a number as a simple integer string.
 */
fun formatNumber(value: Int): String = formatWithCommas(value.toLong())

/**
 * Format a number as a simple double with specified decimals.
 */
fun formatNumber(value: Double, decimals: Int = 2): String {
    val factor = 10.0.pow(decimals)
    val rounded = (value * factor).roundToInt() / factor
    return rounded.toString()
}

