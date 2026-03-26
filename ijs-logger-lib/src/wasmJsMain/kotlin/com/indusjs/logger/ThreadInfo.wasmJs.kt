package com.indusjs.logger

/** WasmJS is single-threaded. */
internal actual fun currentThreadId(): Long = 1L
internal actual fun currentThreadName(): String = "main"

