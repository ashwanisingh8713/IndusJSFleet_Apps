package com.indusjs.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Interface for providing coroutine dispatchers.
 * This abstraction allows for easy testing by swapping implementations.
 *
 * Usage:
 * ```kotlin
 * class MyRepository(
 *     private val dispatchers: DispatcherProvider
 * ) {
 *     suspend fun fetchData() = withContext(dispatchers.io) {
 *         // IO operation
 *     }
 * }
 * ```
 */
interface DispatcherProvider {
    /**
     * Main/UI dispatcher for UI updates.
     * Use for updating UI elements and state.
     */
    val main: CoroutineDispatcher

    /**
     * IO dispatcher for I/O operations.
     * Use for network calls, database operations, file I/O.
     */
    val io: CoroutineDispatcher

    /**
     * Default dispatcher for CPU-intensive work.
     * Use for heavy computations, sorting, parsing.
     */
    val default: CoroutineDispatcher

    /**
     * Unconfined dispatcher for immediate execution.
     * Use sparingly, mainly for testing.
     */
    val unconfined: CoroutineDispatcher
}

