package com.indusjs.fleet.core.util

/**
 * WasmJS implementation of currentTimeMillis using external JS interop.
 */
@JsFun("() => Date.now()")
external fun dateNow(): Double

actual fun currentTimeMillis(): Long = dateNow().toLong()

