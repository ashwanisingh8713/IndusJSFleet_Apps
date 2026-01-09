package com.indusjs.error.result

/**
 * A sealed class representing the result of an operation.
 * Used throughout the architecture to handle success, error, and loading states.
 *
 * Usage:
 * ```kotlin
 * when (val result = repository.getData()) {
 *     is Result.Success -> handleSuccess(result.data)
 *     is Result.Error -> handleError(result.message)
 *     is Result.Loading -> showLoading()
 * }
 * ```
 */
sealed class Result<out T> {

    /**
     * Represents a successful result containing data.
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents a failed result containing an error.
     */
    data class Error(
        val exception: Throwable,
        val message: String? = null
    ) : Result<Nothing>() {
        /**
         * Get the error message, falling back to exception message
         */
        val errorMessage: String
            get() = message ?: exception.message ?: "Unknown error"
    }

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
     * Returns the data if successful, or throws the exception.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is still loading")
    }

    /**
     * Returns the error if this is an Error result, null otherwise.
     */
    fun exceptionOrNull(): Throwable? = when (this) {
        is Error -> exception
        else -> null
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
     * Transforms the error if this is an Error result.
     */
    inline fun mapError(transform: (Throwable) -> Throwable): Result<T> = when (this) {
        is Success -> this
        is Error -> Error(transform(exception), message)
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

    /**
     * Fold the result into a single value.
     */
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onError: (Throwable, String?) -> R,
        onLoading: () -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onError(exception, message)
        is Loading -> onLoading()
    }

    /**
     * Fold the result into a single value (without loading state).
     */
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onError: (Throwable, String?) -> R
    ): R? = when (this) {
        is Success -> onSuccess(data)
        is Error -> onError(exception, message)
        is Loading -> null
    }

    companion object {
        /**
         * Create a success result.
         */
        fun <T> success(data: T): Result<T> = Success(data)

        /**
         * Create an error result.
         */
        fun error(
            exception: Throwable,
            message: String? = null
        ): Result<Nothing> = Error(exception, message)

        /**
         * Create an error result from message only.
         */
        fun error(message: String): Result<Nothing> = Error(
            exception = Exception(message),
            message = message
        )

        /**
         * Create a loading result.
         */
        fun <T> loading(): Result<T> = Loading

        /**
         * Execute a block and wrap the result.
         */
        inline fun <T> runCatching(block: () -> T): Result<T> {
            return try {
                Success(block())
            } catch (e: Throwable) {
                Error(e, e.message)
            }
        }

        /**
         * Execute a suspending block and wrap the result.
         */
        suspend inline fun <T> runCatchingSuspend(block: suspend () -> T): Result<T> {
            return try {
                Success(block())
            } catch (e: Throwable) {
                Error(e, e.message)
            }
        }
    }
}

// ==================== Extension Functions ====================

/**
 * Convert a nullable value to a Result.
 */
fun <T> T?.toResult(errorMessage: String = "Value is null"): Result<T> {
    return if (this != null) {
        Result.Success(this)
    } else {
        Result.Error(NullPointerException(errorMessage), errorMessage)
    }
}

/**
 * Combine two results into a pair.
 */
fun <A, B> Result<A>.combine(other: Result<B>): Result<Pair<A, B>> {
    return when {
        this is Result.Success && other is Result.Success ->
            Result.Success(this.data to other.data)
        this is Result.Error -> this
        other is Result.Error -> other
        else -> Result.Loading
    }
}

/**
 * Convert a list of results to a result of list.
 */
fun <T> List<Result<T>>.sequence(): Result<List<T>> {
    val results = mutableListOf<T>()
    for (result in this) {
        when (result) {
            is Result.Success -> results.add(result.data)
            is Result.Error -> return result
            is Result.Loading -> return Result.Loading
        }
    }
    return Result.Success(results)
}

