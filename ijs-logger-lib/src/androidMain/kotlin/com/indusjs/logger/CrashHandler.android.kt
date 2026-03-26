package com.indusjs.logger

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Android crash handler: installs [Thread.UncaughtExceptionHandler]
 * that writes the full stack trace to the log, then delegates to the
 * previous default handler (preserving Crashlytics / Sentry chain).
 */
internal actual class CrashHandler actual constructor(
    private val fileManager: LogFileManager
) {
    actual fun install() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val ts = timestamp()
                val tid = android.os.Process.myTid()
                val tname = thread.name

                val entry = buildString {
                    append("[$ts] [CRASH  ] [ThreadID:$tid | ThreadName:$tname] [UncaughtException]\n")
                    append(throwable.stackTraceToString())
                    append('\n')
                }
                fileManager.write(entry)
                fileManager.close()
            } catch (_: Exception) {
                // Best-effort; must never prevent the default handler
            }

            // Delegate to previous handler (Crashlytics, etc.)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun timestamp(): String {
        val now = Clock.System.now()
        val local = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val millis = (now.toEpochMilliseconds() % 1000).toString().padStart(3, '0')
        return "${local.year}-${p(local.monthNumber)}-${p(local.dayOfMonth)} " +
                "${p(local.hour)}:${p(local.minute)}:${p(local.second)}.$millis"
    }

    private fun p(v: Int): String = v.toString().padStart(2, '0')
}

