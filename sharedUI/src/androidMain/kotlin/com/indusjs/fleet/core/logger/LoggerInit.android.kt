package com.indusjs.fleet.core.logger

/**
 * Android: Logger is initialized in FleetApplication.onCreate() with applicationContext.
 * This is a no-op to satisfy the expect/actual contract.
 */
actual fun initPlatformLogger() {
    // No-op — initialized in FleetApplication.onCreate() with applicationContext
}

