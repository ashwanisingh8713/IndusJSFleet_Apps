package com.indusjs.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * iOS-specific implementation of [DispatcherProvider].
 * Uses Dispatchers.Default for IO operations as iOS doesn't have Dispatchers.IO.
 */
class IosDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.Default // iOS doesn't have IO dispatcher
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}

/**
 * Create platform-specific dispatcher provider.
 * On iOS, this returns IosDispatcherProvider using Default for IO.
 */
actual fun createPlatformDispatcherProvider(): DispatcherProvider = IosDispatcherProvider()

