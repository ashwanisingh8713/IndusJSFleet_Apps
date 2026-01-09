package com.indusjs.dispatcher

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlin.coroutines.CoroutineContext

/**
 * Provider for pre-configured CoroutineScopes.
 * Provides scopes with SupervisorJob for proper error handling.
 *
 * Usage:
 * ```kotlin
 * class MyClass {
 *     private val scope = CoroutineScopeProvider.createIoScope()
 *
 *     fun doWork() {
 *         scope.launch {
 *             // Work that won't cancel sibling coroutines on failure
 *         }
 *     }
 *
 *     fun cleanup() {
 *         scope.cancel()
 *     }
 * }
 * ```
 */
object CoroutineScopeProvider {

    /**
     * Create a scope for Main/UI operations.
     * Uses SupervisorJob so child failures don't cancel siblings.
     */
    fun createMainScope(
        exceptionHandler: CoroutineExceptionHandler? = null
    ): CoroutineScope {
        val context: CoroutineContext = SupervisorJob() + Dispatchers.Main
        return CoroutineScope(
            if (exceptionHandler != null) context + exceptionHandler else context
        )
    }

    /**
     * Create a scope for IO operations.
     * Uses SupervisorJob so child failures don't cancel siblings.
     */
    fun createIoScope(
        exceptionHandler: CoroutineExceptionHandler? = null
    ): CoroutineScope {
        val context: CoroutineContext = SupervisorJob() + Dispatchers.Default // Use Default for KMP compatibility
        return CoroutineScope(
            if (exceptionHandler != null) context + exceptionHandler else context
        )
    }

    /**
     * Create a scope for CPU-intensive operations.
     * Uses SupervisorJob so child failures don't cancel siblings.
     */
    fun createDefaultScope(
        exceptionHandler: CoroutineExceptionHandler? = null
    ): CoroutineScope {
        val context: CoroutineContext = SupervisorJob() + Dispatchers.Default
        return CoroutineScope(
            if (exceptionHandler != null) context + exceptionHandler else context
        )
    }

    /**
     * Create a scope with custom dispatcher.
     */
    fun createScope(
        dispatcher: kotlinx.coroutines.CoroutineDispatcher,
        exceptionHandler: CoroutineExceptionHandler? = null
    ): CoroutineScope {
        val context: CoroutineContext = SupervisorJob() + dispatcher
        return CoroutineScope(
            if (exceptionHandler != null) context + exceptionHandler else context
        )
    }

    /**
     * Create a default exception handler that logs errors.
     */
    fun createLoggingExceptionHandler(
        tag: String = "CoroutineException",
        onError: ((Throwable) -> Unit)? = null
    ): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            println("[$tag] Coroutine exception: ${throwable.message}")
            throwable.printStackTrace()
            onError?.invoke(throwable)
        }
    }
}

/**
 * A managed scope that can be cancelled and provides lifecycle awareness.
 */
class ManagedCoroutineScope(
    private val dispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.Default,
    private val exceptionHandler: CoroutineExceptionHandler? = null
) {
    private var _scope: CoroutineScope? = null

    /**
     * Get the current scope, creating one if needed.
     */
    val scope: CoroutineScope
        get() {
            if (_scope == null) {
                _scope = CoroutineScopeProvider.createScope(dispatcher, exceptionHandler)
            }
            return _scope!!
        }

    /**
     * Check if scope is active.
     */
    val isActive: Boolean
        get() = _scope?.coroutineContext?.isActive == true

    /**
     * Cancel the current scope.
     */
    fun cancel() {
        _scope?.cancel()
        _scope = null
    }

    /**
     * Cancel and recreate the scope.
     */
    fun reset() {
        cancel()
        _scope = CoroutineScopeProvider.createScope(dispatcher, exceptionHandler)
    }
}

