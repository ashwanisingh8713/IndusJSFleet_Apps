package com.indusjs.logger

import android.content.Context

/**
 * Android wrapper around [Context] for logger initialization.
 * Usage: `IjsLogger.init(PlatformContext(applicationContext))`
 */
actual class PlatformContext(val context: Context)

