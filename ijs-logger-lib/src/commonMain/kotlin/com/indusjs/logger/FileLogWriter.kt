package com.indusjs.logger

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Kermit [LogWriter] that writes formatted log entries to a platform file/store.
 *
 * Thread-safe: all file I/O guarded by [Mutex] and dispatched off-main via
 * a [Dispatchers.Default] coroutine scope.
 *
 * Note: Using `Dispatchers.Default` instead of `Dispatchers.IO` because
 * `Dispatchers.IO` is not available on JS/WasmJS targets. The [Mutex]
 * already serializes writes, and platform implementations (BufferedWriter
 * on Android, NSFileHandle on iOS, IndexedDB on Web) handle I/O efficiently.
 *
 * Log entry format:
 * ```
 * [yyyy-MM-dd HH:mm:ss.SSS] [LEVEL  ] [ThreadID:id | ThreadName:name] [Tag] Message
 * ```
 */
internal class FileLogWriter(
    private val fileManager: LogFileManager
) : LogWriter() {

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val timestamp = formatTimestamp()
        val level = severity.name.padEnd(7)
        val threadId = currentThreadId()
        val threadName = currentThreadName()

        val entry = buildString {
            append("[$timestamp] [$level] [ThreadID:$threadId | ThreadName:$threadName] [$tag] $message")
            if (throwable != null) {
                append('\n')
                append(throwable.stackTraceToString())
            }
            append('\n')
        }

        scope.launch {
            mutex.withLock {
                fileManager.write(entry)
            }
        }
    }

    private fun formatTimestamp(): String {
        val now = Clock.System.now()
        val local = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val millis = (now.toEpochMilliseconds() % 1000).toString().padStart(3, '0')
        return "${local.year}-${pad(local.monthNumber)}-${pad(local.dayOfMonth)} " +
                "${pad(local.hour)}:${pad(local.minute)}:${pad(local.second)}.$millis"
    }

    private fun pad(value: Int): String = value.toString().padStart(2, '0')
}

