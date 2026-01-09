package com.indusjs.dispatcher

/**
 * Create a platform-specific dispatcher provider.
 *
 * This is an expect declaration that will be implemented by each platform:
 * - Android: Returns AndroidDispatcherProvider with Dispatchers.IO
 * - iOS: Returns IosDispatcherProvider with Dispatchers.Default for IO
 * - JS/Wasm: Returns DefaultDispatcherProvider
 */
expect fun createPlatformDispatcherProvider(): DispatcherProvider

