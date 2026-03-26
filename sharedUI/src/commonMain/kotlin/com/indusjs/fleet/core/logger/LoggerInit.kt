package com.indusjs.fleet.core.logger

/**
 * Platform-specific logger initialization.
 *
 * - **Android:** no-op (initialized in FleetApplication.onCreate with Context)
 * - **iOS:** no-op (initialized in MainViewController)
 * - **JS/WasmJS:** initializes IjsLogger with marker PlatformContext
 *
 * Called once from [com.indusjs.fleet.App] composable.
 * Idempotent — multiple calls are safe (IjsLogger.init guards against re-init).
 */
expect fun initPlatformLogger()

