package com.indusjs.dispatcher

/**
 * Create platform-specific dispatcher provider for WasmJS.
 */
actual fun createPlatformDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

