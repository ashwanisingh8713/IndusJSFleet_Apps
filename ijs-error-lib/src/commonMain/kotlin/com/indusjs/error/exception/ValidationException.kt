package com.indusjs.error.exception

/**
 * Exception thrown when input validation fails.
 *
 * @param message Human-readable error message
 * @param field The field that failed validation (optional)
 * @param validationErrors Map of field names to error messages for multiple validation errors
 * @param cause The underlying exception
 */
class ValidationException(
    message: String,
    val field: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
    cause: Throwable? = null
) : IjsException(message, cause) {

    override val errorCode: String = "VALIDATION"

    override val isRecoverable: Boolean = true

    /**
     * Check if there are multiple validation errors
     */
    val hasMultipleErrors: Boolean
        get() = validationErrors.size > 1

    /**
     * Get error message for a specific field
     */
    fun getErrorForField(fieldName: String): String? {
        return validationErrors[fieldName]
    }

    /**
     * Get all field names with errors
     */
    val errorFields: Set<String>
        get() = validationErrors.keys

    companion object {
        /**
         * Create a single field validation error
         */
        fun forField(
            field: String,
            message: String
        ) = ValidationException(
            message = message,
            field = field,
            validationErrors = mapOf(field to message)
        )

        /**
         * Create a required field error
         */
        fun required(
            field: String,
            fieldLabel: String = field
        ) = forField(field, "$fieldLabel is required")

        /**
         * Create an invalid format error
         */
        fun invalidFormat(
            field: String,
            fieldLabel: String = field,
            expectedFormat: String? = null
        ): ValidationException {
            val message = if (expectedFormat != null) {
                "$fieldLabel format is invalid. Expected: $expectedFormat"
            } else {
                "$fieldLabel format is invalid"
            }
            return forField(field, message)
        }

        /**
         * Create a multiple fields validation error
         */
        fun multiple(
            errors: Map<String, String>,
            message: String = "Please correct the errors and try again"
        ) = ValidationException(
            message = message,
            validationErrors = errors
        )

        /**
         * Create a min length error
         */
        fun minLength(
            field: String,
            fieldLabel: String = field,
            minLength: Int
        ) = forField(field, "$fieldLabel must be at least $minLength characters")

        /**
         * Create a max length error
         */
        fun maxLength(
            field: String,
            fieldLabel: String = field,
            maxLength: Int
        ) = forField(field, "$fieldLabel must be at most $maxLength characters")

        /**
         * Create a range error
         */
        fun range(
            field: String,
            fieldLabel: String = field,
            min: Number,
            max: Number
        ) = forField(field, "$fieldLabel must be between $min and $max")
    }
}

