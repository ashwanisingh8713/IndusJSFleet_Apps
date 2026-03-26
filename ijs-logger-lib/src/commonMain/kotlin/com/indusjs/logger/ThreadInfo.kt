package com.indusjs.logger

/**
 * Resolves current thread ID and name per platform.
 *
 * | Platform | Thread ID Source                          | Thread Name Source              |
 * |----------|-------------------------------------------|---------------------------------|
 * | Android  | `android.os.Process.myTid()`              | `Thread.currentThread().name`   |
 * | iOS      | `pthread_mach_thread_np(pthread_self())`   | `NSThread.currentThread.name`   |
 * | Web      | Fixed `1` (single-threaded)                | `"main"`                        |
 */
internal expect fun currentThreadId(): Long
internal expect fun currentThreadName(): String

