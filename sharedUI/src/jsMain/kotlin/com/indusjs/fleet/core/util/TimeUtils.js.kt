package com.indusjs.fleet.core.util

import kotlin.js.Date

/**
 * JS implementation of currentTimeMillis.
 */
actual fun currentTimeMillis(): Long = Date.now().toLong()

