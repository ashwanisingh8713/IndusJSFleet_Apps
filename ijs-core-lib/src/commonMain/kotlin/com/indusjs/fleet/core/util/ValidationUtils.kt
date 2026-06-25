package com.indusjs.fleet.core.util

/**
 * Centralized validation utilities for date and time fields.
 * Ensures consistent validation across all APIs.
 *
 * Date Format: DD-MM-YYYY
 * Time Format: HH:MM (24-hour)
 */
object ValidationUtils {

    // ============ DATE VALIDATION ============

    /**
     * Validates a date string in DD-MM-YYYY format.
     *
     * @param date The date string to validate
     * @return ValidationResult with success status and error message if invalid
     */
    fun validateDate(date: String): ValidationResult {
        if (date.isBlank()) {
            return ValidationResult.Error("Date is required")
        }

        // Check format: DD-MM-YYYY
        val regex = Regex("""^(\d{2})-(\d{2})-(\d{4})$""")
        val match = regex.matchEntire(date)
            ?: return ValidationResult.Error("Date must be in DD-MM-YYYY format")

        val day = match.groupValues[1].toIntOrNull() ?: return ValidationResult.Error("Invalid day")
        val month = match.groupValues[2].toIntOrNull() ?: return ValidationResult.Error("Invalid month")
        val year = match.groupValues[3].toIntOrNull() ?: return ValidationResult.Error("Invalid year")

        // Validate ranges
        if (month < 1 || month > 12) {
            return ValidationResult.Error("Month must be between 01 and 12")
        }

        if (year < 1900 || year > 2100) {
            return ValidationResult.Error("Year must be between 1900 and 2100")
        }

        val maxDays = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 31
        }

        if (day < 1 || day > maxDays) {
            return ValidationResult.Error("Day must be between 01 and $maxDays for month $month")
        }

        return ValidationResult.Success
    }

    /**
     * Checks if a date string is in valid DD-MM-YYYY format.
     */
    fun isValidDate(date: String): Boolean = validateDate(date) is ValidationResult.Success

    /**
     * Returns error message for invalid date, or null if valid.
     */
    fun getDateError(date: String): String? {
        val result = validateDate(date)
        return if (result is ValidationResult.Error) result.message else null
    }

    // ============ TIME VALIDATION ============

    /**
     * Validates a time string in HH:MM 24-hour format.
     *
     * @param time The time string to validate
     * @param required Whether the time field is required (default false)
     * @return ValidationResult with success status and error message if invalid
     */
    fun validateTime(time: String, required: Boolean = false): ValidationResult {
        if (time.isBlank()) {
            return if (required) {
                ValidationResult.Error("Time is required")
            } else {
                ValidationResult.Success // Optional field, empty is OK
            }
        }

        // Check format: HH:MM
        val regex = Regex("""^(\d{2}):(\d{2})$""")
        val match = regex.matchEntire(time)
            ?: return ValidationResult.Error("Time must be in HH:MM format (24-hour)")

        val hour = match.groupValues[1].toIntOrNull() ?: return ValidationResult.Error("Invalid hour")
        val minute = match.groupValues[2].toIntOrNull() ?: return ValidationResult.Error("Invalid minute")

        if (hour < 0 || hour > 23) {
            return ValidationResult.Error("Hour must be between 00 and 23")
        }

        if (minute < 0 || minute > 59) {
            return ValidationResult.Error("Minute must be between 00 and 59")
        }

        return ValidationResult.Success
    }

    /**
     * Checks if a time string is in valid HH:MM 24-hour format.
     */
    fun isValidTime(time: String): Boolean = validateTime(time) is ValidationResult.Success

    /**
     * Returns error message for invalid time, or null if valid.
     */
    fun getTimeError(time: String, required: Boolean = false): String? {
        val result = validateTime(time, required)
        return if (result is ValidationResult.Error) result.message else null
    }

    // ============ AMOUNT VALIDATION ============

    /**
     * Validates an amount string.
     *
     * @param amount The amount string to validate
     * @param required Whether the amount is required (default true)
     * @param minValue Minimum allowed value (default 0.0, exclusive)
     * @return ValidationResult with success status and error message if invalid
     */
    fun validateAmount(
        amount: String,
        required: Boolean = true,
        minValue: Double = 0.0
    ): ValidationResult {
        if (amount.isBlank()) {
            return if (required) {
                ValidationResult.Error("Amount is required")
            } else {
                ValidationResult.Success
            }
        }

        val value = amount.toDoubleOrNull()
            ?: return ValidationResult.Error("Invalid amount format")

        if (value <= minValue) {
            return ValidationResult.Error("Amount must be greater than $minValue")
        }

        return ValidationResult.Success
    }

    /**
     * Returns error message for invalid amount, or null if valid.
     */
    fun getAmountError(amount: String, required: Boolean = true): String? {
        val result = validateAmount(amount, required)
        return if (result is ValidationResult.Error) result.message else null
    }

    // ============ COST TYPE VALIDATION ============

    /**
     * Validates a cost type selection.
     *
     * @param costType The cost type to validate
     * @param validTypes Optional list of valid cost types
     * @return ValidationResult with success status and error message if invalid
     */
    fun validateCostType(costType: String, validTypes: List<String>? = null): ValidationResult {
        if (costType.isBlank()) {
            return ValidationResult.Error("Please select a cost type")
        }

        if (validTypes != null && costType !in validTypes) {
            return ValidationResult.Error("Invalid cost type: $costType")
        }

        return ValidationResult.Success
    }

    /**
     * Returns error message for invalid cost type, or null if valid.
     */
    fun getCostTypeError(costType: String): String? {
        val result = validateCostType(costType)
        return if (result is ValidationResult.Error) result.message else null
    }

    // ============ VEHICLE/SELECTION VALIDATION ============

    /**
     * Validates a required selection (vehicle, trip, etc.)
     */
    fun validateRequiredSelection(value: Any?, fieldName: String): ValidationResult {
        return if (value == null) {
            ValidationResult.Error("Please select a $fieldName")
        } else {
            ValidationResult.Success
        }
    }

    // ============ HELPER FUNCTIONS ============

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    /**
     * Formats date digits (e.g., "25122024") to DD-MM-YYYY format.
     * Returns the formatted string or original if cannot format.
     */
    fun formatDateFromDigits(digits: String): String {
        val cleanDigits = digits.filter { it.isDigit() }.take(8)
        if (cleanDigits.length != 8) return digits

        return "${cleanDigits.substring(0, 2)}-${cleanDigits.substring(2, 4)}-${cleanDigits.substring(4, 8)}"
    }

    /**
     * Formats time digits (e.g., "1430") to HH:MM format.
     * Returns the formatted string or original if cannot format.
     */
    fun formatTimeFromDigits(digits: String): String {
        val cleanDigits = digits.filter { it.isDigit() }.take(4)
        if (cleanDigits.length != 4) return digits

        return "${cleanDigits.substring(0, 2)}:${cleanDigits.substring(2, 4)}"
    }

    /**
     * Validates all fields for a cost entry.
     * Returns the first error found, or null if all valid.
     */
    fun validateCostEntry(
        costType: String,
        date: String,
        time: String,
        amount: String,
        timeRequired: Boolean = false
    ): String? {
        getCostTypeError(costType)?.let { return it }
        getDateError(date)?.let { return it }
        getTimeError(time, timeRequired)?.let { return it }
        getAmountError(amount)?.let { return it }
        return null
    }

    // ============ EMAIL VALIDATION ============

    /**
     * Validates an email address.
     *
     * @param email The email string to validate
     * @return true if the email is valid
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }

    /**
     * Returns error message for invalid email, or null if valid.
     */
    fun getEmailError(email: String, required: Boolean = false): String? {
        return when {
            email.isBlank() && required -> "Email is required"
            email.isNotBlank() && !isValidEmail(email) -> "Invalid email address"
            else -> null
        }
    }

    // ============ MOBILE VALIDATION ============

    /**
     * Validates a mobile number (10 digits).
     *
     * @param mobile The mobile number to validate
     * @return true if the mobile is valid
     */
    fun isValidMobile(mobile: String): Boolean = isValidIndianMobile(mobile)

    /**
     * Canonical mobile rule for the app: exactly 10 digits, starting 6-9 (Indian mobile).
     * Non-digit characters (spaces, etc.) are ignored before checking.
     */
    fun isValidIndianMobile(mobile: String): Boolean {
        val digits = mobile.filter { it.isDigit() }
        return digits.length == 10 && digits.first() in '6'..'9'
    }

    /**
     * Returns error message for invalid mobile, or null if valid.
     */
    fun getMobileError(mobile: String, required: Boolean = true): String? {
        return when {
            mobile.isBlank() && required -> "Mobile number is required"
            mobile.isNotBlank() && !isValidMobile(mobile) -> "Invalid mobile number"
            else -> null
        }
    }

    // ============ NAME VALIDATION ============

    /**
     * Canonical name rule for the app: 2–50 chars after trimming, made up of letters (any
     * script — so Hindi/Latin names pass), spaces, and the joiners . ' - (e.g. "S. R. Kumar",
     * "D'Souza", "Al-Hassan"). Digits and other symbols are rejected.
     */
    fun isValidName(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.length < 2 || trimmed.length > 50) return false
        return trimmed.all { it.isLetter() || it == ' ' || it == '.' || it == '\'' || it == '-' }
    }

    /**
     * Returns an error message for an invalid name, or null if valid.
     * [fieldName] is interpolated so callers can say "First name", "Customer name", etc.
     */
    fun getNameError(name: String, fieldName: String = "Name", required: Boolean = true): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isBlank() && required -> "$fieldName is required"
            trimmed.isBlank() -> null
            trimmed.length < 2 -> "$fieldName is too short"
            !isValidName(trimmed) -> "Enter a valid $fieldName"
            else -> null
        }
    }
}

/**
 * Sealed class representing validation result.
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()

    val isValid: Boolean get() = this is Success
    val errorMessage: String? get() = (this as? Error)?.message
}

