package com.indusjs.logger

import android.os.Build
import android.os.Environment
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

/**
 * Android [LogFileManager]: writes to external storage.
 *
 * - API < 29: `sdcard/IndusJS/Fleet/Log/` (legacy external storage)
 * - API >= 29: `<app-external>/IndusJS/Fleet/Log/` (scoped storage via getExternalFilesDir)
 *
 * - New file per session; 2 MB max with rollover suffix `_1`, `_2`, …
 * - Uses [BufferedWriter] for efficient I/O.
 */
internal actual class LogFileManager actual constructor(
    context: PlatformContext,
    sessionTimestamp: String
) {
    private companion object {
        const val MAX_FILE_BYTES = 2L * 1024 * 1024 // 2 MB
        const val LOG_DIR = "IndusJS/Fleet/Log"
        const val FILE_PREFIX = "ijs_fleet_"
    }

    private val logDir: File
    private val baseFileName: String
    private var rolloverIndex = 0
    private var currentFile: File
    private var writer: BufferedWriter

    init {
        logDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Scoped storage (API 29+): use app-specific external directory
            // Path: /storage/emulated/0/Android/data/<pkg>/files/IndusJS/Fleet/Log/
            File(context.context.getExternalFilesDir(null), LOG_DIR)
        } else {
            // Legacy storage (API < 29): use shared external storage
            // Path: /storage/emulated/0/IndusJS/Fleet/Log/
            @Suppress("DEPRECATION")
            File(Environment.getExternalStorageDirectory(), LOG_DIR)
        }
        if (!logDir.exists()) logDir.mkdirs()

        baseFileName = "$FILE_PREFIX$sessionTimestamp"
        currentFile = File(logDir, "$baseFileName.txt")
        writer = newWriter(currentFile)
    }

    actual fun write(entry: String) {
        if (currentFile.length() + entry.toByteArray(Charsets.UTF_8).size > MAX_FILE_BYTES) {
            rollover()
        }
        writer.write(entry)
        writer.flush()
    }

    actual fun close() {
        runCatching { writer.close() }
    }

    private fun rollover() {
        runCatching { writer.close() }
        rolloverIndex++
        currentFile = File(logDir, "${baseFileName}_$rolloverIndex.txt")
        writer = newWriter(currentFile)
    }

    private fun newWriter(file: File): BufferedWriter =
        BufferedWriter(OutputStreamWriter(FileOutputStream(file, true), Charsets.UTF_8))
}

