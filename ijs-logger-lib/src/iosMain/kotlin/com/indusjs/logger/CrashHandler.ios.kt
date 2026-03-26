package com.indusjs.logger

import kotlin.time.Clock
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSException
import platform.Foundation.NSThread

/**
 * iOS crash handler: installs `NSSetUncaughtExceptionHandler` to write
 * the exception info to the log before the process terminates.
 *
 * Uses a global reference to [LogFileManager] because
 * `NSSetUncaughtExceptionHandler` requires a C function pointer
 * (staticCFunction) which cannot capture Kotlin state.
 *
 * Does NOT suppress existing crash reporters — Firebase Crashlytics on iOS
 * uses Mach exception + signal handlers (not NSSetUncaughtExceptionHandler),
 * so this is generally safe.
 */

/** Global reference for the static C function callback. */
private var globalFileManager: LogFileManager? = null

internal actual class CrashHandler actual constructor(
    private val fileManager: LogFileManager
) {
    @OptIn(ExperimentalForeignApi::class)
    actual fun install() {
        globalFileManager = fileManager
        platform.Foundation.NSSetUncaughtExceptionHandler(
            staticCFunction { exception: NSException? ->
                try {
                    val ts = crashTimestamp()
                    val tid = NSThread.currentThread.hash
                    val tname = NSThread.currentThread.name ?: "unknown"

                    val entry = buildString {
                        append("[$ts] [CRASH  ] [ThreadID:$tid | ThreadName:$tname] [UncaughtException]\n")
                        append("Name: ${exception?.name}\n")
                        append("Reason: ${exception?.reason}\n")
                        append("Stack:\n")
                        exception?.callStackSymbols?.forEach { symbol ->
                            append("  $symbol\n")
                        }
                    }
                    globalFileManager?.write(entry)
                    globalFileManager?.close()
                } catch (_: Exception) {
                    // Best-effort
                }
                Unit
            }
        )
    }
}

/** Timestamp helper callable from static context. */
private fun crashTimestamp(): String {
    val now = Clock.System.now()
    val local = now.toLocalDateTime(TimeZone.currentSystemDefault())
    val millis = (now.toEpochMilliseconds() % 1000).toString().padStart(3, '0')
    return "${local.year}-${p(local.monthNumber)}-${p(local.dayOfMonth)} " +
            "${p(local.hour)}:${p(local.minute)}:${p(local.second)}.$millis"
}

private fun p(v: Int): String = v.toString().padStart(2, '0')

