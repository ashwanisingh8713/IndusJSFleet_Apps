package com.indusjs.fleet.core.network

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Centralized API error handler for extracting user-friendly error messages.
 *
 * This utility provides consistent error message extraction across all data sources,
 * prioritizing server-provided messages over generic HTTP status messages.
 */
object ApiErrorHandler {

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    // Friendly message for the delete-guard 409 (DELETE /vehicles/:id or
    // /drivers/:id when the entity is on a planned/in-progress trip). Backend
    // envelope key: "vehicle_has_active_trips" / "driver_has_active_trips".
    // ApiErrorHandler returns a raw String (non-composable), so this is a
    // hardcoded EN line; see needsString for the EN/HI to localise later.
    private const val ACTIVE_TRIP_DELETE_GUARD_MESSAGE =
        "Can't delete — finish or cancel the active trip first"

    /**
     * Extracts a user-friendly error message from an API response.
     *
     * Priority order:
     * 1. Server's "message" field from JSON response
     * 2. Server's "error" field from JSON response (parsed for DB constraints)
     * 3. Server's "detail" field from JSON response
     * 4. HTTP status code-based fallback message
     *
     * @param statusCode The HTTP status code of the response
     * @param responseBody The raw response body as string
     * @return A user-friendly error message
     */
    fun extractErrorMessage(statusCode: HttpStatusCode, responseBody: String): String {
        // First, try to extract message from JSON response body
        val jsonMessage = tryExtractJsonMessage(responseBody)
        if (jsonMessage != null) {
            return jsonMessage
        }

        // Fallback to HTTP status code-based message
        return getHttpStatusMessage(statusCode)
    }

    /**
     * Extracts error message from exception, attempting to parse JSON if present.
     *
     * @param exception The exception to extract message from
     * @return A user-friendly error message
     */
    fun extractErrorMessage(exception: Exception): String {
        val message = exception.message ?: return "An unexpected error occurred"

        // Check if exception message contains JSON
        val jsonStart = message.indexOf('{')
        val jsonEnd = message.lastIndexOf('}')

        if (jsonStart != -1 && jsonEnd > jsonStart) {
            val jsonString = message.substring(jsonStart, jsonEnd + 1)
            val jsonMessage = tryExtractJsonMessage(jsonString)
            if (jsonMessage != null) {
                return jsonMessage
            }
        }

        // Parse common error patterns from exception message
        return parseExceptionMessage(message)
    }

    /**
     * Tries to extract error message from JSON response body.
     * Checks 'message', 'error', and 'detail' fields.
     *
     * @param body The JSON response body
     * @return Extracted message or null if parsing fails
     */
    private fun tryExtractJsonMessage(body: String): String? {
        if (body.isBlank()) return null

        return try {
            val jsonElement = json.parseToJsonElement(body)
            val jsonObject = jsonElement as? JsonObject ?: return null

            // Try to get message field first (most common)
            val messageField = jsonObject["message"]?.jsonPrimitive?.contentOrNull
            if (!messageField.isNullOrBlank() && messageField != "null") {
                return messageField
            }

            // The v1 'errorMessage' field carries the backend i18n key (e.g.
            // "vehicle_has_active_trips" / "driver_has_active_trips" for the
            // delete-guard 409). Map known keys before the generic handling
            // below, which otherwise skips underscore codes and would surface
            // the raw developerMessage sentence instead.
            val errorMessageKey = jsonObject["errorMessage"]?.jsonPrimitive?.contentOrNull
            if (!errorMessageKey.isNullOrBlank() && errorMessageKey != "null"
                && errorMessageKey.contains("has_active_trip", ignoreCase = true)) {
                return ACTIVE_TRIP_DELETE_GUARD_MESSAGE
            }

            // Duplicate-account conflict (e.g. signup with an already-used mobile or
            // email). The IAM/Fleet envelope carries the i18n key in `errorMessage`;
            // some keys (notably `mobile_already_exists`) have no backend translation
            // and arrive as the raw underscored key, which the generic handling below
            // skips — leaving the unhelpful "This record already exists" fallback.
            // Map the known duplicate keys (and their translated sentences) here.
            val duplicateMessage = matchDuplicateConflict(errorMessageKey)
            if (duplicateMessage != null) {
                return duplicateMessage
            }

            // Try 'developerMessage' which carries the actual IAM/backend error detail
            val developerMessage = jsonObject["developerMessage"]?.jsonPrimitive?.contentOrNull
            if (!developerMessage.isNullOrBlank() && developerMessage != "null") {
                return parseDbConstraintError(developerMessage)
            }

            // Try new v1 'errorMessage' field (skip if it looks like a code e.g. "internal_error")
            val errorMessageField = jsonObject["errorMessage"]?.jsonPrimitive?.contentOrNull
            if (!errorMessageField.isNullOrBlank() && errorMessageField != "null"
                && !errorMessageField.contains('_')) {
                return errorMessageField
            }

            // Try error field (may contain DB constraint errors)
            val errorField = jsonObject["error"]?.jsonPrimitive?.contentOrNull
            if (!errorField.isNullOrBlank() && errorField != "null") {
                return parseDbConstraintError(errorField)
            }

            // Try detail field (used by some APIs)
            val detailField = jsonObject["detail"]?.jsonPrimitive?.contentOrNull
            if (!detailField.isNullOrBlank() && detailField != "null") {
                return parseDbConstraintError(detailField)
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Recognizes a duplicate-account conflict from an IAM/Fleet `errorMessage`
     * value — either the raw i18n key (e.g. `mobile_already_exists`,
     * `user_already_exists`) or its translated sentence ("user with this email
     * already exists"). Returns a clear, user-facing message, or null when the
     * value is not a duplicate-account conflict.
     */
    private fun matchDuplicateConflict(value: String?): String? {
        if (value.isNullOrBlank() || value == "null") return null
        val v = value.lowercase()
        val isDuplicate = v.contains("exist") || v.contains("already") || v.contains("registered")
        if (!isDuplicate) return null
        return when {
            v.contains("mobile") || v.contains("phone") ->
                "This mobile number is already registered. Please log in or use a different number."
            v.contains("email") || v.contains("user") ->
                "This email is already registered. Please log in or use a different email."
            else -> null
        }
    }

    /**
     * Parses database constraint error messages into user-friendly messages.
     *
     * @param error The raw database error message
     * @return A user-friendly error message
     */
    fun parseDbConstraintError(error: String): String {
        return when {
            // Delete-guard 409: vehicle/driver is on a planned/in-progress trip.
            // Matches the backend i18n key ("vehicle_has_active_trips" /
            // "driver_has_active_trips") or its developerMessage sentence
            // ("cannot delete ... while it has active or planned trips").
            error.contains("has_active_trip", ignoreCase = true) ||
            (error.contains("cannot delete", ignoreCase = true) &&
                error.contains("active or planned trip", ignoreCase = true)) ->
                ACTIVE_TRIP_DELETE_GUARD_MESSAGE

            // Email duplicates
            error.contains("duplicate key", ignoreCase = true) && error.contains("email", ignoreCase = true) ->
                "An account with this email already exists"

            // Mobile duplicates
            error.contains("duplicate key", ignoreCase = true) && error.contains("mobile", ignoreCase = true) ->
                "A record with this mobile number already exists. Please use a different mobile number."

            // Driver IAM role setup missing in backend/IAM seed data
            error.contains("role not found in tenant", ignoreCase = true) ->
                "Driver role is not configured for this organization. Please contact support or backend team."

            // License duplicates (for drivers)
            error.contains("duplicate key", ignoreCase = true) && error.contains("license", ignoreCase = true) ->
                "A driver with this license number already exists"

            // Registration number duplicates (for vehicles)
            error.contains("duplicate key", ignoreCase = true) && error.contains("registration", ignoreCase = true) ->
                "A vehicle with this registration number already exists"

            // Generic duplicate
            error.contains("duplicate key", ignoreCase = true) ->
                "This record already exists"

            // Foreign key constraint
            error.contains("foreign key", ignoreCase = true) ->
                "Cannot complete this operation due to related records"

            // Not null constraint
            error.contains("not null", ignoreCase = true) ->
                "Required field is missing"

            // Return original error if no pattern matches
            else -> error
        }
    }

    /**
     * Returns a user-friendly message for common HTTP status codes.
     *
     * @param statusCode The HTTP status code
     * @return A user-friendly error message
     */
    fun getHttpStatusMessage(statusCode: HttpStatusCode): String {
        return when (statusCode) {
            HttpStatusCode.BadRequest -> "Invalid request. Please check your input."
            HttpStatusCode.Unauthorized -> "Session expired. Please log in again."
            HttpStatusCode.Forbidden -> "You don't have permission to perform this action."
            HttpStatusCode.NotFound -> "The requested resource was not found."
            HttpStatusCode.Conflict -> "This record already exists."
            HttpStatusCode.UnprocessableEntity -> "Invalid data provided. Please check your input."
            HttpStatusCode.TooManyRequests -> "Too many requests. Please try again later."
            HttpStatusCode.InternalServerError -> "Server error occurred. Please try again later."
            HttpStatusCode.BadGateway -> "Service temporarily unavailable. Please try again later."
            HttpStatusCode.ServiceUnavailable -> "Service is currently unavailable. Please try again later."
            HttpStatusCode.GatewayTimeout -> "Request timed out. Please try again."
            else -> "Request failed. Please try again."
        }
    }

    /**
     * Parses common patterns from exception messages.
     *
     * @param message The exception message
     * @return A user-friendly error message
     */
    private fun parseExceptionMessage(message: String): String {
        return when {
            // Network connectivity issues
            message.contains("Unable to resolve host", ignoreCase = true) ||
            message.contains("No address associated", ignoreCase = true) ||
            message.contains("UnknownHostException", ignoreCase = true) ->
                "No internet connection. Please check your network."

            // Connection failures (including raw IP/port in message)
            message.contains("Failed to connect", ignoreCase = true) ||
            message.contains("Connection refused", ignoreCase = true) ||
            message.contains("ECONNREFUSED", ignoreCase = true) ||
            containsIpAddressPattern(message) ->
                "Unable to connect to server. Please check your connection and try again."

            // Timeout issues
            message.contains("timeout", ignoreCase = true) ||
            message.contains("timed out", ignoreCase = true) ||
            message.contains("SocketTimeoutException", ignoreCase = true) ->
                "Connection timed out. Please try again."

            // SSL/Security issues
            message.contains("SSL", ignoreCase = true) ||
            message.contains("certificate", ignoreCase = true) ||
            message.contains("handshake", ignoreCase = true) ->
                "Secure connection failed. Please try again."

            // General socket/connection exceptions
            message.contains("SocketException", ignoreCase = true) ||
            message.contains("ConnectException", ignoreCase = true) ||
            message.contains("IOException", ignoreCase = true) ||
            message.contains("Network is unreachable", ignoreCase = true) ||
            message.contains("Connection reset", ignoreCase = true) ->
                "Network error occurred. Please check your connection."

            // HTTP status codes in message
            message.contains("401") -> "Session expired. Please log in again."
            message.contains("403") -> "You don't have permission to perform this action."
            message.contains("404") -> "The requested resource was not found."
            message.contains("409") -> "This record already exists."
            message.contains("422") -> "Invalid data provided. Please check your input."
            message.contains("500") -> "Server error occurred. Please try again later."
            message.contains("502") -> "Service temporarily unavailable. Please try again later."
            message.contains("503") -> "Service is currently unavailable. Please try again later."
            message.contains("504") -> "Request timed out. Please try again."

            else -> "Something went wrong. Please try again."
        }
    }

    /**
     * Checks if the message contains an IP address pattern (e.g., /192.168.1.7:8080).
     * These are technical details that shouldn't be shown to users.
     */
    private fun containsIpAddressPattern(message: String): Boolean {
        // Match patterns like /192.168.1.7:8080 or 192.168.1.7:8080
        val ipPattern = Regex("""/?(\d{1,3}\.){3}\d{1,3}(:\d+)?""")
        return ipPattern.containsMatchIn(message)
    }

    /**
     * Creates a network error message for catch blocks.
     * Use this when catching exceptions in data sources.
     *
     * @param exception The caught exception
     * @param fallbackMessage Optional fallback message
     * @return A user-friendly error message
     */
    fun getNetworkErrorMessage(exception: Exception, fallbackMessage: String = "Network error occurred"): String {
        return extractErrorMessage(exception).takeIf { it != "Something went wrong. Please try again." }
            ?: fallbackMessage
    }
}

