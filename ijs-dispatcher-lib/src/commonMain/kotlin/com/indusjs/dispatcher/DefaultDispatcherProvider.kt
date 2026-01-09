package com.indusjs.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Default implementation of [DispatcherProvider] using standard Dispatchers.
 *
 * Note: In KMP, Dispatchers.IO is not available on all platforms.
 * This implementation uses Dispatchers.Default for IO on non-Android platforms.
 * Use platform-specific implementations for true IO dispatcher support.
 */
class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.Default // KMP doesn't have Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}

/**
 * Test implementation of [DispatcherProvider] that uses Unconfined dispatcher.
 * Use this in unit tests for synchronous execution.
 */
class TestDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Unconfined
    override val io: CoroutineDispatcher = Dispatchers.Unconfined
    override val default: CoroutineDispatcher = Dispatchers.Unconfined
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}

/**
 * Immediate dispatcher provider that uses Unconfined for all dispatchers.
 * Useful for testing and immediate execution scenarios.
 */
class ImmediateDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Unconfined
    override val io: CoroutineDispatcher = Dispatchers.Unconfined
    override val default: CoroutineDispatcher = Dispatchers.Unconfined
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}

