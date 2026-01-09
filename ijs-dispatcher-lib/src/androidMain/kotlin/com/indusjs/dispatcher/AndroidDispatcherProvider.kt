package com.indusjs.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Android-specific implementation of [DispatcherProvider].
 * Uses Dispatchers.IO for IO operations on Android.
 */
class AndroidDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
    override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}

/**
 * Create platform-specific dispatcher provider.
 * On Android, this returns AndroidDispatcherProvider with Dispatchers.IO support.
 */
actual fun createPlatformDispatcherProvider(): DispatcherProvider = AndroidDispatcherProvider()

