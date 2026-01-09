package com.indusjs.dispatcher

/**
 * Create platform-specific dispatcher provider for JS.
 */
actual fun createPlatformDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
