package com.indusjs.fleet.core.util

import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Utility functions for formatting numbers, currency, and percentages
 * in a cross-platform compatible way.
 */

/**
 * Format a number as Indian Rupee currency.
 * Uses Indian number formatting (lakhs, crores).
 */
fun formatCurrency(value: Double): String {
    val absValue = value.absoluteValue
    val sign = if (value < 0) "-" else ""

    return when {
        absValue >= 10_000_000 -> "${sign}₹${formatIndianNumber(absValue / 10_000_000, 2)} Cr"
        absValue >= 100_000 -> "${sign}₹${formatIndianNumber(absValue / 100_000, 2)} L"
        absValue >= 1000 -> "${sign}₹${formatIndianNumber(absValue / 1000, 1)}K"
        else -> "${sign}₹${formatWithCommas(absValue.roundToInt().toLong())}"
    }
}

/**
 * Format a number with proper Indian number formatting.
 */
fun formatCurrencyFull(value: Double): String {
    val sign = if (value < 0) "-" else ""
    return "${sign}₹${formatWithCommas(value.absoluteValue.roundToInt().toLong())}"
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

