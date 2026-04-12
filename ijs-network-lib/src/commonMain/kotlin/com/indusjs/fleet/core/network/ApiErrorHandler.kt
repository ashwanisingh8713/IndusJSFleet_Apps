package com.indusjs.fleet.core.network

import com.indusjs.fleet.core.debug.postDebugLog9fbb5d
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

            // #region agent log — Hypothesis A: v1 error format uses 'errorMessage' not 'message'
            val keys = jsonObject.keys.take(8).joinToString(",")
            postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"A","location":"ApiErrorHandler:tryExtractJsonMessage","message":"error_body_keys","data":{"keys":"$keys","body_preview":"${body.take(120).replace("\"","'")}"},"timestamp":0}""")
            // #endregion

            // Try to get message field first (most common)
            val messageField = jsonObject["message"]?.jsonPrimitive?.contentOrNull
            if (!messageField.isNullOrBlank() && messageField != "null") {
                // #region agent log
                postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"A","location":"ApiErrorHandler:tryExtractJsonMessage","message":"found_message_field","data":{"value":"${messageField.take(80)}"},"timestamp":0}""")
                // #endregion
                return messageField
            }

            // Try new v1 'errorMessage' field
            val errorMessageField = jsonObject["errorMessage"]?.jsonPrimitive?.contentOrNull
            if (!errorMessageField.isNullOrBlank() && errorMessageField != "null") {
                // #region agent log
                postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"A","location":"ApiErrorHandler:tryExtractJsonMessage","message":"found_errorMessage_field","data":{"value":"${errorMessageField.take(80)}"},"timestamp":0}""")
                // #endregion
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
                return detailField
            }

            // #region agent log
            postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"A","location":"ApiErrorHandler:tryExtractJsonMessage","message":"no_known_field_found_fallback_to_http_status","data":{"keys":"$keys"},"timestamp":0}""")
            // #endregion
            null
        } catch (e: Exception) {
            null
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
            // Email duplicates
            error.contains("duplicate key", ignoreCase = true) && error.contains("email", ignoreCase = true) ->
                "An account with this email already exists"

            // Mobile duplicates
            error.contains("duplicate key", ignoreCase = true) && error.contains("mobile", ignoreCase = true) ->
                "An account with this mobile number already exists"

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

