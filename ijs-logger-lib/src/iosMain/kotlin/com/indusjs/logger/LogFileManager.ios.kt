package com.indusjs.logger

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.fileHandleForWritingAtPath
import platform.Foundation.writeData
import platform.Foundation.closeFile
import platform.Foundation.seekToEndOfFile

/**
 * iOS [LogFileManager]: writes to `<Documents>/IndusJS/Fleet/Log/`.
 *
 * - New file per session; 2 MB max with rollover suffix.
 * - Uses [NSFileHandle] for append-writes.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual class LogFileManager actual constructor(
    context: PlatformContext,
    sessionTimestamp: String
) {
    private companion object {
        const val MAX_FILE_BYTES = 2L * 1024 * 1024 // 2 MB
        const val LOG_DIR = "IndusJS/Fleet/Log"
        const val FILE_PREFIX = "ijs_fleet_"
    }

    private val logDirPath: String
    private val baseFileName: String
    private var rolloverIndex = 0
    private var currentFilePath: String
    private val fm = NSFileManager.defaultManager

    init {
        val docs = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).firstOrNull() as? String ?: ""

        logDirPath = "$docs/$LOG_DIR"
        if (!fm.fileExistsAtPath(logDirPath)) {
            fm.createDirectoryAtPath(
                logDirPath,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }

        baseFileName = "$FILE_PREFIX$sessionTimestamp"
        currentFilePath = "$logDirPath/$baseFileName.txt"
        ensureFileExists(currentFilePath)
    }

    actual fun write(entry: String) {
        val entryBytes = entry.encodeToByteArray().size.toLong()
        if (fileSize(currentFilePath) + entryBytes > MAX_FILE_BYTES) {
            rollover()
        }
        appendToFile(currentFilePath, entry)
    }

    actual fun close() {
        // Writes are atomic append operations; nothing to close.
    }

    private fun rollover() {
        rolloverIndex++
        currentFilePath = "$logDirPath/${baseFileName}_$rolloverIndex.txt"
        ensureFileExists(currentFilePath)
    }

    private fun ensureFileExists(path: String) {
        if (!fm.fileExistsAtPath(path)) {
            fm.createFileAtPath(path, contents = null, attributes = null)
        }
    }

    private fun appendToFile(path: String, text: String) {
        val handle = NSFileHandle.fileHandleForWritingAtPath(path) ?: return
        handle.seekToEndOfFile()
        @Suppress("CAST_NEVER_SUCCEEDS")
        val nsString = text as NSString
        val data = nsString.dataUsingEncoding(NSUTF8StringEncoding) ?: run {
            handle.closeFile()
            return
        }
        handle.writeData(data)
        handle.closeFile()
    }

    private fun fileSize(path: String): Long {
        val attrs = fm.attributesOfItemAtPath(path, error = null) ?: return 0L
        return (attrs["NSFileSize"] as? Number)?.toLong() ?: 0L
    }
}

