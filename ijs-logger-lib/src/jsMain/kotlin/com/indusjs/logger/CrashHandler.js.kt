package com.indusjs.logger

/**
 * JS crash handler: installs `window.onerror` and `window.onunhandledrejection`
 * that write error info to the log file via [LogFileManager].
 *
 * Uses Kotlin `dynamic` dispatch instead of raw `js()` strings so that
 * [fileManager] is accessible from the callback closures without relying
 * on fragile mangled Kotlin function names.
 *
 * Does not suppress existing handlers — previous handlers are captured
 * and delegated to after logging.
 */
internal actual class CrashHandler actual constructor(
    private val fileManager: LogFileManager
) {
    actual fun install() {
        val window: dynamic = js("window")
        val prevOnError: dynamic = window.onerror
        val prevOnRejection: dynamic = window.onunhandledrejection
        val fm = fileManager // capture for lambda closure

        window.onerror = { message: dynamic, source: dynamic,
                           lineno: dynamic, colno: dynamic, error: dynamic ->
            try {
                val entry = buildString {
                    append("[CRASH] [ThreadID:1 | ThreadName:main] [UncaughtException]\n")
                    append("Message: $message\nSource: $source:$lineno:$colno\n")
                    if (error != null) {
                        val stack = error.stack
                        if (stack != null) append("$stack\n")
                    }
                }
                fm.write(entry)
            } catch (_: Throwable) {
                // Best-effort; must never prevent the default handler
            }
            if (prevOnError != null) {
                prevOnError(message, source, lineno, colno, error)
            }
            Unit
        }

        window.onunhandledrejection = { event: dynamic ->
            try {
                val reason = event.reason
                val entry = buildString {
                    append("[CRASH] [ThreadID:1 | ThreadName:main] [UnhandledPromiseRejection]\n")
                    append("Reason: $reason\n")
                    if (reason != null) {
                        val stack = reason.stack
                        if (stack != null) append("$stack\n")
                    }
                }
                fm.write(entry)
            } catch (_: Throwable) {
                // Best-effort
            }
            if (prevOnRejection != null) {
                prevOnRejection(event)
            }
            Unit
        }
    }
}

