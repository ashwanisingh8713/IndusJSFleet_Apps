package com.indusjs.fleet.core.result

/**
 * A sealed class representing the result of an operation.
 * Used throughout the architecture to handle success and error states.
 */
sealed class Result<out T> {

    /**
     * Represents a successful result containing data.
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents a failed result containing an error.
     */
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()

    /**
     * Represents a loading state.
     */
    data object Loading : Result<Nothing>()

    /**
     * Returns true if this result is successful.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns true if this result is an error.
     */
    val isError: Boolean get() = this is Error

    /**
     * Returns true if this result is loading.
     */
    val isLoading: Boolean get() = this is Loading

    /**
     * Returns the data if successful, null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Returns the data if successful, or the default value otherwise.
     */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> default
    }

    /**
     * Transforms the result data if successful.
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> Loading
    }

    /**
     * Transforms the result with a function that returns another Result.
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> Loading
    }

    /**
     * Executes the given block if this result is successful.
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Executes the given block if this result is an error.
     */
    inline fun onError(action: (Throwable, String?) -> Unit): Result<T> {
        if (this is Error) action(exception, message)
        return this
    }

    /**
     * Executes the given block if this result is loading.
     */
    inline fun onLoading(action: () -> Unit): Result<T> {
        if (this is Loading) action()
        return this
    }
}

/**
 * Gets a user-friendly display message for the error.
 * Uses ApiErrorHandler to convert technical error messages to user-friendly ones.
 *
 * @return A user-friendly error message suitable for displaying to users
 */
fun Result.Error.displayMessage(): String {
    // If message is already set and looks user-friendly (doesn't contain technical details), use it
    if (!message.isNullOrBlank() && !containsTechnicalDetails(message)) {
        return message
    }

    // Otherwise, use ApiErrorHandler to extract a friendly message
    return com.indusjs.fleet.core.network.ApiErrorHandler.extractErrorMessage(
        exception as? Exception ?: Exception(exception.message ?: "Unknown error")
    )
}

/**
 * Checks if a message contains technical details that shouldn't be shown to users.
 */
private fun containsTechnicalDetails(message: String): Boolean {
    // Check for IP addresses
    val ipPattern = Regex("""/?(\d{1,3}\.){3}\d{1,3}(:\d+)?""")
    if (ipPattern.containsMatchIn(message)) return true

    // Check for common technical patterns
    val technicalPatterns = listOf(
        "Exception",
        "failed to connect",
        "java.",
        "kotlin.",
        "io.ktor",
        "okhttp",
        "ECONNREFUSED",
        "errno",
        "stacktrace"
    )

    return technicalPatterns.any { message.contains(it, ignoreCase = true) }
}

/**
 * Wraps a suspending operation in a Result, catching any exceptions.
 * Uses ApiErrorHandler to convert technical errors to user-friendly messages.
 */
suspend inline fun <T> runCatching(block: () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: Throwable) {
    val friendlyMessage = if (e is Exception) {
        com.indusjs.fleet.core.network.ApiErrorHandler.extractErrorMessage(e)
    } else {
        e.message ?: "An unexpected error occurred"
    }
    Result.Error(e, friendlyMessage)
}

