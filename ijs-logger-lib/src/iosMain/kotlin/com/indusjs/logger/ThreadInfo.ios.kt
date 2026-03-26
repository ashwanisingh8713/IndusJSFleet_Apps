package com.indusjs.logger

import platform.Foundation.NSThread

/**
 * iOS thread info.
 * Uses NSThread hash as a numeric thread identifier proxy
 * (pthread_mach_thread_np is not exposed in Kotlin/Native iOS headers).
 */
internal actual fun currentThreadId(): Long =
    NSThread.currentThread.hash.toLong()

internal actual fun currentThreadName(): String =
    NSThread.currentThread.name?.ifEmpty { "unknown" } ?: "unknown"

