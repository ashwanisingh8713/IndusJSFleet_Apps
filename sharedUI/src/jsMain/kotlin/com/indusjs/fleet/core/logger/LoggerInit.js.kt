package com.indusjs.fleet.core.logger

import com.indusjs.logger.IjsLogger
import com.indusjs.logger.PlatformContext

/**
 * JS: Initialize logger with marker PlatformContext.
 * Called from App() composable on first composition.
 */
actual fun initPlatformLogger() {
    IjsLogger.init(PlatformContext())
}

