package com.indusjs.uicomponents.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Visual transformation for date input (DD-MM-YYYY format).
 * Displays delimiters visually while keeping raw digits as actual value.
 */
class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(8)
        val out = StringBuilder()

        for (i in trimmed.indices) {
            out.append(trimmed[i])
            if (i == 1 || i == 3) {
                out.append("-")
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    offset <= 8 -> offset + 2
                    else -> 10
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    offset <= 10 -> offset - 2
                    else -> 8
                }
            }
        }

        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

/**
 * Visual transformation for time input (HH:MM format).
 * Displays colon visually while keeping raw digits as actual value.
 */
class TimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(4)
        val out = StringBuilder()

        for (i in trimmed.indices) {
            out.append(trimmed[i])
            if (i == 1) {
                out.append(":")
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    else -> 5
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else -> 4
                }
            }
        }

        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

/**
 * Visual transformation for mobile number input (10 digits).
 * Displays as XXX-XXX-XXXX format.
 */
class MobileVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(10)
        val out = StringBuilder()

        for (i in trimmed.indices) {
            out.append(trimmed[i])
            if (i == 2 || i == 5) {
                out.append("-")
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 3 -> offset
                    offset <= 6 -> offset + 1
                    offset <= 10 -> offset + 2
                    else -> 12
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 3 -> offset
                    offset <= 7 -> offset - 1
                    offset <= 12 -> offset - 2
                    else -> 10
                }
            }
        }

        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

/**
 * Visual transformation for date input (YYYY-MM-DD format - ISO format).
 * Displays delimiters visually while keeping raw digits as actual value.
 */
class IsoDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(8)
        val out = StringBuilder()

        for (i in trimmed.indices) {
            out.append(trimmed[i])
            if (i == 3 || i == 5) {
                out.append("-")
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 4 -> offset
                    offset <= 6 -> offset + 1
                    offset <= 8 -> offset + 2
                    else -> 10
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 4 -> offset
                    offset <= 7 -> offset - 1
                    offset <= 10 -> offset - 2
                    else -> 8
                }
            }
        }

        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

/**
 * Filters input to only allow digits.
 * @param input The raw input string
 * @param maxLength Maximum number of digits allowed
 * @return Filtered string containing only digits up to maxLength
 */
fun filterDigitsOnly(input: String, maxLength: Int): String {
    return input.filter { it.isDigit() }.take(maxLength)
}

fun isValidEmail(email: String): Boolean {
    if (email.isBlank()) return false
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return emailRegex.matches(email)
}

fun isValidMobile(mobile: String): Boolean {
    val digits = mobile.filter { it.isDigit() }
    return digits.length == 10
}

/**
 * Validates date in raw format (8 digits: DDMMYYYY).
 * Checks for valid day (1-31), month (1-12), year (1900-2100).
 */
fun isValidDateRaw(rawDigits: String): Boolean {
    if (rawDigits.length != 8) return false
    val day = rawDigits.substring(0, 2).toIntOrNull() ?: return false
    val month = rawDigits.substring(2, 4).toIntOrNull() ?: return false
    val year = rawDigits.substring(4, 8).toIntOrNull() ?: return false

    if (month < 1 || month > 12) return false
    if (year < 1900 || year > 2100) return false

    val maxDay = when (month) {
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

    return day in 1..maxDay
}

/**
 * Validates time in raw format (4 digits: HHMM).
 * Checks for valid hour (0-23) and minute (0-59).
 */
fun isValidTimeRaw(rawDigits: String): Boolean {
    if (rawDigits.length != 4) return false
    val hour = rawDigits.substring(0, 2).toIntOrNull() ?: return false
    val minute = rawDigits.substring(2, 4).toIntOrNull() ?: return false
    return hour in 0..23 && minute in 0..59
}

fun formatTimeRaw(rawDigits: String): String {
    if (rawDigits.length != 4) return rawDigits
    return "${rawDigits.substring(0, 2)}:${rawDigits.substring(2, 4)}"
}

fun parseTimeToRaw(timeStr: String): String {
    return timeStr.replace(":", "").filter { it.isDigit() }.take(4)
}

fun formatToIsoDate(rawDigits: String): String {
    if (rawDigits.length != 8) return rawDigits
    return "${rawDigits.substring(0, 4)}-${rawDigits.substring(4, 6)}-${rawDigits.substring(6, 8)}"
}

fun parseIsoDateToRaw(isoDate: String): String {
    return isoDate.replace("-", "")
}

fun formatToDdMmYyyy(rawDigits: String): String {
    if (rawDigits.length != 8) return rawDigits
    return "${rawDigits.substring(0, 2)}-${rawDigits.substring(2, 4)}-${rawDigits.substring(4, 8)}"
}

fun parseDdMmYyyyToRaw(dateStr: String): String {
    return dateStr.replace("-", "")
}

fun convertDdMmYyyyToIso(rawDigits: String): String {
    if (rawDigits.length != 8) return rawDigits
    val day = rawDigits.substring(0, 2)
    val month = rawDigits.substring(2, 4)
    val year = rawDigits.substring(4, 8)
    return "$year-$month-$day"
}

fun convertIsoToDdMmYyyyRaw(isoDate: String): String {
    if (isoDate.isBlank()) return ""
    val parts = isoDate.split("-")
    if (parts.size != 3) return isoDate.replace("-", "")
    return "${parts[2]}${parts[1]}${parts[0]}"
}

fun convertIsoToDdMmYyyy(isoDate: String): String {
    if (isoDate.isBlank()) return ""
    val parts = isoDate.split("-")
    if (parts.size != 3) return isoDate
    return "${parts[2]}-${parts[1]}-${parts[0]}"
}
